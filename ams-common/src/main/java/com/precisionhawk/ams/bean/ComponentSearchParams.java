package com.precisionhawk.ams.bean;

import com.precisionhawk.ams.domain.*;
import io.swagger.oas.annotations.media.Schema;

/**
 * An object which represents a component of an asset.
 *
 * @author pchapman
 */
@Schema(description="Search criteria for components.  At least one field must have a non-null value.")
public final class ComponentSearchParams extends AbstractSearchParams implements SiteAware {

    @Schema(description="The unique ID of the site.")
    private String siteId;
    @Override
    public String getSiteId() {
        return siteId;
    }
    public void setSiteId(String siteId) {
        this.siteId = siteId;
    }
    
    @Schema(description="The unique ID of the asset.")
    private String assetId;
    public String getAssetId() {
        return assetId;
    }
    public void setAssetId(String assetId) {
        this.assetId = assetId;
    }
    
    @Schema(description="The type of component.")
    private ComponentType type;
    public ComponentType getType() {
        return type;
    }
    public void setType(ComponentType type) {
        this.type = type;
    }

    @Schema(description="The serial number of the component.")
    private String serialNumber;
    public String getSerialNumber() {
        return serialNumber;
    }
    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }
    
    @Schema(description="Unique ID of the component assigned by the Utility.")
    private String utilityId;
    public String getUtilityId() {
        return utilityId;
    }
    public void setUtilityId(String utilityId) {
        this.utilityId = utilityId;
    }

    @Override
    public boolean hasCriteria() {
        return hasValue(assetId) || hasValue(siteId) || hasValue(assetId) || hasValue(utilityId) || hasValue(serialNumber) || type != null;
    }
}
