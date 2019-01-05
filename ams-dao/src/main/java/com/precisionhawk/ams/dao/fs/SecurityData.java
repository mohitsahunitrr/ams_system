package com.precisionhawk.ams.dao.fs;

import com.precisionhawk.ams.bean.security.Application;
import com.precisionhawk.ams.bean.security.Group;
import com.precisionhawk.ams.domain.Organization;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author pchapman
 */
public class SecurityData {
    
    private Map<String, Application> applications = new HashMap<>();
    public Map<String, Application> getApplications() {
        return applications;
    }
    public void setApplications(Map<String, Application> applications) {
        this.applications = applications;
    }

    private Map<String, Group> groups = new HashMap<>();
    public Map<String, Group> getGroups() {
        return groups;
    }
    public void setGroups(Map<String, Group> groups) {
        this.groups = groups;
    }

    private Map<String, Organization> organizations = new HashMap<>();
    public Map<String, Organization> getOrganizations() {
        return organizations;
    }
    public void setOrganizations(Map<String, Organization> organizations) {
        this.organizations = organizations;
    }

    private Map<String, SelfContainedUserInfo> users = new HashMap<>();
    public Map<String, SelfContainedUserInfo> getUsers() {
        return users;
    }
    public void setUsers(Map<String, SelfContainedUserInfo> users) {
        this.users = users;
    }
}
