package com.sos.jitl.eventing;

import org.apache.log4j.Logger;

import sos.scheduler.job.JobSchedulerJobAdapter;
import sos.spooler.Supervisor_client;

// Super-Class for JobScheduler Java-API-Jobs
/**
 * \class 		JSEventsClientJSAdapterClass - JobScheduler Adapter for "Submit and Delete Events"
 *
 * \brief AdapterClass of JSEventsClient for the SOSJobScheduler
 *
 * This Class JSEventsClientJSAdapterClass works as an adapter-class between the SOS
 * JobScheduler and the worker-class JSEventsClient.
 *

 *
 * see \see C:\Users\KB\AppData\Local\Temp\scheduler_editor-4778075809216214864.html for more details.
 *
 * \verbatim ;
 * mechanicaly created by C:\ProgramData\sos-berlin.com\jobscheduler\latestscheduler\config\JOETemplates\java\xsl\JSJobDoc2JSAdapterClass.xsl from http://www.sos-berlin.com at 20130109134235
 * \endverbatim
 */
public class JSEventsClientJSAdapterClass extends JobSchedulerJobAdapter {
	private final String	conClassName	= "JSEventsClientJSAdapterClass";						//$NON-NLS-1$
	private static Logger	logger			= Logger.getLogger(JSEventsClientJSAdapterClass.class);

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
		}
		catch (Exception e) {
			logger.error(e.getLocalizedMessage());
			throw e;
		}
		finally {
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

		JSEventsClient objR = new JSEventsClient();
		JSEventsClientOptions objO = objR.Options();

		// Check Supervisor
		if (objO.scheduler_event_handler_host.isDirty() == true) {
			Supervisor_client supervisor = null;
			try {
				supervisor = spooler.supervisor_client();
				objO.scheduler_event_handler_host.Value(supervisor.hostname());
				objO.scheduler_event_handler_port.value(supervisor.tcp_port());
			}
			catch (Exception e) { // there is no supervisor
				objO.scheduler_event_handler_host.Value(spooler.hostname());
				objO.scheduler_event_handler_port.value(spooler.tcp_port());
			}
		}

        objR.setJSJobUtilites(this);
        objR.setJSCommands(this);
		objO.CurrentNodeName(this.getCurrentNodeName());
		objO.setAllOptions(getSchedulerParameterAsProperties(getJobOrOrderParameters()));
		objO.CheckMandatory();
		objR.setJSJobUtilites(this);
		objR.Execute();
	} // doProcessing

}
