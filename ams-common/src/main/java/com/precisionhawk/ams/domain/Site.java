package com.precisionhawk.ams.domain;

import com.precisionhawk.ams.bean.Address;
import io.swagger.oas.annotations.media.Schema;
import java.util.Objects;

/**
 * The wind turbine farms site.
 *
 * @author <a href="mail:pchapman@pcsw.us">Philip A. Chapman</a>
 */
@Schema(description="A site where assets exist.")
public class Site implements Identifyable {

    @Schema(description="Unique ID of the site.")
    private String id;
    @Override
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    @Schema(description="Name of the site, should be unique.")
    private String name;
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    
    @Schema(description="Unique ID of the owning organization.")
    private String organizationId;
    public String getOrganizationId() {
        return organizationId;
    }
    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }
    
    @Schema(description="The physical address of the site.")
    private Address physicalAddress;
    public Address getPhysicalAddress() {
        return physicalAddress;
    }
    public void setPhysicalAddress(Address physicalAddress) {
        this.physicalAddress = physicalAddress;
    }
    
    @Override
    public String toString() {
        return name;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + Objects.hashCode(this.id);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Site other = (Site) obj;
        return Objects.equals(this.id, other.id);
    }    
}
