/*
 * All rights reserved.
 */

package com.precisionhawk.ams.bean.security;

/**
 *
 * @author <a href="mailto:pchapman@pcsw.us">Philip A. Chapman</a>
 */
public class ServicesSessionBean {

    private String windAMSAPIAccessToken;
    private Credentials credentials;
    private String reason;
    private String tenantId;
    private boolean tokenValid;
    private String appData;

    public String getAppData() {
        if (appData == null) {
            return "{}";
        }
        return appData;
    }
    public void setAppData(String appData) {
        this.appData = appData;
    }

    public String getWindAMSAPIAccessToken() {
        return windAMSAPIAccessToken;
    }
    public void setWindAMSAPIAccessToken(String bearerToken) {
        this.windAMSAPIAccessToken = bearerToken;
    }

    public Credentials getCredentials() {
        return credentials;
    }
    public void setCredentials(Credentials credentials) {
        this.credentials = credentials;
    }

    public String getReason() {
        return reason;
    }
    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getTenantId() {
        return tenantId;
    }
    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }
    
    public boolean isTokenValid() {
        return tokenValid;
    }
    public void setTokenValid(boolean tokenValid) {
        this.tokenValid = tokenValid;
    }
}
