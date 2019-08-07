package org.maggus.spirit.services;

import lombok.extern.java.Log;
import org.maggus.spirit.api.QueryMetadata;
import org.maggus.spirit.models.Whisky;
import org.maggus.spirit.models.WhiskyDiff;

import javax.ejb.Stateful;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.*;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Stateful
@Log
public class WhiskyService {

    @PersistenceContext(unitName = "spirited-test", type = PersistenceContextType.EXTENDED)
    private EntityManager em;
    private List<Whisky> cachedSpirits;

    //@Transactional(value=Transactional.TxType.REQUIRES_NEW)
    public List<Whisky> getAllWhiskies(QueryMetadata metaData) throws Exception {
        TypedQuery<Whisky> q = em.createQuery("select w from Whisky w " + getSafeOrderByClause(Whisky.class, metaData.getSortBy()), Whisky.class);
        List<Whisky> resultList = q.getResultList();
        int totalSize = resultList.size();
        metaData.setTotalResults(totalSize);    // report back total number of unfiltered results
        if (totalSize > 0 && metaData.getResultsPerPage() != null && metaData.getPageNumber() != null) {
            int idx0 = metaData.getResultsPerPage() * (metaData.getPageNumber() - 1);
            idx0 = idx0 < 0 ? 0 : idx0;
            idx0 = idx0 >= totalSize ? totalSize - 1 : idx0;
            int idx1 = idx0 + metaData.getResultsPerPage();
            idx1 = idx1 < 0 ? 0 : idx1;
            idx1 = idx1 > totalSize ? totalSize : idx1;
            resultList = resultList.subList(idx0, idx1);
        }
        return resultList.stream().map(w -> {
            em.detach(w);
            return w.clone();
        }).collect(Collectors.toList());
//        resultList.forEach(w -> {
//            em.detach(w);
//        });
//        return resultList;
    }

    public Whisky getWhisky(long id) throws Exception {
        return em.find(Whisky.class, id);
    }

    public Whisky findWhiskyByCode(String productCode) throws Exception {
        try {
            if (productCode == null || productCode.isEmpty()) {
                throw new NoResultException("productCode can not be null");
            }
            TypedQuery<Whisky> q = em.createQuery("select w from Whisky w where w.productCode=:productCode", Whisky.class);
            q.setParameter("productCode", productCode);
            return q.getSingleResult();
        } catch (NoResultException ex) {
            return null;
        } catch (NonUniqueResultException ex) {
            log.warning("! multiple products with the same product code = \"" + productCode + "\". This should not happen!");
            return null;
        }
    }

    public List<Whisky> fuzzyFindWhiskiesByName(String name) throws Exception {
        if (name == null || name.isEmpty()) {
            throw new NoResultException("name can not be null");
        }
        if (cachedSpirits == null) {
            synchronized (this) {
                if (cachedSpirits == null) {
                    QueryMetadata meta = new QueryMetadata();
                    meta.setSortBy("name");
                    cachedSpirits = getAllWhiskies(meta);
                }
            }
        }
        Map<Double, Whisky> candidates = cachedSpirits.parallelStream().map(w -> {
            WhiskyDiff diff = new WhiskyDiff(w);
            diff.setStdDeviation(AbstractParser.fuzzyMatchNames(w.getName(), name));
            return diff;
        }).collect(Collectors.toMap(WhiskyDiff::getStdDeviation, WhiskyDiff::getCandidate, (oldValue, newValue) -> oldValue, TreeMap::new));
        int maxCandidates = 10; // return at max that many candidates
        ArrayList<Whisky> whiskies = new ArrayList<>(maxCandidates);
        for (Map.Entry<Double, Whisky> entry : candidates.entrySet()) {
            if (maxCandidates-- <= 0) {
                break;
            }
            whiskies.add(entry.getValue());
        }
        return whiskies;
    }

    private void resetCache() {
        if (cachedSpirits != null) {
            synchronized (this) {
                cachedSpirits = null;
            }
        }
    }

    public List<Whisky> findWhisky(String name, Integer volumeMl, String country) throws Exception {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Whisky> q = cb.createQuery(Whisky.class);
        Root<Whisky> c = q.from(Whisky.class);
        List<Predicate> predicates = new ArrayList<Predicate>();
        predicates.add(cb.equal(c.get("name"), name));
        if (volumeMl != null) {
            predicates.add(cb.equal(c.get("unitVolumeMl"), volumeMl));
        }
        if (country != null) {
            predicates.add(cb.equal(c.get("country"), country));
        }
        q.select(c).where(predicates.toArray(new Predicate[]{}));
        TypedQuery<Whisky> query = em.createQuery(q);
        return query.getResultList();
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public synchronized Whisky persistWhisky(Whisky whisky) throws Exception {
        resetCache();
        if (whisky.getId() > 0) {
            return em.merge(whisky);
        } else {
            em.persist(whisky);
            return whisky;
        }
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public synchronized void deleteWhisky(Whisky whisky) throws Exception {
        resetCache();
        em.remove(whisky);
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public synchronized void deleteAllWhisky() throws Exception {
        resetCache();
        log.warning("! Clearing the whole DB Whisky table!");
        Query q = em.createQuery("DELETE FROM Whisky");
        q.executeUpdate();
        em.flush();
    }

    // delete all quantities tables
    public void clearQuantities() {
//        log.warning("! Clearing the Warehouse Quantities table!");
//        Query q = em.createNativeQuery("delete from whisky_warehousequantity");
//        q.executeUpdate();

        Query q = em.createNativeQuery("delete from warehousequantity");
        q.executeUpdate();

        em.flush();
    }

    public static String getSafeOrderByClause(Class clazz, String sortBy) {
        try {
            if (sortBy != null && !sortBy.isEmpty()) {
                // check ordering direction
                String dirStr = "ASC";
                if (sortBy.startsWith("-")) {
                    sortBy = sortBy.substring(1);
                    dirStr = "DESC";
                }
                // check that field name is valid
                if (!findClassField(clazz, sortBy)) {
                    throw new NoSuchFieldException(sortBy); // paranoia
                }
                String ent = clazz.getSimpleName().substring(0, 1).toLowerCase();
                return "ORDER BY " + ent + "." + sortBy + " " + dirStr;
            } else {
                return "";
            }
        } catch (NoSuchFieldException e) {
            log.warning("Can not order; no such field: \"" + sortBy + "\" in entity " + clazz.getSimpleName());
            return "";
        }
    }

    private static boolean findClassField(Class clazz, String field) {
        if (clazz == null) {
            return false;
        }
        try {
            if (clazz.getDeclaredField(field) != null) {
                return true;
            }
        } catch (NoSuchFieldException e) {
            // ignore
        }
        return findClassField(clazz.getSuperclass(), field);
    }
}
