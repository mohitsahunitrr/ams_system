package com.precisionhawk.ams.webservices;

import io.swagger.oas.annotations.Operation;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author pchapman
 */
@Path("/utils")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface UtilsWebService extends WebService {
    
    @GET
    @Path("/uuid")
    @Operation(summary = "Get generated UUIDs", description = "Returns an array of UUIDs")
    List<String> retrieve(@QueryParam("count") @DefaultValue("10") Integer count);
}
