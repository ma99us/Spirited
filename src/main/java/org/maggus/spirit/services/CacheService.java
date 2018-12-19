package org.maggus.spirit.services;

import lombok.extern.java.Log;
import org.maggus.spirit.models.Warehouse;
import org.maggus.spirit.models.WarehouseQuantity;
import org.maggus.spirit.models.Whisky;
import org.maggus.spirit.models.WhiskyCategory;

import javax.ejb.Stateful;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import java.util.*;
import java.util.concurrent.ForkJoinPool;

@Stateful
@Log
public class CacheService {

    private final long CACHE_TIMEOUT = 24 * 60 * 60 * 1000;  // invalidate cache after that time

    @PersistenceContext(unitName = "spirited-test", type = PersistenceContextType.EXTENDED)
    private EntityManager em;

    @Inject
    private AnblParser anblParser;

    @Inject
    private WhiskyTestService whiskyService;

    @Inject
    private WarehouseService warehouseService;

    @Inject
    private WhiskyCategoryService whiskyCategoryService;

    public static enum CacheOperation {
        NO_CACHE,
        CACHE_IF_NEEDED,
        RE_CACHE
    }

    public synchronized void rebuildProductsCategoriesCache(boolean full) throws Exception {
//        try {
//            em.createNativeQuery("SET FOREIGN_KEY_CHECKS = 1").executeUpdate();

//        if(true){
////            //whiskyCategoryService.deleteAllWhiskyCategories();
////            //whiskyService.deleteAllWhisky();
//            whiskyService.clearQuantities();
////            //warehouseService.deleteAllWarehouses();
//            return;
//        }
            log.info("Rebuilding Products Categories cache; full=" + full);
            ForkJoinPool commonPool = ForkJoinPool.commonPool();
            log.info("Trying to use Parallelism of " + commonPool.getParallelism());
            long t0 = System.currentTimeMillis();

            // load all Products Categories now
            List<Whisky> whiskies = Collections.synchronizedList(new ArrayList<>());
            List<WhiskyCategory> categories = anblParser.buildProductsCategories();
            categories.parallelStream().forEach(wc -> {
                whiskies.addAll(loadProductCategory(wc));
            });
            whiskyCategoryService.deleteAllWhiskyCategories();
            for (WhiskyCategory wc : categories) {
                whiskyCategoryService.persistWhiskyCategory(wc);
            }
            long dt = System.currentTimeMillis() - t0;
            log.info("Found " + whiskies.size() + " products from all categories in " + dt / 1000 + " seconds.");

            // load all Products now
            if (whiskies.size() > 0) {
                if (full) {
                    log.info("Loading details for " + whiskies.size() + " products...");
                    whiskies.parallelStream().forEach(w -> loadProductDetails(w));
                }
                // get rid of duplicates
                HashSet<Whisky> whiskiesCache = new HashSet<>(whiskies);

                // delete it ALL!?
                //log.info("*** preparing database for the fresh cache data");
                //whiskyService.deleteAllWhisky();
                //whiskyService.clearQuantities();
                //warehouseService.deleteAllWarehouses();

                log.info("caching " + whiskiesCache.size() + " new products into DB");
                for (Whisky w : whiskiesCache) {
                    //System.out.print(".");
                    updateWhiskyCache(w);
                }
                long now = System.currentTimeMillis();
                dt = now - t0;
                WhiskyCategory rootCategory = new WhiskyCategory(AnblParser.CacheUrls.BASE_URL.name(), AnblParser.CacheUrls.BASE_URL.getUrl(), now, dt);
                whiskyCategoryService.persistWhiskyCategory(rootCategory);
                log.info("+ persisted ROOT category" + rootCategory);
                em.flush();
                log.info("Done caching all products in " + dt / 1000 + " seconds");
            } else {
                log.warning("! No products to cache!?");
            }
//        } finally{
//            em.createNativeQuery("SET FOREIGN_KEY_CHECKS = 1").executeUpdate();
//        }
    }

    private List<Whisky> loadProductCategory(WhiskyCategory wc) {
        log.info("caching category for: " + wc);
        if (wc.getCacheExternalUrl() == null) {
            throw new NullPointerException("cacheExternalUrl can not be null");
        }
        long t0 = System.currentTimeMillis();
        List<Whisky> whiskies = anblParser.loadProductCategoryPage(wc.getCacheExternalUrl());
        if(whiskies == null){
            throw new NullPointerException("no data was parsed from: " + wc.getCacheExternalUrl());
        }
        for(Whisky w : whiskies){
            w.setCountry(wc.getCountry());
            w.setRegion(wc.getRegion());
            w.setType(wc.getType());
        }
        long now = System.currentTimeMillis();
        wc.setCacheLastUpdatedMs(now);
        wc.setCacheSpentMs(now - t0);
        return whiskies;
    }

    private void loadProductDetails(Whisky whisky) {
        //log.info("caching details for: " + whisky);
        if (whisky.getCacheExternalUrl() == null) {
            throw new NullPointerException("cacheExternalUrl can not be null");
        }
        long t0 = System.currentTimeMillis();
        anblParser.loadProductPage(whisky);
        long now = System.currentTimeMillis();
        whisky.setCacheLastUpdatedMs(now);
        whisky.setCacheSpentMs(now - t0);
    }

    private void updateWhiskyCache(Whisky whisky) throws Exception {
        // re-map Whisky entity
        //log.warning("* updating cache for Whisky: " + whisky);      // #TEST

        Whisky cacheW = whiskyService.findWhisky(whisky.getProductCode());
        if(cacheW == null) {
            List<Whisky> matchesW = whiskyService.findWhisky(whisky.getName(), whisky.getUnitVolumeMl(), whisky.getCountry());
            if (matchesW != null && matchesW.size() == 1) {
                cacheW = matchesW.get(0);
            }
        }
        if(cacheW != null){
            cacheW.mergeFrom(whisky);
        }
        else {
            cacheW = whisky;
        }
        // re-map Warehouses entities
        for (WarehouseQuantity wq : cacheW.getQuantities()) {
            //log.warning("? quiring Warehouse: " + wq.getName());      // #TEST
            Warehouse wh = warehouseService.getWarehouseByName(wq.getName());
            if (wh != null) {
                //log.warning("= old Warehouse: " + wh);      // #TEST
            }
            else{
                wh = wq.buildWarehouse();
                //log.warning("+ new Warehouse: " + wh);      // #TEST
                wh = warehouseService.persistWarehouse(wh);
                em.flush();
                //log.warning("+= persisted Warehouse: " + wh);      // #TEST
            }
        }

        //log.warning("+ persisting Whisky: " + cacheW);      // #TEST
        whiskyService.persistWhisky(cacheW);
        //em.flush();
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

    public WhiskyTestService getWhiskyService() {
        return whiskyService;
    }

    public WhiskyCategoryService getWhiskyCategoryService() {
        return whiskyCategoryService;
    }
}
