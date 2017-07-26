package com.sos.jitl.join;

import static org.junit.Assert.*;

import org.junit.Test;


public class JoinOrderTest {

    @Test
    public void getMainOrderIdTest() {
        String jobChain = "jobchain"; 
        String orderId = "parent_orderId";
        String joinSessionId = "myId";
        boolean isMainOrder = false;
        String state = "state";
        JoinOrder joinOrder  = new JoinOrder(jobChain, orderId, joinSessionId, isMainOrder, state);
        String mainOrder = joinOrder.getMainOrderId();
        assertEquals ("getMainOrderIdTest", "parent" , mainOrder);

        orderId = "parent_orderId_anystring";
        joinOrder  = new JoinOrder(jobChain, orderId, joinSessionId, isMainOrder, state);
        mainOrder = joinOrder.getMainOrderId();
        assertEquals ("getMainOrderIdTest", "parent" , mainOrder);

        orderId = "parentorderId";
        joinOrder  = new JoinOrder(jobChain, orderId, joinSessionId, isMainOrder, state);
        mainOrder = joinOrder.getMainOrderId();
        assertEquals ("getMainOrderIdTest", "parentorderId" , mainOrder);

    
    }

}
