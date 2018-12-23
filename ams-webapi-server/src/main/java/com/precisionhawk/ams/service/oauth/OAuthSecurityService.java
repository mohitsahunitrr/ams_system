/*
 * All rights reserved.
 */

package com.precisionhawk.ams.service.oauth;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.BadJOSEException;
import com.nimbusds.jose.proc.JWSKeySelector;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import com.precisionhawk.ams.bean.SiteSearchParams;
import com.precisionhawk.ams.cache.SecurityTokenCache;
import com.precisionhawk.ams.bean.security.AppCredentials;
import com.precisionhawk.ams.bean.security.CachedUserInfo;
import com.precisionhawk.ams.bean.security.ExtUserCredentials;
import com.precisionhawk.ams.bean.security.Group;
import com.precisionhawk.ams.bean.security.ServicesSessionBean;
import com.precisionhawk.ams.bean.security.UserInfoBean;
import com.precisionhawk.ams.bean.security.UserSearchParams;
import com.precisionhawk.ams.service.AbstractSecurityService;
import com.precisionhawk.ams.config.ClientConfig;
import com.precisionhawk.ams.config.SecurityConfig;
import com.precisionhawk.ams.config.TenantConfig;
import com.precisionhawk.ams.dao.DaoException;
import com.precisionhawk.ams.dao.SecurityDao;
import com.precisionhawk.ams.dao.SiteProvider;
import com.precisionhawk.ams.domain.Organization;
import com.precisionhawk.ams.domain.Site;
import com.precisionhawk.ams.security.AccessTokenProvider;
import com.precisionhawk.ams.security.Constants;
import com.precisionhawk.ams.support.jackson.ObjectMapperFactory;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.InternalServerErrorException;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * The class responsible for applying security rules and setting up the session
 * and user credentials objects based on authentication against and
 * authorization based on OAuth.
 *
 * @author <a href="mailto:pchapman@pcsw.us">Philip A. Chapman</a>
 */
@Named
public final class OAuthSecurityService extends AbstractSecurityService {
    
    private static final String CLAIM_AUDIENCE = "aud";
    private static final String CLAIM_EMAIL = "email";
    private static final String CLAIM_FAMILY_NAME = "family_name";
    private static final String CLAIM_GIVEN_NAME = "given_name";
    private static final String CLAIM_OBJECT_ID = "oid";
    private static final String CLAIM_SCP = "scp";
    private static final String CLAIM_TENANT_ID = "tid";
    private static final String CLAIM_UNIQUE_NAME = "unique_name";
    private static final String CLAIM_VAL_USER_IMPERSONATION = "user_impersonation";
    
    @Inject
    protected SecurityDao dao;
    
    private final Map<String, OAuthAuthenticationProvider> authProviders = new HashMap();
    private final Map<String, GroupsProvider> groupsProviders = new HashMap();
   
    private final ObjectMapper MAPPER = ObjectMapperFactory.getObjectMapper();
    
    private JWKSource jwkSource;
    
    private List<SiteProvider> siteDaos;
    public List<SiteProvider> getSiteProviders() {
        return siteDaos;
    }
    public void setSiteProviders(List<SiteProvider> providers) {
        this.siteDaos = providers;
    }
    
    private SecurityTokenCache tokenCache;
    public SecurityTokenCache getTokenCache() {
        return tokenCache;
    }
    public void setTokenCache(SecurityTokenCache cache) {
        this.tokenCache = cache;
    }
    
    @Override
    public void configure(SecurityDao securityDao, List<SiteProvider> siteDaos, SecurityTokenCache tokenCache, SecurityConfig config) {
        setSecurityConfig(config);
        setSiteProviders(siteDaos);
        setTokenCache(tokenCache);
        
        OAuthAuthenticationProvider a;
        Class<OAuthAuthenticationProvider> authClazz;
        GroupsProvider g;
        Class<? extends GroupsProvider> groupClazz;
        DelegatingJWKSource djwks = new DelegatingJWKSource();
        
        for (TenantConfig tcfg : getSecurityConfig().getTenantConfigurations().values()) {
            try {
                authClazz = (Class<OAuthAuthenticationProvider>)getClass().getClassLoader().loadClass(tcfg.getAuthProvider());
                a = authClazz.newInstance();
                a.configure(tcfg);
                djwks.add(a.getKeySource());
                authProviders.put(tcfg.getTenantId(), a);
            } catch (ClassNotFoundException | IllegalAccessException | InstantiationException ex) {
                throw new IllegalArgumentException(String.format("Invalid authentication provider %s for tenant %s: %s", tcfg.getAuthProvider(), tcfg.getTenantId(), ex.getLocalizedMessage()));
            } catch (MalformedURLException ex) {
                throw new RuntimeException(String.format("Unable to configure the authentication provider for tenant %s: %s", tcfg.getTenantId(), ex.getLocalizedMessage()));
            }
            
            try {
                groupClazz = (Class<? extends GroupsProvider>) getClass().getClassLoader().loadClass(tcfg.getGroupProvider());
                g = groupClazz.newInstance();
                g.setDao(dao);
                g.setMapper(MAPPER);
                g.setMaxRetries(tcfg.getMaxRetries());
                g.configure(tcfg);
                groupsProviders.put(tcfg.getApiId(), g);
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
                throw new IllegalArgumentException(String.format("Invalid groups provider %s for tenant %s: %s", tcfg.getGroupProvider(), tcfg.getTenantId(), ex.getLocalizedMessage()));
            }
        }
        
        this.jwkSource = djwks;
    }

    @Override
    public ServicesSessionBean validateToken(String bearerToken) {
        //FIXME: Not case sensitive
        if (bearerToken != null) {
            bearerToken = bearerToken.replace("Bearer ", "");
        }
        if (bearerToken == null || bearerToken.isEmpty()) {
            return null;
        }
        ServicesSessionBean bean = tokenCache.retrieve(bearerToken);
        try {
            if (bean != null && !bean.isTokenValid()) {
                return bean;
            }
            // Even if we have the token cached, we need to re-evaluate to see
            // if it has expired.
            
            // Set up a JWT processor to parse the tokens and then check their signature
            // and validity time window (bounded by the "iat", "nbf" and "exp" claims)
            ConfigurableJWTProcessor jwtProcessor = new DefaultJWTProcessor();

            // The expected JWS algorithm of the access tokens (agreed out-of-band)
            JWSAlgorithm expectedJWSAlg = JWSAlgorithm.RS256;

            // Configure the JWT processor with a key selector to feed matching public
            // RSA keys sourced from the JWK set URL
            JWSKeySelector keySelector = new JWSVerificationKeySelector(expectedJWSAlg, jwkSource);
            jwtProcessor.setJWSKeySelector(keySelector);

            // Process the token
            SecurityContext ctx = null; // optional context parameter, not required here
            JWTClaimsSet claimsSet;
            claimsSet = jwtProcessor.process(bearerToken, ctx);
            
            // The token is valid.  See if we have already processed the token and it is cached.
            if (bean == null) {
                LOGGER.debug("Access Token Valid, {}", claimsSet);
                bean = createServicesSessionBean(claimsSet);
                bean.setWindAMSAPIAccessToken(bearerToken);
                tokenCache.store(bean);
            }
            return bean;
        } catch (BadJOSEException | JOSEException | ParseException | SecurityException ex) {
            LOGGER.debug("Access token fails: {}", ex);
            if (bean == null) {
                bean = new ServicesSessionBean();
                bean.setWindAMSAPIAccessToken(bearerToken);
            }
            bean.setTokenValid(false);
            bean.setReason(ex.getLocalizedMessage());
            tokenCache.store(bean);
            return bean;
        }
    }
    
    /**
     * Create a session bean based on the provided Claims Set.
     * @param claimsSet The claims set contained within the supplied access token.
     * @return The session bean.
     */
    private ServicesSessionBean createServicesSessionBean(JWTClaimsSet claimsSet) {
        ServicesSessionBean bean = null;
        for (OAuthAuthenticationProvider p : authProviders.values()) {
            bean = p.createServicesSessionBean(claimsSet);
            if (bean != null) {
                if (bean.getCredentials() instanceof ExtUserCredentials) {
                    processExtUserCredentials(claimsSet, bean);
                } else if (bean.getCredentials() instanceof AppCredentials) {
                    processAppCredentials(claimsSet, bean);
                } else {
                    String s = String.format("Missing or unknown credentials object %s", bean.getCredentials() == null ? "null" : bean.getCredentials().getClass().getName());
                    LOGGER.error(s);
                    bean.setTokenValid(false);
                    bean.setReason(s);
                    return bean;
                }
            }
        }
        return bean;
    }
    
    private void processAppCredentials(JWTClaimsSet claimsSet, ServicesSessionBean bean) {
        AppCredentials creds =(AppCredentials)bean.getCredentials();
        ClientConfig cconfig = config.getClientConfigurations().get(creds.getApplicationId());
        if (cconfig == null) {
            bean.setTokenValid(false);
            bean.setReason(String.format("Unreconized client application %s", creds.getApplicationId()));
        } else {
            List<Organization> orgs = new LinkedList<>();
            orgs.add(dao.selectOrganizationById(cconfig.getOrganizationId()));
            creds.setOrganizations(orgs);
            List<Site> sites = new ArrayList<>();
            try {
                if (Constants.COMPANY_ORG_KEY.equals(orgs.get(0).getKey())) {
                    for (SiteProvider p : siteDaos) {
                        sites.addAll( p.retrieveAllSites());
                    }
                } else {
                    SiteSearchParams params = new SiteSearchParams();
                    for (Organization org : orgs) {
                        params.setOrganizationId(org.getId());
                        for (SiteProvider p : siteDaos) {
                            sites.addAll(p.retrieve(params));
                        }
                    }
                }
            } catch (DaoException ex) {
                LOGGER.error("Error loading approved sites for clientId {}, orgs {}", creds.getApplicationId(), orgs.toString(), ex);
                throw new InternalServerErrorException("Error loading approved sites", ex);
            }
            for (Site s : sites) {
                creds.getSiteIDs().add(s.getId());
            }
        }
    }
    
    private void processExtUserCredentials(JWTClaimsSet claimsSet, ServicesSessionBean bean) {
        GroupsProvider provider = groupsProviders.get(bean.getTenantId());
        Set<String> groupIDs = provider.loadGroupIDs(accessTokenProvider(bean.getTenantId()), bean, claimsSet);
        // Do final load of permissions
        loadUserPermissions(bean, groupIDs);
        // Cache user info
        cacheUserInfo(config.getTenantConfigurations().get(bean.getTenantId()), (ExtUserCredentials)bean.getCredentials());
    }
    
    /**
     * Populates the user credentials in the session bean with the user's assigned
     * organizations, the sites he has access to, and the user's assigned AMS roles.
     * @param tcfg The configuration object for the OAuth tenant against which the user
     * has been authenticated.
     * @param bean The session bean to be updated.
     * @param groupIDs The groups or roles associated with the user.
     * @throws SecurityException Indicates an error loading permissions.
     */
    private void loadUserPermissions(ServicesSessionBean bean, Set<String> groupIDs)
        throws SecurityException
    {
        TenantConfig tcfg = config.getTenantConfigurations().get(bean.getTenantId());
        ExtUserCredentials creds = (ExtUserCredentials)bean.getCredentials();
        LOGGER.debug("Authenticating against tenant {}", tcfg.getTenantName());
        
        // Match provider group IDs to AMS Groups
        Set<Group> groups = new HashSet();
        for (String groupId : groupIDs) {
            List<Group> glist = dao.selectGroupsByTenantGroup(bean.getTenantId(), groupId);
            if (glist != null && !glist.isEmpty()) {
                groups.addAll(glist);
            }
        }

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
        List<Organization> orgs;
        List<String> siteIDs;
        List<Site> sites;
        Map<String, List<Site>> sitesByOrg = new HashMap<>();
        try {
            if (tcfg.getOrganizationId() == null) {
                orgs = dao.selectOrganizationsForUser(creds.getUserId());
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
                    siteIDs = new ArrayList<>(slist.size());
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
                    siteIDs = new ArrayList<>();
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
                        }
                    }
                }
            } else {
                // Organization specific security.  All users belong to that org.
                // These users have permissions for all sites belonging to that org
                // but no others.
                orgs = new LinkedList<>();
                orgs.add(dao.selectOrganizationById(tcfg.getOrganizationId()));
                // Add all the sites for the entire organization.
                SiteSearchParams query = new SiteSearchParams();
                query.setOrganizationId(tcfg.getOrganizationId());
                sites = new ArrayList<>();
                for (SiteProvider p : siteDaos) {
                    sites.addAll(p.retrieve(query));
                }
                siteIDs = new ArrayList<>(sites.size());
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
        creds.setOrganizations(orgs);
        creds.setRolesByApplication(rolesMap);
        creds.setSiteIDs(siteIDs);
        creds.setSitesByOrganization(sitesByOrg);
    }

    /**
     * Obtains user info based on the provided parameters.  User information is cached.
     * In case of a cache miss or outdated info in the cache, the information is queried
     * from the OAuth authentication authority.
     * @param parameters The parameters to use for searching for the user info.
     * @return The user info if the user is found, else null.
     */
    @Override
    public UserInfoBean queryUserInfo(UserSearchParams parameters) {
        if (parameters.getEmailAddress() != null) {
            parameters.setEmailAddress(parameters.getEmailAddress().toLowerCase());
        }
        CachedUserInfo userInfo = null;
        // See if we already have info.
        List<CachedUserInfo> results = dao.selectUserInfo(parameters);
        // We should only have 1
        if (results.size() == 1) {
            userInfo = results.get(0);
        } else if (results.size() > 1) {
            // This REALLY Shouldn't happen.  Delete them all and come again.
            dao.deleteUserInfo(results.get(0).getUserId());
        }
        // If exists and up to date return it.
        if (userInfo != null && userInfo.getLastUpdated() != null && (System.currentTimeMillis() - userInfo.getLastUpdated()) < config.getUserInfoCacheTime()) {
            return userInfo;
        }
        Set<String> tenantsToTry = new HashSet<>();
        if (userInfo != null) {
            // We already know what tenant to query
            tenantsToTry.add(userInfo.getTenantId());
        } else {
            if (parameters.getTenantId() != null) {
                tenantsToTry.add(parameters.getTenantId());
            } else {
                // Try them all.
                for (TenantConfig cfg : config.getTenantConfigurations().values()) {
                    tenantsToTry.add(cfg.getTenantId());
                }
            }
        }
        for (String tenantId : tenantsToTry) {
            parameters.setTenantId(tenantId);
            CachedUserInfo info = authProviders.get(tenantId).queryForUserInfo(parameters);
            if (info != null) {
                if (userInfo == null && parameters.getUserId() == null) {
                    // It may be possible that the user existed, but didn't have
                    // the secondary info.  Attempt to look the user up by id.
                    parameters = new UserSearchParams();
                    parameters.setTenantId(tenantId);
                    parameters.setUserId(info.getUserId());
                    results = dao.selectUserInfo(parameters);
                    if (!results.isEmpty()) {
                        userInfo = results.get(0);
                    }
                }
                // Cache the info for later re-use
                info.setLastUpdated(System.currentTimeMillis());
                if (userInfo == null) {
                    dao.insertUserInfo(info);
                } else {
                    dao.updateUserInfo(info);
                }
                // Return what we found short-circuiting our search.
                return info;
            }
        }
        return null;
    }

    /**
     * Caches user info for later use.
     * @param tenantId Unique ID of the tenant in which the user exists.
     * @param creds The credentials object holding the user's info.
     */
    private void cacheUserInfo(TenantConfig tcfg, ExtUserCredentials creds) {
        UserSearchParams params = new UserSearchParams();
        params.setTenantId(tcfg.getTenantId());
        params.setUserId(creds.getUserId());
        List<CachedUserInfo> results = dao.selectUserInfo(params);
        if (results.isEmpty()) {
            CachedUserInfo info = CachedUserInfo.fromUserInfo(creds, null);
            info.setTenantId(tcfg.getTenantId());
            // If we don't have all the user's complete info do not set last
            // updated timestamp so that it can be retrieved from the provider later.
            if (info.testHasMininumInfo()) {
                info.setLastUpdated(System.currentTimeMillis());
            } 
            dao.insertUserInfo(info);
        }
    }
    
    @Override
    public ServicesSessionBean addSiteToCredentials(ServicesSessionBean sess, Site site) {
        sess = super.addSiteToCredentials(sess, site);
        tokenCache.store(sess);
        return sess;
    }

    @Override
    public AccessTokenProvider accessTokenProvider(String tenantId) {
        OAuthAuthenticationProvider p = authProviders.get(tenantId);
        if (p == null) {
            return null;
        } else {
            return p.getAccessTokenProvider();
        }
    }
}
