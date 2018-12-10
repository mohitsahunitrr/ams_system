package com.precisionhawk.ams.dao.cassandra;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.SimpleStatement;
import com.precisionhawk.ams.bean.InspectionEventResourceSearchParams;
import com.precisionhawk.ams.dao.DaoException;
import com.precisionhawk.ams.dao.InspectionEventResourceDao;
import com.precisionhawk.ams.domain.InspectionEventResource;
import com.precisionhawk.ams.util.CollectionsUtilities;
import java.util.List;

/**
 *
 * @author pchapman
 */
public class InspectionEventResourceCassDao extends AbstractCassandraDao implements InspectionEventResourceDao {
    protected static final String STATEMENTS_MAPS = "com/precisionhawk/ams/dao/cassandra/InspectionEventResource_Statements.yaml";

    private static final String COL_ASSET_ID = "asset_id";
    private static final String COL_COMP_ID = "component_id";
    private static final String COL_ID = "id";
    private static final String COL_INSP_EVT_ID = "insp_event_id";
    private static final String COL_RES_ID = "res_id";
    private static final String COL_SITE_ID = "site_id";
    private static final String COL_ORD_NUM = "order_num";
    
    private static final int PARAM_DEL_ID = 0;
    
    private static final int PARAM_INS_SITE_ID = 0;
    private static final int PARAM_INS_ORD_NUM = 1;
    private static final int PARAM_INS_ASSET_ID = 2;
    private static final int PARAM_INS_COMP_ID = 3;
    private static final int PARAM_INS_INSP_EVT_ID = 4;
    private static final int PARAM_INS_RES_ID = 5;
    private static final int PARAM_INS_OBJ_JSON = 6;
    private static final int PARAM_INS_ID = 7;
    
    private static final int PARAM_UPD_SITE_ID = 0;
    private static final int PARAM_UPD_ORD_NUM = 1;
    private static final int PARAM_UPD_ASSET_ID = 2;
    private static final int PARAM_UPD_COMP_ID = 3;
    private static final int PARAM_UPD_INSP_EVT_ID = 4;
    private static final int PARAM_UPD_RES_ID = 5;
    private static final int PARAM_UPD_OBJ_JSON = 6;
    private static final int PARAM_UPD_ID = 7;

    @Override
    protected String statementsMapsPath() {
        return STATEMENTS_MAPS;
    }
    
    @Override
    public boolean insert(InspectionEventResource evt) throws DaoException {
        ensureExists(evt, "Inspection Event Resource is required");
        ensureExists(evt.getId(), "Inspection Event Resource ID is required");
        ensureExists(evt.getOrderNumber(), "Work Order is required");
        ensureExists(evt.getSiteId(), "Site ID is required");
        ensureExists(evt.getInspectionEventId(), "Inspection event ID is required");
        ensureExists(evt.getResourceId(), "Resource ID is required");
        
        InspectionEventResource e = retrieve(evt.getId());
        if (e == null) {
            StatementBuilder stmt = new StatementBuilder(getStatementsMaps().getInsertStmt());
            stmt = stmt.setParameter(PARAM_INS_ASSET_ID, evt.getAssetId());
            stmt = stmt.setParameter(PARAM_INS_COMP_ID, evt.getComponentId());
            stmt = stmt.setParameter(PARAM_INS_ID, evt.getId());
            stmt = stmt.setParameter(PARAM_INS_INSP_EVT_ID, evt.getInspectionEventId());
            stmt = stmt.setParameter(PARAM_INS_OBJ_JSON, serializeObject(evt));
            stmt = stmt.setParameter(PARAM_INS_ORD_NUM, evt.getOrderNumber());
            stmt = stmt.setParameter(PARAM_INS_RES_ID, evt.getResourceId());
            stmt = stmt.setParameter(PARAM_INS_SITE_ID, evt.getSiteId());
            ResultSet rs = getSession().execute(stmt.build());
            return rs.wasApplied();
        } else {
            return false;
        }
    }

    @Override
    public boolean update(InspectionEventResource evt) throws DaoException {
        ensureExists(evt, "Inspection Event Resource is required");
        ensureExists(evt.getId(), "Inspection Event Resource ID is required");
        ensureExists(evt.getOrderNumber(), "Work Order is required");
        ensureExists(evt.getSiteId(), "Site ID is required");
        ensureExists(evt.getInspectionEventId(), "Inspection event ID is required");
        ensureExists(evt.getResourceId(), "Resource ID is required");
        
        InspectionEventResource e = retrieve(evt.getId());
        if (e == null) {
            return false;
        } else {
            StatementBuilder stmt = new StatementBuilder(getStatementsMaps().getUpdateStmt());
            stmt = stmt.setParameter(PARAM_UPD_ASSET_ID, evt.getAssetId());
            stmt = stmt.setParameter(PARAM_UPD_COMP_ID, evt.getComponentId());
            stmt = stmt.setParameter(PARAM_UPD_ID, evt.getId());
            stmt = stmt.setParameter(PARAM_UPD_INSP_EVT_ID, evt.getInspectionEventId());
            stmt = stmt.setParameter(PARAM_UPD_OBJ_JSON, serializeObject(evt));
            stmt = stmt.setParameter(PARAM_UPD_ORD_NUM, evt.getOrderNumber());
            stmt = stmt.setParameter(PARAM_UPD_RES_ID, evt.getResourceId());
            stmt = stmt.setParameter(PARAM_UPD_SITE_ID, evt.getSiteId());
            ResultSet rs = getSession().execute(stmt.build());
            return rs.wasApplied();
        }
    }

    @Override
    public boolean delete(String id) throws DaoException {
        ensureExists(id, "Inspection Event Resource ID is required");
        
        InspectionEventResource e = retrieve(id);
        if (e == null) {
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
    public InspectionEventResource retrieve(String id) throws DaoException {
        ensureExists(id, "Inspection Event Resource ID is required");
        StatementBuilder stmt = new StatementBuilder()
                .withSqlTemplate(getStatementsMaps().getSelectTemplate())
                .addEquals(COL_ID, id);
        return CollectionsUtilities.firstItemIn(selectObjects(InspectionEventResource.class, stmt.build(), 0));
    }

    @Override
    public List<InspectionEventResource> search(InspectionEventResourceSearchParams params) throws DaoException {
        StatementBuilder stmt = new StatementBuilder()
                .withSqlTemplate(getStatementsMaps().getSelectTemplate())
                .addEqualsConditionally(COL_ASSET_ID, params.getAssetId())
                .addEqualsConditionally(COL_COMP_ID, params.getComponentId())
                .addEqualsConditionally(COL_INSP_EVT_ID, params.getInspectionEventId())
                .addEqualsConditionally(COL_ORD_NUM, params.getOrderNumber())
                .addEqualsConditionally(COL_RES_ID, params.getResourceId())
                .addEqualsConditionally(COL_SITE_ID, params.getSiteId());
        if (stmt.hasWhereClause()) {
            return selectObjects(InspectionEventResource.class, stmt.build(), 0);
        } else {
            throw new DaoException("Search parameters are required.");
        }
    }

}
