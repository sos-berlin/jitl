package com.sos.jitl.eventing;

import org.apache.log4j.Logger;

 
public class EventMonitorTaskBefore extends JSEventsClientBaseClass {
	private final String	conClassName	= "EventMonitorTaskBefore";						//$NON-NLS-1$
	private static Logger	logger			= Logger.getLogger(EventMonitorTaskBefore.class);

	@Override
	public boolean spooler_task_before() throws Exception {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::spooler_task_before"; //$NON-NLS-1$

		try {
			super.spooler_process();
			doProcessing();
		}
		catch (Exception e) {
			e.printStackTrace(System.err);
			logger.error(e.getLocalizedMessage());
			throw e;
		}
		finally {
		} // finally
		return continue_with_task;   // Task can start

	} // spooler_task_before


}
