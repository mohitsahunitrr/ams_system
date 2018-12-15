package com.precisionhawk.ams.dao.rdbms;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author pchapman
 */
public abstract class AbstractSQLDao {
    
    protected final Logger LOGGER = LoggerFactory.getLogger(getClass());

    @Autowired
    protected DataSource dataSource;

    protected void closeQuietly(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException ex) {
                LOGGER.error("Error closing DB connection", ex);
            }
        }
    }
    
    protected void closeQuietly(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException ex) {
                LOGGER.error("Error closing JDBC ResultSet", ex);
            }
        }
    }
    
    protected void closeQuietly(Statement stmt) {
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException ex) {
                LOGGER.error("Error closing JDBC Statmeent", ex);
            }
        }
    }
    
    protected void beginTrans(Connection conn) throws SQLException {
        if (conn != null && conn.getAutoCommit()) {
            conn.setAutoCommit(false);
        }
    }
    
    protected void commitTrans(Connection conn) throws SQLException {
        if (conn != null) {
            conn.commit();
        }
    }
    
    protected void rollbackTrans(Connection conn) {
        try {
            if (conn != null && (!conn.getAutoCommit())) {
                    conn.rollback();
            }
        } catch (SQLException ex2) {
            LOGGER.error("Unable to rollback transaction", ex2);
        }
    }
    
    protected void resetConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException ex) {
                LOGGER.error("Unable to set autocommit to true", ex);
            }
        }
    }
}
