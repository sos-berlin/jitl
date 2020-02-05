package com.sos.jitl.reporting.helper;

import java.util.Date;

import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;

import com.sos.jitl.eventhandler.EventMeta.EventKey;

public class TaskStarted {

    private String jobPath;
    private Long historyId;
    private Date startTime;

    public TaskStarted(JsonObject jo) throws Exception {
        JsonValue key = jo.get(EventKey.key.name());
        if (key != null && key.getValueType().equals(ValueType.OBJECT)) {
            try {
                jobPath = ((JsonObject) key).getString("jobPath");
                historyId = Long.parseLong(((JsonObject) key).getString("taskId"));
                startTime = ReportUtil.getEventIdAsDate(jo.getJsonNumber("eventId").longValue());
            } catch (Throwable e) {
                throw new Exception(String.format("can't parse TaskStarted event: %s", e.toString()), e);
            }
        }
    }

    public Date getStartTime() {
        return startTime;
    }

    public Long getHistoryId() {
        return historyId;
    }

    public String getJobPath() {
        return jobPath;
    }

}
