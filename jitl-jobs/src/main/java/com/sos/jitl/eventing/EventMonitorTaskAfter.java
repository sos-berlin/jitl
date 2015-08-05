package com.sos.jitl.eventing;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Exceptions.JobSchedulerException;

 
public class EventMonitorTaskAfter extends JSEventsClientBaseClass {
	private final String	conClassName	= "EventMonitorTaskAfter";
	private static Logger	logger			= Logger.getLogger(EventMonitorTaskAfter.class);

	@Override
	public void spooler_task_after() throws Exception {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::spooler_process";

		try {
			super.spooler_init();
			super.spooler_process();
			doProcessing();
		}
		catch (Exception e) {
			e.printStackTrace(System.err);
			logger.error(e.getLocalizedMessage());
			throw new JobSchedulerException(e.getLocalizedMessage(), e);
		}
		finally {
		} // finally
	} // spooler_process
}
