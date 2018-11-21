package com.precisionhawk.ams.config;

/**
 *
 * @author pchapman
 */
public interface CacheConfig {
    
    String getCacheImplementation();
    
    Integer getTimeout();
}
