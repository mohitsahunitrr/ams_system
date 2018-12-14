/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.precisionhawk.ams.bean.orgconfig;

import io.swagger.oas.annotations.media.Schema;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author pchapman
 */
@Schema(description="Unique ID of the translations definition")
public class FieldTranslation {
    
    @Schema(description="The ID of a field that this field's list values are depenent on.")
    private String dependent;
    public String getDependent() {
        return dependent;
    }
    public void setDependent(String dependent) {
        this.dependent = dependent;
    }
    
    @Schema(description="Field identifier.  Field identifier is usually generated from domainObject.member or domainObject.map[key].")
    private String key;
    public String getKey() {
        return key;
    }
    public void setKey(String key) {
        this.key = key;
    }

    @Schema(description="A label to be shown on any user interface for labeling the field's value.")
    private String label;
    public String getLabel() {
        return label;
    }
    public void setLabel(String label) {
        this.label = label;
    }

    @Schema(description="A long description of the field value which can be shown to the user.")
    private String description;
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    @Schema(description="Possible values to chose from, if the field has a limited set of valid values.")
    private List<ListValue> listValues = new LinkedList();
    public List<ListValue> getListValues() {
        return listValues;
    }
    public void setListValues(List<ListValue> listValues) {
        this.listValues = listValues;
    }
    
    public ListValue lookupListValue(String value) {
        for (ListValue v : listValues) {
            if (v.getValue().equals(value)) {
                return v;
            }
        }
        return null;
    }

    @Schema(description="A mapping of error text to be used for common errors keyed unique per error type.")
    private Map<String, String> errors = new HashMap();
    public Map<String, String> getErrors() {
        return errors;
    }
    public void setErrors(Map<String, String> errors) {
        this.errors = errors;
    }
    
    @Schema(description="A map of the fields used to populate columns in views keyed by view identifier mapped to a list of field identifiers.")
    private Map<String, List<String>> views = new HashMap<>();
    public Map<String, List<String>> getViews() {
        return views;
    }
    public void setViews(Map<String, List<String>> views) {
        this.views = views;
    }
}
