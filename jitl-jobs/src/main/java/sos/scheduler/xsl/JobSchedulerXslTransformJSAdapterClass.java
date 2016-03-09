package sos.scheduler.xsl;

import org.apache.log4j.Logger;

import sos.scheduler.job.JobSchedulerJobAdapter;

import com.sos.JSHelper.Exceptions.JobSchedulerException;

public class JobSchedulerXslTransformJSAdapterClass extends JobSchedulerJobAdapter {

    private final String conClassName = "JobSchedulerXslTransformationJSAdapterClass";					//$NON-NLS-1$
    private static Logger logger = Logger.getLogger(JobSchedulerXslTransformJSAdapterClass.class);

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
        } catch (Exception e) {
            logger.fatal(StackTrace2String(e));
            throw new JobSchedulerException(e);
        } finally {
        } // finally
        return signalSuccess();

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
        objO.CurrentNodeName(getCurrentNodeName());

        hsmParameters = getSchedulerParameterAsProperties(getJobOrOrderParameters());
        objO.CurrentNodeName(this.getCurrentNodeName());
        objO.setAllOptions(hsmParameters);

        objO.CheckMandatory();
        objR.setJSJobUtilites(this);
        objR.Execute();
    } // doProcessing
}
