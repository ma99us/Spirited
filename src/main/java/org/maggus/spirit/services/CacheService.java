package org.maggus.spirit.services;

import lombok.extern.java.Log;
import org.maggus.spirit.models.Warehouse;
import org.maggus.spirit.models.Whisky;
import org.maggus.spirit.models.WhiskyCategory;

import javax.ejb.Stateful;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import javax.persistence.TypedQuery;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

@Stateful
@Log
public class CacheService {

    private final long CACHE_TIMEOUT = 1 * 60 * 60 * 1000;  // invalidate cache after that time

    @PersistenceContext(unitName = "spirited-test", type = PersistenceContextType.EXTENDED)
    private EntityManager em;

    @Inject
    private AnblParser anblParser;

    @Inject
    private WhiskyTestService whiskyService;

    @Inject
    private WarehouseService warehouseService;

    public WhiskyCategory getCacheStatus(String url) throws Exception {
        TypedQuery<WhiskyCategory> q = em.createQuery("select c from WhiskyCategory c where c.cacheExternalUrl = :URL", WhiskyCategory.class);
        q.setParameter("URL", url);
        List<WhiskyCategory> res = q.getResultList();
        return !res.isEmpty() ? res.get(0) : null;
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void insertCacheStatus(WhiskyCategory cs) throws Exception {
        em.persist(cs);
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public WhiskyCategory updateCacheStatus(WhiskyCategory cs) throws Exception {
        return em.merge(cs);
    }

    private void updateCacheTs(String tag, String url, Long startedMs) {
        try {
            long now = System.currentTimeMillis();
            Long spentMs = startedMs != null ? (now - startedMs) : null;
            WhiskyCategory cs = getCacheStatus(url);
            if (cs == null) {
                insertCacheStatus(new WhiskyCategory(tag, url, now, spentMs));
            } else {
                cs.setCacheLastUpdatedMs(now);
                cs.setCacheSpentMs(spentMs);
                updateCacheStatus(cs);
            }

        } catch (Exception e) {
            log.log(Level.SEVERE, "Failed to update Cache Status", e);
        }
    }

    public void rebuildProductsCategoriesCache(boolean full) throws Exception {
        long t0 = System.currentTimeMillis();
        List<Whisky> whiskies = anblParser.loadProductsCategories();
        updateCacheTs(AnblParser.CacheUrls.BASE_URL.name(), AnblParser.CacheUrls.BASE_URL.getUrl(), t0);
        if (whiskies.size() > 0) {
            // delete it ALL!
            whiskyService.deleteAllWhisky();
            warehouseService.deleteAllWarehouses();
            log.info("adding " + whiskies.size() + " new products into DB");
            for (Whisky w : whiskies) {
                whiskyService.persistWhisky(w);
                if (full) {
                    rebuildProductCache(w);   //FIXME this is too slow
                }
            }
            log.info("Done adding products");
        } else {
            log.warning("! No products to cache!?");
        }
    }

    public void rebuildProductCache(Whisky whisky) throws Exception {
        if (whisky.getCacheExternalUrl() == null) {
            throw new NullPointerException("cacheExternalUrl can not be null");
        }
        long t0 = System.currentTimeMillis();
        anblParser.loadProduct(whisky);
        long now = System.currentTimeMillis();
        whisky.setCacheLastUpdatedMs(now);
        whisky.setCacheSpentMs(now - t0);
        // re-map to existing warehouses, and persist new once
        for (Map.Entry<Warehouse, Integer> entry : whisky.getQuantities().entrySet()) {
            Warehouse wh = entry.getKey();
            Warehouse existingWh = warehouseService.getWarehouseByName(wh.getName());
            if (existingWh != null) {
                wh.setId(existingWh.getId());
                log.info("= old Warehouse: " + wh);
            }
            if (wh.getId() <= 0) {
                log.info("+ new Warehouse: " + wh);
                warehouseService.persistWarehouse(wh);
            }
        }
        whiskyService.persistWhisky(whisky);
    }

    public boolean validateCache(Whisky whisky, boolean reCache) throws Exception {
        if (whisky == null || whisky.getCacheLastUpdatedMs() == null || whisky.getCacheLastUpdatedMs() < System.currentTimeMillis() - CACHE_TIMEOUT) {
            if (whisky != null && reCache) {
                rebuildProductCache(whisky);
            }
            return false;
        } else {
            return true;
        }
    }
}
