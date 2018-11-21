/*
 * All rights reserved.
 */
package com.precisionhawk.ams.support.http;

/**
 *
 * @author Philip A. Chapman
 */
public interface ResponseCallback {
    void receive(int responseStatus, String response);
}
