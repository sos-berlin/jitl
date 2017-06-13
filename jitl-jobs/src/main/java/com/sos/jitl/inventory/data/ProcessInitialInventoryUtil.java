package com.sos.jitl.inventory.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hibernate.query.NativeQuery;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import sos.xml.SOSXMLXPath;

import com.sos.hibernate.classes.SOSHibernateFactory;
import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.hibernate.exceptions.SOSHibernateException;
import com.sos.jitl.inventory.exceptions.SOSInventoryInitialProcessingException;
import com.sos.jitl.inventory.helper.AgentHelper;
import com.sos.jitl.reporting.db.DBItemInventoryAgentInstance;
import com.sos.jitl.reporting.db.DBItemInventoryInstance;
import com.sos.jitl.reporting.db.DBItemInventoryOperatingSystem;
import com.sos.jitl.reporting.db.DBLayer;

public class ProcessInitialInventoryUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessInitialInventoryUtil.class);
    private static final String DIALECT_REGEX = "org\\.hibernate\\.dialect\\.(.*?)(?:\\d*InnoDB|\\d+[ig]?)?Dialect";
    private static final String NEWLINE_REGEX = "^([^\r\n]*).*";
    private SOSHibernateFactory factory;
    private String supervisorHost = null;
    private String supervisorPort = null;
    private Path liveDirectory;

    public ProcessInitialInventoryUtil() {

    }

    public ProcessInitialInventoryUtil(SOSHibernateFactory factory) {
        this.factory = factory;
    }

    public DBItemInventoryInstance process(SOSXMLXPath xPath, Path liveDirectory, Path schedulerHibernateConfigFileName, String url)
            throws Exception {
        this.liveDirectory = liveDirectory;
        DBItemInventoryInstance jsInstanceItem =
                getDataFromJobscheduler(xPath, this.liveDirectory, schedulerHibernateConfigFileName, url);
        DBItemInventoryOperatingSystem osItem = getOsData(jsInstanceItem);
        return insertOrUpdateDB(jsInstanceItem, osItem);
    }

    private DBItemInventoryInstance getDataFromJobscheduler(SOSXMLXPath xPath, Path liveDirectory,
            Path schedulerHibernateConfigFileName, String url) throws Exception {
        SOSHibernateSession connection = factory.openSession();
        DBItemInventoryInstance jsInstance = new DBItemInventoryInstance();
        Element stateElement = (Element) xPath.selectSingleNode("/spooler/answer/state");
        jsInstance.setSchedulerId(stateElement.getAttribute("id"));
        jsInstance.setHostname(stateElement.getAttribute("host"));
        // TCP_PORT AND UDP_PORT NOT NEEDED ANYMORE, ALWAYS USE THE HTTP_PORT!
        jsInstance.setVersion(stateElement.getAttribute("version"));
        String httpPort = stateElement.getAttribute("http_port");
        if (httpPort != null && !httpPort.isEmpty()) {
            try {
                jsInstance.setPort(Integer.parseInt(httpPort));
            } catch (NumberFormatException e) {
                LOGGER.error("http_port not parseable!");
                throw new SOSInventoryInitialProcessingException(e);
            }
        } else {
            jsInstance.setPort(0);
        }
        jsInstance.setUrl(url);
        String httpsPort = stateElement.getAttribute("https_port");
        // TO DO HTTPS processing
        jsInstance.setAuth(getAuthFromFile(jsInstance.getSchedulerId()));
        if (httpsPort != null && !httpsPort.isEmpty() && jsInstance.getAuth() != null && !jsInstance.getAuth().isEmpty()) {
            StringBuilder strb = new StringBuilder();
            strb.append("https://");
            strb.append(jsInstance.getHostname());
            strb.append(":");
            strb.append(httpsPort);
            jsInstance.setUrl(strb.toString());
        }
        String tcpPort = stateElement.getAttribute("tcp_port");
        if (tcpPort == null || tcpPort.isEmpty()) {
            tcpPort = "0";
        }
        String canonicalHost = jsInstance.getHostname();
        jsInstance.setCommandUrl(canonicalHost + ":" + tcpPort);
        jsInstance.setTimeZone(stateElement.getAttribute("time_zone"));
        String spoolerRunningSince = stateElement.getAttribute("spooler_running_since");
        jsInstance.setStartedAt(getDateFromISO8601String(spoolerRunningSince));
        Element clusterNode = (Element) xPath.selectSingleNode(stateElement, "cluster");
        if (clusterNode != null) {
            NodeList clusterMembers = xPath.selectNodeList(clusterNode, "cluster_member[@distributed_orders='yes']");
            if (clusterMembers != null && clusterMembers.getLength() > 0) {
                jsInstance.setClusterType("active");
            } else {
                jsInstance.setClusterType("passive");
                String precedence = xPath.selectSingleNodeValue(clusterNode, "cluster_member[@cluster_member_id='"
                        + clusterNode.getAttribute("cluster_member_id") + "']/@backup_precedence", "0");
                jsInstance.setPrecedence(Integer.parseInt(precedence));
            }
        } else {
            jsInstance.setClusterType("standalone");
        }
        jsInstance.setDbmsName(getDbmsName(schedulerHibernateConfigFileName));
        jsInstance.setDbmsVersion(getDbVersion(jsInstance.getDbmsName(), connection));
        jsInstance.setLiveDirectory(liveDirectory.toString().replace('\\', '/'));
        if (supervisorHost != null && supervisorPort != null) {
            String supervisorUrl = supervisorHost + ":" + supervisorPort;
            DBItemInventoryInstance supervisorFromDb = getSupervisorInstanceFromDb(supervisorUrl, connection);
            if (supervisorFromDb != null) {
                jsInstance.setSupervisorId(supervisorFromDb.getId());
            } else {
                jsInstance.setSupervisorId(null);
            }
        } else {
            jsInstance.setSupervisorId(null);
        }
        connection.close();
        return jsInstance;
    }

    private DBItemInventoryInstance getSupervisorInstanceFromDb(String commandUrl, SOSHibernateSession connection)
            throws SOSHibernateException {
        StringBuilder sql = new StringBuilder();
        sql.append("from ").append(DBLayer.DBITEM_INVENTORY_INSTANCES);
        sql.append(" where lower(commandUrl) = :commandUrl order by modified desc");
        Query<DBItemInventoryInstance> query = connection.createQuery(sql.toString());
        query.setParameter("commandUrl", commandUrl.toLowerCase());
        List<DBItemInventoryInstance> result = query.getResultList();
        if (result != null && !result.isEmpty()) {
            return result.get(0);
        }
        return null;
    }

    public String getDbmsName(Path hibernateConfigFile) throws Exception {
        SOSXMLXPath xPath = new SOSXMLXPath(hibernateConfigFile);
        String dialect =
                xPath.selectSingleNodeValue("/hibernate-configuration/session-factory/property[@name='hibernate.dialect']");
        Matcher regExMatcher = Pattern.compile(DIALECT_REGEX).matcher(dialect);
        String dbmsName = null;
        if (regExMatcher.find()) {
            dbmsName = regExMatcher.group(1);
        }
        return dbmsName;
    }

    private DBItemInventoryOperatingSystem getOsData(DBItemInventoryInstance schedulerInstanceItem) {
        Properties props = System.getProperties();
        DBItemInventoryOperatingSystem os = new DBItemInventoryOperatingSystem();
        String osNameFromProperty = props.get("os.name").toString();
        try {
            if (osNameFromProperty.toLowerCase().contains("windows")) {
                os.setName("Windows");
                os.setDistribution(getDistributionInfo("cmd.exe", "/c", "ver"));
            } else if (osNameFromProperty.toLowerCase().contains("linux")) {
                os.setName("Linux");
                String completeOsString = getDistributionInfo("/bin/sh", "-c", "cat /etc/*-release");
                os.setDistribution(getDistributionForLinux(completeOsString));
            }
        } catch (Exception e) {
            LOGGER.error(e.getCause() + ":" + e.getMessage(), e);
        }
        os.setArchitecture(props.getProperty("os.arch"));
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

    private Long saveOrUpdateSchedulerInstance(DBItemInventoryInstance schedulerInstanceItem, SOSHibernateSession connection)
            throws SOSHibernateException, Exception {
        StringBuilder sql = new StringBuilder();
        sql.append("select id from ");
        sql.append(DBLayer.DBITEM_INVENTORY_OPERATING_SYSTEMS);
        sql.append(" where upper(hostname) = :hostname");
        Query<Long> query = connection.createQuery(sql.toString());
        query.setParameter("hostname", schedulerInstanceItem.getHostname().toUpperCase());
        Long osId = query.getSingleResult();
        DBItemInventoryInstance schedulerInstanceFromDb = getInventoryInstance(schedulerInstanceItem.getSchedulerId(),
                schedulerInstanceItem.getHostname(), schedulerInstanceItem.getPort(), connection);
        Instant newDate = Instant.now();
        if (schedulerInstanceFromDb != null) {
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
            if (schedulerInstanceItem.getOsId() == DBLayer.DEFAULT_ID && osId != null) {
                schedulerInstanceItem.setOsId(osId);
            }
            schedulerInstanceFromDb.setOsId(schedulerInstanceItem.getOsId());
            schedulerInstanceFromDb.setAuth(schedulerInstanceItem.getAuth());
            schedulerInstanceFromDb.setModified(Date.from(newDate));
            connection.beginTransaction();
            connection.update(schedulerInstanceFromDb);
            connection.commit();
            return schedulerInstanceFromDb.getId();
        } else {
            if (schedulerInstanceItem.getOsId() == DBLayer.DEFAULT_ID) {
                schedulerInstanceItem.setOsId(osId);
            }
            schedulerInstanceItem.setCreated(Date.from(newDate));
            schedulerInstanceItem.setModified(Date.from(newDate));
            connection.beginTransaction();
            connection.save(schedulerInstanceItem);
            connection.commit();
            return schedulerInstanceItem.getId();
        }
    }

    private Long saveOrUpdateOperatingSystem(DBItemInventoryOperatingSystem osItem, String hostname, SOSHibernateSession connection)
            throws SOSHibernateException, Exception {
        DBItemInventoryOperatingSystem osFromDb = getOperatingSystem(hostname, connection);
        Instant newDate = Instant.now();
        if (osFromDb != null) {
            osFromDb.setArchitecture(osItem.getArchitecture());
            osFromDb.setDistribution(osItem.getDistribution());
            osFromDb.setName(osItem.getName());
            osFromDb.setModified(Date.from(newDate));
            connection.beginTransaction();
            connection.update(osFromDb);
            connection.commit();
            return osFromDb.getId();
        } else {
            osItem.setCreated(Date.from(newDate));
            osItem.setModified(Date.from(newDate));
            connection.beginTransaction();
            connection.save(osItem);
            connection.commit();
            return osItem.getId();
        }
    }

    public Long saveOrUpdateOperatingSystem(DBItemInventoryOperatingSystem osItem, SOSHibernateSession connection)
            throws SOSHibernateException, Exception {
        return saveOrUpdateOperatingSystem(osItem, osItem.getHostname(), connection);
    }

    private Long saveOrUpdateAgentInstance(DBItemInventoryAgentInstance agentItem, SOSHibernateSession connection)
            throws SOSHibernateException, Exception {
        DBItemInventoryAgentInstance agentFromDb = getAgentInstance(agentItem.getUrl(), agentItem.getInstanceId(), connection);
        Instant newDate = Instant.now();
        if (agentFromDb != null) {
            agentFromDb.setStartedAt(agentItem.getStartedAt());
            agentFromDb.setState(agentItem.getState());
            agentFromDb.setModified(Date.from(newDate));
            connection.beginTransaction();
            connection.update(agentFromDb);
            connection.commit();
            return agentFromDb.getId();
        } else {
            agentItem.setCreated(Date.from(newDate));
            agentItem.setModified(Date.from(newDate));
            connection.beginTransaction();
            connection.save(agentItem);
            connection.commit();
            return agentItem.getId();
        }
    }

    public String getDbVersion(String dbName, SOSHibernateSession connection) {
        String sql = "";
        try {
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
                sql = "select CONVERT(varchar(255), @@version)";
                break;
            }
            Query<Object> query = connection.createNativeQuery(sql);
            List<Object> result = connection.getResultList(query);
            String version = null;
            if (!result.isEmpty()) {
                version = result.get(0).toString();
                LOGGER.debug("DBMS version: " + version);
            }
            if ("sqlserver".equalsIgnoreCase(dbName)) {
                Matcher regExMatcher = Pattern.compile(NEWLINE_REGEX).matcher(version);
                if (regExMatcher.find()) {
                    version = regExMatcher.group(1);
                }
            }
            return version;
        } catch (Exception e) {
            LOGGER.warn(String.format("Could not determine DB Version through native query: [%s]", sql));
            return "";
        }
    }

    private DBItemInventoryInstance insertOrUpdateDB(DBItemInventoryInstance schedulerInstanceItem,
            DBItemInventoryOperatingSystem osItem) throws SOSHibernateException, Exception {
        SOSHibernateSession connection = null;
        try {
            connection = factory.openSession();
            Long osId = saveOrUpdateOperatingSystem(osItem, schedulerInstanceItem.getHostname(), connection);
            if (osItem.getId() != null && osItem.getId() != DBLayer.DEFAULT_ID) {
                schedulerInstanceItem.setOsId(osItem.getId());
            } else if (osId != null && osId != DBLayer.DEFAULT_ID) {
                schedulerInstanceItem.setOsId(osId);
            }
            Long instanceId = saveOrUpdateSchedulerInstance(schedulerInstanceItem, connection);
            if (schedulerInstanceItem.getId() == null || schedulerInstanceItem.getId() == DBLayer.DEFAULT_ID) {
                schedulerInstanceItem.setId(instanceId);
            }
            List<DBItemInventoryAgentInstance> agentInstances = AgentHelper.getAgentInstances(schedulerInstanceItem, connection, false);
            if (agentInstances != null && !agentInstances.isEmpty()) {
                for (DBItemInventoryAgentInstance agent : agentInstances) {
                    if (agent != null) {
                        LOGGER.debug("agent object: " + agent);
                        agent.setInstanceId(instanceId);
                        LOGGER.debug("hostname: " + agent.getHostname());
                        LOGGER.debug("instanceId: " + agent.getInstanceId());
                        LOGGER.debug("osId: " + agent.getOsId());
                        LOGGER.debug("state: " + agent.getState());
                        LOGGER.debug("startedAt: " + agent.getStartedAt());
                        LOGGER.debug("URL: " + agent.getUrl());
                        Long id = saveOrUpdateAgentInstance(agent, connection);
                        LOGGER.debug("agent Instance with id = " + id + " and url = " + agent.getUrl() + " saved or updated!");
                    }
                }
            }
            connection.close();
            return schedulerInstanceItem;
        } catch (Exception e) {
            try {
                connection.rollback();
            } catch (Exception ex) {}
            connection.close();
            throw new SOSInventoryInitialProcessingException(e);
        }
    }

    public Date getDateFromISO8601String(String dateString) {
        try {
            return Date.from(Instant.parse(dateString));
        } catch (Exception e) {
            return Date.from(Instant.now());
        }
    }

    private DBItemInventoryInstance getInventoryInstance(String schedulerId, String schedulerHost, Integer schedulerPort,
            SOSHibernateSession connection) throws SOSHibernateException {
        StringBuilder sql = new StringBuilder();
        sql.append("from ");
        sql.append(DBLayer.DBITEM_INVENTORY_INSTANCES);
        sql.append(" where upper(schedulerId) = :schedulerId");
        sql.append(" and upper(hostname) = :hostname");
        sql.append(" and port = :port");
        sql.append(" order by id asc");
        Query<DBItemInventoryInstance> query = connection.createQuery(sql.toString());
        query.setParameter("schedulerId", schedulerId.toUpperCase());
        query.setParameter("hostname", schedulerHost.toUpperCase());
        query.setParameter("port", schedulerPort);
        List<DBItemInventoryInstance> result = query.getResultList();
        if (!result.isEmpty()) {
            return result.get(0);
        }
        return null;
    }

    private DBItemInventoryOperatingSystem getOperatingSystem(String schedulerHost, SOSHibernateSession connection)
            throws SOSHibernateException {
        StringBuilder sql = new StringBuilder();
        sql.append("from ");
        sql.append(DBLayer.DBITEM_INVENTORY_OPERATING_SYSTEMS);
        sql.append(" where upper(hostname) = :hostname");
        sql.append(" order by id asc");
        Query<DBItemInventoryOperatingSystem> query = connection.createQuery(sql.toString());
        query.setParameter("hostname", schedulerHost.toUpperCase());
        List<DBItemInventoryOperatingSystem> result = query.getResultList();
        if (!result.isEmpty()) {
            return result.get(0);
        }
        return null;
    }

    private DBItemInventoryAgentInstance getAgentInstance(String url, Long instanceId, SOSHibernateSession connection)
            throws SOSHibernateException {
        StringBuilder sql = new StringBuilder();
        sql.append("from ");
        sql.append(DBLayer.DBITEM_INVENTORY_AGENT_INSTANCES);
        sql.append(" where url = :url");
        sql.append(" and instanceId = :instanceId");
        sql.append(" order by id asc");
        Query<DBItemInventoryAgentInstance> query = connection.createQuery(sql.toString());
        query.setParameter("url", url);
        query.setParameter("instanceId", instanceId);
        List<DBItemInventoryAgentInstance> result = query.getResultList();
        if (!result.isEmpty()) {
            return result.get(0);
        }
        return null;
    }

    public void setSupervisorHost(String supervisorHost) {
        this.supervisorHost = supervisorHost;
    }

    public void setSupervisorPort(String supervisorPort) {
        this.supervisorPort = supervisorPort;
    }

    private String getAuthFromFile(String schedulerId) throws Exception {
        boolean user = false;
        boolean configuration = false;
        String userVal = null;
        String phrase = null;
        if (Files.exists(Paths.get("./config/private/private.conf"))) {
            File privateConf = Paths.get("./config/private/private.conf").toFile();
            FileInputStream fis = new FileInputStream(privateConf);
            Reader reader = new BufferedReader(new InputStreamReader(fis));
            StreamTokenizer tokenizer = new StreamTokenizer(reader);
            tokenizer.resetSyntax();
            tokenizer.slashStarComments(true);
            tokenizer.slashSlashComments(true);
            tokenizer.eolIsSignificant(false);
            tokenizer.whitespaceChars(0, 8);
            tokenizer.whitespaceChars(10, 31);
            tokenizer.wordChars(9, 9);
            tokenizer.wordChars(32, 255);
            tokenizer.commentChar('#');
            tokenizer.quoteChar('"');
            tokenizer.quoteChar('\'');
            int ttype = 0;
            while (ttype != StreamTokenizer.TT_EOF) {
                ttype = tokenizer.nextToken();
                String sval = "";
                switch (ttype) {
                case StreamTokenizer.TT_WORD:
                    sval = tokenizer.sval;
                    if (sval.contains(schedulerId)) {
                        user = true;
                        userVal = sval;
                    } else {
                        user = false;
                    }
                    if (sval.contains("{")) {
                        if (sval.contains("jobscheduler.master.auth.users")) {
                            configuration = true;
                        } else {
                            configuration = false;
                        }
                    }
                    break;
                case '"':
                    sval = "\"" + tokenizer.sval + "\"";
                    if (user && configuration) {
                        phrase = sval;
                    }
                    break;
                }
            }
            phrase = phrase.trim();
            phrase = phrase.substring(1, phrase.length() - 1);
            String[] phraseSplit = phrase.split(":");
            if (userVal.replace("=", "").trim().equalsIgnoreCase(schedulerId) && "plain".equalsIgnoreCase(phraseSplit[0])) {
                byte[] upEncoded = Base64.getEncoder().encode((schedulerId + ":" + phraseSplit[1]).getBytes());
                StringBuilder encoded = new StringBuilder();
                for (byte me : upEncoded) {
                    encoded.append((char) me);
                }
                return encoded.toString();
            }
        }
        return null;
    }

}