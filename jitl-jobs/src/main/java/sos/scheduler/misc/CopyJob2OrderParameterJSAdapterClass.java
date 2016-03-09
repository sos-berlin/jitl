package sos.scheduler.misc;

import java.util.HashMap;

import sos.scheduler.misc.CopyJob2OrderParameter;
import sos.scheduler.misc.CopyJob2OrderParameterOptions;
import sos.spooler.Order;
import sos.spooler.Variable_set;
import sos.scheduler.job.JobSchedulerJobAdapter;  // Super-Class for JobScheduler
                                                 // Java-API-Jobs
import org.apache.log4j.Logger;
import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.localization.*;

public class CopyJob2OrderParameterJSAdapterClass extends JobSchedulerJobAdapter {

    private final String conClassName = "CopyJob2OrderParameterJSAdapterClass";  //$NON-NLS-1$
    private static Logger logger = Logger.getLogger(CopyJob2OrderParameterJSAdapterClass.class);

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
            // e.printStackTrace();
            logger.error(e.toString(), e);
            return false;
        }
        // finally {
        // }
        return signalSuccess();

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

        CopyJob2OrderParameter objR = new CopyJob2OrderParameter();
        CopyJob2OrderParameterOptions objO = objR.Options();
        objO.setAllOptions(getSchedulerParameterAsProperties(getParameters()));
        objO.CheckMandatory();
        objR.setJSJobUtilites(this);
        objR.Execute();
    } // doProcessing

}
