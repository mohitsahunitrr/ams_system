package com.precisionhawk.ams.support.cassandra;

/**
 *
 * @author pchapman
 */
public interface CassandraConfig {
    
    /**
     * The name of the cassandra keyspace.
     * @return The keyspace.
     */
    public String getKeyspace();
    
    /**
     * A comma-delimited list of YugaByte servers to connect to by IP address or
     * host name.
     * @return The list of hosts.
     */
    public String getNodeHosts();
}
