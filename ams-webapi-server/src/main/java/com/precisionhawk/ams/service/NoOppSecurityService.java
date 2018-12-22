/*
 * All rights reserved.
 */

package com.precisionhawk.ams.service;

import com.precisionhawk.ams.bean.security.AppCredentials;
import com.precisionhawk.ams.bean.security.CachedUserInfo;
import com.precisionhawk.ams.bean.security.ServicesSessionBean;
import com.precisionhawk.ams.bean.security.UserInfoBean;
import com.precisionhawk.ams.bean.security.UserSearchParams;
import com.precisionhawk.ams.cache.SecurityTokenCache;
import com.precisionhawk.ams.config.SecurityConfig;
import com.precisionhawk.ams.dao.DaoException;
import com.precisionhawk.ams.dao.SecurityDao;
import com.precisionhawk.ams.dao.SiteProvider;
import com.precisionhawk.ams.domain.Site;
import com.precisionhawk.ams.security.AccessTokenProvider;
import com.precisionhawk.ams.security.NoOppAccessTokenProvider;
import java.util.List;
import javax.ws.rs.InternalServerErrorException;

/**
 * An implementation of the SecurityService which does no checking to see if
 * requests are valid.  <strong>THIS IMPLEMENTATION IS FOR DEVELOPMENT ONLY AND
 * UNDER NO CIRCUMSTANCES SHOULD IT BE USED IN PRODUCTION.</strong>
 *
 * @author <a href="mailto:pchapman@pcsw.us">Philip A. Chapman</a>
 */
public class NoOppSecurityService extends AbstractSecurityService {
    
    private final AccessTokenProvider tokenProvider = new NoOppAccessTokenProvider();
    
    private SecurityDao securityDao;
    private List<SiteProvider> siteDaos;

    public SecurityDao getSecurityDao() {
        return securityDao;
    }

    public void setSecurityDao(SecurityDao securityDao) {
        this.securityDao = securityDao;
    }

    public List<SiteProvider> getSiteProviders() {
        return siteDaos;
    }

    public void setSiteProviders(List<SiteProvider> providers) {
        this.siteDaos = providers;
    }    

    @Override
    public void configure(SecurityDao securityDao, List<SiteProvider> siteDaos, SecurityTokenCache tokenCache, SecurityConfig config) {
        setSecurityConfig(config);
        setSecurityDao(securityDao);
        setSiteProviders(siteDaos);
    }

    @Override
    public UserInfoBean queryUserInfo(UserSearchParams parameters) {
        List<CachedUserInfo> results = securityDao.selectUserInfo(parameters);
        if (results.size() == 1) {
            return results.get(0);
        } else {
            return null;
        }
    }

    @Override
    public ServicesSessionBean validateToken(String accessToken) {
        ServicesSessionBean sess = new ServicesSessionBean();
        AppCredentials creds = new AppCredentials();
        creds.setOrganizations(securityDao.selectOrganizations());
        try {
            for (SiteProvider p : siteDaos) {
                for (Site s : p.retrieveAllSites()) {
                    creds.getSiteIDs().add(s.getId());
                }
            }
        } catch (DaoException dao) {
            LOGGER.error("Error looking up sites.", dao);
            throw new InternalServerErrorException("Error loading approved sites");
        }
        sess.setCredentials(creds);
        sess.setTokenValid(true);
        return sess;
    }

    @Override
    public AccessTokenProvider accessTokenProvider(String tenantId) {
        return tokenProvider;
    }
}
