package com.precisionhawk.ams.dao.cassandra;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.SimpleStatement;
import com.precisionhawk.ams.bean.SiteSearchParams;
import com.precisionhawk.ams.dao.DaoException;
import com.precisionhawk.ams.dao.SiteDao;
import com.precisionhawk.ams.domain.Site;
import com.precisionhawk.ams.util.CollectionsUtilities;
import java.util.List;

/**
 *
 * @author pchapman
 */
public class SiteCassDao extends AbstractCassandraDao implements SiteDao {
    protected static final String STATEMENTS_MAPS = "com/precisionhawk/ams/dao/cassandra/Site_Statements.yaml";

    private static final String COL_ID = "id";
    private static final String COL_ORG_ID = "org_id";
    private static final String COL_SITE_NAME = "site_name";
    
    private static final int PARAM_DEL_ID = 0;
    
    private static final int PARAM_INS_ORG_ID = 0;
    private static final int PARAM_INS_SITE_NAME = 1;
    private static final int PARAM_INS_OBJ_JSON = 2;
    private static final int PARAM_INS_ID = 3;
    
    private static final int PARAM_UPD_ORG_ID = 0;
    private static final int PARAM_UPD_SITE_NAME = 1;
    private static final int PARAM_UPD_OBJ_JSON = 2;
    private static final int PARAM_UPD_ID = 3;

    @Override
    protected String statementsMapsPath() {
        return STATEMENTS_MAPS;
    }
    
    @Override
    public boolean insert(Site site) throws DaoException {
        ensureExists(site, "Site is required");
        ensureExists(site.getId(), "Site ID is required");
        ensureExists(site.getName(), "Site name is required");
        ensureExists(site.getOrganizationId(), "Organization ID is required");
        
        Site s = retrieve(site.getId());
        if (s == null) {        
            Object[] values = new Object[4];
            values[PARAM_INS_ID] = site.getId();
            values[PARAM_INS_OBJ_JSON] = serializeObject(site);
            values[PARAM_INS_ORG_ID] = site.getId();
            values[PARAM_INS_SITE_NAME] = site.getName();
            SimpleStatement stmt = new SimpleStatement(getStatementsMaps().getInsertStmt(), values);
            ResultSet rs = getSession().execute(stmt);
            return rs.wasApplied();
        } else {
            return false;
        }
    }

    @Override
    public boolean update(Site site) throws DaoException {
        ensureExists(site, "Site is required");
        ensureExists(site.getId(), "Site ID is required");
        ensureExists(site.getName(), "Site name is required");
        ensureExists(site.getOrganizationId(), "Organization ID is required");
        
        Site s = retrieve(site.getId());
        if (s == null) {
            return false;
        } else {
            Object[] values = new Object[4];
            values[PARAM_UPD_ID] = site.getId();
            values[PARAM_UPD_OBJ_JSON] = serializeObject(site);
            values[PARAM_UPD_ORG_ID] = site.getId();
            values[PARAM_UPD_SITE_NAME] = site.getName();
            SimpleStatement stmt = new SimpleStatement(getStatementsMaps().getUpdateStmt(), values);
            ResultSet rs = getSession().execute(stmt);
            return rs.wasApplied();
        }
    }

    @Override
    public boolean delete(String id) throws DaoException {
        ensureExists(id, "Site ID is required");
        
        Site s = retrieve(id);
        if (s == null) {
            return false;
        } else {
            Object[] values = new Object[1];
            values[PARAM_DEL_ID] = id;
            SimpleStatement stmt = new SimpleStatement(getStatementsMaps().getDeleteStmt(), values);
            ResultSet rs = getSession().execute(stmt);
            return rs.wasApplied();
        }
    }

    @Override
    public Site retrieve(String id) throws DaoException {
        SelectStatementBuilder stmt = new SelectStatementBuilder()
                .withSqlTemplate(getStatementsMaps().getSelectTemplate())
                .addEquals(COL_ID, id);
        return CollectionsUtilities.firstItemIn(selectObjects(Site.class, stmt.build(), 0));
    }

    @Override
    public List<Site> search(SiteSearchParams params) throws DaoException {
        SelectStatementBuilder stmt = new SelectStatementBuilder()
                .withSqlTemplate(getStatementsMaps().getSelectTemplate())
                .addEqualsConditionally(COL_SITE_NAME, params.getName())
                .addEqualsConditionally(COL_ORG_ID, params.getOrganizationId());
        if (stmt.hasWhereClause()) {
            return selectObjects(Site.class, stmt.build(), 0);
        } else {
            throw new DaoException("Search parameters are required.");
        }
    }

    @Override
    public List<Site> retrieveAll() throws DaoException {
        SelectStatementBuilder stmt = new SelectStatementBuilder()
                .withSqlTemplate(getStatementsMaps().getSelectTemplate());
        return selectObjects(Site.class, stmt.build(), 0);
    }

}
