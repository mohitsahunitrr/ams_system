package com.precisionhawk.ams.bean;

import com.precisionhawk.ams.domain.*;
import io.swagger.oas.annotations.media.Schema;

/**
 *
 * @author pchapman
 */
@Schema(description="Search criteria for assets.  At least one field must have a non-null value.")
public final class AssetSearchParams extends AbstractSearchParams implements SiteAware {

    @Schema(description="Owner-specific name for the asset. Should be unique for the owner or the site, but not necessarily across all assets.")
    private String name;
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    
    @Schema(description="The serial number associated with the asset.  Should be unique across all assets.")
    private String serialNumber;
    public String getSerialNumber() {
        return serialNumber;
    }
    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }
    
    @Schema(description="The unique WindAMS ID of the site.")
    private String siteId;
    @Override
    public String getSiteId() {
        return siteId;
    }
    public void setSiteId(String siteId) {
        this.siteId = siteId;
    }
    
    @Schema(description="The type of asset.")
    private AssetType type;
    public AssetType getType() {
        return type;
    }
    public void setType(AssetType type) {
        this.type = type;
    }

    @Override
    public boolean hasCriteria() {
        return testField(name) || testField(serialNumber) || testField(siteId) || (type != null);
    }
}
