package com.precisionhawk.ams.service.oauth.auth0;

import com.nimbusds.jwt.JWTClaimsSet;
import com.precisionhawk.ams.bean.security.ServicesSessionBean;
import com.precisionhawk.ams.config.TenantConfig;
import com.precisionhawk.ams.security.AccessTokenProvider;
import com.precisionhawk.ams.service.oauth.GroupsProvider;
import com.precisionhawk.ams.support.jackson.ObjectMapperFactory;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.slf4j.LoggerFactory;

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
    public Set<String> loadGroupIDs(AccessTokenProvider accessTokenProvider, ServicesSessionBean session, JWTClaimsSet claimsSet) throws SecurityException {
        if (session.getAppData() != null && !session.getAppData().isEmpty()) {
            try {
                AppMetadata appdata = ObjectMapperFactory.getObjectMapper().readValue(session.getAppData(), AppMetadata.class);
                return new HashSet<>(appdata.getGroups());
            } catch (IOException ioe) {
                LoggerFactory.getLogger(getClass()).error("Error deserializing app data", ioe);
            }
        }
        return Collections.emptySet();
    }
    
}
