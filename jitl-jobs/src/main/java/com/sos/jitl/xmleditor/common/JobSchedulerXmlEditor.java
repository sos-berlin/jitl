package com.sos.jitl.xmleditor.common;

import com.sos.joc.model.xmleditor.common.ObjectType;

public class JobSchedulerXmlEditor {

    public static final String CONFIGURATION_LIVE_FOLDER = "/sos/.configuration";
    public static final String CONFIGURATION_BASENAME_YADE = "yade";
    public static final String CONFIGURATION_BASENAME_NOTIFICATION = "notification";

    public static String getBaseName(ObjectType type) {
        if (type == null) {
            return null;
        }
        if (type.equals(ObjectType.YADE)) {
            return CONFIGURATION_BASENAME_YADE;
        } else if (type.equals(ObjectType.NOTIFICATION)) {
            return CONFIGURATION_BASENAME_NOTIFICATION;
        }
        return null;
    }

    public static String getLivePathXml(ObjectType type) {
        if (type == null) {
            return null;
        }
        if (type.equals(ObjectType.YADE)) {
            return getLivePathYadeXml();
        } else if (type.equals(ObjectType.NOTIFICATION)) {
            return getLivePathNotificationXml();
        }
        return null;
    }

    public static String getLivePathYadeXml() {
        return String.format("%s/%s/%s.xml", CONFIGURATION_LIVE_FOLDER, CONFIGURATION_BASENAME_YADE, CONFIGURATION_BASENAME_YADE);
    }

    public static String getLivePathYadeIni() {
        return String.format("%s/%s/%s.ini", CONFIGURATION_LIVE_FOLDER, CONFIGURATION_BASENAME_YADE, CONFIGURATION_BASENAME_YADE);
    }

    public static String getLivePathNotificationXml() {
        return String.format("%s/%s/%s.xml", CONFIGURATION_LIVE_FOLDER, CONFIGURATION_BASENAME_NOTIFICATION, CONFIGURATION_BASENAME_NOTIFICATION);
    }
}
