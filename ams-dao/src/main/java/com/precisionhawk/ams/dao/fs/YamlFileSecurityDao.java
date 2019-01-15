package com.precisionhawk.ams.dao.fs;

import com.esotericsoftware.yamlbeans.YamlReader;
import com.precisionhawk.ams.bean.security.Application;
import com.precisionhawk.ams.bean.security.CachedUserInfo;
import com.precisionhawk.ams.bean.security.Group;
import com.precisionhawk.ams.bean.security.UserInfoBean;
import com.precisionhawk.ams.bean.security.UserSearchParams;
import com.precisionhawk.ams.dao.DaoException;
import com.precisionhawk.ams.dao.SimpleSecurityDao;
import com.precisionhawk.ams.domain.Organization;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author pchapman
 */
public class YamlFileSecurityDao implements SimpleSecurityDao {
    
    private final Object LOCK = new Object();
    private final Logger LOGGER = LoggerFactory.getLogger(getClass());
    
    private SecurityData data;
    
    private File configFile;
    
    private long lastModified = 0;
    
    public void configure(String configFilePath) {
        configFile = new File(configFilePath);
        ensureData(true);
    }
    
    private void ensureData(boolean failSafe) {
        synchronized (LOCK) {
            if (lastModified != configFile.lastModified()) {
                Reader reader = null;
                try {
                    reader = new FileReader(configFile);
                    YamlReader yamlreader = new YamlReader(reader);
                    SecurityData d = yamlreader.read(SecurityData.class);
                    lastModified = configFile.lastModified();
                    data = d;
                } catch (IOException ex) {
                    LOGGER.error(String.format("Error loading security data from file %s", configFile), ex);
                    if (failSafe) {
                        throw new RuntimeException("Error loading security data.");
                    }
                } finally {
                    IOUtils.closeQuietly(reader);
                }
            }
        }
    }
    
    @Override
    public String getPassHash(String userId) {
        ensureData(false);
        SelfContainedUserInfo ui = data.getUsers().get(userId);
        if (ui == null) {
            return null;
        } else {
            return ui.getPasswordHash();
        }
    }

    @Override
    public void deleteApplication(String appId) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void insertApplication(Application application) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Application selectApplicationById(String appId) {
        ensureData(false);
        return data.getApplications().get(appId);
    }

    @Override
    public List<Application> selectApplications() {
        ensureData(false);
        List<Application> apps = new ArrayList<>(data.getApplications().values());
        return apps;
    }

    @Override
    public void deleteGroup(String groupId) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void insertGroup(Group group) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Group selectGroupById(String groupId) {
        return data.getGroups().get(groupId);
    }
    
    private final Comparator<Group> NAME_COMPARATOR = new Comparator<Group>() {
        @Override
        public int compare(Group o1, Group o2) {
            return o1.getName().compareTo(o2.getName());
        }
    };

    @Override
    public List<Group> selectGroupsOrderedByName() {
        ensureData(false);
        List<Group> groups = new ArrayList<>(data.getGroups().values());
        Collections.sort(groups, NAME_COMPARATOR);
        return groups;
    }

    @Override
    public List<Group> selectGroupsForAppOrderedByName(String appId) {
        ensureData(false);
        List<Group> groups = new ArrayList<>();
        for (Group g : data.getGroups().values()) {
            if (g.getApplicationId().equals(appId)) {
                groups.add(g);
            }
        }
        Collections.sort(groups, NAME_COMPARATOR);
        return groups;
    }

    @Override
    public Group selectGroupByKey(String appId, String key) {
        ensureData(false);
        for (Group g : data.getGroups().values()) {
            if (g.getKey().equals(key)) {
                return g;
            }
        }
        return null;
    }

    @Override
    public void insertOrganization(Organization org) throws DaoException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void deleteOrganization(String orgId) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Organization selectOrganizationById(String orgId) {
        ensureData(false);
        return data.getOrganizations().get(orgId);
    }

    @Override
    public List<Organization> selectOrganizations() {
        ensureData(false);
        return new ArrayList<>(data.getOrganizations().values());
    }

    @Override
    public List<Organization> selectOrganizationsForUser(String userId) {
        ensureData(false);
        SelfContainedUserInfo ui = data.getUsers().get(userId);
        if (ui == null) {
            return Collections.emptyList();
        } else {
            List<Organization> orgs = new LinkedList<>();
            for (String orgId : ui.getOrganizations()) {
                orgs.add(data.getOrganizations().get(orgId));
            }
            return orgs;
        }
    }

    @Override
    public void insertUserIntoOrganization(String orgId, String userId) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void removeUserFromOrganization(String orgId, String userId) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Organization selectOrganizationByKey(String orgKey) {
        ensureData(false);
        for (Organization org : data.getOrganizations().values()) {
            if (org.getKey().equals(orgKey)) {
                return org;
            }
        }
        return null;
    }

    @Override
    public List<String> selectSitesForUser(String userId) {
        ensureData(false);
        SelfContainedUserInfo ui = data.getUsers().get(userId);
        if (ui == null) {
            return Collections.emptyList();
        } else {
            return ui.getSites();
        }
    }

    @Override
    public void insertUserIntoSite(String siteId, String userId) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void deleteUserFromSite(String siteId, String userId) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void deleteUserInfo(String userId) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void insertUserInfo(UserInfoBean userInfo) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<CachedUserInfo> selectUserInfo(UserSearchParams parameters) {
        ensureData(false);
        // Only email address makes sense here.
        if (parameters.getEmailAddress() == null || parameters.getEmailAddress().isEmpty()) {
            return Collections.emptyList();
        } else {
            List<CachedUserInfo> results = new LinkedList<>();
            for (SelfContainedUserInfo ui : data.getUsers().values()) {
                if (parameters.getEmailAddress().equals(ui.getEmailAddress())) {
                    results.add(ui);
                }
            }
            return results;
        }
    }

    @Override
    public boolean updateUserInfo(UserInfoBean userInfo) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }    
}
