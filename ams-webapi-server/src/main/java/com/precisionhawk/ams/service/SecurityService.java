/*
 * All rights reserved.
 */

package com.precisionhawk.ams.service;

import com.precisionhawk.ams.bean.security.ServicesSessionBean;
import com.precisionhawk.ams.bean.security.UserInfoBean;
import com.precisionhawk.ams.bean.security.UserSearchParams;
import com.precisionhawk.ams.cache.SecurityTokenCache;
import com.precisionhawk.ams.config.SecurityConfig;
import com.precisionhawk.ams.dao.SecurityDao;
import com.precisionhawk.ams.dao.SiteProvider;
import com.precisionhawk.ams.domain.Site;
import com.precisionhawk.ams.security.AccessTokenProvider;
import java.util.List;

/**
 *
 * @author <a href="mailto:pchapman@pcsw.us">Philip A. Chapman</a>
 */
public interface SecurityService {
    
    void configure(SecurityDao securityDao, List<SiteProvider> siteDaos, SecurityTokenCache tokenCache, SecurityConfig config);
    
    /**
     * Obtain a provider that will obtains tokens for applications within the
     * given Azure tenant.
     * @param tenantId the unique ID of the Azure tenant.
     * @return The provider.
     */
    AccessTokenProvider accessTokenProvider(String tenantId);
    
    /**
     * Query for the information of a user that matches the given parameters.
     * @param parameters The information to be used to identify the user.
     * @return Information about the user or null if not found.
     */
    UserInfoBean queryUserInfo(UserSearchParams parameters);
    
    /**
     * Validates the token and returns a response.  If the token is valid, the
     * credentials will be populated, else not.
     * @param accessToken The token to test.
     * @return A session bean containing results of validating the token.
     */
    ServicesSessionBean validateToken(String accessToken);

    /**
     * Adds a site to the user's credentials after the site has been created.
     * @param sess
     * @param site
     * @return 
     */
    ServicesSessionBean addSiteToCredentials(ServicesSessionBean sess, Site site);
}
