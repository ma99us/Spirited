package org.maggus.spirit.services;

import lombok.extern.java.Log;
import org.maggus.spirit.models.*;

import javax.ejb.Stateful;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.logging.Level;

@Stateful
@Log
public class CacheService {

    private final long CACHE_TIMEOUT = 24 * 60 * 60 * 1000;  // invalidate cache after that time

    @PersistenceContext(unitName = "spirited-test", type = PersistenceContextType.EXTENDED)
    private EntityManager em;

    @Inject
    private AnblParser anblParser;

    @Inject
    private DistillerParser dstlrParser;

    @Inject
    private WhiskyService whiskyService;

    @Inject
    private WarehouseService warehouseService;

    @Inject
    private WhiskyCategoryService whiskyCategoryService;

    @Inject
    private FlavorProfileService flavorProfileService;

    public static enum CacheOperation {
        NO_CACHE,
        CACHE_IF_NEEDED,
        RE_CACHE
    }

    public final String CACHE_UPDATE = "CACHE_UPDATE";

    public synchronized void rebuildAllProductsCategoriesCache(boolean deep) throws Exception {
        rebuildProductsCategoriesCache(deep,
                Locators.SpiritType.WHISKY,
                Locators.SpiritType.BEER,
                Locators.SpiritType.RUM,
                Locators.SpiritType.TEQUILA,
                Locators.SpiritType.GIN,
                Locators.SpiritType.CIDER,
                Locators.SpiritType.BRANDY,
                Locators.SpiritType.VODKA
                //TODO: add others here
        );
    }

    public synchronized void rebuildProductsCategoriesCache(boolean deep, Locators.SpiritType... types) throws Exception {
//        try {
//            em.createNativeQuery("SET FOREIGN_KEY_CHECKS = 1").executeUpdate();

//        if(true){
////            //whiskyCategoryService.deleteAllWhiskyCategories();
////            //whiskyService.deleteAllWhisky();
//            whiskyService.clearQuantities();
////            //warehouseService.deleteAllWarehouses();
//            return;
//        }
        log.info("*** Rebuilding Products Categories cache; deep=" + deep + ", types: " + Arrays.toString(types));
        ForkJoinPool commonPool = ForkJoinPool.commonPool();
        log.info("Trying to use Parallelism of " + commonPool.getParallelism());
        final long t0 = System.currentTimeMillis();

        // load all Products Categories now
        List<Whisky> whiskies = Collections.synchronizedList(new ArrayList<>());
        List<WhiskyCategory> categories = anblParser.buildProductsCategories(types);
        categories.parallelStream().forEach(wc -> {
            whiskies.addAll(loadProductCategory(wc));
        });

        // persist categories first
        for (WhiskyCategory wc : categories) {
            WhiskyCategory wcEntity = whiskyCategoryService.getWhiskyCategoryByName(wc.getName());
            if(wcEntity != null){
                wcEntity.mergeFrom(wc);
            }
            else{
                wcEntity = wc;
            }
            whiskyCategoryService.persistWhiskyCategory(wcEntity);
        }
        final long t1 = System.currentTimeMillis();
        log.info("Found " + whiskies.size() + " products from " + categories.size() + " categories in " + (t1 - t0) / 1000 + " seconds.");

        //cacheAllFlavorProfiles();
        // load all Products details
        if (whiskies.size() > 0) {
            if (deep) {
                log.info("Loading details for " + whiskies.size() + " products...");
                whiskies.parallelStream().forEach(w -> {
                    loadProductDetails(w);
                    if (Locators.SpiritType.isWhisky(w.getType())) {
                        findFlavorProfileForWhisky(w);
                    }
                });
            }
            // get rid of duplicates
            HashSet<Whisky> whiskiesCache = new HashSet<>(whiskies);

            // delete it ALL!?
            //log.info("*** preparing database for the fresh cache data");
            //whiskyService.deleteAllWhisky();
            //whiskyService.clearQuantities();
            //warehouseService.deleteAllWarehouses();

            // persist all products
            final long t2 = System.currentTimeMillis();
            log.info("Products loaded in " + (t2 - t1) / 1000 + " seconds - " + (t2 - t1) / whiskies.size() + "ms per product.");     // #DEBUG
            log.info("Caching " + whiskiesCache.size() + " new products into DB...");
            for (Whisky w : whiskiesCache) {
                //System.out.print(".");
                updateWhiskyCache(w);
            }

            // store meta information about the cached types
            final long t3 = System.currentTimeMillis();
            log.info("Products cached in " + (t3 - t2) / 1000 + " seconds - " + (t3 - t2) / whiskies.size() + "ms per product.");     // #DEBUG

            WhiskyCategory cacheUpdate = new WhiskyCategory(CACHE_UPDATE, null, t3, t3 - t0);
            cacheUpdate.setType(Arrays.toString(types));
            cacheUpdate.setCountry(String.format("%d", whiskiesCache.size()));
            WhiskyCategory cacheUpdateEntity = whiskyCategoryService.getWhiskyCategoryByName(cacheUpdate.getName());
            if (cacheUpdateEntity != null) {
                cacheUpdateEntity.mergeFrom(cacheUpdate);
            } else {
                cacheUpdateEntity = cacheUpdate;
            }
            log.info("Finalizing cache status: " + cacheUpdateEntity + "...");
            whiskyCategoryService.persistWhiskyCategory(cacheUpdateEntity);

            em.flush();
            log.info("*** All Done for " + whiskiesCache.size() + " products, in " + (t3 - t0) / 1000 + " seconds in total.");
        } else {
            log.warning("! No products to cache!?");
        }
//        } finally{
//            em.createNativeQuery("SET FOREIGN_KEY_CHECKS = 1").executeUpdate();
//        }
    }

    private List<Whisky> loadProductCategory(WhiskyCategory wc) {
        log.info("caching category for: " + wc);
        long t0 = System.currentTimeMillis();
        List<Whisky> whiskies = anblParser.loadProductCategoryPage(wc.getCacheExternalUrl());
        if (whiskies.isEmpty()) {
            log.warning("no data was loaded from: " + wc.getCacheExternalUrl());
            return whiskies;
        }
        for (Whisky w : whiskies) {
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
        long t0 = System.currentTimeMillis();
        if(!anblParser.loadProductPage(whisky)){
            log.warning("no data was loaded from: " + whisky.getCacheExternalUrl());
            return;
        }
        long now = System.currentTimeMillis();
        whisky.setCacheLastUpdatedMs(now);
        whisky.setCacheSpentMs(now - t0);
    }

    private Whisky updateWhiskyCache(Whisky whisky) throws Exception {
        //log.warning("* updating cache for Whisky: " + whisky);      // #TEST
        Whisky whiskyEntity = null; // try to locate and update existing Whisky entity
        if(whisky.getId() <= 0) {
            // re-map 'new' Whisky with existing entity
            whiskyEntity = whiskyService.findWhiskyByCode(whisky.getProductCode()); // lookup by product code
            if (whiskyEntity == null) {
                List<Whisky> candidates = whiskyService.findWhisky(whisky.getName(), whisky.getUnitVolumeMl(), whisky.getCountry());  // fallback to lookup by name and other params
                if (candidates != null && candidates.size() == 1) {
                    whiskyEntity = candidates.get(0);
                }
            }
            if (whiskyEntity != null) {
                whiskyEntity.mergeFrom(whisky);   // update existing whisky with new info (merge)
            }
        }
        if(whiskyEntity == null){
            whiskyEntity = whisky; // use 'new' whisky as is
        }
        //log.warning("* whiskyEntity=" + whiskyEntity);      // #TEST
        // see if new Warehouses entities needs to be added
        for (WarehouseQuantity wq : whiskyEntity.getQuantities()) {
            //log.warning("? quiring Warehouse: " + wq.getName());      // #TEST
            Warehouse wh = warehouseService.getWarehouseByName(wq.getName());
            if (wh != null) {
                //log.warning("= old Warehouse: " + wh);      // #TEST
            } else {
                wh = wq.buildWarehouse();
                //log.warning("+ new Warehouse: " + wh);      // #TEST
                wh = warehouseService.persistWarehouse(wh);
                //log.warning("+= persisted Warehouse: " + wh);      // #TEST
                em.flush(); // only flush if we added new Warehouse
            }
        }
        // re-map FlavorProfile entry
//        FlavorProfile fp = whiskyEntity.getFlavorProfile();
//        if(fp != null){
//            FlavorProfile fpCache = flavorProfileService.getFlavorProfileByName(fp.getName());
//            if(fpCache != null){
//                // update?
//            } else{
//                fp = flavorProfileService.persistFlavorProfile(fp);
//                em.flush();
//            }
//        }

        //log.warning("+ persisting Whisky: " + whiskyEntity);      // #TEST
        return whiskyService.persistWhisky(whiskyEntity);
        //em.flush();
    }

    public void cacheAllFlavorProfiles() {
        try {
            log.info("* Caching all Flavor Profiles...");
            List<FlavorProfile> flavors = dstlrParser.loadAllProducts();
            for (FlavorProfile fp : flavors) {
                FlavorProfile fpEntity = flavorProfileService.getFlavorProfileByName(fp.getName());
                if (fpEntity == null) {
                    // new one, persist it
                    flavorProfileService.persistFlavorProfile(fp);
                }
            }
            em.flush();
            log.info("* Caching all Flavor Profiles. Done.");
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Failed to cache all Products for Flavor Profiles", ex);
        }
    }

    public boolean validateCache(Whisky whisky, CacheOperation reCache) throws Exception {
        boolean isCacheInvalid = whisky == null || whisky.getCacheLastUpdatedMs() == null || whisky.getCacheLastUpdatedMs() < (System.currentTimeMillis() - CACHE_TIMEOUT);
        if (whisky != null && (reCache == CacheOperation.RE_CACHE || (reCache == CacheOperation.CACHE_IF_NEEDED && isCacheInvalid))) {
            log.info("* updating cache for Whisky: " + whisky + "; reCache=" + reCache + "; isCacheInvalid=" + isCacheInvalid + "; whisky.getCacheLastUpdatedMs: " + whisky.getCacheLastUpdatedMs() + " < " + (System.currentTimeMillis() - CACHE_TIMEOUT));      // #TEST
            loadProductDetails(whisky);
            if (Locators.SpiritType.isWhisky(whisky.getType())) {
                findFlavorProfileForWhisky(whisky);
            }
            updateWhiskyCache(whisky);
            em.flush();
        }
        return isCacheInvalid;
    }

    private boolean findFlavorProfileForWhisky(Whisky whisky) {
        try {
            //log.info("= Matching Flavor Profile for Whisky: \"" + whisky.getName() + "\"");
            long t0 = System.currentTimeMillis();
            FlavorProfile fp = dstlrParser.fuzzySearchFlavorProfile(whisky);
            if (fp == null) {
                log.warning("! No FP Product match for: \"" + whisky.getName() + "\" " + whisky.getType() + ", " + whisky.getRegion() + ", " + whisky.getCountry());
                return false;
            }
            dstlrParser.loadFlavorProfile(fp);
            if (fp.getFlavors() == null || fp.getFlavors().isEmpty()) {
                log.warning("! No FP data for: \"" + whisky.getName() + "\" => \"" + fp.getName() + "\"");
                return false;
            }
            long now = System.currentTimeMillis();
            fp.setCacheLastUpdatedMs(now);
            fp.setCacheSpentMs(now - t0);
            whisky.setFlavorProfile(fp);
            //log.info("? Found Flavor Profile for Whisky: \"" + whisky.getName() + "\" => \"" + fp.getName() + "\" - " + fp.getFlavors());
            return true;
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Failed to find a Flavor Profile for Whisky: " + whisky.getName(), ex);
            return false;
        }
    }

    public WhiskyService getWhiskyService() {
        return whiskyService;
    }

    public WhiskyCategoryService getWhiskyCategoryService() {
        return whiskyCategoryService;
    }

    public WarehouseService getWarehouseService() {
        return warehouseService;
    }
}
