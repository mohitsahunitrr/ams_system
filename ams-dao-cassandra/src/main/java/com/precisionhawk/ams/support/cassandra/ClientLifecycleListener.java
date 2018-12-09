package com.precisionhawk.ams.support.cassandra;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;

/**
 *
 * @author pchapman
 */
public interface ClientLifecycleListener {

    public void clientConnectionOpened(Cluster cluster, Session session);

    public void prepareForClose(Cluster cluster, Session session);

    public void afterClose();
}