package com.precisionhawk.ams.dao.rdbms;

import com.precisionhawk.ams.bean.security.Application;
import com.precisionhawk.ams.bean.security.CachedUserInfo;
import com.precisionhawk.ams.bean.security.Group;
import com.precisionhawk.ams.bean.security.TenantGroup;
import com.precisionhawk.ams.bean.security.UserInfoBean;
import com.precisionhawk.ams.bean.security.UserSearchParams;
import com.precisionhawk.ams.domain.Organization;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Philip A. Chapman <pchapman@pcsw.us>
 */
@Component
public final class SecurityDaoImpl implements com.precisionhawk.ams.dao.SecurityDao {
    
    private final Logger LOGGER = LoggerFactory.getLogger(getClass());
    private static final String PARAM_EMAIL_ADDRESS_ID = "emailAddress";
    private static final String PARAM_EMAIL_NICKNAME = "emailNickname";
    private static final String PARAM_SITE_ID = "siteId";
    private static final String PARAM_TENANT_ID = "tenantId";
    private static final String PARAM_USER_ID = "userId";

    @Autowired
    private SqlSession sess;

    public SqlSession getSqlSession() {
        return sess;
    }

    public void setSqlSession(SqlSession session) {
        this.sess = session;
    }

    @Override
    public void deleteGroup(String groupId) {
        SqlSession session = getSqlSession();
        session.delete("Group.deleteGroup", groupId);
    }

    @Override
    public void insertGroup(Group group) {
        SqlSession session = getSqlSession();
        group.setId(UUID.randomUUID().toString());
        session.insert("Group.insertGroup", group);
    }

    @Override
    public Group selectGroupById(String groupId) {
        SqlSession session = getSqlSession();
        return (Group) session.selectOne("Group.selectGroupById", groupId);
    }

    @Override
    public List<Group> selectGroupsOrderedByName() {
        SqlSession session = getSqlSession();
        return session.selectList("Group.selectGroupsOrderedByName");
    }

    @Override
    public Group selectGroupByKey(String appId, String key) {
        Map<String, Object> params = new HashMap<>();
        params.put("appId", appId);
        params.put("groupKey", key);
        SqlSession session = getSqlSession();
        return session.selectOne("Group.selectGroupByKey", params);
    }

    @Override
    public List<Group> selectGroupsForAppOrderedByName(String appId) {
        SqlSession session = getSqlSession();
        return session.selectList("selectGroupsForAppOrderedByName", appId);
    }

    @Override
    public void insertOrganization(Organization org) {
        SqlSession session = getSqlSession();
        session.insert("Org.insertOrg", org);
    }

    @Override
    public void deleteOrganization(String orgId) {
        SqlSession session = getSqlSession();
        session.delete("Org.deleteOrg", orgId);
    }

    @Override
    public Organization selectOrganizationById(String orgId) {
        SqlSession session = getSqlSession();
        return (Organization) session.selectOne("Org.selectOrgById", orgId);
    }

    @Override
    public List<Organization> selectOrganizations() {
        SqlSession session = getSqlSession();
        return session.selectList("Org.selectOrgsOrderedByName");
    }

    @Override
    public List<Organization> selectOrganizationsForUser(String userId) {
        SqlSession session = getSqlSession();
        return session.selectList("Org.selectOrgsByUserId", userId);
    }

    @Override
    public void insertUserIntoOrganization(String orgId, String userId) {
        Map<String, Object> params = new HashMap<>();
        params.put("orgId", orgId);
        params.put("userId", userId);
        getSqlSession().insert("Org.insertUserIntoOrg", params);
    }

    @Override
    public void removeUserFromOrganization(String orgId, String userId) {
        Map<String, Object> params = new HashMap<>();
        params.put("orgId", orgId);
        params.put("userId", userId);
        getSqlSession().insert("Org.deleteUserFromOrg", params);
    }

    @Override
    public Organization selectOrganizationByKey(String orgKey) {
        SqlSession session = getSqlSession();
        return (Organization) session.selectOne("Org.selectOrgByKey", orgKey);
    }

    @Override
    public void deleteApplication(String appId) {
        SqlSession session = getSqlSession();
        session.delete("App.deleteApplication");
    }

    @Override
    public void insertApplication(Application application) {
        SqlSession session = getSqlSession();
        session.insert("App.insertApplication", application);
    }

    @Override
    public Application selectApplicationById(String appId) {
        SqlSession session = getSqlSession();
        return session.selectOne("App.selectApplicationById", appId);
    }

    @Override
    public List<Application> selectApplications() {
        SqlSession session = getSqlSession();
        return session.selectList("App.selectApplications");
    }

    @Override
    public List<TenantGroup> selectTenantGroups() {
        SqlSession session = getSqlSession();
        return session.selectList("Group.selectTenantGroups");
    }

    @Override
    public List<Group> selectGroupsByTenantGroup(String tenantId, String addGroupId) {
        Map<String, String> params = new HashMap();
        params.put("tenantId", tenantId);
        params.put("addGroupId", addGroupId);
        SqlSession session = getSqlSession();
        return session.selectList("Group.selectGroupByTenantGroup", params);
    }

    @Override
    public List<String> selectAADGroupsByTenant(String tenantId) {
        SqlSession session = getSqlSession();
        return session.selectList("Group.selectTenantAADGroups", tenantId);
    }

    @Override
    public void insertUserIntoSite(String siteId, String userId) {
        Map<String, Object> params = new HashMap<>();
        params.put(PARAM_SITE_ID, siteId);
        params.put(PARAM_USER_ID, userId);
        SqlSession sqlSession = getSqlSession();
        sqlSession.insert("User.insertUserForSite", params);
    }

    @Override
    public void deleteUserFromSite(String siteId, String userId) {
        Map<String, Object> params = new HashMap<>();
        params.put(PARAM_SITE_ID, siteId);
        params.put(PARAM_USER_ID, userId);
        SqlSession sqlSession = getSqlSession();
        sqlSession.delete("User.deleteUserFromSite", params);
    }

    @Override
    public List<String> selectSitesForUser(String userId) {
        SqlSession sqlSession = getSqlSession();
        return sqlSession.selectList("User.selectSitesForUser", userId);
    }

    @Override
    public void deleteUserInfo(String userId) {
        SqlSession sqlSession = getSqlSession();
        sqlSession.delete("User.deleteUser");
    }

    @Override
    public void insertUserInfo(UserInfoBean userInfo) {
        SqlSession sqlSession = getSqlSession();
        sqlSession.insert("User.insertUser", userInfo);
    }
    
    @Override
    public boolean updateUserInfo(UserInfoBean userInfo) {
        SqlSession sqlSession = getSqlSession();
        int count = sqlSession.update("User.updateUser", userInfo);
        return count > 0;
    }

    @Override
    public List<CachedUserInfo> selectUserInfo(UserSearchParams searchBean) {
        if (
                searchBean.getEmailAddress() == null
                &&
                searchBean.getEmailNickname() == null
                &&
                searchBean.getUserId() == null
           )
        {
            throw new IllegalArgumentException("The email address, email nickname or user ID to search for must be provided.");
        }
        SqlSession sqlSession = getSqlSession();
        return sqlSession.selectList("User.selectUserSearch", searchBean);
    }
}
