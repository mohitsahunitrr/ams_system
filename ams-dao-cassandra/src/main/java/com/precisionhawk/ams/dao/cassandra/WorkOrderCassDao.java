package com.precisionhawk.ams.dao.cassandra;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.SimpleStatement;
import com.precisionhawk.ams.bean.WorkOrderSearchParams;
import com.precisionhawk.ams.dao.DaoException;
import com.precisionhawk.ams.dao.WorkOrderDao;
import com.precisionhawk.ams.domain.WorkOrder;
import com.precisionhawk.ams.util.CollectionsUtilities;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

/**
 *
 * @author pchapman
 */
public class WorkOrderCassDao extends AbstractCassandraDao implements WorkOrderDao {
    protected static final String STATEMENTS_MAPS = "com/precisionhawk/ams/dao/cassandra/WorkOrder_Statements.yaml";

    private static final String COL_ORD_NUM = "id";
    private static final String COL_STATUS = "status";
    private static final String COL_TYPE = "type";
    
    private static final int PARAM_DEL_ID = 0;
    
    private static final int PARAM_INS_STATUS = 0;
    private static final int PARAM_INS_TYPE = 1;
    private static final int PARAM_INS_OBJ_JSON = 2;
    private static final int PARAM_INS_ORD_NUM = 3;
    
    private static final int PARAM_UPD_STATUS = 0;
    private static final int PARAM_UPD_TYPE = 1;
    private static final int PARAM_UPD_OBJ_JSON = 2;
    private static final int PARAM_UPD_ORD_NUM = 3;
    
    private static final String SWO_SQL_DEL = "deleteSiteIDs";
    private static final String SWO_SQL_INS = "insertSiteIDs";
    private static final String SWO_SQL_SEL = "selectSiteIDs";
    
    private static final int SWO_DEL_ID = 0;
    private static final String SWO_DEL_SITE_ID_PLACEHOLDER = "{site_ids}";
    private static final int SWO_PARAM_INS_ID = 0;
    private static final int SWO_PARAM_INS_SITE_ID = 1;
    private static final int SWO_PARAM_SEL_ID = 0;

    @Override
    protected String statementsMapsPath() {
        return STATEMENTS_MAPS;
    }
    
    @Override
    public boolean insert(WorkOrder order) throws DaoException {
        ensureExists(order, "Work Order is required");
        ensureExists(order.getOrderNumber(), "Order Number is required");
        
        WorkOrder o = retrieve(order.getOrderNumber());
        if (o == null) {
            StatementBuilder stmt = new StatementBuilder(getStatementsMaps().getInsertStmt());
            stmt = stmt.setParameter(PARAM_INS_OBJ_JSON, serializeObject(order));
            stmt = stmt.setParameter(PARAM_INS_ORD_NUM, order.getOrderNumber());
            stmt = stmt.setParameter(PARAM_INS_STATUS, order.getStatus());
            stmt = stmt.setParameter(PARAM_INS_TYPE, order.getType());
            ResultSet rs = getSession().execute(stmt.build());
            if (rs.wasApplied()) {
                return insertSiteRelationships(order.getOrderNumber(), order.getSiteIds());
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
    
    private boolean insertSiteRelationships(String orderNumber, Collection<String> siteIDs) {
        // Insert site relationships
        ResultSet rs;
        StatementBuilder stmt = new StatementBuilder(getStatementsMaps().getNamedTemplates().get(SWO_SQL_INS));
        stmt.setParameter(SWO_PARAM_INS_ID, orderNumber);
        for (String siteId : siteIDs) {
            stmt.setParameter(SWO_PARAM_INS_SITE_ID, siteId);
            rs = getSession().execute(stmt.build());
            if (!rs.wasApplied()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean update(WorkOrder order) throws DaoException {
        ensureExists(order, "Work Order is required");
        ensureExists(order.getOrderNumber(), "Order Number is required");
        
        boolean inTransaction = false;
        boolean success = false;
        try {
            WorkOrder o = retrieve(order.getOrderNumber());
            if (o == null) {
                return false;
            } else {
                success = beginTransaction();
                if (success) {
                    inTransaction = true;
                    StatementBuilder stmt = new StatementBuilder(getStatementsMaps().getUpdateStmt());
                    stmt = stmt.setParameter(PARAM_UPD_OBJ_JSON, serializeObject(order));
                    stmt = stmt.setParameter(PARAM_UPD_ORD_NUM, order.getOrderNumber());
                    stmt = stmt.setParameter(PARAM_UPD_STATUS, order.getStatus());
                    stmt = stmt.setParameter(PARAM_UPD_TYPE, order.getType());
                    ResultSet rs = getSession().execute(stmt.build());
                    success = rs.wasApplied();
                    if (success) {
                        // Add any sites not already associated with the work order
                        HashSet<String> toSave = new HashSet<>(order.getSiteIds());
                        HashSet<String> alreadySaved = new HashSet<>(o.getSiteIds());
                        toSave.removeAll(alreadySaved);
                        if (!toSave.isEmpty()) {
                            success = insertSiteRelationships(order.getOrderNumber(), toSave);
                        }
                        if (success) {
                            // Delete any site relationships that are no longer valid.
                            stmt = new StatementBuilder(getStatementsMaps().getNamedTemplates().get(SWO_SQL_DEL));
                            stmt.setParameter(SWO_DEL_ID, order.getOrderNumber());
                            stmt.setInClause(order.getSiteIds()); // Delete all sites related to the work order that are not in this list
                            rs = getSession().execute(stmt.build());
                            success = rs.wasApplied();
                        }
                    }
                }
            }
        } finally {
            if (inTransaction) {
                success = endTransaction();
            }
        }
        return success;
    }

    @Override
    public boolean delete(String id) throws DaoException {
        ensureExists(id, "Order Number is required");
        
        WorkOrder o = retrieve(id);
        if (o == null) {
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
    public WorkOrder retrieve(String id) throws DaoException {
        ensureExists(id, "Order Number is required");

        StatementBuilder stmt = new StatementBuilder()
                .withSqlTemplate(getStatementsMaps().getSelectTemplate())
                .addEquals(COL_ORD_NUM, id);
        return CollectionsUtilities.firstItemIn(selectObjects(WorkOrder.class, stmt.build(), 0));
    }

    @Override
    public List<WorkOrder> search(WorkOrderSearchParams params) throws DaoException {
        StatementBuilder stmt = new StatementBuilder()
                .withSqlTemplate(getStatementsMaps().getSelectTemplate())
                .addInClauseConditionally(COL_STATUS, params.getStatuses())
                .addEqualsConditionally(COL_TYPE, params.getType());
        if (stmt.hasWhereClause()) {
            return selectObjects(WorkOrder.class, stmt.build(), 0);
        } else {
            throw new DaoException("Search parameters are required.");
        }
    }

}
