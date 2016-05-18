package sos.scheduler.db;

import sos.scheduler.job.JobSchedulerJobAdapter;
import sos.spooler.Variable_set;

import com.sos.JSHelper.Exceptions.JobSchedulerException;

public class SOSSQLPlusJobJSAdapterClass extends JobSchedulerJobAdapter {

    @Override
    public boolean spooler_process() throws Exception {
        try {
            super.spooler_process();
            doProcessing();
        } catch (Exception e) {
            throw new JobSchedulerException("Fatal Error:" + e.getMessage(), e);
        }
        return signalSuccess();
    }

    private void doProcessing() throws Exception {
        SOSSQLPlusJob objR = new SOSSQLPlusJob();
        SOSSQLPlusJobOptions objO = objR.getOptions();
        objO.CurrentNodeName(this.getCurrentNodeName());
        Variable_set jobOrOrderParameters = getJobOrOrderParameters();
        String[] ignoreParams = new String[] { "ignore_sp2_messages", "ignore_ora_messages" };
        for (String ignoreParam : ignoreParams) {
            String value = jobOrOrderParameters.value(ignoreParam).toString();
            if (isNotEmpty(value) && value.matches("[,;|]") == false) {
                jobOrOrderParameters.set_value(ignoreParam, value + ";");
            }
        }
        objO.setAllOptions(getSchedulerParameterAsProperties());
        if (objO.command_script_file.isNotDirty()) {
            String strS = getJobScript();
            if (isNotEmpty(strS)) {
                objO.command_script_file.Value(strS);
            }
        }
        objO.setAllOptions(getSchedulerParameterAsProperties(jobOrOrderParameters));
        setJobScript(objO.command_script_file);
        objO.checkMandatory();
        objR.setJSJobUtilites(this);
        objR.Execute();
    }

}
