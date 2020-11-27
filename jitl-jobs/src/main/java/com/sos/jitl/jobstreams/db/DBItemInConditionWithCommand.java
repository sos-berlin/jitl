package com.sos.jitl.jobstreams.db;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Transient;

import com.sos.jitl.jobstreams.interfaces.IJSJobConditionKey;

public class DBItemInConditionWithCommand implements IJSJobConditionKey {

    private Long incId;
    private String jobSchedulerId;
    private String job;
    private String expression;
    private Boolean markExpression;
    private Boolean skipOutCondition;
    private String jobStream;
    private String folder;
    private Date nextPeriod;
    private Date incCreated;

    private Long commandId;
    private Long inConditionId;
    private String command;
    private String commandParam;
    private Date commandCreated;

    private Set<String> consumedForContext;

    public DBItemInConditionWithCommand() {

    }

    public Long getIncId() {
        return incId;
    }

    public void setIncId(Long incId) {
        this.incId = incId;
    }

    public void setJobSchedulerId(String jobSchedulerId) {
        this.jobSchedulerId = jobSchedulerId;
    }

    public String getJobSchedulerId() {
        return this.jobSchedulerId;
    }

    public String getJob() {
        return job;
    }

    public void setJob(String job) {
        this.job = job;
    }

    public Boolean getSkipOutCondition() {
        return this.skipOutCondition;
    }

    public void setSkipOutCondition(Boolean skipOutCondition) {
        this.skipOutCondition = skipOutCondition;
    }

    public Boolean getMarkExpression() {
        return this.markExpression;
    }

    public void setMarkExpression(Boolean markExpression) {
        this.markExpression = markExpression;
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public String getJobStream() {
        return jobStream;
    }

    public void setJobStream(String jobStream) {
        this.jobStream = jobStream;
    }

    public String getFolder() {
        return folder;
    }

    public void setFolder(String folder) {
        this.folder = folder;
    }

    public Date getNextPeriod() {
        return nextPeriod;
    }

    public void setNextPeriod(Date nextPeriod) {
        this.nextPeriod = nextPeriod;
    }

    public Date getIncCreated() {
        return incCreated;
    }

    public void setIncCreated(Date incCreated) {
        this.incCreated = incCreated;
    }

    public Long getCommandId() {
        return commandId;
    }

    public void setCommandId(Long commandId) {
        this.commandId = commandId;
    }

    public Long getInConditionId() {
        return inConditionId;
    }

    public void setInConditionId(Long inConditionId) {
        this.inConditionId = inConditionId;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getCommandParam() {
        return commandParam;
    }

    public void setCommandParam(String commandParam) {
        this.commandParam = commandParam;
    }

    public Date getCommandCreated() {
        return commandCreated;
    }

    public void setCommandCreated(Date commandCreated) {
        this.commandCreated = commandCreated;
    }

    @Transient
    public boolean isConsumed(String context) {
        return consumedForContext.contains(context);
    }

    public void setConsumed(String context) {
        if (consumedForContext == null) {
            consumedForContext = new HashSet<String>();
        }
        this.consumedForContext.add(context);
    }

    @Transient
    public Set<String> getConsumedForContext() {
        return consumedForContext;
    }

    public void setConsumedForContext(Set<String> consumedForContext) {
        this.consumedForContext = consumedForContext;
    }

}