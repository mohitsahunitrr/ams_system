package com.precisionhawk.ams.cache;

import com.precisionhawk.ams.config.CacheConfig;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import net.sf.ehcache.CacheManager;

/**
 *
 * @author pchapman
 */
@Named
public class SecurityTokenCacheFactory implements Provider<SecurityTokenCache> {
    
    @Inject private CacheManager cacheManager;
    @Inject private CacheConfig config;

    @Override
    public SecurityTokenCache get() {
        if (EHSecurityTokenCache.class.getName().equals(config.getCacheImplementation())) {
            EHSecurityTokenCache cache = new EHSecurityTokenCache();
            cache.setCacheConfig(config);
            cache.setCacheManager(cacheManager);
            return cache;
        } else if (NoOppSecurityTokenCache.class.getName().equals(config.getCacheImplementation())) {
            NoOppSecurityTokenCache cache = new NoOppSecurityTokenCache();
            return cache;
        } else {
            throw new IllegalArgumentException(String.format("Invalid security cache class %s", config.getCacheImplementation()));
        }
    }
    
}
