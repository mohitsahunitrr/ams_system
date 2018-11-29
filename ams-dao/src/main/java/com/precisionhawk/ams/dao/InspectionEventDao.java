/*
 * All rights reserved.
 */

package com.precisionhawk.ams.dao;

import com.precisionhawk.ams.bean.InspectionEventSearchParams;
import com.precisionhawk.ams.domain.InspectionEvent;
import java.util.List;

/**
 *
 * @author <a href="mailto:pchapman@pcsw.us">Philip A. Chapman</a>
 */
public interface InspectionEventDao {
    
    void delete(String id) throws DaoException;

    InspectionEvent retrieve(String id) throws DaoException;
    
    Long count(InspectionEventSearchParams searchBean) throws DaoException;
    
    List<InspectionEvent> lookup(InspectionEventSearchParams searchBean) throws DaoException;
    
    boolean insert(InspectionEvent report) throws DaoException;
    
    boolean update(InspectionEvent report) throws DaoException;

}
