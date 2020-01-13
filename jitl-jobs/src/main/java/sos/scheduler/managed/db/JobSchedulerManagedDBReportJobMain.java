package sos.scheduler.managed.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.Basics.JSToolBox;

/** @author KB */
public class JobSchedulerManagedDBReportJobMain extends JSToolBox {

    protected JobSchedulerManagedDBReportJobOptions objOptions = null;
    private static final Logger LOGGER = LoggerFactory.getLogger(JobSchedulerManagedDBReportJobMain.class);

    public final static void main(String[] pstrArgs) {
        final String methodName = "JobSchedulerManagedDBReportJobMain::Main";
        LOGGER.info("JobSchedulerManagedDBReportJob - Main");
        try {
            JobSchedulerManagedDBReportJob objM = new JobSchedulerManagedDBReportJob();
            JobSchedulerManagedDBReportJobOptions objO = objM.Options();
            objO.commandLineArgs(pstrArgs);
            objM.Execute();
        } catch (Exception e) {
            LOGGER.error(methodName + ": " + "Error occured ..." + e.getMessage());
            int intExitCode = 99;
            LOGGER.error(String.format("JSJ-E-105: %1$s - terminated with exit-code %2$d", methodName, intExitCode), e);
            System.exit(intExitCode);
        }
        LOGGER.info(String.format("JSJ-I-106: %1$s - ended without errors", methodName));
    }

}