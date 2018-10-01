package sos.scheduler.InstallationService;

import java.util.HashMap;

import sos.scheduler.job.JobSchedulerJobAdapter;

public class JSBatchInstallerJSAdapterClass extends JobSchedulerJobAdapter {


    @Override
    public boolean spooler_process() throws Exception {
        try {
            super.spooler_process();
            doProcessing();
        } catch (Exception e) {
            return false;
        }
        return spooler_task.job().order_queue() != null;
    }

    private void doProcessing() throws Exception {
        JSBatchInstaller objR = new JSBatchInstaller();
        JSBatchInstallerOptions objO = objR.Options();
        HashMap<String, String> hshT = null;
        hshT = getSchedulerParameterAsProperties(getParameters());
        objO.setAllOptions(hshT);
        objO.checkMandatory();
        objR.setJSJobUtilites(this);
        objR.setJSCommands(this);
        objR.Execute();
    }

}