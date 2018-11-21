/*
 * All rights reserved.
 */

package com.precisionhawk.ams.webservices;
import com.precisionhawk.ams.bean.security.ExtUserCredentials;
import io.swagger.oas.annotations.Operation;
import io.swagger.oas.annotations.Parameter;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * Provides services calls needed for authentication as well as user
 * maintenance.
 *
 * @author Philip A. Chapman
 */
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Path("/")
public interface AuthenticationWebService extends WebService {

//    /**
//     * Retrieves a mapping of user names to ID.  The user names are in the form
//     * of first name + space + last name.
//     */
//    @GET
//    @Path("user/names")
//    Map<String, String> mapUserNamesByID();

    @GET
    @Path("auth/credentials")
    @Operation(summary = "Obtains User Credentials", description = "Validates the Authorization token and returns user credentials.")
    public ExtUserCredentials obtainCredentials(@Parameter(required = true) @HeaderParam("Authorization") String authToken);
}
