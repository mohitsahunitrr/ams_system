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
import com.precisionhawk.ams.config.TenantConfig;
import com.precisionhawk.ams.dao.DaoException;
import com.precisionhawk.ams.dao.SecurityDao;
import com.precisionhawk.ams.dao.SiteProvider;
import com.precisionhawk.ams.domain.Organization;
import com.precisionhawk.ams.domain.Site;
import com.precisionhawk.ams.security.AccessTokenProvider;
import com.precisionhawk.ams.security.Constants;
import com.precisionhawk.ams.support.jackson.ObjectMapperFactory;
import com.precisionhawk.ams.util.RegexUtils;
import java.io.IOException;
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
import org.papernapkin.liana.util.StringUtil;

/**
 * The class responsible for applying security rules and setting up the session
 * and user credentials objects based on authentication against and
 * authorization based on OAuth.
 *
 * @author <a href="mailto:pchapman@pcsw.us">Philip A. Chapman</a>
 */
@Named
public abstract class OAuthSecurityService extends AbstractSecurityService {
    
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
    
    private final Map<String, GroupsProvider> groupsProviders = new HashMap();
    
    protected final ObjectMapper MAPPER;
    
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
    
    protected abstract JWKSource getKeySource() throws IOException;
    
    public OAuthSecurityService() {
        MAPPER = ObjectMapperFactory.getObjectMapper();
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
            JWSKeySelector keySelector = new JWSVerificationKeySelector(expectedJWSAlg, getKeySource());
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
        } catch (IOException ex) {
            LOGGER.debug("Error validating access token: {}", ex);
            if (bean == null) {
                bean = new ServicesSessionBean();
                bean.setWindAMSAPIAccessToken(bearerToken);
            }
            //TODO: Treat significantly different?
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
        ServicesSessionBean bean = new ServicesSessionBean();
        String audience = claimsSet.getClaim(CLAIM_AUDIENCE).toString();
        String tenantId = claimsSet.getClaim(CLAIM_TENANT_ID).toString();
        TenantConfig tcfg = config.getTenantConfigurations().get(tenantId);
        if (tcfg == null) {
            String msg = String.format("Unknown tenant ID: %s", tenantId);
            LOGGER.error(msg);
            throw new SecurityException(msg);
        }
        if (!audience.contains(tcfg.getClientId())) {
            LOGGER.error("The provided token is not intended for this application.  Declaired audience: {}", audience);
            throw new SecurityException("The provided token is not intended for this application.");
        }
        bean.setTenantId(tenantId);
        bean.setTokenValid(true);

        // Determine if we have a User on-behalf-of or a service-to-service
        if (CLAIM_VAL_USER_IMPERSONATION.equals(claimsSet.getClaim(CLAIM_SCP))) {
            // User on-behalf-of
            createExtUserCredentials(tcfg, claimsSet, bean);
        } else {
            // Treat it like a service-to-service
            createAppCredentials(claimsSet, bean);
        }
        return bean;
    }
    
    /**
     * Create a session bean based on server to server communication.
     * @param claimsSet The claims set of the connecting service.
     * @return The session bean.
     */
    private void createAppCredentials(JWTClaimsSet claimsSet, ServicesSessionBean bean) {
        String clientAppID = StringUtil.nullableToString(claimsSet.getClaim("appid"));
        if (clientAppID == null) {
            bean.setTokenValid(false);
            bean.setReason("Invalid token, not a user on-behalf of token or a valid service-to-service token.");
        } else {
            ClientConfig cconfig = config.getClientConfigurations().get(clientAppID);
            if (cconfig == null) {
                bean.setTokenValid(false);
                bean.setReason(String.format("Service-to-service token from an unknown source with app ID %s.", clientAppID));
            } else {
                AppCredentials creds = new AppCredentials();
                List<Organization> orgs = new LinkedList<>();
                creds.setApplicationId(clientAppID);
                orgs.add(dao.selectOrganizationById(cconfig.getOrganizationId()));
                List<Site> sites = new ArrayList<>();
                try {
                    if (Constants.COMPANY_ORG_KEY.equals(orgs.get(0).getKey())) {
                        for (SiteProvider p : siteDaos) {
                            sites.addAll( p.retrieveAllSites());
                        }
                    } else {
                        SiteSearchParams params = new SiteSearchParams();
                        params.setOrganizationId(cconfig.getOrganizationId());
                        for (SiteProvider p : siteDaos) {
                            sites.addAll(p.retrieve(params));
                        }
                    }
                } catch (DaoException ex) {
                    LOGGER.error("Error loading approved sites for clientId {}, org {}", clientAppID, cconfig.getOrganizationId(), ex);
                    throw new InternalServerErrorException("Error loading approved sites", ex);
                }
                for (Site s : sites) {
                    creds.getSiteIDs().add(s.getId());
                }
                creds.setOrganizations(orgs);
                bean.setCredentials(creds);
            }
        }
    }
    
    /**
     * Create a session bean based on user to server (on-behalf-of) communication.
     * @param claimsSet The claims set of the connecting user.
     * @return The session bean.
     */
    private void createExtUserCredentials(TenantConfig tcfg, JWTClaimsSet claimsSet, ServicesSessionBean bean) {
        ExtUserCredentials creds = new ExtUserCredentials();
        creds.setFirstName(StringUtil.notNull(claimsSet.getClaim(CLAIM_GIVEN_NAME)));
        creds.setLastName(StringUtil.notNull(claimsSet.getClaim(CLAIM_FAMILY_NAME)));
        creds.setUserId(claimsSet.getClaim(CLAIM_OBJECT_ID).toString());
        if (claimsSet.getClaims().containsKey(CLAIM_EMAIL)) {
            // External users are likely to have email address set in this claim
            String s = claimsSet.getClaim(CLAIM_EMAIL).toString();
            if (s.matches(RegexUtils.EMAIL_REGEX)) {
                creds.setEmailAddress(s.toLowerCase());
            }
        } else if (claimsSet.getClaim(CLAIM_UNIQUE_NAME).toString().matches(RegexUtils.EMAIL_REGEX)) {
            creds.setEmailAddress(claimsSet.getClaim(CLAIM_UNIQUE_NAME).toString().toLowerCase());
        }
        bean.setCredentials(creds);

        GroupsProvider provider = loadGroupsProvider(tcfg);
        if (provider == null) {
            throw new SecurityException(String.format("Error loading AAD Groups Provider %s", tcfg.getGroupProvider()));
        }
        Set<String> aadGroupIDs = provider.loadGroupIDs(accessTokenProvider(tcfg.getTenantId()), bean, claimsSet);
        // Do final load of permissions
        loadUserPermissions(tcfg, bean, aadGroupIDs);
        // Cache user info
        cacheUserInfo(tcfg, creds);
    }
    
    /**
     * Load the GroupsProvider associated with the given OAuth tenant.
     * @param tcfg The configuration object associated with the tenant.
     * @return The provider.
     */
    private GroupsProvider loadGroupsProvider(TenantConfig tcfg) {
        GroupsProvider provider;
        Exception ex = null;
        synchronized (groupsProviders) {
            provider = groupsProviders.get(tcfg.getGroupProvider());
            if (provider == null) {
                try {
                    Class<? extends GroupsProvider> clazz = (Class<? extends GroupsProvider>) getClass().getClassLoader().loadClass(tcfg.getGroupProvider());
                    provider = clazz.newInstance();
                    provider.setDao(dao);
                    provider.setMapper(MAPPER);
                    provider.setMaxRetries(config.getMaxRetries());
                    groupsProviders.put(tcfg.getGroupProvider(), provider);
                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                    ex = e;
                }
            }
        }
        if (ex != null) {
            LOGGER.error("Unable to load the AADGroupProvider {}", tcfg.getGroupProvider(), ex);
        }
        return provider;
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
    private void loadUserPermissions(TenantConfig tcfg, ServicesSessionBean bean, Set<String> groupIDs)
        throws SecurityException
    {
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
            CachedUserInfo info = queryForUserInfo(parameters);
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
     * Queries the authentication authority for user information.
     * @param parameters The parameters to search for.
     * @return The information of the user which matches the parameters or null if
     * no matches were found.
     */
    protected abstract CachedUserInfo queryForUserInfo(UserSearchParams parameters);

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
            // updated timestamp so that it can be retrieved from Graph later.
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
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
