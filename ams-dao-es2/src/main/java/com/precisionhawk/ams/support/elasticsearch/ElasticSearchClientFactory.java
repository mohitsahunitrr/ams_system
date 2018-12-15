package com.precisionhawk.ams.support.elasticsearch;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import org.elasticsearch.client.Client;
import us.pcsw.es.util.AbstractSearchClientFactory;
import us.pcsw.es.util.ClientLifecycleListener;
import us.pcsw.es.util.InProcessElasticSearchClientFactory;
import us.pcsw.es.util.TransportElasticSearchClientFactory;

/**
 *
 * @author Philip A. Chapman
 */
@Named
public class ElasticSearchClientFactory implements Provider<Client> {
    
    private static final Object LOCK = new Object();

    @Inject private ElasticSearchConfig config;
    public ElasticSearchConfig getConfig() {
        return config;
    }
    public void setConfig(ElasticSearchConfig config) {
        this.config = config;
    }
    
    @Inject private ClientLifecycleListener listener;
    public ClientLifecycleListener getListener() {
        return listener;
    }
    public void setListener(ClientLifecycleListener listener) {
        this.listener = listener;
    }

    private AbstractSearchClientFactory delegate;
    
    @Override
    public Client get() {
        synchronized (LOCK) {
            if (delegate == null) {
                if (config.isInProcess()) {
                    delegate = new InProcessElasticSearchClientFactory();
                } else {
                    TransportElasticSearchClientFactory f = new TransportElasticSearchClientFactory();
                    f.setClusterName(config.getClusterName());
                    f.setConnectTimeout(config.getConnectTimeout());
                    f.setNodeHosts(config.getNodeHosts());
                    delegate = f;
                }
                delegate.setClientLifecycleListener(listener);
            }
        }
        return delegate.getClient();
    }
}
