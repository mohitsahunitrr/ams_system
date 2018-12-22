/*
 * All rights reserved.
 */

package com.precisionhawk.ams.bean.security;

import java.util.HashMap;
import java.util.Map;

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
    private Map<String, Object> appData = new HashMap<>();

    /**
     * A map which holds misc application data about the user or session.
     * @return The map of data.
     */
    public Map<String, Object> getAppData() {
        return appData;
    }
    /**
     * A map which holds misc application data about the user or session.
     * @param appData The map of data.
     */
    public void setAppData(Map<String, Object> appData) {
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
