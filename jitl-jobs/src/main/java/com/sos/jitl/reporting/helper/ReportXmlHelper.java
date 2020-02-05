package com.sos.jitl.reporting.helper;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
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

    public static String getCriticality(SOSXMLXPath xpath) {
        return xpath.getRoot().getAttribute("criticality");
    }

    public static boolean isOrderJob(SOSXMLXPath xpath) {
        return xpath.getRoot().hasAttribute("order") && "yes,1,true".contains(xpath.getRoot().getAttribute("order").toLowerCase());
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
    
    public static boolean hasDescription(SOSXMLXPath xpath) throws Exception {
        return !ReportXmlHelper.isElementEmpty(xpath, "description");
    }

    public static boolean hasProcessClasses(SOSXMLXPath xpath) throws Exception {
        return !ReportXmlHelper.isElementEmpty(xpath, "process_classes");
    }

    public static boolean hasSchedules(SOSXMLXPath xpath) throws Exception {
        return !ReportXmlHelper.isElementEmpty(xpath, "schedules");
    }

    public static String getProcessClass(SOSXMLXPath xpath) throws Exception {
        String processClass = xpath.getRoot().getAttribute("process_class");
        return !processClass.isEmpty() ? processClass : null;
    }

    public static String getFileWatchingProcessClass(SOSXMLXPath xpath) throws Exception {
        String fwProcessClass = xpath.getRoot().getAttribute("file_watching_process_class");
        return !fwProcessClass.isEmpty() ? fwProcessClass : null;
    }

    public static String getScheduleFromRuntime(SOSXMLXPath xpath) throws Exception {
        Node runtime = xpath.selectSingleNode("run_time");
        if(runtime != null && ((Element)runtime).hasAttribute("schedule")) {
            return ((Element)runtime).getAttribute("schedule");
        } else {
            return null;
        }
    }

    public static NodeList getLockUses(SOSXMLXPath xpath) throws Exception {
        return xpath.selectNodeList("lock.use");
    }
    
    public static Integer getMaxNonExclusive(SOSXMLXPath xpath) throws Exception {
        String maxNonExclusive = xpath.getRoot().getAttribute("max_non_exclusive");
        if(maxNonExclusive != null && !maxNonExclusive.isEmpty()) {
            return Integer.parseInt(maxNonExclusive);
        } else {
            return null; 
        }
    }

    public static Integer getMaxProcesses(SOSXMLXPath xpath) throws Exception {
        String maxProcesses = xpath.getRoot().getAttribute("max_processes");
        if(maxProcesses != null && !maxProcesses.isEmpty()) {
            return Integer.parseInt(maxProcesses);
        } else {
            return null;
        }
    }
    
    public static String getSubstitute(SOSXMLXPath xpath) throws Exception {
        return xpath.getRoot().getAttribute("substitute");
    }
    
    public static Date getSubstituteValidFromTo(SOSXMLXPath xpath, String attribute, String timezone) throws Exception {
        String validFromTo = xpath.getRoot().getAttribute(attribute);
        if(validFromTo != null && !validFromTo.isEmpty()) {
            LocalDateTime localDateTime = LocalDateTime.parse(validFromTo, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            ZonedDateTime zdt = ZonedDateTime.of(localDateTime, ZoneId.of(timezone));
            Instant valid = zdt.toInstant();
            return Date.from(valid);
        } else {
            return null;
        } 
    }
    
    public static boolean hasAgents(SOSXMLXPath xpath) throws Exception {
        String remoteScheduler = xpath.getRoot().getAttribute("remote_scheduler");
        if(remoteScheduler != null && !remoteScheduler.isEmpty()) {
            return true;
        } else {
            Map<String,Integer> remoteSchedulers = getRemoteSchedulersFromProcessClass(xpath);
            return remoteSchedulers != null && !remoteSchedulers.isEmpty();
        }
    }
    
    public static Map<String,Integer> getRemoteSchedulersFromProcessClass (SOSXMLXPath xpath) throws Exception {
        int ordering = 1;
        NodeList remoteSchedulers = xpath.selectNodeList("remote_schedulers/remote_scheduler");
        Map<String, Integer> remoteSchedulerUrls = new HashMap<String, Integer>();
        for (int i = 0; i < remoteSchedulers.getLength(); i++) {
            Element remoteScheduler = (Element)remoteSchedulers.item(i);
            String url = remoteScheduler.getAttribute("remote_scheduler");
            if(url != null && !url.isEmpty()) {
                remoteSchedulerUrls.put(url, ordering);
                ordering++;
            }
        }
        return remoteSchedulerUrls;
    }
    
    public static String getSchedulingType(SOSXMLXPath xpath) throws Exception {
        Element remoteSchedulers = (Element)xpath.selectSingleNode("remote_schedulers");
        return remoteSchedulers.getAttribute("select");
    }
    
    public static boolean hasLockUses(SOSXMLXPath xpath) throws Exception {
        NodeList lockUses = xpath.selectNodeList("lock.use");
        if(lockUses != null && lockUses.getLength() > 0) {
            return true;
        } else {
            return false;
        }
    }
    
}