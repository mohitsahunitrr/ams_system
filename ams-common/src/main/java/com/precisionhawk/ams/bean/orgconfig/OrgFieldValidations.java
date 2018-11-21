package com.precisionhawk.ams.bean.orgconfig;

import java.time.LocalDate;
import java.util.Map;

/**
 *
 * @author pchapman
 */
public class OrgFieldValidations {
    
    private String id;
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    
    private String orgId;
    public String getOrganizationId() {
        return orgId;
    }
    public void setOrganizationId(String orgId) {
        this.orgId = orgId;
    }

    private int version;
    public int getVersion() {
        return version;
    }
    public void setVersion(int version) {
        this.version = version;
    }

    private LocalDate updated;
    public LocalDate getUpdated() {
        return updated;
    }
    public void setUpdated(LocalDate updated) {
        this.updated = updated;
    }

    private Map<String, FieldValidation> fields;
    public Map<String, FieldValidation> getFields() {
        return fields;
    }
    public void setFields(Map<String, FieldValidation> fields) {
        this.fields = fields;
    }
}
