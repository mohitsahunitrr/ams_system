package com.precisionhawk.ams.dao;

import com.precisionhawk.ams.bean.SiteInspectionSearchParams;
import com.precisionhawk.ams.domain.SiteInspection;
import java.util.List;

/**
 *
 * @author Philip A. Chapman
 */
public interface SiteInspectionDao {
        
    boolean insert(SiteInspection inspection) throws DaoException;
    
    boolean update(SiteInspection inspection) throws DaoException;

    boolean delete(String id) throws DaoException;
    
    SiteInspection retrieve(String id) throws DaoException;
    
    List<SiteInspection> search(SiteInspectionSearchParams params) throws DaoException;
}
