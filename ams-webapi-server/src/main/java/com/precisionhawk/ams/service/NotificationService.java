package com.precisionhawk.ams.service;

/**
 * A service for sending notifications asynchronously.
 *
 * @author pchapman
 */
public interface NotificationService {
    public void publishCriticalMessage(String message);    
}
