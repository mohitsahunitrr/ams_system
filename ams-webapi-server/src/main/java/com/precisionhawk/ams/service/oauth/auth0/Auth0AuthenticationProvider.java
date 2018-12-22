package com.precisionhawk.ams.service.oauth.auth0;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpResponseException;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jwt.JWTClaimsSet;
import com.precisionhawk.ams.bean.security.AppCredentials;
import com.precisionhawk.ams.bean.security.CachedUserInfo;
import com.precisionhawk.ams.bean.security.ExtUserCredentials;
import com.precisionhawk.ams.bean.security.ServicesSessionBean;
import com.precisionhawk.ams.bean.security.UserSearchParams;
import com.precisionhawk.ams.config.TenantConfig;
import com.precisionhawk.ams.security.AccessTokenProvider;
import com.precisionhawk.ams.security.Auth0AccessTokenProvider;
import com.precisionhawk.ams.service.oauth.OAuthAuthenticationProvider;
import com.precisionhawk.ams.support.http.HttpTransportClient;
import static com.precisionhawk.ams.support.http.HttpTransportClient.MIME_JSON;
import static com.precisionhawk.ams.support.http.HttpTransportClient.REQ_FACTORY;
import com.precisionhawk.ams.support.jackson.ObjectMapperFactory;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.ParseException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.ws.rs.InternalServerErrorException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.papernapkin.liana.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An implementation of the security service which validates users authenticated
 * against Auth0.
 *
 * @author pchapman
 */
public class Auth0AuthenticationProvider implements OAuthAuthenticationProvider {
    
    private static final String CLAIM_GRANT_TYPE = "gty";
    private static final String GRANT_TYPE_CLIENT_CREDS = "client-credentials";
    private final Logger LOGGER = LoggerFactory.getLogger(getClass());
    private final ObjectMapper MAPPER = ObjectMapperFactory.getObjectMapper();
    
    private AccessTokenProvider accessTokenProvider;
    private JWKSource keySource;
    private TenantConfig config;
    
    @Override
    public void configure(TenantConfig config) throws MalformedURLException {
        this.config = config;
        Auth0AccessTokenProvider p = new Auth0AccessTokenProvider();
        p.setClientId(config.getClientId());
        p.setClientSecret(config.getPasswordKey());
        p.setTenantId(config.getTenantId());
        accessTokenProvider = p;
        keySource = new RemoteJWKSet(new URL(String.format("https://%s/.well-known/jwks.json", config.getTenantId())));
    }

    @Override
    public TenantConfig getTenantConfig() {
        return config;
    }

    @Override
    public JWKSource getKeySource() {
        return keySource;
    }
    
    @Override
    public CachedUserInfo queryForUserInfo(UserSearchParams parameters) {
        if (parameters.getTenantId() != null && !parameters.getTenantId().equals(config.getTenantId())) {
            throw new IllegalArgumentException("Tenant ID does not match.");
        }
        if (parameters.getUserId() != null && !parameters.getUserId().isEmpty()) {
            return queryUserInfo(parameters.getUserId());
        } else if (parameters.getEmailAddress() != null || !parameters.getEmailAddress().isEmpty()) {
            List<CachedUserInfo> list = queryUsersInfo(parameters);
            if (list.isEmpty()) {
                return null;
            } else if (list.size() == 1) {
                return list.get(0);
            } else {
                LOGGER.error("Returned too many results for parameters: {}", parameters);
                return null;
            }
        } else {
            throw new IllegalArgumentException("Either user ID or email address required for query.");
        }
    }
    
    protected CachedUserInfo queryUserInfo(String userId) {
        String accessToken = null;
        try {
            accessToken = accessTokenProvider.obtainAccessToken(config.getUserManagementResource());
        } catch (IOException ex) {
            LOGGER.error("Error gaining access token for Microsoft Graph", ex);
        }
        if (accessToken == null) {
            throw new InternalServerErrorException("Unable to obtain access token for Microsoft Graph APIs.");
        }
        URL url;
        try {
            String s = String.format("https://%s/api/v2/users/%s", URLEncoder.encode(config.getTenantId(), "UTF-8"), URLEncoder.encode(userId, "UTF-8"));
            url = new URL(s);
        } catch (MalformedURLException | UnsupportedEncodingException ex) {
            // Shouldn't happen
            return null;
        }
        // Load Azure Active Directory groups
        int tries = 0;
        while (tries < config.getMaxRetries()) {
            tries++;
            try {
                HttpRequest req = REQ_FACTORY.buildGetRequest(new GenericUrl(url));
                req.getHeaders().setAccept(MIME_JSON);
                req.getHeaders().setAuthorization("Bearer " + accessToken);
                HttpResponse resp = req.execute();
                String respText = HttpTransportClient.loadContent(resp);
                LOGGER.debug("User Results: {}", respText);
                Map<String, Object> data = MAPPER.readValue(respText, new TypeReference<Map<String, Object>>(){});
                UserData udata = userInfoFromData(data);
                return udata == null ? null : udata.userInfo;
            } catch (IOException ex) {
                if (ex instanceof HttpResponseException) {
                    HttpResponseException re = (HttpResponseException)ex;
                    if (re.getStatusCode() == 404) {
                        return null;
                    } else {
                        LOGGER.error("Error loading User Info from tenant {} user{}: attempt: {} status: {} : message: {}", config.getTenantId(), userId, tries, re.getStatusCode(), re.getStatusMessage(), ex);
                    }
                } else {
                    LOGGER.error("Error loading user info: attempt: {}", tries, ex);
                }
                if (tries >= config.getMaxRetries()) {
                    throw new SecurityException(String.format("Error loading user after %d attempts", tries), ex);
                } else {
                    // Wait a few moments before trying again
                    try { Thread.sleep(3000 * tries); } catch (InterruptedException ie) {}
                }
            }
        }
        return null;
    }
    
    private static final String USER_INFO_APP_METADATA = "app_metadata";
    private static final String USER_INFO_EMAIL = "email";
    private static final String USER_INFO_FAMILY_NAME = "family_name";
    private static final String USER_INFO_GIVEN_NAME = "given_name";
    private static final String USER_INFO_NAME = "name";
    private static final String USER_INFO_PHONE = "phone_number";
    private static final String USER_INFO_USER_ID = "user_id";
    private static final String USER_INFO_USERNAME = "username";
    
    /**
     * Obtains the user info from the Map returned from parsing the User Management API's response.
     * @see https://auth0.com/docs/api/management/v2#!/Users/get_users_by_id
     * @param tenantId Unique ID of the tenant in which the user exists.
     * @param data The Map of data parsed from User Management API's response.
     * @return The user information.
     */
    private UserData userInfoFromData(Map<String, Object> data) {
        UserData userData = new UserData();
        CachedUserInfo info = new CachedUserInfo();
        userData.userInfo = info;
        String appDataString = null;
        Object appData = data.get(USER_INFO_APP_METADATA);
        if (appData != null) {
            try {
                appDataString = MAPPER.writeValueAsString(appData);
            } catch (IOException ioe) {
                LOGGER.error("Error converting app data into JSON", ioe);
            }
        }
        userData.appData = appDataString == null ? "{}" : appDataString;
        info.setTenantId(config.getTenantId());
        info.setFirstName(StringUtil.notNull(data.get(USER_INFO_GIVEN_NAME)));
        String emailAddress = StringUtil.notNull(data.get(USER_INFO_EMAIL));
        if (emailAddress != null) {
            emailAddress = emailAddress.toLowerCase();
        }
        info.setEmailAddress(emailAddress);
        info.setLastName(StringUtil.notNull(data.get(USER_INFO_FAMILY_NAME)));
        info.setUserId(StringUtil.notNull(data.get(USER_INFO_USER_ID)));
        return userData;
    }
    
    private class UserData {
        String appData;
        CachedUserInfo userInfo;
    }

    /**
     * Obtains a list of user info based on search parameters.
     * @See https://auth0.com/docs/api/management/v2#!/Users/get_users
     * @See https://auth0.com/docs/users/search/v3/query-syntax
     * @See https://auth0.com/docs/best-practices/search-best-practices
     * @param parameters The parameters to query for users by.
     * @return A list of matches.
     */
    protected List<CachedUserInfo> queryUsersInfo(UserSearchParams parameters) {
        if (parameters.getTenantId() == null || parameters.getTenantId().isEmpty()) {
            throw new IllegalArgumentException("Tenant ID is required.");
        }
        // for now, we only have search by email, which makes the search easier.
        if (parameters.getEmailAddress() == null || parameters.getEmailAddress().isEmpty()) {
            //TODO: Query for all users?
            return Collections.emptyList();
        } else {
            String accessToken = null;
            try {
                accessToken = accessTokenProvider.obtainAccessToken(config.getUserManagementResource());
            } catch (IOException ex) {
                LOGGER.error("Error gaining access token for Auth0", ex);
            }
            if (accessToken == null) {
                throw new InternalServerErrorException("Unable to obtain access token for Auth0.");
            }
            URL url;
            try {
                String s = String.format("https://%s/api/v2/users-by-email?email=%s", URLEncoder.encode(config.getTenantId(), "UTF-8"), URLEncoder.encode(parameters.getEmailAddress(), "UTF-8"));
                url = new URL(s);
            } catch (MalformedURLException | UnsupportedEncodingException ex) {
                // Shouldn't happen
                return null;
            }
            // Load Azure Active Directory groups
            int tries = 0;
            while (tries < config.getMaxRetries()) {
                tries++;
                try {
                    HttpRequest req = REQ_FACTORY.buildGetRequest(new GenericUrl(url));
                    req.getHeaders().setAccept(MIME_JSON);
                    req.getHeaders().setAuthorization("Bearer " + accessToken);
                    HttpResponse resp = req.execute();
                    String respText = HttpTransportClient.loadContent(resp);
                    LOGGER.debug("User Results: {}", respText);
                    List<Map<String, Object>> data = MAPPER.readValue(respText, new TypeReference<List<Map<String, Object>>>(){});
                    List<CachedUserInfo> list = new LinkedList<>();
                    for (Map<String, Object> userData : data) {
                        list.add(userInfoFromData(userData).userInfo);
                    }
                    return list;
                } catch (IOException ex) {
                    if (ex instanceof HttpResponseException) {
                        HttpResponseException re = (HttpResponseException)ex;
                        if (re.getStatusCode() == 404) {
                            return null;
                        } else {
                            LOGGER.error("Error loading User Info from tenant {} attempt: {} status: {} : message: {}", parameters.getTenantId(), tries, re.getStatusCode(), re.getStatusMessage(), ex);
                        }
                    } else {
                        LOGGER.error("Error loading user info: attempt: {}", tries, ex);
                    }
                    if (tries >= config.getMaxRetries()) {
                        throw new SecurityException(String.format("Error loading user after %d attempts", tries), ex);
                    } else {
                        // Wait a few moments before trying again
                        try { Thread.sleep(3000 * tries); } catch (InterruptedException ie) {}
                    }
                }
            }
        }
        return Collections.emptyList();
    }

    @Override
    public ServicesSessionBean createServicesSessionBean(JWTClaimsSet claimsSet) {
        // Determine if the claimsSet is for this tenant
        String issuer = claimsSet.getIssuer();
        if (issuer.contains(config.getTenantId())) {
            ServicesSessionBean bean = new ServicesSessionBean();
            // It's this issuer.  Now chech the audience.
            List<String> audience = claimsSet.getAudience();
//            if (!audience.contains(config.getApiId())) {
//                LOGGER.error("The provided token is not intended for this application.  Declaired audience: {}", audience);
//                throw new SecurityException("The provided token is not intended for this application.");
//            }
            boolean found = false;
            for (String aud : audience) {
                if (config.getApiId().equals(aud)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                LOGGER.error("The provided token is not intended for this application.  Declaired audience: {}", audience);
                throw new SecurityException("The provided token is not intended for this application.");
            }
            bean.setTenantId(config.getTenantId());
            bean.setTokenValid(true);
            String grantType;
            try {
                grantType = claimsSet.getStringClaim(CLAIM_GRANT_TYPE);
            } catch (ParseException pe) {
                LOGGER.error("Error parsing out grant type from claims set.", pe);
                grantType = null;
            }
            if (grantType != null && GRANT_TYPE_CLIENT_CREDS.equals(grantType)) {
                createAppCredentials(claimsSet, bean);
            } else {
                createExtUserCredentials(claimsSet, bean);
            }
            return bean;
        } else {
            return null;
        }
    }
    
    /**
     * Create a session bean based on server to server communication.
     * @param claimsSet The claims set of the connecting service.
     * @return The session bean.
     */
    private void createAppCredentials(JWTClaimsSet claimsSet, ServicesSessionBean bean) {
        String clientAppID = claimsSet.getSubject();
        if (clientAppID == null) {
            bean.setTokenValid(false);
            bean.setReason("Invalid token, not a user on-behalf of token or a valid service-to-service token.");
        } else {
            AppCredentials creds = new AppCredentials();
            creds.setApplicationId(clientAppID);
            bean.setCredentials(creds);
        }
    }
    
    /**
     * Create a session bean based on user to server (on-behalf-of) communication.
     * @param claimsSet The claims set of the connecting user.
     * @return The session bean.
     */
    private void createExtUserCredentials(JWTClaimsSet claimsSet, ServicesSessionBean bean) {
        ExtUserCredentials creds = new ExtUserCredentials();
        creds.setUserId(claimsSet.getClaim(CLAIM_SUBJECT).toString());
        if (creds.getUserId() == null) {
            bean.setTokenValid(false);
            bean.setReason("Invalid token, not a user on-behalf of token or a valid service-to-service token.");
        } else {
            // Auth0 claims do not have any identifying information except the subscriber's ID.
            // Calls must be made to the service to get profile information.
            CachedUserInfo userInfo = queryUserInfo(creds.getUserId());
            if (userInfo == null) {
                bean.setTokenValid(false);
                bean.setReason(String.format("Invalid user ID %s", creds.getUserId()));
            } else {
                creds.populate(userInfo);
                bean.setCredentials(creds);
            }
        }
    }
}
