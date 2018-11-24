package com.precisionhawk.ams.webservices;

import com.precisionhawk.ams.bean.SiteInspectionSearchParams;
import com.precisionhawk.ams.domain.SiteInspection;
import com.precisionhawk.ams.webservices.WebService;
import io.swagger.oas.annotations.Operation;
import io.swagger.oas.annotations.Parameter;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * The interface of a web service providing APIs for accessing substation data.
 *
 * @author Philip A. Chapman
 */
@Path("/siteInspection")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface SiteInspectionWebService extends WebService {
    
    @PUT
    @Operation(summary = "Create a new site inspection record", description = "Creates a new site inspection.  If unique ID is not populated, it will be populated in the returned object.")
    SiteInspection create(
            @Parameter(required = true) @HeaderParam("Authorization") String authToken,
            SiteInspection subStation
    );
    
    @GET
    @Path("/{inspectionId}")
    @Operation(summary = "Get site inspection By ID", description = "Gets site inspection by unique ID")
    SiteInspection retrieve(
            @Parameter(required = true) @HeaderParam("Authorization") String authToken,
            @PathParam("inspectionId") String id);
    
    @POST
    @Path("/search")
    @Operation(summary = "Search site inspections", description = "Get a list of site inspections by search criteria.")
    List<SiteInspection> search(
            @Parameter(required = true) @HeaderParam("Authorization") String authToken,
            SiteInspectionSearchParams searchParams);
    
    @POST
    @Operation(summary = "Updates a site inspection.", description = "Updates an existing site inspection record.")
    void update(
            @Parameter(required = true) @HeaderParam("Authorization") String authToken,
            SiteInspection inspection
    );    
}
