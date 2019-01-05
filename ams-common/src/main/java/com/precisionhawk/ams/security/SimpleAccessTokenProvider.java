package com.precisionhawk.ams.security;

import com.precisionhawk.ams.support.jackson.ObjectMapperFactory;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * An access token provider which passes client ID (login) and password hash to
 * the services for authentication and authorization;
 *
 * @author pchapman
 */
public class SimpleAccessTokenProvider implements AccessTokenProvider {

    private final Object LOCK = new Object();
    private String token;

    /** The application's client ID (login). */
    private String clientId;
    public String getClientId() {
        return clientId;
    }
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    /** The application's client secret (password hash). */
    private String clientSecret;
    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }
    protected String getClientSecret() {
        return clientSecret;
    }
    
    @Override
    public void configure(AccessTokenProviderConfig config) {
        if (config.getClientId() == null || config.getClientId().isEmpty()) {
            throw new IllegalArgumentException("Client ID required");
        } else {
            setClientId(config.getClientId());
        }
        if (config.getClientSecret() == null || config.getClientSecret().isEmpty()) {
            throw new IllegalArgumentException("Client secret required");
        } else {
            setClientSecret(config.getClientSecret());
        }
    }

    @Override
    public String obtainAccessToken(String resource) throws IOException {
        synchronized (LOCK) {
            if (token == null) {
                Map<String, String> map = new HashMap<>();
                map.put("login", getClientId());
                map.put("passwordHash", getClientSecret());
                token = ObjectMapperFactory.getObjectMapper().writeValueAsString(map);
            }
        }
        return token;
    }
    
}
