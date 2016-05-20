package sos.scheduler.job;

import org.apache.log4j.Logger;

import com.sos.scheduler.converter.graphviz.JSObjects2Graphviz;
import com.sos.scheduler.converter.graphviz.JSObjects2GraphvizOptions;

public class JSObjects2GraphvizJSAdapterClass extends JobSchedulerJobAdapter {

    private final String conClassName = "JSObjects2GraphvizJSAdapterClass";						//$NON-NLS-1$
    private final Logger logger = Logger.getLogger(JSObjects2GraphvizJSAdapterClass.class);
    private final String conSVNVersion = "$Id: SOSSSHJob2JSAdapter.java 18220 2012-10-18 07:46:10Z kb $";

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
    public boolean spooler_process() {
        @SuppressWarnings("unused")
        final String conMethodName = conClassName + "::spooler_process"; //$NON-NLS-1$

        try {
            super.spooler_process();
            doProcessing();
        } catch (Exception e) {
            return signalFailure();
        } finally {
        } // finally
        return signalSuccess();

    } // spooler_process

    @Override
    public void spooler_exit() {
        @SuppressWarnings("unused")
        final String conMethodName = conClassName + "::spooler_exit"; //$NON-NLS-1$
        super.spooler_exit();
    }

    private void doProcessing() throws Exception {
        @SuppressWarnings("unused")
        final String conMethodName = conClassName + "::doProcessing"; //$NON-NLS-1$

        logger.info(conSVNVersion);

        JSObjects2Graphviz objR = new JSObjects2Graphviz();
        JSObjects2GraphvizOptions objO = objR.getOptions();
        objO.setAllOptions(getSchedulerParameterAsProperties(getJobOrOrderParameters()));
        objO.setCurrentNodeName(this.getCurrentNodeName());
        objO.checkMandatory();
        objR.setJSJobUtilites(this);
        objR.execute();
    } // doProcessing

}
