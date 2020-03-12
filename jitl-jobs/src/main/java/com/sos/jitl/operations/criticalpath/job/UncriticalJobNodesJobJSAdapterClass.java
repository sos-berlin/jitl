package com.sos.jitl.operations.criticalpath.job;

import sos.scheduler.job.JobSchedulerJobAdapter;

import com.sos.JSHelper.Exceptions.JobSchedulerException;

public class UncriticalJobNodesJobJSAdapterClass extends JobSchedulerJobAdapter {

    @Override
    public boolean spooler_process() throws Exception {
        try {
            super.spooler_process();

            UncriticalJobNodesJob job = new UncriticalJobNodesJob();
            UncriticalJobNodesJobOptions options = job.getOptions();
            options.setCurrentNodeName(this.getCurrentNodeName(getSpoolerProcess().getOrder(), true));
            options.setAllOptions(getSchedulerParameterAsProperties(getJobOrOrderParameters(getSpoolerProcess().getOrder())));
            job.setJSJobUtilites(this);
            job.setJSCommands(this);
            job.setSpooler(spooler);

            job.execute();
            return getSpoolerProcess().getSuccess();
        } catch (Exception e) {
            throw new JobSchedulerException("Fatal Error:" + e.getMessage(), e);
        }
    }

}
