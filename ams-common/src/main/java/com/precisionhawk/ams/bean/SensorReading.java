/*
 * All rights reserved.
 */

package com.precisionhawk.ams.bean;

import com.precisionhawk.ams.support.time.TimeFormattingConstants;
import io.swagger.oas.annotations.media.Schema;
import java.time.Duration;
import java.time.ZonedDateTime;

/**
 *
 * @author <a href="mailto:pchapman@pcsw.us">Philip A. Chapman</a>
 */
@Schema(description="A sensor reading taken while doing an inspection.")
public class SensorReading {
    
    @Schema(description="The type of reading taken.")
    private SensorReadingType type;
    public SensorReadingType getType() {
        return type;
    }
    public void setType(SensorReadingType type) {
        this.type = type;
    }

    @Schema(description="The value of the reading taken. Units will depend on reading type.")
    private Double value;
    public Double getValue() {
        return value;
    }
    public void setValue(Double value) {
        this.value = value;
    }

    @Schema(description="The time the taking of the reading started.")
    private ZonedDateTime startTime;
    public ZonedDateTime getStartTime() {
        return startTime;
    }
    public void setStartTime(ZonedDateTime startTime) {
        this.startTime = startTime;
    }

    @Schema(description="The time the taking of the reading ended.")
    private ZonedDateTime endTime;
    public ZonedDateTime getEndTime() {
        return endTime;
    }
    public void setEndTime(ZonedDateTime endtime) {
        this.endTime = endtime;
    }
    
    public Duration duration() {
        if (startTime == null || endTime == null) {
            return null;
        } else {
            return Duration.between(startTime, endTime);
        }
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 71 * hash + (this.type != null ? this.type.hashCode() : 0);
        hash = 71 * hash + (this.value != null ? this.value.hashCode() : 0);
        hash = 71 * hash + (this.startTime != null ? this.startTime.hashCode() : 0);
        hash = 71 * hash + (this.endTime != null ? this.endTime.hashCode() : 0);
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
        final SensorReading other = (SensorReading) obj;
        if (this.type != other.type) {
            return false;
        }
        if (this.value != other.value && (this.value == null || !this.value.equals(other.value))) {
            return false;
        }
        if (this.startTime != other.startTime && (this.startTime == null || !this.startTime.equals(other.startTime))) {
            return false;
        }
        if (this.endTime != other.endTime && (this.endTime == null || !this.endTime.equals(other.endTime))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("SensorReading{");
        sb.append("startTime=");
        sb.append(TimeFormattingConstants.DATE_TIME_FORMATTER.format(startTime));
        sb.append(", ");
        sb.append("endtime=");
        sb.append(TimeFormattingConstants.DATE_TIME_FORMATTER.format(endTime));
        sb.append(", ");
        sb.append("type=");
        sb.append(type);
        sb.append(", ");
        sb.append("value=");
        sb.append(value);
        sb.append('}');
        return sb.toString();
    }
}
