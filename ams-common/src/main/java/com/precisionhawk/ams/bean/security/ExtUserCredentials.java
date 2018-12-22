/*
 * All rights reserved.
 */

package com.precisionhawk.ams.bean.security;

import com.precisionhawk.ams.domain.Site;
import io.swagger.oas.annotations.media.Schema;
import java.util.List;
import java.util.Map;

/**
 * The security layer does not have access to site data.  Therefore, we created
 * this sub-class that can be returned and will contain sites available for the
 * user.
 *
 * @author <a href="mailto:pchapman@pcsw.us">Philip A. Chapman</a>
 */
@Schema(description="User credentials with convenience mapping of sites available by organization.")
public class ExtUserCredentials extends UserCredentials
{
    @Schema(description="A list of sites available to the user keyed by organization ID.")
    private Map<String, List<Site>> sitesByOrganization;
    public Map<String, List<Site>> getSitesByOrganization() {
        return sitesByOrganization;
    }
    public void setSitesByOrganization(Map<String, List<Site>> sitesByOrganization) {
        this.sitesByOrganization = sitesByOrganization;
    }
    
    public ExtUserCredentials() {}
    
    public ExtUserCredentials(UserCredentials source, Map<String, List<Site>> sitesByOrganization) {
        setFirstName(source.getFirstName());
        setLastName(source.getLastName());
        setOrganizations(source.getOrganizations());
        setRolesByApplication(source.getRolesByApplication());
        setSiteIDs(source.getSiteIDs());
        setUserId(source.getUserId());
        this.sitesByOrganization = sitesByOrganization;
    }
    
    public void populate(UserInfo userInfo) {
        setEmailAddress(userInfo.getEmailAddress());
        setFirstName(userInfo.getFirstName());
        setLastName(userInfo.getLastName());
        setMiddleName(userInfo.getMiddleName());
        setUserId(userInfo.getUserId());
    }
    
    public void populate(UserInfoBean userInfo) {
        populate((UserInfo)userInfo);
        setEmailNickname(userInfo.getEmailNickname());
        setTenantId(userInfo.getTenantId());
    }
}
