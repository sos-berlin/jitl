package com.sos.jitl.textprocessor;

import sos.scheduler.job.JobSchedulerJobAdapter;

import com.sos.JSHelper.Exceptions.JobSchedulerException;

public class JobSchedulerTextProcessorJSAdapterClass extends JobSchedulerJobAdapter {

    private static final String conReturnParameterSCHEDULER_TEXTPROCESSOR_PARAM = "scheduler_textprocessor_param";
    private static final String conReturnParameterSCHEDULER_TEXTPROCESSOR_COMMAND = "scheduler_textprocessor_command";
    private static final String conReturnParameterSCHEDULER_TEXTPROCESSOR_RESULT = "scheduler_textprocessor_result";
    private static final String conReturnParameterSCHEDULER_TEXTPROCESSOR_FILENAME = "scheduler_textprocessor_filename";

    @Override
    public boolean spooler_process() throws Exception {
        try {
            super.spooler_process();
            doProcessing();
            return getSpoolerProcess().getSuccess();
        } catch (Exception e) {
            throw new JobSchedulerException("Fatal Error:" + e.getMessage(), e);
        }
    }

    private void doProcessing() throws Exception {
        JobSchedulerTextProcessor jobSchedulerTextProcessor = new JobSchedulerTextProcessor();
        JobSchedulerTextProcessorOptions jobSchedulerTextProcessorOptions = jobSchedulerTextProcessor.getOptions();
        jobSchedulerTextProcessorOptions.setCurrentNodeName(this.getCurrentNodeName(getSpoolerProcess().getOrder(),true));
        jobSchedulerTextProcessorOptions.setAllOptions(getSchedulerParameterAsProperties(getSpoolerProcess().getOrder()));
        jobSchedulerTextProcessorOptions.checkMandatory();
        jobSchedulerTextProcessor.setJSJobUtilites(this);
        if (spooler_job.order_queue() != null) {
            spooler_task.order().params().set_var(conReturnParameterSCHEDULER_TEXTPROCESSOR_RESULT, jobSchedulerTextProcessorOptions.result.getValue());
            spooler_task.order().params().set_var(conReturnParameterSCHEDULER_TEXTPROCESSOR_COMMAND, jobSchedulerTextProcessorOptions.command.getValue());
            spooler_task.order().params().set_var(conReturnParameterSCHEDULER_TEXTPROCESSOR_PARAM, jobSchedulerTextProcessorOptions.param.getValue());
        }
        jobSchedulerTextProcessor.Execute();
        spooler_task.order().params().set_var(conReturnParameterSCHEDULER_TEXTPROCESSOR_FILENAME, jobSchedulerTextProcessorOptions.filename.getValue());
    }
}