package com.precisionhawk.ams.dao.rdbms;

import com.precisionhawk.ams.config.DbConfig;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.sql.DataSource;
import org.postgresql.ds.PGPoolingDataSource;

/**
 *
 * @author pchapman
 */
@Named
public class DataSourceFactory implements Provider<DataSource> {
    
    @Inject private DbConfig config;

    @Override
    public DataSource get() {
        PGPoolingDataSource ds = new PGPoolingDataSource();
        ds.setApplicationName("PrecisionHawk AMS");
        ds.setDataSourceName("DS Pool");
        ds.setDatabaseName(config.getDataBaseName());
        ds.setInitialConnections(config.getInitialConnections());
        ds.setMaxConnections(config.getMaxCommections());
        ds.setPassword(config.getPassword());
        ds.setPortNumber(config.getPortNumber());
        ds.setServerName(config.getServerName());
        ds.setUser(config.getUserName());
        return ds;
    }
    
}
