/*
 * All rights reserved.
 */

package com.precisionhawk.ams.bean;

import io.swagger.oas.annotations.media.Schema;
import java.io.Serializable;

/**
 *
 * @author <a href="mailto:pchapman@pcsw.us">Philip A. Chapman</a>
 */
@Schema(description="A point in 3-D space.")
public class Point implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private Double x;
    public Double getX() {
        return x;
    }
    public void setX(Double x) {
        this.x = x;
    }

    private Double y;
    public Double getY() {
        return y;
    }
    public void setY(Double y) {
        this.y = y;
    }

    private Double z;
    public Double getZ() {
        return z;
    }
    public void setZ(Double z) {
        this.z = z;
    }
    
    public Point() {
        this(null, null, null);
    }
    
    public Point(Double x, Double y) {
        this(x, y, null);
    }
    
    public Point(Double x, Double y, Double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    public Point clone() {
        return new Point(x, y, z);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 59 * hash + (this.x != null ? this.x.hashCode() : 0);
        hash = 59 * hash + (this.y != null ? this.y.hashCode() : 0);
        hash = 59 * hash + (this.z != null ? this.z.hashCode() : 0);
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
        final Point other = (Point) obj;
        if (this.x != other.x && (this.x == null || !this.x.equals(other.x))) {
            return false;
        }
        if (this.y != other.y && (this.y == null || !this.y.equals(other.y))) {
            return false;
        }
        if (this.z != other.z && (this.z == null || !this.z.equals(other.z))) {
            return false;
        }
        return true;
    }
    
    public java.awt.Point toAwtPoint() {
        return new java.awt.Point(x.intValue(), y.intValue());
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("(X: ");
        sb.append(fieldToString(x));
        sb.append(", Y: ");
        sb.append(fieldToString(y));
        sb.append(", Z: ");
        sb.append(fieldToString(z));
        sb.append(")");
        
        return sb.toString();
    }
    
    private static String fieldToString(Double fieldValue) {
        if (fieldValue == null) {
            return "NULL";
        } else {
            return fieldValue.toString();
        }
    }
}
