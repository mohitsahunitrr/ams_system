package com.precisionhawk.ams.service.oauth.auth0;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpResponseException;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.precisionhawk.ams.bean.security.CachedUserInfo;
import com.precisionhawk.ams.bean.security.UserSearchParams;
import com.precisionhawk.ams.config.TenantConfig;
import com.precisionhawk.ams.service.oauth.DelegatingJWKSource;
import com.precisionhawk.ams.service.oauth.OAuthSecurityService;
import com.precisionhawk.ams.support.http.HttpTransportClient;
import static com.precisionhawk.ams.support.http.HttpTransportClient.MIME_JSON;
import static com.precisionhawk.ams.support.http.HttpTransportClient.REQ_FACTORY;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.ws.rs.InternalServerErrorException;
import org.codehaus.jackson.type.TypeReference;
import org.papernapkin.liana.util.StringUtil;

/**
 * An implementation of the security service which validates users authenticated
 * against Auth0.
 *
 * @author pchapman
 */
public class Auth0SecurityService extends OAuthSecurityService {
    
    private JWKSource keySource;
    private final Object LOCK = new Object();

    @Override
    protected JWKSource getKeySource() throws IOException {
        synchronized (LOCK) {
            if (keySource == null) {
                Map<String, JWKSource> sources = new HashMap<>();
                for (TenantConfig cfg : getSecurityConfig().getTenantConfigurations().values()) {
                    if (!sources.containsKey(cfg.getTenantId())) {
                        sources.put(cfg.getTenantId(), new RemoteJWKSet(new URL(String.format("https://%s/.well-known/jwks.json", cfg.getTenantId()))));
                    }
                }
                DelegatingJWKSource s = new DelegatingJWKSource();
                s.addAll(sources.values());
                keySource = s;
            }
        }
        return keySource;
    }
    
    @Override
    protected CachedUserInfo queryForUserInfo(UserSearchParams parameters) {
        if (parameters.getTenantId() == null) {
            throw new IllegalArgumentException("Tenant ID required");
        }
        if (parameters.getUserId() != null && !parameters.getUserId().isEmpty()) {
            return queryUserInfo(parameters.getTenantId(), parameters.getUserId());
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
    
    protected CachedUserInfo queryUserInfo(String tenantId, String userId) {
        String accessToken = null;
        try {
            accessToken = accessTokenProvider(tenantId).obtainAccessToken(getUserManagmentAPIID());
        } catch (IOException ex) {
            LOGGER.error("Error gaining access token for Microsoft Graph", ex);
        }
        if (accessToken == null) {
            throw new InternalServerErrorException("Unable to obtain access token for Microsoft Graph APIs.");
        }
        URL url;
        try {
            String s = String.format("https://%s/api/v2/users/%s", URLEncoder.encode(tenantId, "UTF-8"), URLEncoder.encode(userId, "UTF-8"));
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
                return userInfoFromData(tenantId, data);
            } catch (IOException ex) {
                if (ex instanceof HttpResponseException) {
                    HttpResponseException re = (HttpResponseException)ex;
                    if (re.getStatusCode() == 404) {
                        return null;
                    } else {
                        LOGGER.error("Error loading User Info from tenant {} user{}: attempt: {} status: {} : message: {}", tenantId, userId, tries, re.getStatusCode(), re.getStatusMessage(), ex);
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
    
    /**
     * Obtains the user info from the Map returned from parsing the User Management API's response.
     * @see https://auth0.com/docs/api/management/v2#!/Users/get_users_by_id
     * @param tenantId Unique ID of the tenant in which the user exists.
     * @param data The Map of data parsed from User Management API's response.
     * @return The user information.
     */
    private CachedUserInfo userInfoFromData(String tenantId, Map<String, Object> data) {
        CachedUserInfo info = new CachedUserInfo();
        info.setTenantId(tenantId);
        info.setFirstName(StringUtil.notNull(data.get("given_name")));
        String emailAddress = StringUtil.notNull(data.get("email"));
        if (emailAddress != null) {
            emailAddress = emailAddress.toLowerCase();
        }
        info.setEmailAddress(emailAddress);
        info.setLastName(StringUtil.notNull(data.get("family_name")));
        info.setUserId(StringUtil.notNull(data.get("user_id")));
        return info;
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
                accessToken = accessTokenProvider(parameters.getTenantId()).obtainAccessToken(getUserManagmentAPIID());
            } catch (IOException ex) {
                LOGGER.error("Error gaining access token for Auth0", ex);
            }
            if (accessToken == null) {
                throw new InternalServerErrorException("Unable to obtain access token for Auth0.");
            }
            URL url;
            try {
                String s = String.format("https://%s/api/v2/users-by-email?email=%s", URLEncoder.encode(parameters.getTenantId(), "UTF-8"), URLEncoder.encode(parameters.getEmailAddress(), "UTF-8"));
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
                    List<Map<String, Object>> data = MAPPER.readValue(respText, new TypeReference<List<Map<String, Object>>>(){});
                    List<CachedUserInfo> list = new LinkedList<>();
                    for (Map<String, Object> userData : data) {
                        list.add(userInfoFromData(parameters.getTenantId(), userData));
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

    private String getUserManagmentAPIID() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
