package com.precisionhawk.ams.config;

/**
 *
 * @author pchapman
 */
public class NotificationServicesConfigBean implements NotificationServicesConfig {
    private String criticalNotificationsTopic;
    public String getCriticalNotificationsTopic() {
        return criticalNotificationsTopic;
    }
    public void setCriticalNotificationsTopic(String criticalNotificationsTopic) {
        this.criticalNotificationsTopic = criticalNotificationsTopic;
    }
}
