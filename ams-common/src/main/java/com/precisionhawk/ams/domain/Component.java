package com.precisionhawk.ams.domain;

import io.swagger.oas.annotations.media.Schema;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * An object which represents a component of an asset.
 *
 * @author pchapman
 */
@Schema(description="A component of an asset.")
public class Component implements Identifyable, SiteAware {

    @Schema(description="Misc attributes associated with the component.")
    private Map<String, String> attributes = new HashMap<>();
    public Map<String, String> getAttributes() {
        return attributes;
    }
    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    @Schema(description="The unique ID of the component.")
    private String id;
    @Override
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
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

    @Schema(description="The manufacturer of the component.")
    private String make;
    public String getMake() {
        return make;
    }
    public void setMake(String make) {
        this.make = make;
    }

    @Schema(description="The model number of the component.")
    private String model;
    public String getModel() {
        return model;
    }
    public void setModel(String model) {
        this.model = model;
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
    public int hashCode() {
        int hash = 5;
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
        final Component other = (Component) obj;
        return Objects.equals(this.id, other.id);
    }
}
