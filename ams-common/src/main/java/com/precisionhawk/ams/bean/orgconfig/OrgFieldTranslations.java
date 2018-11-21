/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.precisionhawk.ams.bean.orgconfig;

import io.swagger.oas.annotations.media.Schema;
import java.net.URL;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author pchapman
 */
@Schema(description="A set of translations and other localized settings for an organization based on langauge and country code.")
public class OrgFieldTranslations {
    
    @Schema(description="Unique ID of the translations definition")
    private String id;
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    @Schema(description="Unique ID of the owning organization.")
    private String organizationId;
    public String getOrganizationId() {
        return organizationId;
    }
    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    @Schema(description="2-character language code.")
    private String languageCode;
    public String getLanguageCode() {
        return languageCode;
    }
    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }

    @Schema(description="Language name.")
    private String languageName;
    public String getLanguageName() {
        return languageName;
    }
    public void setLanguageName(String languageName) {
        this.languageName = languageName;
    }

    @Schema(description="2-character country code.")
    private String countryCode;
    public String getCountryCode() {
        return countryCode;
    }
    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    @Schema(description="Country name.")
    private String countryName;
    public String getCountryName() {
        return countryName;
    }
    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    @Schema(description="The version of the translation schema.")
    private int version;
    public int getVersion() {
        return version;
    }
    public void setVersion(int version) {
        this.version = version;
    }

    @Schema(description="Date translations data was last updated.")
    private LocalDate updated;
    public LocalDate getUpdated() {
        return updated;
    }
    public void setUpdated(LocalDate updated) {
        this.updated = updated;
    }

    @Schema(description="The name of the organization.")
    private String name;
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    @Schema(description="A mapping of URLs by key from which various forms of the organization's logo can be downloaded.  The unique variants may be different sizes or have different opacity, for example.")
    private Map<String, URL> logoURLs = new HashMap();
    public Map<String, URL> getLogoURLs() {
        return logoURLs;
    }
    public void setLogoURLs(Map<String, URL> logoURLs) {
        this.logoURLs = logoURLs;
    }

    @Schema(description="A mapping of error text to be used for common errors keyed unique per error type.")
    private Map<String, String> errors = new HashMap();
    public Map<String, String> getErrors() {
        return errors;
    }
    public void setErrors(Map<String, String> errors) {
        this.errors = errors;
    }

    @Schema(description="A mapping of field translations keyed by field identifier.  Field identifier is usually generated from domainObject.member or domainObject.map[key].")
    private Map<String, FieldTranslation> fields = new HashMap();
    public Map<String, FieldTranslation> getFields() {
        return fields;
    }
    public void setFields(Map<String, FieldTranslation> fields) {
        this.fields = fields;
    }
    
    public String translationFromValue(String key, Object value) {
        if (value == null) {
            return "";
        } else {
            if (value instanceof Enum) {
                value = ((Enum)value).name();
            } else {
                value = value.toString();
            }
            FieldTranslation ftrans = fields.get(key);
            if (ftrans == null) {
                return null;
            } else {
                if (ftrans.getListValues() == null || ftrans.getListValues().isEmpty()) {
                    return null;
                } else {
                    for (ListValue lv : ftrans.getListValues()) {
                        if (lv.getValue().equals(value)) {
                            return lv.getLabel();
                        }
                    }
                }
            }
        }
        return null;
    }
}
