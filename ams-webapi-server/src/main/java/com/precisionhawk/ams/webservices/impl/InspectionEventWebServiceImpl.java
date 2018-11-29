/*
 * All rights reserved.
 */

package com.precisionhawk.ams.webservices.impl;

import com.precisionhawk.ams.bean.InspectionEventResourceSearchParams;
import com.precisionhawk.ams.bean.InspectionEventSearchParams;
import com.precisionhawk.ams.bean.security.ServicesSessionBean;
import com.precisionhawk.ams.dao.DaoException;
import com.precisionhawk.ams.dao.InspectionEventDao;
import com.precisionhawk.ams.dao.InspectionEventResourceDao;
import com.precisionhawk.ams.domain.InspectionEvent;
import com.precisionhawk.ams.domain.InspectionEventResource;
import com.precisionhawk.ams.webservices.InspectionEventWebService;
import java.util.List;
import java.util.UUID;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;

/**
 *
 * @author <a href="mailto:pchapman@pcsw.us">Philip A. Chapman</a>
 */
@Named
public class InspectionEventWebServiceImpl extends AbstractWebService implements InspectionEventWebService {
    
    @Inject
    private InspectionEventDao dao;
    @Inject
    private InspectionEventResourceDao ierdao;

    @Override
    public Long count(String accessToken, InspectionEventSearchParams searchParms) {
        ServicesSessionBean sess = lookupSessionBean(accessToken);
        if (searchParms == null || (!searchParms.hasCriteria())) {
            throw new BadRequestException("Search parameters are required.");
        }
        authorize(sess, searchParms);
        try {
            return dao.count(searchParms);
        } catch (DaoException e) {
            throw new InternalServerErrorException(String.format("Unable to a count of inspection events: %s", searchParms), e);
        }
    }

    @Override
    public InspectionEvent retrieve(String accessToken, String id) {
        ServicesSessionBean sess = lookupSessionBean(accessToken);
        ensureExists(id, "Inspection event ID is required.");
        try {
            InspectionEvent result = dao.retrieve(id);
            if (result == null) {
                throw new NotFoundException(String.format("Inspection event %s not found", id));
            }
            return authorize(sess, result);
        } catch (DaoException e) {
	    throw new InternalServerErrorException(String.format("Unable load inspection event %s ", id), e);
        }
    }

    @Override
    public List<InspectionEvent> search(String accessToken, InspectionEventSearchParams searchParams) {
        ServicesSessionBean sess = lookupSessionBean(accessToken);
        if (searchParams == null || (!searchParams.hasCriteria())) {
            throw new BadRequestException("Search parameters are required.");
        }
        authorize(sess, searchParams);
        try {
            return authorize(sess, dao.lookup(searchParams));
        } catch (DaoException e) {
	    throw new InternalServerErrorException(String.format("Unable to a search for inspection events: %s", searchParams), e);
        }
    }

    @Override
    public InspectionEvent create(String accessToken, InspectionEvent event) {
        ServicesSessionBean sess = lookupSessionBean(accessToken);
        ensureExists(event, "Inspection event is required.");
        authorize(sess, event);
        if (event.getId() == null) {
            event.setId(UUID.randomUUID().toString());
        }
        try {
            dao.insert(event);
            return event;
        } catch (DaoException e) {
            throw new InternalServerErrorException("Unable save the new inspection event", e);
        }
    }
    
    @Override
    public void delete(String accessToken, String id) {
        ServicesSessionBean sess = lookupSessionBean(accessToken);
        ensureExists(id, "Inspection event ID is required.");
        try {
            InspectionEvent ie = dao.retrieve(id);
            authorize(sess, ie);
            // Delete related resources
            InspectionEventResourceSearchParams bean = new InspectionEventResourceSearchParams();
            bean.setInspectionEventId(id);
            for (InspectionEventResource r : ierdao.lookup(bean)) {
                ierdao.delete(r.getId());
            }
            dao.delete(id);
        } catch (DaoException e) {
            throw new InternalServerErrorException(String.format("Unable delete the inspection event %s", id), e);
        }
    }

    @Override
    public void update(String accessToken, InspectionEvent event) {
        ServicesSessionBean sess = lookupSessionBean(accessToken);
        ensureExists(event, "Inspection event is required.");
        ensureExists(event.getId(), "Inspection event ID is required.");
        authorize(sess, event);
        try {
            if (!dao.update(event)) {
                throw new NotFoundException(String.format("Inspection event %s does not exist.", event.getId()));
            }
        } catch (DaoException e) {
            throw new InternalServerErrorException("Unable save the existing inspection event", e);
        }
    }

}
