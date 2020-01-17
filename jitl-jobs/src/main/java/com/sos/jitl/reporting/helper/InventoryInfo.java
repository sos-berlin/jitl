package com.sos.jitl.reporting.helper;

import java.io.Serializable;

import com.sos.scheduler.SOSJobSchedulerGlobal;

import sos.util.SOSString;

public class InventoryInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    private String schedulerId;
    private String hostname;
    private int port;
    private String clusterType;
    private String name;
    private String title;
    private String url;
    private String criticality;
    private boolean isOrderJob;
    private boolean isRuntimeDefined;
    private Integer ordering;

    public InventoryInfo() {
    }

    public void setSchedulerId(String val) {
        schedulerId = val;
    }

    public String getSchedulerId() {
        return schedulerId;
    }

    public void setHostname(String val) {
        hostname = val;
    }

    public String getHostname() {
        return hostname;
    }

    public void setPort(int val) {
        port = val;
    }

    public int getPort() {
        return port;
    }

    public void setClusterType(String val) {
        clusterType = val;
    }

    public String getClusterType() {
        return clusterType;
    }

    public void setName(String val) {
        name = val;
    }

    public String getName() {
        return name;
    }

    public void setUrl(String val) {
        url = val;
    }

    public String getUrl() {
        return url;
    }

    public void setCriticality(String val) {
        if (SOSString.isEmpty(val)) {
            val = SOSJobSchedulerGlobal.JOB_CRITICALITY.NORMAL.toString();
        }
        criticality = val;
    }

    public String getCriticality() {
        return criticality;
    }

    public void setTitle(String val) {
        if (val != null && val.trim().length() == 0) {
            val = null;
        }
        title = val;
    }

    public String getTitle() {
        return title;
    }

    public void setIsOrderJob(boolean val) {
        isOrderJob = val;
    }

    public boolean getIsOrderJob() {
        return isOrderJob;
    }

    public void setIsRuntimeDefined(boolean val) {
        isRuntimeDefined = val;
    }

    public boolean getIsRuntimeDefined() {
        return isRuntimeDefined;
    }

    public void setOrdering(Integer val) {
        ordering = val;
    }

    public Integer getOrdering() {
        return ordering;
    }

    public String getClusterMemberIdFromInstance() {
        if (clusterType != null && !clusterType.equals("standalone")) {
            return schedulerId + "/" + hostname + ":" + port;
        }
        return null;
    }
}
