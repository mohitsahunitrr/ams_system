/*
 * All rights reserved.
 */

package com.precisionhawk.ams.cache;

import com.precisionhawk.ams.bean.security.ServicesSessionBean;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 *
 * @author <a href="mailto:pchapman@pcsw.us">Philip A. Chapman</a>
 */
@Lazy(true)
@Component("nullSecurityTokenCache")
public class NoOppSecurityTokenCache implements SecurityTokenCache {

    @Override
    public void store(ServicesSessionBean bean) {
        // Do not store the token.
    }

    @Override
    public ServicesSessionBean retrieve(String bearerToken) {
        // We never have the token.
        return null;
    }
}
