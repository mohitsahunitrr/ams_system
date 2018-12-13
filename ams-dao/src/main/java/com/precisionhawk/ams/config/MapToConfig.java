package com.precisionhawk.ams.config;

import java.util.Map;

/**
 *
 * @author pchapman
 */
public abstract class MapToConfig {
    
    protected abstract Map<String, Object> configMap();
    
    private Object objectFromMap(String name, boolean errorOnNull) {
        Object o = configMap().get(name);
        if (o == null) {
            if (errorOnNull) {
                throw new IllegalArgumentException(String.format("Value missing for property \"%s\"", name));
            } else {
                return null;
            }
        } else {
            return o;
        }
    }
        
    protected Boolean booleanFromMap(String name) {
        return booleanFromMap(name, true);
    }
    
    protected Boolean booleanFromMap(String name, boolean errorOnNull) {
        Object o = objectFromMap(name, errorOnNull);
        if (o == null) {
            return null;
        } else {
            if (o instanceof Boolean) {
                return (Boolean)o;
            }
            // Boolean's valueOf method is too permissive.  Thus we do it ourselves.
            String s = o.toString().trim().toLowerCase();
            switch (s) {
                case "1":
                case "true":
                case "yes":
                    return Boolean.TRUE;
                case "0":
                case "false":
                case "no":
                    return Boolean.FALSE;
                default:
                    throw new IllegalArgumentException(String.format("Invalid boolean value \"%s\" for property \"%s\"", o.toString(), name));
            }
        }
    }
 
    protected Integer integerFromMap(String name) {
        return integerFromMap(name, true);
    }
    
    protected Integer integerFromMap(String name, boolean errorOnNull) {
        Object o = objectFromMap(name, errorOnNull);
        if (o == null) {
            return null;
        } else {
            if (o instanceof Integer) {
                return (Integer)o;
            }
            String s = o.toString();
            try {
                return Integer.valueOf(s);
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException(String.format("Invalid integer value \"%s\" for property \"%s\"", s, name));
            }
        }
    }
 
    protected Long longFromMap(String name) {
        return longFromMap(name, true);
    }
    
    protected Long longFromMap(String name, boolean errorOnNull) {
        Object o = objectFromMap(name, errorOnNull);
        if (o == null) {
            return null;
        } else {
            if (o instanceof Long) {
                return (Long)o;
            }
            String s = o.toString();
            try {
                return Long.valueOf(s);
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException(String.format("Invalid long value \"%s\" for property \"%s\"", s, name));
            }
        }
    }
    
    protected String stringFromMap(String name) {
        return stringFromMap(name, true);
    }
    
    protected String stringFromMap(String name, boolean errorOnNull) {
        Object o = objectFromMap(name, errorOnNull);
        if (o == null) {
            return null;
        } else {
            return o.toString();
        }
    }
}
