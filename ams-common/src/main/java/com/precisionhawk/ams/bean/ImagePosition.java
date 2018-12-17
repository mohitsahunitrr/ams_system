package com.precisionhawk.ams.bean;

import io.swagger.oas.annotations.media.Schema;

/**
 * The position of the portion of the object being photographed.
 * 
 * @author <a href="mailto:pchapman@pcsw.us">Philip A. Chapman</a>
 */
@Schema(description="The position of the portion of the object being photographed.")
public class ImagePosition {
    
    @Schema(description="The side of the component being photographed.")
    private String side;
    public String getSide() {
        return side;
    }
    public void setSide(String side) {
        this.side = side;
    }

    @Schema(description="The position as an integer percentage (0.00 = 0%, 1.00 = 100%) along the widge of the object.")
    private Float x;
    public Float getX() {
        return x;
    }
    public void setX(Float x) {
        this.x = x;
    }

    @Schema(description="The position as an integer percentage (0.00 = 0%, 1.00 = 100%) along the height of the object.")
    private Float y;
    public Float getY() {
        return y;
    }
    public void setY(Float y) {
        this.y = y;
    }
}