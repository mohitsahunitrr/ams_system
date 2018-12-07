package com.precisionhawk.ams.webservices.impl;

import com.precisionhawk.ams.bean.SiteSearchParams;
import com.precisionhawk.ams.bean.security.ServicesSessionBean;
import com.precisionhawk.ams.dao.DaoException;
import com.precisionhawk.ams.dao.SiteDao;
import com.precisionhawk.ams.domain.Site;
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
        ServicesSessionBean sess = lookupSessionBean(authToken);
        if (site.getId() == null) {
            site.setId(UUID.randomUUID().toString());
        }
        try {
            if (dao.insert(site)) {
                securityService.addSiteToCredentials(sess, site);
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
        ServicesSessionBean sess = lookupSessionBean(authToken);
        ensureExists(id, "Site ID is required.");
        try {
            return SiteWebServiceUtilities.authorizeSite(sess, validateFound(dao.retrieve(id)));
        } catch (DaoException ex) {
            throw new InternalServerErrorException("Error retrieving site.", ex);
        }
    }

    @Override
    public List<Site> retrieveAll(String authToken) {
        ServicesSessionBean sess = lookupSessionBean(authToken);
        try {
            return SiteWebServiceUtilities.cleanseUnAuthorizedSites(sess, dao.retrieveAll());
        } catch (DaoException ex) {
            throw new InternalServerErrorException("Error retrieving sites.", ex);
        }
    }

    @Override
    public List<Site> search(String authToken, SiteSearchParams searchParams) {
        ServicesSessionBean sess = lookupSessionBean(authToken);
        ensureExists(searchParams, "Search parameters are required.");
        try {
            return SiteWebServiceUtilities.cleanseUnAuthorizedSites(sess, dao.search(searchParams));
        } catch (DaoException ex) {
            throw new InternalServerErrorException("Unable to search for sites.", ex);
        }
    }

    @Override
    public void update(String authToken, Site site) {
        ServicesSessionBean sess = lookupSessionBean(authToken);
        ensureExists(site, "The Site is required.");
        ensureExists(site.getId(), "Site ID is required.");
        try {
            SiteWebServiceUtilities.authorizeSite(sess, site);
            Site s = dao.retrieve(site.getId());
            boolean updated = false;
            if (s != null) {
                SiteWebServiceUtilities.authorizeSite(sess, s);
                updated = dao.update(site);
            }
            if (!updated) {
                throw new BadRequestException(String.format("The site %s does not already exist.", site.getId()));
            }
        } catch (DaoException ex) {
            throw new InternalServerErrorException("Error persisting site.", ex);
        }
    }
    
}
