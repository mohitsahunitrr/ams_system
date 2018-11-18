package com.precisionhawk.ams.dao;

/**
 *
 * @author pchapman
 */
public class AbstractDao {
    protected static void ensureExists(Object obj, String errMsg) throws DaoException {
        if (
                obj == null
                ||
                (obj instanceof String && ((String)obj).isEmpty())
            )
        {
            throw new DaoException(errMsg);
        }
    }    
}
