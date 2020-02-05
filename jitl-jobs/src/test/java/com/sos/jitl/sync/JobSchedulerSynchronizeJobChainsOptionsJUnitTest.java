package com.sos.jitl.sync;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.sos.JSHelper.Basics.JSToolBox;
import com.sos.JSHelper.Listener.JSListenerClass;

/** \class JobSchedulerSynchronizeJobChainsOptionsJUnitTest - Synchronize Job
 * Chains
 *
 * \brief
 *
 *
 * 
 *
 * 
 * \verbatim ; mechanicaly created by
 * C:\ProgramData\sos-berlin.com\jobscheduler\
 * scheduler_ur\config\JOETemplates\java
 * \xsl\JSJobDoc2JSJUnitOptionSuperClass.xsl from http://www.sos-berlin.com at
 * 20121217120436 \endverbatim
 *
 * \section TestData Eine Hilfe zum Erzeugen einer HashMap mit Testdaten
 *
 * Die folgenden Methode kann verwendet werden, um für einen Test eine HashMap
 * mit sinnvollen Werten für die einzelnen Optionen zu erzeugen.
 *
 * \verbatim private HashMap <String, String> SetJobSchedulerSSHJobOptions
 * (HashMap <String, String> pobjHM) { pobjHM.put
 * ("		JobSchedulerSynchronizeJobChainsOptionsJUnitTest.auth_file", "test"); //
 * This parameter specifies the path and name of a user's pr return pobjHM; } //
 * private void SetJobSchedulerSSHJobOptions (HashMap <String, String> pobjHM)
 * \endverbatim */
public class JobSchedulerSynchronizeJobChainsOptionsJUnitTest extends JSToolBox {

    private final String conClassName = "JobSchedulerSynchronizeJobChainsOptionsJUnitTest"; //$NON-NLS-1$
    private JobSchedulerSynchronizeJobChains objE = null;

    protected JobSchedulerSynchronizeJobChainsOptions objOptions = null;

    public JobSchedulerSynchronizeJobChainsOptionsJUnitTest() {
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
        objE = new JobSchedulerSynchronizeJobChains();
        objE.registerMessageListener(this);
        objOptions = objE.getOptions();
        objOptions.registerMessageListener(this);

        JSListenerClass.bolLogDebugInformation = true;
        JSListenerClass.intMaxDebugLevel = 9;
    }

    @After
    public void tearDown() throws Exception {
    }

    /** \brief testjob_chain_required_orders :
     * 
     * \details This parameter specifies the number of orders that are required
     * to be present for a job chain to make orders proceed. Without
     * specification one order is expected to be present. If e.g. three orders
     * are specified then three orders from that chain have to be present and
     * these three orders will simultaneously proceed having matched the
     * synchronization criteria. The name of this parameter is created from the
     * name of the respective job chain and the suffix _required_orders . */
    @Test
    public void testjob_chain_required_orders() {  // SOSOptionInteger
        objOptions.job_chain_required_orders.setValue("12345");
        assertEquals("", objOptions.job_chain_required_orders.getValue(), "12345");
        assertEquals("", objOptions.job_chain_required_orders.value(), 12345);
        objOptions.job_chain_required_orders.value(12345);
        assertEquals("", objOptions.job_chain_required_orders.getValue(), "12345");
        assertEquals("", objOptions.job_chain_required_orders.value(), 12345);

    }

    /** \brief testjob_chain_state_required_orders :
     * 
     * \details This parameter specifies the number of orders that are required
     * to be present for certain state a job chain to make orders proceed.
     * Without specification one order is expected to be present. If e.g. three
     * orders are specified then three orders from that chain have to be present
     * and these three orders will simultaneously proceed having matched the
     * synchronization criteria. The name of this parameter is created from the
     * name of the respective job chain, a ";" , the name of the state and the
     * suffix _required_orders . */
    @Test
    public void testjob_chain_state_required_orders() {  // SOSOptionString
        objOptions.job_chain_state_required_orders.setValue("++1++");
        assertEquals("", objOptions.job_chain_state_required_orders.getValue(), "++1++");

    }

    /** \brief testrequired_orders :
     * 
     * \details This parameter specifies the number of orders that are required
     * to be present for each job chain to make orders proceed. Without
     * specification one order is expected to be present. If e.g. three orders
     * are specified then three orders from that chain have to be present and
     * these three orders will simultaneously proceed having matched the
     * synchronization criteria. This parameter is considered only if no
     * parameter [job_chain]_required_orders has been specified for the current
     * job chain. */
    @Test
    public void testrequired_orders() {  // SOSOptionString
        objOptions.required_orders.setValue("++1++");
        assertEquals("", objOptions.required_orders.getValue(), "++1++");

    }

    /** \brief testsetback_count :
     * 
     * \details This parameter can be used with the parameter setback_type and
     * its value setback to specify the maximum number of trials to set back
     * orders that do not match the synchronization criteria. By default the
     * setback_type suspend will be used that suspends orders and therefore
     * would not require an interval. For better visibility it is recommended to
     * set this value using the element <delay _order_after_setback> instead. */
    @Test
    public void testsetback_count() {  // SOSOptionInteger
        objOptions.setback_count.setValue("12345");
        assertEquals("", objOptions.setback_count.getValue(), "12345");
        assertEquals("", objOptions.setback_count.value(), 12345);
        objOptions.setback_count.value(12345);
        assertEquals("", objOptions.setback_count.getValue(), "12345");
        assertEquals("", objOptions.setback_count.value(), 12345);

    }

    /** \brief testsetback_interval :
     * 
     * \details This parameter can be used with the parameter setback_type and
     * its value setback to specify the interval in seconds, for which orders
     * are being set back that do not match the synchronization criteria. By
     * default the setback_type suspend will be used that suspends orders and
     * therefore would not require an interval. For better visibility it is
     * recommended to set this value using the element <delay
     * _order_after_setback> instead. */
    @Test
    public void testsetback_interval() {  // SOSOptionInteger
        objOptions.setback_interval.setValue("12345");
        assertEquals("", objOptions.setback_interval.getValue(), "12345");
        assertEquals("", objOptions.setback_interval.value(), 12345);
        objOptions.setback_interval.value(12345);
        assertEquals("", objOptions.setback_interval.getValue(), "12345");
        assertEquals("", objOptions.setback_interval.value(), 12345);

    }

    /** \brief testsetback_type :
     * 
     * \details This parameter can be used in order to choose between suspend
     * and setback for the handling of waiting orders: suspend Orders are
     * suspended if the synchronization criteria were not matched. Such orders
     * remain in this state for an arbitrary duration provided that they were
     * not continued by the synchronization job. Alternatively such orders can
     * be continued manually in the Web GUI. setback Orders are repeatedly
     * executed as specified by the parameters setback_interval and
     * setback_count . Should the specified interval and frequency be exceeded
     * then the order enters an error state and might leave the job chain.
     * Alternatively such orders can be continued manually in the Web GUI. */
    @Test
    public void testsetback_type() {  // SOSOptionSetBack
        objOptions.setback_type.setValue("++suspend++");
        assertEquals("", objOptions.setback_type.getValue(), "++suspend++");

    }

    /** \brief testsync_session_id :
     * 
     * \details If an order has the sync_session_id parameter, then this order
     * will only be synchronized with orders which have the same value for the
     * sync_session_id parameter. This is required if multiple groups of
     * parallel orders run through parallel job chains. In the end, the orders
     * will be synchronized for each group (which may have been created by a
     * split). */
    @Test
    public void testsync_session_id() {  // SOSOptionString
        objOptions.sync_session_id.setValue("++----++");
        assertEquals("", objOptions.sync_session_id.getValue(), "++----++");

    }

} // public class JobSchedulerSynchronizeJobChainsOptionsJUnitTest