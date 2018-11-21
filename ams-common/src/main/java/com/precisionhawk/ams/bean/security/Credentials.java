/*
 * All rights reserved.
 */
package com.precisionhawk.ams.bean.security;

import com.precisionhawk.ams.domain.Organization;
import io.swagger.oas.annotations.media.Schema;
import java.util.List;

/**
 *
 * @author Philip A. Chapman
 */
public interface Credentials {
    
    @Schema(description="List of sites the user has access to.")
    public List<String> getSiteIDs();

    @Schema(description="List of organizations the user is associated with.")
    public List<Organization> getOrganizations();
    
    public boolean checkAuthorization(String appKey, String orgId, String siteId, boolean mustBeInspectoolsEmployee, String ... groupKeys);
}
