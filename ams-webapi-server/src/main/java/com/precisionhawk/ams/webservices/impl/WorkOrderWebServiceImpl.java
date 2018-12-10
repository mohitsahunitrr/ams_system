package com.precisionhawk.ams.webservices.impl;

import com.precisionhawk.ams.bean.WorkOrderSearchParams;
import com.precisionhawk.ams.bean.security.ServicesSessionBean;
import com.precisionhawk.ams.dao.DaoException;
import com.precisionhawk.ams.dao.WorkOrderDao;
import com.precisionhawk.ams.domain.WorkOrder;
import com.precisionhawk.ams.webservices.WorkOrderWebService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotAuthorizedException;

/**
 *
 * @author pchapman
 */
@Named
public class WorkOrderWebServiceImpl extends AbstractWebService implements WorkOrderWebService {
    
    @Inject private WorkOrderDao dao;

    @Override
    public WorkOrder retrieveById(String authToken, String orderNumber) {
        ServicesSessionBean sess = super.lookupSessionBean(authToken);
        ensureExists(orderNumber, "Work order number is required.");
        try {
            return authorize(sess, validateFound(dao.retrieve(orderNumber)));
        } catch (DaoException ex) {
            throw new InternalServerErrorException(String.format("Error retrieving work order for order number %s.", orderNumber), ex);
        }
    }

    @Override
    public List<WorkOrder> search(String authToken, WorkOrderSearchParams searchBean) {
        ensureExists(searchBean, "Search parameters are required.");
        ServicesSessionBean sess = super.lookupSessionBean(authToken);
        authorize(sess, searchBean);
        if (searchBean.hasCriteria()) {
            try {
                return cleanseUnauthorizedWorkOrders(sess, dao.search(searchBean));
            } catch (DaoException ex) {
                throw new InternalServerErrorException("Error searching for work orders.");
            }
        } else {
            throw new BadRequestException("Search parameters are required.");
        }
    }

    @Override
    public WorkOrder create(String authToken, WorkOrder workOrder) {
        ServicesSessionBean sess = super.lookupSessionBean(authToken);
        ensureExists(workOrder, "Work order is required.");
        authorize(sess, workOrder);
        if (workOrder.getOrderNumber() == null) {
            workOrder.setOrderNumber(UUID.randomUUID().toString().split("-")[0]);
        }
        try {
            if (dao.insert(workOrder)) {
                return workOrder;
            } else {
                throw new BadRequestException(String.format("The work order %s already exists.", workOrder.getOrderNumber()));
            }
        } catch (DaoException ex) {
            throw new InternalServerErrorException("Error storing new work order", ex);
        }
    }

    @Override
    public void delete(String authToken, String orderNumber) {
        ServicesSessionBean sess = super.lookupSessionBean(authToken);
        ensureExists(orderNumber, "Work order number is required.");
        try {
            WorkOrder order = dao.retrieve(orderNumber);
            if (order != null) {
                authorize(sess, order);
                dao.delete(orderNumber);
            }
        } catch (DaoException ex) {
            throw new InternalServerErrorException(String.format("Error deleting work order for order number %s.", orderNumber), ex);
        }
    }

    @Override
    public void update(String authToken, WorkOrder workOrder) {
        ServicesSessionBean sess = super.lookupSessionBean(authToken);
        ensureExists(workOrder, "Work order is required.");
        ensureExists(workOrder.getOrderNumber(), "Work order number is required.");
        authorize(sess, workOrder);
        try {
            boolean updated = false;
            WorkOrder order = dao.retrieve(workOrder.getOrderNumber());
            if (order != null) {
                authorize(sess, order);
                updated = dao.update(workOrder);
            }
            if (!updated) {
                throw new BadRequestException(String.format("The work order %s does not already exist.", workOrder.getOrderNumber()));
            }
        } catch (DaoException ex) {
            throw new InternalServerErrorException("Error storing new work order", ex);
        }
    }
    
    private boolean testAuthorization(ServicesSessionBean sess, WorkOrder order, String ... groupKeys) {
        for (String siteId : order.getSiteIds()) {
            if (sess.getCredentials().checkAuthorization(null, null, siteId, false, groupKeys)) {
                return true;
            }
        }
        return false;
    }
    
    private WorkOrder authorize(ServicesSessionBean sess, WorkOrder order, String ... groupKeys) {
        if (!sess.isTokenValid()) {
            throw new NotAuthorizedException(sess.getReason());
        }
        if (testAuthorization(sess, order, groupKeys)) {
            return order;
        } else {
            throw new NotAuthorizedException(String.format("User not authorized for work order %s", order.getOrderNumber()));
        }
    }
    
    private List<WorkOrder> cleanseUnauthorizedWorkOrders(ServicesSessionBean sess, List<WorkOrder> input, String ... groupKeys) {
        List<WorkOrder> output = new ArrayList<>(input.size());
        for (WorkOrder order : input) {
            if (testAuthorization(sess, order, groupKeys)) {
                output.add(order);
            }
        }
        return output;
    }
}
