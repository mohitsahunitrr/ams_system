/*
 * All rights reserved.
 */

package com.precisionhawk.ams.bean.security;

import com.precisionhawk.ams.domain.Organization;
import com.precisionhawk.ams.security.Constants;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author <a href="mailto:pchapman@pcsw.us">Philip A. Chapman</a>
 */
public class AppCredentials implements Credentials
{
    private String applicationId;
    public String getApplicationId() {
        return applicationId;
    }
    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }
    
    private List<String> siteIDs = new LinkedList<>();
    @Override
    public List<String> getSiteIDs() {
        return siteIDs;
    }
    public void setSiteIDs(List<String> siteIDs) {
        this.siteIDs = siteIDs;
    }

    private List<Organization> organizations = new LinkedList<>();
    @Override
    public List<Organization> getOrganizations() {
        return organizations;
    }
    public void setOrganizations(List<Organization> organizations) {
        this.organizations = organizations;
    }
    
    private boolean isPrecisionHawk() {
        for (Organization org : organizations) {
            if (Constants.COMPANY_ORG_KEY.equals(org.getKey())) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public boolean checkAuthorization(String appKey, String orgId, String siteId, boolean mustBePrecisionHawkEmployee, String... groupKeys) {
        if (isPrecisionHawk()) {
            return true;
        }
        // Authorized applications can do almost anything within their organization and sites
        // AppKey, inspectools employee, and group keys are all ignored.
        if (siteId != null) {
            boolean authorized = getSiteIDs().contains(siteId);
            if (!authorized && orgId != null) {
                // Even if the site isn't listed, org may allow.
                for (Organization org :getOrganizations()) {
                    if (Constants.COMPANY_ORG_KEY.equals(org.getKey()) || orgId.equals(org.getId())) {
                        authorized = true;
                        break;
                    }
                }
            }
            return authorized;
        } else if (orgId != null) {
            for (Organization org :getOrganizations()) {
                if (orgId.equals(org.getId())) {
                    return true;
                }
            }
            return false;
        } else {
            return true;
        }
    }
}
