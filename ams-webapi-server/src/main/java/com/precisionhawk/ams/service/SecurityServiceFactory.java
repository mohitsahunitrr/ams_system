package com.precisionhawk.ams.service;

import com.precisionhawk.ams.cache.SecurityTokenCache;
import com.precisionhawk.ams.config.SecurityConfig;
import com.precisionhawk.ams.dao.SecurityDao;
import com.precisionhawk.ams.dao.SiteProvider;
import com.precisionhawk.ams.service.aad.AADSecurityService;
import java.net.MalformedURLException;
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
        if (AADSecurityService.class.getName().equals(config.getSecurityImplementation())) {
            try {
                AADSecurityService svc = new AADSecurityService();
                svc.setSecurityConfig(config);
                svc.setSiteProviders(siteProviders);
                svc.setTokenCache(tokenCache);
                return svc;
            } catch (MalformedURLException ex) {
                throw new RuntimeException(ex);
            }
        } else if (NoOppSecurityService.class.getName().equals(config.getSecurityImplementation())) {
            NoOppSecurityService svc = new NoOppSecurityService();
            svc.setSecurityConfig(config);
            svc.setSecurityDao(securityDao);
            svc.setSiteProviders(siteProviders);
            return svc;
        } else {
            throw new IllegalArgumentException(String.format("Invalid security service class %s", config.getSecurityImplementation()));
        }
    }
    
}
