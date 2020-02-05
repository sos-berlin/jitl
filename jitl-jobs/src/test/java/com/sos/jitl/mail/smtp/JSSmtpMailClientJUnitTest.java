package com.sos.jitl.mail.smtp;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.sos.JSHelper.Basics.JSToolBox;
import com.sos.JSHelper.Listener.JSListenerClass;

/** \class JSSmtpMailClientJUnitTest - JUnit-Test for "Submit and Delete Events"
 *
 * \brief MainClass to launch JSSmtpMailClient as an executable command-line
 * program
 *
 * 
 *
 * see \see
 * C:\Users\KB\AppData\Local\Temp\scheduler_editor-4778075809216214864.html for
 * (more) details.
 *
 * \verbatim ; mechanicaly created by
 * C:\ProgramData\sos-berlin.com\jobscheduler\
 * latestscheduler\config\JOETemplates\java\xsl\JSJobDoc2JSJUnitClass.xsl from
 * http://www.sos-berlin.com at 20130109134235 \endverbatim */
public class JSSmtpMailClientJUnitTest extends JSToolBox {

    @SuppressWarnings("unused")
    private final static String conClassName = "JSSmtpMailClientJUnitTest";						//$NON-NLS-1$

    protected JSSmtpMailOptions objOptions = null;
    private JSSmtpMailClient objE = null;

    public JSSmtpMailClientJUnitTest() {
        //
    }

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
        objE = new JSSmtpMailClient();
        objOptions = objE.getOptions();
        JSListenerClass.bolLogDebugInformation = true;
        JSListenerClass.intMaxDebugLevel = 9;
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testExecute() throws Exception {

        objOptions.host.setValue("new.sos-berlin.com");
        objOptions.port.value(25);

        objOptions.from.setValue("JUnit-Test@sos-berlin.com");

        objOptions.body.setValue("bodobodododo\nfjfjfjfjfjjf\nfkfkfkfkfkfkf\nfjfjfjfjfjf\n %{host}  ");
        objOptions.subject.setValue("mail from JSSmtpMailClientJUnitTest %{host}\n  date = %{date}\n");
        objOptions.to.setValue("scheduler_test@sos-berlin.com");
        objOptions.cc.setValue("info@sos-berlin.com");
        objOptions.bcc.setValue("oh@sos-berlin.com");

        objE.Execute();
    }

    @Test
    public void testExecuteWithTaskLog() throws Exception {

        objOptions.host.setValue("new.sos-berlin.com");
        objOptions.port.value(25);
        objOptions.tasklog_to_body.value(true);
        objOptions.scheduler_port.value(4210);
        objOptions.scheduler_host.setValue("8of9.sos");
        objOptions.job_name.setValue("/attachNetDrive");
        objOptions.job_id.value(5584486);
        objOptions.from.setValue("JUnit-Test@sos-berlin.com");
        objOptions.body.setValue("Task-Protokoll von: %{job_name}:%{job_id}@%{scheduler_host}:%{scheduler_port}\n%{Log}\na line after the log");
        objOptions.subject.setValue("SOSJobScheduler: %{job_name} - %{job_title} - CC %{cc} ");
        objOptions.to.setValue("scheduler_test@sos-berlin.com");

        objE.Execute();
    }

} // class JSSmtpMailClientJUnitTest