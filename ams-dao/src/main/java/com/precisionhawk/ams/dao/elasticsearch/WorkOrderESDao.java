/*
 * All rights reserved.
 */

package com.precisionhawk.ams.dao.elasticsearch;

import com.precisionhawk.ams.bean.WorkOrderSearchParams;
import com.precisionhawk.ams.dao.DaoException;
import com.precisionhawk.ams.dao.WorkOrderDao;
import com.precisionhawk.ams.domain.WorkOrder;
import com.precisionhawk.ams.domain.WorkOrderStatus;
import java.util.Collections;
import java.util.List;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

/**
 *
 * @author <a href="mailto:pchapman@pcsw.us">Philip A. Chapman</a>
 */
public abstract class WorkOrderESDao extends AbstractEsDao implements WorkOrderDao
{
    private static final String DOCUMENT_FIELD_SITE_ID = "siteId";
    private static final String DOCUMENT_FIELD_STATUS = "status";
    private static final String DOCUMENT_FIELD_TYPE = "type";
    private static final String DOCUMENT_TYPE_WORK_ORDER = "WorkOrder";

    @Override
    protected String getDocumentType() {
        return DOCUMENT_TYPE_WORK_ORDER;
    }
    
    @Override
    public WorkOrder retrieveById(String orderNumber) throws DaoException {
        return retrieveObject(orderNumber, WorkOrder.class);
    }
    
    private QueryBuilder prepareQuery(WorkOrderSearchParams searchBean) {
        if (searchBean == null) {
            return null;
        }
        
        BoolQueryBuilder query = null;
        if (searchBean.getSiteId() != null) {
            query = QueryBuilders.boolQuery().must(QueryBuilders.termQuery(DOCUMENT_FIELD_SITE_ID, searchBean.getSiteId()));
        }
        if (searchBean.getStatuses() != null && !(searchBean.getStatuses().isEmpty())) {
            BoolQueryBuilder bqb = QueryBuilders.boolQuery();
            for (WorkOrderStatus s : searchBean.getStatuses()) {
                bqb = bqb.should(QueryBuilders.termQuery(DOCUMENT_FIELD_STATUS, s.getValue()));
            }
            bqb.minimumNumberShouldMatch(1);
            if (query == null) {
                query = QueryBuilders.boolQuery().must(bqb);
            } else {
                query = query.must(bqb);
            }
        }
        if (searchBean.getType() != null) {
            QueryBuilder qb = QueryBuilders.termQuery(DOCUMENT_FIELD_TYPE, searchBean.getType().getValue());
            if (query == null) {
                query = QueryBuilders.boolQuery().must(qb);
            } else {
                query = query.must(qb);
            }
        }
        return query;
    }

    @Override
    public List<WorkOrder> search(WorkOrderSearchParams searchBean) throws DaoException {
        QueryBuilder query = prepareQuery(searchBean);
        if (query == null) {
            return Collections.emptyList();
        }
        
        LOGGER.debug("Executing query for work orders: {}", query);

        TimeValue scrollLifeLimit = new TimeValue(getScrollLifespan());
        SearchRequestBuilder search =
                getClient().prepareSearch(getIndexName())
                        .setSearchType(SearchType.QUERY_AND_FETCH)
                        .setTypes(DOCUMENT_TYPE_WORK_ORDER)
                        .setQuery(query)
                        .setScroll(scrollLifeLimit)
                        .setSize(getScrollSize());

        SearchResponse response = search.execute().actionGet();
        
        List<WorkOrder> workOrders = loadFromScrolledSearch(WorkOrder.class, response, scrollLifeLimit);
        
        return workOrders;
    }

    @Override
    public boolean insert(WorkOrder workOrder) throws DaoException
    {
        if (workOrder == null) {
            throw new IllegalArgumentException("Work order cannot be null.");
        } else if (workOrder.getOrderNumber() == null || workOrder.getOrderNumber().isEmpty()) {
            throw new IllegalArgumentException("Order number required.");
        }
        WorkOrder wo = retrieveById(workOrder.getOrderNumber());
        if (wo == null) {
            indexObject(DOCUMENT_TYPE_WORK_ORDER, workOrder);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void delete(String orderNumber) throws DaoException {
        super.deleteDocument(orderNumber);
    }

    @Override
    public boolean update(WorkOrder workOrder) throws DaoException
    {
        if (workOrder == null) {
            throw new IllegalArgumentException("Work order cannot be null.");
        } else if (workOrder.getOrderNumber() == null || workOrder.getOrderNumber().isEmpty()) {
            throw new IllegalArgumentException("Order number required.");
        }
        WorkOrder wo = retrieveById(workOrder.getOrderNumber());
        if (wo == null) {
            return false;
        } else {
            indexObject(DOCUMENT_TYPE_WORK_ORDER, workOrder);
            return true;
        }
    }
}
