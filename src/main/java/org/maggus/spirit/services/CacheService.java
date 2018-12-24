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
import java.util.stream.Collectors;

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
    private WhiskyTestService whiskyService;

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

        //cacheAllFlavorProfiles();
        // load all Products now
        if (whiskies.size() > 0) {
            if (full) {
                log.info("Loading details for " + whiskies.size() + " products...");
                whiskies.parallelStream().forEach(w -> {
                    loadProductDetails(w);
                    findFlavorProfileForWhisky(w);
                });
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
        if (whiskies == null) {
            throw new NullPointerException("no data was parsed from: " + wc.getCacheExternalUrl());
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
        if (whisky.getCacheExternalUrl() == null) {
            throw new NullPointerException("cacheExternalUrl can not be null");
        }
        long t0 = System.currentTimeMillis();
        anblParser.loadProductPage(whisky);
        long now = System.currentTimeMillis();
        whisky.setCacheLastUpdatedMs(now);
        whisky.setCacheSpentMs(now - t0);
    }

    private Whisky updateWhiskyCache(Whisky whisky) throws Exception {
        // re-map Whisky entity
        //log.warning("* updating cache for Whisky: " + whisky);      // #TEST

        Whisky cacheW = whiskyService.findWhisky(whisky.getProductCode());
        if (cacheW == null) {
            List<Whisky> matchesW = whiskyService.findWhisky(whisky.getName(), whisky.getUnitVolumeMl(), whisky.getCountry());
            if (matchesW != null && matchesW.size() == 1) {
                cacheW = matchesW.get(0);
            }
        }
        if (cacheW != null) {
            cacheW.mergeFrom(whisky);   // update existing whisky with new info
        } else {
            cacheW = whisky;    // just use new whisky as is
        }
        // re-map Warehouses entities
        for (WarehouseQuantity wq : cacheW.getQuantities()) {
            //log.warning("? quiring Warehouse: " + wq.getName());      // #TEST
            Warehouse wh = warehouseService.getWarehouseByName(wq.getName());
            if (wh != null) {
                //log.warning("= old Warehouse: " + wh);      // #TEST
            } else {
                wh = wq.buildWarehouse();
                //log.warning("+ new Warehouse: " + wh);      // #TEST
                wh = warehouseService.persistWarehouse(wh);
                em.flush();
                //log.warning("+= persisted Warehouse: " + wh);      // #TEST
            }
        }
        // re-map FlavorProfile entry
//        FlavorProfile fp = cacheW.getFlavorProfile();
//        if(fp != null){
//            FlavorProfile fpCache = flavorProfileService.getFlavorProfileByName(fp.getName());
//            if(fpCache != null){
//                // update?
//            } else{
//                fp = flavorProfileService.persistFlavorProfile(fp);
//                em.flush();
//            }
//        }

        //log.warning("+ persisting Whisky: " + cacheW);      // #TEST
        return whiskyService.persistWhisky(cacheW);
        //em.flush();
    }

    public void cacheAllFlavorProfiles() {
        try {
            log.info("* Caching all Flavor Profiles...");
            List<FlavorProfile> flavors = dstlrParser.loadAllProducts();
            for (FlavorProfile fp : flavors) {
                FlavorProfile fpCache = flavorProfileService.getFlavorProfileByName(fp.getName());
                if (fpCache == null) {
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
            updateWhiskyCache(whisky);
        }
        return isCacheInvalid;
    }

    private boolean findFlavorProfileForWhisky(Whisky whisky) {
        try {
            //log.info("= Matching Flavor Profile for Whisky: \"" + whisky.getName() + "\"");
            long t0 = System.currentTimeMillis();
            // fix ANBL whisky names to match Distiller
            String name = whisky.getName();
            name = name.replaceAll("\\s+YO", "");
            FlavorProfile fp = null;
            do {
                fp = dstlrParser.searchSingleProduct(name, whisky.getType(), whisky.getCountry(), whisky.getRegion(), Locators.Age.parse(whisky.getName())); // find on a external site
                String simName = simplifyWhiskyName(name);
                if (simName.equals(name)) {
                    break;  // search string can not be simplified anymore, we are done
                } else {
                    name = simName;
                }
            } while (fp == null);
            if (fp == null) {
                log.warning("! Can not find FP Product for : \"" + whisky.getName() + "\"");
                return false;
            }
            dstlrParser.loadFlavorProfile(fp);
            if (fp.getFlavors() == null || fp.getFlavors().isEmpty()) {
                log.warning("! No Flavor Profile in Product: \"" + whisky.getName() + "\" => \"" + fp.getName() + "\"");
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

    private String simplifyWhiskyName(String name) {
        List<String> fixed = new ArrayList<>();
        String[] tags = name.split("\\s+");
        int lastNonDigitTagIdx = 0;
        for (int i = 0; i < tags.length; i++) {
            String tag = tags[i];
            if (i == 0) {
                // always use first word as is
                fixed.add(tag);
            } else {
                String digits = tag.replaceAll("[^\\d]+", "");
                if (digits.isEmpty()) {
                    lastNonDigitTagIdx = i;
                    fixed.add(tag); // candidate for truncation
                } else {
                    fixed.add(digits);  // add only numbers
                }
            }
        }
        // finally drop last word without any numbers in it
        if (lastNonDigitTagIdx > 0 && lastNonDigitTagIdx < fixed.size()) {
            fixed.remove(lastNonDigitTagIdx);
        }
        return name = String.join(" ", fixed);
    }

    public WhiskyTestService getWhiskyService() {
        return whiskyService;
    }

    public WhiskyCategoryService getWhiskyCategoryService() {
        return whiskyCategoryService;
    }
}
