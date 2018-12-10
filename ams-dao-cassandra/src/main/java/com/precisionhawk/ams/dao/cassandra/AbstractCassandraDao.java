package com.precisionhawk.ams.dao.cassandra;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import org.codehaus.jackson.map.ObjectMapper;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import com.esotericsoftware.yamlbeans.YamlReader;
import com.precisionhawk.ams.dao.AbstractDao;
import com.precisionhawk.ams.dao.DaoException;
import com.precisionhawk.ams.support.cassandra.CassandraConfig;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author pchapman
 */
public abstract class AbstractCassandraDao extends AbstractDao {
    
    protected final Logger LOGGER = LoggerFactory.getLogger(getClass());
    
    @Inject
    private CassandraConfig config;
    public CassandraConfig getConfig() {
        return config;
    }
    public void setConfig(CassandraConfig config) {
        this.config = config;
    }

    @Inject
    private ObjectMapper mapper;
    public ObjectMapper getMapper() {
        return mapper;
    }
    public void setMapper(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Inject
    private Session session;
    public Session getSession() {
        return session;
    }
    public void setSession(Session session) {
        this.session = session;
    }
    
    private StatementsMaps statementsMaps;
    protected StatementsMaps getStatementsMaps() {
        return statementsMaps;
    }
    protected void setStatementsMaps(StatementsMaps statementsMaps) {
        this.statementsMaps = statementsMaps;
    }
    
    protected abstract String statementsMapsPath();
    
    protected <T> List<T> selectObjects(Class<T> klass, Statement statement, int objCol) throws DaoException {
        ResultSet rset = getSession().execute(statement);
        return readObjects(klass, rset, objCol);
    }
    
    /**
     * Deserializes a JSON string to the specified object type
     *
     * @param json the string to deserialize
     * @param klass the object type to use
     * @return the object
     * @throws DaoException on error
     */
    protected <T> List<T> readObjects(Class<T> klass, ResultSet rset, int objCol) throws DaoException {
        String json = null;
        try {
            List<T> results = new ArrayList<>();
            for (Row row = rset.one(); row != null; row = rset.one()) {
                json = row.getString(objCol);
                results.add(getMapper().readValue(json, klass));
            }
            return results;
        } catch (IOException e) {
            LOGGER.error("Unable to deserialize object from JSON", e);
            LOGGER.error("{}", json);
            throw new DaoException("Unable to deserialize object", e);
        }
    }
    
    protected String serializeObject(Object obj) throws DaoException {
        try {
            return getMapper().writeValueAsString(obj);
        } catch (IOException e) {
            throw new DaoException("Unable to serialize object", e);
        }
    }

    @PostConstruct
    public void init() {
        Reader reader = null;
        try {
            // Load statements map
            URL url = getClass().getClassLoader().getResource(statementsMapsPath());
            if (url == null) {
                throw new IllegalArgumentException(String.format("Invalid statements map source URL: %s", statementsMapsPath()));
            }
            reader = new InputStreamReader(url.openStream());
            YamlReader yamlreader = new YamlReader(reader);
            statementsMaps = yamlreader.read(StatementsMaps.class);
            // Execute the initialization statements
            for (String stmt : statementsMaps.getInitStmts()) {
                getSession().execute(stmt);
            }
        } catch (IOException | IllegalArgumentException e) {
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            IOUtils.closeQuietly(reader);
        }
    }
    
    protected boolean endTransaction() {
        ResultSet rs = getSession().execute("END TRANSACTION;");
        return rs.wasApplied();
    }
    
    protected boolean beginTransaction() {
        ResultSet rs = getSession().execute("BEGIN TRANSACTION;");
        return rs.wasApplied();
    }
}
