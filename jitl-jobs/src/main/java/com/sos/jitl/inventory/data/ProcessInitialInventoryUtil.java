package com.sos.jitl.inventory.data;

import java.io.InputStream;
import java.io.StringReader;
import java.net.InetAddress;
import java.net.URI;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonString;

import org.apache.http.client.utils.URIBuilder;
import org.hibernate.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.sos.hibernate.classes.SOSHibernateConnection;
import com.sos.jitl.reporting.db.DBItemInventoryAgentInstance;
import com.sos.jitl.reporting.db.DBItemInventoryInstance;
import com.sos.jitl.reporting.db.DBItemInventoryOperatingSystem;
import com.sos.jitl.reporting.db.DBLayer;
import com.sos.jitl.restclient.JobSchedulerRestApiClient;

import sos.xml.SOSXMLXPath;

public class ProcessInitialInventoryUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessInitialInventoryUtil.class);
    private static final String DIALECT_REGEX = "org\\.hibernate\\.dialect\\.(.*?)(?:\\d*InnoDB|\\d+[ig]?)?Dialect";
    private static final String NEWLINE_REGEX = "^([^\r\n]*).*";
    private static final String MASTER_WEBSERVICE_URL_APPEND = "/jobscheduler/master/api/agent/";
    private static final String AGENT_WEBSERVICE_URL_APPEND = "/jobscheduler/agent/api";
    private static final String ACCEPT_HEADER = "Accept";
    private static final String CONTENT_TYPE_HEADER = "Content-Type";
    private static final String APPLICATION_HEADER_VALUE = "application/json";
    SOSHibernateConnection connection;
    private String supervisorHost = null;
    private String supervisorPort = null;

    public ProcessInitialInventoryUtil() {

    }

    public ProcessInitialInventoryUtil(SOSHibernateConnection connection) {
        this.connection = connection;
    }

    public DBItemInventoryInstance process(SOSXMLXPath xPath, Path liveDirectory, Path schedulerHibernateConfigFileName, String url)
            throws Exception {
        DBItemInventoryInstance jsInstanceItem = getDataFromJobscheduler(xPath, liveDirectory, schedulerHibernateConfigFileName, url);
        DBItemInventoryOperatingSystem osItem = getOsData(jsInstanceItem);
        return insertOrUpdateDB(jsInstanceItem, osItem);
    }

    private DBItemInventoryInstance getDataFromJobscheduler(SOSXMLXPath xPath, Path liveDirectory, Path schedulerHibernateConfigFileName, String url)
            throws Exception {
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
                throw e;
            }
        } else {
            jsInstance.setPort(0);
        }
        jsInstance.setUrl(url);
        String tcpPort = stateElement.getAttribute("tcp_port");
        if(tcpPort == null || tcpPort.isEmpty()) {
            tcpPort = "0";
        }
        String canonicalHost = InetAddress.getByName(jsInstance.getHostname()).getCanonicalHostName().toLowerCase();
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
                String precedence =
                        xPath.selectSingleNodeValue(clusterNode, "cluster_member[@cluster_member_id='"
                                + clusterNode.getAttribute("cluster_member_id") + "']/@backup_precedence", "0");
                jsInstance.setPrecedence(Integer.parseInt(precedence));
            }
        } else {
            jsInstance.setClusterType("standalone");
        }
        jsInstance.setDbmsName(getDbmsName(schedulerHibernateConfigFileName));
        jsInstance.setDbmsVersion(getDbVersion(jsInstance.getDbmsName()));
        jsInstance.setLiveDirectory(liveDirectory.toString().replace('\\', '/'));
        // TODO hier immer null, supervisor from scheduler.xml
        if (supervisorHost != null && supervisorPort != null) {
            String supervisorUrl = supervisorHost + ":" + supervisorPort; 
            DBItemInventoryInstance supervisorFromDb = getSupervisorInstanceFromDb(supervisorUrl);
            if (supervisorFromDb != null) {
                jsInstance.setSupervisorId(supervisorFromDb.getId());
            } else {
                jsInstance.setSupervisorId(null);
            }
        } else {
            jsInstance.setSupervisorId(null);
        }
        return jsInstance;
    }

    @SuppressWarnings("unchecked")
    private DBItemInventoryInstance getSupervisorInstanceFromDb() throws Exception {
        // only ID is relevant
        StringBuilder sql = new StringBuilder();
        sql.append("from ").append(DBLayer.DBITEM_INVENTORY_INSTANCES);
        sql.append(" where hostname = :hostname");
        sql.append(" and port = :port");
        Query query = connection.createQuery(sql.toString());
        query.setParameter("hostname", supervisorHost);
        query.setParameter("port", supervisorPort);
        List<DBItemInventoryInstance> result = query.list();
        if (result != null && !result.isEmpty()) {
            return result.get(0);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private DBItemInventoryInstance getSupervisorInstanceFromDb(String commandUrl) throws Exception {
        // only ID is relevant
        StringBuilder sql = new StringBuilder();
        sql.append("from ").append(DBLayer.DBITEM_INVENTORY_INSTANCES);
        sql.append(" where commandUrl = :commandUrl");
        Query query = connection.createQuery(sql.toString());
        query.setParameter("commandUrl", commandUrl);
        List<DBItemInventoryInstance> result = query.list();
        if (result != null && !result.isEmpty()) {
            return result.get(0);
        }
        return null;
    }

    public String getDbmsName(Path hibernateConfigFile) throws Exception {
        SOSXMLXPath xPath = new SOSXMLXPath(hibernateConfigFile);
        String dialect = xPath.selectSingleNodeValue("/hibernate-configuration/session-factory/property[@name='hibernate.dialect']");
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
            // TODO Solaris? AIX? etc...
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

    private Long saveOrUpdateSchedulerInstance(DBItemInventoryInstance schedulerInstanceItem) throws Exception {
        StringBuilder sql = new StringBuilder();
        sql.append("select id from ");
        sql.append(DBLayer.DBITEM_INVENTORY_OPERATING_SYSTEMS);
        sql.append(" where upper(hostname) = :hostname");
        Query query = connection.createQuery(sql.toString());
        query.setParameter("hostname", schedulerInstanceItem.getHostname().toUpperCase());
        Long osId = (Long) query.uniqueResult();
        DBItemInventoryInstance schedulerInstanceFromDb =
                getInventoryInstance(schedulerInstanceItem.getSchedulerId(), schedulerInstanceItem.getHostname(), schedulerInstanceItem.getPort());
        Instant newDate = Instant.now();
        if (schedulerInstanceFromDb != null) {
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
            if (schedulerInstanceItem.getOsId() == DBLayer.DEFAULT_ID && osId != null) {
                schedulerInstanceItem.setOsId(osId);
            }
            schedulerInstanceFromDb.setOsId(schedulerInstanceItem.getOsId());
            schedulerInstanceFromDb.setModified(Date.from(newDate));
            connection.update(schedulerInstanceFromDb);
            return schedulerInstanceFromDb.getId();
        } else {
            // insert
            if (schedulerInstanceItem.getOsId() == DBLayer.DEFAULT_ID) {
                schedulerInstanceItem.setOsId(osId);
            }
            schedulerInstanceItem.setCreated(Date.from(newDate));
            schedulerInstanceItem.setModified(Date.from(newDate));
            connection.save(schedulerInstanceItem);
            return schedulerInstanceItem.getId();
        }
    }

    private Long saveOrUpdateOperatingSystem(DBItemInventoryOperatingSystem osItem, String hostname) throws Exception {
        DBItemInventoryOperatingSystem osFromDb = getOperatingSystem(hostname);
        Instant newDate = Instant.now();
        if (osFromDb != null) {
            osFromDb.setArchitecture(osItem.getArchitecture());
            osFromDb.setDistribution(osItem.getDistribution());
            osFromDb.setName(osItem.getName());
            osFromDb.setModified(Date.from(newDate));
            connection.update(osFromDb);
            return osFromDb.getId();
        } else {
            osItem.setCreated(Date.from(newDate));
            osItem.setModified(Date.from(newDate));
            connection.save(osItem);
            return osItem.getId();
        }
    }

    public Long saveOrUpdateOperatingSystem(DBItemInventoryOperatingSystem osItem) throws Exception {
        return saveOrUpdateOperatingSystem(osItem, osItem.getHostname());
    }

    private Long saveOrUpdateAgentInstance(DBItemInventoryAgentInstance agentItem) throws Exception {
        DBItemInventoryAgentInstance agentFromDb = getAgentInstance(agentItem.getUrl(), agentItem.getInstanceId());
        Instant newDate = Instant.now();
        if (agentFromDb != null) {
            agentFromDb.setStartedAt(agentItem.getStartedAt());
            agentFromDb.setState(agentItem.getState());
            agentFromDb.setModified(Date.from(newDate));
            connection.update(agentFromDb);
            return agentFromDb.getId();
        } else {
            agentItem.setCreated(Date.from(newDate));
            agentItem.setModified(Date.from(newDate));
            connection.save(agentItem);
            return agentItem.getId();
        }
    }

    @SuppressWarnings("unchecked")
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
        List<Object> result = query.list();
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

    private DBItemInventoryInstance insertOrUpdateDB(DBItemInventoryInstance schedulerInstanceItem, DBItemInventoryOperatingSystem osItem)
            throws Exception {
        try {
            connection.beginTransaction();
            Long osId = saveOrUpdateOperatingSystem(osItem, schedulerInstanceItem.getHostname());
            if (osItem.getId() != null && osItem.getId() != DBLayer.DEFAULT_ID) {
                schedulerInstanceItem.setOsId(osItem.getId());
            } else if (osId != null && osId != DBLayer.DEFAULT_ID) {
                schedulerInstanceItem.setOsId(osId);
            }
            Long instanceId = saveOrUpdateSchedulerInstance(schedulerInstanceItem);
            if (schedulerInstanceItem.getId() == null || schedulerInstanceItem.getId() == DBLayer.DEFAULT_ID) {
                schedulerInstanceItem.setId(instanceId);
            }
            List<DBItemInventoryAgentInstance> agentInstances = getAgentInstances(schedulerInstanceItem);
            for (DBItemInventoryAgentInstance agent : agentInstances) {
                agent.setInstanceId(instanceId);
                LOGGER.debug("hostname: " + agent.getHostname());
                LOGGER.debug("instanceId: " + agent.getInstanceId());
                LOGGER.debug("osId: " + agent.getOsId());
                LOGGER.debug("state: " + agent.getState());
                LOGGER.debug("startedAt: " + agent.getStartedAt());
                Long id = saveOrUpdateAgentInstance(agent);
                LOGGER.debug("agent Instance with id = " + id + " and url = " + agent.getUrl() + " saved!");
            }
            connection.commit();
            return schedulerInstanceItem;
        } catch (Exception e) {
            connection.rollback();
            throw e;
        }
    }

    public Date getDateFromISO8601String(String dateString) {
        try {
            return Date.from(Instant.parse(dateString));
        } catch (Exception e) {
            return Date.from(Instant.now());
        }
    }

    @SuppressWarnings("unchecked")
    private DBItemInventoryInstance getInventoryInstance(String schedulerId, String schedulerHost, Integer schedulerPort) throws Exception {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("from ");
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

    @SuppressWarnings("unchecked")
    private DBItemInventoryOperatingSystem getOperatingSystem(String schedulerHost) throws Exception {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("from ");
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

    @SuppressWarnings("unchecked")
    private DBItemInventoryAgentInstance getAgentInstance(String url, Long instanceId) throws Exception {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("from ");
            sql.append(DBLayer.DBITEM_INVENTORY_AGENT_INSTANCES);
            sql.append(" where url = :url");
            sql.append(" and instanceId = :instanceId");
            sql.append(" order by id asc");
            Query query = connection.createQuery(sql.toString());
            query.setParameter("url", url);
            query.setParameter("instanceId", instanceId);
            List<DBItemInventoryAgentInstance> result = query.list();
            if (!result.isEmpty()) {
                return result.get(0);
            }
            return null;
        } catch (Exception ex) {
            throw new Exception(SOSHibernateConnection.getException(ex));
        }
    }

    private List<String> getAgentInstanceUrls(DBItemInventoryInstance masterInstance) throws Exception {
        List<String> agentInstanceUrls = new ArrayList<String>();
        StringBuilder connectTo = new StringBuilder();
        connectTo.append(masterInstance.getUrl());
        connectTo.append(MASTER_WEBSERVICE_URL_APPEND);
        URIBuilder uriBuilder = new URIBuilder(connectTo.toString());
        JsonObject result = getJsonObjectFromResponse(uriBuilder.build());
        for (JsonString element : result.getJsonArray("elements").getValuesAs(JsonString.class)) {
            agentInstanceUrls.add(element.getString().toLowerCase());
        }
        return agentInstanceUrls;
    }

    private List<DBItemInventoryAgentInstance> getAgentInstances(DBItemInventoryInstance masterInstance) throws Exception {
        List<DBItemInventoryAgentInstance> agentInstances = new ArrayList<DBItemInventoryAgentInstance>();
        for (String agentUrl : getAgentInstanceUrls(masterInstance)) {
            StringBuilder connectTo = new StringBuilder();
            connectTo.append(masterInstance.getUrl());
            connectTo.append(MASTER_WEBSERVICE_URL_APPEND);
            connectTo.append(agentUrl);
            connectTo.append(AGENT_WEBSERVICE_URL_APPEND);
            URIBuilder uriBuilder = new URIBuilder(connectTo.toString());
            DBItemInventoryAgentInstance agentInstance = new DBItemInventoryAgentInstance();
            agentInstance.setInstanceId(masterInstance.getId());
            try {
                JsonObject result = getJsonObjectFromResponse(uriBuilder.build());
                if (result != null) {
                    JsonObject system = result.getJsonObject("system");
                    agentInstance.setHostname(system.getString("hostname"));
                    JsonString distributionFromJsonAnswer = system.getJsonString("distribution");
                    // OS Information from Agent
                    DBItemInventoryOperatingSystem os = getOperatingSystem(agentInstance.getHostname());
                    JsonObject javaResult = result.getJsonObject("java");
                    JsonObject systemProps = javaResult.getJsonObject("systemProperties");
                    if (os == null) {
                        os = new DBItemInventoryOperatingSystem();
                        if (distributionFromJsonAnswer != null) {
                            os.setDistribution(distributionFromJsonAnswer.getString());
                        } else {
                            os.setDistribution(systemProps.getString("os.version"));
                        }
                        os.setArchitecture(systemProps.getString("os.arch"));
                        os.setName(systemProps.getString("os.name"));
                        os.setHostname(getHostnameFromAgentUrl(agentUrl));
                        Long osId = saveOrUpdateOperatingSystem(os);
                        agentInstance.setOsId(osId);
                    } else {
                        agentInstance.setOsId(os.getId());
                    }
                    agentInstance.setStartedAt(getDateFromISO8601String(result.getString("startedAt")));
                    agentInstance.setState(0);
                    agentInstance.setUrl(agentUrl);
                    String version = result.getString("version");
                    if (version.length() > 30) {
                        agentInstance.setVersion(version.substring(0, 30));
                    } else {
                        agentInstance.setVersion(version);
                    }
                } else {
                    agentInstance.setHostname(null);
                    agentInstance.setOsId(0L);
                    agentInstance.setStartedAt(null);
                    agentInstance.setState(1);
                    agentInstance.setUrl(agentUrl);
                    agentInstance.setVersion(null);
                }
                agentInstances.add(agentInstance);
            } catch (Exception e) {
                // do Nothing
            }
        }
        return agentInstances;
    }

    private String getHostnameFromAgentUrl(String url) {
        return url.substring(url.lastIndexOf("/") + 1, url.lastIndexOf(":"));
    }

    private JsonObject getJsonObjectFromResponse(URI uri) throws Exception {
        JobSchedulerRestApiClient client = new JobSchedulerRestApiClient();
        client.addHeader(CONTENT_TYPE_HEADER, APPLICATION_HEADER_VALUE);
        client.addHeader(ACCEPT_HEADER, APPLICATION_HEADER_VALUE);
        String response = client.getRestService(uri);
        int httpReplyCode = client.statusCode();
        String contentType = client.getResponseHeader(CONTENT_TYPE_HEADER);
        JsonObject json = null;
        if (contentType.contains(APPLICATION_HEADER_VALUE)) {
            JsonReader rdr = Json.createReader(new StringReader(response));
            json = rdr.readObject();
        }
        switch (httpReplyCode) {
        case 200:
            if (json != null) {
                return json;
            } else {
                throw new Exception("Unexpected content type '" + contentType + "'. Response: " + response);
            }
        case 400:
            // TODO check Content-Type
            // for now the exception is plain/text instead of JSON
            // throw message item value
            if (json != null) {
                throw new Exception(json.getString("message"));
            } else {
                throw new Exception("Unexpected content type '" + contentType + "'. Response: " + response);
            }
        default:
            throw new Exception(httpReplyCode + " " + client.getHttpResponse().getStatusLine().getReasonPhrase());
        }
    }

    public void setSupervisorHost(String supervisorHost) {
        this.supervisorHost = supervisorHost;
    }

    public void setSupervisorPort(String supervisorPort) {
        this.supervisorPort = supervisorPort;
    }
}