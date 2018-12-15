package org.maggus.spirit.api;

import lombok.extern.java.Log;
import org.maggus.spirit.models.Whisky;
import org.maggus.spirit.services.CacheService;
import org.maggus.spirit.services.WhiskyTestService;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("/whisky")
@Produces({MediaType.APPLICATION_JSON})
@Consumes({MediaType.APPLICATION_JSON})
@Log
public class WhiskyApi {
    @Inject
    private WhiskyTestService whiskyService;

    @Inject
    private CacheService cacheService;

    @Path("/rebuild")
    @GET
    public Response rebuildAllWhiskyData(@QueryParam("full") @DefaultValue("false") boolean full) {
        try {
            cacheService.rebuildProductsCategoriesCache(full);
            return Response.ok();
        } catch (Exception e) {
            return Response.fail(e);
        }
    }

    @GET
    public Response getAllWhisky(@QueryParam("resultsPerPage") @DefaultValue("100") int resultsPerPage,
                                 @QueryParam("pageNumber") @DefaultValue("1") int pageNumber,
                                 @QueryParam("sortBy") @DefaultValue("name") String sortBy) {
        try {
            QueryMetadata metaData = new QueryMetadata(resultsPerPage, pageNumber, sortBy, null);
            Response resp = Response.ok(whiskyService.getAllWhisky(metaData));
            resp.setMetaData(metaData);
            return resp;
        } catch (Exception e) {
            return Response.fail(e);
        }
    }

    @Path("/{id}")
    @GET
    public Response getWhisky(@PathParam("id") long id) {
        try {
            Whisky whisky = whiskyService.getWhiskyById(id);
            cacheService.validateCache(whisky, CacheService.CacheOperation.CACHE_IF_NEEDED);
            return Response.ok(whisky);
        } catch (Exception e) {
            return Response.fail(e);
        }
    }

    @PUT
    public Response createWhisky(Whisky whisky) {
        try {
            if (whiskyService.getWhiskyById(whisky.getId()) != null) {
                throw new IllegalArgumentException("Such ID " + whisky.getId() + " already exists");
            }
            whisky.setId(0);
            whisky = whiskyService.persistWhisky(whisky);
            return Response.ok(whisky);
        } catch (Exception e) {
            return Response.fail(e);
        }
    }

    @Path("/{id}")
    @POST
    public Response updateWhisky(@PathParam("id") long id, Whisky whisky) {
        try {
            if (whiskyService.getWhiskyById(id) == null) {
                throw new IllegalArgumentException("No such ID " + id + " exists");
            }
            whisky.setId(id);
            whisky = whiskyService.persistWhisky(whisky);
            return Response.ok(whisky);
        } catch (Exception e) {
            return Response.fail(e);
        }
    }

    @Path("/{id}")
    @DELETE
    public Response deleteWhisky(@PathParam("id") long id) {
        try {
            Whisky whisky = whiskyService.getWhiskyById(id);
            whiskyService.deleteWhisky(whisky);
            return Response.ok();
        } catch (Exception e) {
            return Response.fail(e);
        }
    }

}