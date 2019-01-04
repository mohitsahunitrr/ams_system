package com.precisionhawk.ams.bean;

import io.swagger.oas.annotations.media.Schema;
import java.time.ZonedDateTime;

/**
 * A status change event.
 * @author pchapman
 * @param <T> The type of status changed.
 */
@Schema(description="A status change event and related information.")
public class StatusEvent <T extends StatusEnum> {
    
    @Schema(description="Comments about the status change.")
    private String comments;
    @Schema(description="That status")
    private T status;
    @Schema(description="The time when the status changed.")
    private ZonedDateTime timestamp;
    @Schema(description="the unique ID of the user responsible for the status change, if any.")
    private String userId;

    public String getComments() {
        return comments;
    }
    public void setComments(String comments) {
        this.comments = comments;
    }

    public T getStatus() {
        return status;
    }
    public void setStatus(T status) {
        this.status = status;
    }

    public ZonedDateTime getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(ZonedDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getUserId() {
        return userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }
}
