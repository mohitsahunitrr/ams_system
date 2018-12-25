package com.precisionhawk.ams.service.oauth;

import com.nimbusds.jwt.JWTClaimsSet;
import com.precisionhawk.ams.bean.security.Group;
import com.precisionhawk.ams.bean.security.ServicesSessionBean;
import com.precisionhawk.ams.security.AccessTokenProvider;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author pchapman
 */
public abstract class MappedGroupsProvider extends GroupsProvider {
    
    @Override
    public Set<Group> loadGroups(AccessTokenProvider accessTokenProvider, ServicesSessionBean session, JWTClaimsSet claimsSet) throws SecurityException {
        Set<String> groupIDs = loadGroupIDs(accessTokenProvider, session, claimsSet);
        // Match provider group IDs to AMS Groups
        Set<Group> groups = new HashSet();
        for (String groupId : groupIDs) {
            List<Group> glist = dao.selectGroupsByTenantGroup(session.getTenantId(), groupId);
            if (glist != null && !glist.isEmpty()) {
                groups.addAll(glist);
            }
        }
        return groups;
    }
    
    /**
     * Queries Azure Active Directory for group memberships and returns them.
     * @param accessTokenProvider The provider for the access token to be used
     * to make the necessary service calls.
     * @param session The session containing user credentials.
     * @param claimsSet The claims set used to build the user credentials.
     * @return The list of group IDs.
     * @throws SecurityException  Indicates an error obtaining the information.
     */
    protected abstract Set<String> loadGroupIDs(AccessTokenProvider accessTokenProvider, ServicesSessionBean session, JWTClaimsSet claimsSet) throws SecurityException;
}
