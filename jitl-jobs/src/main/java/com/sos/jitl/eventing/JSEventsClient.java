package com.sos.jitl.eventing;

import static com.sos.scheduler.messages.JSMessages.JSJ_F_107;
import static com.sos.scheduler.messages.JSMessages.JSJ_I_110;
import static com.sos.scheduler.messages.JSMessages.JSJ_I_111;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.sos.JSHelper.Basics.JSJobUtilitiesClass;
import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.JSHelper.Options.SOSOptionTime;

import sos.scheduler.command.SOSSchedulerCommand;
import sos.xml.SOSXMLTransformer;
import sos.xml.SOSXMLXPath;

public class JSEventsClient extends JSJobUtilitiesClass<JSEventsClientOptions> {

    private final String conClassName = this.getClass().getSimpleName();
    private final Logger logger = Logger.getLogger(this.getClass());
    private final String conSVNVersion = "$Id: JSEventsClient.java 18220 2012-10-18 07:46:10Z kb $";

    private final HashMap<String, String> eventParameters = new HashMap<String, String>();

    public JSEventsClient() {
        super(new JSEventsClientOptions());
        this.getOptions();
    }

    @Override
    public JSEventsClientOptions getOptions() {

        if (objOptions == null) {
            objOptions = new JSEventsClientOptions();
        }
        return objOptions;
    }

    public JSEventsClient execute() throws Exception {
        final String conMethodName = conClassName + "::Execute";

        logger.debug(JSJ_I_110.get(conMethodName));
        logger.debug(conSVNVersion);

        try {
            getOptions().checkMandatory();
            logger.debug(getOptions().dirtyString());

            try {
                if (objOptions.EventParameter.isDirty() == true) {
                    eventParameters.put(objOptions.EventParameter.getShortKey(), objOptions.EventParameter.getValue());
                    String[] strEP = objOptions.EventParameter.getValue().split(";");
                    HashMap<String, String> objH = objOptions.settings();
                    for (String strParamName : strEP) {
                        strParamName = strParamName.trim();
                        String strValue = objH.get(strParamName);
                        if (strValue != null) {
                            eventParameters.put(strParamName, strValue.trim());
                        }
                    }
                }
                String action = objOptions.scheduler_event_action.getValue();
                String strEventIDs = objOptions.id.getValue();
                if (strEventIDs.length() > 0) {
                    String strA[] = objOptions.id.getValue().split(";");
                    for (String strEventID : strA) {
                        strEventID = strEventID.trim();
                        String addOrder = createAddOrder(action, strEventID, eventParameters);
                        submitToSupervisor(addOrder);
                    }
                }
                // Check for del_events
                if (objOptions.del_events.isDirty()) {
                    String strA[] = objOptions.del_events.getValue().split(";");
                    action = "remove";
                    for (String strEventID : strA) {
                        String addOrder = createAddOrder(action, strEventID.trim(), eventParameters);
                        submitToSupervisor(addOrder);
                    }
                }
            } catch (Exception e) {
                throw new JobSchedulerException("Error submitting event order: " + e, e);
            }
        } catch (Exception e) {
            String strM = JSJ_F_107.get(conMethodName);
            throw new JobSchedulerException(strM, e);
        } finally {
            logger.debug(JSJ_I_111.get(conMethodName));
        }

        return this;
    }

    private String createAddOrder(final String action, final String eventId, final Map<String, String> eventParameters1) {
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document addOrderDocument = docBuilder.newDocument();
            Element addOrderElement = addOrderDocument.createElement("add_order");
            addOrderDocument.appendChild(addOrderElement);
            addOrderElement.setAttribute("job_chain", objOptions.supervisor_job_chain.getValue());
            Element paramsElement = addOrderDocument.createElement("params");
            addOrderElement.appendChild(paramsElement);
            addParam(paramsElement, "action", action);
            addParam(paramsElement, "event_id", eventId);
            addParam(paramsElement, "remote_scheduler_host", objOptions.scheduler_event_handler_host.getValue());
            addParam(paramsElement, "remote_scheduler_port", objOptions.scheduler_event_handler_port.getValue());
            addParam(paramsElement, "job_chain", objOptions.supervisor_job_chain.getValue());
            String orderId = "";
            String jobName = "";
            addParam(paramsElement, "order_id", orderId);
            addParam(paramsElement, "job_name", jobName);
            addParam(paramsElement, "event_class", objOptions.EventClass.getValue());
            addParam(paramsElement, "exit_code", objOptions.scheduler_event_exit_code.getValue());
            addParam(paramsElement, "created", SOSOptionTime.getCurrentTimeAsString());
            addParam(paramsElement, "expires", objOptions.scheduler_event_expires.getValue());

            for (Map.Entry<String, String> entry : eventParameters1.entrySet()) {
                addParam(paramsElement, entry.getKey(), entry.getValue());
            }
            return SOSXMLTransformer.docToString(addOrderDocument, "iso-8859-1");
        } catch (Exception e) {
            throw new JobSchedulerException("Error creating add_order xml: " + e, e);
        }
    }

    private void addParam(final Element paramsElement, final String name, final String value) {
        if (value != null && value.length() > 0) {
            Element paramElement = paramsElement.getOwnerDocument().createElement("param");
            paramElement.setAttribute("name", name);
            paramElement.setAttribute("value", value);
            paramsElement.appendChild(paramElement);
        }
    }

    private void submitToSupervisor(String xml) throws Exception {
        try {
            if (xml.indexOf("<?xml") == -1) {
                // TODO encoding as a parameter
                xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + xml;
            }
            logger.debug("Sending add_order to the EventService:\n" + xml);
            String answer = null;
            if (objOptions.scheduler_event_handler_host.isDirty() && objOptions.scheduler_event_handler_port.value() > 0) {
                SOSSchedulerCommand schedulerCommand = new SOSSchedulerCommand();
                schedulerCommand.setHost(objOptions.scheduler_event_handler_host.getValue());
                schedulerCommand.setPort(objOptions.scheduler_event_handler_port.value());
                // TODO protocol as Parameter
                schedulerCommand.setProtocol("tcp");
                logger.debug(".. connecting to EventService " + schedulerCommand.getHost() + ":" + schedulerCommand.getPort());
                schedulerCommand.connect();
                schedulerCommand.sendRequest(xml);
                answer = schedulerCommand.getResponse();
                logger.debug(answer.replaceAll(Pattern.quote("\n"), ""));
            } else {
                logger.info("No supervisor configured, submitting event to this JobScheduler.");
                answer = objJSCommands.executeXML(xml);
            }

            if (answer != null && answer.length() > 0) {
                SOSXMLXPath xAnswer = new SOSXMLXPath(new StringBuffer(answer));
                String errorText = xAnswer.selectSingleNodeValue("//ERROR/@text");
                if (errorText != null && errorText.length() > 0) {
                    throw new JobSchedulerException("EventService returned an error: " + errorText);
                }
            }
        } catch (Exception e) {
            throw new JobSchedulerException("Failed to submit event: " + e, e);
        }
    }

 
} 
