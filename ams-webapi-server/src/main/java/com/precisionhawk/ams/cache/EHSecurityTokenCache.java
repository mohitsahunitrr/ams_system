/*
 * All rights reserved.
 */

package com.precisionhawk.ams.cache;

import com.precisionhawk.ams.config.CacheConfig;
import com.precisionhawk.ams.bean.security.ServicesSessionBean;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

/**
 *
 * @author <a href="mailto:pchapman@pcsw.us">Philip A. Chapman</a>
 */
public class EHSecurityTokenCache implements SecurityTokenCache {

    private static final String CACHE_NAME = "user_sess";
    
    private CacheManager cacheManager;
    public CacheManager getCacheManager() {
        return cacheManager;
    }
    public void setCacheManager(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    private Cache sessionCache() {
        Cache c = cacheManager.getCache(CACHE_NAME);
        return c;
    }
    
    private CacheConfig config;
    public CacheConfig getCacheConfig() {
        return config;
    }
    public void setCacheConfig(CacheConfig config) {
        this.config = config;
    }
    
    @Override
    public void store(ServicesSessionBean bean) {
        sessionCache().put(new Element(bean.getWindAMSAPIAccessToken(), bean, 0, config.getTimeout()), true);
    }

    @Override
    public ServicesSessionBean retrieve(String bearerToken) {
        Element elem = sessionCache().get(bearerToken);
        if (elem != null) {
            return (ServicesSessionBean)elem.getObjectValue();
        } else {
            return null;
        }
    }
}
