/*
 * All rights reserved.
 */

package com.precisionhawk.ams.service;

import com.precisionhawk.ams.bean.SiteSearchParams;
import com.precisionhawk.ams.bean.security.ExtUserCredentials;
import com.precisionhawk.ams.bean.security.Group;
import com.precisionhawk.ams.bean.security.ServicesSessionBean;
import com.precisionhawk.ams.bean.security.UserCredentials;
import com.precisionhawk.ams.config.SecurityConfig;
import com.precisionhawk.ams.config.TenantConfig;
import com.precisionhawk.ams.dao.DaoException;
import com.precisionhawk.ams.dao.SecurityDao;
import com.precisionhawk.ams.dao.SiteProvider;
import com.precisionhawk.ams.domain.Organization;
import com.precisionhawk.ams.domain.Site;
import com.precisionhawk.ams.security.Constants;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author <a href="mailto:pchapman@pcsw.us">Philip A. Chapman</a>
 */
public abstract class AbstractSecurityService implements SecurityService {
    
    protected final Logger LOGGER = LoggerFactory.getLogger(getClass());
    
    protected SecurityConfig config;
    public SecurityConfig getSecurityConfig() {
        return config;
    }
    public void setSecurityConfig(SecurityConfig securityConfig) {
        this.config = securityConfig;
    }
    
    private List<SiteProvider> siteDaos;
    public List<SiteProvider> getSiteProviders() {
        return siteDaos;
    }
    public void setSiteProviders(List<SiteProvider> providers) {
        this.siteDaos = providers;
    }

    @Override
    public ServicesSessionBean addSiteToCredentials(ServicesSessionBean sess, Site site) {
        if (sess.getCredentials() instanceof UserCredentials) {
            UserCredentials creds = (UserCredentials)sess.getCredentials();
            for (Organization org : creds.getOrganizations()) {
                if (org.getId().equals(site.getOrganizationId())) {
                    creds.getSiteIDs().add(site.getId());
                    if (creds instanceof ExtUserCredentials) {
                        ExtUserCredentials excreds = (ExtUserCredentials)creds;
                        List<Site> sites = excreds.getSitesByOrganization().get(site.getOrganizationId());
                        if (sites == null) {
                            sites = new LinkedList<>();
                            excreds.getSitesByOrganization().put(site.getOrganizationId(), sites);
                        }
                        sites.add(site);
                    }
                }
            }
        }
        return sess;
    }
    
    /**
     * Populates the user credentials in the session bean with the user's assigned
     * organizations, the sites he has access to, and the user's assigned AMS roles.
     * @param dao The security DAO to get security information from.
     * @param bean The session bean to be updated.
     * @param groups The groups or roles associated with the user.
     * @throws SecurityException Indicates an error loading permissions.
     */
    protected void loadUserPermissions(SecurityDao dao, ServicesSessionBean bean, Set<Group> groups)
        throws SecurityException
    {
        TenantConfig tcfg = config.getTenantConfigurations().get(bean.getTenantId());
        ExtUserCredentials creds = (ExtUserCredentials)bean.getCredentials();
        
        // Map groups by application
        Map<String, List<String>> rolesMap = new HashMap<>();
        List<String> rolesByApp;
        for (Group g : groups) {
            rolesByApp = rolesMap.get(g.getApplicationId());
            if (rolesByApp == null) {
                rolesByApp = new LinkedList<>();
                rolesMap.put(g.getApplicationId(), rolesByApp);
            }
            rolesByApp.add(g.getKey());
        }
        
        // Organizations/Sites
        Set<Organization> orgs;
        Set<String> siteIDs;
        List<Site> sites;
        Map<String, List<Site>> sitesByOrg = new HashMap<>();
        try {
            if (tcfg.getOrganizationId() == null) {
                orgs = new HashSet(dao.selectOrganizationsForUser(creds.getUserId()));
                boolean inspecToolsUser = false;
                for (Organization o : orgs) {
                    if (Constants.COMPANY_ORG_KEY.equals(o.getKey())) {
                        inspecToolsUser = true;
                        break;
                    }
                }
                if (inspecToolsUser) {
                    // If the user is an InspecTools user, he has access to all sites.
                    List<Site> slist = new ArrayList<>();
                    for (SiteProvider p : siteDaos) {
                        slist.addAll(p.retrieveAllSites());
                    }
                    siteIDs = new HashSet(slist.size());
                    for (Site site : slist) {
                        sites = sitesByOrg.get(site.getOrganizationId());
                        if (sites == null) {
                            sites = new LinkedList<>();
                            sitesByOrg.put(site.getOrganizationId(), sites);
                        }
                        sites.add(site);
                        siteIDs.add(site.getId());
                    }
                } else {
                    sites = new ArrayList<>();
                    siteIDs = new HashSet<>();
                    // The user has access to all sites for the org to which the user belongs.
                    SiteSearchParams query = new SiteSearchParams();
                    for (Organization o : orgs) {
                        query.setOrganizationId(o.getId());
                        for (SiteProvider p : siteDaos) {
                            sites.addAll(p.retrieve(query));
                        }
                        if (sites.isEmpty()) {
                            sitesByOrg.put(o.getId(), Collections.emptyList());
                        } else {
                            sitesByOrg.put(o.getId(), sites);
                            for (Site site : sites) {
                                siteIDs.add(site.getId());
                            }
                        }
                    }
                    // The user may have access to additional sites.
                    List<String> sids = dao.selectSitesForUser(creds.getUserId());
                    List<Site> allowedSites = new ArrayList<>();
                    for (SiteProvider p : siteDaos) {
                        allowedSites.addAll(p.retrieveByIDs(sids));
                    }
                    for (Site site : allowedSites) {
                        // On occasion, sites have been deleted without permissions removed.  This causes nulls.
                        if (
                                site != null
                                && (!siteIDs.contains(site.getId()))
                            )
                        {
                            sites = sitesByOrg.get(site.getOrganizationId());
                            if (sites == null) {
                                sites = new LinkedList<>();
                                sitesByOrg.put(site.getOrganizationId(), sites);
                            }
                            sites.add(site);
                            siteIDs.add(site.getId());
                        }
                    }
                }
            } else {
                // Organization specific security.  All users belong to that org.
                // These users have permissions for all sites belonging to that org
                // but no others.
                orgs = new HashSet();
                orgs.add(dao.selectOrganizationById(tcfg.getOrganizationId()));
                // Add all the sites for the entire organization.
                SiteSearchParams query = new SiteSearchParams();
                query.setOrganizationId(tcfg.getOrganizationId());
                sites = new ArrayList<>();
                for (SiteProvider p : siteDaos) {
                    sites.addAll(p.retrieve(query));
                }
                siteIDs = new HashSet(sites.size());
                for (Site site : sites) {
                    siteIDs.add(site.getId());
                }
                sitesByOrg.put(tcfg.getOrganizationId(), sites);
            }
        } catch (DaoException ex) {
            LOGGER.error("Error loading user's available sites.", ex);
            throw new SecurityException("Error loading user's available sites.", ex);
        }
        
        // Populate Credentials
        creds.setOrganizations(new ArrayList(orgs));
        creds.setRolesByApplication(rolesMap);
        creds.setSiteIDs(new ArrayList(siteIDs));
        creds.setSitesByOrganization(sitesByOrg);
    }
}
