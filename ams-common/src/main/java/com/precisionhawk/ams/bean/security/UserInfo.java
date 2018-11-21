/*
 * All rights reserved.
 */
package com.precisionhawk.ams.bean.security;

/**
 *
 * @author <a href="mailto:pchapman@pcsw.us">Philip A. Chapman</a>
 */
public interface UserInfo {

    String getUserId();

    String getFirstName();
    
    String getMiddleName();

    String getLastName();

    String getEmailAddress();
}
