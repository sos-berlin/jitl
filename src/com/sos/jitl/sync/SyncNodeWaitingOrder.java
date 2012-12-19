package com.sos.jitl.sync;

public class SyncNodeWaitingOrder {
	private String syncId;
    private String id;
	
	
	public SyncNodeWaitingOrder(String id,String syncId) {
		super();
		this.syncId = syncId;
		this.id = id;
	}

	public String getSyncId() {
		return syncId;
	}

	public void setSyncId(String syncId) {
		this.syncId = syncId;
	}

	public String getId() {
		return id;
	}

	

}
