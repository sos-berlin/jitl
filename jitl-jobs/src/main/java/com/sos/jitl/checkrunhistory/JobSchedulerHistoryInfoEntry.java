package com.sos.jitl.checkrunhistory;

import java.time.LocalDateTime;

public class JobSchedulerHistoryInfoEntry {

    public boolean found;
    public String name;
    public int position;
    public LocalDateTime start;
    public LocalDateTime end;
    public String duration;
    public int executionResult;
    public String errorMessage;
    public int error;
    public String errorCode;
    public int id;
    public String jobName;
    public String state;
    public String orderId;
    public String jobChainName;
}