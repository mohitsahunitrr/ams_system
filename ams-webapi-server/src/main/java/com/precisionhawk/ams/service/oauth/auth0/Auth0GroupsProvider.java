package com.precisionhawk.ams.service.oauth.auth0;

import com.nimbusds.jwt.JWTClaimsSet;
import com.precisionhawk.ams.bean.security.Group;
import com.precisionhawk.ams.bean.security.ServicesSessionBean;
import com.precisionhawk.ams.config.TenantConfig;
import com.precisionhawk.ams.security.AccessTokenProvider;
import com.precisionhawk.ams.service.oauth.GroupsProvider;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author pchapman
 */
public class Auth0GroupsProvider extends GroupsProvider {

    @Override
    public void configure(TenantConfig config) {
        super.configure(config);
    }

    @Override
    public Set<Group> loadGroups(AccessTokenProvider accessTokenProvider, ServicesSessionBean session, JWTClaimsSet claimsSet) throws SecurityException {
        // Expect a list of assigned groups in AppData.
        if (session.getAppData() != null && !session.getAppData().isEmpty()) {
            Object o = session.getAppData().get("groups");
            if (o instanceof List) {
                Map<String, Group> groups = new HashMap<>();
                for (Group g : dao.selectGroupsOrderedByName()) {
                    groups.put(g.getId(), g);
                }
                Group g;
                Set<Group> results = new HashSet<>();
                for (Object gid : (List)o) {
                    g = groups.get(gid.toString());
                    if (g != null) {
                        results.add(g);
                    }
                }
                return results;
            }
        }
        return Collections.emptySet();
    }    
}
