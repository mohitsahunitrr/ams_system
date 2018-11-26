package com.precisionhawk.ams.wb;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.gaffer.GafferConfigurator;
import ch.qos.logback.core.util.StatusPrinter;
import com.precisionhawk.ams.util.ReflectionUtilities;
import com.precisionhawk.ams.wb.config.WorkbenchConfig;
import com.precisionhawk.ams.wb.process.CommandProcess;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import org.slf4j.LoggerFactory;

/**
 *
 * @author <a href="mail:pchapman@pcsw.us">Philip A. Chapman</a>
 */
public final class Main {

    private static final String DEFAULT_LOGGING_CONFIG = "com/precisionhawk/ams/wb/workbench.logback.groovy";
    private static final String USER_LOGGING_COFIG = "workbench.logback.groovy";
    
    private Main() {}

    public static void main(String[] argsArray) {
        new Main().execute(argsArray);
    }
    
    private void execute(String[] argsArray) {
        WorkbenchConfig config = loadConfiguration();
        if (config == null) {
            System.exit(1);
        }
        configureLogging(config);
        Queue<String> args = new LinkedList<>();
        Collections.addAll(args, argsArray);
        boolean success = false;

        String command = args.poll();
        for (CommandProcess p : config.getCommands()) {
            if (p.canProcess(command)) {
                // Process the request
                success = p.process(config, args);
                break;
            }
        }

        if (!success) {
            printError(config);
        }
        System.exit(success ? 0 : 1);
    }
    
    private void printError(WorkbenchConfig config) {
        System.out.println("java -jar workbench.jar command args...");
        for (CommandProcess p : config.getCommands()) {
            p.printHelp(System.out);
        }
        System.exit(1);
    }

    private void configureLogging(WorkbenchConfig wbconfig) {
        // If there is a file in $home/.windams/workbench.logback.groovy, use that
        // Otherwise, use classpath://com.windams.wb.workbench.logback.groovy
        URL config = null;
        File f = new File(new File(new File(new File(System.getProperty("user.home")), ".ph"), wbconfig.getConfigDirName()), USER_LOGGING_COFIG);
        if (f.canRead()) {
            try {
                config = f.toURI().toURL();
            } catch (MalformedURLException ex) {
                
            }
        }
        if (config == null) {
            config = getClass().getClassLoader().getResource(DEFAULT_LOGGING_CONFIG);
        }
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        GafferConfigurator configurator = new GafferConfigurator(context);
        configurator.run(config);
        StatusPrinter.printInCaseOfErrorsOrWarnings(context);
        
        LoggerFactory.getLogger(getClass()).info("LogBack has been configured from {}", config);
    }
    
    private WorkbenchConfig loadConfiguration() {
        WorkbenchConfig config = null;
        List<Class<WorkbenchConfig>> impls = ReflectionUtilities.findClassesImpmenenting(WorkbenchConfig.class, WorkbenchConfig.class.getPackage());
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
