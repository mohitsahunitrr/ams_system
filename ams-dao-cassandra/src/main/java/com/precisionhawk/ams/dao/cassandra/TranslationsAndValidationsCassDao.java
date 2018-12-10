package com.precisionhawk.ams.dao.cassandra;

import com.datastax.driver.core.ResultSet;
import com.precisionhawk.ams.bean.orgconfig.OrgFieldTranslations;
import com.precisionhawk.ams.bean.orgconfig.OrgFieldValidations;
import com.precisionhawk.ams.dao.DaoException;
import com.precisionhawk.ams.dao.TranslationsAndValidationsDao;
import com.precisionhawk.ams.util.CollectionsUtilities;
import java.util.List;

/**
 *
 * @author pchapman
 */
public class TranslationsAndValidationsCassDao extends AbstractCassandraDao implements TranslationsAndValidationsDao {
    protected static final String STATEMENTS_MAPS = "com/precisionhawk/ams/dao/cassandra/TranslationsAndValidations_Statements.yaml";
    
    private static final String T_STMT_INS = "insertTrans";
    private static final String T_STMT_SEL = "selectTrans";
    private static final String T_STMT_UPD = "updateTrans";

    private static final String T_COL_ID = "id";
    private static final String T_COL_ORG_ID = "org_id";
    private static final String T_COL_LANG_CODE = "lang_code";
    private static final String T_COL_CTRY_CODE = "country_code";
    
    private static final int T_PARM_INS_UPD_ORG_ID = 0;
    private static final int T_PARM_INS_UPD_LANG_CODE = 1;
    private static final int T_PARM_INS_UPD_CTRY_CODE = 2;
    private static final int T_PARM_INS_UPD_OBJ_JSON = 3;
    private static final int T_PARM_INS_UPD_ID = 4;
    
    private static final String V_STMT_INS = "insertVals";
    private static final String V_STMT_SEL = "selectVals";
    private static final String V_STMT_UPD = "updateVals";

    private static final String V_COL_ID = "id";
    private static final String V_COL_ORG_ID = "org_id";
    
    private static final int V_PARM_INS_UPD_ORG_ID = 0;
    private static final int V_PARM_INS_UPD_OBJ_JSON = 1;
    private static final int V_PARM_INS_UPD_ID = 2;
    
    @Override
    protected String statementsMapsPath() {
        return STATEMENTS_MAPS;
    }

    @Override
    public List<OrgFieldTranslations> loadOrgTranslations(String orgId, String lang, String country) throws DaoException {
        StatementBuilder stmt = new StatementBuilder()
                .withSqlTemplate(getStatementsMaps().getNamedTemplates().get(T_STMT_SEL))
                .addEqualsConditionally(T_COL_CTRY_CODE, country)
                .addEqualsConditionally(T_COL_LANG_CODE, lang)
                .addEqualsConditionally(T_COL_ORG_ID, orgId)
                ;
        if (!stmt.hasWhereClause()) {
            throw new DaoException("Parameters are required.");
        }
        return super.selectObjects(OrgFieldTranslations.class, stmt.build(), 0);
    }

    @Override
    public boolean storeOrgTranslations(OrgFieldTranslations translations) throws DaoException {
        ensureExists(translations, "The translations object is required");
        ensureExists(translations.getId(), "Translations ID is required");
        ensureExists(translations.getOrganizationId(), "Organization ID is required");
        
        String sql;
        OrgFieldTranslations t = retrieveTranslations(translations.getId());
        if (t == null) {
            // Attempt insert
            sql = getStatementsMaps().getNamedTemplates().get(T_STMT_INS);
        } else {
            // Attempt update
            sql = getStatementsMaps().getNamedTemplates().get(T_STMT_UPD);
        }
        StatementBuilder stmt = new StatementBuilder(sql);
        stmt = stmt.setParameter(T_PARM_INS_UPD_CTRY_CODE, translations.getCountryCode());
        stmt = stmt.setParameter(T_PARM_INS_UPD_ID, translations.getId());
        stmt = stmt.setParameter(T_PARM_INS_UPD_LANG_CODE, translations.getLanguageCode());
        stmt = stmt.setParameter(T_PARM_INS_UPD_OBJ_JSON, serializeObject(translations));
        stmt = stmt.setParameter(T_PARM_INS_UPD_ORG_ID, translations.getOrganizationId());
        ResultSet rs = getSession().execute(stmt.build());
        return rs.wasApplied();
    }
    
    private OrgFieldTranslations retrieveTranslations(String id) throws DaoException {
        StatementBuilder stmt = new StatementBuilder()
                .withSqlTemplate(getStatementsMaps().getNamedTemplates().get(T_STMT_SEL))
                .addEquals(T_COL_ID, id)
                ;
        return CollectionsUtilities.firstItemIn(super.selectObjects(OrgFieldTranslations.class, stmt.build(), 0));
    }

    @Override
    public OrgFieldValidations loadOrgValidations(String orgId) throws DaoException {
        StatementBuilder stmt = new StatementBuilder()
                .withSqlTemplate(getStatementsMaps().getNamedTemplates().get(V_STMT_SEL))
                .addEqualsConditionally(V_COL_ORG_ID, orgId)
                ;
        if (!stmt.hasWhereClause()) {
            throw new DaoException("Parameters are required.");
        }
        return CollectionsUtilities.firstItemIn(super.selectObjects(OrgFieldValidations.class, stmt.build(), 0));
    }

    @Override
    public boolean storeOrgValidations(OrgFieldValidations validations) throws DaoException {
        ensureExists(validations, "The translations object is required");
        ensureExists(validations.getId(), "Translations ID is required");
        ensureExists(validations.getOrganizationId(), "Organization ID is required");
        
        String sql;
        OrgFieldValidations t = retrieveValidations(validations.getId());
        if (t == null) {
            // Attempt insert
            sql = getStatementsMaps().getNamedTemplates().get(V_STMT_INS);
        } else {
            // Attempt update
            sql = getStatementsMaps().getNamedTemplates().get(V_STMT_UPD);
        }
        StatementBuilder stmt = new StatementBuilder(sql);
        stmt = stmt.setParameter(V_PARM_INS_UPD_ID, validations.getId());
        stmt = stmt.setParameter(V_PARM_INS_UPD_OBJ_JSON, serializeObject(validations));
        stmt = stmt.setParameter(V_PARM_INS_UPD_ORG_ID, validations.getOrganizationId());
        ResultSet rs = getSession().execute(stmt.build());
        return rs.wasApplied();
    }
    
    private OrgFieldValidations retrieveValidations(String id) throws DaoException {
        StatementBuilder stmt = new StatementBuilder()
                .withSqlTemplate(getStatementsMaps().getNamedTemplates().get(V_STMT_SEL))
                .addEquals(V_COL_ID, id)
                ;
        return CollectionsUtilities.firstItemIn(super.selectObjects(OrgFieldValidations.class, stmt.build(), 0));
    }
}
