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
    boolean delete(String orderNumber) throws DaoException;
    
    WorkOrder retrieve(String orderNumber) throws DaoException;
     
    List<WorkOrder> search(WorkOrderSearchParams params) throws DaoException;
     
    boolean insert(WorkOrder workOrder) throws DaoException;
    
    boolean update(WorkOrder workOrder) throws DaoException;
}
