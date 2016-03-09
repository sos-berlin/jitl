package sos.scheduler.InstallationService;

import java.util.HashMap;

import org.apache.log4j.Logger;

import sos.scheduler.job.JobSchedulerJobAdapter;
import sos.spooler.Variable_set;

public class JSBatchInstallerJSAdapterClass extends JobSchedulerJobAdapter {

    private final String conClassName = "JSBatchInstallerJSAdapterClass";						//$NON-NLS-1$

    // private static Logger logger =
    // Logger.getLogger(JSBatchInstallerJSAdapterClass.class);

    public void init() {
        @SuppressWarnings("unused")//$NON-NLS-1$
        final String conMethodName = conClassName + "::init"; //$NON-NLS-1$
        doInitialize();
    }

    private void doInitialize() {
    } // doInitialize

    @Override
    public boolean spooler_init() {
        @SuppressWarnings("unused")//$NON-NLS-1$
        final String conMethodName = conClassName + "::spooler_init"; //$NON-NLS-1$
        return super.spooler_init();
    }

    @Override
    public boolean spooler_process() throws Exception {
        @SuppressWarnings("unused")//$NON-NLS-1$
        final String conMethodName = conClassName + "::spooler_process"; //$NON-NLS-1$
        try {
            super.spooler_process();
            doProcessing();
        } catch (Exception e) {
            return false;
        } finally {
        } // finally
          // return value for classic and order driven processing
          // TODO create method in base-class for this functionality
        return (spooler_task.job().order_queue() != null);
    } // spooler_process

    @Override
    public void spooler_exit() {
        @SuppressWarnings("unused")//$NON-NLS-1$
        final String conMethodName = conClassName + "::spooler_exit"; //$NON-NLS-1$
        super.spooler_exit();
    }

    private void doProcessing() throws Exception {
        @SuppressWarnings("unused")//$NON-NLS-1$
        final String conMethodName = conClassName + "::doProcessing"; //$NON-NLS-1$
        JSBatchInstaller objR = new JSBatchInstaller();
        JSBatchInstallerOptions objO = objR.Options();
        Variable_set varT = getParameters();
        HashMap<String, String> hshT = null;
        hshT = getSchedulerParameterAsProperties(varT);
        objO.setAllOptions(hshT);
        objO.CheckMandatory();
        objR.setJSJobUtilites(this);
        objR.setJSCommands(this);

        objR.Execute();
    } // doProcessing
}
