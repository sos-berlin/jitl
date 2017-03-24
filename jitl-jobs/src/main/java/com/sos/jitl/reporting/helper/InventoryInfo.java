package com.sos.jitl.reporting.helper;

import java.io.Serializable;

public class InventoryInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    private String schedulerId;
    private String hostname;
    private Integer port;
    private String clusterType;
    private String name;
    private String title;
    private String url;
    private boolean isRuntimeDefined;
    private Integer ordering;

    public InventoryInfo() {
    }

    public void setSchedulerId(String val) {
        this.schedulerId = val;
    }

    public String getSchedulerId() {
        return this.schedulerId;
    }

    public void setHostname(String val) {
        this.hostname = val;
    }

    public String getHostname() {
        return this.hostname;
    }

    public void setPort(Integer val) {
        this.port = val;
    }

    public Integer getPort() {
        return this.port;
    }

    public void setClusterType(String val) {
        this.clusterType = val;
    }

    public String getClusterType() {
        return this.clusterType;
    }

    public void setName(String val) {
        this.name = val;
    }

    public String getName() {
        return this.name;
    }

    public void setUrl(String val) {
        this.url = val;
    }

    public String getUrl() {
        return this.url;
    }

    public void setTitle(String val) {
        if (val != null && val.trim().length() == 0) {
            val = null;
        }
        this.title = val;
    }

    public String getTitle() {
        return this.title;
    }

    public void setIsRuntimeDefined(boolean val) {
        this.isRuntimeDefined = val;
    }

    public boolean getIsRuntimeDefined() {
        return this.isRuntimeDefined;
    }

    public void setOrdering(Integer val) {
        this.ordering = val;
    }

    public Integer getOrdering() {
        return this.ordering;
    }

    public String getClusterMemberIdFromInstance() {
        if (clusterType != null && schedulerId != null && hostname != null && port != null && !clusterType.equals("standalone")) {
            return schedulerId + "/" + hostname + ":" + port;
        }
        return null;
    }
}
