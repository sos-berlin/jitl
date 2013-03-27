package com.sos.jitl.sync;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

public class SyncNodeList {
	public static final String	CHAIN_ORDER_DELIMITER				= ",";   // ";" is the delimiter in variable_set :-((

	private static Logger		logger								= Logger.getLogger(SyncNodeList.class);

	public static final String	CONST_PARAM_REQUIRED_ORDERS	= "required_orders";
	public static final String	CONST_PARAM_PART_REQUIRED_ORDERS	= "_" + CONST_PARAM_REQUIRED_ORDERS;
	private List<SyncNode>		listOfNodes;
	private int					nodeIndex							= 0;

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

	public boolean isReleased() {
		boolean erg = true;

		for (SyncNode sn : listOfNodes) {
			erg = erg && sn.isReleased();
		}
		return erg;
	}

	public int getCount() {
		return listOfNodes.size();
	}

	public void setRequired(final String job_chain_required) {
		logger.debug("checking " + job_chain_required);
		for (SyncNode sn : listOfNodes) {
			String prefix = sn.getSyncNodeJobchain() + CHAIN_ORDER_DELIMITER;
			if (job_chain_required.startsWith(prefix + CONST_PARAM_PART_REQUIRED_ORDERS)) {
				sn.setRequired(getRequiredFromPrefix(prefix, job_chain_required));
			}

			prefix = prefix + sn.getSyncNodeState();
			if (job_chain_required.startsWith(prefix + CONST_PARAM_PART_REQUIRED_ORDERS)) {
				sn.setRequired(getRequiredFromPrefix(prefix, job_chain_required));
			}
		}
	}

	public String getRequiredFromPrefix(final String jobchain_name, final String jobchain_required) {
		String erg = jobchain_required.replaceAll("^" + jobchain_name + CONST_PARAM_PART_REQUIRED_ORDERS, "");
		return erg;
	}

	public void setRequired(final int required) {
		for (SyncNode sn : listOfNodes) {
			if (sn.getClass() == null) {
				sn.setRequired(required);
			}
		}
	}

	public void addOrder(final SyncNodeWaitingOrder order, final String jobchain, final String state, final String syncId) {
		logger.debug(String.format("Adding order: %s.%s", jobchain, order.getId()));
		for (SyncNode sn : listOfNodes) {
			if (sn.getSyncNodeState().equals(state) && sn.getSyncNodeJobchainPath().equals(jobchain)) {
				logger.debug("---->" + sn.getSyncNodeJobchainPath() + ":" + sn.getSyncNodeState());
				sn.addOrder(order, syncId);
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
}
