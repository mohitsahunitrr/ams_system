/*
 * All rights reserved.
 */

package com.precisionhawk.ams.config;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author <a href="mailto:pchapman@pcsw.us">Philip A. Chapman</a>
 */
public class SecurityConfig {
    
    private String securityImplementation;
    public String getSecurityImplementation() {
        return securityImplementation;
    }
    public void setSecurityImplementation(String impl) {
        this.securityImplementation = impl;
    }
    
    private Integer maxRetries;
    public Integer getMaxRetries() {
        return maxRetries;
    }
    public void setMaxRetries(Integer maxRetries) {
        this.maxRetries = maxRetries;
    }
    
    private Long userInfoCacheTime;
    public Long getUserInfoCacheTime() {
        return userInfoCacheTime;
    }
    public void setUserInfoCacheTime(Long time) {
        this.userInfoCacheTime = time;
    }

    /**
     * Configurations for this service within Azure as a registered application mapped
     * by tenant ID.  The application can be registered with multiple Azure tenants
     * allowing for authentication against each of them.
     */
    private Map<String, TenantConfig> tenantConfigurations = new HashMap<>();
    public Map<String, TenantConfig> getTenantConfigurations() {
        return tenantConfigurations;
    }
    public void setTenantConfigurations(Map<String, TenantConfig> tenantConfigurations) {
        this.tenantConfigurations = tenantConfigurations;
    }
    
    /**
     * Configurations for clients which may programmatically access these services mapped
     * by Azure Client ID.  This allows for server-to-server communication.
     */
    private Map<String, ClientConfig> clientConfigurations = new HashMap<>();
    public Map<String, ClientConfig> getClientConfigurations() {
        return clientConfigurations;
    }
    public void setClientConfigurations(Map<String, ClientConfig> clientConfigurations) {
        this.clientConfigurations = clientConfigurations;
    }
}
