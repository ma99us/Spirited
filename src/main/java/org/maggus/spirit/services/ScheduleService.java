package org.maggus.spirit.services;

import lombok.extern.java.Log;
import org.maggus.spirit.models.Locators;

import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Stateless;
import javax.inject.Inject;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;

@Log
@Stateless
public class ScheduleService {

    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd '  ' hh:mm:ss");

    @Inject
    private CacheService cacheService;

    @Schedule(hour = "0", minute = "0", second = "59", persistent=false)
    public synchronized void reBuildWhiskyCache() {
        try {
            log.warning("=== Scheduled cache re-build started at " + sdf.format(new Date()) + " ===");
            cacheService.rebuildProductsCategoriesCache(true, Locators.SpiritType.WHISKY);
            log.warning("=== Scheduled cache re-build done at " + sdf.format(new Date()) + " ===");
        } catch (Exception e) {
            log.log(Level.SEVERE, "Scheduled cache re-build failed!", e);
        }
    }

    @Schedule(hour = "0", minute = "30", second = "59", persistent=false)
    public synchronized void reBuildBeerCache() {
        try {
            log.warning("=== Scheduled cache re-build started at " + sdf.format(new Date()) + " ===");
            cacheService.rebuildProductsCategoriesCache(true, Locators.SpiritType.BEER);
            log.warning("=== Scheduled cache re-build done at " + sdf.format(new Date()) + " ===");
        } catch (Exception e) {
            log.log(Level.SEVERE, "Scheduled cache re-build failed!", e);
        }
    }

    @Schedule(hour = "0", minute = "45", second = "59", persistent=false)
    public synchronized void reBuildOtherSpiritsCache() {
        try {
            log.warning("=== Scheduled cache re-build started at " + sdf.format(new Date()) + " ===");
            cacheService.rebuildProductsCategoriesCache(true, Locators.SpiritType.RUM,
                    Locators.SpiritType.TEQUILA, Locators.SpiritType.GIN, Locators.SpiritType.CIDER,
                    Locators.SpiritType.BRANDY);
            log.warning("=== Scheduled cache re-build done at " + sdf.format(new Date()) + " ===");
        } catch (Exception e) {
            log.log(Level.SEVERE, "Scheduled cache re-build failed!", e);
        }
    }

    @Schedule(hour = "1-23", minute = "*/30")
    public synchronized void dbKeepAlive() {
        try {
            log.warning("=== Scheduled DB keep-alive run at " + sdf.format(new Date()) + " ===");
            cacheService.getWhiskyCategoryService().getWhiskyCategoryById(1);
            //log.warning("=== Scheduled job done at " + sdf.format(new Date()) + " ===");
            // we do not care about results
        } catch (Exception e) {
            log.log(Level.SEVERE, "Scheduled DB keep-alive failed!", e);
        }
    }

}
