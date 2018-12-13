/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.precisionhawk.ams.config;

/**
 *
 * @author pchapman
 */
public interface DbConfig {
    String getDataBaseName();
    String getPassword();
    Integer getPortNumber();
    String getServerName();
    String getUserName();
    Integer getInitialConnections();
    Integer getMaxConnections();
}
