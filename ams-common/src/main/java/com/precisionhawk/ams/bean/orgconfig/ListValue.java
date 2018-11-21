/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.precisionhawk.ams.bean.orgconfig;

import io.swagger.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.Objects;

/**
 *
 * @author pchapman
 */
@Schema(description="Possible value which can be chosen to assign to a field if it is limited set of valid values.")
public class ListValue {

    @Schema(description="The user-friendly label for the value.")
    private String label;
    public String getLabel() {
        return label;
    }
    public void setLabel(String label) {
        this.label = label;
    }

    @Schema(description="The value to be stored in the field.")
    private String value;
    public String getValue() {
        return value;
    }
    public void setValue(String value) {
        this.value = value;
    }

    @Schema(description="The sort order for this value in relation to other valid values for the field.  Sort order is zero based.")
    private int sortOrder;
    public int getSortOrder() {
        return sortOrder;
    }
    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }
    
    @Schema(description="The date upon which the value was deprecated.  For data entered after this date, this value is not valid.")
    private LocalDate deprecated;
    public LocalDate getDeprecated() {
        return deprecated;
    }
    public void setDeprecated(LocalDate deprecated) {
        this.deprecated = deprecated;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 17 * hash + Objects.hashCode(this.value);
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
        final ListValue other = (ListValue) obj;
        if (!Objects.equals(this.value, other.value)) {
            return false;
        }
        return true;
    }
}
