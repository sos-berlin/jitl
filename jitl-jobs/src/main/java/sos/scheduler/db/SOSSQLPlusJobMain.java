package sos.scheduler.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.Basics.JSToolBox;

public class SOSSQLPlusJobMain extends JSToolBox {

    protected SOSSQLPlusJobOptions objOptions = null;
    private static final Logger LOGGER = LoggerFactory.getLogger(SOSSQLPlusJobMain.class);

    public final static void main(final String[] pstrArgs) {
        final String methodName = "SOSSQLPlusJobMain::Main";
        LOGGER.info("SOSSQLPlusJob - Main");
        try {
            SOSSQLPlusJob objM = new SOSSQLPlusJob();
            SOSSQLPlusJobOptions objO = objM.getOptions();
            objO.shell_command.setValue("sqlplus");
            objO.command_script_file.setValue("c:\\temp\\1.sql");
            objO.db_url.setValue("xe");
            objO.db_password.setValue("scheduler");
            objO.db_user.setValue("scheduler");
            objM.execute();
        } catch (Exception e) {
            LOGGER.error(methodName + ": " + "Error occured ..." + e.getMessage());
            int intExitCode = 99;
            LOGGER.error(String.format("JSJ-E-105: %1$s - terminated with exit-code %2$d", methodName, intExitCode), e);
            System.exit(intExitCode);
        }
        LOGGER.info(String.format("JSJ-I-106: %1$s - ended without errors", methodName));
        System.exit(0);
    }

}