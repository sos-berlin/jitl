package com.sos.jitl.jobstreams.db;


public class FilterCalendarUsage {
    
    private String schedulerId;
    private String path;
    private String objectType;
    private String join;
    
    public String getSchedulerId() {
        return schedulerId;
    }
    
    public void setSchedulerId(String schedulerId) {
        this.schedulerId = schedulerId;
    }
    
    public String getPath() {
        return path;
    }
    
    public void setPath(String path) {
        this.path = path;
    }

    
    public String getJoin() {
        return join;
    }

    
    public void setJoin(String join) {
        this.join = join;
    }

    
    public String getObjectType() {
        return objectType;
    }

    
    public void setObjectType(String objectType) {
        this.objectType = objectType;
    }

}
