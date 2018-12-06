/*
 * All rights reserved.
 */

package com.precisionhawk.ams.domain;

import io.swagger.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author <a href="mailto:pchapman@pcsw.us">Philip A. Chapman</a>
 */
@Schema(description="An order for work.")
public class WorkOrder implements SitesAware {

    @Schema(description="A description of the work to be done.")
    private String description;
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    @Schema(description="Unique order number for the order.")
    private String orderNumber;
    public String getOrderNumber() {
        return orderNumber;
    }
    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    @Schema(description="Date order was requested.")
    private LocalDate requestDate;
    public LocalDate getRequestDate() {
        return requestDate;
    }
    public void setRequestDate(LocalDate requestDate) {
        this.requestDate = requestDate;
    }
    
    @Schema(description="The sites at which the work is to be done.")
    private List<String> siteIds = new LinkedList<>();
    @Override
    public List<String> getSiteIds() {
        return siteIds;
    }
    public void setSiteIds(List<String> siteIds) {
        this.siteIds = siteIds;
    }

    @Schema(description="The status of the work.")
    private WorkOrderStatus status;
    public WorkOrderStatus getStatus() {
        return status;
    }
    public void setStatus(WorkOrderStatus status) {
        this.status = status;
    }

    @Schema(description="The type of work to be done.")
    private WorkOrderType type;
    public WorkOrderType getType() {
        return type;
    }
    public void setType(WorkOrderType type) {
        this.type = type;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + Objects.hashCode(this.orderNumber);
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
        final WorkOrder other = (WorkOrder) obj;
        return Objects.equals(this.orderNumber, other.orderNumber);
    }
}
