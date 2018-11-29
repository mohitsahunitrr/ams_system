package com.precisionhawk.ams.bean;

import com.precisionhawk.ams.domain.SiteAware;
import io.swagger.oas.annotations.media.Schema;

/**
 *
 * @author pchapman
 */
@Schema(description="A bean containing search criteria for inspection events.  At least one field must have a non-null value.")
public class InspectionEventSearchParams extends AbstractSearchParams implements SiteAware {
    
    @Schema(description="The unique ID of the component that the inspection events will be related to.")
    private String componentId;
    public String getComponentId() {
        return componentId;
    }
    public void setComponentId(String componentId) {
        this.componentId = componentId;
    }
    
    @Schema(description="The order number under which the inspections were made.")
    private String orderNumber;
    public String getOrderNumber() {
        return orderNumber;
    }
    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    @Schema(description="The unique ID of the site that inspection events will be related to.")
    private String siteId;
    @Override
    public String getSiteId() {
        return siteId;
    }
    public void setSiteId(String siteId) {
        this.siteId = siteId;
    }

    @Schema(description="The unique ID of the asset that inspection events will be related to.")
    private String assetId;
    public String getAssetId() {
        return assetId;
    }
    public void setAssetId(String assetId) {
        this.assetId = assetId;
    }
   
    @Override
    public boolean hasCriteria() {
        return
                hasValue(siteId)
                || hasValue(orderNumber)
                || hasValue(assetId)
                || hasValue(componentId);
    }
}
