/*
 * All rights reserved.
 */

package com.precisionhawk.ams.service.aad;

import com.nimbusds.jwt.JWTClaimsSet;
import com.precisionhawk.ams.bean.security.ServicesSessionBean;
import com.precisionhawk.ams.dao.SecurityDao;
import com.precisionhawk.ams.security.AccessTokenProvider;
import java.util.Set;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A provider of AAD groups a user is enrolled in which can then be mapped to
 * WindAMS roles.
 *
 * @author <a href="mailto:pchapman@pcsw.us">Philip A. Chapman</a>
 */
abstract class AADGroupsProvider {
    
    protected final Logger LOGGER = LoggerFactory.getLogger(getClass());
    
    protected ObjectMapper mapper;    
    ObjectMapper getMapper() {
        return mapper;
    }
    void setMapper(ObjectMapper mapper) {
        this.mapper = mapper;
    }
    
    protected SecurityDao dao;
    SecurityDao getDao() {
        return dao;
    }
    void setDao(SecurityDao dao) {
        this.dao = dao;
    }
    
    private int maxRetries = 3;
    public int getMaxRetries() {
        return maxRetries;
    }
    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }
    
    /**
     * Queries Azure Active Directory for group memberships and returns them.
     * @param accessTokenProvider The provider for the access token to be used
     * to make the necessary service calls.
     * @param session The session containing user credentials.
     * @param claimsSet The claims set used to build the user credentials.
     * @return The list of group IDs.
     * @throws SecurityException  Indicates an error obtaining the information.
     */
    abstract Set<String> loadAADGroupIDs(AccessTokenProvider accessTokenProvider, ServicesSessionBean session, JWTClaimsSet claimsSet) throws SecurityException;
}
