/*
 * All rights reserved.
 */

package com.precisionhawk.ams.bean.security;

import org.codehaus.jackson.annotate.JsonIgnore;

/**
 *
 * @author <a href="mailto:pchapman@pcsw.us">Philip A. Chapman</a>
 */
public class CachedUserInfo extends UserInfoBean {
    
    public CachedUserInfo() {}
    
    public static CachedUserInfo fromUserInfo(UserInfo original, Long lastUpdated) {
        if (original instanceof CachedUserInfo) {
            return (CachedUserInfo)original;
        }
        CachedUserInfo info = new CachedUserInfo();
        info.lastUpdated = lastUpdated;
        info.setEmailAddress(original.getEmailAddress());
        info.setFirstName(original.getFirstName());
        info.setLastName(original.getLastName());
        info.setMiddleName(original.getMiddleName());
        if (original instanceof UserInfoBean) {
            info.setTenantId(((UserInfoBean)original).getTenantId());
            info.setEmailNickname(((UserInfoBean)original).getEmailNickname());
        }
        info.setUserId(original.getUserId());
        return info;
    }
    
    @JsonIgnore
    private Long lastUpdated;
    public Long getLastUpdated() {
        return lastUpdated;
    }
    public void setLastUpdated(Long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
    
    // Valid if we have the minimum necessary info
    public boolean testHasMininumInfo() {
        return
            getEmailAddress() != null && (!getEmailAddress().isEmpty())
            &&
            getEmailNickname()!= null && (!getEmailNickname().isEmpty())
            &&
            getFirstName() != null && (!getFirstName().isEmpty())
            &&
            getLastName() != null && (!getLastName().isEmpty())
            &&
            getTenantId() != null && (!getTenantId().isEmpty())
            &&
            getUserId() != null && (!getUserId().isEmpty());
    }
}
