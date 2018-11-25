package com.precisionhawk.ams.dao;

import com.precisionhawk.ams.bean.orgconfig.OrgFieldTranslations;
import com.precisionhawk.ams.bean.orgconfig.OrgFieldValidations;
import java.util.List;

/**
 *
 * @author pchapman
 */
public interface TranslationsAndValidationsDao {

    List<OrgFieldTranslations> loadOrgTranslations(String orgId, String lang, String country) throws DaoException;
    
    void storeOrgTranslations(OrgFieldTranslations translations) throws DaoException;

    OrgFieldValidations loadOrgValidations(String orgId) throws DaoException;
    
    void storeOrgValidations(OrgFieldValidations translations) throws DaoException;    
}
