package com.precisionhawk.ams.wb.config;

import com.precisionhawk.ams.util.ReflectionUtilities;
import java.io.File;
import java.util.List;

/**
 *
 * @author pchapman
 */
public class ConfigUtil {
    
    private ConfigUtil() {}
    
    public static File defaultConfigDir(WorkbenchConfig config) {
        return new File(new File(new File(System.getProperty("user.home")), ".ph"), config.getConfigDirName());
    }
    
    public static WorkbenchConfig loadConfiguration() {
        WorkbenchConfig config = null;
        List<Class<WorkbenchConfig>> impls = ReflectionUtilities.findClassesImplementing(WorkbenchConfig.class, WorkbenchConfig.class.getPackage());
        if (impls.isEmpty()) {
            System.err.printf("No implentations of %s found in package %s.\n", WorkbenchConfig.class, WorkbenchConfig.class.getPackage());
        } else if (impls.size() == 1) {
            Class<WorkbenchConfig> clazz = impls.get(0);
            try {
                config = clazz.newInstance();
            } catch (IllegalAccessException | InstantiationException ex) {
                System.err.printf("Unable to instantiate workbench config class.", ex);
            }
        } else {
            System.err.printf("Multiple implentations of %s found in package %s.  Only one expected.\n", WorkbenchConfig.class, WorkbenchConfig.class.getPackage());
        }
        return config;
    }

}
