package org.maggus.spirit.api;

import lombok.extern.java.Log;
import org.maggus.spirit.models.Whisky;
import org.maggus.spirit.models.WhiskyCategory;
import org.maggus.spirit.services.AnblParser;
import org.maggus.spirit.services.CacheService;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

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
}