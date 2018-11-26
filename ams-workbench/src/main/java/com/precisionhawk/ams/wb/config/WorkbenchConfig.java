package com.precisionhawk.ams.wb.config;

import com.precisionhawk.ams.wb.process.CommandProcess;
import java.util.List;

/**
 *
 * @author pchapman
 */
public interface WorkbenchConfig {

    String getConfigDirName();
    
    List<CommandProcess> getCommands();
}
