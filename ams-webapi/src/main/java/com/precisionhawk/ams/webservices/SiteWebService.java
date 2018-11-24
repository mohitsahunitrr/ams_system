package com.precisionhawk.ams.webservices;

import com.precisionhawk.ams.bean.SiteSearchParams;
import com.precisionhawk.ams.domain.Site;
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
@Path("/site")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface SiteWebService extends WebService {
    
    @PUT
    @Operation(summary = "Create a new site record", description = "Creates a new site.  If unique ID is not populated, it will be populated in the returned object.")
    Site create(
            @Parameter(required = true) @HeaderParam("Authorization") String authToken,
            Site site
    );
    
    @GET
    @Path("/{siteId}")
    @Operation(summary = "Get site By ID", description = "Gets site by unique ID")
    Site retrieve(
            @Parameter(required = true) @HeaderParam("Authorization") String authToken,
            @PathParam("siteId") String id);
    
    @GET
    @Operation(summary = "Get all sites", description = "Gets all sites.")
    List<Site> retrieveAll(@Parameter(required = true) @HeaderParam("Authorization") String authToken);

    @POST
    @Path("/search")
    @Operation(summary = "Search sites", description = "Get a list of sites by search criteria.")
    List<Site> search(
            @Parameter(required = true) @HeaderParam("Authorization") String authToken,
            SiteSearchParams searchParams);
    
    @POST
    @Operation(summary = "Updates a site.", description = "Updates an existing site record.")
    void update(
            @Parameter(required = true) @HeaderParam("Authorization") String authToken,
            Site site
    );    
}
