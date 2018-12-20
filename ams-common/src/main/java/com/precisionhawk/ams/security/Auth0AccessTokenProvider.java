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
 * An access token provider that authenticates against Auth0.
 *
 * @author pchapman
 */
public final class Auth0AccessTokenProvider extends OAuthAccessTokenProvider {

    @Override
    protected AccessTokenResponse queryAccessToken(String resource) throws IOException {
        CloseableHttpClient httpclient = null;
        CloseableHttpResponse response = null;
        try {
            List <NameValuePair> parameters = new LinkedList<>();
            parameters.add(new BasicNameValuePair("grant_type", "client_credentials"));
            parameters.add(new BasicNameValuePair("client_id", getClientId()));
            parameters.add(new BasicNameValuePair("client_secret", getClientSecret()));
            parameters.add(new BasicNameValuePair("audience", resource));
            httpclient = HttpClients.createDefault();
            HttpPost httpOp = new HttpPost(String.format("https://%s/oauth/token", getTenantId())); // TenantID is the domain, such as precisionhawk.auth0.com
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
