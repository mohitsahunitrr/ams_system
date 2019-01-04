/*
 * All rights reserved.
 */

package com.precisionhawk.ams.domain;

import com.precisionhawk.ams.bean.StatusEvent;
import io.swagger.oas.annotations.media.Schema;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author <a href="mailto:pchapman@pcsw.us">Philip A. Chapman</a>
 */
@Schema(description="An inspection of a component as part of an asset inspection.")
public class ComponentInspection implements Identifyable, SiteAware {
    
    @Schema(description="The unique ID of the component.")
    private String id;
    @Override
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

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
    
    @Schema(description="A description of the inspection.")
    private String description;
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
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
    
    @Schema(description="A list of status changes.")
    private List<StatusEvent<ComponentInspectionStatus>> statusHistory = new LinkedList<>();
    public List<StatusEvent<ComponentInspectionStatus>> getStatusHistory() {
        return statusHistory;
    }
    public void setStatusHistory(List<StatusEvent<ComponentInspectionStatus>> statusHistory) {
        this.statusHistory = statusHistory;
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
    public int hashCode() {
        int hash = 3;
        hash = 79 * hash + Objects.hashCode(this.id);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ComponentInspection other = (ComponentInspection) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        return true;
    }
}
