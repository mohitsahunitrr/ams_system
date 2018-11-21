/*
 * All rights reserved.
 */

package com.precisionhawk.ams.config;

import java.util.Objects;

/**
 * Configuration for client applications which may access this service
 * (Server to Server communication).
 *
 * @author <a href="mailto:pchapman@pcsw.us">Philip A. Chapman</a>
 */
public class ClientConfig {

    private String clientId;
    /** The unique ID of the client as it is registered in Azure */
    public String getClientId() {
        return clientId;
    }
    /** The unique ID of the client as it is registered in Azure */
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    private String name;
    /** A user friendly name for the client. */
    public String getName() {
        return name;
    }
    /** A user friendly name for the client. */
    public void setName(String name) {
        this.name = name;
    }

    private String organizationId;
    /** A organizational access rights for the app. */
    public String getOrganizationId() {
        return organizationId;
    }
    /** A organizational access rights for the app. */
    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + Objects.hashCode(this.clientId);
        hash = 53 * hash + Objects.hashCode(this.organizationId);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ClientConfig other = (ClientConfig) obj;
        if (!Objects.equals(this.clientId, other.clientId)) {
            return false;
        }
        if (!Objects.equals(this.organizationId, other.organizationId)) {
            return false;
        }
        return true;
    }
    
    
}
