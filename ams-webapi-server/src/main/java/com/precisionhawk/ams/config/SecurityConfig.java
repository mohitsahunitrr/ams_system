/*
 * All rights reserved.
 */

package com.precisionhawk.ams.config;

import java.util.Map;

/**
 *
 * @author <a href="mailto:pchapman@pcsw.us">Philip A. Chapman</a>
 */
public interface SecurityConfig {
    
    String getSecurityImplementation();
    
    Long getUserInfoCacheTime();

    /**
     * Configurations for this service within OAuth as a registered application mapped
     * by tenant ID.  The application can be registered with multiple OAuth tenants
     * allowing for authentication against each of them, provided they are all against
     * similar services.
     */
    Map<String, TenantConfig> getTenantConfigurations();
    
    /**
     * Configurations for clients which may programmatically access these services mapped
     * by OAuth Client ID.  This allows for server-to-server communication.
     */
    Map<String, ClientConfig> getClientConfigurations();
}
