package com.precisionhawk.ams.dao.cassandra;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.SimpleStatement;
import com.precisionhawk.ams.bean.InspectionEventSearchParams;
import com.precisionhawk.ams.dao.DaoException;
import com.precisionhawk.ams.dao.InspectionEventDao;
import com.precisionhawk.ams.domain.InspectionEvent;
import com.precisionhawk.ams.domain.Site;
import com.precisionhawk.ams.util.CollectionsUtilities;
import java.util.List;

/**
 *
 * @author pchapman
 */
public class InspectionEventCassDao extends AbstractCassandraDao implements InspectionEventDao {
    protected static final String STATEMENTS_MAPS = "com/precisionhawk/ams/dao/cassandra/InspectionEvent_Statements.yaml";

    private static final String COL_ASSET_ID = "asset_id";
    private static final String COL_COMP_ID = "component_id";
    private static final String COL_ID = "id";
    private static final String COL_SITE_ID = "site_id";
    private static final String COL_ORD_NUM = "order_num";
    
    private static final int PARAM_DEL_ID = 0;
    
    private static final int PARAM_INS_SITE_ID = 0;
    private static final int PARAM_INS_ORD_NUM = 1;
    private static final int PARAM_INS_ASSET_ID = 2;
    private static final int PARAM_INS_COMP_ID = 3;
    private static final int PARAM_INS_OBJ_JSON = 4;
    private static final int PARAM_INS_ID = 5;
    
    private static final int PARAM_UPD_SITE_ID = 0;
    private static final int PARAM_UPD_ORD_NUM = 1;
    private static final int PARAM_UPD_ASSET_ID = 2;
    private static final int PARAM_UPD_COMP_ID = 3;
    private static final int PARAM_UPD_OBJ_JSON = 4;
    private static final int PARAM_UPD_ID = 5;

    @Override
    protected String statementsMapsPath() {
        return STATEMENTS_MAPS;
    }
    
    @Override
    public boolean insert(InspectionEvent evt) throws DaoException {
        ensureExists(evt, "Inspection Event is required");
        ensureExists(evt.getId(), "Inspection Event ID is required");
        ensureExists(evt.getOrderNumber(), "Work Order is required");
        ensureExists(evt.getSiteId(), "Site ID is required");
        
        InspectionEvent e = retrieve(evt.getId());
        if (e == null) {
            Object[] values = new Object[6];
            values[PARAM_INS_ASSET_ID] = evt.getAssetId();
            values[PARAM_INS_COMP_ID] = evt.getComponentId();
            values[PARAM_INS_ID] = evt.getId();
            values[PARAM_INS_OBJ_JSON] = serializeObject(evt);
            values[PARAM_INS_ORD_NUM] = evt.getOrderNumber();
            values[PARAM_INS_SITE_ID] = evt.getSiteId();
            SimpleStatement stmt = new SimpleStatement(getStatementsMaps().getInsertStmt(), values);
            ResultSet rs = getSession().execute(stmt);
            return rs.wasApplied();
        } else {
            return false;
        }
    }

    @Override
    public boolean update(InspectionEvent evt) throws DaoException {
        ensureExists(evt, "Inspection Event is required");
        ensureExists(evt.getId(), "Inspection Event ID is required");
        ensureExists(evt.getOrderNumber(), "Work Order is required");
        ensureExists(evt.getSiteId(), "Site ID is required");
        
        InspectionEvent e = retrieve(evt.getId());
        if (e == null) {
            return false;
        } else {
            Object[] values = new Object[6];
            values[PARAM_INS_ASSET_ID] = evt.getAssetId();
            values[PARAM_INS_COMP_ID] = evt.getComponentId();
            values[PARAM_INS_ID] = evt.getId();
            values[PARAM_INS_OBJ_JSON] = serializeObject(evt);
            values[PARAM_INS_ORD_NUM] = evt.getOrderNumber();
            values[PARAM_INS_SITE_ID] = evt.getSiteId();
            SimpleStatement stmt = new SimpleStatement(getStatementsMaps().getUpdateStmt(), values);
            ResultSet rs = getSession().execute(stmt);
            return rs.wasApplied();
        }
    }

    @Override
    public boolean delete(String id) throws DaoException {
        ensureExists(id, "Site ID is required");
        
        InspectionEvent e = retrieve(id);
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
    public InspectionEvent retrieve(String id) throws DaoException {
        SelectStatementBuilder stmt = new SelectStatementBuilder()
                .withSqlTemplate(getStatementsMaps().getSelectTemplate())
                .addEquals(COL_ID, id);
        return CollectionsUtilities.firstItemIn(selectObjects(InspectionEvent.class, stmt.build(), 0));
    }

    @Override
    public List<InspectionEvent> search(InspectionEventSearchParams params) throws DaoException {
        SelectStatementBuilder stmt = new SelectStatementBuilder()
                .withSqlTemplate(getStatementsMaps().getSelectTemplate())
                .addEqualsConditionally(COL_ASSET_ID, params.getAssetId())
                .addEqualsConditionally(COL_COMP_ID, params.getComponentId())
                .addEqualsConditionally(COL_ORD_NUM, params.getOrderNumber())
                .addEqualsConditionally(COL_SITE_ID, params.getSiteId());
        if (stmt.hasWhereClause()) {
            return selectObjects(InspectionEvent.class, stmt.build(), 0);
        } else {
            throw new DaoException("Search parameters are required.");
        }
    }

    @Override
    public Long count(InspectionEventSearchParams params) throws DaoException {
        SelectStatementBuilder stmt = new SelectStatementBuilder()
                .withSqlTemplate(getStatementsMaps().getSelectTemplate())
                .addEqualsConditionally(COL_ASSET_ID, params.getAssetId())
                .addEqualsConditionally(COL_COMP_ID, params.getComponentId())
                .addEqualsConditionally(COL_ORD_NUM, params.getOrderNumber())
                .addEqualsConditionally(COL_SITE_ID, params.getSiteId());
        if (stmt.hasWhereClause()) {
            ResultSet rset = getSession().execute(stmt.build());
            return rset.one().getLong(0);
        } else {
            throw new DaoException("Search parameters are required.");
        }
    }

}
