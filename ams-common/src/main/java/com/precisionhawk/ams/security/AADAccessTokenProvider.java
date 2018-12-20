package com.precisionhawk.ams.security;

import com.precisionhawk.ams.util.HttpClientUtil;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;

/**
 * A class which knows how to make the appropriate calls to Azure in order to
 * obtain access tokens.  The tokens are cached until they time out such that
 * subsequent calls do not cause delay.
 *
 * @author <a href="mailto:pchapman@pcsw.us">Philip A. Chapman</a>
 */
public final class AADAccessTokenProvider extends OAuthAccessTokenProvider {

    /**
     * Returns an access token for the indicated scope.  The tokens are cached
     * until they time out such that subsequent calls do not cause delay.
     * @param scope The scope to request a token for.
     * @return The Base64 encoded token.
     * @throws java.io.IOException Indicates an error querying Azure for the token.
     */
    public String obtainAccessTokenV2(String scope)
        throws IOException
    {
        AccessTokenResponse resp;
        synchronized (tokenCache) {
            resp = tokenCache.get("v2" + scope);
        }
        long nowTime = System.currentTimeMillis() / 1000; // Convert milliseconds ot seconds
        // If the Cert doesn't exist, has expired or will expire in the next BUFFER_TIME seconds get a new one.
        if (resp == null || nowTime >= resp.getExpiresOn()) {
            resp = queryAccessTokenV2(scope);
            synchronized (tokenCache) {
                tokenCache.put("v2" + scope, resp);
            }
        }
        return resp.getAccessToken();
    }
    
    /**
     * Queries Azure for an access token.
     * @param resource The resource to request a token for.
     * @return The Base64 encoded token.
     * @throws java.io.IOException Indicates an error querying Azure for the token.
     */
    @Override
    protected AccessTokenResponse queryAccessToken(String resource)
        throws IOException
    {
        CloseableHttpClient httpclient = null;
        CloseableHttpResponse response = null;
        try {
            List <NameValuePair> parameters = new LinkedList<>();
            parameters.add(new BasicNameValuePair("grant_type", "client_credentials"));
            parameters.add(new BasicNameValuePair("client_id", getClientId()));
            parameters.add(new BasicNameValuePair("client_secret", getClientSecret()));
            parameters.add(new BasicNameValuePair("resource", resource));
            httpclient = HttpClients.createDefault();
            HttpPost httpOp = new HttpPost(String.format("https://login.microsoftonline.com/%s/oauth2/token", getTenantId()));
            httpOp.addHeader("Accept", "application/json");
            httpOp.setEntity(new UrlEncodedFormEntity(parameters));
            response = httpclient.execute(httpOp);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                String rawJSON = HttpClientUtil.consumeEntity(response.getEntity());
                return MAPPER.readValue(rawJSON, AccessTokenResponse.class);
            } else {
                throw new IOException(String.format("Error obtaining access token, got status: %d, resonse: %s", response.getStatusLine().getStatusCode(), HttpClientUtil.consumeEntity(response.getEntity())));
            }
        } finally {
            IOUtils.closeQuietly(response);
            IOUtils.closeQuietly(httpclient);
        }
    }
    
    /**
     * Queries Azure for an access token.
     * @param scope The scope to request a token for.
     * @return The Base64 encoded token.
     * @throws java.io.IOException Indicates an error querying Azure for the token.
     */
    private AccessTokenResponse queryAccessTokenV2(String scope)
        throws IOException
    {
        CloseableHttpClient httpclient = null;
        CloseableHttpResponse response = null;
        try {
            List <NameValuePair> parameters = new LinkedList<>();
            parameters.add(new BasicNameValuePair("grant_type", "client_credentials"));
            parameters.add(new BasicNameValuePair("client_id", getClientId()));
            parameters.add(new BasicNameValuePair("client_secret", getClientSecret()));
            parameters.add(new BasicNameValuePair("scope", scope));
            httpclient = HttpClients.createDefault();
            HttpPost httpOp = new HttpPost("https://login.microsoftonline.com/common/oauth2/v2.0/token");
            httpOp.addHeader("Accept", "application/json");
            httpOp.setEntity(new UrlEncodedFormEntity(parameters));
            response = httpclient.execute(httpOp);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                String rawJSON = HttpClientUtil.consumeEntity(response.getEntity());
                return MAPPER.readValue(rawJSON, AccessTokenResponse.class);
            } else {
                throw new IOException(String.format("Error obtaining access token, got status: %d, resonse: %s", response.getStatusLine().getStatusCode(), HttpClientUtil.consumeEntity(response.getEntity())));
            }
        } finally {
            IOUtils.closeQuietly(response);
            IOUtils.closeQuietly(httpclient);
        }
    }
}
