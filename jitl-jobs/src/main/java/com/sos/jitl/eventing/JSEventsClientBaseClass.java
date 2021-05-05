package com.sos.jitl.eventing;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import sos.connection.SOSConnection;
import sos.scheduler.command.SOSSchedulerCommand;
import sos.scheduler.job.JobSchedulerJobAdapter;
import sos.spooler.Order;
import sos.spooler.Supervisor_client;

import com.sos.JSHelper.Exceptions.JobSchedulerException;

public class JSEventsClientBaseClass extends JobSchedulerJobAdapter {

    protected static final String conNodeNameEVENT = "event";
    protected static String tableEvents = "REPORTING_CUSTOM_EVENTS";
    protected final boolean continue_with_spooler_process = true;
    protected final boolean continue_with_task = true;
    protected JSEventsClientOptions jsEventsClientOptions = null;
    protected JSEventsClient jsEventsClient = null;
    private static final String NODE_NAME_EVENTS = "events";
    private static final Logger LOGGER = LoggerFactory.getLogger(JSEventsClientBaseClass.class);

    public void setOptions(final JSEventsClientOptions pobjOptions) {
        jsEventsClientOptions = pobjOptions;
    }

    protected void doProcessing() throws Exception {
        initialize();
        jsEventsClientOptions.checkMandatory();
        jsEventsClient.execute();
    }

    protected void initialize() {
        jsEventsClient = new JSEventsClient();
        jsEventsClientOptions = jsEventsClient.getOptions();

        if (jsEventsClientOptions.ExitCode.isNotDirty()) {
            jsEventsClientOptions.ExitCode.value(spooler_task.exit_code());
        }

        if (!jsEventsClientOptions.scheduler_event_handler_host.isDirty()) {
            if (spooler != null) {
                Supervisor_client objRemoteConfigurationService = null;
                try {
                    objRemoteConfigurationService = spooler.supervisor_client();
                    jsEventsClientOptions.scheduler_event_handler_host.setValue(objRemoteConfigurationService.hostname());
                    jsEventsClientOptions.scheduler_event_handler_port.value(objRemoteConfigurationService.tcp_port());
                } catch (Exception e) {
                    jsEventsClientOptions.scheduler_event_handler_host.setValue(spooler.hostname());
                    jsEventsClientOptions.scheduler_event_handler_port.value(SOSSchedulerCommand.getHTTPPortFromScheduler(spooler));
                }
            } else {
                throw new JobSchedulerException("No Event Service specified. Parameter " + jsEventsClientOptions.scheduler_event_handler_host.getShortKey());
            }
        }
        jsEventsClient.setJSJobUtilites(this);
        jsEventsClient.setJSCommands(this);
        try {
            Order order = spooler_task.order();
            jsEventsClientOptions.setCurrentNodeName(this.getCurrentNodeName(order,false));
            jsEventsClientOptions.setAllOptions(getSchedulerParameterAsProperties(order));
        } catch (Exception e) {
            throw new JobSchedulerException("error " + e.getMessage(), e);
        }
    }

    @Override
    public boolean spooler_init() {
        return super.spooler_init();
    }

    @Override
    public void spooler_exit() {
        super.spooler_exit();
    }

    private class EventNode {

        public Element event = null;
        public HashMap<?, ?> record = null;

        EventNode(final Element pevent, final HashMap<?, ?> hshmap) {
            event = pevent;
            record = hshmap;
        }

        public void setAttr(final String pstrName) {
            event.setAttribute(pstrName, getVal(pstrName));
        }

        public String getParameters() {
            return record.get("parameters").toString();
        }

        public boolean hasParameters() {
            return getParameters().length() > 0;
        }

        public String getVal(final String pstrName) {
            String strR = record.get(pstrName).toString();
            return strR;
        }
    }

    public Document readEventsFromDB(final SOSConnection conn) throws Exception {
        Document eventsDoc = null;
        try {
            conn.executeUpdate("DELETE FROM " + getEventsTableName() + " WHERE \"EXPIRES\"<=%now AND (\"SPOOLER_ID\" IS NULL OR \"SPOOLER_ID\"='' OR \"SPOOLER_ID\"='" + spooler
                    .id() + "')");
            conn.commit();

            Vector<?> vEvents = conn.getArrayAsVector("SELECT \"SPOOLER_ID\", \"REMOTE_SCHEDULER_HOST\", \"REMOTE_SCHEDULER_PORT\", \"JOB_CHAIN\", "
                    + "\"ORDER_ID\", \"JOB_NAME\", \"EVENT_CLASS\", \"EVENT_ID\", \"EXIT_CODE\", \"CREATED\", \"EXPIRES\", \"PARAMETERS\" FROM " + getEventsTableName()
                    + " WHERE (\"SPOOLER_ID\" IS NULL OR \"SPOOLER_ID\"='' OR \"SPOOLER_ID\"='" + spooler.id() + "') ORDER BY \"ID\" ASC");
            String[] strAttr = new String[] { "remote_scheduler_host", "remote_scheduler_port", "job_chain", "order_id", "job_name", "event_class", "event_id", "exit_code",
                    "expires", "created", };
            Iterator<?> vIterator = vEvents.iterator();
            int vCount = 0;
            eventsDoc = createEventsDocument(NODE_NAME_EVENTS);
            while (vIterator.hasNext()) {
                HashMap<?, ?> record = (HashMap<?, ?>) vIterator.next();
                Element event = eventsDoc.createElement(conNodeNameEVENT);
                EventNode objEvent = new EventNode(event, record);
                event.setAttribute("scheduler_id", objEvent.getVal("spooler_id"));
                for (String strAttributeName : strAttr) {
                    objEvent.setAttr(strAttributeName);
                }
                if (objEvent.hasParameters()) {
                    Document eventParameters = createEventsDocument(new InputSource(new StringReader(objEvent.getParameters())));
                    LOGGER.debug("Importing params node...");
                    Node impParameters = eventsDoc.importNode(eventParameters.getDocumentElement(), true);
                    LOGGER.debug("appending params child...");
                    event.appendChild(impParameters);
                }
                eventsDoc.getLastChild().appendChild(event);
                vCount++;
            }
            LOGGER.info(vCount + " events read from database");
        } catch (Exception e) {
            throw new JobSchedulerException("Failed to read events from database: " + e, e);
        }
        return eventsDoc;
    }

    public String getEventsTableName() {
        return tableEvents;
    }

    protected Document createDomDocument() {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder;
        Document eventDocument = null;
        try {
            docBuilder = docFactory.newDocumentBuilder();
            eventDocument = docBuilder.newDocument();
        } catch (ParserConfigurationException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return eventDocument;
    }

    protected Document createEventsDocument(final String pstrNodeName) {
        Document eventDocument = createDomDocument();
        eventDocument.appendChild(eventDocument.createElement(NODE_NAME_EVENTS));
        return eventDocument;
    }

    protected Document createEventsDocument(final InputSource pobjInputSource) {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder;
        Document eventDocument = null;
        try {
            docBuilder = docFactory.newDocumentBuilder();
            eventDocument = docBuilder.parse(pobjInputSource);
        } catch (ParserConfigurationException e) {
            LOGGER.error(e.getMessage(), e);
        } catch (SAXException e) {
            LOGGER.error(e.getMessage(), e);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return eventDocument;
    }

    private String getText(final Node n) {
        if (n != null) {
            return n.getNodeValue();
        } else {
            return "";
        }
    }

   
    private String modifyXMLTags(final String pstrEventString) {
        return pstrEventString.replaceAll(String.valueOf((char) 254), "<").replaceAll(String.valueOf((char) 255), ">");
    }

    private String sendCommand(final String command) {
        String s = "";
        SOSSchedulerCommand socket = null;
        LOGGER.debug("...sendCommand: " + command);
        try {
            if (socket == null) {
                socket = new SOSSchedulerCommand();
                socket.connect(jsEventsClientOptions.scheduler_event_handler_host.getValue(), jsEventsClientOptions.scheduler_event_handler_port.value());
            }
            socket.sendRequest(command);
            s = socket.getResponse();
            LOGGER.debug("Response = " + modifyXMLTags(s));
        } catch (Exception ee) {
            throw new JobSchedulerException(String.format("Error sending command to Job Scheduler: '%1$s' \n %2$s", command, ee.getMessage()), ee);
        }
        return s;
    }

}