package com.sos.jitl.runonce.data;

import java.io.InputStream;
import java.time.Instant;
import java.util.Date;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sos.jitl.reporting.db.DBItemInventoryInstance;
import com.sos.jitl.reporting.db.DBItemInventoryOperatingSystem;

import sos.xml.SOSXMLXPath;

public class ProcessDataUtil {

    // has to be moved to the calling job- and/or plugin-class
    private static final String DIALECT_REGEX = "org\\.hibernate\\.dialect\\.(.*?)(?:\\d*InnoDB|\\d+[ig]?)?Dialect";
    private static final String NEWLINE_REGEX = "^([^\r\n]*)";
    private static final Logger LOGGER = Logger.getLogger(ProcessDataUtil.class);
    private String schedulerHibernateConfigFileName;
    private String webserviceHibernateConfigFileName;
    
    public ProcessDataUtil() {

    }

    public ProcessDataUtil(String hibernateCfgXml) {
        this.schedulerHibernateConfigFileName = hibernateCfgXml;
    }

    // TODO: return type has to be changed to DBItem when ready
    public DBItemInventoryInstance getDataFromJobscheduler(String answerXml) throws Exception {
        DBItemInventoryInstance jsInstance = new DBItemInventoryInstance();
        SOSXMLXPath xPath = new SOSXMLXPath(new StringBuffer(answerXml));
        Node stateNode = xPath.selectSingleNode("/spooler/answer/state");
        Element stateElement = (Element) stateNode;
        jsInstance.setSchedulerId(stateElement.getAttribute("id"));
        jsInstance.setHostname(stateElement.getAttribute("host"));
        String tcpPort = stateElement.getAttribute("tcp_port");
        if (tcpPort != null && !tcpPort.isEmpty()) {
            jsInstance.setPort(Integer.parseInt(tcpPort));
        } else {
            jsInstance.setPort(Integer.parseInt(stateElement.getAttribute("udp_port")));
        }
        jsInstance.setVersion(stateElement.getAttribute("version"));
        String httpPort = stateElement.getAttribute("http_port");
        if(httpPort != null && !httpPort.isEmpty()) {
            jsInstance.setCommandUrl("http://" + jsInstance.getHostname() + ":" + httpPort);
        }
        jsInstance.setUrl("http://" + jsInstance.getHostname() + ":" + jsInstance.getPort().toString());
        jsInstance.setTimeZone(stateElement.getAttribute("time_zone"));
        String spoolerRunningSince = stateElement.getAttribute("spooler_running_since");
        if(spoolerRunningSince != null) {
            jsInstance.setStartedAt(getDateFromISO8601String(spoolerRunningSince));
        }
        jsInstance.setLiveDirectory("???");
        Node clusterNode = xPath.selectSingleNode(stateNode, "cluster");
        if(clusterNode != null) {
            String activeCluster = ((Element)clusterNode).getAttribute("active");
            String clusterMemberId = ((Element)clusterNode).getAttribute("cluster_member_id");
            if("yes".equalsIgnoreCase(activeCluster)) {
                jsInstance.setClusterType("active");
            } else {
                jsInstance.setClusterType("passive");
            }
            NodeList clusterMembers = clusterNode.getChildNodes();
            for(int i = 0; i < clusterMembers.getLength(); i++) {
                Element clusterMember = (Element)clusterMembers.item(i);
                if(clusterMember.getAttribute("cluster_member_id").equalsIgnoreCase(clusterMemberId)){
                    jsInstance.setPrecedence(Integer.parseInt(clusterMember.getAttribute("backup_precedence")));
                    break;
                }
            }
        } else {
            jsInstance.setClusterType("standalone");
        }
        jsInstance.setDbmsName(getDbmsName(schedulerHibernateConfigFileName));
        jsInstance.setDbmsVersion("???");
        jsInstance.setSupervisorId(null);
        // jsInstance.setOsId(osId);
        // this has to happen at db level only, if entry exist in db set modified else set created AND modified (NOT NULL IN DB)
        jsInstance.setCreated(new Date());
        jsInstance.setModified(new Date());
        return jsInstance;
    }

    public String getDbmsName(String hibernateConfigFile) throws Exception {
        SOSXMLXPath xPath = new SOSXMLXPath(hibernateConfigFile);
        String dialect = xPath.selectSingleNodeValue("/hibernate-configuration/session-factory/property[@name='hibernate.dialect']");
        Matcher regExMatcher = Pattern.compile(DIALECT_REGEX).matcher(dialect);
        String dbmsName = null;
        if (regExMatcher.find()) {
            dbmsName = regExMatcher.group(1);
        }
        return dbmsName;
    }

    public DBItemInventoryOperatingSystem getOsData(DBItemInventoryInstance schedulerInstanceItem) {
        Properties props = System.getProperties();
        DBItemInventoryOperatingSystem os = new DBItemInventoryOperatingSystem();
        String osNameFromProperty = props.get("os.name").toString();
        try {
            if (osNameFromProperty.toLowerCase().contains("windows")) {
                os.setName("Windows");
                os.setDistribution(getDistributionInfo("cmd.exe","/c", "ver"));
            } else if (osNameFromProperty.toLowerCase().contains("linux")) {
                os.setName("Linux");
                String completeOsString = getDistributionInfo("/bin/sh","-c", "cat /etc/*-release");
                os.setDistribution(getDistributionForLinux(completeOsString));
            }
        } catch (Exception e) {
            LOGGER.error(e.getCause() + ":" + e.getMessage(), e);
        }
        String osArch = props.getProperty("os.arch");
        if (osArch.contains("64")) {
            os.setArchitecture("x64");
        } else {
            os.setArchitecture("x86");
        }
        os.setHostname(schedulerInstanceItem.getHostname());
        // this has to happen at db level only, if entry exist in db set modified else set created AND modified (NOT NULL IN DB)
        os.setCreated(new Date());
        os.setModified(new Date());
        return os;
    }

    private String getDistributionInfo(String... commands) throws Exception {
        ProcessBuilder builder = new ProcessBuilder();
        builder.command(commands);
        Process process = builder.start();
        process.waitFor();
        InputStream out = process.getInputStream();
        StringBuilder outContent = new StringBuilder();
        byte[] tmp = new byte[1024];
        while (out.available() > 0) {
            int i = out.read(tmp, 0, 1024);
            if (i < 0) {
                break;
            }
            outContent.append(new String(tmp, 0, i));
        }
        return outContent.toString().trim();
    }
    
    private String getDistributionForLinux(String runtimeOutput) {
        Matcher regExMatcher = Pattern.compile(NEWLINE_REGEX).matcher(runtimeOutput);
        String distribution = null;
        if (regExMatcher.find()) {
            distribution = regExMatcher.group(1);
        }
        return distribution;
    }
    
    // TODO: parameter type has to be changed to DBItem when ready
    private void insertOrUpdateDB(DBItemInventoryInstance schedulerInstanceItem, DBItemInventoryOperatingSystem osItem) {
        // TODO: if id exists in db then update else insert
    }

    public Date getDateFromISO8601String(String dateString) {
        return Date.from(Instant.parse(dateString));
    }

    public String getHibernateConfigFileName() {
        return schedulerHibernateConfigFileName;
    }

    public void setHibernateConfigFileName(String hibernateConfigFileName) {
        this.schedulerHibernateConfigFileName = hibernateConfigFileName;
    }
    
}