/*
 * All rights reserved.
 */

package com.precisionhawk.ams.bean;

import com.precisionhawk.ams.domain.*;
import io.swagger.oas.annotations.media.Schema;

/**
 *
 * @author <a href="mailto:pchapman@pcsw.us">Philip A. Chapman</a>
 */
@Schema(description="Search criteria for component inspections.  At least one field must have a non-null value.")
public class ComponentInspectionSearchParams extends AbstractSearchParams implements SiteAware {
    
    @Schema(description="The unique ID of the asset.")
    private String assetId;
    public String getAssetId() {
        return assetId;
    }
    public void setAssetId(String assetId) {
        this.assetId = assetId;
    }

    @Schema(description="The unique ID of the asset inspection.")
    private String assetInspectionId;
    public String getAssetInspectionId() {
        return assetInspectionId;
    }
    public void setAssetInspectionId(String assetInspectionId) {
        this.assetInspectionId = assetInspectionId;
    }
    
    @Schema(description="The unique ID of the component.")
    private String componentId;
    public String getComponentId() {
        return componentId;
    }
    public void setComponentId(String componentId) {
        this.componentId = componentId;
    }
    
    @Schema(description="The work order number.")
    private String orderNumber;
    public String getOrderNumber() {
        return orderNumber;
    }
    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }
    
    @Schema(description="The unique ID of the site.")
    private String siteId;
    @Override
    public String getSiteId() {
        return siteId;
    }
    public void setSiteId(String siteId) {
        this.siteId = siteId;
    }

    @Schema(description="The unique ID of the site related site inspection.")
    private String siteInspectionId;
    public String getSiteInspectionId() {
        return siteInspectionId;
    }
    public void setSiteInspectionId(String siteInspectionId) {
        this.siteInspectionId = siteInspectionId;
    }
    
    @Schema(description="Current status of the inspection.")
    private ComponentInspectionStatus status;
    public ComponentInspectionStatus getStatus() {
        return status;
    }
    public void setStatus(ComponentInspectionStatus status) {
        this.status = status;
    }

    @Schema(description="The type of inspection.")
    private ComponentInspectionType type;
    public ComponentInspectionType getType() {
        return type;
    }
    public void setType(ComponentInspectionType type) {
        this.type = type;
    }

    @Override
    public boolean hasCriteria() {
        return testField(assetId) || testField(assetInspectionId)
                || testField(componentId) || testField(orderNumber)
                || testField(siteId) || testField(siteInspectionId)
                || (status != null) || (type != null);
    }
}
