/*
 * All rights reserved.
 */
package com.precisionhawk.ams.wb.process;

import com.precisionhawk.ams.wb.config.WorkbenchConfig;
import java.io.PrintStream;
import java.util.Queue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Philip A. Chapman
 */
public abstract class CommandProcess {
    
    protected final Logger LOGGER = LoggerFactory.getLogger(getClass());
    
    public abstract boolean canProcess(String command);
    
    public abstract void printHelp(PrintStream output);
    
    public abstract boolean process(WorkbenchConfig config, Queue<String> args);
}
