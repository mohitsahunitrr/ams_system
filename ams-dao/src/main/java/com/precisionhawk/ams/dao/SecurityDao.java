package com.precisionhawk.ams.dao;

import com.precisionhawk.ams.bean.security.Application;
import com.precisionhawk.ams.bean.security.CachedUserInfo;
import com.precisionhawk.ams.bean.security.Group;
import com.precisionhawk.ams.bean.security.UserInfoBean;
import com.precisionhawk.ams.bean.security.UserSearchParams;
import com.precisionhawk.ams.domain.Organization;
import java.util.List;

/**
 * @author Philip A. Chapman <pchapman@pcsw.us>
 */
public interface SecurityDao {

    // APPLICATION
    void deleteApplication(String appId);
    
    void insertApplication(Application application);
    
    Application selectApplicationById(String appId);
    
    List<Application> selectApplications();

    // GROUP
    void deleteGroup(String groupId);

    void insertGroup(Group group);

    Group selectGroupById(String groupId);

    List<Group> selectGroupsOrderedByName();

    List<Group> selectGroupsForAppOrderedByName(String appId);

    Group selectGroupByKey(String appId, String key);

    // ORGANIZATION
    
    void insertOrganization(Organization org) throws DaoException;

    void deleteOrganization(String orgId);

    Organization selectOrganizationById(String orgId);

    List<Organization> selectOrganizations();

    List<Organization> selectOrganizationsForUser(String userId);

    void insertUserIntoOrganization(String orgId, String userId);

    void removeUserFromOrganization(String orgId, String userId);

    Organization selectOrganizationByKey(String orgKey);
            
    // SITE
    List<String> selectSitesForUser(String userId);

    void insertUserIntoSite(String siteId, String userId);

    void deleteUserFromSite(String siteId, String userId);
    
    // User Info (non-authorative cache of User information)
    void deleteUserInfo(String userId);
    
    void insertUserInfo(UserInfoBean userInfo);
    
    List<CachedUserInfo> selectUserInfo(UserSearchParams parameters);
    
    boolean updateUserInfo(UserInfoBean userInfo);
}
