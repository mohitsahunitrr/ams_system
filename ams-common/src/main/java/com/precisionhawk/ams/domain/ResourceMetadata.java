package com.precisionhawk.ams.domain;

import com.precisionhawk.ams.bean.Dimension;
import com.precisionhawk.ams.bean.GeoPoint;
import io.swagger.oas.annotations.media.Schema;
import java.time.ZonedDateTime;
import java.util.Objects;

/**
 *
 * @author Philip A. Chapman
 */
@Schema(description="Data related to a resource (image, sound, video, sensor readings, etc) gathered when an inspection is made.")
public final class ResourceMetadata {

    @Schema(description="Unique ID of the related asset.")
    private String assetId;
    public String getAssetId() {
        return assetId;
    }
    public void setAssetId(String poleId) {
        this.assetId = poleId;
    }

    @Schema(description="Unique ID of the related asset inspection.")
    private String assetInspectionId;
    public String getAssetInspectionId() {
        return assetInspectionId;
    }
    public void setAssetInspectionId(String poleInspectionId) {
        this.assetInspectionId = poleInspectionId;
    }
    
    @Schema(description="The media type of the resource. See https://en.wikipedia.org/wiki/Media_type")
    private String contentType;
    public String getContentType() {
        return contentType;
    }
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    @Schema(description="The unique ID of the resource.")
    private String resourceId;
    public String getResourceId() {
        return resourceId;
    }
    public void setResourceId(String id) {
        this.resourceId = id;
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

    @Schema(description="The date and time the resource was obtained.")
    private ZonedDateTime timestamp;
    public ZonedDateTime getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(ZonedDateTime timestamp) {
        this.timestamp = timestamp;
    }

    @Schema(description="A geographic point indicating where the resouce was obtained.")
    private GeoPoint location;
    public GeoPoint getLocation() {
        return location;
    }
    public void setLocation(GeoPoint location) {
        this.location = location;
    }

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
    
    @Schema(description="The size of the resource, if applicable.")
    private Dimension size;
    public Dimension getSize() {
        return size;
    }
    public void setSize(Dimension size) {
        this.size = size;
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
    
    /** The ID of the zoomified data stored in the repository. */
    @Schema(description="If the resource is an image, the unique ID of the zoomify file created from the image.")
    private String zoomifyId;
    public String getZoomifyId() {
        return zoomifyId;
    }
    public void setZoomifyId(String zoomifyId) {
        this.zoomifyId = zoomifyId;
    }
    
    public ResourceMetadata() {}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ResourceMetadata that = (ResourceMetadata) o;
        
        return Objects.equals(resourceId, that.resourceId);
    }
    
    @Override
    public int hashCode() {
        return resourceId != null ? resourceId.hashCode() : 0;
    }

    @Override
    public String toString() {
        return String.format("Resource %s Name: %s Content Type: %s", resourceId, name, contentType);
    }
}
