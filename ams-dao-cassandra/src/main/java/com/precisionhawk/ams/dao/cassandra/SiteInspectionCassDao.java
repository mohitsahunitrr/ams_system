package com.precisionhawk.ams.dao.cassandra;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.SimpleStatement;
import com.precisionhawk.ams.bean.SiteInspectionSearchParams;
import com.precisionhawk.ams.dao.DaoException;
import com.precisionhawk.ams.dao.SiteInspectionDao;
import com.precisionhawk.ams.domain.SiteInspection;
import com.precisionhawk.ams.util.CollectionsUtilities;
import java.util.List;

/**
 *
 * @author pchapman
 */
public class SiteInspectionCassDao extends AbstractCassandraDao implements SiteInspectionDao {
    protected static final String STATEMENTS_MAPS = "com/precisionhawk/ams/dao/cassandra/SiteInspection_Statements.yaml";

    private static final String COL_ID = "id";
    private static final String COL_ORD_NUM = "ord_num";
    private static final String COL_SITE_ID = "site_id";
    private static final String COL_STATUS = "status";
    private static final String COL_TYPE = "type";
    
    private static final int PARAM_DEL_ID = 0;
    
    private static final int PARAM_INS_SITE_ID = 0;
    private static final int PARAM_INS_ORD_NUM = 1;
    private static final int PARAM_INS_STATUS = 2;
    private static final int PARAM_INS_TYPE = 3;
    private static final int PARAM_INS_OBJ_JSON = 4;
    private static final int PARAM_INS_ID = 5;
    
    private static final int PARAM_UPD_SITE_ID = 0;
    private static final int PARAM_UPD_ORD_NUM = 1;
    private static final int PARAM_UPD_STATUS = 2;
    private static final int PARAM_UPD_TYPE = 3;
    private static final int PARAM_UPD_OBJ_JSON = 4;
    private static final int PARAM_UPD_ID = 5;

    @Override
    protected String statementsMapsPath() {
        return STATEMENTS_MAPS;
    }
    
    @Override
    public boolean insert(SiteInspection insp) throws DaoException {
        ensureExists(insp, "Site Inspection is required");
        ensureExists(insp.getId(), "Site Inspection ID is required");
        ensureExists(insp.getOrderNumber(), "Order number is required");
        ensureExists(insp.getSiteId(), "Site ID is required");
        
        SiteInspection s = retrieve(insp.getId());
        if (s == null) {
            StatementBuilder stmt = new StatementBuilder(getStatementsMaps().getInsertStmt());
            stmt = stmt.setParameter(PARAM_INS_ID, insp.getId());
            stmt = stmt.setParameter(PARAM_INS_OBJ_JSON, serializeObject(insp));
            stmt = stmt.setParameter(PARAM_INS_ORD_NUM, insp.getOrderNumber());
            stmt = stmt.setParameter(PARAM_INS_SITE_ID, insp.getSiteId());
            stmt = stmt.setParameter(PARAM_INS_STATUS, insp.getStatus());
            stmt = stmt.setParameter(PARAM_INS_TYPE, insp.getType());
            ResultSet rs = getSession().execute(stmt.build());
            return rs.wasApplied();
        } else {
            return false;
        }
    }

    @Override
    public boolean update(SiteInspection insp) throws DaoException {
        ensureExists(insp, "Site Inspection is required");
        ensureExists(insp.getId(), "Site Inspection ID is required");
        ensureExists(insp.getOrderNumber(), "Order number is required");
        ensureExists(insp.getSiteId(), "Site ID is required");
        
        SiteInspection s = retrieve(insp.getId());
        if (s == null) {        
            return false;
        } else {
            StatementBuilder stmt = new StatementBuilder(getStatementsMaps().getUpdateStmt());
            stmt = stmt.setParameter(PARAM_UPD_ID, insp.getId());
            stmt = stmt.setParameter(PARAM_UPD_OBJ_JSON, serializeObject(insp));
            stmt = stmt.setParameter(PARAM_UPD_ORD_NUM, insp.getOrderNumber());
            stmt = stmt.setParameter(PARAM_UPD_SITE_ID, insp.getSiteId());
            stmt = stmt.setParameter(PARAM_UPD_STATUS, insp.getStatus());
            stmt = stmt.setParameter(PARAM_UPD_TYPE, insp.getType());
            ResultSet rs = getSession().execute(stmt.build());
            return rs.wasApplied();
        }
    }

    @Override
    public boolean delete(String id) throws DaoException {
        ensureExists(id, "Site Inspection ID is required");
        
        SiteInspection s = retrieve(id);
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
    public SiteInspection retrieve(String id) throws DaoException {
        StatementBuilder stmt = new StatementBuilder()
                .withSqlTemplate(getStatementsMaps().getSelectTemplate())
                .addEquals(COL_ID, id);
        return CollectionsUtilities.firstItemIn(selectObjects(SiteInspection.class, stmt.build(), 0));
    }

    @Override
    public List<SiteInspection> search(SiteInspectionSearchParams params) throws DaoException {
        StatementBuilder stmt = new StatementBuilder()
                .withSqlTemplate(getStatementsMaps().getSelectTemplate())
                .addEqualsConditionally(COL_ORD_NUM, params.getOrderNumber())
                .addEqualsConditionally(COL_SITE_ID, params.getSiteId())
                .addEqualsConditionally(COL_STATUS, params.getStatus())
                .addEqualsConditionally(COL_TYPE, params.getType())
                ;
        if (stmt.hasWhereClause()) {
            return selectObjects(SiteInspection.class, stmt.build(), 0);
        } else {
            throw new DaoException("Search parameters are required.");
        }
    }

}
