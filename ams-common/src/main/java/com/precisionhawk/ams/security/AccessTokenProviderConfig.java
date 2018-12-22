package com.precisionhawk.ams.security;

/**
 *
 * @author pchapman
 */
public interface AccessTokenProviderConfig {
    
    public String getClientId();
    
    public String getClientSecret();
    
    public String getTenantId();
}
