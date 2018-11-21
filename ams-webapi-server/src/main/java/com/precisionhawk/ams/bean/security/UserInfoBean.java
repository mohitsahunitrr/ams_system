/*
 * All rights reserved.
 */

package com.precisionhawk.ams.bean.security;

import io.swagger.oas.annotations.media.Schema;
import org.codehaus.jackson.annotate.JsonIgnore;

/**
 *
 * @author <a href="mailto:pchapman@pcsw.us">Philip A. Chapman</a>
 */
public class UserInfoBean implements UserInfo {

    @Schema(description="Unique ID of the user.")
    private String userId;
    @Override
    public String getUserId() {
        return userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    @Schema(description="Unique ID of the tenant in which the user exists.")
    private String tenantId;
    public String getTenantId() {
        return tenantId;
    }
    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }
    
    @Schema(description="User's first name.")
    private String firstName;
    @Override
    public String getFirstName() {
        return firstName;
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    @Schema(description="User's middle name.")
    private String middleName;
    @Override
    public String getMiddleName() {
        return middleName;
    }
    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    @Schema(description="User's last name.")
    private String lastName;
    @Override
    public String getLastName() {
        return lastName;
    }
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    @Schema(description="Email address of the user.")
    private String emailAddress;
    @Override
    public String getEmailAddress() {
        return emailAddress;
    }
    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }
    
    @JsonIgnore
    private String emailNickname;
    public String getEmailNickname() {
        return emailNickname;
    }
    public void setEmailNickname(String mailNickname) {
        this.emailNickname = mailNickname;
    }
}
