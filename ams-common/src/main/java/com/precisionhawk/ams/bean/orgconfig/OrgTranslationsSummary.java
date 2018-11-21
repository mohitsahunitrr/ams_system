package com.precisionhawk.ams.bean.orgconfig;

import io.swagger.oas.annotations.media.Schema;
import java.time.LocalDate;

/**
 *
 * @author pchapman
 */
@Schema(description="Information about translations for a language and country code for the organization.")
public class OrgTranslationsSummary {
    
    @Schema(description="Unique ID of the translations definition")
    private String id;
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
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

    @Schema(description="The version of the translation.")
    private int version;
    public int getVersion() {
        return version;
    }
    public void setVersion(int version) {
        this.version = version;
    }

    @Schema(description="Date last updated.")
    private LocalDate updated;
    public LocalDate getUpdated() {
        return updated;
    }
    public void setUpdated(LocalDate updated) {
        this.updated = updated;
    }
    
    public OrgTranslationsSummary() {}
    
    public OrgTranslationsSummary(OrgFieldTranslations trans) {
        this.countryCode = trans.getCountryCode();
        this.countryName = trans.getCountryName();
        this.languageCode = trans.getLanguageCode();
        this.languageName = trans.getLanguageName();
        this.updated = trans.getUpdated();
        this.version = trans.getVersion();
    }
}
