package com.precisionhawk.ams.util;

/**
 *
 * @author pchapman
 */
public class RegexUtils {    
    public static String EMAIL_REGEX = "^[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,}$";
    public static String UUID_REGEX = "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$";
    
    private RegexUtils() {}
}
