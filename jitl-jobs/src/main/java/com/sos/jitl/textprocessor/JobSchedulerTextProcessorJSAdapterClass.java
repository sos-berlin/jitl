

package com.sos.jitl.textprocessor;

import java.io.File;

import com.sos.jitl.textprocessor.JobSchedulerTextProcessor;
import com.sos.jitl.textprocessor.JobSchedulerTextProcessorOptions;
import sos.scheduler.job.JobSchedulerJobAdapter;  // Super-Class for JobScheduler Java-API-Jobs
import sos.spooler.Variable_set;
import org.apache.log4j.Logger;
import com.sos.JSHelper.Basics.VersionInfo;
import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.localization.*;

public class JobSchedulerTextProcessorJSAdapterClass extends JobSchedulerJobAdapter  {
   
	private final String					conClassName						= "JobSchedulerTextProcessorJSAdapterClass";
    private static final String conReturnParameterSCHEDULER_TEXTPROCESSOR_PARAM     = "scheduler_textprocessor_param";
    private static final String conReturnParameterSCHEDULER_TEXTPROCESSOR_COMMAND   = "scheduler_textprocessor_command";
    private static final String conReturnParameterSCHEDULER_TEXTPROCESSOR_RESULT    = "scheduler_textprocessor_result";
    private static final String conReturnParameterSCHEDULER_TEXTPROCESSOR_FILENAME  = "scheduler_textprocessor_filename";

	
	private static Logger		logger			= Logger.getLogger(JobSchedulerTextProcessorJSAdapterClass.class);

	public void init() {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::init";
		doInitialize();
	}

	private void doInitialize() {
	} // doInitialize

	@Override
	public boolean spooler_init() {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::spooler_init";
		return super.spooler_init();
	}

	@Override
	public boolean spooler_process() throws Exception {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::spooler_process";

		try {
			super.spooler_process();
			doProcessing();
		}
		catch (Exception e) {
            throw new JobSchedulerException("Fatal Error:" + e.getMessage(), e);
   		}
		finally {
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

		JobSchedulerTextProcessor jobSchedulerTextProcessor = new JobSchedulerTextProcessor();
		JobSchedulerTextProcessorOptions jobSchedulerTextProcessorOptions = jobSchedulerTextProcessor.Options();

        jobSchedulerTextProcessorOptions.CurrentNodeName(this.getCurrentNodeName());
		jobSchedulerTextProcessorOptions.setAllOptions(getSchedulerParameterAsProperties(getJobOrOrderParameters()));
		jobSchedulerTextProcessorOptions.CheckMandatory();
        jobSchedulerTextProcessor.setJSJobUtilites(this);
        
        if (spooler_job.order_queue() != null) {
            spooler_task.order().params().set_var(conReturnParameterSCHEDULER_TEXTPROCESSOR_RESULT, jobSchedulerTextProcessorOptions.result.Value());
            spooler_task.order().params().set_var(conReturnParameterSCHEDULER_TEXTPROCESSOR_COMMAND, jobSchedulerTextProcessorOptions.command.Value());
            spooler_task.order().params().set_var(conReturnParameterSCHEDULER_TEXTPROCESSOR_PARAM, jobSchedulerTextProcessorOptions.param.Value());
        }
		jobSchedulerTextProcessor.Execute();
        spooler_task.order().params().set_var(conReturnParameterSCHEDULER_TEXTPROCESSOR_FILENAME, jobSchedulerTextProcessorOptions.filename.Value());

	} // doProcessing

}

