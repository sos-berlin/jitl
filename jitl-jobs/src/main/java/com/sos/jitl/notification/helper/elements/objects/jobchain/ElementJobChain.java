package com.sos.jitl.notification.helper.elements.objects.jobchain;

import java.util.ArrayList;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.sos.jitl.notification.db.DBLayerSchedulerMon;
import com.sos.jitl.notification.helper.NotificationXmlHelper;
import com.sos.jitl.notification.helper.elements.monitor.ElementNotificationMonitor;

import sos.util.SOSString;

public class ElementJobChain {

    public static String ELEMENT_NAME_NOTIFY_REPEATED_ERROR = "NotifyRepeatedError";

    private Node xml;
    private ElementNotificationMonitor monitor;
    private String schedulerId;
    private String name;
    private Long notifications;
    private String stepFrom;
    private String stepTo;
    private String returnCodeFrom;
    private String returnCodeTo;
    private ArrayList<String> excludedSteps;
    private String excludedStepsAsString;
    private ElementNotifyRepeatedError notifyRepeatedError;

    public ElementJobChain(ElementNotificationMonitor element, Node node) throws Exception {
        monitor = element;
        xml = node;
        Element el = (Element) xml;
        schedulerId = getValue(NotificationXmlHelper.getSchedulerId(el));
        name = getValue(NotificationXmlHelper.getJobChainName(el));
        notifications = getLongValue(NotificationXmlHelper.getNotifications(el));
        stepFrom = getValue(NotificationXmlHelper.getStepFrom(el));
        stepTo = getValue(NotificationXmlHelper.getStepTo(el));
        returnCodeFrom = getValue(NotificationXmlHelper.getReturnCodeFrom(el));
        returnCodeTo = getValue(NotificationXmlHelper.getReturnCodeTo(el));
        setExcludedSteps(el);

        Node n = NotificationXmlHelper.getChildNode(el, ELEMENT_NAME_NOTIFY_REPEATED_ERROR);
        if (n != null) {
            notifyRepeatedError = new ElementNotifyRepeatedError(n);
        }
    }

    private String getValue(String val) {
        return SOSString.isEmpty(val) ? DBLayerSchedulerMon.DEFAULT_EMPTY_NAME : val;
    }

    private Long getLongValue(String val) {
        return SOSString.isEmpty(val) ? new Long(1) : new Long(val);
    }

    public ElementNotificationMonitor getMonitor() {
        return monitor;
    }

    public Node getXml() {
        return xml;
    }

    public String getSchedulerId() {
        return schedulerId;
    }

    public String getName() {
        return name;
    }

    public Long getNotifications() {
        return notifications;
    }

    public String getStepFrom() {
        return stepFrom;
    }

    public String getStepTo() {
        return stepTo;
    }

    public String getReturnCodeFrom() {
        return returnCodeFrom;
    }

    public String getReturnCodeTo() {
        return returnCodeTo;
    }

    private void setExcludedSteps(Element jobChain) {
        excludedSteps = new ArrayList<String>();
        excludedStepsAsString = "";
        String es = NotificationXmlHelper.getExcludedSteps(jobChain);
        if (!SOSString.isEmpty(es)) {
            excludedStepsAsString = es;
            String[] arr = es.trim().split(";");
            for (int i = 0; i < arr.length; i++) {
                if (!arr[i].trim().isEmpty()) {
                    excludedSteps.add(arr[i].trim());
                }
            }
        }
    }

    public ArrayList<String> getExcludedSteps() {
        return excludedSteps;
    }

    public String getExcludedStepsAsString() {
        return excludedStepsAsString;
    }

    public ElementNotifyRepeatedError getNotifyRepeatedError() {
        return notifyRepeatedError;
    }
}