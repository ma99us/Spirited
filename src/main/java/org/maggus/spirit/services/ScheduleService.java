package org.maggus.spirit.services;

import lombok.extern.java.Log;

import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.inject.Inject;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;

@Singleton
@Log
public class ScheduleService {

    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd '  ' hh:mm:ss");

    @Inject
    private CacheService cacheService;

    @Schedule(hour = "23", minute = "59", second = "59")
    public synchronized void reBuildTheWholeCache() {
        try {
            log.warning("=== Scheduled cache re-build started at " + sdf.format(new Date()) + " ===");
            cacheService.rebuildProductsCategoriesCache(true);
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
