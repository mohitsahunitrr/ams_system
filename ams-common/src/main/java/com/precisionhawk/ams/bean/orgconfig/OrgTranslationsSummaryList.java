package com.precisionhawk.ams.bean.orgconfig;

import io.swagger.oas.annotations.media.Schema;
import java.util.LinkedList;
import java.util.List;

/*
{
    "orgId": "e968f9ce-86e3-4373-8792-fd495d992ddb",
    "translations": [
        {
            "languageCode": "en",
            "languageName": "English",
            "countryCode": "US",
            "countryName": "United States",
            "version": 1,
            "updated": "20180606"
        }
    ]
}
*/

/**
 *
 * @author pchapman
 */
@Schema(description="A summary of translations available for the organization.")
public class OrgTranslationsSummaryList {
    @Schema(description="Unique ID of the owning organization.")
    private String organizationId;
    public String getOrganizationId() {
        return organizationId;
    }
    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }
    
    @Schema(description="Translations available for the organization.")
    private List<OrgTranslationsSummary> translations = new LinkedList<>();
    public List<OrgTranslationsSummary> getTranslations() {
        return translations;
    }
    public void setTranslations(List<OrgTranslationsSummary> translations) {
        this.translations = translations;
    }
}
