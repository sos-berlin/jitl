

package com.sos.jitl.housekeeping.rotatelog;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Basics.JSToolBox;
import com.sos.scheduler.messages.JSMsg;

 
public class JobSchedulerRotateLogMain extends JSToolBox {
	// new Object() { }.getClass().getEnclosingClass()
//	private static final String conClassName = JobSchedulerRotateLogMain.class.getSimpleName();
	// see http://stackoverflow.com/questions/8275499/how-to-call-getclass-from-a-static-method-in-java
	private static Class<?> currentClass = new Object() { }.getClass().getEnclosingClass();
	private static final String conClassName = currentClass.getSimpleName();
	private static final Logger logger = Logger.getLogger(currentClass.getEnclosingClass());
	private static final String conSVNVersion = "$Id$";

	protected JobSchedulerRotateLogOptions	objOptions			= null;

	/**
	 * 
	 * \brief main
	 * 
	 * \details
	 *
	 * \return void
	 *
	 * @param pstrArgs
	 * @throws Exception
	 */
	public final static void main(String[] pstrArgs) {

		final String conMethodName = conClassName + "::Main"; //$NON-NLS-1$

		logger.info("JobSchedulerRotateLog - Main"); //$NON-NLS-1$
		logger.info(conSVNVersion);

		try {
			JobSchedulerRotateLog objM = new JobSchedulerRotateLog();
			JobSchedulerRotateLogOptions objO = objM.getOptions();
			
			objO.AllowEmptyParameterList.setFalse();
			objO.ApplicationName.Value("JITL");
			objO.ApplicationDocuUrl.Value("http://www.sos-berlin.com/jitl/JobSchedulerRotateLog.xml");
			
			objO.CommandLineArgs(pstrArgs);
            objM.executeDebugLog();
            objM.executeMainLog();
		}
		
		catch (Exception e) {
			System.err.println(conMethodName + ": " + "Error occured ..." + e.getMessage()); 
			e.printStackTrace(System.err);
			int intExitCode = 99;
			logger.error(String.format(new JSMsg("JSJ-E-105").get(), conMethodName, intExitCode), e);		
			System.exit(intExitCode);
		}
		
		logger.info(String.format(new JSMsg("JSJ-I-106").get(), conMethodName));		
	}

}  // class JobSchedulerRotateLogMain