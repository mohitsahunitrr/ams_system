package com.precisionhawk.ams.dao.elasticsearch2;

import com.precisionhawk.ams.bean.SiteInspectionSearchParams;
import com.precisionhawk.ams.dao.DaoException;
import com.precisionhawk.ams.dao.SiteInspectionDao;
import com.precisionhawk.ams.domain.SiteInspection;
import java.util.List;
import javax.inject.Named;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;

/**
 *
 * @author pchapman
 */
@Named
public abstract class SiteInspectionEsDao extends AbstractEsDao implements SiteInspectionDao {

    protected static final String COL_ORDER_NUM = "orderNumber";
    protected static final String COL_SITE_ID = "siteId";
    protected static final String COL_STATUS = "status";
    protected static final String COL_TYPE = "type";
    protected static final String DOCUMENT = "SiteInspection";
    protected static final String MAPPING = "com/precisionhawk/ams/dao/elasticsearch2/SiteInspection_Mapping.json";

    @Override
    protected String getDocumentType() {
        return DOCUMENT;
    }

    @Override
    protected String getMappingPath() {
        return MAPPING;
    }

    @Override
    public boolean insert(SiteInspection inspection) throws DaoException {
        ensureExists(inspection, "Site inspection required.");
        ensureExists(inspection.getId(), "Unique ID required for site inspection.");
        SiteInspection pi = retrieve(inspection.getId());
        if (pi == null) {
            super.indexObject(inspection.getId(), inspection);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean update(SiteInspection inspection) throws DaoException {
        ensureExists(inspection, "Site inspection required.");
        ensureExists(inspection.getId(), "Unique ID required for site inspection.");
        SiteInspection pi = retrieve(inspection.getId());
        if (pi == null) {
            return false;
        } else {
            super.indexObject(inspection.getId(), inspection);
            return true;
        }
    }

    @Override
    public boolean delete(String id) throws DaoException {
        ensureExists(id, "Unique ID required for site inspection.");
        super.deleteDocument(id);
        return true;
    }

    @Override
    public SiteInspection retrieve(String id) throws DaoException {
        ensureExists(id, "Unique ID required for site inspection.");
        return super.retrieveObject(id, SiteInspection.class);
    }

    @Override
    public List<SiteInspection> search(SiteInspectionSearchParams params) throws DaoException {
        ensureExists(params, "Search parameters are required.");
        if (!params.hasCriteria()) {
            throw new DaoException("Search parameters are required.");
        }
        BoolQueryBuilder query = addQueryMust(null, COL_ORDER_NUM, params.getOrderNumber());
        query = addQueryMust(query, COL_SITE_ID, params.getSiteId());
        if (params.getStatus() != null) {
            query = addQueryMust(query, COL_STATUS, params.getStatus().getValue());
        }
        query = addQueryMust(query, COL_TYPE, params.getType());
        TimeValue scrollLifeLimit = new TimeValue(getScrollLifespan());
        SearchRequestBuilder search =
                getClient().prepareSearch(getIndexName())
                        .setSearchType(SearchType.QUERY_AND_FETCH)
                        .setTypes(DOCUMENT)
                        .setQuery(query)
                        .setScroll(scrollLifeLimit)
                        .setSize(getScrollSize());

        SearchResponse response = search.execute().actionGet();
        return loadFromScrolledSearch(SiteInspection.class, response, scrollLifeLimit);
    }    
}
