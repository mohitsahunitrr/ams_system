package com.precisionhawk.ams.security;

import java.io.IOException;

/**
 *
 * @author pchapman
 */
public class NoOppAccessTokenProvider implements AccessTokenProvider {

    @Override
    public String obtainAccessToken(String resource) throws IOException {
        return "NotImplemented";
    }
    
}
