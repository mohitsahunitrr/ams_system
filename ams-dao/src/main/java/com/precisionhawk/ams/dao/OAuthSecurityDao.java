package com.precisionhawk.ams.dao;

import com.precisionhawk.ams.bean.security.Group;
import com.precisionhawk.ams.bean.security.TenantGroup;
import java.util.List;

/**
 * @author Philip A. Chapman <pchapman@pcsw.us>
 */
public interface OAuthSecurityDao extends SecurityDao {

    // Tenant User
    
    List<TenantGroup> selectTenantGroups();
    
    List<Group> selectGroupsByTenantGroup(String tenantId, String aadGroupId);
    
    List<String> selectAADGroupsByTenant(String tenantId);
    
}
