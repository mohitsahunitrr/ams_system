/*
 * All rights reserved.
 */

package com.precisionhawk.ams.service.oauth.aad;

import com.precisionhawk.ams.service.oauth.GroupsProvider;
import com.precisionhawk.ams.service.oauth.OAuthSecurityService;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpResponseException;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.precisionhawk.ams.bean.security.CachedUserInfo;
import com.precisionhawk.ams.bean.security.UserSearchParams;
import com.precisionhawk.ams.dao.SecurityDao;
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
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.InternalServerErrorException;
import org.apache.commons.io.IOUtils;
import org.codehaus.jackson.type.TypeReference;
import org.papernapkin.liana.util.StringUtil;

/**
 * The class responsible for applying security rules and setting up the session
 * and user credentials objects based on authentication against and
 * authorization based on Azure Active Directory.
 *
 * @author <a href="mailto:pchapman@pcsw.us">Philip A. Chapman</a>
 */
@Named
public final class AADSecurityService extends OAuthSecurityService {
    
//    private static final String CLAIM_AUDIENCE = "aud";
//    private static final String CLAIM_EMAIL = "email";
//    private static final String CLAIM_FAMILY_NAME = "family_name";
//    private static final String CLAIM_GIVEN_NAME = "given_name";
//    private static final String CLAIM_OBJECT_ID = "oid";
//    private static final String CLAIM_SCP = "scp";
//    private static final String CLAIM_TENANT_ID = "tid";
//    private static final String CLAIM_UNIQUE_NAME = "unique_name";
//    private static final String CLAIM_VAL_USER_IMPERSONATION = "user_impersonation";
    
    @Inject
    protected SecurityDao dao;
    
    protected final JWKSource KEY_SOURCE;
    
    private final Map<String, GroupsProvider> groupsProviders = new HashMap();
    
    public AADSecurityService() throws MalformedURLException {
        KEY_SOURCE = new RemoteJWKSet(new URL("https://login.windows.net/common/discovery/keys"));
    }
        
    @Override
    protected CachedUserInfo queryForUserInfo(UserSearchParams parameters) {
        if (parameters.getTenantId() == null) {
            throw new IllegalArgumentException("Tenant ID required");
        }
        if (parameters.getUserId() != null) {
            return queryUserInfo(parameters.getTenantId(), parameters.getUserId());
        } else if (parameters.getEmailAddress() != null || parameters.getEmailNickname() != null) {
            String emailAddress = parameters.getEmailAddress();
            String nick = parameters.getEmailNickname();
            List<CachedUserInfo> list = queryUsersInfo(parameters.getTenantId());
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
    
    static final String GRAPH_RESOURCE = "https://graph.windows.net/";
    
    /**
     * Queries user information from Azure active Directory through calls to Graph.
     * @param tenantId The unique ID of the tenant to be queried.
     * @param userId The unique ID of the user in AAD.
     * @return The information of the user associated with the ID or null if
     * no matches were found.
     */
    private CachedUserInfo queryUserInfo(String tenantId, String userId) {
        String accessToken = null;
        try {
            accessToken = accessTokenProvider(tenantId).obtainAccessToken(GRAPH_RESOURCE);
        } catch (IOException ex) {
            LOGGER.error("Error gaining access token for Microsoft Graph", ex);
        }
        if (accessToken == null) {
            throw new InternalServerErrorException("Unable to obtain access token for Microsoft Graph APIs.");
        }
        URL url;
        try {
            String s = String.format("https://graph.windows.net/%s/users/%s?api-version=1.6", URLEncoder.encode(tenantId, "UTF-8"), URLEncoder.encode(userId, "UTF-8"));
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
    
    protected List<CachedUserInfo> queryUsersInfo(String tenantId) {
        String accessToken = null;
        try {
            accessToken = accessTokenProvider(tenantId).obtainAccessToken("https://graph.windows.net/");
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
                    list.add(userInfoFromData(tenantId, userData));
                }
                return list;
            } catch (IOException ex) {
                if (ex instanceof HttpResponseException) {
                    HttpResponseException re = (HttpResponseException)ex;
                    if (re.getStatusCode() == 404) {
                        return null;
                    } else {
                        LOGGER.error("Error loading Users Info from tenant {}: attempt: {} status: {} : message: {}", tenantId, tries, re.getStatusCode(), re.getStatusMessage(), ex);
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
    private CachedUserInfo userInfoFromData(String tenantId, Map<String, Object> data) {
        CachedUserInfo info = new CachedUserInfo();
        info.setTenantId(tenantId);
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
    protected JWKSource getKeySource() throws IOException {
        return KEY_SOURCE;
    }
}
