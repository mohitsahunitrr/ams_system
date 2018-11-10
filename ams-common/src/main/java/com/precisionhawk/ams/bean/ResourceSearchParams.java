/*
 * All rights reserved.
 */

package com.precisionhawk.ams.bean;

import com.precisionhawk.ams.domain.ResourceStatus;
import com.precisionhawk.ams.domain.ResourceType;
import io.swagger.oas.annotations.media.Schema;

/**
 *
 * @author <a href="mailto:pchapman@pcsw.us">Philip A. Chapman</a>
 */
@Schema(description="A bean containing search criteria for resources and objects related to resources.  At least one field must have a non-null value.")
public class ResourceSearchParams extends AbstractSearchParams {

    @Schema(description="A name for the resource.")
    private String name;
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    
    @Schema(description="The organization.")
    private String organizationId;
    public String getOrganizationId() {
        return organizationId;
    }
    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    @Schema(description="The unique ID of the related asset.")
    private String assetId;
    public String getAssetId() {
        return assetId;
    }
    public void setAssetId(String assetId) {
        this.assetId = assetId;
    }

    @Schema(description="The unique ID of the related asset inspection.")
    private String assetInspectionId;
    public String getAssetInspectionId() {
        return assetInspectionId;
    }
    public void setAssetInspectionId(String assetInspectionId) {
        this.assetInspectionId = assetInspectionId;
    }
    
    @Schema(description="The unique ID of the related site.")
    private String siteId;
    public String getSiteId() {
        return siteId;
    }
    public void setSiteId(String siteId) {
        this.siteId = siteId;
    }
    
    @Schema(description="The unique ID of the related site inspection.")
    private String siteInspectionId;
    public String getSiteInspectionId() {
        return siteInspectionId;
    }
    public void setSiteInspectionId(String siteInspectionId) {
        this.siteInspectionId = siteInspectionId;
    }

    /**
     * Unique ID of the original resource image if this one is the result of
     * processing.
     */
    @Schema(description="If this resource was produced by modifying another resource (such as cropping an image), this is the unique ID of the source resource.")
    private String sourceResourceId;
    public String getSourceResourceId() {
        return sourceResourceId;
    }
    public void setSourceResourceId(String id) {
        this.sourceResourceId = id;
    }

    @Schema(description="The status of the resource.")
    private ResourceStatus status;
    public ResourceStatus getStatus() {
	return status;
    }
    public void setStatus(ResourceStatus status) {
	this.status = status;
    }

    @Schema(description="The type of resource being stored.")
    private ResourceType type;
    public ResourceType getType() {
        return type;
    }
    public void setType(ResourceType type) {
        this.type = type;
    }
    
    @Schema(description="The unique ID of the zoomify data for the image.")
    private String zoomifyId;
    public String getZoomifyId() {
        return zoomifyId;
    }
    public void setZoomifyId(String zoomifyId) {
        this.zoomifyId = zoomifyId;
    }
    
    @Override
    public boolean hasCriteria() {
        return testField(siteId) || testField(organizationId) || testField(assetId) ||
                testField(assetInspectionId) || testField(siteInspectionId) ||
                status != null || testField(sourceResourceId) || type != null;
    }
}
