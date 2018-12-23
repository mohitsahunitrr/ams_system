package com.precisionhawk.ams.security;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The base class for 0Auth access token providers.  The tokens are cached until
 * they time out such that subsequent calls do not cause delay.
 *
 * @author <a href="mailto:pchapman@pcsw.us">Philip A. Chapman</a>
 */
public abstract class OAuthAccessTokenProvider implements AccessTokenProvider {
    
    protected final ObjectMapper MAPPER = new ObjectMapper();
    protected final Logger LOGGER = LoggerFactory.getLogger(getClass());
    
    /** The application's client ID within Azure. */
    private String clientId;
    public String getClientId() {
        return clientId;
    }
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    /** The application's client secret within Azure. */
    private String clientSecret;
    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }
    protected String getClientSecret() {
        return clientSecret;
    }
    
    /** The unique ID of the Azure tenant. */
    private String tenantId;
    public String getTenantId() {
        return tenantId;
    }
    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }
    
    /** The cache of tokens that have been obtained from Azure. */
    protected final Map<String, AccessTokenResponse> tokenCache = Collections.synchronizedMap(new HashMap<>());
    
    @Override
    public void configure(AccessTokenProviderConfig config) {
        setClientId(config.getClientId());
        setClientSecret(config.getClientSecret());
        setTenantId(config.getTenantId());
    }
    
    /**
     * Returns an access token for the indicated resource.  The tokens are cached
     * until they time out such that subsequent calls do not cause delay.
     * @param resource The resource to request a token for.
     * @return The Base64 encoded token.
     * @throws java.io.IOException Indicates an error querying for the token.
     */
    @Override
    public String obtainAccessToken(String resource)
        throws IOException
    {
        AccessTokenResponse resp;
        synchronized (tokenCache) {
            resp = tokenCache.get(resource);
        }
        long nowTime = System.currentTimeMillis() / 1000; // Convert milliseconds to seconds
        // If the Cert doesn't exist, has expired or will expire in the next BUFFER_TIME seconds get a new one.
        if (resp != null) {
            LOGGER.debug(
                "Found token for resource {}\tExpire time: {}\tCurrent Time: {}",
                resource, resp.getExpiresOn(), nowTime
            );
            if (nowTime >= resp.getExpiresOn()) {
                LOGGER.debug("Expiring token due to invalidation time.");
                resp = null;
            }
        }
        if (resp == null) {
            resp = queryAccessToken(resource);
            if (resp.getExpiresOn() == null) {
                resp.setExpiresOn(nowTime + resp.getExpiresIn());
            }
            synchronized (tokenCache) {
                tokenCache.put(resource, resp);
            }
            LOGGER.debug(
                "Obtained new token for resource {}\tNot before time: {}\tExpire time: {}\tCurrent Time: {}",
                resource, resp.getNotBefore(), resp.getExpiresOn(), (System.currentTimeMillis() / 1000)
            );
        }
        return resp.getAccessToken();
    }
    
    /**
     * Queries Azure for an access token.
     * @param resource The resource to request a token for.
     * @return The Base64 encoded token.
     * @throws java.io.IOException Indicates an error querying for the token.
     */
    protected abstract AccessTokenResponse queryAccessToken(String resource)
        throws IOException;
}
