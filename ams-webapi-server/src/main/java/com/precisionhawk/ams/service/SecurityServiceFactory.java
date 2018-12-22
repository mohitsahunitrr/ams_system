package com.precisionhawk.ams.service;

import com.precisionhawk.ams.cache.SecurityTokenCache;
import com.precisionhawk.ams.config.SecurityConfig;
import com.precisionhawk.ams.dao.SecurityDao;
import com.precisionhawk.ams.dao.SiteProvider;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

/**
 *
 * @author pchapman
 */
@Named
public class SecurityServiceFactory implements Provider<SecurityService> {
    
    @Inject private SecurityConfig config;
    
    @Inject private SecurityDao securityDao;
    
    @Inject private List<SiteProvider> siteProviders;
    
    @Inject private SecurityTokenCache tokenCache;

    @Override
    public SecurityService get() {
        try {
            Class<SecurityService> implClazz = (Class<SecurityService>) getClass().getClassLoader().loadClass(config.getSecurityImplementation());
            SecurityService svc = implClazz.newInstance();
            svc.configure(securityDao, siteProviders, tokenCache, config);
            return svc;
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException ex) {
            throw new IllegalArgumentException(String.format("Invalid Security service implementation %s: %s", config.getSecurityImplementation(), ex.getLocalizedMessage()));
        }
    }
    
}
