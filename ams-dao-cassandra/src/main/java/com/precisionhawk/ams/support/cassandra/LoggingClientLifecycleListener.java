package com.precisionhawk.ams.support.cassandra;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author pchapman
 */
public class LoggingClientLifecycleListener implements ClientLifecycleListener {
    
    private final Logger LOGGER = LoggerFactory.getLogger(getClass());

    @Override
    public void clientConnectionOpened(Cluster cluster, Session session) {
        LOGGER.info("Connections to Cassandra servers has been successful");
    }

    @Override
    public void prepareForClose(Cluster cluster, Session session) {
        LOGGER.info("Closing connections to Cassandra servers");
    }

    @Override
    public void afterClose() {
        LOGGER.info("Connections to Cassandra servers closed");
    }
    
}
