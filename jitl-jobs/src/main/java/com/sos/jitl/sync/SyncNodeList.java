package com.sos.jitl.sync;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SyncNodeList {

    private static final Logger LOGGER = LoggerFactory.getLogger(SyncNodeList.class);
    private List<SyncNode> listOfNodes;
    private int nodeIndex = 0;
    private int numberOfWaitingNodes = -1;
    public static final String CHAIN_ORDER_DELIMITER = ",";
    public static final String CONST_PARAM_REQUIRED_ORDERS = "required_orders";
    public static final String CONST_PARAM_PART_REQUIRED_ORDERS = "_" + CONST_PARAM_REQUIRED_ORDERS;

    public SyncNodeList() {
        super();
        listOfNodes = new ArrayList<SyncNode>();
    }

    public void addNode(final SyncNode sn) {
        if (listOfNodes == null) {
            listOfNodes = new ArrayList<SyncNode>();
        }
        listOfNodes.add(sn);
    }

    public SyncNode getNode(String jobChain, String state) {
        for (SyncNode sn : listOfNodes) {
            if (sn.getSyncNodeState().equals(state) && sn.getSyncNodeJobchain().equals(jobChain)) {
                return sn;
            }
        }
        return null;
    }

    public boolean isReleased() {
        boolean erg = true;
        for (SyncNode sn : listOfNodes) {
            erg = erg && sn.isReleased();
        }
        return erg;
    }

    public SyncNode getFirstNotReleasedNode() {
        SyncNode snResult = null;
        numberOfWaitingNodes = 0;
        for (SyncNode sn : listOfNodes) {
            if (!sn.isReleased()) {
                numberOfWaitingNodes = numberOfWaitingNodes + 1;
                if (snResult == null) {
                    snResult = sn;
                }
            }
        }
        return snResult;
    }

    public int getCount() {
        return listOfNodes.size();
    }

    public void setRequired(final String job_chain_required) {
        LOGGER.debug("checking " + job_chain_required);
        for (SyncNode sn : listOfNodes) {
            String prefix = sn.getSyncNodeJobchain();
            LOGGER.debug(String.format("Prefix=%s, job_chain_required=%s", prefix, job_chain_required));
            if (job_chain_required.startsWith(prefix + CONST_PARAM_PART_REQUIRED_ORDERS)) {
                sn.setRequired(getRequiredFromPrefix(prefix, job_chain_required));
                LOGGER.debug(String.format("--> %s", getRequiredFromPrefix(prefix, job_chain_required)));
            }
            prefix = prefix + CHAIN_ORDER_DELIMITER + sn.getSyncNodeState();
            LOGGER.debug(String.format("Prefix=%s, job_chain_required=%s", prefix, job_chain_required));
            if (job_chain_required.startsWith(prefix + CONST_PARAM_PART_REQUIRED_ORDERS)) {
                sn.setRequired(getRequiredFromPrefix(prefix, job_chain_required));
                LOGGER.debug(String.format("--> %s", getRequiredFromPrefix(prefix, job_chain_required)));
            }
        }
    }

    public String getRequiredFromPrefix(final String jobchain_name, final String jobchain_required) {
        return jobchain_required.replaceAll("^" + jobchain_name + CONST_PARAM_PART_REQUIRED_ORDERS + "_?", "");
    }

    public void setRequired(final int required) {
        for (SyncNode sn : listOfNodes) {
            if (sn.getClass() != null) {
                sn.setRequired(required);
            }
        }
    }

    public void addOrder(final SyncNodeWaitingOrder order, final String jobchain, final String state) {
        LOGGER.debug(String.format("Adding order: %s.%s", jobchain, order.getId()));
        for (SyncNode sn : listOfNodes) {
            if (sn.getSyncNodeState().equals(state) && sn.getSyncNodeJobchainPath().equals(jobchain)) {
                LOGGER.debug("---->" + sn.getSyncNodeJobchainPath() + ":" + sn.getSyncNodeState());
                sn.addOrder(order);
            }

        }
    }

    public List<SyncNode> getListOfNodes() {
        return listOfNodes;
    }

    public boolean eof() {
        return nodeIndex >= getCount();
    }

    public SyncNode getNextSyncNode() {
        SyncNode sn = getListOfNodes().get(nodeIndex);
        nodeIndex++;
        return sn;
    }

    public int getNumberOfWaitingNodes() {
        if (numberOfWaitingNodes == -1) {
            getFirstNotReleasedNode();
        }
        return numberOfWaitingNodes;
    }

}