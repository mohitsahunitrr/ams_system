package com.precisionhawk.ams.dao.elasticsearch;

import com.precisionhawk.ams.bean.SiteSearchParams;
import com.precisionhawk.ams.dao.DaoException;
import com.precisionhawk.ams.dao.SiteDao;
import com.precisionhawk.ams.dao.SiteProvider;
import com.precisionhawk.ams.domain.Site;
import java.util.List;
import javax.inject.Named;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import java.util.LinkedList;

/**
 *
 * @author Philip A. Chapman
 */
@Named
public abstract class SiteEsDao extends AbstractEsDao implements SiteDao, SiteProvider {
    
    private static final String COL_ID = "id";
    private static final String COL_NAME = "name";
    private static final String COL_ORG_ID = "organizationId";
    private static final String DOCUMENT = "Site";
    private static final String MAPPING = "com/precisionhawk/ams/dao/elasticsearch/Site_Mapping.json";

    @Override
    public String getMappingPath() {
        return MAPPING;
    }

    @Override
    protected String getDocumentType() {
        return DOCUMENT;
    }

    @Override
    public boolean insert(Site site) throws DaoException {
        ensureExists(site, "Site is required.");
        ensureExists(site.getId(), "Site ID is required.");
        Site ss = retrieve(site.getId());
        if (ss == null) {
            indexObject(site.getId(), site);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean update(Site site) throws DaoException {
        ensureExists(site, "Site is required.");
        ensureExists(site.getId(), "Site ID is required.");
        Site ss = retrieve(site.getId());
        if (ss == null) {
            return false;
        } else {
            indexObject(site.getId(), site);
            return true;
        }
    }

    @Override
    public boolean delete(String id) throws DaoException {
        ensureExists(id, "Feeder ID is required.");
        deleteDocument(id);
        return true;
    }

    @Override
    public Site retrieve(String id) throws DaoException {
        ensureExists(id, "Site ID is required.");
        return retrieveObject(id, Site.class);
    }

    @Override
    public List<Site> search(SiteSearchParams params) throws DaoException {
        if (params == null || !params.hasCriteria()) {
            throw new IllegalArgumentException("Search parameters are required.");
        }
        BoolQueryBuilder query = addQueryMust(null, COL_NAME, params.getName());
        query = addQueryMust(query, COL_ORG_ID, params.getOrganizationId());
        if (query == null) {
            throw new IllegalArgumentException("Search parameters are required.");
        }
        
        TimeValue scrollLifeLimit = new TimeValue(getScrollLifespan());
        SearchRequestBuilder search =
                getClient().prepareSearch(getIndexName())
                        .setSearchType(SearchType.QUERY_AND_FETCH)
                        .setTypes(DOCUMENT)
                        .setQuery(query)
                        .setScroll(scrollLifeLimit)
                        .setSize(getScrollSize());

        SearchResponse response = search.execute().actionGet();
        return loadFromScrolledSearch(Site.class, response, scrollLifeLimit);
    }

    @Override
    public List<Site> retrieveAll() throws DaoException {
        TimeValue scrollLifeLimit = new TimeValue(getScrollLifespan());
        SearchRequestBuilder search =
                getClient().prepareSearch(getIndexName())
                        .setSearchType(SearchType.QUERY_AND_FETCH)
                        .setTypes(DOCUMENT)
                        .setQuery(QueryBuilders.matchAllQuery())
                        .setScroll(scrollLifeLimit)
                        .setSize(getScrollSize());

        SearchResponse response = search.execute().actionGet();

        return loadFromScrolledSearch(Site.class, response, scrollLifeLimit);
    }

    @Override
    public List<Site> retrieve(SiteSearchParams params) throws DaoException {
        return search(params);
    }
    
    @Override
    public List<Site> retrieveAllSites() throws DaoException {
        return retrieveAll();
    }

    @Override
    public List<Site> retrieveByIDs(List<String> siteIDs) throws DaoException {
        List<Site> list = new LinkedList<>();
        for (String id : siteIDs) {
            list.add(retrieve(id));
        }
        return list;
    }
}
