/*
 * All rights reserved.
 */

package com.precisionhawk.ams.webservices.impl;

import com.precisionhawk.ams.bean.InspectionEventResourceSearchParams;
import com.precisionhawk.ams.bean.ResourcePolygons;
import com.precisionhawk.ams.bean.ResourceSearchParams;
import com.precisionhawk.ams.dao.DaoException;
import com.precisionhawk.ams.dao.InspectionEventResourceDao;
import com.precisionhawk.ams.dao.ResourceMetadataDao;
import com.precisionhawk.ams.domain.InspectionEventResource;
import com.precisionhawk.ams.domain.ResourceMetadata;
import com.precisionhawk.ams.webservices.InspectionEventResourceWebService;
import java.util.LinkedList;
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
public class InspectionEventResourceWebServiceImpl extends AbstractWebService implements InspectionEventResourceWebService {
    
    @Inject
    private InspectionEventResourceDao dao;
    
    @Inject
    private ResourceMetadataDao resourceDao;

    @Override
    public InspectionEventResource retrieve(String accessToken, String id) {
        ensureExists(id, "Inspection event resource ID is required");
        try {
            InspectionEventResource result = dao.retrieve(id);
            if (result == null) {
                throw new NotFoundException(String.format("Inspection event resource %s not found", id));
            }
            return result;
        } catch (DaoException e) {
            throw new InternalServerErrorException(String.format("Error loading inspection event resource {}", id), e);
        }
    }

    @Override
    public List<InspectionEventResource> search(String accessToken, InspectionEventResourceSearchParams searchParams) {
        ensureExists(searchParams, "Search parameters are required");
        if (!searchParams.hasCriteria()) {
            throw new BadRequestException("Search parameters are required");
        }
        try {
            return dao.lookup(searchParams);
        } catch (DaoException e) {
            throw new InternalServerErrorException(String.format("Error searching for inspection event resources for event {} and/or resource {}", searchParams.getInspectionEventId(), searchParams.getResourceId()), e);
        }
    }

    @Override
    public InspectionEventResource create(String accessToken, InspectionEventResource ier) {
        ensureExists(ier, "Inspection event resource is required.");
        if (ier.getId() == null) {
            ier.setId(UUID.randomUUID().toString());
        }
        try {
            if (!dao.insert(ier)) {
                throw new BadRequestException(String.format("The inspection event resource %s already exists.", ier.getId()));
            } else {
                return ier;
            }
        } catch (DaoException e) {
            throw new InternalServerErrorException("Unable save the new inspection event resource", e);
        }
    }
    
    @Override
    public void delete(String accessToken, String id) {
        ensureExists(id, "Inspection event resource ID is required.");
        try {
            InspectionEventResource ier = dao.retrieve(id);
            if (ier != null) {
                dao.delete(id);
            }
        } catch (DaoException e) {
            throw new InternalServerErrorException(String.format("Unable delete the inspection event resource {}", id), e);
        }
    }

    @Override
    public void update(String accessToken, InspectionEventResource event) {
        ensureExists(event, "Inspection event resource");
        ensureExists(event.getId(), "Inspection event resource");
        try {
            if (!dao.update(event)) {
                throw new NotFoundException(String.format("The inspection event resource %s does not already exist.", event.getId()));
            }
        } catch (DaoException e) {
            throw new InternalServerErrorException("Unable save the existing inspection event resource", e);
        }
    }

    @Override
    public List<ResourcePolygons> searchResourcePolygons(String accessToken, ResourceSearchParams searchParams) {
        ensureExists(searchParams, "Search parameters are required.");
        if (!searchParams.hasCriteria()) {
            throw new BadRequestException("Search parameters are required.");
        }
        List<ResourcePolygons> polysList = new LinkedList<ResourcePolygons>();
        ResourcePolygons polys;
        InspectionEventResourceSearchParams search2 = new InspectionEventResourceSearchParams();
        try {
            for (ResourceMetadata r : resourceDao.lookup(searchParams)) {
                search2.setResourceId(r.getResourceId());
                polys = new ResourcePolygons();
                polys.setResource(r);
                polys.setInspectionEventResources(dao.lookup(search2));
                polysList.add(polys);
            }
        } catch (DaoException e) {
            throw new InternalServerErrorException(String.format("Unable look up resources or inspection event resources for the search parameters {}", searchParams), e);
        }
        
        return polysList;
    }
}
