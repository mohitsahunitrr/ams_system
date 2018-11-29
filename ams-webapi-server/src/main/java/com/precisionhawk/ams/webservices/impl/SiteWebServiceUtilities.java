package com.precisionhawk.ams.webservices.impl;

import com.precisionhawk.ams.bean.security.ServicesSessionBean;
import com.precisionhawk.ams.domain.Site;
import java.util.Collection;
import java.util.Iterator;
import javax.ws.rs.NotAuthorizedException;

/**
 * Utility functions for authorizing actions against site objects.
 *
 * @author pchapman
 */
public class SiteWebServiceUtilities {
    
    public static <T extends Site> T authorizeSite(ServicesSessionBean sess, T site, String ... groupKeys) {
        if (site == null) {
            return null;
        }
        if (!sess.isTokenValid()) {
            throw new NotAuthorizedException(sess.getReason());
        }
        //TODO: Org Aware for Org ID?
        if (sess.getCredentials().checkAuthorization(null, null, site.getId(), false, groupKeys)) {
            return site;
        } else {
            throw new NotAuthorizedException(String.format("User not authorized for site %s", site.getId()));
        }
    }

    public static <T extends Collection<? extends Site>> T authorizeSites(ServicesSessionBean sess, T sites, String ... groupKeys)
        throws NotAuthorizedException
    {
        if (sites == null) {
            return null;
        }
        if (!sess.isTokenValid()) {
            throw new NotAuthorizedException(sess.getReason());
        }
        // Check org and group permissions
        //TODO: Org Aware for Org ID?
        if (sess.getCredentials().checkAuthorization(null, null, null, false, groupKeys)) {
            // Check site permissions
            for (Site site : sites) {
                if (site != null) {
                    if (site.getId() != null && !sess.getCredentials().getSiteIDs().contains(site.getId())) {
                        throw new NotAuthorizedException(String.format("User not authorized for site %s", site.getId()));
                    }
                }
            }
        }
        return sites;
    }
    
    public static <T extends Collection<? extends Site>> T cleanseUnAuthorizedSites(ServicesSessionBean sess, T sites)
    {
        Site site;
        for (Iterator<? extends Site> iter = sites.iterator(); iter.hasNext() ;) {
            site = iter.next();
            if (site.getId() != null && !sess.getCredentials().getSiteIDs().contains(site.getId())) {
                iter.remove();
            }
        }
        return sites;
    }
    
}
