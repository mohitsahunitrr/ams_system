/*
 * All rights reserved.
 */

package com.precisionhawk.ams.service.oauth;

import com.nimbusds.jwt.JWTClaimsSet;
import com.precisionhawk.ams.bean.security.ServicesSessionBean;
import com.precisionhawk.ams.config.TenantConfig;
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
public abstract class GroupsProvider {
    
    protected final Logger LOGGER = LoggerFactory.getLogger(getClass());
    
    protected ObjectMapper mapper;    
    public ObjectMapper getMapper() {
        return mapper;
    }
    public void setMapper(ObjectMapper mapper) {
        this.mapper = mapper;
    }
    
    protected SecurityDao dao;
    public SecurityDao getDao() {
        return dao;
    }
    public void setDao(SecurityDao dao) {
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
     * A configuration methed that can be overridden if necessary.
     * @param config 
     */
    public void configure(TenantConfig config) {}
    
    /**
     * Queries Azure Active Directory for group memberships and returns them.
     * @param accessTokenProvider The provider for the access token to be used
     * to make the necessary service calls.
     * @param session The session containing user credentials.
     * @param claimsSet The claims set used to build the user credentials.
     * @return The list of group IDs.
     * @throws SecurityException  Indicates an error obtaining the information.
     */
    public abstract Set<String> loadGroupIDs(AccessTokenProvider accessTokenProvider, ServicesSessionBean session, JWTClaimsSet claimsSet) throws SecurityException;
}
