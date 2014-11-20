package com.sos.jitl.mail.smtp;

import org.apache.log4j.Logger;

// Super-Class for JobScheduler Java-API-Jobs
/**
 * \class 		JSJSSmtpMailClientAdapterClass - JobScheduler Adapter for "Submit and Delete Events"
 *
 * \brief AdapterClass of JSJSSmtpMailClient for the SOSJobScheduler
 *
 * This Class JSJSSmtpMailClientAdapterClass works as an adapter-class between the SOS
 * JobScheduler and the worker-class JSJSSmtpMailClient.
 *


<order  title="Send Mails using pre-/Post-Processing">
    <params >
        <param  name="sendMail/MailOnJobStart_to" value="kb@sos-berlin.com"/>
        <param  name="sendMail/MailOnJobStart_subject" value="MailOnJobStart_Test 4  %SCHEDULER_JOB_NAME% - %SCHEDULER_JOB_TITLE% "/>
        <param  name="sendMail/MailOnJobStart_body" value="This is a test"/>
		
        <param  name="process2/MailOnError_to" value="kb@sos-berlin.com"/>
        <param  name="process2/MailOnError_subject" value="MailOnJobError_Test %SCHEDULER_JOB_NAME% - %SCHEDULER_JOB_TITLE% - CC %CC%"/>
        <param  name="process2/MailOnError_body" value="This is a test"/>
	
        <param  name="process3/MailOnSuccess_to" value="kb@sos-berlin.com"/>
        <param  name="process3/MailOnSuccess_subject" value="MailOnJobSuccess_Test %time% %SCHEDULER_JOB_NAME% ended with success"/>
        <param  name="process3/MailOnSuccess_body" value="This is a test"/>

        <param  name="to" value="kb@sos-berlin.com"/>
        <param  name="subject" value="Test 4 mail send using pre and post-processing"/>
        <param  name="body" value="This is a test"/>
        <param  name="attachment" value="C:\temp\JobSchedulerShout.xml.bak"/>

        <param  name="host" value="smtp.sos"/>
        <param  name="port" value="25"/>
        <param  name="smtp_user" value="kb"/>
        <param  name="smtp_password" value="kb"/>
    </params>

    <run_time  let_run="no"/>
</order>

 *
 * see \see C:\Users\KB\AppData\Local\Temp\scheduler_editor-4778075809216214864.html for more details.
 *
 * \verbatim ;
 * mechanicaly created by C:\ProgramData\sos-berlin.com\jobscheduler\latestscheduler\config\JOETemplates\java\xsl\JSJobDoc2JSAdapterClass.xsl from http://www.sos-berlin.com at 20130109134235
 * \endverbatim
 */
public class JSSmtpMailClientAdapterClass extends JSSmtpMailClientBaseClass {
	private final String	conClassName	= "JSSmtpMailClientAdapterClass";						//$NON-NLS-1$
	private static Logger	logger			= Logger.getLogger(JSSmtpMailClientAdapterClass.class);

	public void init() {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::init"; //$NON-NLS-1$
		doInitialize();
	}

	private void doInitialize() {
	} // doInitialize

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
}
