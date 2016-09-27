package com.sos.jitl.runonce.data;

import java.io.InputStream;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import sos.xml.SOSXMLXPath;

import com.sos.hibernate.classes.SOSHibernateConnection;
import com.sos.jitl.reporting.db.DBItemInventoryInstance;
import com.sos.jitl.reporting.db.DBItemInventoryOperatingSystem;
import com.sos.jitl.reporting.db.DBLayer;

public class ProcessDataUtil {

    private static final String DIALECT_REGEX = "org\\.hibernate\\.dialect\\.(.*?)(?:\\d*InnoDB|\\d+[ig]?)?Dialect";
    private static final String NEWLINE_REGEX = "^([^\r\n]*).*";
    private static final Logger LOGGER = Logger.getLogger(ProcessDataUtil.class);
    private String schedulerHibernateConfigFileName;
    private String webserviceHibernateConfigFileName;
    private String liveDirectory;
    SOSHibernateConnection connection;
    
    public ProcessDataUtil() {

    }

    public ProcessDataUtil(String hibernateCfgXml, SOSHibernateConnection connection) {
        this.schedulerHibernateConfigFileName = hibernateCfgXml;
        this.connection = connection;
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
            jsInstance.setUrl("http://" + jsInstance.getHostname() + ":" + httpPort);
        }
        jsInstance.setCommandUrl("http://" + jsInstance.getHostname() + ":" + jsInstance.getPort().toString());
        jsInstance.setTimeZone(stateElement.getAttribute("time_zone"));
        String spoolerRunningSince = stateElement.getAttribute("spooler_running_since");
        if(spoolerRunningSince != null) {
            jsInstance.setStartedAt(getDateFromISO8601String(spoolerRunningSince));
        }
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
        jsInstance.setDbmsVersion(getDbVersion(jsInstance.getDbmsName()));
        jsInstance.setLiveDirectory(getLiveDirectory());
        jsInstance.setSupervisorId(null);
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
    
    private void saveOrUpdateSchedulerInstance (DBItemInventoryInstance schedulerInstanceItem) throws Exception {
        Long osId = null;
        Query query = connection.createQuery("select id from " + DBLayer.DBITEM_INVENTORY_OPERATING_SYSTEMS + " where hostname = :hostname");
        query.setParameter("hostname", schedulerInstanceItem.getHostname());
        List result = query.list();
        if (!result.isEmpty()) {
            osId = Long.valueOf(result.get(0).toString());
        }
        connection.beginTransaction();
        DBItemInventoryInstance schedulerInstanceFromDb = getInventoryInstance(schedulerInstanceItem.getSchedulerId(), schedulerInstanceItem.getHostname(),
                schedulerInstanceItem.getPort());
        Instant newDate = Instant.now();
        if(schedulerInstanceFromDb != null) {
            // update
            schedulerInstanceFromDb.setLiveDirectory(schedulerInstanceItem.getLiveDirectory());
            schedulerInstanceFromDb.setCommandUrl(schedulerInstanceItem.getCommandUrl());
            schedulerInstanceFromDb.setUrl(schedulerInstanceItem.getUrl());
            schedulerInstanceFromDb.setClusterType(schedulerInstanceItem.getClusterType());
            schedulerInstanceFromDb.setPrecedence(schedulerInstanceItem.getPrecedence());
            schedulerInstanceFromDb.setDbmsName(schedulerInstanceItem.getDbmsName());
            schedulerInstanceFromDb.setDbmsVersion(schedulerInstanceItem.getDbmsVersion());
            schedulerInstanceFromDb.setSupervisorId(schedulerInstanceItem.getSupervisorId());
            schedulerInstanceFromDb.setStartedAt(schedulerInstanceItem.getStartedAt());
            schedulerInstanceFromDb.setVersion(schedulerInstanceItem.getVersion());
            schedulerInstanceFromDb.setTimeZone(schedulerInstanceItem.getTimeZone());
            if(schedulerInstanceItem.getOsId() == DBLayer.DEFAULT_ID) {
                schedulerInstanceItem.setOsId(osId);
            }
            schedulerInstanceFromDb.setOsId(schedulerInstanceItem.getOsId());
            schedulerInstanceFromDb.setModified(Date.from(newDate));
            connection.update(schedulerInstanceFromDb);
            connection.commit();
        } else {
            // insert
            if(schedulerInstanceItem.getOsId() == DBLayer.DEFAULT_ID) {
                schedulerInstanceItem.setOsId(osId);
            }
            schedulerInstanceItem.setCreated(Date.from(newDate));
            schedulerInstanceItem.setModified(Date.from(newDate));
            connection.save(schedulerInstanceItem);
            connection.commit();
        }
    }
    
    private Long saveOrUpdateOperationgSystem(DBItemInventoryOperatingSystem osItem, String hostname) throws Exception {
        connection.beginTransaction();
        DBItemInventoryOperatingSystem osFromDb = getOperationSystem(hostname);
        Instant newDate = Instant.now();
        if(osFromDb != null) {
            osFromDb.setArchitecture(osItem.getArchitecture());
            osFromDb.setDistribution(osItem.getDistribution());
            osFromDb.setName(osItem.getName());
            osFromDb.setModified(Date.from(newDate));
            connection.update(osFromDb);
            connection.commit();
            return osFromDb.getId();
        } else {
            osItem.setCreated(Date.from(newDate));
            osItem.setModified(Date.from(newDate));
            connection.save(osItem);
            connection.commit();
            return osItem.getId();
        }
    }
    
    public String getDbVersion(String dbName) throws Exception {
        String sql = "";
        switch (dbName.toUpperCase()) {
        case "MYSQL":
            sql = "select version()";
            break;
        case "POSTGRESQL":
            sql = "show server_version";
            break;
        case "ORACLE":
            sql = "select * from v$version";
            break;
        case "SQLSERVER":
            sql = "select @@version";
            break;
        }
        Query query = connection.createSQLQuery(sql);
        List result = query.list();
        String version = null;
        if (!result.isEmpty()) {
            version = result.get(0).toString();
        }
        if ("sqlserver".equalsIgnoreCase(dbName)) {
            Matcher regExMatcher = Pattern.compile(NEWLINE_REGEX).matcher(version);
            if (regExMatcher.find()) {
                version = regExMatcher.group(1);
            }
        }
        return version;
    }
    
    public void insertOrUpdateDB(DBItemInventoryInstance schedulerInstanceItem, DBItemInventoryOperatingSystem osItem) throws Exception {
        Long osId = saveOrUpdateOperationgSystem(osItem, schedulerInstanceItem.getHostname());
        if(osId != null) {
            schedulerInstanceItem.setOsId(osItem.getId());
        }
        saveOrUpdateSchedulerInstance(schedulerInstanceItem);
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
    
    private DBItemInventoryInstance getInventoryInstance(String schedulerId, String schedulerHost, Integer schedulerPort) throws Exception {
        try {
            StringBuilder sql = new StringBuilder("from ");
            sql.append(DBLayer.DBITEM_INVENTORY_INSTANCES);
            sql.append(" where upper(schedulerId) = :schedulerId");
            sql.append(" and upper(hostname) = :hostname");
            sql.append(" and port = :port");
            sql.append(" order by id asc");
            Query query = connection.createQuery(sql.toString());
            query.setParameter("schedulerId", schedulerId.toUpperCase());
            query.setParameter("hostname", schedulerHost.toUpperCase());
            query.setParameter("port", schedulerPort);
            List<DBItemInventoryInstance> result = query.list();
            if (!result.isEmpty()) {
                return result.get(0);
            }
            return null;
        } catch (Exception ex) {
            throw new Exception(SOSHibernateConnection.getException(ex));
        }
    }

    private DBItemInventoryOperatingSystem getOperationSystem(String schedulerHost) throws Exception {
        try {
            StringBuilder sql = new StringBuilder("from ");
            sql.append(DBLayer.DBITEM_INVENTORY_OPERATING_SYSTEMS);
            sql.append(" where upper(hostname) = :hostname");
            sql.append(" order by id asc");
            Query query = connection.createQuery(sql.toString());
            query.setParameter("hostname", schedulerHost.toUpperCase());
            List<DBItemInventoryOperatingSystem> result = query.list();
            if (!result.isEmpty()) {
                return result.get(0);
            }
            return null;
        } catch (Exception ex) {
            throw new Exception(SOSHibernateConnection.getException(ex));
        }
    }

    public String getLiveDirectory() {
        return liveDirectory;
    }
    
    public void setLiveDirectory(String liveDirectory) {
        this.liveDirectory = liveDirectory;
    }

}