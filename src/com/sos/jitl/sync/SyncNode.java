package com.sos.jitl.sync;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

public class SyncNode {

	private String						syncNodeJobchainName;
	private String						syncNodeJobchainPath;
	private String						syncNodeState;

	private int							required	= 1;
	private List<SyncNodeWaitingOrder>	listOfSyncNodeWaitingOrder;

	private static Logger				logger		= Logger.getLogger(SyncNode.class);

	public SyncNode() {
		super();
		listOfSyncNodeWaitingOrder = new ArrayList<SyncNodeWaitingOrder>();
	}

	public void addOrder(final SyncNodeWaitingOrder so) {
		if (listOfSyncNodeWaitingOrder == null) {
			listOfSyncNodeWaitingOrder = new ArrayList<SyncNodeWaitingOrder>();
		}

		listOfSyncNodeWaitingOrder.add(so);

	}

	public void addOrder(final SyncNodeWaitingOrder so, final String syncId) {
		if (listOfSyncNodeWaitingOrder == null) {
			listOfSyncNodeWaitingOrder = new ArrayList<SyncNodeWaitingOrder>();
		}

		logger.debug(String.format("check wether order: %s with syncId %s should be added to syncId %s", so.getId(), so.getSyncId(), syncId));
		if (syncId.equals("") || syncId == null || so.getSyncId().equals(syncId)) {
			logger.debug(" ----->added");
			listOfSyncNodeWaitingOrder.add(so);
		}

	}

	public String getSyncNodeJobchain() {
		return syncNodeJobchainName;
	}

	public String getSyncNodeState() {
		return syncNodeState;
	}

	public void setSyncNodeState(final String syncNodeState) {
		this.syncNodeState = syncNodeState;
	}

	public int getRequired() {
		return required;
	}

	public void setRequired(final int required) {
		logger.debug(String.format("%s: required orders=%s", syncNodeJobchainName, required));
		this.required = required;
	}

	public void setRequired(final String required) {
		try {
			logger.debug(String.format("%s: required orders=%s", syncNodeJobchainName, required));
			this.required = Integer.parseInt(required);
		}
		catch (NumberFormatException e) {
			logger.warn(String.format("could not convert %s", required));
		}
	}

	public List<SyncNodeWaitingOrder> getSyncNodeWaitingOrderList() {
		return listOfSyncNodeWaitingOrder;
	}

	public void setSyncNodeWaitingOrderList(final List<SyncNodeWaitingOrder> syncNodeWaitingOrderList) {
		listOfSyncNodeWaitingOrder = syncNodeWaitingOrderList;
	}

	public boolean isReleased() {
		boolean erg = listOfSyncNodeWaitingOrder.size() >= required;
		logger.debug(String.format("Jobchain: %s, State: %s,  required: %s, waiting: %s ----> %s", syncNodeJobchainPath, syncNodeState, required,
				listOfSyncNodeWaitingOrder.size(), erg));
		return listOfSyncNodeWaitingOrder.size() >= required;
	}

	public boolean isReleased(final String syncId) {
		return listOfSyncNodeWaitingOrder.size() >= required;
	}

	public void setSyncNodeJobchainName(final String syncNodeJobchainName) {
		this.syncNodeJobchainName = syncNodeJobchainName;
	}

	public String getSyncNodeJobchainPath() {
		return syncNodeJobchainPath;
	}

	public void setSyncNodeJobchainPath(final String syncNodeJobchainPath) {
		this.syncNodeJobchainPath = syncNodeJobchainPath;
	}

	public String getSyncNodeJobchainName() {
		return syncNodeJobchainName;
	}

}
