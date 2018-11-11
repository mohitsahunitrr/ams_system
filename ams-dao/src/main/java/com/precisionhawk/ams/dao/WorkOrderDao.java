/*
 * All rights reserved.
 */

package com.precisionhawk.ams.dao;

import com.precisionhawk.ams.bean.WorkOrderSearchParams;
import com.precisionhawk.ams.domain.WorkOrder;
import java.util.List;

/**
 *
 * @author Philip A. Chapman
 */
public interface WorkOrderDao
{
    void delete(String orderNumber) throws DaoException;
    
    WorkOrder retrieveById(String orderNumber) throws DaoException;
     
    List<WorkOrder> search(WorkOrderSearchParams params) throws DaoException;
     
    void store(WorkOrder workOrder) throws DaoException;
}
