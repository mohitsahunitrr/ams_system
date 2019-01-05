package com.precisionhawk.ams.dao;

/**
 *
 * @author pchapman
 */
public interface SimpleSecurityDao extends SecurityDao {
    
    String getPassHash(String userId);
}
