

package com.sos.jitl.eventing.checkevents;

import java.util.HashMap;

import com.sos.jitl.eventing.checkevents.JobSchedulerCheckEvents;
import com.sos.jitl.eventing.checkevents.JobSchedulerCheckEventsOptions;
import sos.spooler.Order;
import sos.spooler.Variable_set;
import sos.scheduler.job.JobSchedulerJobAdapter;  // Super-Class for JobScheduler Java-API-Jobs
import org.apache.log4j.Logger;
import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.localization.*;
/**
 * \class 		JobSchedulerCheckEventsJSAdapterClass - JobScheduler Adapter for "Check if events exist"
 *
 * \brief AdapterClass of JobSchedulerCheckEvents for the SOSJobScheduler
 *
 * This Class JobSchedulerCheckEventsJSAdapterClass works as an adapter-class between the SOS
 * JobScheduler and the worker-class JobSchedulerCheckEvents.
 *

 *
 *
 * \verbatim ;
 * \endverbatim
 */
public class JobSchedulerCheckEventsJSAdapterClass extends JobSchedulerJobAdapter  {
	private final String					conClassName						= "JobSchedulerCheckEventsJSAdapterClass";
	private static Logger		logger			= Logger.getLogger(JobSchedulerCheckEventsJSAdapterClass.class);

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

		JobSchedulerCheckEvents objR = new JobSchedulerCheckEvents();
		JobSchedulerCheckEventsOptions objO = objR.Options();

        objO.CurrentNodeName(this.getCurrentNodeName());
		objO.setAllOptions(getSchedulerParameterAsProperties(getJobOrOrderParameters()));
		objO.CheckMandatory();
        objR.setJSJobUtilites(this);
		objR.Execute();
	} // doProcessing

}

