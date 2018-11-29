/*
 * All rights reserved.
 */

package com.precisionhawk.ams.bean;

import com.precisionhawk.ams.domain.SiteAware;
import com.precisionhawk.ams.domain.WorkOrderStatus;
import com.precisionhawk.ams.domain.WorkOrderType;
import io.swagger.oas.annotations.media.Schema;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author <a href="mailto:pchapman@pcsw.us">Philip A. Chapman</a>
 */
@Schema(description="Search criteria for work orders.  At least one field must have a non-null value.")
public class WorkOrderSearchParams extends AbstractSearchParams implements SiteAware
{
    @Schema(description="Unique ID of related site.")
    private String siteId;
    @Override
    public String getSiteId() {
        return siteId;
    }
    public void setSiteId(String siteId) {
        this.siteId = siteId;
    }

    @Schema(description="Statuses of the work orders.")
    private List<WorkOrderStatus> statuses = new LinkedList<>();
    public List<WorkOrderStatus> getStatuses() {
        return statuses;
    }
    public void setStatuses(List<WorkOrderStatus> statuses) {
        this.statuses = statuses;
    }

    @Schema(description="Type of work orders.")
    private WorkOrderType type;
    public WorkOrderType getType() {
        return type;
    }
    public void setType(WorkOrderType type) {
        this.type = type;
    }

    @Override
    public boolean hasCriteria() {
        return hasValue(siteId) || (statuses != null && !statuses.isEmpty()) || type != null;
    }
}
