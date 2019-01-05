package com.precisionhawk.ams.dao.fs;

import com.precisionhawk.ams.bean.security.CachedUserInfo;
import java.util.List;
import org.codehaus.jackson.annotate.JsonIgnore;

/**
 *
 * @author pchapman
 */
public class SelfContainedUserInfo extends CachedUserInfo {
    
    @JsonIgnore
    private List<String> groups;
    public List<String> getGroups() {
        return groups;
    }
    public void setGroups(List<String> groups) {
        this.groups = groups;
    }
    
    @JsonIgnore
    private String passHash;
    public String getPasswordHash() {
        return passHash;
    }
    public void setPasswordHash(String passHash) {
        this.passHash = passHash;
    }
    
    @JsonIgnore
    private List<String> organizations;
    public List<String> getOrganizations() {
        return organizations;
    }
    public void setOrganizations(List<String> organizations) {
        this.organizations = organizations;
    }

    @JsonIgnore
    private List<String> sites;
    public List<String> getSites() {
        return sites;
    }
    public void setSites(List<String> sites) {
        this.sites = sites;
    }
}
