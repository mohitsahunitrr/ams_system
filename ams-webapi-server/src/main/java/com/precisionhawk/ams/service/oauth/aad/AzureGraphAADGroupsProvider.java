/*
 * All rights reserved.
 */

package com.precisionhawk.ams.service.oauth.aad;

import com.precisionhawk.ams.service.oauth.GroupsProvider;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpContent;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpResponseException;
import com.nimbusds.jwt.JWTClaimsSet;
import com.precisionhawk.ams.bean.security.ServicesSessionBean;
import com.precisionhawk.ams.bean.security.UserCredentials;
import com.precisionhawk.ams.security.AADAccessTokenProvider;
import com.precisionhawk.ams.security.AccessTokenProvider;
import com.precisionhawk.ams.support.http.HttpTransportClient;
import static com.precisionhawk.ams.support.http.HttpTransportClient.MIME_JSON;
import static com.precisionhawk.ams.support.http.HttpTransportClient.REQ_FACTORY;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.codehaus.jackson.type.TypeReference;

/**
 * An implementation of AADGroupsProvider which makes use of calls to Azure Graph.
 *
 * @author <a href="mailto:pchapman@pcsw.us">Philip A. Chapman</a>
 */
public class AzureGraphAADGroupsProvider extends GroupsProvider {
    
    @Override
    public Set<String> loadGroupIDs(AccessTokenProvider tokenProvider, ServicesSessionBean session, JWTClaimsSet claimsSet) throws SecurityException {
        Set<String> groupIDs = null;
        try {
            groupIDs = loadAADGroupIDsEnMass(tokenProvider, session);
        } catch (SecurityException ex) {
            LOGGER.error("Unable to load all groups at once", ex);
        }
        if (groupIDs == null) {
            try {
                groupIDs = loadAADGroupIDsIndividually(tokenProvider, session);
            } catch (SecurityException ex) {
                LOGGER.error("Unable to load all groups individually", ex);
                throw ex;
            }
        }
        return groupIDs;
    }
        
    private Set<String> loadAADGroupIDsEnMass(AccessTokenProvider tProvider, ServicesSessionBean session) throws SecurityException {
        if (!(session.getCredentials() instanceof UserCredentials)) {
            throw new SecurityException("Groups can only be loaded for user credentials.");
        }
        AADAccessTokenProvider tokenProvider;
        if (tProvider instanceof AADAccessTokenProvider) {
            tokenProvider = (AADAccessTokenProvider)tProvider;
        } else {
            throw new IllegalArgumentException("Only AADAccessTokenProvider instances are acceptable for this opperation.");
        }
        UserCredentials creds = (UserCredentials)session.getCredentials();
        boolean success = false;
        int tries = 0;
        Set<String> aadGroupIDs = new HashSet();
        while ((!success) && tries < getMaxRetries()) {
            tries++;
            try {
                String url = String.format("https://graph.windows.net/%s/users/%s/getMemberGroups?api-version=1.6", tokenProvider.getTenantId(), creds.getUserId());
                HttpContent content = new ByteArrayContent("application/json", "{\"securityEnabledOnly\":true}".getBytes());
                HttpRequest req = REQ_FACTORY.buildPostRequest(new GenericUrl(url), content);
                req.getHeaders().setAccept(MIME_JSON);
                req.getHeaders().setAuthorization("Bearer " + tokenProvider.obtainAccessToken(AADSecurityService.GRAPH_RESOURCE));
                HttpResponse resp = req.execute();
                String respText = HttpTransportClient.loadContent(resp);
                Map<String, Object> data = mapper.readValue(respText, new TypeReference<Map<String, Object>>(){});
                for (Object o : (Collection<Object>)data.get("value")) {
                    aadGroupIDs.add(o.toString());
                }
                success = true;
            } catch (IOException ex) {
                if (ex instanceof HttpResponseException) {
                    HttpResponseException re = (HttpResponseException)ex;
                    LOGGER.error("Error loading AAD groups: attempt: {} status: {} : message: {}", tries, re.getStatusCode(), re.getStatusMessage(), ex);
                } else {
                    LOGGER.error("Error loading AAD groups: attempt: {}", tries, ex);
                }
                if (tries >= getMaxRetries()) {
                    throw new SecurityException(String.format("Error loading AAD groups after %d attempts", tries), ex);
                } else {
                    // Wait a few moments before trying again
                    try { Thread.sleep(3000 * tries); } catch (InterruptedException ie) {}
                }
            }
        }
        return aadGroupIDs;
    }

    private static final String IS_MEMBER_OF_JSON = "{\"groupId\":\"%s\",\"memberId\":\"%s\"}";

    // I don't like making this call for each group.  Thankfully, there are few.
    private Set<String> loadAADGroupIDsIndividually(AccessTokenProvider tProvider, ServicesSessionBean session) throws SecurityException {
        if (!(session.getCredentials() instanceof UserCredentials)) {
            throw new SecurityException("Groups can only be loaded for user credentials.");
        }
        AADAccessTokenProvider tokenProvider;
        if (tProvider instanceof AADAccessTokenProvider) {
            tokenProvider = (AADAccessTokenProvider)tProvider;
        } else {
            throw new IllegalArgumentException("Only AADAccessTokenProvider instances are acceptable for this opperation.");
        }
        UserCredentials creds = (UserCredentials)session.getCredentials();
        boolean success;
        int tries;
        Set<String> aadGroupIDs = new HashSet();
        String url = String.format("https://graph.windows.net/%s/isMemberOf?api-version=1.6", tokenProvider.getTenantId());
        GenericUrl gurl = new GenericUrl(url);

        for (String aadGroupId : dao.selectAADGroupsByTenant(tokenProvider.getTenantId())) {
            success = false;
            tries = 0;
            while ((!success) && tries < getMaxRetries()) {
                tries++;
                try {
                    HttpContent content = new ByteArrayContent("application/json", String.format(IS_MEMBER_OF_JSON, aadGroupId, creds.getUserId()).getBytes());
                    HttpRequest req = REQ_FACTORY.buildPostRequest(gurl, content);
                    req.getHeaders().setAccept(MIME_JSON);
                    req.getHeaders().setAuthorization("Bearer " + tokenProvider.obtainAccessToken(AADSecurityService.GRAPH_RESOURCE));
                    HttpResponse resp = req.execute();
                    String respText = HttpTransportClient.loadContent(resp);
                    Map<String, Object> data = mapper.readValue(respText, new TypeReference<Map<String, Object>>(){});
                    respText = data.get("value").toString();
                    if (Boolean.valueOf(respText)) {
                        aadGroupIDs.add(aadGroupId);
                    }
                    success = true;
                } catch (IOException ex) {
                    if (ex instanceof HttpResponseException) {
                        HttpResponseException re = (HttpResponseException)ex;
                        LOGGER.error("Error loading AAD groups: attempt: {} status: {} : message: {}", tries, re.getStatusCode(), re.getStatusMessage(), ex);
                    } else {
                        LOGGER.error("Error loading AAD groups: attempt: {}", tries, ex);
                    }
                    if (tries >= getMaxRetries()) {
                        throw new SecurityException(String.format("Error loading AAD groups after %d attempts", tries), ex);
                    } else {
                        // Wait a few moments before trying again
                        try { Thread.sleep(3000 * tries); } catch (InterruptedException ie) {}
                    }
                }
            }
        }
        return aadGroupIDs;
    }
}
