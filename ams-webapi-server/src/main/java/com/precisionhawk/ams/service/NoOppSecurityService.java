/*
 * All rights reserved.
 */

package com.precisionhawk.ams.service;

import com.precisionhawk.ams.bean.security.AppCredentials;
import com.precisionhawk.ams.bean.security.CachedUserInfo;
import com.precisionhawk.ams.bean.security.ServicesSessionBean;
import com.precisionhawk.ams.bean.security.UserInfoBean;
import com.precisionhawk.ams.bean.security.UserSearchParams;
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
    
    private SecurityDao securityDao;
    private SiteProvider siteDao;

    public SecurityDao getSecurityDao() {
        return securityDao;
    }

    public void setSecurityDao(SecurityDao securityDao) {
        this.securityDao = securityDao;
    }

    public SiteProvider getSiteProvider() {
        return siteDao;
    }

    public void setSiteProvider(SiteProvider siteDao) {
        this.siteDao = siteDao;
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
            for (Site s : siteDao.retrieveAllSites()) {
                creds.getSiteIDs().add(s.getId());
            }
        } catch (DaoException dao) {
            LOGGER.error("Error looking up sites.", dao);
            throw new InternalServerErrorException("Error loading approved sites");
        }
        sess.setCredentials(creds);
        sess.setTokenValid(true);
        return sess;
    }
    
    private final AccessTokenProvider tokenProvider = new NoOppAccessTokenProvider();

    @Override
    public AccessTokenProvider accessTokenProvider(String tenantId) {
        return tokenProvider;
    }
}
