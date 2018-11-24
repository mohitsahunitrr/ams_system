package com.precisionhawk.ams.webservices.impl;

import com.precisionhawk.ams.bean.SiteSearchParams;
import com.precisionhawk.ams.dao.DaoException;
import com.precisionhawk.ams.dao.SiteDao;
import com.precisionhawk.ams.domain.Site;
import com.precisionhawk.ams.webservices.AbstractWebService;
import com.precisionhawk.ams.webservices.SiteWebService;
import java.util.List;
import java.util.UUID;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.InternalServerErrorException;

/**
 *
 * @author pchapman
 */
@Named
public class SiteWebServiceImpl extends AbstractWebService implements SiteWebService {
    
    @Inject private SiteDao dao;

    @Override
    public Site create(String authToken, Site site) {
        ensureExists(site, "The site is required.");
        if (site.getId() == null) {
            site.setId(UUID.randomUUID().toString());
        }
        try {
            if (dao.insert(site)) {
                return site;
            } else {
                throw new BadRequestException(String.format("The site %s already exists.", site.getId()));
            }
        } catch (DaoException ex) {
            throw new InternalServerErrorException("Error persisting site.", ex);
        }
    }

    @Override
    public Site retrieve(String authToken, String id) {
        ensureExists(id, "Site ID is required.");
        try {
            return dao.retrieve(id);
        } catch (DaoException ex) {
            throw new InternalServerErrorException("Error retrieving site.", ex);
        }
    }

    @Override
    public List<Site> retrieveAll(String authToken) {
        try {
            return dao.retrieveAll();
        } catch (DaoException ex) {
            throw new InternalServerErrorException("Error retrieving sites.", ex);
        }
    }

    @Override
    public List<Site> search(String authToken, SiteSearchParams searchParams) {
        ensureExists(searchParams, "Search parameters are required.");
        try {
            return dao.search(searchParams);
        } catch (DaoException ex) {
            throw new InternalServerErrorException("Unable to search for sites.", ex);
        }
    }

    @Override
    public void update(String authToken, Site site) {
        ensureExists(site, "The Site is required.");
        ensureExists(site.getId(), "Site ID is required.");
        try {
            if (!dao.update(site)) {
                throw new BadRequestException(String.format("The site %s already exists.", site.getId()));
            }
        } catch (DaoException ex) {
            throw new InternalServerErrorException("Error persisting site.", ex);
        }
    }
    
}
