/*
 * All rights reserved.
 */

package com.precisionhawk.ams.bean;

import com.precisionhawk.ams.domain.InspectionEventResource;
import com.precisionhawk.ams.domain.ResourceMetadata;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/**
 * A bean which contains a ResourceMetadata object along with all its related
 * InspectionEventResource instances.  Useful for returning them related for
 * the Zoomify front-end to use.
 *
 * @author <a href="mailto:pchapman@pcsw.us">Philip A. Chapman</a>
 */
public class ResourcePolygons implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    public ResourcePolygons() {
        inspectionEventResources = new LinkedList<InspectionEventResource>();
    }

    private List<InspectionEventResource> inspectionEventResources;
    public List<InspectionEventResource> getInspectionEventResources() {
        return inspectionEventResources;
    }
    public void setInspectionEventResources(List<InspectionEventResource> inspectionEventResources) {
        this.inspectionEventResources = inspectionEventResources;
    }

    private ResourceMetadata resource;
    public ResourceMetadata getResource() {
        return resource;
    }
    public void setResource(ResourceMetadata resource) {
        this.resource = resource;
    }
}
