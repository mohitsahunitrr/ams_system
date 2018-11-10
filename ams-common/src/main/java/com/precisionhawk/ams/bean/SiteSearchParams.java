package com.precisionhawk.ams.bean;

import io.swagger.oas.annotations.media.Schema;

/**
 * The wind turbine farms site.
 *
 * @author <a href="mail:pchapman@pcsw.us">Philip A. Chapman</a>
 */
@Schema(description="Search criteria for sites.  At least one field must have a non-null value.")
public class SiteSearchParams extends AbstractSearchParams {

    @Schema(description="Name of the site, should be unique.")
    private String name;
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    
    @Schema(description="Unique ID of the owning organization.")
    private String organizationId;
    public String getOrganizationId() {
        return organizationId;
    }
    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    @Override
    public boolean hasCriteria() {
        return testField(name) || testField(organizationId);
    }
}
