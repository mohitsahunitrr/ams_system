package com.precisionhawk.ams.webapi.swagger;

import io.swagger.jaxrs2.integration.JaxrsApplicationAndAnnotationScanner;
import java.util.Set;
import org.slf4j.LoggerFactory;

/**
 *
 * @author pchapman
 */
//TODO: Make this a shared class in AMS
public class JaxrsAnnotationScanner extends JaxrsApplicationAndAnnotationScanner {

    @Override
    public Set<Class<?>> classes() {
        Set<Class<?>> results = super.classes();
        if (openApiConfiguration.getResourceClasses() != null && !openApiConfiguration.getResourceClasses().isEmpty()) {
            for (String className : openApiConfiguration.getResourceClasses()) {
                try {
                    Class<?> clazz = Class.forName(className);
                    Object[] annots = clazz.getAnnotationsByType(javax.ws.rs.Path.class);
                    if (annots.length > 0) {
                        results.add(clazz);
                    }
                } catch (ClassNotFoundException ex) {
                    LoggerFactory.getLogger(getClass()).error("Error loading class for scanning of JAX WS RS annotations", ex);
                }
            }
        }
        return results;
    }
    
}
