package com.precisionhawk.ams.bean;

/**
 *
 * @author pchapman
 */
public abstract class AbstractSearchParams {
    
    public abstract boolean hasCriteria();
    
    protected static boolean hasValue(String field) {
        return field != null && field.length() > 0;
    }
}
