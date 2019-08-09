package com.sos.jitl.inventory.helper;

import java.io.StringReader;
import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonString;

import org.apache.http.client.utils.URIBuilder;
import org.hibernate.query.Query;

import com.sos.exception.SOSBadRequestException;
import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.hibernate.exceptions.SOSHibernateException;
import com.sos.jitl.reporting.db.DBItemInventoryAgentInstance;
import com.sos.jitl.reporting.db.DBItemInventoryInstance;
import com.sos.jitl.reporting.db.DBItemInventoryOperatingSystem;
import com.sos.jitl.reporting.db.DBLayer;
import com.sos.jitl.restclient.JobSchedulerRestApiClient;


public class AgentHelper {

    private static final String MASTER_WEBSERVICE_URL_APPEND = "/jobscheduler/master/api/agent/";
    private static final String AGENT_WEBSERVICE_URL_APPEND = "/jobscheduler/agent/api";
    private static final String ACCEPT_HEADER = "Accept";
    private static final String CONTENT_TYPE_HEADER = "Content-Type";
    private static final String APPLICATION_HEADER_VALUE = "application/json";

    public static DBItemInventoryAgentInstance createNewAgent(DBItemInventoryInstance masterInstance, String agentUrl, 
            SOSHibernateSession connection, boolean transactionAlreadyStarted) throws SOSHibernateException, Exception {
        DBItemInventoryAgentInstance agent = new DBItemInventoryAgentInstance();
        StringBuilder connectTo = new StringBuilder();
        connectTo.append("http://127.0.0.1:");
        connectTo.append(masterInstance.getPort());
        connectTo.append(MASTER_WEBSERVICE_URL_APPEND);
        connectTo.append(agentUrl);
        connectTo.append(AGENT_WEBSERVICE_URL_APPEND);
        URIBuilder uriBuilder = new URIBuilder(connectTo.toString());
        agent.setInstanceId(masterInstance.getId());
        InventoryAgentCallable callable = new InventoryAgentCallable(uriBuilder, agent, agentUrl);
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        Future<CallableAgent> future = executorService.submit(callable);
        try {
            CallableAgent ca = future.get();
            if (ca != null) {
                agent = ca.getAgent();
                JsonObject result = ca.getResult();
                if (result != null) {
                    JsonObject system = result.getJsonObject("system");
                    agent.setHostname(system.getString("hostname"));
                    // OS Information from Agent
                    JsonObject javaResult = result.getJsonObject("java");
                    JsonObject systemProps = javaResult.getJsonObject("systemProperties");
                    agent.setState(0);
                    DBItemInventoryOperatingSystem os = getOperatingSystem(agent.getHostname(), connection);
                    if (os == null) {
                        os = new DBItemInventoryOperatingSystem();
                        JsonString distributionFromJsonAnswer = system.getJsonString("distribution");
                        if (distributionFromJsonAnswer != null) {
                            os.setDistribution(distributionFromJsonAnswer.getString());
                        } else {
                            os.setDistribution(systemProps.getString("os.version"));
                        }
                        os.setArchitecture(systemProps.getString("os.arch"));
                        os.setName(systemProps.getString("os.name"));
                        os.setHostname(getHostnameFromAgentUrl(agent.getUrl()));
                        Long osId = saveOrUpdateOperatingSystem(os, connection, transactionAlreadyStarted);
                        agent.setOsId(osId);
                    } else {
                        agent.setOsId(os.getId());
                    }
                    agent.setStartedAt(getDateFromISO8601String(result.getString("startedAt")));
                    String version = result.getString("version");
                    if (version.length() > 30) {
                        agent.setVersion(version.substring(0, 30));
                    } else {
                        agent.setVersion(version);
                    }
                }
            }
        } catch (ExecutionException e) {
            executorService.shutdown();
            if(e.getCause() != null) {
                throw (Exception)e.getCause();
            }
        } catch (SOSHibernateException e) {
            executorService.shutdown();
            throw e;
        }
        executorService.shutdown();
        return agent;
    }
    
    public static List<DBItemInventoryAgentInstance> getAgentInstances(DBItemInventoryInstance masterInstance,
            SOSHibernateSession connection, boolean transactionAlreadyStarted, String httpPort) throws SOSHibernateException, Exception {
        List<DBItemInventoryAgentInstance> agentInstances = new ArrayList<DBItemInventoryAgentInstance>();
        List<InventoryAgentCallable> callables = new ArrayList<InventoryAgentCallable>();
        for (String agentUrl : getAgentInstanceUrls(masterInstance, httpPort)) {
            StringBuilder connectTo = new StringBuilder();
            connectTo.append("http://");
            connectTo.append(HttpHelper.getHttpHost(httpPort, "127.0.0.1"));
            connectTo.append(":");
            connectTo.append(HttpHelper.getHttpPort(httpPort));
            connectTo.append(MASTER_WEBSERVICE_URL_APPEND);
            connectTo.append(agentUrl);
            connectTo.append(AGENT_WEBSERVICE_URL_APPEND);
            URIBuilder uriBuilder = new URIBuilder(connectTo.toString());
            DBItemInventoryAgentInstance agentInstance = new DBItemInventoryAgentInstance();
            agentInstance.setInstanceId(masterInstance.getId());
            InventoryAgentCallable callable = new InventoryAgentCallable(uriBuilder, agentInstance, agentUrl);
            callables.add(callable);
        }
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        for (Future<CallableAgent> future : executorService.invokeAll(callables)) {
            try {
                CallableAgent ca = future.get();
                if (ca != null) {
                    DBItemInventoryAgentInstance agentInstance = ca.getAgent();
                    JsonObject result = ca.getResult();
                    if (result != null) {
                        JsonObject system = result.getJsonObject("system");
                        agentInstance.setHostname(system.getString("hostname"));
                        // OS Information from Agent
                        JsonObject javaResult = result.getJsonObject("java");
                        JsonObject systemProps = javaResult.getJsonObject("systemProperties");
                        agentInstance.setState(0);
                        DBItemInventoryOperatingSystem os = getOperatingSystem(agentInstance.getHostname(), connection);
                        if (os == null) {
                            os = new DBItemInventoryOperatingSystem();
                            JsonString distributionFromJsonAnswer = system.getJsonString("distribution");
                            if (distributionFromJsonAnswer != null) {
                                os.setDistribution(distributionFromJsonAnswer.getString());
                            } else {
                                os.setDistribution(systemProps.getString("os.version"));
                            }
                            os.setArchitecture(systemProps.getString("os.arch"));
                            os.setName(systemProps.getString("os.name"));
                            os.setHostname(getHostnameFromAgentUrl(agentInstance.getUrl()));
                            Long osId = saveOrUpdateOperatingSystem(os, connection, transactionAlreadyStarted);
                            agentInstance.setOsId(osId);
                        } else {
                            agentInstance.setOsId(os.getId());
                        }
                        agentInstance.setStartedAt(getDateFromISO8601String(result.getString("startedAt")));
                        String version = result.getString("version");
                        if (version.length() > 30) {
                            agentInstance.setVersion(version.substring(0, 30));
                        } else {
                            agentInstance.setVersion(version);
                        }
                    }
                    agentInstances.add(agentInstance);
                }
            } catch (ExecutionException e) {
                executorService.shutdown();
                if(e.getCause() != null) {
                    throw (Exception)e.getCause();
                }
            } catch (SOSHibernateException e) {
                executorService.shutdown();
                throw e;
            }
        }
        executorService.shutdown();
        return agentInstances;
    }

    public static List<String> getAgentInstanceUrls(DBItemInventoryInstance masterInstance, String httpPort) throws Exception {
        List<String> agentInstanceUrls = new ArrayList<String>();
        StringBuilder connectTo = new StringBuilder();
        connectTo.append("http://");
        connectTo.append(HttpHelper.getHttpHost(httpPort, "127.0.0.1"));
        connectTo.append(":");
        connectTo.append(HttpHelper.getHttpPort(httpPort));
        connectTo.append(MASTER_WEBSERVICE_URL_APPEND);
        URIBuilder uriBuilder = new URIBuilder(connectTo.toString());
        JsonObject result = getJsonObjectFromResponse(uriBuilder.build());
        for (JsonString element : result.getJsonArray("elements").getValuesAs(JsonString.class)) {
            agentInstanceUrls.add(element.getString().toLowerCase());
        }
        return agentInstanceUrls;
    }

    private static Date getDateFromISO8601String(String dateString) {
        try {
            return Date.from(Instant.parse(dateString));
        } catch (Exception e) {
            return Date.from(Instant.now());
        }
    }

    private static DBItemInventoryOperatingSystem getOperatingSystem(String schedulerHost, SOSHibernateSession connection)
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

    private static String getHostnameFromAgentUrl(String url) {
        return url.substring(url.lastIndexOf("/") + 1, url.lastIndexOf(":"));
    }

    private static Long saveOrUpdateOperatingSystem(DBItemInventoryOperatingSystem osItem, String hostname,
            SOSHibernateSession connection, boolean transactionAlreadyStarted) throws SOSHibernateException, Exception {
        DBItemInventoryOperatingSystem osFromDb = getOperatingSystem(hostname, connection);
        Instant newDate = Instant.now();
        if (osFromDb != null) {
            osFromDb.setArchitecture(osItem.getArchitecture());
            osFromDb.setDistribution(osItem.getDistribution());
            osFromDb.setName(osItem.getName());
            osFromDb.setModified(Date.from(newDate));
            if (!transactionAlreadyStarted) {
                connection.beginTransaction();
            }
            connection.update(osFromDb);
            if (!transactionAlreadyStarted) {
                connection.commit();
            }
            return osFromDb.getId();
        } else {
            osItem.setCreated(Date.from(newDate));
            osItem.setModified(Date.from(newDate));
            if (!transactionAlreadyStarted) {
                connection.beginTransaction();
            }
            connection.save(osItem);
            if (!transactionAlreadyStarted) {
                connection.commit();
            }
            return osItem.getId();
        }
    }

    private static Long saveOrUpdateOperatingSystem(DBItemInventoryOperatingSystem osItem, SOSHibernateSession connection,
            boolean transactionAlreadyStarted) throws SOSHibernateException, Exception {
        return saveOrUpdateOperatingSystem(osItem, osItem.getHostname(), connection, transactionAlreadyStarted);
    }

    private static JsonObject getJsonObjectFromResponse(URI uri) throws Exception {
        JobSchedulerRestApiClient client = new JobSchedulerRestApiClient();
        client.addHeader(CONTENT_TYPE_HEADER, APPLICATION_HEADER_VALUE);
        client.addHeader(ACCEPT_HEADER, APPLICATION_HEADER_VALUE);
        client.setSocketTimeout(60000);
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
            if (json != null) {
                throw new SOSBadRequestException(json.getString("message"));
            } else {
                throw new SOSBadRequestException("Unexpected content type '" + contentType + "'. Response: " + response);
            }
        default:
            throw new Exception(httpReplyCode + " " + client.getHttpResponse().getStatusLine().getReasonPhrase());
        }
    }

}
