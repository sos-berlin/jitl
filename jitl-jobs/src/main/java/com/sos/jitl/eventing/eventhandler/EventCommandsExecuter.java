package com.sos.jitl.eventing.eventhandler;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.xpath.XPathAPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.sos.jitl.eventing.db.SchedulerEventDBItem;

import sos.scheduler.command.SOSSchedulerCommand;
import sos.util.ParameterSubstitutor;
import sos.util.SOSDate;
import sos.xml.SOSXMLTransformer;
import sos.xml.SOSXMLXPath;

public class EventCommandsExecuter {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventCommandsExecuter.class);
    private Collection<Object> eventHandlerResultedCommands = new Vector<Object>();
    private List<SchedulerEventDBItem> listOfEvents;

    private String hostName;
    private Integer httpPort;
    private Integer socketTimeout;
    private ParameterSubstitutor parameterSubstitutor = new ParameterSubstitutor();
    private List<SchedulerEventDBItem> listOfAddEvents;
    private List<SchedulerEventDBItem> listOfRemoveEvents;

    public EventCommandsExecuter(Collection<Object> eventHandlerResultedCommands, List<SchedulerEventDBItem> listOfEvents, String hostName,
            Integer httpPort, Integer socketTimeout) {
        super();
        this.eventHandlerResultedCommands = eventHandlerResultedCommands;
        this.listOfEvents = listOfEvents;
        this.hostName = hostName;
        this.httpPort = httpPort;
        this.socketTimeout = socketTimeout;
        listOfAddEvents = new ArrayList<SchedulerEventDBItem>();
        listOfRemoveEvents = new ArrayList<SchedulerEventDBItem>();
    }

    public void executeCommands() throws SAXException, IOException, Exception {
        parameterSubstitutor = new ParameterSubstitutor();
        parameterSubstitutor.addKey("current_date", SOSDate.getCurrentTimeAsString());
        getParametersFromEvents();

        try {
            Iterator<Object> eventHandlerResultedCommandsIterator = eventHandlerResultedCommands.iterator();

            while (eventHandlerResultedCommandsIterator.hasNext()) {
                NodeList commands = null;
                Object result = eventHandlerResultedCommandsIterator.next();
                if (result instanceof File) {
                    File resultFile = (File) result;
                    DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
                    Document eventDocument = docBuilder.parse(new InputSource(new StringReader(this.getFileContent(resultFile).toString())));

                    commands = XPathAPI.selectNodeList(eventDocument, "//command");
                } else {
                    commands = XPathAPI.selectNodeList((Node) result, "command");
                }
                for (int i = 0; i < commands.getLength(); i++) {
                    Node command = commands.item(i);
                    NamedNodeMap commandAttributes = command.getAttributes();
                    String commandHost = this.hostName;
                    String commandPort = String.valueOf(this.httpPort);
                    String commandProtocol = "http";
                    for (int j = 0; j < commandAttributes.getLength(); j++) {
                        if ("scheduler_host".equals(commandAttributes.item(j).getNodeName()) && !commandAttributes.item(j).getNodeValue().isEmpty()) {
                            LOGGER.debug("using host from command: " + commandAttributes.item(j).getNodeValue());
                            commandHost = commandAttributes.item(j).getNodeValue();
                        }
                        if ("scheduler_port".equals(commandAttributes.item(j).getNodeName()) && !commandAttributes.item(j).getNodeValue().isEmpty()) {
                            commandPort = commandAttributes.item(j).getNodeValue();
                        }
                        if ("protocol".equals(commandAttributes.item(j).getNodeName()) && !commandAttributes.item(j).getNodeValue().isEmpty()) {
                            commandProtocol = commandAttributes.item(j).getNodeValue();
                        }
                    }
                    SOSSchedulerCommand schedulerCommand = new SOSSchedulerCommand();
                    schedulerCommand.setTimeout(this.socketTimeout);
                    if (!commandHost.isEmpty()) {
                        schedulerCommand.setHost(commandHost);
                        if (!commandPort.isEmpty()) {
                            schedulerCommand.setPort(Integer.parseInt(commandPort));
                        } else {
                            throw new Exception("empty port has been specified by event handler response for commands");
                        }
                    } else {
                        throw new Exception("empty JobScheduler ID or host and port have been specified by event handler response for commands");
                    }
                    if (!commandProtocol.isEmpty()) {
                        schedulerCommand.setProtocol(commandProtocol);
                    }
                    try {
                        LOGGER.debug(".. connecting to JobScheduler " + schedulerCommand.getHost() + ":" + schedulerCommand.getPort());
                        schedulerCommand.connect();
                        NodeList commandElements = command.getChildNodes();
                        for (int k = 0; k < commandElements.getLength(); k++) {
                            if (commandElements.item(k).getNodeType() != Node.ELEMENT_NODE) {
                                continue;
                            }
                            String commandRequest = this.xmlNodeToString(commandElements.item(k));
                            commandRequest = parameterSubstitutor.replace(commandRequest);
                            commandRequest = parameterSubstitutor.replaceEnvVars(commandRequest);
                            commandRequest = parameterSubstitutor.replaceSystemProperties(commandRequest);
                            LOGGER.info(".. sending command to remote JobScheduler [" + commandHost + ":" + commandPort + "]: " + commandRequest);
                            schedulerCommand.sendRequest(commandRequest);
                            SOSXMLXPath answer = new SOSXMLXPath(new StringBuffer(schedulerCommand.getResponse()));
                            String errorText = answer.selectSingleNodeValue("//ERROR/@text");
                            if (errorText != null && !errorText.isEmpty()) {
                                throw new Exception("could not send command to remote JobScheduler [" + commandHost + ":" + commandPort + "]: "
                                        + errorText);
                            }
                        }
                    } catch (Exception e) {
                        throw new Exception("Error contacting remote JobScheduler: " + e, e);
                    } finally {
                        try {
                            schedulerCommand.disconnect();
                        } catch (Exception ex) {
                            //
                        }
                    }

                }
            }
            try {
                eventHandlerResultedCommandsIterator = eventHandlerResultedCommands.iterator();

                while (eventHandlerResultedCommandsIterator.hasNext()) {
                    Object result = eventHandlerResultedCommandsIterator.next();

                    NodeList commands = null;
                    if (result instanceof File) {
                        File resultFile = (File) result;
                        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
                        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
                        Document eventDocument = docBuilder.parse(new InputSource(new StringReader(this.getFileContent(resultFile).toString())));
                        commands = XPathAPI.selectNodeList(eventDocument, "//remove_event");
                    } else {
                        commands = XPathAPI.selectNodeList((Node) result, "remove_event/event");
                    }
                    if (commands.getLength() > 0) {
                        LOGGER.debug("-->" + commands.getLength() + " removements found in event handler");

                        for (int i = 0; i < commands.getLength(); i++) {
                            if (commands.item(i) == null) {
                                continue;
                            }
                            if (commands.item(i).getNodeType() == Node.ELEMENT_NODE) {
                                NamedNodeMap attributes = commands.item(i).getAttributes();
                                if (attributes == null) {
                                    continue;
                                }
                                SchedulerEventDBItem event = getItemFromAttributes(attributes);
                                this.listOfRemoveEvents.add(event);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                throw new Exception("could not remove event caused by event handler: " + e.getMessage());
            }
            try {
                eventHandlerResultedCommandsIterator = eventHandlerResultedCommands.iterator();

                while (eventHandlerResultedCommandsIterator.hasNext()) {
                    Object result = eventHandlerResultedCommandsIterator.next();
                    NodeList commands = null;
                    if (result instanceof File) {
                        File resultFile = (File) result;
                        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
                        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
                        Document eventDocument = docBuilder.parse(new InputSource(new StringReader(this.getFileContent(resultFile).toString())));
                        commands = XPathAPI.selectNodeList(eventDocument, "//add_event/event");
                    } else {
                        commands = XPathAPI.selectNodeList((Node) result, "add_event/event");
                    }
                    if (commands.getLength() > 0) {
                        LOGGER.debug("-->" + commands.getLength() + " add events found in event handler");

                        for (int i = 0; i < commands.getLength(); i++) {
                            if (commands.item(i) == null) {
                                continue;
                            }
                            if (commands.item(i).getNodeType() == Node.ELEMENT_NODE) {
                                NamedNodeMap attributes = commands.item(i).getAttributes();
                                if (attributes == null) {
                                    continue;
                                }
                                SchedulerEventDBItem event = getItemFromAttributes(attributes);
                                this.listOfAddEvents.add(event);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                throw new Exception("could not add event created by event handler: " + e.getMessage());
            }
        } catch (Exception e) {
            throw new Exception("events processed with errors: " + e.getMessage());
        } finally {
            try {
                Iterator<Object> eventHandlerResultedCommandsIterator = eventHandlerResultedCommands.iterator();

                while (eventHandlerResultedCommandsIterator.hasNext()) {
                    Object result = eventHandlerResultedCommandsIterator.next();
                    if (result instanceof File) {
                        File resultFile = (File) result;
                        if (resultFile != null && !resultFile.delete()) {
                            resultFile.deleteOnExit();
                        }
                    }
                }
            } catch (Exception e) {
                LOGGER.warn("could not delete temporary file: " + e.getMessage());
            }
        }

    }

    private SchedulerEventDBItem getItemFromAttributes(NamedNodeMap attributes) {
        SchedulerEventDBItem event = new SchedulerEventDBItem();
        for (int j = 0; j < attributes.getLength(); j++) {
            if ("event_name".equalsIgnoreCase(attributes.item(j).getNodeName())) {
                continue;
            }
            if ("created".equalsIgnoreCase(attributes.item(j).getNodeName())) {
                event.setCreated(new Date());
            }
            if ("event_class".equalsIgnoreCase(attributes.item(j).getNodeName())) {
                event.setEventClass(attributes.item(j).getNodeValue());
            }
            if ("event_id".equalsIgnoreCase(attributes.item(j).getNodeName())) {
                event.setEventId(attributes.item(j).getNodeValue());
            }
            if ("exit_code".equalsIgnoreCase(attributes.item(j).getNodeName())) {
                event.setExitCode(attributes.item(j).getNodeValue());
            }
            if ("job_chain".equalsIgnoreCase(attributes.item(j).getNodeName())) {
                event.setJobChain(attributes.item(j).getNodeValue());
            }
            if ("job_name".equalsIgnoreCase(attributes.item(j).getNodeName())) {
                event.setJobName(attributes.item(j).getNodeValue());
            }
            if ("order_id".equalsIgnoreCase(attributes.item(j).getNodeName())) {
                event.setOrderId(attributes.item(j).getNodeValue());
            }
            if ("remote_scheduler_host".equalsIgnoreCase(attributes.item(j).getNodeName())) {
                event.setRemoteSchedulerHost(attributes.item(j).getNodeValue());
            }
            if ("remote_scheduler_port".equalsIgnoreCase(attributes.item(j).getNodeName())) {
                event.setRemoteSchedulerPort(attributes.item(j).getNodeValue());
            }
            if ("scheduler_id".equalsIgnoreCase(attributes.item(j).getNodeName())) {
                event.setSchedulerId(attributes.item(j).getNodeValue());
            }
        }
        return event;

    }

    private void getParametersFromEvents() throws Exception {
        LOGGER.debug("executing getParametersFromEvents....");
        LOGGER.debug("events length: " + listOfEvents.size());
        for (SchedulerEventDBItem event : listOfEvents) {

            LOGGER.debug("event_class:" + event.getEventClass());
            LOGGER.debug("event_id:" + event.getEventId());
            if (event.getParameters() != null && !event.getParameters().isEmpty()) {
                Element nodeParameters = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(event
                        .getParameters().getBytes())).getDocumentElement();
                NodeList parameters = XPathAPI.selectNodeList(nodeParameters, "params/param");
                LOGGER.debug("parameter length: " + parameters.getLength());
                if (parameters != null && parameters.getLength() > 0) {
                    for (int ii = 0; ii < parameters.getLength(); ii++) {
                        Node eventParam = parameters.item(ii);
                        NamedNodeMap paramAttr = eventParam.getAttributes();
                        String paramName = getText(paramAttr.getNamedItem("name"));
                        String paramValue = getText(paramAttr.getNamedItem("value"));
                        parameterSubstitutor.addKey(event.getEventClass() + "." + event.getEventId() + "." + paramName, paramValue);
                        parameterSubstitutor.addKey(event.getEventClass() + ".*." + paramName, paramValue);
                        parameterSubstitutor.addKey(event.getEventId() + "." + paramName, paramValue);
                        parameterSubstitutor.addKey(paramName, paramValue);
                        LOGGER.debug(event.getEventClass() + "." + event.getEventId() + "." + paramName + "=" + paramValue);
                    }
                }
            }
        }
    }

    private String getText(final Node n) {
        if (n != null) {
            return n.getNodeValue();
        } else {
            return "";
        }
    }

    private String xmlNodeToString(final Node node) throws Exception {
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document document = docBuilder.newDocument();
            document.appendChild(document.importNode(node, true));
            return SOSXMLTransformer.docToString(document);
        } catch (Exception e) {
            throw new Exception("error occurred transforming node: " + e.getMessage());
        }
    }

    private StringBuffer getFileContent(final File file) throws Exception {
        BufferedInputStream in = null;
        StringBuffer content = new StringBuffer();
        if (file == null) {
            throw new Exception("no valid file object found");
        }
        if (!file.canRead()) {
            throw new Exception("file not accessible: " + file.getCanonicalPath());
        }
        try {
            FileInputStream fis = new FileInputStream(file);
            in = new BufferedInputStream(fis);
            byte buffer[] = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                content.append(new String(buffer, 0, bytesRead));
            }
            fis.close();
            fis = null;
            in.close();
            in = null;
            return content;
        } catch (Exception e) {
            throw new Exception("error occurred reading content of file [" + file.getCanonicalPath() + "]: " + e.getMessage(), e);
        } finally {
            if (in != null) {
                in.close();
                in = null;
            }
        }
    }

    public List<SchedulerEventDBItem> getListOfAddEvents() {
        return listOfAddEvents;
    }

    public List<SchedulerEventDBItem> getListOfRemoveEvents() {
        return listOfRemoveEvents;
    }
}
