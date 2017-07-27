package com.sos.jitl.join;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JoinOrderList implements java.io.Serializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(JoinOrderList.class);
    private static final long serialVersionUID = 4371766177498357215L;
    private Map<String, List<JoinOrder>> joinSessionIdListOfOrders;

    public JoinOrderList() {
        super();
    }

    private String getKey(JoinOrder joinOrder) {
        return joinOrder.getJoinSessionId() + "::" + joinOrder.getJoinState();
    }

    private boolean orderExist(List<JoinOrder> listOfOrders, JoinOrder joinOrder) {
        for (JoinOrder order : listOfOrders) {
            if (order.getJobChain().equals(joinOrder.getJobChain()) && order.getOrderId().equals(joinOrder.getOrderId())) {
                return true;
            }
        }
        return false;
    }

    private List<JoinOrder> getListOfOrders(JoinOrder joinOrder){
        List<JoinOrder> listOfOrders = joinSessionIdListOfOrders.get(getKey(joinOrder));
        if (listOfOrders == null) {
            listOfOrders = new ArrayList<JoinOrder>();
        }
        return listOfOrders;

    }
    
    public void addOrder(JoinOrder joinOrder) {
        if (joinSessionIdListOfOrders == null) {
            joinSessionIdListOfOrders = new HashMap<String, List<JoinOrder>>();
        }

        List<JoinOrder> listOfOrders = getListOfOrders(joinOrder);

        if (!orderExist(listOfOrders, joinOrder)) {
            listOfOrders.add(joinOrder);
            joinSessionIdListOfOrders.put(getKey(joinOrder), listOfOrders);
        }else{
            LOGGER.debug(String.format("Order %s will not be added to the list of orders as the list already contains it", joinOrder.getTitle()));
        }
    }

    public int size(JoinOrder joinOrder) {
        return joinSessionIdListOfOrders.get(getKey(joinOrder)).size();
    }

    public void reset(JoinOrder joinOrder) {
        if (joinSessionIdListOfOrders == null) {
            joinSessionIdListOfOrders = new HashMap<String, List<JoinOrder>>();
        } 
        List<JoinOrder> listOfOrders = new ArrayList<JoinOrder>();
        joinSessionIdListOfOrders.put(getKey(joinOrder), listOfOrders);
    }

    public JoinOrder getMainOrder(JoinOrder joinOrder) {
        List<JoinOrder> listOfOrders = joinSessionIdListOfOrders.get(getKey(joinOrder));
        if (listOfOrders == null) {
            return null;
        } else {
            for (JoinOrder order : listOfOrders) {
                if (order.isMainOrder()) {
                    return order;
                }
            }
        }
        return null;
    }

    public void showJoinOrderList(JoinOrder joinOrder) {
        List<JoinOrder> listOfOrders = getListOfOrders(joinOrder);

        LOGGER.info("Members of list: " + joinOrder.paramNameForSerializedList());
        for (JoinOrder order : listOfOrders) {
            String s = order.getTitle();
            if (order.isMainOrder()) {
                s = s + " --> mainOrder";
            }
            LOGGER.info(s);
        }
    }
}
