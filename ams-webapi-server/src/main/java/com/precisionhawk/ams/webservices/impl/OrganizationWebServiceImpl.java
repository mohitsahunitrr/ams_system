package com.precisionhawk.ams.webservices.impl;

import com.precisionhawk.ams.bean.orgconfig.OrgFieldTranslations;
import com.precisionhawk.ams.bean.orgconfig.OrgFieldValidations;
import com.precisionhawk.ams.bean.orgconfig.OrgTranslationsSummary;
import com.precisionhawk.ams.bean.orgconfig.OrgTranslationsSummaryList;
import com.precisionhawk.ams.bean.security.ServicesSessionBean;
import com.precisionhawk.ams.dao.DaoException;
import com.precisionhawk.ams.dao.SecurityDao;
import com.precisionhawk.ams.dao.TranslationsAndValidationsDao;
import com.precisionhawk.ams.domain.Organization;
import com.precisionhawk.ams.security.Constants;
import com.precisionhawk.ams.util.CollectionsUtilities;
import com.precisionhawk.ams.webservices.OrganizationWebService;
import java.util.List;
import java.util.Objects;
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
public class OrganizationWebServiceImpl extends AbstractWebService implements OrganizationWebService {
    
    @Inject private SecurityDao securityDao;
    @Inject private TranslationsAndValidationsDao tavDao;
    
    @Override
    public Organization retrievePrecisionHawkOrg() {
        return securityDao.selectOrganizationByKey(COMPANY_ORG_KEY);
    }

    @Override
    public List<Organization> retrieveOrgs() {
        return securityDao.selectOrganizations();
    }

    public Organization ensurePrecisionHawkOrg() {
        Organization org = securityDao.selectOrganizationByKey(COMPANY_ORG_KEY);
        if (org == null) {
            org = new Organization(UUID.randomUUID().toString(), COMPANY_ORG_KEY, "InspecTools LLC");
            try {
                securityDao.insertOrganization(org);
            } catch (DaoException ex) {
                throw new InternalServerErrorException("Error saving the inspectools organization.");
            }
        }
        return org;
    }

    @Override
    public Organization retrieveOrg(String orgId) {
        return validateFound(securityDao.selectOrganizationById(orgId));
    }
    
    //TODO: This needs to be stored somewhere.  Probably ElasticSearch in MetaData index.
    private static final String CONFIG_FILES_BASE_PATH = "com/windams/webservices/org_cfg/";
    private static final String ORG_FIELD_VALIDATIONS = CONFIG_FILES_BASE_PATH + "org_validations.json";

    @Override
    public OrgFieldTranslations retrieveOrgTranslations(
        String orgId, String lang, String country
    ) {
        try {
            return validateFound(CollectionsUtilities.firstItemIn(_retrieveOrgTranslations(orgId, lang, country)));
        } catch (DaoException ex) {
            throw new InternalServerErrorException("Error loading translations", ex);
        }
    }
    
    private List<OrgFieldTranslations> _retrieveOrgTranslations(
        String orgId, String lang, String country
    ) throws DaoException
    {
        List<OrgFieldTranslations> list = tavDao.loadOrgTranslations(orgId, lang, country);
        if (list.isEmpty()) {
            if ("en".equals(lang)) {
                if (!"US".equals(country)) {
                    return tavDao.loadOrgTranslations(orgId, lang, "US");
                }
            } else {
                // try returning en_US
                return tavDao.loadOrgTranslations(orgId, "en", "US");
            }
        }
        return list;
    }

    @Override
    public OrgTranslationsSummaryList retrieveOrgTranslationsSummary(String orgId)
    {
        OrgTranslationsSummaryList summary = new OrgTranslationsSummaryList();
        try {
            for (OrgFieldTranslations trans : tavDao.loadOrgTranslations(orgId, null, null)) {
                summary.getTranslations().add(new OrgTranslationsSummary(trans));
            }
            return summary;
        } catch (DaoException ex) {
            throw new InternalServerErrorException("Error loading translations", ex);
        }
    }

    @Override
    public OrgFieldValidations retrieveOrgFieldValidations(String orgId)
    {
        try {
            OrgFieldValidations validations = tavDao.loadOrgValidations(orgId);
            
            if (validations == null) {
                validations = tavDao.loadOrgValidations(retrievePrecisionHawkOrg().getId());
            }
        
            return validateFound(validations);
        } catch (DaoException ex) {
            throw new InternalServerErrorException("Error loading validations", ex);
        }
    }
    
    @Override
    public void postOrgTranslations(String authToken, String orgId, OrgFieldTranslations trans)
    {
        ServicesSessionBean sess = securityService.validateToken(authToken);
        if (orgId == null || orgId.isEmpty()) {
            throw new BadRequestException("Organization ID is required.");
        }
        if (!sess.getCredentials().checkAuthorization(null, orgId, null, true, Constants.GROUP_KEY_ADMIN)) {
            throw new NotAuthorizedException(String.format("The user is not authorized to upload translations for the organization %s", orgId));
        }
        if (!Objects.equals(orgId, trans.getOrganizationId())) {
            throw new BadRequestException("Organization ID mismatch.");
        }
        if (trans.getId() == null) {
            trans.setId(UUID.randomUUID().toString());
        }
        try {
            tavDao.storeOrgTranslations(trans);
        } catch (DaoException ex) {
            throw new InternalServerErrorException("Error storing translations", ex);
        }
    }

    @Override
    public void postOrgFieldValidations(
        String authToken, String orgId, OrgFieldValidations validations
    )
    {
        ServicesSessionBean sess = securityService.validateToken(authToken);
        if (orgId == null || orgId.isEmpty()) {
            throw new BadRequestException("Organization ID is required.");
        }
        if (!sess.getCredentials().checkAuthorization(null, orgId, null, true, Constants.GROUP_KEY_ADMIN)) {
            throw new NotAuthorizedException(String.format("The user is not authorized to upload validations for the organization %s", orgId));
        }
        if (!Objects.equals(orgId, validations.getOrganizationId())) {
            throw new BadRequestException("Organization ID mismatch.");
        }
        if (validations.getId() == null) {
            validations.setId(UUID.randomUUID().toString());
        }
        try {
            tavDao.storeOrgValidations(validations);
        } catch (DaoException ex) {
            throw new InternalServerErrorException("Error storing translations", ex);
        }
    }

    @Override
    public Organization createOrg(String authToken, Organization org) {
        ServicesSessionBean sess = securityService.validateToken(authToken);
        ensureExists(org, "Organization is required");
        if (!sess.getCredentials().checkAuthorization(null, null, null, true, Constants.GROUP_KEY_ADMIN)) {
            throw new NotAuthorizedException("The user is not authorized to create organizations");
        }
        if (org.getId() == null) {
            org.setId(UUID.randomUUID().toString());
        }
        try {
            securityDao.insertOrganization(org);
            return org;
        } catch (DaoException ex) {
            throw new InternalServerErrorException("Error storing organization", ex);
        }
    }
}
