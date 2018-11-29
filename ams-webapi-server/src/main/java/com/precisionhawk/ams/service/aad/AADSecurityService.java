/*
 * All rights reserved.
 */

package com.precisionhawk.ams.service.aad;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpResponseException;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
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
import com.precisionhawk.ams.support.http.HttpTransportClient;
import static com.precisionhawk.ams.support.http.HttpTransportClient.MIME_JSON;
import static com.precisionhawk.ams.support.http.HttpTransportClient.REQ_FACTORY;
import com.precisionhawk.ams.support.jackson.ObjectMapperFactory;
import com.precisionhawk.ams.util.RegexUtils;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
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
import org.apache.commons.io.IOUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.papernapkin.liana.util.StringUtil;

/**
 * The class responsible for applying security rules and setting up the session
 * and user credentials objects based on authentication against and
 * authorization based on Azure Active Directory.
 *
 * @author <a href="mailto:pchapman@pcsw.us">Philip A. Chapman</a>
 */
@Named
public class AADSecurityService extends AbstractSecurityService {
    
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
    
    private final Map<String, AADGroupsProvider> groupsProviders = new HashMap();
    
    protected final JWKSource KEY_SOURCE;
    
    protected final ObjectMapper MAPPER;
    
//    @Value("${security.max.graph.retries}")
//    private int maxRetries = 3;
//    public int getMaxRetries() {
//        return maxRetries;
//    }
//    public void setMaxRetries(int maxRetries) {
//        this.maxRetries = maxRetries;
//    }
    
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
    
//    @Value("${security.user.cachetime}")
//    private long userInfoCacheTime;
    
    public AADSecurityService() throws MalformedURLException {
        KEY_SOURCE = new RemoteJWKSet(new URL("https://login.windows.net/common/discovery/keys"));
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
            JWSKeySelector keySelector = new JWSVerificationKeySelector(expectedJWSAlg, KEY_SOURCE);
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

        AADGroupsProvider provider = loadAADGroupsProvider(tcfg);
        if (provider == null) {
            throw new SecurityException(String.format("Error loading AAD Groups Provider %s", tcfg.getGroupProvider()));
        }
        Set<String> aadGroupIDs = provider.loadAADGroupIDs(accessTokenProvider(tcfg.getTenantId()), bean, claimsSet);
        // Do final load of permissions
        loadUserPermissions(tcfg, bean, aadGroupIDs);
        // Cache user info
        cacheUserInfo(tcfg, creds);
    }
    
    /**
     * Load the AADGroupsProvider associated with the given Azure tenant.
     * @param tcfg The configuration object associated with the tenant.
     * @return The provider.
     */
    private AADGroupsProvider loadAADGroupsProvider(TenantConfig tcfg) {
        AADGroupsProvider provider;
        Exception ex = null;
        synchronized (groupsProviders) {
            provider = groupsProviders.get(tcfg.getGroupProvider());
            if (provider == null) {
                try {
                    Class<? extends AADGroupsProvider> clazz = (Class<? extends AADGroupsProvider>) getClass().getClassLoader().loadClass(tcfg.getGroupProvider());
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
     * organizations, the sites he has access to, and the user's assigned WindAMS roles.
     * @param tcfg The configuration object for the Azure tenant against which the user
     * has been authenticated.
     * @param bean The session bean to be updated.
     * @param aadGroupIDs The AAD groups or roles associated with the user.
     * @throws SecurityException Indicates an error loading permissions.
     */
    private void loadUserPermissions(TenantConfig tcfg, ServicesSessionBean bean, Set<String> aadGroupIDs)
        throws SecurityException
    {
        ExtUserCredentials creds = (ExtUserCredentials)bean.getCredentials();
        LOGGER.debug("Authenticating against tenant {}", tcfg.getTenantName());
        
        // Match Azure Active Directory groups to WindAMS Groups
        Set<Group> groups = new HashSet();
        for (String aadGroupId : aadGroupIDs) {
            List<Group> glist = dao.selectGroupsByTenantGroup(bean.getTenantId(), aadGroupId);
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
     * from Azure Active Directory through Graph.
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
            CachedUserInfo info = queryADDForUserInfo(parameters);
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
     * Queries user information from Azure active Directory through calls to Graph.
     * @param parameters The parameters to search for.
     * @return The information of the user which matches the parameters or null if
     * no matches were found.
     */
    private CachedUserInfo queryADDForUserInfo(UserSearchParams parameters) {
        if (parameters.getTenantId() == null) {
            throw new IllegalArgumentException("Tenant ID required");
        }
        if (parameters.getUserId() != null) {
            return queryUserInfo(parameters.getTenantId(), parameters.getUserId());
        } else if (parameters.getEmailAddress() != null || parameters.getEmailNickname() != null) {
            String emailAddress = parameters.getEmailAddress();
            String nick = parameters.getEmailNickname();
            List<CachedUserInfo> list = queryUsersInfo(parameters.getTenantId());
            for (CachedUserInfo info : list) {
                if (
                        StringUtil.equalsIgnoreCase(emailAddress, info.getEmailAddress())
                        ||
                        StringUtil.equalsIgnoreCase(nick, info.getEmailNickname())
                   )
                {
                    return info;
                }
            }
            return null;
        } else {
            throw new IllegalArgumentException("Either user ID or email address required for query.");
        }
    }
    
    static final String GRAPH_RESOURCE = "https://graph.windows.net/";
    
    /**
     * Queries user information from Azure active Directory through calls to Graph.
     * @param tenantId The unique ID of the tenant to be queried.
     * @param userId The unique ID of the user in AAD.
     * @return The information of the user associated with the ID or null if
     * no matches were found.
     */
    private CachedUserInfo queryUserInfo(String tenantId, String userId) {
        String accessToken = null;
        try {
            accessToken = accessTokenProvider(tenantId).obtainAccessToken(GRAPH_RESOURCE);
        } catch (IOException ex) {
            LOGGER.error("Error gaining access token for Microsoft Graph", ex);
        }
        if (accessToken == null) {
            throw new InternalServerErrorException("Unable to obtain access token for Microsoft Graph APIs.");
        }
        URL url;
        try {
            String s = String.format("https://graph.windows.net/%s/users/%s?api-version=1.6", URLEncoder.encode(tenantId, "UTF-8"), URLEncoder.encode(userId, "UTF-8"));
            url = new URL(s);
        } catch (MalformedURLException | UnsupportedEncodingException ex) {
            // Shouldn't happen
            return null;
        }
        // Load Azure Active Directory groups
        HttpURLConnection conn = null;
        BufferedReader reader = null;
        int tries = 0;
        while (tries < config.getMaxRetries()) {
            tries++;
            try {
                HttpRequest req = REQ_FACTORY.buildGetRequest(new GenericUrl(url));
                req.getHeaders().setAccept(MIME_JSON);
                req.getHeaders().setAuthorization("Bearer " + accessToken);
                HttpResponse resp = req.execute();
                String respText = HttpTransportClient.loadContent(resp);
                LOGGER.debug("User Results: {}", respText);
                Map<String, Object> data = MAPPER.readValue(respText, new TypeReference<Map<String, Object>>(){});
                return userInfoFromData(tenantId, data);
            } catch (IOException ex) {
                if (ex instanceof HttpResponseException) {
                    HttpResponseException re = (HttpResponseException)ex;
                    if (re.getStatusCode() == 404) {
                        return null;
                    } else {
                        LOGGER.error("Error loading User Info from tenant {} user{}: attempt: {} status: {} : message: {}", tenantId, userId, tries, re.getStatusCode(), re.getStatusMessage(), ex);
                    }
                } else {
                    LOGGER.error("Error loading user info: attempt: {}", tries, ex);
                }
                if (tries >= config.getMaxRetries()) {
                    throw new SecurityException(String.format("Error loading user after %d attempts", tries), ex);
                } else {
                    // Wait a few moments before trying again
                    try { Thread.sleep(3000 * tries); } catch (InterruptedException ie) {}
                }
            }
        }
        return null;
    }
    
    /**
     * Returns information for all users within a Azure Tenant's active directory.
     * @param tenantId The unique ID of the tenant in Azure.
     * @return The list of tenant info.
     */
    private List<CachedUserInfo> queryUsersInfo(String tenantId) {
        String accessToken = null;
        try {
            accessToken = accessTokenProvider(tenantId).obtainAccessToken("https://graph.windows.net/");
        } catch (IOException ex) {
            LOGGER.error("Error gaining access token for Microsoft Graph", ex);
        }
        if (accessToken == null) {
            throw new InternalServerErrorException("Unable to obtain access token for Microsoft Graph APIs.");
        }
        URL url;
        try {
            url = new URL("https://graph.windows.net/myorganization/users/?api-version=1.6");
        } catch (MalformedURLException ex) {
            // Shouldn't happen
            return Collections.emptyList();
        }
        // Load Azure Active Directory groups
        HttpURLConnection conn = null;
        BufferedReader reader = null;
        int tries = 0;
        while (tries < config.getMaxRetries()) {
            try {
                HttpRequest req = REQ_FACTORY.buildGetRequest(new GenericUrl(url));
                req.getHeaders().setAccept(MIME_JSON);
                req.getHeaders().setAuthorization("Bearer " + accessToken);
                HttpResponse resp = req.execute();
                String respText = HttpTransportClient.loadContent(resp);
                LOGGER.debug("User Results: {}", respText);
                Map<String, Object> data = MAPPER.readValue(respText, new TypeReference<Map<String, Object>>(){});
                List<Map<String, Object>> userDataList = (List<Map<String, Object>>)data.get("value");
                List<CachedUserInfo> list = new LinkedList<>();
                for (Map<String, Object> userData : userDataList) {
                    list.add(userInfoFromData(tenantId, userData));
                }
                return list;
            } catch (IOException ex) {
                if (ex instanceof HttpResponseException) {
                    HttpResponseException re = (HttpResponseException)ex;
                    if (re.getStatusCode() == 404) {
                        return null;
                    } else {
                        LOGGER.error("Error loading Users Info from tenant {}: attempt: {} status: {} : message: {}", tenantId, tries, re.getStatusCode(), re.getStatusMessage(), ex);
                    }
                } else {
                    LOGGER.error("Error loading users info: attempt: {}", tries, ex);
                }
                if (tries >= config.getMaxRetries()) {
                    throw new SecurityException(String.format("Error loading users after %d attempts", tries), ex);
                } else {
                    // Wait a few moments before trying again
                    try { Thread.sleep(3000 * tries); } catch (InterruptedException ie) {}
                }
            } finally {
                IOUtils.closeQuietly(reader);
                if (conn != null) {
                    conn.disconnect();
                }
            }
        }
        return null;
    }
    
    /**
     * Obtains the user info from the Map returned from parsing the Graph JSON response.
     * @param tenantId Unique ID of the tenant in which the user exists.
     * @param data The Map of data parsed from Graph's JSON response.
     * @return The user information.
     */
    private CachedUserInfo userInfoFromData(String tenantId, Map<String, Object> data) {
        CachedUserInfo info = new CachedUserInfo();
        info.setTenantId(tenantId);
        info.setFirstName(StringUtil.notNull(data.get("givenName")));
        String emailAddress = StringUtil.notNull(data.get("mail"));
        if (emailAddress.length() == 0) {
            emailAddress = StringUtil.notNull(data.get("userPrincipalName"));
        }
        emailAddress = emailAddress.toLowerCase();            
        info.setEmailAddress(emailAddress);
        info.setLastName(StringUtil.notNull(data.get("surname")));
        info.setUserId(StringUtil.notNull(data.get("objectId")));
        info.setEmailNickname(StringUtil.notNull(data.get("mailNickname")));
        return info;
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
