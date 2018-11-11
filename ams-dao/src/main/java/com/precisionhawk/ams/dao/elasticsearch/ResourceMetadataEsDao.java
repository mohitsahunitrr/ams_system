package com.precisionhawk.ams.dao.elasticsearch;

import com.precisionhawk.ams.dao.DaoException;
import com.precisionhawk.ams.domain.ResourceMetadata;
import com.precisionhawk.ams.bean.ResourceSearchParams;
import com.precisionhawk.ams.dao.ResourceMetadataDao;
import java.util.List;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;

/**
 * Implementation of ResourceMetadataDao which stores metadata in Elasticsearch.
 *
 * @author Philip A. Chapman
 */
public abstract class ResourceMetadataEsDao extends AbstractEsDao implements ResourceMetadataDao {

    private static final String COL_CONTENT_TYPE = "contentType";
    private static final String COL_NAME = "name";
    private static final String COL_ORG_ID = "organizationId";
    private static final String COL_ASSET_ID = "assetId";
    private static final String COL_ASSET_INSP_ID = "assetInspectionId";
    private static final String COL_ORDER_NUM = "orderNumber";
    private static final String COL_RESOURCE_ID = "resourceId";
    private static final String COL_SOURCE_RESOURCE_ID = "sourceResourceId";
    private static final String COL_STATUS = "status";
    private static final String COL_SITE_ID = "siteId";
    private static final String COL_SITE_INSP_ID = "siteInspectionId";
    private static final String COL_TYPE = "type";
    private static final String COL_ZOOMIFY_ID = "zoomifyId";
    private static final String DOCUMENT = "Resource";

    @Override
    protected String getDocumentType() {
        return DOCUMENT;
    }

    @Override
    public ResourceMetadata retrieveResourceMetadata(String resourceId) throws DaoException {
        if (resourceId == null || resourceId.isEmpty()) {
            throw new IllegalArgumentException("Resource ID is required.");
        }
        return retrieveObject(resourceId, ResourceMetadata.class);
    }

    @Override
    public List<ResourceMetadata> lookup(ResourceSearchParams params) throws DaoException {
        if (params == null) {
            throw new IllegalArgumentException("The search parameters are required.");
        }
        BoolQueryBuilder query = null;
        query = addQueryMust(query, COL_NAME, params.getName());
        query = addQueryMust(query, COL_ORDER_NUM, params.getOrderNumber());
        query = addQueryMust(query, COL_ASSET_ID, params.getAssetId());
        query = addQueryMust(query, COL_ASSET_INSP_ID, params.getAssetInspectionId());
        query = addQueryMust(query, COL_SOURCE_RESOURCE_ID, params.getSourceResourceId());
        query = addQueryMust(query, COL_STATUS, params.getStatus());
        query = addQueryMust(query, COL_SITE_ID, params.getSiteId());
        query = addQueryMust(query, COL_SITE_INSP_ID, params.getSiteInspectionId());
        query = addQueryMust(query, COL_TYPE, params.getType());
        query = addQueryMust(query, COL_ZOOMIFY_ID, params.getZoomifyId());
        
        TimeValue scrollLifeLimit = new TimeValue(getScrollLifespan());
        SearchRequestBuilder search =
                getClient().prepareSearch(getIndexName())
                        .setSearchType(SearchType.QUERY_AND_FETCH)
                        .setTypes(DOCUMENT)
                        .setQuery(query)
                        .setScroll(scrollLifeLimit)
                        .setSize(getScrollSize());

        SearchResponse response = search.execute().actionGet();
        return loadFromScrolledSearch(ResourceMetadata.class, response, scrollLifeLimit);
    }

    @Override
    public boolean insertMetadata(ResourceMetadata meta) throws DaoException {
        if (meta == null) {
            throw new IllegalArgumentException("The resource metadata is required.");
        } else if (meta.getResourceId() == null || meta.getResourceId().isEmpty()) {
            throw new IllegalArgumentException("The resource ID is required.");
        }
        ResourceMetadata existing = retrieveObject(meta.getResourceId(), ResourceMetadata.class);
        if (existing == null) {
            indexObject(meta.getResourceId(), meta);
            LOGGER.debug("Resource {} has been inserted.", meta.getResourceId());
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean deleteMetadata(String resourceId) throws DaoException {
        if (resourceId == null || resourceId.isEmpty()) {
            throw new IllegalArgumentException("The resource ID is required.");
        }
        deleteDocument(resourceId);
        return true;
    }

    @Override
    public boolean updateMetadata(ResourceMetadata meta) throws DaoException {
        if (meta == null) {
            throw new IllegalArgumentException("The resource metadata is required.");
        } else if (meta.getResourceId() == null || meta.getResourceId().isEmpty()) {
            throw new IllegalArgumentException("The resource ID is required.");
        }
        ResourceMetadata existing = retrieveObject(meta.getResourceId(), ResourceMetadata.class);
        if (existing == null) {
            return false;
        } else {
            indexObject(meta.getResourceId(), meta);
            LOGGER.debug("Resource {} has been updated.", meta.getResourceId());
            return true;
        }
    }
}
