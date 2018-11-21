/*
 * All rights reserved.
 */

package com.precisionhawk.ams.bean.security;

import com.precisionhawk.ams.support.jackson.ObjectMapperFactory;
import io.swagger.oas.annotations.media.Schema;
import java.io.IOException;

/**
 *
 * @author <a href="mailto:pchapman@pcsw.us">Philip A. Chapman</a>
 */
@Schema(description="A convenience bean which adds the name of the related asset to the AssetInspection object.")
public class UserSearchParams {

    @Schema(description="The email address of the user to search for.")
    private String emailAddress;
    public String getEmailAddress() {
        return emailAddress;
    }
    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }
    
    @Schema(description="The nickname part of an email address such as AdeleV of AdeleV@M365x214355.onmicrosoft.com.")
    private String emailNickname;
    public String getEmailNickname() {
        return emailNickname;
    }
    public void setEmailNickname(String emailNickname) {
        this.emailNickname = emailNickname;
    }
    
    @Schema(description="The ID of the Azure tenant to search.")
    private String tenantId;
    public String getTenantId() {
        return tenantId;
    }
    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    @Schema(description="The ID of the user to search for.")
    private String userId;
    public String getUserId() {
        return userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        try {
            return getClass().getSimpleName() + new ObjectMapperFactory().get().writeValueAsString(this);
        } catch (IOException ex) {
            return super.toString();
        }
    }
}
