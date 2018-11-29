/*
 * All rights reserved.
 */

package com.precisionhawk.ams.webservices.impl;

import com.precisionhawk.ams.bean.security.ExtUserCredentials;
import com.precisionhawk.ams.bean.security.ServicesSessionBean;
import com.precisionhawk.ams.webservices.AuthenticationWebService;
import javax.ws.rs.BadRequestException;
import org.springframework.stereotype.Component;

/**
 *
 * @author <a href="mailto:pchapman@pcsw.us">Philip A. Chapman</a>
 */
//TODO: Do checks against user credentials.  Ensure proper authority to make these calls.
@Component
public class AuthenticationWebServiceImpl extends AbstractWebService  implements AuthenticationWebService {
    
    @Override
    public ExtUserCredentials obtainCredentials(String authToken) {
        ServicesSessionBean session = lookupSessionBean(authToken);
        if (!(session.getCredentials() instanceof ExtUserCredentials)) {
            throw new BadRequestException("This call only supports user credentials.");
        }
        return (ExtUserCredentials)session.getCredentials();
    }
}
