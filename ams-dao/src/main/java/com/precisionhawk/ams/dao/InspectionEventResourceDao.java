/*
 * All rights reserved.
 */

package com.precisionhawk.ams.dao;

import com.precisionhawk.ams.bean.InspectionEventResourceSearchParams;
import com.precisionhawk.ams.domain.InspectionEventResource;
import java.util.List;

/**
 *
 * @author <a href="mailto:pchapman@pcsw.us">Philip A. Chapman</a>
 */
public interface InspectionEventResourceDao {
    
    void delete(String id) throws DaoException;

    InspectionEventResource retrieve(String id) throws DaoException;
    
    List<InspectionEventResource> lookup(InspectionEventResourceSearchParams queryParams) throws DaoException;
    
    boolean insert(InspectionEventResource ieResource) throws DaoException;

    boolean update(InspectionEventResource ieResource) throws DaoException;

}
