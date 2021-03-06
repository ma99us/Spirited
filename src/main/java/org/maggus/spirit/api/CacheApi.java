package org.maggus.spirit.api;

import lombok.extern.java.Log;
import org.maggus.spirit.models.Locators;
import org.maggus.spirit.models.Whisky;
import org.maggus.spirit.models.WhiskyCategory;
import org.maggus.spirit.services.CacheService;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/cache")
@Produces({MediaType.APPLICATION_JSON})
@Consumes({MediaType.APPLICATION_JSON})
@Log
public class CacheApi {

    @Inject
    private CacheService cacheService;

    @Path("/status")
    @GET
    public Response getCacheStatus() {
        try {
            List<Whisky> allWhiskies = cacheService.getWhiskyService().getWhiskies(null, null, null, null, false, new QueryMetadata());
            int wCount = allWhiskies != null ? allWhiskies.size() : 0;
            int fpCount = allWhiskies != null ? (int) allWhiskies.stream().filter(w -> w.getFlavorProfile() != null).count() : 0;
            int fpPerc = wCount > 0 ? (int) ((double) fpCount / wCount * 100) : 0;
            WhiskyCategory wc = cacheService.getWhiskyCategoryService().getWhiskyCategoryByName(cacheService.CACHE_UPDATE);
            if (wc == null) {
                String status = "Bad DB status";
                Long rebuildStarted = cacheService.getRebuildStarted();
                if (rebuildStarted != null) {
                    status += "; data re-build in progress, started " + (System.currentTimeMillis() - rebuildStarted) / 1000 + " seconds ago.";
                }
                throw new IllegalStateException(status);
            }
            wc.setCountry(String.format("%d", wCount));     // hack; use "country" to send total number of whiskies
            wc.setRegion(String.format("%d%%", fpPerc));    // hack; use "region" to send percentage of whiskies with Flavour Profiles
            return Response.ok(wc);
        } catch (Exception e) {
            return Response.fail(e);
        }
    }

    @Path("/rebuild")
    @GET
    public Response rebuildCache(@QueryParam("category") @DefaultValue("ALL") String category,
                                 @QueryParam("deep") @DefaultValue("true") boolean deep) {
        try {
            if ("ALL".equalsIgnoreCase(category)) {
                cacheService.rebuildAllProductsCategoriesCache(deep);
            } else {
                Locators.SpiritType spiritType = Locators.SpiritType.valueOf(category);
                cacheService.rebuildProductsCategoriesCache(deep, spiritType);
            }
            return Response.ok();
        } catch (Exception e) {
            return Response.fail(e);
        }
    }

    @Path("/auth")
    @GET
    public Response auth() {
        try {
            // TODO: check that HTTP basic authorization was sent
            return Response.ok();
        } catch (Exception e) {
            return Response.fail(e);
        }
    }
}