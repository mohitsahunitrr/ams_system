package com.precisionhawk.ams.service.oauth.auth0;

import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author pchapman
 */
public class AppMetadata {
    private List<String> groups = new LinkedList<>();
    public List<String> getGroups() {
        return groups;
    }
    public void setGroups(List<String> groups) {
        this.groups = groups;
    }

    private List<String> organizations = new LinkedList<>();
    public List<String> getOrganizations() {
        return organizations;
    }
    public void setOrganizations(List<String> organizations) {
        this.organizations = organizations;
    }

    private List<String> sites = new LinkedList<>();
    public List<String> getSites() {
        return sites;
    }
    public void setSites(List<String> sites) {
        this.sites = sites;
    }
}
