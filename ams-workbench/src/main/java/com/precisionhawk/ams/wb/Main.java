package com.precisionhawk.ams.wb;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.gaffer.GafferConfigurator;
import ch.qos.logback.core.util.StatusPrinter;
import com.precisionhawk.ams.wb.config.ConfigUtil;
import com.precisionhawk.ams.wb.config.WorkbenchConfig;
import com.precisionhawk.ams.wb.process.CommandProcess;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.LinkedList;
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
        WorkbenchConfig config = ConfigUtil.loadConfiguration();
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
        File f = new File(ConfigUtil.defaultConfigDir(wbconfig), USER_LOGGING_COFIG);
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
}
