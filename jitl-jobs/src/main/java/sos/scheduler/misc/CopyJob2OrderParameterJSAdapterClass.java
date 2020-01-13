package sos.scheduler.misc;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sos.scheduler.job.JobSchedulerJobAdapter;

public class CopyJob2OrderParameterJSAdapterClass extends JobSchedulerJobAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(CopyJob2OrderParameterJSAdapterClass.class);

    @Override
    public boolean spooler_process() throws Exception {
        try {
            super.spooler_process();
            doProcessing();
        } catch (Exception e) {
            LOGGER.error(e.toString(), e);
            return false;
        }
        return signalSuccess();
    }

    private void doProcessing() throws Exception {
        CopyJob2OrderParameter objR = new CopyJob2OrderParameter();
        CopyJob2OrderParameterOptions objO = objR.Options();
        objO.setAllOptions(getSchedulerParameterAsProperties(getParameters()));
        objO.checkMandatory();
        objR.setJSJobUtilites(this);
        objR.Execute();
    }

}