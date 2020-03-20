package com.sos.jitl.agentbatchinstaller;

import sos.scheduler.job.JobSchedulerJobAdapter;

public class JSUniversalAgentBatchInstallerJSAdapterClass extends JobSchedulerJobAdapter {

    @Override
    public boolean spooler_init() {
        return super.spooler_init();
    }

    @Override
    public boolean spooler_process() throws Exception {
        super.spooler_process();
        doProcessing();
        return getSpoolerProcess().isOrderJob();
    } // spooler_process

    @Override
    public void spooler_exit() {
        super.spooler_exit();
    }

    private void doProcessing() throws Exception {
        JSUniversalAgentBatchInstaller jsUniversalAgentBatchInstaller = new JSUniversalAgentBatchInstaller();
        JSUniversalAgentBatchInstallerOptions jsUniversalAgentBatchInstallerOptions = jsUniversalAgentBatchInstaller.options();
        
        jsUniversalAgentBatchInstallerOptions.setAllOptions(getSchedulerParameterAsProperties(getSpoolerProcess().getOrder()));
        jsUniversalAgentBatchInstallerOptions.checkMandatory();
        jsUniversalAgentBatchInstaller.setJSJobUtilites(this);
        jsUniversalAgentBatchInstaller.setJSCommands(this);

        jsUniversalAgentBatchInstaller.execute();
    } // doProcessing
}
