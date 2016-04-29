package com.sos.jitl.reporting.helper;

import org.w3c.dom.NodeList;

import sos.xml.SOSXMLXPath;

public class ReportXmlHelper {

    public static NodeList getRootChilds(SOSXMLXPath xpath) throws Exception {
        return xpath.selectNodeList("/" + xpath.getRoot().getNodeName() + "/*");
    }

    public static String getJobChainStartCause(SOSXMLXPath xpath) throws Exception {
        String root = xpath.getRoot().getNodeName();
        boolean hasFileOrderSink = !ReportXmlHelper.isElementEmpty(xpath, "/" + root + "/file_order_source");
        String startCase = EStartCauses.ORDER.value();
        if (hasFileOrderSink) {
            startCase = EStartCauses.FILE_TRIGGER.value();
        }
        return startCase;
    }

    public static String getTitle(SOSXMLXPath xpath) {
        return xpath.getRoot().getAttribute("title");
    }

    public static boolean isOrderJob(SOSXMLXPath xpath) {
        return xpath.getRoot().getAttribute("order") != null && "yes".equals(xpath.getRoot().getAttribute("order").toLowerCase());
    }

    public static boolean isRuntimeDefined(SOSXMLXPath xpath) throws Exception {
        String root = xpath.getRoot().getNodeName();
        boolean isRuntimeDefined = !ReportXmlHelper.isElementEmpty(xpath, "/" + root + "/run_time[1]/*");
        if (!isRuntimeDefined) {
            isRuntimeDefined = !ReportXmlHelper.isElementEmpty(xpath, "/" + root + "/run_time[1][@schedule and string-length(@schedule)!=0]");
        }
        return isRuntimeDefined;
    }

    private static boolean isElementEmpty(SOSXMLXPath xpath, String expr) throws Exception {
        NodeList nl = xpath.selectNodeList(expr);
        if (nl != null && nl.item(0) != null) {
            return false;
        }
        return true;
    }

}