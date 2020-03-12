package sos.scheduler.xsl;

import sos.scheduler.job.JobSchedulerJobAdapter;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.Exceptions.JobSchedulerException;

public class JobSchedulerXslTransformJSAdapterClass extends JobSchedulerJobAdapter {

    private final String conClassName = "JobSchedulerXslTransformationJSAdapterClass";					//$NON-NLS-1$
    private static Logger logger = LoggerFactory.getLogger(JobSchedulerXslTransformJSAdapterClass.class);

    public void init() {
        @SuppressWarnings("unused")
        final String conMethodName = conClassName + "::init"; //$NON-NLS-1$
        doInitialize();
    }

    private void doInitialize() {
    } // doInitialize

    @Override
    public boolean spooler_init() {
        @SuppressWarnings("unused")
        final String conMethodName = conClassName + "::spooler_init"; //$NON-NLS-1$
        return super.spooler_init();
    }

    @Override
    public boolean spooler_process() throws Exception {
        @SuppressWarnings("unused")
        final String conMethodName = conClassName + "::spooler_process"; //$NON-NLS-1$

        try {
            super.spooler_process();
            doProcessing();
            return getSpoolerProcess().getSuccess();
        } catch (Exception e) {
            logger.error(e.toString(), e);
            throw new JobSchedulerException(e);
        } finally {
        } // finally

    } // spooler_process

    @Override
    public void spooler_exit() {
        @SuppressWarnings("unused")
        final String conMethodName = conClassName + "::spooler_exit";
        super.spooler_exit();
    }

    private void doProcessing() throws Exception {
        @SuppressWarnings("unused")
        final String conMethodName = conClassName + "::doProcessing";

        JobSchedulerXslTransform objR = new JobSchedulerXslTransform();
        JobSchedulerXslTransformOptions objO = objR.getOptions();

        HashMap<String, String> params = getSchedulerParameterAsProperties();
        objO.setCurrentNodeName(this.getCurrentNodeName(getSpoolerProcess().getOrder(), true));
        objO.setAllOptions(params);

        objO.checkMandatory();
        objR.setJSJobUtilites(this);
        objR.Execute();
    } // doProcessing
}
