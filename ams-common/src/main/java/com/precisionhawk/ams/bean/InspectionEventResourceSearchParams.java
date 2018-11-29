package com.precisionhawk.ams.bean;

import io.swagger.oas.annotations.media.Schema;

/**
 *
 * @author pchapman
 */
@Schema(description="A bean containing search criteria for inspection event resources.  At least one field must have a non-null value.")
public class InspectionEventResourceSearchParams extends InspectionEventSearchParams {

    @Schema(description="The unique ID of the related inspection event.")
    private String inspectionEventId;
    public String getInspectionEventId() {
        return inspectionEventId;
    }
    public void setInspectionEventId(String id) {
        this.inspectionEventId = id;
    }

    @Schema(description="The unique ID of the related resource.")
    private String resourceId;
    public String getResourceId() {
        return resourceId;
    }
    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    @Override
    public boolean hasCriteria() {
        return super.hasCriteria() || hasValue(inspectionEventId) || hasValue(resourceId);
    }
}
