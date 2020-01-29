
package com.sos.jitl.join;

import java.util.HashMap;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.Basics.JSToolBox;
import com.sos.JSHelper.Listener.JSListenerClass;

public class JobSchedulerJoinOrdersJUnitTest extends JSToolBox {

    protected JobSchedulerJoinOrdersOptions objOptions = null;
    private static final String CLASSNAME = "JobSchedulerJoinOrdersJUnitTest";
    private static final Logger LOGGER = LoggerFactory.getLogger(JobSchedulerJoinOrdersJUnitTest.class);
    private JobSchedulerJoinOrders objE = null;

    public JobSchedulerJoinOrdersJUnitTest() {
        //
    }

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        // TODO: Implement Method setUpBeforeClass here
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        // TODO: Implement Method tearDownAfterClass here
    }

    @Before
    public void setUp() throws Exception {
        objE = new JobSchedulerJoinOrders();
        objE.registerMessageListener(this);
        objOptions = objE.getOptions();
        objOptions.registerMessageListener(this);
        JSListenerClass.bolLogDebugInformation = true;
        JSListenerClass.intMaxDebugLevel = 9;
    }

    @After
    public void tearDown() throws Exception {
        // TODO: Implement Method tearDown here
    }

    private String executeOrder(JoinOrder joinOrder, JobSchedulerJoinOrders jobSchedulerJoinOrders,String joinOrderListString) throws Exception {

        
        if (joinOrder.isMainOrder()) {
            LOGGER.info("Suspend order");
        }

        jobSchedulerJoinOrders.setJoinOrder(joinOrder);
        
        jobSchedulerJoinOrders.setJoinOrderListString(joinOrderListString);

        jobSchedulerJoinOrders.execute();

        if (jobSchedulerJoinOrders.isResumeAllOrders()) {
            LOGGER.info("Resume order");
        }

        return jobSchedulerJoinOrders.getJoinSerializer().getSerializedObject();
    }

    @Test
    public void testExecute() throws Exception {
        JobSchedulerJoinOrders jobSchedulerJoinOrders = new JobSchedulerJoinOrders();

        HashMap<String,String> parameters = new HashMap<String,String>();
        parameters.put("required_orders", "3");
        parameters.put("join_session_id", "myid");

        jobSchedulerJoinOrders.getOptions().setAllOptions(parameters);

        String ser="";

        String jobChain = "/job_chain1";
        String orderId = "start";
        String joinSessionId = "mySession";
        boolean isMainOrder = true;
        if (jobSchedulerJoinOrders.getOptions().joinSessionId.isDirty()) {
            joinSessionId = jobSchedulerJoinOrders.getOptions().joinSessionId.getValue();
        }

        JoinOrder joinOrder = new JoinOrder(jobChain, orderId, joinSessionId, isMainOrder,"state");

        ser = executeOrder(joinOrder,jobSchedulerJoinOrders, ser);
        
        orderId = "start2";
        isMainOrder = false;
        joinOrder = new JoinOrder(jobChain, orderId, joinSessionId, isMainOrder,"state");
        ser = executeOrder(joinOrder,jobSchedulerJoinOrders,ser);

        orderId = "start3";
        isMainOrder = false;
        joinOrder = new JoinOrder(jobChain, orderId, joinSessionId, isMainOrder,"state");
        ser = executeOrder(joinOrder,jobSchedulerJoinOrders,ser);


    }

}
