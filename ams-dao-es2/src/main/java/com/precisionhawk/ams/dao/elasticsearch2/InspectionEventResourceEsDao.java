/*
 * All rights reserved.
 */
package com.precisionhawk.ams.dao.elasticsearch2;

import com.precisionhawk.ams.bean.InspectionEventResourceSearchParams;
import com.precisionhawk.ams.dao.DaoException;
import com.precisionhawk.ams.dao.InspectionEventResourceDao;
import com.precisionhawk.ams.domain.InspectionEventResource;
import java.util.List;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;

/**
 *
 * @author <a href="mailto:pchapman@pcsw.us">Philip A. Chapman</a>
 */
public abstract class InspectionEventResourceEsDao extends AbstractEsDao implements InspectionEventResourceDao {
    
    protected static final String DOCUMENT_TYPE = "InspectionEventResource";
    protected static final String FIELD_ASSET_ID = "assetId";
    protected static final String FIELD_INSPECTION_EVENT_ID = "inspectionEventId";
    protected static final String FIELD_ORDER_NUMBER = "orderNumber";
    protected static final String FIELD_RESOURCE_ID = "resourceId";
    protected static final String FIELD_SITE_ID = "siteId";
    protected static final String MAPPING = "com/precisionhawk/ams/dao/elasticsearch2/InspectionEventResource_Mapping.json";

    @Override
    protected String getDocumentType() {
        return DOCUMENT_TYPE;
    }

    @Override
    protected String getMappingPath() {
        return MAPPING;
    }
    
    @Override
    public boolean delete(String id) throws DaoException {
        ensureExists(id, "Inspection event resource ID is required.");
        super.deleteDocument(id);
        return true;
    }

    @Override
    public InspectionEventResource retrieve(String id) throws DaoException {
        ensureExists(id, "Inspection event resource ID is required.");
        return super.retrieveObject(id, InspectionEventResource.class);
    }

    @Override
    public List<InspectionEventResource> search(InspectionEventResourceSearchParams searchBean) throws DaoException {
        ensureExists(searchBean, "Search parameters are required.");
        QueryBuilder queryBuilder = prepareSearchQuery(searchBean);
        ensureExists(queryBuilder, "Search parameters are required.");
        
        TimeValue scrollLifeLimit = new TimeValue(getScrollLifespan());

        SearchRequestBuilder search =
                getClient().prepareSearch(getIndexName())
                        .setTypes(DOCUMENT_TYPE)
                        .setSearchType(SearchType.QUERY_AND_FETCH)
                        .setQuery(queryBuilder)
                        .setScroll(scrollLifeLimit)
                        .setSize(getScrollSize());
        LOGGER.debug("Executing the following query: {}", search.toString());
        SearchResponse response = search.execute().actionGet();

        return loadFromScrolledSearch(InspectionEventResource.class, response, scrollLifeLimit);
    }

    @Override
    public boolean insert(InspectionEventResource ier) throws DaoException {
        ensureExists(ier, "Inspection event resource is required.");
        ensureExists(ier.getId(), "Inspection event resource ID is required.");
        InspectionEventResource e = retrieve(ier.getId());
        if (e == null) {
            indexObject(ier.getId(), ier);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean update(InspectionEventResource ier) throws DaoException {
        ensureExists(ier, "Inspection event resource is required.");
        ensureExists(ier.getId(), "Inspection event resource ID is required.");
        InspectionEventResource e = retrieve(ier.getId());
        if (e == null) {
            return false;
        } else {
            indexObject(ier.getId(), ier);
            return true;
        }
    }
    
    private QueryBuilder prepareSearchQuery(InspectionEventResourceSearchParams searchBean) {
        BoolQueryBuilder queryBuilder = addQueryMust(null, FIELD_ASSET_ID, searchBean.getAssetId());
        queryBuilder = addQueryMust(queryBuilder, FIELD_INSPECTION_EVENT_ID, searchBean.getInspectionEventId());
        queryBuilder = addQueryMust(queryBuilder, FIELD_ORDER_NUMBER, searchBean.getOrderNumber());
        queryBuilder = addQueryMust(queryBuilder, FIELD_RESOURCE_ID, searchBean.getResourceId());
        queryBuilder = addQueryMust(queryBuilder, FIELD_SITE_ID, searchBean.getSiteId());
        return queryBuilder;
    }
}
