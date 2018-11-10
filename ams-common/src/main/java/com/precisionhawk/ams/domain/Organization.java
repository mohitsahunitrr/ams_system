/*
 * All rights reserved.
 */

package com.precisionhawk.ams.domain;

import io.swagger.oas.annotations.media.Schema;
import java.util.Objects;

/**
 *
 * @author <a href="mailto:pchapman@pcsw.us">Philip A. Chapman</a>
 */
@Schema(description="An organization.")
public class Organization implements Comparable<Organization>, Identifyable {
    
    private static final long serialVersionUID = 1L;

    @Schema(description="The unique ID for the org.")
    private String id;
    @Override
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    
    @Schema(description="A unique key for the org.")
    private String key;
    public String getKey() {
        return key;
    }
    public void setKey(String key) {
        this.key = key;
    }

    @Schema(description="The name of the org.")
    private String name;
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    
    public Organization() {}
    
    public Organization(String id, String key, String name) {
        this.id = id;
        this.key = key;
        this.name = name;
    }

    @Override
    public int compareTo(Organization o) {
        if (o == null) {
            return 1;
        }
        return getName().compareTo(o.getName());
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 31 * hash + Objects.hashCode(this.id);
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
        final Organization other = (Organization) obj;
        return Objects.equals(this.id, other.id);
    }
}
