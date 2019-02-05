package org.maggus.spirit.api;

import lombok.extern.java.Log;
import org.maggus.spirit.models.Whisky;
import org.maggus.spirit.models.WhiskyCategory;
import org.maggus.spirit.services.AnblParser;
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
            WhiskyCategory wc = cacheService.getWhiskyCategoryService().getWhiskyCategoryByName(AnblParser.CacheUrls.BASE_URL.name());
            List<Whisky> allWhiskies = cacheService.getWhiskyService().getAllWhiskies(new QueryMetadata());
            int wCount = allWhiskies.size();
            int fpCount = (int) allWhiskies.stream().filter(w -> w.getFlavorProfile() != null).count();
            int fpPerc = (int) ((double) fpCount / wCount * 100);
            wc.setCountry(String.format("%d", wCount));
            wc.setRegion(String.format("%d%%", fpPerc));
            return Response.ok(wc);
        } catch (Exception e) {
            return Response.fail(e);
        }
    }

    @Path("/rebuild")
    @GET
    public Response rebuildCache(@QueryParam("full") @DefaultValue("false") boolean full) {
        try {
            cacheService.rebuildProductsCategoriesCache(full);
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