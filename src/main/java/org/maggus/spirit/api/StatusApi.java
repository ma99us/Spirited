package org.maggus.spirit.api;

import lombok.extern.java.Log;
import org.maggus.spirit.models.Whisky;
import org.maggus.spirit.models.WhiskyCategory;
import org.maggus.spirit.services.AnblParser;
import org.maggus.spirit.services.CacheService;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("/status")
@Produces({MediaType.APPLICATION_JSON})
@Consumes({MediaType.APPLICATION_JSON})
@Log
public class StatusApi {

    @Inject
    private CacheService cacheService;

    @GET
    public Response getCacheStatus() {
        try {
            WhiskyCategory wc = cacheService.getWhiskyCategoryService().getWhiskyCategoryByName(AnblParser.CacheUrls.BASE_URL.name());
            return Response.ok(wc);
        } catch (Exception e) {
            return Response.fail(e);
        }
    }

}