package com.precisionhawk.ams.config;

import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

/**
 *
 * @author Philip A. Chapman
 */
@Named
public class AwsConfigFactory extends MapToConfig implements Provider<AwsConfig> {

    @Inject
    @Named("AwsConfigMap")
    private Map<String, Object> configMap;
    public Map<String, Object> getConfigMap() {
        return configMap;
    }
    public void setConfigMap(Map<String, Object> configMap) {
        this.configMap = configMap;
    }
    
    private AwsConfigBean config;
    
    private final Object LOCK = new Object();
    
    @Override
    protected Map<String, Object> configMap() {
        return configMap;
    }
    
    @Override
    public AwsConfig get() {
        synchronized (LOCK) {
            if (config == null) {
                config = new AwsConfigBean();
                config.setAccessKey(stringFromMap("accessKey"));
                config.setRegion(stringFromMap("region"));
                config.setSecretKey(stringFromMap("secretKey"));
            }
        }
        return config;
    }
}
