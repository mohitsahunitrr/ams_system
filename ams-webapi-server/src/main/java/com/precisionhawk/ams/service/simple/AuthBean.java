package com.precisionhawk.ams.service.simple;

/**
 *
 * @author pchapman
 */
public class AuthBean {
    private String login;
    public String getLogin() {
        return login;
    }
    public void setLogin(String login) {
        this.login = login;
    }

    private String passwordHash;
    public String getPasswordHash() {
        return passwordHash;
    }
    public void setPasswordHash(String passworkHash) {
        this.passwordHash = passworkHash;
    }
}
