/*
 * All rights reserved.
 */

package com.precisionhawk.ams.bean.security;

import com.precisionhawk.ams.domain.Organization;
import com.precisionhawk.ams.security.Constants;
import io.swagger.oas.annotations.media.Schema;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

/**
 *
 * @author <a href="mailto:pchapman@pcsw.us">Philip A. Chapman</a>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description="An object containing information about a user including roles.")
public class UserCredentials extends UserInfoBean implements Credentials
{
    @Schema(description="Roles available for the user mapped by application ID.")
    private Map<String, List<String>> rolesByApplication;
    public Map<String, List<String>> getRolesByApplication() {
        return rolesByApplication;
    }
    public void setRolesByApplication(Map<String, List<String>> rolesByApplication) {
        this.rolesByApplication = rolesByApplication;
    }
    
    private List<String> siteIDs;
    @Override
    public List<String> getSiteIDs() {
        return siteIDs;
    }
    public void setSiteIDs(List<String> siteIDs) {
        this.siteIDs = siteIDs;
    }

    private List<Organization> organizations;
    @Override
    public List<Organization> getOrganizations() {
        return organizations;
    }
    public void setOrganizations(List<Organization> organizations) {
        this.organizations = organizations;
    }
    
    public UserCredentials() {}
    
    public UserCredentials(UserInfo source) {
        setEmailAddress(source.getEmailAddress());
        setFirstName(source.getFirstName());
        setLastName(source.getLastName());
        setMiddleName(source.getMiddleName());
        setUserId(source.getUserId());
    }
    
    @Override
    public boolean checkAuthorization(String appKey, String orgId, String siteId, boolean mustBeInspectoolsEmployee, String ... groupKeys)
    {
        boolean isAdmin = false;
        boolean isInspectToolsEmp = checkIsInspecToolsUser();
        
        // If Inspectools User requred, check that.
        if (mustBeInspectoolsEmployee && (!isInspectToolsEmp)) {
            return false;
        }
        
        // Check to see if the user has any roles for the application.  These are required.
        Set<String> roleKeys = new HashSet<>();
        if (appKey == null) {
            // Get 'em all
            for (String akey : rolesByApplication.keySet()) {
                roleKeys.addAll(rolesByApplication.get(akey));
            }
        } else {
            // Get the specific app's role keys
            roleKeys.addAll(rolesByApplication.get(appKey));
        }
        if (roleKeys.isEmpty()) {
            // No permissions for the app
            return false;
        } else {
            if (roleKeys.contains(Constants.GROUP_KEY_ADMIN)) {
                // InspecTools admin can do anything
                return true;
            }
            // isAdmin is used for skipping site access check later, if needed.
            isAdmin = roleKeys.contains(Constants.GROUP_KEY_ORG_ADMIN);
        }
        
        // Check to see if user is in the org.  If not, group permissions do not matter.
        if (orgId != null) {
            // Assume the user isn't in the org.
            boolean authorized = false;
            for (Organization org : organizations) {
                if (org.getId().equals(orgId)) {
                    authorized = true;
                    break;
                }
            }
            if (!authorized) {
                return false;
            }
        }
        
        // Check vs provided group Keys, if any
        if (groupKeys != null && groupKeys.length > 0) {
            // User must have at least one group.  Assume user doesn't.
            boolean authorized = false;
            for (String gkey : groupKeys) {
                if (roleKeys.contains(gkey)) {
                    authorized = true;
                    break;
                }
            }
            if (!authorized) {
                return false;
            }
        }
        
        // Organization admins can always see any site in the org.  InspecTools users can always see all sites.
        if (siteId != null) {
            return isAdmin  || isInspectToolsEmp || siteIDs.contains(siteId);
        }
        
        return true;
    }
    
    public boolean checkForRole(String appId, String groupKey) {
        List<String> groupKeys = rolesByApplication.get(appId);
        if (groupKeys != null) {
            return groupKeys.contains(groupKey);
        }
        return false;
    }
    
    public boolean checkIsInspecToolsUser() {
        for (Organization org : organizations) {
            if (Constants.COMPANY_ORG_KEY.equals(org.getKey())) {
                return true;
            }
        }
        return false;
    }
}
