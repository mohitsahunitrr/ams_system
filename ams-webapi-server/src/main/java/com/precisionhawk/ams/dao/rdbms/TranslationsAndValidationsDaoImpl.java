package com.precisionhawk.ams.dao.rdbms;

import com.precisionhawk.ams.bean.orgconfig.OrgFieldTranslations;
import com.precisionhawk.ams.bean.orgconfig.OrgFieldValidations;
import com.precisionhawk.ams.dao.DaoException;
import com.precisionhawk.ams.dao.TranslationsAndValidationsDao;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author pchapman
 */
@Component
public class TranslationsAndValidationsDaoImpl extends AbstractSQLDao implements TranslationsAndValidationsDao {

    @Autowired
    protected ObjectMapper mapper;

    private static final String TRANS_TABLE = "wams_field_translations";
    private static final String SQL_INSERT_TRANS =
            "INSERT INTO " + TRANS_TABLE +
            " (org_id, lang_code, country_code, json, id)" +
            " VALUES (?, ?, ?, ?, ?)";
    private static final String SQL_UPDATE_TRANS = "UPDATE " + TRANS_TABLE + " SET json = ? WHERE id = ?";
    private static final String SQL_SELECT_TRANS = "SELECT json FROM " + TRANS_TABLE + " WHERE org_id = ?";
    
    
    private static final String VAL_TABLE = "wams_field_validations";
    private static final String SQL_INSERT_VAL =
            "INSERT INTO " + VAL_TABLE +
            " (org_id, json, id)" +
            " VALUES (?, ?, ?)";
    private static final String SQL_UPDATE_VAL = "UPDATE " + VAL_TABLE + " SET json = ? WHERE id = ?";
    private static final String SQL_SELECT_VAL = "SELECT json FROM " + VAL_TABLE + " WHERE org_id = ?";

    @Override
    public List<OrgFieldTranslations> loadOrgTranslations(String orgId, String lang, String country) throws DaoException {
        String sql = SQL_SELECT_TRANS;
        if (orgId == null || orgId.isEmpty()) {
            return Collections.emptyList();
        }
        boolean qlang = false;
        if (lang != null && !lang.isEmpty()) {
            sql += " AND lang_code = ?";
            qlang = true;
        }
        boolean qcountry = false;
        if (country != null && !country.isEmpty()) {
            sql += " AND country_code = ?";
            qcountry = true;
        }
        Connection conn = null;
        ResultSet rst = null;
        PreparedStatement stmt = null;
        int index = 1;
        try {
            conn = dataSource.getConnection();
            beginTrans(conn);
            stmt = conn.prepareStatement(sql);
            stmt.setString(index++, orgId);
            if (qlang) {
                stmt.setString(index++, lang);
            }
            if (qcountry) {
                stmt.setString(index, country);
            }
            rst = stmt.executeQuery();
            List<OrgFieldTranslations> list = new LinkedList<>();
            String json;
            while (rst.next()) {
                json = rst.getString(1);
                try {
                    list.add(mapper.readValue(json, OrgFieldTranslations.class));
                } catch (IOException ex) {
                    throw new DaoException("Unable to load object", ex);
                }
            }
            commitTrans(conn);
            return list;
        } catch (SQLException ex) {
            LOGGER.error("Unable to insert process queue entries", ex);
            rollbackTrans(conn);
            throw new DaoException("Error inserting entries", ex);
        } finally {
            closeQuietly(rst);
            closeQuietly(stmt);
            resetConnection(conn);
            closeQuietly(conn);
        }
    }
    
    @Override
    public void storeOrgTranslations(OrgFieldTranslations trans) throws DaoException {
        if (trans.getOrganizationId() == null || trans.getOrganizationId().isEmpty() || trans.getLanguageCode() == null || trans.getLanguageCode().isEmpty()) {
            throw new DaoException("Organization ID and language code are required.");
        }
        if (trans.getId() == null) {
            trans.setId(UUID.randomUUID().toString());
        }
        String json;
        try {
            json = mapper.writeValueAsString(trans);
        } catch (IOException ex) {
            throw new DaoException("Unable to serialize data.", ex);
        }
        // Attempt an insert.
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = dataSource.getConnection();
            beginTrans(conn);
            // Attempt an update
            closeQuietly(stmt);
            stmt = conn.prepareStatement(SQL_UPDATE_TRANS);
            stmt.setString(1, json);
            stmt.setString(2, trans.getId());
            int count = stmt.executeUpdate();
            if (count == 0) {
                // Attempt an insert
                stmt = conn.prepareStatement(SQL_INSERT_TRANS);
                stmt.setString(1, trans.getOrganizationId());
                stmt.setString(2, trans.getLanguageCode());
                if (trans.getCountryCode() == null) {
                    stmt.setNull(3, Types.CHAR);
                } else {
                    stmt.setString(3, trans.getCountryCode());
                }
                stmt.setString(4, json);
                stmt.setString(5, trans.getId());
                count = stmt.executeUpdate();
                if (count != 1) {
                    throw new DaoException("Error inserting entries");
                }
            }
            commitTrans(conn);
        } catch (SQLException ex) {
            LOGGER.error("Unable to insert process queue entries", ex);
            rollbackTrans(conn);
            throw new DaoException("Error inserting entries", ex);
        } finally {
            closeQuietly(stmt);
            resetConnection(conn);
            closeQuietly(conn);
        }
    }

    @Override
    public OrgFieldValidations loadOrgValidations(String orgId) throws DaoException {
        String sql = SQL_SELECT_VAL;
        if (orgId == null || orgId.isEmpty()) {
            return null;
        }
        Connection conn = null;
        ResultSet rst = null;
        PreparedStatement stmt = null;
        try {
            conn = dataSource.getConnection();
            beginTrans(conn);
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, orgId);
            rst = stmt.executeQuery();
            List<OrgFieldTranslations> list = new LinkedList<>();
            String json;
            if (rst.next()) {
                try {
                    json = rst.getString(1);
                    return mapper.readValue(json, OrgFieldValidations.class);
                } catch (IOException ex) {
                    throw new DaoException("Unable to load object", ex);
                } finally {
                    commitTrans(conn);            
                }
            }
        } catch (SQLException ex) {
            LOGGER.error("Unable to insert process queue entries", ex);
            rollbackTrans(conn);
            throw new DaoException("Error inserting entries", ex);
        } finally {
            closeQuietly(rst);
            closeQuietly(stmt);
            resetConnection(conn);
            closeQuietly(conn);
        }
        return null;
    }

    @Override
    public void storeOrgValidations(OrgFieldValidations val) throws DaoException {
        if (val.getOrganizationId() == null || val.getOrganizationId().isEmpty()) {
            throw new DaoException("Organization ID is required.");
        }
        if (val.getId() == null) {
            val.setId(UUID.randomUUID().toString());
        }
        String json;
        try {
            json = mapper.writeValueAsString(val);
        } catch (IOException ex) {
            throw new DaoException("Unable to serialize data.", ex);
        }
        // Attempt an insert.
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = dataSource.getConnection();
            beginTrans(conn);
            // Attempt an update
            closeQuietly(stmt);
            stmt = conn.prepareStatement(SQL_UPDATE_VAL);
            stmt.setString(1, json);
            stmt.setString(2, val.getId());
            int count = stmt.executeUpdate();
            
            if (count == 0) {
                // Attempt an insert
                stmt = conn.prepareStatement(SQL_INSERT_VAL);
                stmt.setString(1, val.getOrganizationId());
                stmt.setString(2, json);
                stmt.setString(3, val.getId());
                count = stmt.executeUpdate();
                if (count != 1) {
                    throw new DaoException("Error inserting entries");
                }
            }
            
            commitTrans(conn);
        } catch (SQLException ex) {
            LOGGER.error("Unable to insert process queue entries", ex);
            rollbackTrans(conn);
            throw new DaoException("Error inserting entries", ex);
        } finally {
            closeQuietly(stmt);
            resetConnection(conn);
            closeQuietly(conn);
        }
    }
}
