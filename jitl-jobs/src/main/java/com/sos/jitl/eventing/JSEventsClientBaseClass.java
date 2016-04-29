package com.sos.jitl.eventing;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import sos.connection.SOSConnection;
import sos.scheduler.command.SOSSchedulerCommand;
import sos.scheduler.job.JobSchedulerConstants;
import sos.scheduler.job.JobSchedulerJobAdapter;
import sos.spooler.Supervisor_client;

import com.sos.JSHelper.Exceptions.JobSchedulerException;

public class JSEventsClientBaseClass extends JobSchedulerJobAdapter {

    protected static final String conNodeNameEVENT = "event";
    protected static String tableEvents = "SCHEDULER_EVENTS";
    protected final boolean continue_with_spooler_process = true;
    protected final boolean continue_with_task = true;
    protected JSEventsClientOptions objO = null;
    protected JSEventsClient objR = null;
    private static final String NODE_NAME_EVENTS = "events";
    private static final Logger LOGGER = Logger.getLogger(JSEventsClientBaseClass.class);

    public void setOptions(final JSEventsClientOptions pobjOptions) {
        objO = pobjOptions;
    }

    protected void doProcessing() throws Exception {
        Initialize();
        objO.CheckMandatory();
        objR.Execute();
    }

    protected void Initialize() {
        initializeLog4jAppenderClass();
        objR = new JSEventsClient();
        objO = objR.getOptions();
        if (!objO.scheduler_event_handler_host.isDirty()) {
            if (spooler != null) {
                Supervisor_client objRemoteConfigurationService = null;
                try {
                    objRemoteConfigurationService = spooler.supervisor_client();
                    objO.scheduler_event_handler_host.Value(objRemoteConfigurationService.hostname());
                    objO.scheduler_event_handler_port.value(objRemoteConfigurationService.tcp_port());
                } catch (Exception e) {
                    objO.scheduler_event_handler_host.Value(spooler.hostname());
                    objO.scheduler_event_handler_port.value(spooler.tcp_port());
                }
            } else {
                throw new JobSchedulerException("No Event Service specified. Parameter " + objO.scheduler_event_handler_host.getShortKey());
            }
        }
        objR.setJSJobUtilites(this);
        objR.setJSCommands(this);
        try {
            objO.CurrentNodeName(this.getCurrentNodeName());
            objO.setAllOptions(getSchedulerParameterAsProperties());
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
            conn.executeUpdate("DELETE FROM " + getEventsTableName()
                    + " WHERE \"EXPIRES\"<=%now AND (\"SPOOLER_ID\" IS NULL OR \"SPOOLER_ID\"='' OR \"SPOOLER_ID\"='" + spooler.id() + "')");
            conn.commit();

            Vector<?> vEvents =
                    conn.getArrayAsVector("SELECT \"SPOOLER_ID\", \"REMOTE_SCHEDULER_HOST\", \"REMOTE_SCHEDULER_PORT\", \"JOB_CHAIN\", "
                            + "\"ORDER_ID\", \"JOB_NAME\", \"EVENT_CLASS\", \"EVENT_ID\", \"EXIT_CODE\", \"CREATED\", \"EXPIRES\", \"PARAMETERS\" FROM "
                            + getEventsTableName() + " WHERE (\"SPOOLER_ID\" IS NULL OR \"SPOOLER_ID\"='' OR \"SPOOLER_ID\"='" + spooler.id()
                            + "') ORDER BY \"ID\" ASC");
            String[] strAttr =
                    new String[] { "remote_scheduler_host", "remote_scheduler_port", "job_chain", "order_id", "job_name", "event_class", "event_id",
                            "exit_code", "expires", "created", };
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

    public Document getEventsFromSchedulerVar() throws Exception {
        Document eventDocument = null;
        SOSConnection objConn = null;
        try {
            String eventSet = "";
            if (spooler == null) {
                eventSet = sendCommand("<param.get name=\"" + JobSchedulerConstants.eventVariableName + "\"/>");
                if ("".equals(eventSet)) {
                    String strM =
                            String.format("No Answer from Scheduler %1$s:%2$s", objO.scheduler_event_handler_host.Value(),
                                    objO.scheduler_event_handler_port.Value());
                    LOGGER.error(strM);
                }
                Document doc = createEventsDocument(new InputSource(new StringReader(eventSet)));
                NodeList params = doc.getElementsByTagName("param");
                if (params.item(0) == null) {
                    LOGGER.error("No events param found in JobScheduler answer");
                } else {
                    NamedNodeMap attr = params.item(0).getAttributes();
                    eventSet = getText(attr.getNamedItem("value"));
                    eventSet = modifyXMLTags(eventSet);
                }
            } else {
                eventSet = spooler.var(JobSchedulerConstants.eventVariableName);
                objConn = getConnection();
            }
            if (objConn != null && (eventSet == null || eventSet.isEmpty())) {
                eventDocument = readEventsFromDB(objConn);
            } else {
                if (eventSet.isEmpty()) {
                    return eventDocument;
                }
                LOGGER.debug("current event set: " + eventSet);
                eventDocument = createEventsDocument(new InputSource(new StringReader(eventSet)));
            }
        } catch (Exception e) {
            throw new JobSchedulerException(e);
        }
        return eventDocument;
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
                socket.connect(objO.scheduler_event_handler_host.Value(), objO.scheduler_event_handler_port.value());
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
