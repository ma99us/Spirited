package org.maggus.spirit.services;

import lombok.extern.java.Log;
import org.maggus.spirit.api.QueryMetadata;
import org.maggus.spirit.models.Whisky;
import org.maggus.spirit.models.SpiritDiff;

import javax.ejb.Stateful;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.*;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Stateful
@Log
public class WhiskyService {

    @PersistenceContext(unitName = "spirited-test", type = PersistenceContextType.EXTENDED)
    private EntityManager em;
    private List<Whisky> cachedSpirits;

    //@Transactional(value=Transactional.TxType.REQUIRES_NEW)
    public List<Whisky> getWhiskies(String name, String type, QueryMetadata metaData) throws Exception {
        List<Whisky> whiskies = getCache();

        if(name != null && !name.isEmpty()){
            whiskies = filterByFuzzyNameMatch(whiskies, name, 0.75);
        }

        if(type != null && !type.isEmpty()) {
            whiskies = filterByTypeMatch(whiskies, type);
        }

        if (name == null || name.isEmpty()) {
            // sort only if name likeness is not specified. Otherwise name likeness is the sorting order
            if (metaData.getSortBy() != null && !metaData.getSortBy().isEmpty()) {
                whiskies = sortByField(whiskies, metaData.getSortBy());
            }
        }

        int totalSize = whiskies.size();
        metaData.setTotalResults(totalSize);    // report back total number of unfiltered results
        if (totalSize > 0 && metaData.getResultsPerPage() != null && metaData.getPageNumber() != null) {
            int idx0 = metaData.getResultsPerPage() * (metaData.getPageNumber() - 1);
            idx0 = idx0 < 0 ? 0 : idx0;
            idx0 = idx0 >= totalSize ? totalSize - 1 : idx0;
            int idx1 = idx0 + metaData.getResultsPerPage();
            idx1 = idx1 < 0 ? 0 : idx1;
            idx1 = idx1 > totalSize ? totalSize : idx1;
            whiskies = whiskies.subList(idx0, idx1);
        }

        return whiskies.stream().map(w -> {
            return w.clone();   // detach from cache
        }).collect(Collectors.toList());
    }

    public Whisky getWhisky(long id) throws Exception {
        return em.find(Whisky.class, id);
    }

    public Whisky findWhiskyByName(String name) throws Exception {
        try {
            if (name == null || name.isEmpty()) {
                throw new NoResultException("Name can not be null");
            }
            TypedQuery<Whisky> q = em.createQuery("select w from Whisky w where w.name=:name", Whisky.class);
            q.setParameter("name", name);
            List<Whisky> resultList = q.getResultList();
            return resultList != null && !resultList.isEmpty() ? resultList.get(0) : null;
        } catch (NoResultException ex) {
            return null;
        }
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
            try {
                TypedQuery<Whisky> q = em.createQuery("select w from Whisky w where w.productCode LIKE :productCode", Whisky.class);
                q.setParameter("productCode", "%" + productCode + "%");
                return q.getSingleResult();
            }
            catch (Exception ex1){
                log.warning("No such product; code = \"" + productCode + "\"; - " + ex1.getMessage());
                return null;
            }
        } catch (NonUniqueResultException ex) {
            log.warning("! multiple products with the same product code = \"" + productCode + "\". This should not happen!");
            return null;
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

    private List<Whisky> filterByFuzzyNameMatch(List<Whisky> whiskies, String name, double maxDeviation) throws Exception {
        if (name == null || name.isEmpty()) {
            throw new NoResultException("Name must be specified");
        }
        Map<Double, Whisky> candidates = whiskies.parallelStream().map(w -> {
            SpiritDiff diff = new SpiritDiff(w);
            diff.setStdDeviation(AbstractParser.fuzzyMatchNames(w.getName(), name));
            return diff;
        })
                .filter(diff -> diff.getStdDeviation() <= maxDeviation)
                .collect(Collectors.toMap(SpiritDiff::getStdDeviation, SpiritDiff::getCandidate, (oldValue, newValue) -> oldValue, TreeMap::new));
        return candidates.entrySet().stream().map(e -> {
            return e.getValue();
        }).collect(Collectors.toList());
    }

    private List<Whisky> filterByTypeMatch(List<Whisky> whiskies, String type) throws Exception {
        if (type == null || type.isEmpty()) {
            throw new NoResultException("Type must be specified");
        }
        return whiskies.parallelStream().filter(w -> {
            String types = w.getType();
            types = types != null ? types.toUpperCase() : types;
            return types != null && types.contains(type.toUpperCase());
        }).collect((Collectors.toList()));
    }

    private List<Whisky> sortByField(List<Whisky> whiskies, String sortBy) throws Exception {
        if (sortBy == null || sortBy.isEmpty()) {
            throw new NoResultException("Sort by field must be specified");
        }
        // check ordering direction
        boolean asc = true;
        if (sortBy.startsWith("-")) {
            sortBy = sortBy.substring(1);
            asc = false;
        }
        // check that field name is valid
//        if (!findClassField(Whisky.class, sortBy)) {
//            throw new NoSuchFieldException(sortBy); // paranoia
//        }

        if ("name".equalsIgnoreCase(sortBy)) {
            whiskies = whiskies.stream()
                    .sorted(Comparator.comparing(Whisky::getName, asc ? String.CASE_INSENSITIVE_ORDER : Collections.reverseOrder(String.CASE_INSENSITIVE_ORDER)))
                    .collect(Collectors.toList());
        } else if ("price".equalsIgnoreCase(sortBy)) {
            final Comparator<Whisky> priceComparator = (w1, w2) -> {
                final BigDecimal d1 = w1.getUnitPrice();
                final BigDecimal d2 = w2.getUnitPrice();
                if (d1 == null && d2 == null) {
                    return 0;
                } else if (d1 == null && d2 != null) {
                    return -1;
                } else if (d1 != null && d2 == null) {
                    return 1;
                } else {
                    return d1.compareTo(d2);
                }};
            whiskies = whiskies.stream()
                    .sorted(asc ? priceComparator : priceComparator.reversed())
                    .collect(Collectors.toList());
        } else if ("type".equalsIgnoreCase(sortBy)) {
            whiskies = whiskies.stream()
                    .sorted(Comparator.comparing(Whisky::getType, asc ? String.CASE_INSENSITIVE_ORDER : Collections.reverseOrder(String.CASE_INSENSITIVE_ORDER)))
                    .collect(Collectors.toList());
        }

        return whiskies;
    }

    private List<Whisky> getCache() throws Exception {
        if (cachedSpirits == null) {
            synchronized (this) {
                if (cachedSpirits == null) {
                    TypedQuery<Whisky> q = em.createQuery("select w from Whisky w ORDER BY w.name ASC", Whisky.class);
                    List<Whisky> resultList = q.getResultList();
                    cachedSpirits = resultList.stream().map(w -> {
                        em.detach(w);
                        return w.clone();
                    }).collect(Collectors.toList());
                }
            }
        }
        return cachedSpirits;
    }

    private void resetCache() {
        if (cachedSpirits != null) {
            synchronized (this) {
                cachedSpirits = null;
            }
        }
    }

    private static String getSafeOrderByClause(Class clazz, String sortBy) {
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
