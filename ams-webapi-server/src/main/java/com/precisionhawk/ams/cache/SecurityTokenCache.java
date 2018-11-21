/*
 * All rights reserved.
 */

package com.precisionhawk.ams.cache;

import com.precisionhawk.ams.bean.security.ServicesSessionBean;

/**
 *
 * @author <a href="mailto:pchapman@pcsw.us">Philip A. Chapman</a>
 */
public interface SecurityTokenCache {

    public void store(ServicesSessionBean bean);

    public ServicesSessionBean retrieve(String bearerToken);

}
