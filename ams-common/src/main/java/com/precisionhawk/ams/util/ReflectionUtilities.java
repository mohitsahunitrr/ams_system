package com.precisionhawk.ams.util;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author pchapman
 */
public class ReflectionUtilities {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ReflectionUtilities.class);

    public static <T> List<Class<T>> findClassesImpmenenting(final Class<T> interfaceClass, final Package fromPackage) {

        if (interfaceClass == null) {
            LOGGER.debug("Unknown subclass.");
            return null;
        }

        if (fromPackage == null) {
            LOGGER.debug("Unknown package.");
            return null;
        }

        final List<Class<T>> rVal = new ArrayList<>();
        try {
            final Class<?>[] targets = getAllClassesFromPackage(fromPackage.getName());
            if (targets != null) {
                for (Class<?> aTarget : targets) {
                    if (aTarget == null) {
                        continue;
                    }
                    else if (aTarget.equals(interfaceClass)) {
                        LOGGER.debug("Found the interface definition.");
                        continue;
                    }
                    else if (!interfaceClass.isAssignableFrom(aTarget)) {
                        LOGGER.debug("Class '" + aTarget.getName() + "' is not a " + interfaceClass.getName());
                        continue;
                    }
                    else {
                        rVal.add((Class<T>)aTarget);
                    }
                }
            }
        }
        catch (ClassNotFoundException e) {
            LOGGER.warn("Error reading package name.", e);
        }
        catch (IOException e) {
            LOGGER.error("Error reading classes in package.", e);
        }

        return rVal;
    }

    /**
     * Load all classes from a package.
     * 
     * @param packageName
     * @return
     * @throws ClassNotFoundException
     * @throws IOException
     */
    public static Class[] getAllClassesFromPackage(final String packageName) throws ClassNotFoundException, IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        assert classLoader != null;
        String path = packageName.replace('.', '/');
        Enumeration<URL> resources = classLoader.getResources(path);
        List<File> dirs = new ArrayList<>();
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            dirs.add(new File(resource.getFile()));
        }
        ArrayList<Class> classes = new ArrayList<>();
        for (File directory : dirs) {
            classes.addAll(findClasses(directory, packageName));
        }
        return classes.toArray(new Class[classes.size()]);
    }

    /**
     * Find file in package.
     * 
     * @param directory
     * @param packageName
     * @return
     * @throws ClassNotFoundException
     */
    public static List<Class<?>> findClasses(File directory, String packageName) throws ClassNotFoundException {
        List<Class<?>> classes = new ArrayList<>();
        if (!directory.exists()) {
            return classes;
        }
        File[] files = directory.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                assert !file.getName().contains(".");
                classes.addAll(findClasses(file, packageName + "." + file.getName()));
            }
            else if (file.getName().endsWith(".class")) {
                classes.add(Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6)));
            }
        }
        return classes;
    }    
}
