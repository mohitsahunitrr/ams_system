package com.precisionhawk.ams.dao.cassandra;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.SimpleStatement;
import com.precisionhawk.ams.bean.ResourceSearchParams;
import com.precisionhawk.ams.dao.DaoException;
import com.precisionhawk.ams.dao.ResourceMetadataDao;
import com.precisionhawk.ams.domain.ResourceMetadata;
import com.precisionhawk.ams.util.CollectionsUtilities;
import java.util.List;

/**
 *
 * @author pchapman
 */
public class ResourceMetadataCassDao extends AbstractCassandraDao implements ResourceMetadataDao {
    protected static final String STATEMENTS_MAPS = "com/precisionhawk/ams/dao/cassandra/ResourceMetadata_Statements.yaml";

    private static final String COL_ASSET_ID = "asset_id";
    private static final String COL_ASSET_INSP_ID = "asset_insp_id";
    private static final String COL_COMP_ID = "component_id";
    private static final String COL_COMP_INSP_ID = "comp_insp_id";
    private static final String COL_ID = "id";
    private static final String COL_NAME = "name";
    private static final String COL_ORD_NUM = "order_num";
    private static final String COL_SITE_ID = "site_id";
    private static final String COL_SITE_INSP_ID = "site_insp_id";
    private static final String COL_SOURCE_ID = "source_id";
    private static final String COL_STATUS = "status";
    private static final String COL_TYPE = "type";
    private static final String COL_ZOOMIFY_ID = "zoomify_id";
    
    private static final int PARAM_DEL_ID = 0;
    
    private static final int PARAM_INS_SITE_ID = 0;
    private static final int PARAM_INS_ORD_NUM = 1;
    private static final int PARAM_INS_ASSET_ID = 2;
    private static final int PARAM_INS_COMP_ID = 3;
    private static final int PARAM_INS_SITE_INSP_ID = 4;
    private static final int PARAM_INS_ASSET_INSP_ID = 5;
    private static final int PARAM_INS_COMP_INSP_ID = 6;
    private static final int PARAM_INS_NAME = 7;
    private static final int PARAM_INS_SOURCE_ID = 8;
    private static final int PARAM_INS_STATUS = 9;
    private static final int PARAM_INS_TYPE = 10;
    private static final int PARAM_INS_ZOOMIFY_ID = 11;
    private static final int PARAM_INS_OBJ_JSON = 12;
    private static final int PARAM_INS_ID = 13;
    
    private static final int PARAM_UPD_SITE_ID = 0;
    private static final int PARAM_UPD_ORD_NUM = 1;
    private static final int PARAM_UPD_ASSET_ID = 2;
    private static final int PARAM_UPD_COMP_ID = 3;
    private static final int PARAM_UPD_SITE_INSP_ID = 4;
    private static final int PARAM_UPD_ASSET_INSP_ID = 5;
    private static final int PARAM_UPD_COMP_INSP_ID = 6;
    private static final int PARAM_UPD_NAME = 7;
    private static final int PARAM_UPD_SOURCE_ID = 8;
    private static final int PARAM_UPD_STATUS = 9;
    private static final int PARAM_UPD_TYPE = 10;
    private static final int PARAM_UPD_ZOOMIFY_ID = 11;
    private static final int PARAM_UPD_OBJ_JSON = 12;
    private static final int PARAM_UPD_ID = 13;

    @Override
    protected String statementsMapsPath() {
        return STATEMENTS_MAPS;
    }
    
    @Override
    public boolean insert(ResourceMetadata rmeta) throws DaoException {
        ensureExists(rmeta, "Resource is required");
        ensureExists(rmeta.getResourceId(), "Resource ID is required");
        ensureExists(rmeta.getSiteId(), "Site ID is required");
        
        ResourceMetadata r = retrieve(rmeta.getResourceId());
        if (r == null) {
            StatementBuilder stmt = new StatementBuilder(getStatementsMaps().getInsertStmt());
            stmt = stmt.setParameter(PARAM_INS_ASSET_ID, rmeta.getAssetId());
            stmt = stmt.setParameter(PARAM_INS_ASSET_INSP_ID, rmeta.getAssetInspectionId());
            stmt = stmt.setParameter(PARAM_INS_COMP_ID, rmeta.getComponentId());
            stmt = stmt.setParameter(PARAM_INS_COMP_INSP_ID, rmeta.getComponentInspectionId());
            stmt = stmt.setParameter(PARAM_INS_ID, rmeta.getResourceId());
            stmt = stmt.setParameter(PARAM_INS_NAME, rmeta.getName());
            stmt = stmt.setParameter(PARAM_INS_OBJ_JSON, serializeObject(rmeta));
            stmt = stmt.setParameter(PARAM_INS_ORD_NUM, rmeta.getOrderNumber());
            stmt = stmt.setParameter(PARAM_INS_SITE_ID, rmeta.getSiteId());
            stmt = stmt.setParameter(PARAM_INS_SITE_INSP_ID, rmeta.getSiteInspectionId());
            stmt = stmt.setParameter(PARAM_INS_SOURCE_ID, rmeta.getSourceResourceId());
            stmt = stmt.setParameter(PARAM_INS_STATUS, rmeta.getStatus());
            stmt = stmt.setParameter(PARAM_INS_TYPE, rmeta.getType());
            stmt = stmt.setParameter(PARAM_INS_ZOOMIFY_ID, rmeta.getZoomifyId());
            ResultSet rs = getSession().execute(stmt.build());
            return rs.wasApplied();
        } else {
            return false;
        }
    }

    @Override
    public boolean update(ResourceMetadata rmeta) throws DaoException {
        ensureExists(rmeta, "Resource is required");
        ensureExists(rmeta.getResourceId(), "Resource ID is required");
        ensureExists(rmeta.getSiteId(), "Site ID is required");
        
        ResourceMetadata r = retrieve(rmeta.getResourceId());
        if (r == null) {
            return false;
        } else {
            StatementBuilder stmt = new StatementBuilder(getStatementsMaps().getUpdateStmt());
            stmt = stmt.setParameter(PARAM_UPD_ASSET_ID, rmeta.getAssetId());
            stmt = stmt.setParameter(PARAM_UPD_ASSET_INSP_ID, rmeta.getAssetInspectionId());
            stmt = stmt.setParameter(PARAM_UPD_COMP_ID, rmeta.getComponentId());
            stmt = stmt.setParameter(PARAM_UPD_COMP_INSP_ID, rmeta.getComponentInspectionId());
            stmt = stmt.setParameter(PARAM_UPD_ID, rmeta.getResourceId());
            stmt = stmt.setParameter(PARAM_UPD_NAME, rmeta.getName());
            stmt = stmt.setParameter(PARAM_UPD_OBJ_JSON, serializeObject(rmeta));
            stmt = stmt.setParameter(PARAM_UPD_ORD_NUM, rmeta.getOrderNumber());
            stmt = stmt.setParameter(PARAM_UPD_SITE_ID, rmeta.getSiteId());
            stmt = stmt.setParameter(PARAM_UPD_SITE_INSP_ID, rmeta.getSiteInspectionId());
            stmt = stmt.setParameter(PARAM_UPD_SOURCE_ID, rmeta.getSourceResourceId());
            stmt = stmt.setParameter(PARAM_UPD_STATUS, rmeta.getStatus());
            stmt = stmt.setParameter(PARAM_UPD_TYPE, rmeta.getType());
            stmt = stmt.setParameter(PARAM_UPD_ZOOMIFY_ID, rmeta.getZoomifyId());
            ResultSet rs = getSession().execute(stmt.build());
            return rs.wasApplied();
        }
    }

    @Override
    public boolean delete(String id) throws DaoException {
        ensureExists(id, "Resource ID is required");
        
        ResourceMetadata r = retrieve(id);
        if (r == null) {
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
    public ResourceMetadata retrieve(String id) throws DaoException {
        ensureExists(id, "Resource ID is required");
        StatementBuilder stmt = new StatementBuilder()
                .withSqlTemplate(getStatementsMaps().getSelectTemplate())
                .addEquals(COL_ID, id);
        return CollectionsUtilities.firstItemIn(selectObjects(ResourceMetadata.class, stmt.build(), 0));
    }

    @Override
    public List<ResourceMetadata> search(ResourceSearchParams params) throws DaoException {
        StatementBuilder stmt = new StatementBuilder()
                .withSqlTemplate(getStatementsMaps().getSelectTemplate())
                .addEqualsConditionally(COL_ASSET_ID, params.getAssetId())
                .addEqualsConditionally(COL_ASSET_INSP_ID, params.getAssetInspectionId())
                .addEqualsConditionally(COL_COMP_ID, params.getComponentId())
                .addEqualsConditionally(COL_COMP_INSP_ID, params.getComponentInspectionId())
                .addEqualsConditionally(COL_NAME, params.getName())
                .addEqualsConditionally(COL_ORD_NUM, params.getOrderNumber())
                .addEqualsConditionally(COL_SITE_ID, params.getSiteId())
                .addEqualsConditionally(COL_SITE_INSP_ID, params.getSiteInspectionId())
                .addEqualsConditionally(COL_SOURCE_ID, params.getSourceResourceId())
                .addEqualsConditionally(COL_STATUS, params.getStatus())
                .addEqualsConditionally(COL_TYPE, params.getType())
                .addEqualsConditionally(COL_ZOOMIFY_ID, params.getZoomifyId())
                ;
        if (stmt.hasWhereClause()) {
            return selectObjects(ResourceMetadata.class, stmt.build(), 0);
        } else {
            throw new DaoException("Search parameters are required.");
        }
    }

}
