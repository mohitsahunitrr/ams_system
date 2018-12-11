package com.precisionhawk.ams.domain;

import com.precisionhawk.ams.bean.GeoPoint;
import io.swagger.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 *
 * @author pchapman
 */
@Schema(description="An asset being managed.")
public class Asset implements Identifyable, SiteAware {

    @Schema(description="Misc attributes associated with the asset.")
    private Map<String, String> attributes = new HashMap<>();
    public Map<String, String> getAttributes() {
        return attributes;
    }
    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    @Schema(description="The unique WindAMS ID for the asset.")
    private String id;
    @Override
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    
    @Schema(description="The date the asset was installed.")
    private LocalDate dateOfInstall;
    public LocalDate getDateOfInstall() {
        return dateOfInstall;
    }
    public void setDateOfInstall(LocalDate dateOfInstall) {
        this.dateOfInstall = dateOfInstall;
    }

    @Schema(description="Where the asset is installed.")
    private GeoPoint location;
    public GeoPoint getLocation() {
        return location;
    }
    public void setLocation(GeoPoint location) {
        this.location = location;
    }

    @Schema(description="Manufacturer of the asset.")
    private String make;
    public String getMake() {
        return make;
    }
    public void setMake(String make) {
        this.make = make;
    }

    @Schema(description="Model of the asset.")
    private String model;
    public String getModel() {
        return model;
    }
    public void setModel(String model) {
        this.model = model;
    }
    
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
        final Asset other = (Asset) obj;
        return Objects.equals(this.id, other.id);
    }

    @Override
    public String toString() {
        return "Asset{" + "id=" + id + ", siteId=" + siteId + ", name=" + name + ", type=" + type + ", make=" + make + ", model=" + model + ", serialNumber=" + serialNumber + '}';
    }
}
