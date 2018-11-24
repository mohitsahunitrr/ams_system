package com.precisionhawk.ams.webservices.impl;

import com.precisionhawk.ams.bean.SiteInspectionSearchParams;
import com.precisionhawk.ams.dao.DaoException;
import com.precisionhawk.ams.dao.SiteInspectionDao;
import com.precisionhawk.ams.domain.SiteInspection;
import com.precisionhawk.ams.webservices.AbstractWebService;
import com.precisionhawk.ams.webservices.SiteInspectionWebService;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.InternalServerErrorException;

/**
 *
 * @author pchapman
 */
@Named
public class SiteInspectionWebServiceImpl extends AbstractWebService implements SiteInspectionWebService {

    @Inject private SiteInspectionDao dao;

    @Override
    public SiteInspection create(String authToken, SiteInspection inspection) {
        ensureExists(inspection, "The site inspection is required.");
        try {
            if (dao.insert(inspection)) {
                return inspection;
            } else {
                throw new BadRequestException(String.format("The site inspection %s already exists.", inspection.getId()));
            }
        } catch (DaoException ex) {
            throw new InternalServerErrorException("Error persisting feeder.", ex);
        }
    }

    @Override
    public SiteInspection retrieve(String authToken, String id) {
        ensureExists(id, "Site inspection ID is required.");
        try {
            return dao.retrieve(id);
        } catch (DaoException ex) {
            throw new InternalServerErrorException(String.format("Error retrieving site inspection %s.", id), ex);
        }
    }

    @Override
    public List<SiteInspection> search(String authToken, SiteInspectionSearchParams searchParams) {
        ensureExists(searchParams, "Search parameters are required.");
        try {
            return dao.search(searchParams);
        } catch (DaoException ex) {
            throw new InternalServerErrorException("Error retrieving site inspections.", ex);
        }
    }

    @Override
    public void update(String authToken, SiteInspection inspection) {
        ensureExists(inspection, "The site inspection is required.");
        ensureExists(inspection.getId(), "Site ID is required.");
        try {
            if (!dao.update(inspection)) {
                throw new BadRequestException(String.format("The site inspection %s already exists.", inspection.getId()));
            }
        } catch (DaoException ex) {
            throw new InternalServerErrorException("Error persisting site inspection.", ex);
        }
    }
    
}
