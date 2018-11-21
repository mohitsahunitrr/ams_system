/*
 * All rights reserved.
 */

package com.precisionhawk.ams.service;

import com.precisionhawk.ams.bean.security.ExtUserCredentials;
import com.precisionhawk.ams.bean.security.ServicesSessionBean;
import com.precisionhawk.ams.bean.security.UserCredentials;
import com.precisionhawk.ams.config.SecurityConfig;
import com.precisionhawk.ams.domain.Organization;
import com.precisionhawk.ams.domain.Site;
import java.util.LinkedList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author <a href="mailto:pchapman@pcsw.us">Philip A. Chapman</a>
 */
public abstract class AbstractSecurityService implements SecurityService {
    
    protected final Logger LOGGER = LoggerFactory.getLogger(getClass());
    
    protected SecurityConfig config;
    public SecurityConfig getSecurityConfig() {
        return config;
    }
    public void setSecurityConfig(SecurityConfig securityConfig) {
        this.config = securityConfig;
    }

    @Override
    public ServicesSessionBean addSiteToCredentials(ServicesSessionBean sess, Site site) {
        if (sess.getCredentials() instanceof UserCredentials) {
            UserCredentials creds = (UserCredentials)sess.getCredentials();
            for (Organization org : creds.getOrganizations()) {
                if (org.getId().equals(site.getOrganizationId())) {
                    creds.getSiteIDs().add(site.getId());
                    if (creds instanceof ExtUserCredentials) {
                        ExtUserCredentials excreds = (ExtUserCredentials)creds;
                        List<Site> sites = excreds.getSitesByOrganization().get(site.getOrganizationId());
                        if (sites == null) {
                            sites = new LinkedList<>();
                            excreds.getSitesByOrganization().put(site.getOrganizationId(), sites);
                        }
                        sites.add(site);
                    }
                }
            }
        }
        return sess;
    }
}
