package sos.scheduler.managed;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import sos.connection.SOSMySQLConnection;
import sos.spooler.Job_chain;
import sos.spooler.Order;
import sos.spooler.Variable_set;
import sos.util.SOSDate;

/** @author andreas pueschel */
public class JobSchedulerManagedUserJob extends JobSchedulerManagedJob {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobSchedulerManagedUserJob.class);
    private int maxOrderCount = 1000;
    private List<Map<String, String>> orders = new ArrayList<Map<String, String>>();
    private Iterator<Map<String, String>> orderIterator = null;
    private String jobChainName = "user_database_statements";

    public boolean spooler_init() {
        if (!super.spooler_init()) {
            return false;
        }
        try {
            if (!(getConnection() instanceof SOSMySQLConnection)) {
                spooler_log.warn("This Job only works with MySQL databases.");
                return false;
            }
            List<Map<String, String>> hostPort = getConnection().getArray(
                            "SELECT \"NAME\", \"WERT\" FROM " + JobSchedulerManagedObject.getTableManagedUserVariables()
                                    + " WHERE \"NAME\"='scheduler_managed_user_job.port' OR" + " \"NAME\"='scheduler_managed_user_job.host'");
            getConnection().commit();
            boolean correctSettings = true;
            if (hostPort.size() < 2) {
                correctSettings = false;
            }
            String schedUdp = "";
            String schedHost = "";
            Iterator<Map<String, String>> it = hostPort.iterator();
            while (it.hasNext()) {
                Map<String, String> line = it.next();
                String name = line.get("name");
                String wert = line.get("wert");
                if (name != null && "scheduler_managed_user_job.port".equals(name)) {
                    schedUdp = wert;
                }
                if (name != null && "scheduler_managed_user_job.host".equals(name)) {
                    schedHost = wert;
                }
            }
            String udp = "" + spooler.udp_port();
            if (!schedUdp.equals(udp) || !schedHost.equals(spooler.hostname())) {
                correctSettings = false;
            }
            if (!correctSettings) {
                getConnection().execute("DELETE FROM " + JobSchedulerManagedObject.getTableManagedUserVariables()
                                + " WHERE \"NAME\"='scheduler_managed_user_job.port'");
                getConnection().execute("DELETE FROM " + JobSchedulerManagedObject.getTableManagedUserVariables()
                                + " WHERE \"NAME\"='scheduler_managed_user_job.host'");
                getConnection().execute("INSERT INTO " + JobSchedulerManagedObject.getTableManagedUserVariables()
                                + " (\"NAME\", \"WERT\") VALUES ('scheduler_managed_user_job.port'," + "'" + udp + "')");
                getConnection().execute("INSERT INTO " + JobSchedulerManagedObject.getTableManagedUserVariables()
                                + " (\"NAME\", \"WERT\") VALUES ('scheduler_managed_user_job.host'," + "'" + spooler.hostname() + "')");
                getConnection().commit();
            }
        } catch (Exception e) {
            try {
                spooler_log.warn("Could not register scheduler host and port in database. SCHEDULER_JOB_RUN" + " will not succeed. " + e);
            } catch (Exception f) {}
        }
        try {
            if (spooler.job_chain_exists(jobChainName)) {
                return true;
            }
            spooler_log.debug3("Creating jobchain user_database_statements.");
            Job_chain jobChain = spooler.create_job_chain();
            jobChain.set_name(jobChainName);
            spooler_log.debug3("Adding job scheduler_managed_user_database_statement to" + " job_chain.");
            jobChain.add_job("scheduler_managed_user_database_statement", "0", "100", "1100");
            jobChain.add_end_state("100");
            jobChain.add_end_state("1100");
            spooler.add_job_chain(jobChain);
        } catch (Exception e) {
            LOGGER.error("Failed to create jobchain " + jobChainName);
            return false;
        }
        return true;
    }

    public boolean spooler_open() {
        try {
            String query =
                    new String("SELECT \"ID\", \"SPOOLER_ID\", \"JOB_CHAIN\", \"PRIORITY\", \"TITLE\""
                            + ", \"JOB_TYPE\", \"SCHEMA\", \"USER_NAME\", \"ACTION\", \"PARAMS\""
                            + ", \"RUN_TIME\", \"NEXT_START\", \"NEXT_TIME\", \"TIMEOUT\", \"DELETED\", \"SUSPENDED\"" + " FROM "
                            + JobSchedulerManagedObject.getTableManagedUserJobs() + " WHERE (\"UPDATED\"=1 OR \"NEXT_TIME\"< %now )"
                            + "   AND (\"SPOOLER_ID\" IS NULL OR \"SPOOLER_ID\"='" + spooler.id() + "')" + " ORDER BY \"NEXT_TIME\" ASC");
            spooler_log.debug3(".. query: " + query.toString());
            this.setOrders(this.getConnection().getArray(query));
            this.getConnection().rollback();
            this.setOrderIterator(this.getOrders().iterator());
        } catch (Exception e) {
            spooler_log.error("spooler_open(): fatal error occurred: " + e.getMessage());
            return false;
        } finally {
            if (this.getConnection() != null) {
                try {
                    this.getConnection().rollback();
                } catch (Exception ex) {}
            }
        }
        return !this.getOrders().isEmpty();
    }

    public boolean spooler_process() {
        Order order = null;
        Map<String, String> orderAttributes = new HashMap<String, String>();
        boolean rc = false;
        try {
            if (!this.getOrderIterator().hasNext()) {
                spooler_log.info("no more orders found in queue");
                return false;
            }
            orderAttributes = this.getOrderIterator().next();
            if (orderAttributes.isEmpty()) {
                spooler_log.warn("no order attributes found in queue");
                return false;
            }
            if (orderAttributes.get("job_chain") == null || orderAttributes.get("job_chain").isEmpty()) {
                orderAttributes.put("job_chain", jobChainName);
            }
            if (!this.spooler.job_chain_exists(orderAttributes.get("job_chain"))) {
                spooler_log.warn("no job chain found for this order: " + orderAttributes.get("job_chain"));
            }
            boolean deleted = false;
            if (orderAttributes.get("deleted") != null) {
                String sDeleted = orderAttributes.get("deleted");
                deleted = !("0".equals(sDeleted.trim()));
            }
            boolean suspended = false;
            if (orderAttributes.get("suspended") != null) {
                String sSuspended = orderAttributes.get("suspended");
                suspended = !("0".equals(sSuspended.trim()));
            }
            if (deleted) {
                spooler_log.debug6("deleted=1, deleting order...");
                getConnection().execute("DELETE FROM " + JobSchedulerManagedObject.getTableManagedUserJobs() + " WHERE \"ID\"="
                                + orderAttributes.get("id"));
                getConnection().commit();
                String answer = spooler.execute_xml("<remove_order job_chain=\"" + orderAttributes.get("job_chain") + "\" order=\""
                                + orderAttributes.get("id") + "\" />");
            } else {
                if (suspended) {
                    spooler_log.debug6("suspended=1, deactivating order...");
                    String answer = spooler.execute_xml("<remove_order job_chain=\"" + orderAttributes.get("job_chain") + "\" order=\""
                                    + orderAttributes.get("id").toString() + "\" />");
                    getConnection().executeUpdate("UPDATE " + JobSchedulerManagedObject.getTableManagedUserJobs() + " SET \"UPDATED\"=0 WHERE \"ID\"="
                                    + orderAttributes.get("id"));
                    getConnection().commit();
                    return orderIterator.hasNext();
                }
                if (this.getMaxOrderCount() > 0
                        && spooler.job_chain(orderAttributes.get("job_chain")).order_count() >= this.getMaxOrderCount()) {
                    spooler_log.info(".. current order [" + orderAttributes.get("id") + "] skipped: order queue length ["
                                    + spooler.job_chain(orderAttributes.get("job_chain")).order_count() + "] exceeds maximum size ["
                                    + this.getMaxOrderCount() + "]");
                    return this.orderIterator.hasNext();
                }
                String command = orderAttributes.get("action");
                String runTime = orderAttributes.get("run_time");
                String hexCommand = JobSchedulerManagedObject.toHexString(command.getBytes("US-ASCII"));
                order = spooler.create_order();
                order.set_id(orderAttributes.get("id"));
                order.set_state("0");
                order.set_priority(Integer.parseInt(orderAttributes.get("priority")));
                if (orderAttributes.get("title") != null) {
                    order.set_title(orderAttributes.get("title"));
                }
                sos.spooler.Variable_set orderData = spooler.create_variable_set();
                orderData.set_var("command", hexCommand);
                orderData.set_var("scheduler_order_schema", orderAttributes.get("schema"));
                orderData.set_var("scheduler_order_user_name", orderAttributes.get("user_name"));
                orderData.set_var("scheduler_order_is_user_job", "1");
                if (orderAttributes.get("params") != null) {
                    String paramsXml = orderAttributes.get("params");
                    if (!paramsXml.isEmpty()) {
                        Variable_set paramsSet = spooler.create_variable_set();
                        paramsSet.set_xml(paramsXml);
                        orderData.merge(paramsSet);
                    }
                }
                order.set_payload(orderData);
                if (runTime != null && !runTime.isEmpty()) {
                    if (isOver(runTime)) {
                        try {
                            spooler_log.debug3("Order " + order.id() + " was not executed at specified runtime. Calculating new runtime.");
                        } catch (Exception e) {}
                        JobSchedulerManagedDatabaseJob.updateRunTime(order, getConnection());
                        runTime = getConnection().getSingleValue("SELECT \"RUN_TIME\" FROM " + JobSchedulerManagedObject.getTableManagedUserJobs()
                                + " WHERE \"ID\"=" + order.id());
                        if (runTime == null || runTime.isEmpty()) {
                            return orderIterator.hasNext();
                        }
                    }
                    spooler_log.debug3("Setting order run_time:" + runTime);
                    order.run_time().set_xml(runTime);
                }
                rc = !(spooler_task.job().order_queue() == null);
                try {
                    spooler.job_chain(orderAttributes.get("job_chain")).add_or_replace_order(order);
                } catch (Exception e) {
                    spooler_log.debug6("an ignorable error occurred while removing and adding order: " + e.getMessage());
                    spooler_log.debug6("will try to add order on next run.");
                    return orderIterator.hasNext();
                }
                getConnection().executeUpdate("UPDATE " + JobSchedulerManagedObject.getTableManagedUserJobs() + " SET \"UPDATED\"=0 WHERE \"ID\"="
                                + orderAttributes.get("id"));
                getConnection().commit();
                spooler_log.info("order [" + orderAttributes.get("id") + "] added to job chain [" + orderAttributes.get("job_chain")
                                + "]: " + order.title());
            }
            return orderIterator.hasNext();
        } catch (Exception e) {
            spooler_log.warn("error occurred processing managed user job" + ((order != null) ? " [" + order.id() + "]" : "") + ": " + e.getMessage());
            spooler_task.end();
            return false;
        } finally {
            try {
                if (this.getConnection() != null) {
                    this.getConnection().rollback();
                }
            } catch (Exception ex) {}
        }
    }

    public Iterator<Map<String, String>> getOrderIterator() {
        return orderIterator;
    }

    public void setOrderIterator(Iterator<Map<String, String>> orderIterator) {
        this.orderIterator = orderIterator;
    }

    public List<Map<String, String>> getOrders() {
        return orders;
    }

    public void setOrders(List<Map<String, String>> orders) {
        this.orders = orders;
    }

    public int getMaxOrderCount() {
        return maxOrderCount;
    }

    public void setMaxOrderCount(int maxOrderCount) {
        this.maxOrderCount = maxOrderCount;
    }

    private boolean isOver(String runTime) {
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document payloadDocument = docBuilder.parse(new ByteArrayInputStream(runTime.getBytes()));
            Node node = payloadDocument.getFirstChild();
            while (node != null && node.getNodeType() != Node.ELEMENT_NODE) {
                node = node.getNextSibling();
            }
            if (node == null) {
                return false;
            }
            Element runtimeElement = (Element) node;
            if (!"run_time".equalsIgnoreCase(runtimeElement.getNodeName())) {
                return false;
            }
            node = runtimeElement.getFirstChild();
            while (node != null && node.getNodeType() != Node.ELEMENT_NODE) {
                node = node.getNextSibling();
            }
            Element dateElement = (Element) node;
            if (!"date".equalsIgnoreCase(dateElement.getNodeName())) {
                return false;
            }
            String date = dateElement.getAttribute("date");
            node = dateElement.getFirstChild();
            while (node != null && node.getNodeType() != Node.ELEMENT_NODE) {
                node = node.getNextSibling();
            }
            if (node == null) {
                return false;
            }
            Element periodElement = (Element) node;
            String time = periodElement.getAttribute("single_start");
            if (date == null || time == null) {
                return false;
            }
            Date scheduledRuntime = SOSDate.getTime(date + " " + time);
            Date now = SOSDate.getTime();
            return now.after(scheduledRuntime);
        } catch (Exception e) {}
        return false;
    }

}