package com.sos.jitl.notification.helper.settings;

public class MailSettings {

    String iniPath;
    String smtp;
    String queueDir;
    String from;
    String to;
    String cc;
    String bcc;

    public String getIniPath() {
        return iniPath;
    }

    public void setIniPath(String val) {
        iniPath = val;
    }

    public String getSmtp() {
        return smtp;
    }

    public void setSmtp(String val) {
        smtp = val;
    }

    public String getQueueDir() {
        return queueDir;
    }

    public void setQueueDir(String val) {
        queueDir = val;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String val) {
        from = val;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String val) {
        to = val;
    }

    public String getCc() {
        return cc;
    }

    public void setCc(String val) {
        cc = val;
    }

    public String getBcc() {
        return bcc;
    }

    public void setBcc(String val) {
        bcc = val;
    }

}
