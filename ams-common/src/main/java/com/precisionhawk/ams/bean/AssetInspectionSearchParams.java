/*
 * All rights reserved.
 */

package com.precisionhawk.ams.bean;

import com.precisionhawk.ams.domain.AssetInspectionStatus;
import com.precisionhawk.ams.domain.SiteAware;
import io.swagger.oas.annotations.media.Schema;
import java.util.Objects;

/**
 *
 * @author <a href="mailto:pchapman@pcsw.us">Philip A. Chapman</a>
 */
@Schema(description="Search criteria for asset inspections.  At least one field must have a non-null value.")
public class AssetInspectionSearchParams extends AbstractSearchParams implements SiteAware {
    
    @Schema(description="Unique ID of the related asset.")
    private String assetId;
    public String getAssetId() {
        return assetId;
    }
    public void setAssetId(String assetId) {
        this.assetId = assetId;
    }

    @Schema(description="Unique ID of the site.")
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

    @Schema(description="Order number under which inspection was made.")
    private String orderNumber;
    public String getOrderNumber() {
        return orderNumber;
    }
    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    @Schema(description="The status of the inspection.")
    private AssetInspectionStatus status;
    public AssetInspectionStatus getStatus() {
        return status;
    }
    public void setStatus(AssetInspectionStatus status) {
        this.status = status;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 59 * hash + Objects.hashCode(this.assetId);
        hash = 59 * hash + Objects.hashCode(this.siteId);
        hash = 59 * hash + Objects.hashCode(this.orderNumber);
        hash = 59 * hash + Objects.hashCode(this.status);
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
        final AssetInspectionSearchParams other = (AssetInspectionSearchParams) obj;
        if (!Objects.equals(this.assetId, other.assetId)) {
            return false;
        }
        if (!Objects.equals(this.siteId, other.siteId)) {
            return false;
        }
        if (!Objects.equals(this.orderNumber, other.orderNumber)) {
            return false;
        }
        if (this.status != other.status) {
            return false;
        }
        return true;
    }  

    @Override
    public boolean hasCriteria() {
        return hasValue(assetId) || hasValue(orderNumber) || hasValue(siteId) || hasValue(siteInspectionId) || (status != null);
    }
}
