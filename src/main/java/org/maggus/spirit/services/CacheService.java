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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;
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

    public static enum CacheOperation {
        NO_CACHE,
        CACHE_IF_NEEDED,
        RE_CACHE
    }

    private WhiskyCategory getCacheStatus(String url) throws Exception {
        TypedQuery<WhiskyCategory> q = em.createQuery("select c from WhiskyCategory c where c.cacheExternalUrl = :URL", WhiskyCategory.class);
        q.setParameter("URL", url);
        List<WhiskyCategory> res = q.getResultList();
        return !res.isEmpty() ? res.get(0) : null;
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    private synchronized WhiskyCategory persistCacheStatus(WhiskyCategory cs) throws Exception {
        if (cs.getId() > 0) {
            return em.merge(cs);
        } else {
            em.persist(cs);
            return cs;
        }
    }

    private void updateWhiskyCategorCache(String tag, String url, Long startedMs) {
        try {
            long now = System.currentTimeMillis();
            Long spentMs = startedMs != null ? (now - startedMs) : null;
            WhiskyCategory cs = getCacheStatus(url);
            if (cs == null) {
                cs = new WhiskyCategory(tag, url, now, spentMs);
            }
            cs.setCacheLastUpdatedMs(now);
            cs.setCacheSpentMs(spentMs);
            persistCacheStatus(cs);

        } catch (Exception e) {
            log.log(Level.SEVERE, "Failed to update Cache Status", e);
        }
    }

    public synchronized void rebuildProductsCategoriesCache(boolean full) throws Exception {
        log.info("Rebuilding Products Categories cache; full=" + full);
        long t0 = System.currentTimeMillis();
        List<Whisky> whiskies = anblParser.loadProductsCategories();
        updateWhiskyCategorCache(AnblParser.CacheUrls.BASE_URL.name(), AnblParser.CacheUrls.BASE_URL.getUrl(), t0);
        if (whiskies.size() > 0) {
            if (full) {
                ForkJoinPool commonPool = ForkJoinPool.commonPool();
                log.info("Trying to use Parallelism of " + commonPool.getParallelism());
                whiskies.parallelStream().forEach(w -> loadProductDetails(w));
            }
            // delete it ALL!
            log.info("*** Clearing database to prepare for the fresh cache data");
            whiskyService.deleteAllWhisky();
            warehouseService.deleteAllWarehouses();
            log.info("caching " + whiskies.size() + " new products into DB");
            for (Whisky w : whiskies) {
                //System.out.print(".");
                updateWhiskyCache(w);
            }
            long dt = System.currentTimeMillis() - t0;
            log.info("Done caching products. Done in " + dt / 1000 + " seconds");
        } else {
            log.warning("! No products to cache!?");
        }
    }

    private void loadProductDetails(Whisky whisky) {
        log.info("caching details for: " + whisky);
        if (whisky.getCacheExternalUrl() == null) {
            throw new NullPointerException("cacheExternalUrl can not be null");
        }
        long t0 = System.currentTimeMillis();
        anblParser.loadProduct(whisky);
        long now = System.currentTimeMillis();
        whisky.setCacheLastUpdatedMs(now);
        whisky.setCacheSpentMs(now - t0);
    }

    private void updateWhiskyCache(Whisky whisky) throws Exception {
        // re-map to existing warehouses, and persist new once
        HashMap<Warehouse, Integer> quantities = new HashMap<>();
        for (Map.Entry<Warehouse, Integer> entry : whisky.getQuantities().entrySet()) {
            Warehouse wh = entry.getKey();
            Warehouse existingWh = warehouseService.getWarehouseByName(wh.getName());
            if (existingWh != null) {
                quantities.put(existingWh, entry.getValue());
            }
            else{
                wh = warehouseService.persistWarehouse(wh);
                quantities.put(wh, entry.getValue());
            }
        }
        whisky.setQuantities(quantities);
        whiskyService.persistWhisky(whisky);
    }

    public boolean validateCache(Whisky whisky, CacheOperation reCache) throws Exception {
        boolean isCacheInvalid = whisky == null || whisky.getCacheLastUpdatedMs() == null || whisky.getCacheLastUpdatedMs() < System.currentTimeMillis() - CACHE_TIMEOUT;
        if (whisky != null && (reCache == CacheOperation.RE_CACHE
                || (reCache == CacheOperation.CACHE_IF_NEEDED && isCacheInvalid))) {
            loadProductDetails(whisky);
            updateWhiskyCache(whisky);
        }
        return isCacheInvalid;
    }
}
