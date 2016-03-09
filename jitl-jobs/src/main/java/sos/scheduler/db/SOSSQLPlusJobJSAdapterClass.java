package sos.scheduler.db;

import java.util.HashMap;

import org.apache.log4j.Logger;

import sos.scheduler.job.JobSchedulerJobAdapter;
import sos.spooler.Variable_set;

import com.sos.JSHelper.Exceptions.JobSchedulerException;

public class SOSSQLPlusJobJSAdapterClass extends JobSchedulerJobAdapter {

    private final String conClassName = "SOSSQLPlusJobJSAdapterClass";						//$NON-NLS-1$
    @SuppressWarnings({ "unused", "hiding" })
    private static Logger logger = Logger.getLogger(SOSSQLPlusJobJSAdapterClass.class);

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
            throw new JobSchedulerException("Fatal Error:" + e.getMessage(), e);
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

        SOSSQLPlusJob objR = new SOSSQLPlusJob();
        SOSSQLPlusJobOptions objO = objR.getOptions();
        objO.CurrentNodeName(this.getCurrentNodeName());

        // oh JITL-93 in JSOptionValueList klappt im JUnit, aber nicht im
        // JobScheduler. Ich leg mir die Karten.
        // Daher hier nochmal ein Pflaster. Wenn die Parameter
        // "ignore_sp2_messages" oder "ignore_ora_messages"
        // gefuellt sind, aber nur einen Wert enthalten, dann wird ein ';' am
        // Ende hinzugefuegt.
        Variable_set jobOrOrderParameters = getJobOrOrderParameters();
        String[] ignoreParams = new String[] { "ignore_sp2_messages", "ignore_ora_messages" };
        for (String ignoreParam : ignoreParams) {
            String value = jobOrOrderParameters.value(ignoreParam).toString();
            if (isNotEmpty(value) && value.matches("[,;|]") == false) {
                jobOrOrderParameters.set_value(ignoreParam, value + ";");
            }
        }

        objO.setAllOptions(getSchedulerParameterAsProperties(jobOrOrderParameters));
        // TODO Use content of <script> tag of job as value of
        // command_script_file parameter
        // http://www.sos-berlin.com/jira/browse/JITL-49
        if (objO.command_script_file.isNotDirty()) {
            String strS = getJobScript();
            if (isNotEmpty(strS)) {
                objO.command_script_file.Value(strS);
            }
        }

        objO.setAllOptions(getSchedulerParameterAsProperties(jobOrOrderParameters));
        // TODO Use content of <script> tag of job as value of
        // command_script_file parameter
        // http://www.sos-berlin.com/jira/browse/JITL-49
        setJobScript(objO.command_script_file);

        objO.CheckMandatory();
        objR.setJSJobUtilites(this);
        objR.Execute();
    } // doProcessing

}
