package org.maggus.spirit.api;

import lombok.extern.java.Log;
import org.maggus.spirit.models.Whisky;
import org.maggus.spirit.services.WhiskyService;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("/whisky")
@Produces({MediaType.APPLICATION_JSON})
@Consumes({MediaType.APPLICATION_JSON})
@Log
public class WhiskyApi {
    @Inject
    private WhiskyService whiskyService;

    @Path("/list")
    @GET
    public Response getAllWhisky() {
        //TODO: @QueryParam("first") @DefaultValue("0") int first, @QueryParam("max") @DefaultValue("20") int max
        try {
            return Response.ok(whiskyService.getAllWhisky());
        } catch (Exception e) {
            return Response.fail(e);
        }
    }

    @Path("/get/{id}")
    @GET
    public Response getWhisky(@PathParam("id") long id) {
        try {
            return Response.ok(whiskyService.getWhiskyById(id));
        } catch (Exception e) {
            return Response.fail(e);
        }
    }

    @Path("/create")
    @PUT
    public Response createWhisky(Whisky whisky) {
        //TODO: @QueryParam("whisky")
        try {
            if (whiskyService.getWhiskyById(whisky.getId()) != null) {
                throw new IllegalArgumentException("Such ID " + whisky.getId() + " already exists");
            }
            whiskyService.insertWhisky(whisky);
            return Response.ok(whiskyService.getWhiskyById(whisky.getId()));
        } catch (Exception e) {
            return Response.fail(e);
        }
    }

    @Path("/update")
    @POST
    public Response updateWhisky(Whisky whisky) {
        // @PathParam("id") long id, @QueryParam("author") Whisky whisky
        try {
            if (whiskyService.getWhiskyById(whisky.getId()) == null) {
                throw new IllegalArgumentException("No such ID " + whisky.getId() + " exists");
            }
            whisky = whiskyService.updateWhisky(whisky);
            return Response.ok(whisky);
        } catch (Exception e) {
            return Response.fail(e);
        }
    }

    @Path("/delete/{id}")
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