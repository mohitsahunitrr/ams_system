/*
 * All rights reserved.
 */

package com.precisionhawk.ams.domain;

import io.swagger.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.Objects;

/**
 *
 * @author <a href="mailto:pchapman@pcsw.us">Philip A. Chapman</a>
 */
@Schema(description="An inspetion of a site.")
public class SiteInspection implements Identifyable, SiteAware {

    @Schema(description="The unique ID of the inspection.")
    private String id;
    @Override
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    @Schema(description="The date the inspection was done.")
    private LocalDate dateOfInspection;
    public LocalDate getDateOfInspection() {
        return dateOfInspection;
    }
    public void setDateOfInspection(LocalDate dateOfInspection) {
        this.dateOfInspection = dateOfInspection;
    }
    
    @Schema(description="The order number for the inspection.")
    private String orderNumber;
    public String getOrderNumber() {
        return orderNumber;
    }
    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }
    
    @Schema(description="Who processed the inspection.")
    private String processedBy;
    public String getProcessedBy() {
        return processedBy;
    }
    public void setProcessedBy(String processedBy) {
        this.processedBy = processedBy;
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

    public SiteInspection() {}
    
    public SiteInspection(SiteInspection original) {
        this.dateOfInspection = original.getDateOfInspection();
        this.id = original.getId();
        this.orderNumber = original.getOrderNumber();
        this.processedBy = original.getProcessedBy();
        this.siteId = original.getSiteId();
        this.status = original.getStatus();
        this.type = original.getType();
    }
    
    @Override
    public int hashCode() {
        int hash = 3;
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
        final SiteInspection other = (SiteInspection) obj;
        return Objects.equals(this.id, other.id);
    }
}
