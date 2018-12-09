package com.precisionhawk.ams.support.cassandra;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Cluster.Builder;
import com.datastax.driver.core.Session;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author pchapman
 */
@Named
public class CassandraClientFactory implements Provider<Session> {
    
    private final Logger LOGGER = LoggerFactory.getLogger(getClass());

    @Inject
    private CassandraConfig config;
    public CassandraConfig getConfig() {
        return config;
    }
    public void setConfig(CassandraConfig config) {
        this.config = config;
    }
    
    @Inject
    private ClientLifecycleListener listener;
    public ClientLifecycleListener getListener() {
        return listener;
    }
    public void setListener(ClientLifecycleListener listener) {
        this.listener = listener;
    }
    
    private Cluster cluster;
    private Session session;
    
    private final Object LOCK = new Object();
    
    @Override
    public Session get() {
        synchronized (LOCK) {
            if (cluster == null || session == null) {
                initialize();
            }
        }
        return session;
    }
    
    private void initialize() {
        Builder b = Cluster.builder();
        for (String host : getConfig().getNodeHosts().split(",")) {
            b = b.addContactPoint(host);
        }
        cluster = b.build();
        session = cluster.connect();
        if (listener != null) listener.clientConnectionOpened(cluster, session);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                CassandraClientFactory.this.destroy();
            }
        });
    }
    /**
     * Closes the Client connection and removes references to the singletons.
     * Lifecycle methods in the listener is called, if one was registered. A
     * Subsequent call to getClient() will result in a new Client singleton
     * instance.
     */
    public void destroy() {
        // try to avoid race conditions without using a synchronized block
        Session s = session;
        session = null;
        Cluster c = cluster;
	cluster = null;

        if (c != null && s != null) {
            if (listener != null) listener.prepareForClose(c, s);
            s.close();
            LOGGER.debug("YugaByte session client has been closed");
            c.close();
            LOGGER.debug("YugaByte cluster client has been closed");
        } else {
            LOGGER.warn("No YugaByte cluster client or session to close");
        }
        if (listener != null) listener.afterClose();
    }
}
