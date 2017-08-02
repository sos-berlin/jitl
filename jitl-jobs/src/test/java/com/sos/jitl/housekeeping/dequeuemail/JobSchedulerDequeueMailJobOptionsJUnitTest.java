package com.sos.jitl.housekeeping.dequeuemail;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.sos.JSHelper.Basics.JSToolBox;
import com.sos.JSHelper.Listener.JSListenerClass;

/** \class JobSchedulerDequeueMailJobOptionsJUnitTest - Dequeues Mails
 *
 * \brief */

public class JobSchedulerDequeueMailJobOptionsJUnitTest extends JSToolBox {

    private final String conClassName = "JobSchedulerDequeueMailJobOptionsJUnitTest"; //$NON-NLS-1$
    @SuppressWarnings("unused")//$NON-NLS-1$
    private static Logger logger = Logger.getLogger(JobSchedulerDequeueMailJobOptionsJUnitTest.class);
    private JobSchedulerDequeueMailJob objE = null;

    protected JobSchedulerDequeueMailJobOptions objOptions = null;

    public JobSchedulerDequeueMailJobOptionsJUnitTest() {
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
        objE = new JobSchedulerDequeueMailJob();
        objE.registerMessageListener(this);
        objOptions = objE.getOptions();
        objOptions.registerMessageListener(this);

        JSListenerClass.bolLogDebugInformation = true;
        JSListenerClass.intMaxDebugLevel = 9;
    }

    @After
    public void tearDown() throws Exception {
    }

    /** \brief testdb :
     * 
     * \details This setting states that a database is used and that in order to
     * update their shipping state, mails are to be sought in a table. */
    @Test
    public void testdb() {  // SOSOptionString
        objOptions.db.setValue("++false++");
        assertEquals("", objOptions.db.getValue(), "++false++");

    }

    /** \brief testfile :
     * 
     * \details This parameter provides the name of the file containing a mail
     * to be dequeued. The path is not specified with the filename but in the
     * queue_directory job parameter. */
    @Test
    public void testfile() {  // SOSOptionString
        objOptions.file.setValue("++----++");
        assertEquals("", objOptions.file.getValue(), "++----++");

    }

    /** \brief testmax_delivery :
     * 
     * \details This parameter specifies the maximum number of attempts to be
     * made to send an email. If an email is sent then an X-Header named
     * X-SOSMail-delivery-counter with the value of the current number of trials
     * is created. If the value of this parameter is 0, then an infinite number
     * of attempts may be made to send a mail. For other values the shipment
     * will be cancelled once this number of attempts has been reached. In this
     * case, the mail will be stored in the dequeue directory in a file with the
     * prefix failed. . */
    @Test
    public void testmax_delivery() {  // SOSOptionString
        objOptions.maxDelivery.setValue("++0++");
        assertEquals("", objOptions.maxDelivery.getValue(), "++0++");

    }

    /** \brief testqueue_directory :
     * 
     * \details This parameter contains the name of the directory in which mails
     * have been stored. If this value is left blank then the job will use the
     * dequeueing directory that was configured for the JobScheduler and which
     * is returned by the API Mail.dequeue() method. */
    @Test
    public void testqueue_directory() {  // SOSOptionString
        objOptions.queueDirectory.setValue("++Mail.dequeue()++");
        assertEquals("", objOptions.queueDirectory.getValue(), "++Mail.dequeue()++");

    }

    /** \brief testqueue_prefix :
     * 
     * \details If an email cannot be sent due to mail server problems, then it
     * will be stored as a file. This prefix is then used in the file name. */
    @Test
    public void testqueue_prefix() {  // SOSOptionString
        objOptions.queuePrefix.setValue("++sos.++");
        assertEquals("", objOptions.queuePrefix.getValue(), "++sos.++");

    }

    /** \brief testqueue_prefix_spec :
     * 
     * \details This parameter contains a regular expression to specify the
     * files that should be dequeued. The parameter is ignored if this job is
     * triggered by an order. */
    @Test
    public void testqueue_prefix_spec() {  // SOSOptionString
        objOptions.queuePrefixSpec.setValue("++^(sos.*)(?&lt;!\\~)$++");
        assertEquals("", objOptions.queuePrefixSpec.getValue(), "++^(sos.*)(?&lt;!\\~)$++");

    }

} // public class JobSchedulerDequeueMailJobOptionsJUnitTest