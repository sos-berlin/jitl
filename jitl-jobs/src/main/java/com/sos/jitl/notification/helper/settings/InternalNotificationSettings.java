package com.sos.jitl.notification.helper.settings;

public class InternalNotificationSettings {

    private String schedulerId;
    private String taskId;
    private String message;
    private String messageCode;
    private String messageTitle;

    public String getSchedulerId() {
        return schedulerId;
    }

    public void setSchedulerId(String val) {
        schedulerId = val;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String val) {
        taskId = val;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String val) {
        message = val;
    }

    public String getMessageCode() {
        return messageCode;
    }

    public void setMessageCode(String val) {
        messageCode = val;
    }

    public String getMessageTitle() {
        return messageTitle;
    }

    public void setMessageTitle(String val) {
        messageTitle = val;
    }
}
