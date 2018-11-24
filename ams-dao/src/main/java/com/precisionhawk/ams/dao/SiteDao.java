package com.precisionhawk.ams.dao;

import com.precisionhawk.ams.bean.SiteSearchParams;
import com.precisionhawk.ams.domain.Site;
import java.util.List;

/**
 *
 * @author Philip A. Chapman
 */
public interface SiteDao {
        
    boolean insert(Site site) throws DaoException;
    
    boolean update(Site site) throws DaoException;

    boolean delete(String id) throws DaoException;
    
    Site retrieve(String id) throws DaoException;
    
    List<Site> search(SiteSearchParams params) throws DaoException;

    List<Site> retrieveAll() throws DaoException;
}
