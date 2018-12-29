package org.maggus.spirit.api;

import lombok.extern.java.Log;
import org.maggus.spirit.services.CacheService;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/store")
@Produces({MediaType.APPLICATION_JSON})
@Consumes({MediaType.APPLICATION_JSON})
@Log
public class StoreApi {

    @Inject
    private CacheService cacheService;

    @GET
    public Response getAllStores() {
        try {
            return Response.ok(cacheService.getWarehouseService().getAllWarehouses());
        } catch (Exception e) {
            return Response.fail(e);
        }
    }

}