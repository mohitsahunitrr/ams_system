package com.precisionhawk.ams.domain;

import java.util.Objects;
import org.codehaus.jackson.annotate.JsonValue;

/**
 *
 * @author pchapman
 */
public abstract class ExtendableEnum {
    
    private final String value;
    
    public ExtendableEnum(String value) {
        this.value = value;
    }
    
    @JsonValue
    public String getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 79 * hash + Objects.hashCode(this.value);
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
        final ExtendableEnum other = (ExtendableEnum) obj;
        if (!Objects.equals(this.value, other.value)) {
            return false;
        }
        return true;
    }
}
