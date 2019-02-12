package com.precisionhawk.ams.service.oauth;

import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jwt.JWTClaimsSet;
import com.precisionhawk.ams.bean.security.CachedUserInfo;
import com.precisionhawk.ams.bean.security.ServicesSessionBean;
import com.precisionhawk.ams.bean.security.UserSearchParams;
import com.precisionhawk.ams.config.TenantConfig;
import com.precisionhawk.ams.dao.OAuthSecurityDao;
import com.precisionhawk.ams.domain.Organization;
import com.precisionhawk.ams.security.AccessTokenProvider;
import java.net.MalformedURLException;
import java.util.List;

/**
 *
 * @author pchapman
 */
public interface OAuthAuthenticationProvider {

    static final String CLAIM_AUDIENCE = "aud";
    static final String CLAIM_EXPIRATION = "exp";
    static final String CLAIM_ISSUED_AT = "iat";
    static final String CLAIM_ISSUER = "iss";
    static final String CLAIM_JWT_ID = "jti";
    static final String CLAIM_NOT_BEFORE = "nbf";
    static final String CLAIM_SUBJECT = "sub";
    
    void configure(TenantConfig config) throws MalformedURLException;
    
    TenantConfig getTenantConfig();
    
    /**
     * Create a session bean based on the provided Claims Set.
     * @param claimsSet The claims set contained within the supplied access token.
     * @return The session bean, or null if the claims were not issued by this
     *         the issuer this provider is configured for.
     */
    ServicesSessionBean createServicesSessionBean(JWTClaimsSet claimsSet);
    
    JWKSource getKeySource();
    
    /**
     * Queries the authentication authority for user information.
     * @param parameters The parameters to search for.
     * @return The information of the user which matches the parameters or null if
     * no matches were found.
     */
    CachedUserInfo queryForUserInfo(UserSearchParams parameters);
    
    List<Organization> selectOrganizationsForUser(OAuthSecurityDao dao, ServicesSessionBean bean);
    
    AccessTokenProvider getAccessTokenProvider();
    
//    /**
//     * Query for the information of a user that matches the given parameters.
//     * @param parameters The information to be used to identify the user.
//     * @return Information about the user or null if not found.
//     */
//    UserInfoBean queryUserInfo(UserSearchParams parameters);
}
