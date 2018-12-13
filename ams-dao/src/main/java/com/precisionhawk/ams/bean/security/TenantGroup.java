/*
 * All rights reserved.
 */

package com.precisionhawk.ams.bean.security;

/**
 *
 * @author <a href="mailto:pchapman@pcsw.us">Philip A. Chapman</a>
 */
public class TenantGroup {

    private String addGroupId;
    private String id;
    private String groupId;
    private String tenantId;

    public String getAddGroupId() {
        return addGroupId;
    }
    public void setAddGroupId(String addGroupId) {
        this.addGroupId = addGroupId;
    }
    
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public String getGroupId() {
        return groupId;
    }
    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getTenantId() {
        return tenantId;
    }
    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }
}
