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

    @Inject
    private CacheService cacheService;

    @Schedule(hour = "23", minute = "59", second = "59")
    //@Schedule(hour = "14", minute = "20", second = "0")
    public void reBuildTheWholeCache() {
        try {
            final SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd '  ' hh:mm:ss");
            log.warning("=== Starting scheduled cache wipe/rebuild at " + sdf.format(new Date()) + " ===");
            cacheService.rebuildProductsCategoriesCache(true);
            log.warning("=== Done scheduled job at " + sdf.format(new Date()) + " ===");
        } catch (Exception e) {
            log.log(Level.SEVERE, "Scheduled job failed!", e);
        }
    }
}
