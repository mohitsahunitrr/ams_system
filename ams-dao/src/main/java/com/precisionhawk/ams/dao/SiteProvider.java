package com.precisionhawk.ams.dao;

import com.precisionhawk.ams.bean.SiteSearchParams;
import com.precisionhawk.ams.domain.Site;
import java.util.List;

/**
 *
 * @author pchapman
 */
public interface SiteProvider {

    Site retrieve(String id) throws DaoException;
    
    List<Site> retrieveAllSites() throws DaoException;
    
    List<Site> retrieve(SiteSearchParams params) throws DaoException;

    List<Site> retrieveByIDs(List<String> siteIDs) throws DaoException;
    
}
