/*
 * All rights reserved.
 */

package com.precisionhawk.ams.service.oauth.aad;

import com.precisionhawk.ams.service.oauth.GroupsProvider;
import com.nimbusds.jwt.JWTClaimsSet;
import com.precisionhawk.ams.bean.security.ServicesSessionBean;
import com.precisionhawk.ams.security.AccessTokenProvider;
import java.util.HashSet;
import java.util.Set;
import org.papernapkin.liana.util.StringUtil;

/**
 * Generates WindAMS Roles based on Azure application roles security. The roles are listed as a claim
 * in the JWT Token.  See the &quot;roles&quot; claim documented at
 * {@link https://docs.microsoft.com/en-us/azure/active-directory/develop/active-directory-token-and-claims}
 *
 * @author <a href="mailto:pchapman@pcsw.us">Philip A. Chapman</a>
 */
public class RolesBasedGroupsProvider extends GroupsProvider {

    @Override
    public Set<String> loadGroupIDs(AccessTokenProvider accessTokenProvider, ServicesSessionBean session, JWTClaimsSet claimsSet) throws SecurityException {
        // Parse the "roles" claim and return the roles indicated there as group IDs.
        Set<String> groupIDs = new HashSet();
        String rolesClaim = StringUtil.nullableToString(claimsSet.getClaim("roles"));
        
        if (rolesClaim == null || rolesClaim.length() == 0) {
            throw new SecurityException("Security claim \"roles\" not found.");
        }
        
        // Strip the surrounded brackets.
        if (!rolesClaim.startsWith("[") && rolesClaim.endsWith("]")) {
            throw new SecurityException(String.format("Invalid value for claim \"roles\": %s", rolesClaim));
        }
        rolesClaim = rolesClaim.substring(0, rolesClaim.length() - 1).substring(1);
        
        String[] roles = rolesClaim.split(",");
        for (String role : roles) {
            role = role.trim();
            // Strip the surrounded double-quotes.
            if (!rolesClaim.startsWith("\"") && rolesClaim.endsWith("\"")) {
                throw new SecurityException(String.format("Invalid value for claim \"roles\": %s", claimsSet.getClaim("roles").toString()));
            }
            role = role.substring(0, role.length() - 1).substring(1);
            groupIDs.add(role);
        }
        return groupIDs;
    }
}
