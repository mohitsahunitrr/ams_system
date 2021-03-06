/*
 * All rights reserved.
 */

package com.precisionhawk.ams.config;

/**
 * Configuration for this service within Azure as a registered application.
 *
 * @author <a href="mailto:pchapman@pcsw.us">Philip A. Chapman</a>
 */
public class TenantConfig {
    
    private String apiId;
    private String authProvider;
    private String clientId;
    private String groupProvider;
    private Integer maxRetries;
    private String organizationId;
    private String passwordKey;
    private String tenantId;
    private String tenantName;
    private String userManagementResource;

    /** The unique ID of the application's registration within the OAuth tenant as an API. */
    public String getApiId() {
        return apiId;
    }
    /** The unique ID of the application's registration within the OAuth tenant as an API. */
    public void setApiId(String apiId) {
        this.apiId = apiId;
    }

    public String getAuthProvider() {
        return authProvider;
    }
    public void setAuthProvider(String authProvider) {
        this.authProvider = authProvider;
    }

    /** The unique ID of the application's registration within the OAuth tenant as a client. */
    public String getClientId() {
        return clientId;
    }
    /** The unique ID of the application's registration within the OAuth tenant as a client. */
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    /** The class of the provider to provide group assignments for authenticated users. */
    public String getGroupProvider() {
        return groupProvider;
    }
    /** The class of the provider to provide group assignments for authenticated users. */
    public void setGroupProvider(String groupProvider) {
        this.groupProvider = groupProvider;
    }

    public Integer getMaxRetries() {
        return maxRetries;
    }
    public void setMaxRetries(Integer maxRetries) {
        this.maxRetries = maxRetries;
    }

    /** The organization to which users of this tenant should be associated.  May be null. */
    public String getOrganizationId() {
        return organizationId;
    }
    /** The organization to which users of this tenant should be associated.  May be null. */
    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    /** The Azure password key for this application. */
    public String getPasswordKey() {
        return passwordKey;
    }
    /** The Azure password key for this application. */
    public void setPasswordKey(String passwordKey) {
        this.passwordKey = passwordKey;
    }

    /** The unique ID for the tenant in OAuth to which this app is registered. */
    public String getTenantId() {
        return tenantId;
    }
    /** The unique ID for the tenant in OAuth to which this app is registered. */
    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    /** The name of the tenant. */
    public String getTenantName() {
        return tenantName;
    }
    /** The name of the tenant. */
    public void setTenantName(String tenantName) {
        this.tenantName = tenantName;
    }
    
    public String getUserManagementResource() {
        return userManagementResource;
    }
    public void setUserManagementResource(String userManagementResource) {
        this.userManagementResource = userManagementResource;
    }    
}
