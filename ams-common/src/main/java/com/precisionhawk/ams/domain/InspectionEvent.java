/*
 * All rights reserved.
 */
package com.precisionhawk.ams.domain;

import com.precisionhawk.ams.bean.Dimension;
import io.swagger.oas.annotations.media.Schema;
import java.time.LocalDate;

/**
 *
 * @author <a href="mailto:pchapman@pcsw.us">Philip A. Chapman</a>
 */
@Schema(description="A record of damage found during review of an inspection.")
public class InspectionEvent implements Identifyable, SiteAware {

    @Schema(description="Unique ID of the inspection event.")
    private String id;
    @Override
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    @Schema(description="Unique ID of the site in which the asset is located.")    
    private String siteId;
    @Override
    public String getSiteId() {
        return siteId;
    }
    public void setSiteId(String siteId) {
        this.siteId = siteId;
    }
    
    @Schema(description="The work order number associated with the inspection.")
    private String orderNumber;
    public String getOrderNumber() {
        return orderNumber;
    }
    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    @Schema(description="The unique ID of the damaged asset.")
    private String assetId;
    public String getAssetId() {
        return assetId;
    }
    public void setAssetId(String assetId) {
        this.assetId = assetId;
    }

    @Schema(description="The unique ID of the damaged component.")    
    private String componentId;
    public String getComponentId() {
        return componentId;
    }
    public void setComponentId(String componentId) {
        this.componentId = componentId;
    }

    @Schema(description="A user readable name for the event.")
    private String name;
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    @Schema(description="The type of observation.")
    private String observationType;
    public String getObservationType() {
        return observationType;
    }
    public void setObservationType(String observationType) {
        this.observationType = observationType;
    }
    
    @Schema(description="The type of damage found.  This is the result of a lookup value.  See the translations JSON for details.")
    private String findingType;
    public String getFindingType() {
        return findingType;
    }
    public void setFindingType(String findingType) {
        this.findingType = findingType;
    }

    @Schema(description="The severity of the damage on a scale from 1 to 5.")    
    private Integer severity;
    public Integer getSeverity() {
        return severity;
    }
    public void setSeverity(Integer severity) {
        this.severity = severity;
    }

    @Schema(description="A comment about the damage found.")
    private String comment;
    public String getComment() {
        return comment;
    }
    public void setComment(String comment) {
        this.comment = comment;
    }

    @Schema(description="The size of the area of incident.")
    private Dimension size;
    public Dimension getSize() {
        return size;
    }
    public void setSize(Dimension size) {
        this.size = size;
    }

    @Schema(description="The source of the inspection event.")
    private InspectionEventSource source;
    public InspectionEventSource getSource() {
        return source;
    }
    public void setSource(InspectionEventSource source) {
        this.source = source;
    }

    @Schema(description="The unique ID of the user reporting the damage.")
    private String userId;
    public String getUserId() {
        return userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Schema(description="The date the date was reported.")
    private LocalDate date;
    public LocalDate getDate() {
        return date;
    }
    public void setDate(LocalDate date) {
        this.date = date;
    }
}
