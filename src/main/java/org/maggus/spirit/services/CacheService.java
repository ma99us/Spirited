package org.maggus.spirit.services;

import lombok.extern.java.Log;
import org.maggus.spirit.models.CacheStatus;
import org.maggus.spirit.models.Whisky;

import javax.ejb.Stateful;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import javax.persistence.TypedQuery;
import java.util.List;
import java.util.logging.Level;

@Stateful
@Log
public class CacheService {

    @PersistenceContext(unitName = "spirited-test", type = PersistenceContextType.EXTENDED)
    private EntityManager em;

    @Inject
    private AnblParser anblParser;

    @Inject
    private WhiskyTestService whiskyService;

    public CacheStatus getCacheStatus(String url) throws Exception {
        TypedQuery<CacheStatus> q = em.createQuery("select c from CacheStatus c where c.externalUrl = :URL", CacheStatus.class);
        q.setParameter("URL", url);
        return q.getSingleResult();
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void insertCacheStatus(CacheStatus cs) throws Exception {
        em.persist(cs);
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public CacheStatus updateCacheStatus(CacheStatus cs) throws Exception {
        return em.merge(cs);
    }

    private void updateCacheTs(String url, Long startedMs){
        try {
            long now = System.currentTimeMillis();
            Long spentMs = startedMs != null ? (now - startedMs) : null;
            CacheStatus cs = getCacheStatus(url);
            if(cs == null){
                insertCacheStatus(new CacheStatus(url, now, spentMs));
            }
            else{
                cs.setLastUpdatedMs(now);
                cs.setSpentMs(spentMs);
                updateCacheStatus(cs);
            }

        } catch (Exception e) {
            log.log(Level.SEVERE, "Failed to update Cache Status", e);
        }
    }

    public void rebuildProductsCategoriesCache() {
        try {
            long t0 = System.currentTimeMillis();
            List<Whisky> whiskies = anblParser.loadProductsCategories();
            if (whiskies.size() > 0) {
                whiskyService.deleteAllWhisky();
                log.info("adding " + whiskies.size() + " new products");
                for (Whisky w : whiskies) {
                    whiskyService.insertWhisky(w);
                }
                log.info("Done adding products");
            }
            else{
                log.warning("! No products to cache!?");
            }
            updateCacheTs(AnblParser.BASE_URL, t0);
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Failed to cache ANBL data", ex);
        }
    }
}
