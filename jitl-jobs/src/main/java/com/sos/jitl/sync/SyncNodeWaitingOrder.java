package com.sos.jitl.sync;

public class SyncNodeWaitingOrder {

    private String syncId;
    private final String id;
    private String strEndState = "";

    public SyncNodeWaitingOrder(final String id, final String syncId) {
        super();
        this.syncId = syncId;
        this.id = id;
    }

    public String getSyncId() {
        return syncId;
    }

    public void setSyncId(final String syncId) {
        this.syncId = syncId;
    }

    public String getId() {
        return id;
    }

    public String getEndState() {
        return strEndState;
    }

    public void setEndState(final String pstrEndState) {
        strEndState = pstrEndState;
    }
}
