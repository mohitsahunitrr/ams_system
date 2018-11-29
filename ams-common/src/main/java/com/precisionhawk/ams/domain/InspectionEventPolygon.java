/*
 * All rights reserved.
 */
package com.precisionhawk.ams.domain;

import com.precisionhawk.ams.bean.Point;
import io.swagger.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author <a href="mailto:pchapman@pcsw.us">Philip A. Chapman</a>
 */
@Schema(description="An area on an image that outlines asset damage.")
public class InspectionEventPolygon implements Identifyable {
    
    public InspectionEventPolygon() {
        geometry = new ArrayList<>();
    }

    @Schema(description="The unique ID of this polygon.")
    private String id;
    @Override
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    @Schema(description="The center of the polygon.")
    private Point center;
    public Point getCenter() {
        return center;
    }
    public void setCenter(Point center) {
        this.center = center;
    }

    @Schema(description="A list of points that outline asset damage.")
    private List<Point> geometry;
    public List<Point> getGeometry() {
        return geometry;
    }
    public void setGeometry(List<Point> geometry) {
        this.geometry = geometry;
    }
}
