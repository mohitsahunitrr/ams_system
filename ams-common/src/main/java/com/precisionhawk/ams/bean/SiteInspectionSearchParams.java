/*
 * All rights reserved.
 */

package com.precisionhawk.ams.bean;

import com.precisionhawk.ams.domain.*;
import io.swagger.oas.annotations.media.Schema;
import java.util.Objects;

/**
 *
 * @author <a href="mailto:pchapman@pcsw.us">Philip A. Chapman</a>
 */
@Schema(description="Search criteria for site inspections.  At least one field must have a non-null value.")
public class SiteInspectionSearchParams extends AbstractSearchParams implements SiteAware {

    @Schema(description="The order number for the inspection.")
    private String orderNumber;
    public String getOrderNumber() {
        return orderNumber;
    }
    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }
    
    @Schema(description="The unique ID of the site at which the inspeciton took place.")
    private String siteId;
    @Override
    public String getSiteId() {
        return siteId;
    }
    public void setSiteId(String siteId) {
        this.siteId = siteId;
    }

    @Schema(description="The status of the inspection.")
    private SiteInspectionStatus status;
    public SiteInspectionStatus getStatus() {
        return status;
    }
    public void setStatus(SiteInspectionStatus status) {
        this.status = status;
    }
    
    @Schema(description="The type of inspection done.")
    private SiteInspectionType type;
    public SiteInspectionType getType() {
        return type;
    }
    public void setType(SiteInspectionType type) {
        this.type = type;
    }

    @Override
    public boolean hasCriteria() {
        return testField(orderNumber) || testField(siteId) || (status != null) || (type != null);
    }
}
