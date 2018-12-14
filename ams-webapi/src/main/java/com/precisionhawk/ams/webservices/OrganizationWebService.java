/*
 * All rights reserved.
 */
package com.precisionhawk.ams.webservices;

import com.precisionhawk.ams.bean.orgconfig.OrgFieldTranslations;
import com.precisionhawk.ams.bean.orgconfig.OrgFieldValidations;
import com.precisionhawk.ams.bean.orgconfig.OrgTranslationsSummaryList;
import com.precisionhawk.ams.domain.Organization;
import com.precisionhawk.ams.security.Constants;
import io.swagger.oas.annotations.Operation;
import io.swagger.oas.annotations.Parameter;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author Philip A. Chapman
 */
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Path("/org")
public interface OrganizationWebService extends WebService {
    
    public static final String COMPANY_ORG_KEY = Constants.COMPANY_ORG_KEY;

    @PUT
    Organization createOrg(@Parameter(required = true) @HeaderParam("Authorization") String authToken, Organization org);
    
    @GET
    @Path("/precisionHawk")
    @Operation(summary = "Get InspecTools", description = "Gets the InspecTools organization.")
    Organization retrievePrecisionHawkOrg();
    
    @GET
    @Path("/{orgId}")
    @Operation(summary = "Get Organization", description = "Gets an organization by its unique ID.")
    Organization retrieveOrg(@PathParam("orgId") String orgId);
    
    @GET
    @Operation(summary = "Get Organizations", description = "Gets all organizations.")
    List<Organization> retrieveOrgs();

    @GET
    @Path("/{orgId}/translations")
    @Operation(summary = "Gets logos, field names, list values and errors for an organization", description = "Gets logos, field names, list values and errors for an organization.  Optionally takes a 2 letter ISO 3166-1 country code, a 2 letter ISO 639-2 language code, and/or the date during which the values are valid.  Date should be in yyyyMMdd format.")
    public OrgFieldTranslations retrieveOrgTranslations(
        @PathParam("orgId") String orgId,
        @QueryParam("lang") @DefaultValue("") String lang,
        @QueryParam("country") @DefaultValue("") String country
    );

    @GET
    @Path("/{orgId}/translations/summary")
    @Operation(summary = "Gets the available country and language translations for an organization", description = "Gets the available country and language translations for an organization.")
    public OrgTranslationsSummaryList retrieveOrgTranslationsSummary(
        @PathParam("orgId") String orgId
    );

    @GET
    @Path("/{orgId}/validations")
    @Operation(summary = "Gets an organization's field validation rules.", description = "Gets an organization's field validation rules.  These are basic field validation rules.  More complex business rules are handled in code.")
    public OrgFieldValidations retrieveOrgFieldValidations(
        @PathParam("orgId") String orgId
    );
    
    @POST
    @Path("/translations")
    public void postOrgTranslations(
        @Parameter(required = true) @HeaderParam("Authorization") String authToken,
        OrgFieldTranslations trans
    );

    @POST
    @Path("/validations")
    @Operation(summary = "Gets an organization's field validation rules.", description = "Gets an organization's field validation rules.  These are basic field validation rules.  More complex business rules are handled in code.")
    public void postOrgFieldValidations(
        @Parameter(required = true) @HeaderParam("Authorization") String authToken,
        OrgFieldValidations validations
    );
}
