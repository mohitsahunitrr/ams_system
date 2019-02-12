package com.precisionhawk.ams.service.simple;

import com.esotericsoftware.yamlbeans.YamlReader;
import com.precisionhawk.ams.bean.security.CachedUserInfo;
import com.precisionhawk.ams.bean.security.ExtUserCredentials;
import com.precisionhawk.ams.bean.security.Group;
import com.precisionhawk.ams.bean.security.ServicesSessionBean;
import com.precisionhawk.ams.bean.security.UserCredentials;
import com.precisionhawk.ams.bean.security.UserInfoBean;
import com.precisionhawk.ams.bean.security.UserSearchParams;
import com.precisionhawk.ams.cache.SecurityTokenCache;
import com.precisionhawk.ams.config.SecurityConfig;
import com.precisionhawk.ams.config.TenantConfig;
import com.precisionhawk.ams.dao.SecurityDao;
import com.precisionhawk.ams.dao.SimpleSecurityDao;
import com.precisionhawk.ams.dao.SiteProvider;
import com.precisionhawk.ams.dao.fs.SelfContainedUserInfo;
import com.precisionhawk.ams.domain.Organization;
import com.precisionhawk.ams.security.AccessTokenProvider;
import com.precisionhawk.ams.security.SimpleAccessTokenProvider;
import com.precisionhawk.ams.service.AbstractSecurityService;
import com.precisionhawk.ams.util.CollectionsUtilities;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author pchapman
 */
public class SimpleSecurityService extends AbstractSecurityService {

    private SimpleSecurityDao dao;
    
    @Override
    public void configure(SecurityDao securityDao, List<SiteProvider> siteProviders, SecurityTokenCache tokenCache, SecurityConfig config) {
        if (securityDao instanceof SimpleSecurityDao) {
            dao = (SimpleSecurityDao)securityDao;
        } else {
            throw new IllegalArgumentException("Security dao is not an instance of SimpleSecurityDao");
        }
        super.setSecurityConfig(config);
        super.setSiteProviders(siteProviders);
    }

    @Override
    public AccessTokenProvider accessTokenProvider(String tenantId) {
        TenantConfig tcfg = config.getTenantConfigurations().get(tenantId);
        if (tcfg == null) {
            return null;
        } else {
            SimpleAccessTokenProvider p = new SimpleAccessTokenProvider();
            p.setClientId(tcfg.getClientId());
            p.setClientSecret(tcfg.getPasswordKey());
            return p;
        }
    }

    @Override
    public UserInfoBean queryUserInfo(UserSearchParams parameters) {
        return CollectionsUtilities.firstItemIn(dao.selectUserInfo(parameters));
    }

    @Override
    public ServicesSessionBean validateToken(String accessToken) {
        ServicesSessionBean bean = new ServicesSessionBean();
        if (accessToken == null) {
            bean.setTokenValid(false);
            bean.setReason("Missing access token");
            return bean;
        }
        Reader reader;
        try {
            AuthBean auth;
            reader = new StringReader(accessToken);
            YamlReader yamlreader = new YamlReader(reader);
            auth = yamlreader.read(AuthBean.class);
            UserSearchParams params = new UserSearchParams();
            params.setEmailAddress(auth.getLogin());
            CachedUserInfo ui = CollectionsUtilities.firstItemIn(dao.selectUserInfo(params));
            if (ui != null) {
                String s = dao.getPassHash(ui.getUserId());
                if (s.equals(auth.getPasswordHash())) {
                    ExtUserCredentials creds = new ExtUserCredentials();
                    creds.populate(ui);
                    bean.setCredentials(creds);
                    bean.setTenantId(ui.getTenantId());
                    bean.setTokenValid(true);
                    if (ui instanceof SelfContainedUserInfo) {
                        SelfContainedUserInfo u = (SelfContainedUserInfo)ui;
                        Set<Group> groups = new HashSet<>();
                        for (String groupId : u.getGroups()) {
                            groups.add(dao.selectGroupById(groupId));
                        }
                        super.loadUserPermissions(dao, bean, groups);
                    }
                } else {
                    bean.setTokenValid(false);
                    bean.setReason(String.format("Invalid password %s", auth.getLogin()));
                }
            } else {
                bean.setTokenValid(false);
                bean.setReason(String.format("Invalid login %s", auth.getLogin()));
            }
        } catch (IOException ex) {
            LOGGER.error(String.format("Error parsing access token %s", accessToken), ex);
            bean.setTokenValid(false);
            bean.setReason("Error parsing access token");
        }
        return bean;
    }

    @Override
    protected Set<Organization> selectOrganizationsForUser(ServicesSessionBean bean) {
        Set<Organization> set = new HashSet<>();
        TenantConfig tcfg = config.getTenantConfigurations().get(bean.getTenantId());
        if (tcfg == null || tcfg.getOrganizationId() == null) {
            set.addAll(dao.selectOrganizationsForUser(((UserCredentials)bean.getCredentials()).getUserId()));
        } else {
            set.add(dao.selectOrganizationById(tcfg.getOrganizationId()));
        }
        return set;
    }

}
