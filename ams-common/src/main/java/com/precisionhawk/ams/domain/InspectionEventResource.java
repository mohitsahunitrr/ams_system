/*
 * All rights reserved.
 */
package com.precisionhawk.ams.domain;

import io.swagger.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author <a href="mailto:pchapman@pcsw.us">Philip A. Chapman</a>
 */
@Schema(description="An object indicating damage shown in a resource.")
public class InspectionEventResource implements Identifyable, SiteAware {

    public InspectionEventResource() {
        polygons = new ArrayList<>();
    }
    
    @Schema(description="The unique ID of the asset that is damaged.")
    private String assetId;
    public String getAssetId() {
        return assetId;
    }
    public void setAssetId(String assetId) {
        this.assetId = assetId;
    }
    
    @Schema(description="The unique ID of this object.")
    private String id;
    @Override
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    
    @Schema(description="The unique ID of the inspection event in which the damage is reported.")
    private String inspectionEventId;
    public String getInspectionEventId() {
        return inspectionEventId;
    }
    public void setInspectionEventId(String id) {
        this.inspectionEventId = id;
    }

    @Schema(description="The unique ID of the resource demonstrating the damage.")
    private String resourceId;
    public String getResourceId() {
        return resourceId;
    }
    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    @Schema(description="If the resource is an image, polygons that outline the damage on the image.  Usually one per image per inspection event, but could be more.")
    private List<InspectionEventPolygon> polygons;
    public List<InspectionEventPolygon> getPolygons() {
        return polygons;
    }
    public void setPolygons(List<InspectionEventPolygon> polygons) {
        this.polygons = polygons;
    }

    @Schema(description="The unique ID of the site in which the asset is located.")
    private String siteId;
    @Override
    public String getSiteId() {
        return siteId;
    }
    public void setSiteId(String siteId) {
        this.siteId = siteId;
    }
    
    @Schema(description="The work order number associated with the inspection.")
    private String orderNumber;
    public String getOrderNumber() {
        return orderNumber;
    }
    public void setOrderNumber(String workOrderNumber) {
        this.orderNumber = workOrderNumber;
    }
}
