/*
 * All rights reserved.
 */

package com.precisionhawk.ams.service.oauth.aad;

import com.precisionhawk.ams.service.oauth.GroupsProvider;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpResponseException;
import com.nimbusds.jwt.JWTClaimsSet;
import com.precisionhawk.ams.bean.security.ServicesSessionBean;
import com.precisionhawk.ams.bean.security.UserCredentials;
import com.precisionhawk.ams.security.AccessTokenProvider;
import com.precisionhawk.ams.support.http.HttpTransportClient;
import static com.precisionhawk.ams.support.http.HttpTransportClient.MIME_JSON;
import static com.precisionhawk.ams.support.http.HttpTransportClient.REQ_FACTORY;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.codehaus.jackson.type.TypeReference;

/**
 * An implementation of AADGroupsProvider which makes use of calls to MS Graph.
 *
 * @author <a href="mailto:pchapman@pcsw.us">Philip A. Chapman</a>
 */
public class MSGraphAADGroupsProvider extends GroupsProvider {
    
    private final String API_VERSION = "2013-04-05";
    private final String MS_GRAPH_RESOURCE = "https://graph.microsoft.com/";
    private final String MEMBER_GROUPS_URL = MS_GRAPH_RESOURCE + "v1.0/users/%s/memberOf";
    
    @Override
    public Set<String> loadGroupIDs(AccessTokenProvider tokenProvider, ServicesSessionBean session, JWTClaimsSet claimsSet) throws SecurityException
    {
        if (!(session.getCredentials() instanceof UserCredentials)) {
            throw new SecurityException("Groups can only be loaded for user credentials.");
        }
        UserCredentials creds = (UserCredentials)session.getCredentials();
        try {
            // Load Azure Active Directory groups
            String url = String.format(MEMBER_GROUPS_URL, creds.getUserId());
            HttpRequest req = REQ_FACTORY.buildGetRequest(new GenericUrl(url));
            req.setNumberOfRetries(getMaxRetries());
            req.setReadTimeout(0); // Infinite
            req.getHeaders().setAccept(MIME_JSON);
            req.getHeaders().setAuthorization("Bearer " + tokenProvider.obtainAccessToken(MS_GRAPH_RESOURCE));
            req.getHeaders().set("api-version", API_VERSION);
            HttpResponse resp = req.execute();
            String respText = HttpTransportClient.loadContent(resp);
            Map<String, Object> data = mapper.readValue(respText, new TypeReference<Map<String, Object>>(){});
            LOGGER.debug("Group data read.");

            Set<String> aadGroupIDs = new HashSet();
            List<Map<String, Object>> groupMaps = (List<Map<String, Object>>)data.get("value");
            for (Map<String, Object> groupMap : groupMaps) {
                aadGroupIDs.add(groupMap.get("id").toString());
            }

            return aadGroupIDs;
        } catch (IOException ex) {
        if (ex instanceof HttpResponseException) {
                HttpResponseException re = (HttpResponseException)ex;
                LOGGER.error("Error loading AAD groups: status: {} message: '{}' content: {}", re.getStatusCode(), re.getStatusMessage(), re.getContent());
            }
            LOGGER.error("Unable to load AAD Group IDs", ex);
            throw new SecurityException("Unable to load AAD Group IDs", ex);
        }
    }
}
