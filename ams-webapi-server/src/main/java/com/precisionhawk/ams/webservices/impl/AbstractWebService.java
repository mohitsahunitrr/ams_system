package com.precisionhawk.ams.webservices.impl;

import com.precisionhawk.ams.bean.security.ServicesSessionBean;
import com.precisionhawk.ams.domain.SiteAware;
import com.precisionhawk.ams.service.SecurityService;
import java.util.Collection;
import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotAuthorizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Philip A. Chapman
 */
public abstract class AbstractWebService {
    
    protected Logger LOGGER = LoggerFactory.getLogger(getClass());
    @Inject
    protected SecurityService securityService;
    
    protected static void ensureExists(Object obj, String errMsg) {
        if (
                obj == null
                ||
                (obj instanceof String && ((String)obj).isEmpty())
            )
        {
            throw new BadRequestException(errMsg);
        }
    }
    
    /**
     * Validates the access token.
     * @param accessToken The given access token.
     * @return A bean containing session info.
     * @throws BadRequestException Invalid token.
     * @throws NotAuthorizedException User not authorized.
     * @throws InternalServerErrorException System error.
     */
    protected ServicesSessionBean lookupSessionBean(String accessToken)
       throws BadRequestException, NotAuthorizedException, InternalServerErrorException
    {
        ServicesSessionBean sess = securityService.validateToken(accessToken);
        if (sess == null || (!sess.isTokenValid())) {
            LOGGER.error("Token \"{}\" failed validation: {}", accessToken, sess.getReason());
            throw new NotAuthorizedException(sess.getReason());
        }
        return sess;
    }
    
    /**
     * Validates that the user has access to data by site.
     * @param <T> The type of data related to a site.
     * @param sess The user session data.
     * @param aware The data related to a site.
     * @param groupKeys Unique Keys of groups to check for user enrollment.  The user must exist in at least one but does
     *                  not have to exist in all.
     * @return The data related to a site.
     * @throws NotAuthorizedException User not authorized.
     */
    protected <T extends SiteAware> T authorize(ServicesSessionBean sess, T aware, String ... groupKeys) throws NotAuthorizedException {
        if (!sess.isTokenValid()) {
            throw new NotAuthorizedException(sess.getReason());
        }
        //TODO: Org Aware for Org ID?
        if (sess.getCredentials().checkAuthorization(null, null, aware.getSiteId(), false, groupKeys)) {
            return aware;
        } else {
            throw new NotAuthorizedException(String.format("User not authorized for site %s", aware.getSiteId()));
        }
    }
    
    /**
     * Validates that the user has access to data by site.
     * @param <T> A collection of data related to a site.
     * @param sess The user session data.
     * @param awares The data related to a site.
     * @param groupKeys Unique Keys of groups to check for user enrollment.  The user must exist in at least one but does
     *                  not have to exist in all.
     * @return The collection of data related to a site.
     * @throws NotAuthorizedException User not authorized.
     */
    protected <T extends Collection<? extends SiteAware>> T authorize(ServicesSessionBean sess, T awares, String ... groupKeys)
        throws NotAuthorizedException
    {
        if (!sess.isTokenValid()) {
            throw new NotAuthorizedException(sess.getReason());
        }
        // Check org and group permissions
        //TODO: Org Aware for Org ID?
        if (sess.getCredentials().checkAuthorization(null, null, null, false, groupKeys)) {
            // Check site permissions
            for (SiteAware aware : awares) {
                if (aware != null) {
                    if (aware.getSiteId() != null && !sess.getCredentials().getSiteIDs().contains(aware.getSiteId())) {
                        throw new NotAuthorizedException(String.format("User not authorized for site %s", aware.getSiteId()));
                    }
                }
            }
        }
        return awares;
    }
}
