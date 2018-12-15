package com.precisionhawk.ams.dao.elasticsearch2;

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

    protected static final String COL_CONTENT_TYPE = "contentType";
    protected static final String COL_NAME = "name";
    protected static final String COL_ORG_ID = "organizationId";
    protected static final String COL_ASSET_ID = "assetId";
    protected static final String COL_ASSET_INSP_ID = "assetInspectionId";
    protected static final String COL_ORDER_NUM = "orderNumber";
    protected static final String COL_RESOURCE_ID = "resourceId";
    protected static final String COL_SOURCE_RESOURCE_ID = "sourceResourceId";
    protected static final String COL_STATUS = "status";
    protected static final String COL_SITE_ID = "siteId";
    protected static final String COL_SITE_INSP_ID = "siteInspectionId";
    protected static final String COL_TYPE = "type";
    protected static final String COL_ZOOMIFY_ID = "zoomifyId";
    protected static final String DOCUMENT = "Resource";

    private static final String MAPPING = "com/precisionhawk/ams/dao/elasticsearch2/Resource_Mapping.json";

    //TODO: Could this be done a differenet way?
    @Override
    public String getMappingPath() {
        return MAPPING;
    }

    @Override
    protected String getDocumentType() {
        return DOCUMENT;
    }

    @Override
    public ResourceMetadata retrieve(String resourceId) throws DaoException {
        ensureExists(resourceId, "Resource ID is required.");
        return retrieveObject(resourceId, ResourceMetadata.class);
    }

    @Override
    public List<ResourceMetadata> search(ResourceSearchParams params) throws DaoException {
        if (params == null || (!params.hasCriteria())) {
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
    public boolean insert(ResourceMetadata meta) throws DaoException {
        ensureExists(meta, "The resource metadata is required.");
        ensureExists(meta.getResourceId(), "The resource ID is required.");
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
    public boolean delete(String resourceId) throws DaoException {
        ensureExists(resourceId, "The resource ID is required.");
        deleteDocument(resourceId);
        return true;
    }

    @Override
    public boolean update(ResourceMetadata meta) throws DaoException {
        ensureExists(meta, "The resource metadata is required.");
        ensureExists(meta, "The resource ID is required.");
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
