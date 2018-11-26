/*
 * All rights reserved.
 */

package com.precisionhawk.ams.wb.process;

import com.precisionhawk.ams.wb.config.WorkbenchConfig;
import com.precisionhawk.ams.webservices.client.Environment;
import com.precisionhawk.ams.webservices.client.spring.EnvironmentsFactory;
import java.io.File;
import java.util.List;
import java.util.Queue;

/**
 *
 * @author <a href="mailto:pchapman@pcsw.us">Philip A. Chapman</a>
 */
public abstract class ServiceClientCommandProcess extends CommandProcess {

    private static final String DEFAULT_CFG_FILE = "environments.yaml";
    private static final String ARG_CONFIG = "-cfg";
    private static final String ARG_ENV = "-env";
    protected static final String ARGS_FOR_HELP = "[" + ARG_CONFIG + " environments/config/file.yaml] " + ARG_ENV + " environment";

    @Override
    public final boolean process(WorkbenchConfig config, Queue<String> args) {
        String configURI = "file://" + new File(new File(new File(new File(System.getProperty("user.home")), ".ph"), config.getConfigDirName()), DEFAULT_CFG_FILE).getAbsolutePath();
        String envStr = null;
        
        String s;
        for (String arg = args.poll(); arg != null; arg = args.poll()) {
            switch (arg) {
                case ARG_CONFIG:
                    s = args.poll();
                    if (s == null) {
                        return false;
                    } else {
                        configURI = s;
                    }
                    break;
                case ARG_ENV:
                    s = args.poll();
                    if (s == null || envStr != null) {
                        return false;
                    } else {
                        envStr = s;
                    }
                    break;
                default:
                    if (!processArg(arg, args)) {
                        return false;
                    }
            }
        }
        
        if (configURI == null) {
            return false;
        }
        
        EnvironmentsFactory factory = new EnvironmentsFactory();
        factory.setConfigFilePath(configURI);
        List<Environment> environments;
        try {
            factory.init();
            environments = factory.getEnvironments();
        } catch (Exception ex) {
            System.err.printf("Unable to configure environments from URI %s\n", configURI);
            return false;
        }
        
        Environment env = null;
        if (envStr != null) {
            for (Environment e : environments) {
                if (envStr.equals(e.getName())) {
                    env = e;
                    break;
                }
            }
            if (env == null) {
                System.err.printf("Unable to locate configuration for environment %s\n", envStr);
                return false;
            }
        } else if (environments.size() == 1) {
            env = environments.get(0);
        } else {
            System.err.println("Unable to determine what environment to use");
            return false;
        }
        
        return execute(env);
    }
    
    protected abstract boolean processArg(String arg, Queue<String> args);
    
    protected abstract boolean execute(Environment env);
}
