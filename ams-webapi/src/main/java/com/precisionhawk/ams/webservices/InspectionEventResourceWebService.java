package com.precisionhawk.ams.webservices;

/*
 * All rights reserved.
 */

import com.precisionhawk.ams.bean.InspectionEventResourceSearchParams;
import com.precisionhawk.ams.bean.ResourcePolygons;
import com.precisionhawk.ams.bean.ResourceSearchParams;
import com.precisionhawk.ams.domain.InspectionEventResource;
import io.swagger.oas.annotations.Operation;
import io.swagger.oas.annotations.Parameter;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * An interface implemented by web services that provide access and logic for
 * resources related to inspection events.
 *
 * @author <a href="mailto:pchapman@pcsw.us">Philip A. Chapman</a>
 */
@Path("/inspectionEventResource")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface InspectionEventResourceWebService extends WebService {

    @GET
    @Path("{inspectionEventResourceId}")
    @Operation(summary = "Get Inspection Event Resource", description = "Get an Inspection Event Resource by unique ID.")
    InspectionEventResource retrieve(
            @Parameter(required = true) @HeaderParam("Authorization") String authToken,
            @PathParam("inspectionEventResourceId") String id);

    @POST
    @Path("search")
    @Operation(summary = "Search Inspection Event Resources", description = "Searches for Inspection Event Resources based on search results.")
    List<InspectionEventResource> search(
            @Parameter(required = true) @HeaderParam("Authorization") String authToken,
            InspectionEventResourceSearchParams searchParams);

    @PUT
    @Operation(summary = "Create Inspection Event Resource", description = "Create a new Inspection Event Resource.")
    InspectionEventResource create(
            @Parameter(required = true) @HeaderParam("Authorization") String authToken,
            InspectionEventResource event);
    
    @DELETE
    @Path("{inspectionEventResourceId}")
    @Operation(summary = "Delete Inspection Event Resource", description = "Deletes a specific Inspection Event Resource.")
    void delete(
            @Parameter(required = true) @HeaderParam("Authorization") String authToken,
            @PathParam("inspectionEventResourceId") String id);
    
    @POST
    @Operation(summary = "Update Inspection Event Resource", description = "Updates an existing Inspection Event Resource.")
    void update(
            @Parameter(required = true) @HeaderParam("Authorization") String authToken,
            InspectionEventResource event);
    
    @POST
    @Path("polys")
    @Operation(summary = "Search damage indication polygons", description = "Searches the polygons highlighting damage.")
    List<ResourcePolygons> searchResourcePolygons(
            @Parameter(required = true) @HeaderParam("Authorization") String authToken,
            ResourceSearchParams searchParams);
}
