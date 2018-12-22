/*
 * All rights reserved.
 */

package com.precisionhawk.ams.service.oauth.aad;

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
import com.precisionhawk.ams.security.AADAccessTokenProvider;
import com.precisionhawk.ams.security.AccessTokenProvider;
import com.precisionhawk.ams.service.oauth.OAuthAuthenticationProvider;
import com.precisionhawk.ams.support.http.HttpTransportClient;
import static com.precisionhawk.ams.support.http.HttpTransportClient.MIME_JSON;
import static com.precisionhawk.ams.support.http.HttpTransportClient.REQ_FACTORY;
import com.precisionhawk.ams.support.jackson.ObjectMapperFactory;
import com.precisionhawk.ams.util.RegexUtils;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.inject.Named;
import javax.ws.rs.InternalServerErrorException;
import org.apache.commons.io.IOUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.papernapkin.liana.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The class responsible for applying security rules and setting up the session
 * and user credentials objects based on authentication against and
 * authorization based on Azure Active Directory.
 *
 * @author <a href="mailto:pchapman@pcsw.us">Philip A. Chapman</a>
 */
@Named
public final class AADAuthenticationProvider implements OAuthAuthenticationProvider {
    
    private static final String CLAIM_EMAIL = "email";
    private static final String CLAIM_FAMILY_NAME = "family_name";
    private static final String CLAIM_GIVEN_NAME = "given_name";
    private static final String CLAIM_OBJECT_ID = "oid";
    private static final String CLAIM_SCP = "scp";
    private static final String CLAIM_TENANT_ID = "tid";
    private static final String CLAIM_UNIQUE_NAME = "unique_name";
    private static final String CLAIM_VAL_USER_IMPERSONATION = "user_impersonation";
    
    private final Logger LOGGER = LoggerFactory.getLogger(getClass());
    private final ObjectMapper MAPPER = ObjectMapperFactory.getObjectMapper();

    private AccessTokenProvider accessTokenProvider;
    private JWKSource keySource;
    private TenantConfig config;

    @Override
    public void configure(TenantConfig config) throws MalformedURLException {
        this.config = config;
        AADAccessTokenProvider p = new AADAccessTokenProvider();
        p.setClientId(config.getClientId());
        p.setClientSecret(config.getPasswordKey());
        p.setTenantId(config.getTenantId());
        accessTokenProvider = p;
        keySource = new RemoteJWKSet(new URL("https://login.windows.net/common/discovery/keys"));
    }

    @Override
    public TenantConfig getTenantConfig() {
        return config;
    }
        
    @Override
    public CachedUserInfo queryForUserInfo(UserSearchParams parameters) {
        if (parameters.getTenantId() == null || parameters.getTenantId().isEmpty()) {
            throw new IllegalArgumentException("Tenant ID is required.");
        }
        if (parameters.getUserId() != null) {
            return queryUserInfo(parameters.getUserId());
        } else if (parameters.getEmailAddress() != null || parameters.getEmailNickname() != null) {
            String emailAddress = parameters.getEmailAddress();
            String nick = parameters.getEmailNickname();
            List<CachedUserInfo> list = queryUsersInfo();
            for (CachedUserInfo info : list) {
                if (
                        StringUtil.equalsIgnoreCase(emailAddress, info.getEmailAddress())
                        ||
                        StringUtil.equalsIgnoreCase(nick, info.getEmailNickname())
                   )
                {
                    return info;
                }
            }
            return null;
        } else {
            throw new IllegalArgumentException("Either user ID or email address required for query.");
        }
    }
    
    /**
     * Queries user information from Azure active Directory through calls to Graph.
     * @param tenantId The unique ID of the tenant to be queried.
     * @param userId The unique ID of the user in AAD.
     * @return The information of the user associated with the ID or null if
     * no matches were found.
     */
    private CachedUserInfo queryUserInfo(String userId) {
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
            String s = String.format("https://graph.windows.net/%s/users/%s?api-version=1.6", URLEncoder.encode(config.getTenantId(), "UTF-8"), URLEncoder.encode(userId, "UTF-8"));
            url = new URL(s);
        } catch (MalformedURLException | UnsupportedEncodingException ex) {
            // Shouldn't happen
            return null;
        }
        // Load Azure Active Directory groups
        HttpURLConnection conn = null;
        BufferedReader reader = null;
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
                return userInfoFromData(data);
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
    
    private List<CachedUserInfo> queryUsersInfo() {
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
            url = new URL("https://graph.windows.net/myorganization/users/?api-version=1.6");
        } catch (MalformedURLException ex) {
            // Shouldn't happen
            return Collections.emptyList();
        }
        // Load Azure Active Directory groups
        HttpURLConnection conn = null;
        BufferedReader reader = null;
        int tries = 0;
        while (tries < config.getMaxRetries()) {
            try {
                HttpRequest req = REQ_FACTORY.buildGetRequest(new GenericUrl(url));
                req.getHeaders().setAccept(MIME_JSON);
                req.getHeaders().setAuthorization("Bearer " + accessToken);
                HttpResponse resp = req.execute();
                String respText = HttpTransportClient.loadContent(resp);
                LOGGER.debug("User Results: {}", respText);
                Map<String, Object> data = MAPPER.readValue(respText, new TypeReference<Map<String, Object>>(){});
                List<Map<String, Object>> userDataList = (List<Map<String, Object>>)data.get("value");
                List<CachedUserInfo> list = new LinkedList<>();
                for (Map<String, Object> userData : userDataList) {
                    list.add(userInfoFromData(userData));
                }
                return list;
            } catch (IOException ex) {
                if (ex instanceof HttpResponseException) {
                    HttpResponseException re = (HttpResponseException)ex;
                    if (re.getStatusCode() == 404) {
                        return null;
                    } else {
                        LOGGER.error("Error loading Users Info from tenant {}: attempt: {} status: {} : message: {}", config.getTenantId(), tries, re.getStatusCode(), re.getStatusMessage(), ex);
                    }
                } else {
                    LOGGER.error("Error loading users info: attempt: {}", tries, ex);
                }
                if (tries >= config.getMaxRetries()) {
                    throw new SecurityException(String.format("Error loading users after %d attempts", tries), ex);
                } else {
                    // Wait a few moments before trying again
                    try { Thread.sleep(3000 * tries); } catch (InterruptedException ie) {}
                }
            } finally {
                IOUtils.closeQuietly(reader);
                if (conn != null) {
                    conn.disconnect();
                }
            }
        }
        return null;
    }
    
    /**
     * Obtains the user info from the Map returned from parsing the Graph JSON response.
     * @param tenantId Unique ID of the tenant in which the user exists.
     * @param data The Map of data parsed from Graph's JSON response.
     * @return The user information.
     */
    private CachedUserInfo userInfoFromData(Map<String, Object> data) {
        CachedUserInfo info = new CachedUserInfo();
        info.setTenantId(config.getTenantId());
        info.setFirstName(StringUtil.notNull(data.get("givenName")));
        String emailAddress = StringUtil.notNull(data.get("mail"));
        if (emailAddress.length() == 0) {
            emailAddress = StringUtil.notNull(data.get("userPrincipalName"));
        }
        emailAddress = emailAddress.toLowerCase();            
        info.setEmailAddress(emailAddress);
        info.setLastName(StringUtil.notNull(data.get("surname")));
        info.setUserId(StringUtil.notNull(data.get("objectId")));
        info.setEmailNickname(StringUtil.notNull(data.get("mailNickname")));
        return info;
    }

    @Override
    public JWKSource getKeySource() {        
        return keySource;
    }
    
    @Override
    public ServicesSessionBean createServicesSessionBean(JWTClaimsSet claimsSet) {
        ServicesSessionBean bean = new ServicesSessionBean();
        String audience = claimsSet.getClaim(CLAIM_AUDIENCE).toString();
        String tenantId = claimsSet.getClaim(CLAIM_TENANT_ID).toString();
        if (!config.getTenantId().equals(tenantId)) {
            // Not issued on behalf of this tenant.
            return null;
        }
        if (!audience.contains(config.getClientId())) {
            LOGGER.error("The provided token is not intended for this application.  Declaired audience: {}", audience);
            throw new SecurityException("The provided token is not intended for this application.");
        }
        bean.setTenantId(tenantId);
        bean.setTokenValid(true);

        // Determine if we have a User on-behalf-of or a service-to-service
        if (CLAIM_VAL_USER_IMPERSONATION.equals(claimsSet.getClaim(CLAIM_SCP))) {
            // User on-behalf-of
            createExtUserCredentials(claimsSet, bean);
        } else {
            // Treat it like a service-to-service
            createAppCredentials(claimsSet, bean);
        }
        return bean;
    }
    
    
    /**
     * Create a session bean based on server to server communication.
     * @param claimsSet The claims set of the connecting service.
     * @return The session bean.
     */
    private void createAppCredentials(JWTClaimsSet claimsSet, ServicesSessionBean bean) {
        String clientAppID = StringUtil.nullableToString(claimsSet.getClaim("appid"));
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
        creds.setFirstName(StringUtil.notNull(claimsSet.getClaim(CLAIM_GIVEN_NAME)));
        creds.setLastName(StringUtil.notNull(claimsSet.getClaim(CLAIM_FAMILY_NAME)));
        creds.setUserId(claimsSet.getClaim(CLAIM_OBJECT_ID).toString());
        if (claimsSet.getClaims().containsKey(CLAIM_EMAIL)) {
            // External users are likely to have email address set in this claim
            String s = claimsSet.getClaim(CLAIM_EMAIL).toString();
            if (s.matches(RegexUtils.EMAIL_REGEX)) {
                creds.setEmailAddress(s.toLowerCase());
            }
        } else if (claimsSet.getClaim(CLAIM_UNIQUE_NAME).toString().matches(RegexUtils.EMAIL_REGEX)) {
            creds.setEmailAddress(claimsSet.getClaim(CLAIM_UNIQUE_NAME).toString().toLowerCase());
        }
        bean.setCredentials(creds);
    }
}
