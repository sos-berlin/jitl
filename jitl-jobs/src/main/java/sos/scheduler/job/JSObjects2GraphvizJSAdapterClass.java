package sos.scheduler.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.graphviz.main.JSObjects2Graphviz;
import com.sos.graphviz.main.JSObjects2GraphvizOptions;

public class JSObjects2GraphvizJSAdapterClass extends JobSchedulerJobAdapter {

    private final String conClassName = "JSObjects2GraphvizJSAdapterClass";						//$NON-NLS-1$
    private static final Logger LOGGER = LoggerFactory.getLogger(JSObjects2GraphvizJSAdapterClass.class);
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
            return getSpoolerProcess().getSuccess();
        } catch (Exception e) {
            throw new JobSchedulerException(e);
        }

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

        LOGGER.info(conSVNVersion);

        JSObjects2Graphviz objR = new JSObjects2Graphviz();
        JSObjects2GraphvizOptions objO = objR.getOptions();
        objO.setAllOptions(getSchedulerParameterAsProperties(getSpoolerProcess().getOrder()));
        objO.setCurrentNodeName(getCurrentNodeName(getSpoolerProcess().getOrder(), true));
        objO.checkMandatory();
        objR.setJSJobUtilites(this);
        objR.execute();
    } // doProcessing

}
