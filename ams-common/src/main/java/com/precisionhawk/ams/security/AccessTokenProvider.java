package com.precisionhawk.ams.security;

import java.io.IOException;

/**
 * An interface implemented by a class capable of generating or retrieving an
 * access token for a service call.
 *
 * @author pchapman
 */
public interface AccessTokenProvider {
    
    void configure(AccessTokenProviderConfig config);
    
    /**
     * Returns an access token for the indicated resource.
     * @param resource The resource to request a token for.
     * @return The Base64 encoded token.
     * @throws java.io.IOException Indicates an error obtaining the token.
     */
    String obtainAccessToken(String resource) throws IOException;
}
