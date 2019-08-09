package com.sos.jitl.eventing.eventhandler;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.log4j.Logger;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class SOSEvaluateEvents {

    private static final Logger LOGGER = Logger.getLogger(SOSEvaluateEvents.class);
    private LinkedHashSet<SOSActions> listOfActions;
    private List<SchedulerEvent> listOfActiveEvents;
    private String errmsg;
 
    public SOSEvaluateEvents() {
        super();
        listOfActiveEvents = new ArrayList<SchedulerEvent>();
    }

    public void reconnect() {    
        listOfActiveEvents = new ArrayList<SchedulerEvent>();
    }

    public String getEventStatus(final SchedulerEvent event) {
        String erg = "missing";
        Iterator<SchedulerEvent> i = listOfActiveEvents.iterator();
        while (i.hasNext()) {
            SchedulerEvent e = i.next();
            if (event.isEqual(e)) {
                event.setCreated(e.getCreated());
                event.setExpires(e.getExpires());
                erg = "active";
            }
        }
        return erg;
    }

    private void fillTreeItem(final SOSActions a, final Node n) {
        a.commandNodes = n.getChildNodes();
        for (int i = 0; i < a.commandNodes.getLength(); i++) {
            Node command = a.commandNodes.item(i);
            if ("command".equals(command.getNodeName()) || "remove_event".equals(command.getNodeName()) || "add_event".equals(command.getNodeName())) {
                SOSEventCommand ec = new SOSEventCommand();
                ec.setCommand(command);
                a.listOfCommands.add(ec);
            }
        }
    }

    private void fillEvents(final Node eventGroup, final SOSEventGroups evg) {
        NodeList events = eventGroup.getChildNodes();
        for (int i = 0; i < events.getLength(); i++) {
            Node event = events.item(i);
            if ("event".equals(event.getNodeName())) {
                NamedNodeMap attr = event.getAttributes();
                SchedulerEvent e = new SchedulerEvent();
                e.setProperties(attr);
                e.setEventClassIfBlank(evg.event_class);
                evg.listOfEvents.add(e);
            }
        }
    }

    private void fillEventGroups(final SOSActions a, final Node n) {
        NamedNodeMap attrEvents = n.getAttributes();
        a.condition = getText(attrEvents.getNamedItem("logic"));
        NodeList eventGroups = n.getChildNodes();
        for (int i = 0; i < eventGroups.getLength(); i++) {
            Node eventGroup = eventGroups.item(i);
            if ("event_group".equals(eventGroup.getNodeName())) {
                NamedNodeMap attr = eventGroup.getAttributes();
                SOSEventGroups evg = new SOSEventGroups(getText(attr.getNamedItem("group")));
                evg.condition = getText(attr.getNamedItem("logic"));
                evg.group = getText(attr.getNamedItem("group"));
                evg.event_class = getText(attr.getNamedItem("event_class"));
                fillEvents(eventGroup, evg);
                a.listOfEventGroups.add(evg);
            }
        }
    }

    private void fillAction(final SOSActions a, final NodeList actionChilds) {
        for (int i = 0; i < actionChilds.getLength(); i++) {
            Node n = actionChilds.item(i);
            if ("commands".equals(n.getNodeName())) {
                a.commands = n;
                fillTreeItem(a, n);
            }
            if ("events".equals(n.getNodeName())) {
                fillEventGroups(a, n);
            }
        }
    }

    public void readConfigurationFile(final File f) throws DOMException, Exception {
        listOfActions = new LinkedHashSet<SOSActions>();
        if (f.exists()) {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder;
            try {
                listOfActions.clear();
                docBuilder = docFactory.newDocumentBuilder();
                Document doc = docBuilder.parse(f);
                NodeList actions = doc.getElementsByTagName("action");
                if (actions.item(0) == null) {
                    errmsg = "No actions defined in " + f.getCanonicalPath();
                    LOGGER.info(errmsg);
                } else {
                    for (int i = 0; i < actions.getLength(); i++) {
                        NamedNodeMap attr = actions.item(i).getAttributes();
                        String action_name = getText(attr.getNamedItem("name"));
                        SOSActions a = new SOSActions(action_name);
                        listOfActions.add(a);
                        fillAction(a, actions.item(i).getChildNodes());
                    }
                }
            } catch (ParserConfigurationException e) {
                errmsg = "Error reading actions from " + f.getAbsolutePath();
                LOGGER.info(errmsg);
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

    public LinkedHashSet<SOSActions> getListOfActions() {
        return listOfActions;
    }

    public List<SchedulerEvent> getListOfActiveEvents() {
        return listOfActiveEvents;
    }

    public static void main(final String[] args) {
        SOSEvaluateEvents eval = new SOSEvaluateEvents();
        String configuration_filename = "c:/roche/scheduler/config/events/splitt_gsg.actions.xml";
        File f = new File(configuration_filename);
        try {
            eval.readConfigurationFile(f);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        Iterator<SOSActions> iActions = eval.getListOfActions().iterator();
        while (iActions.hasNext()) {
            SOSActions a = iActions.next();
            // Die Nodelist verwenden
            for (int i = 0; i < a.getCommandNodes().getLength(); i++) {
                Node n = a.getCommandNodes().item(i);
                if ("command".equals(n.getNodeName())) {
                    System.out.println(n.getNodeName());
                    NamedNodeMap attr = n.getAttributes();
                    if (attr != null) {
                        for (int ii = 0; ii < attr.getLength(); ii++) {
                            System.out.println(attr.item(ii).getNodeName() + "=" + attr.item(ii).getNodeValue());
                        }
                    }
                }
            }
            for (int i = 0; i < a.getCommands().getChildNodes().getLength(); i++) {
                Node n = a.getCommands().getChildNodes().item(i);
                if ("command".equals(n.getNodeName())) {
                    System.out.println(n.getNodeName());
                    NamedNodeMap attr = n.getAttributes();
                    if (attr != null) {
                        for (int ii = 0; ii < attr.getLength(); ii++) {
                            System.out.println(attr.item(ii).getNodeName() + "=" + attr.item(ii).getNodeValue());
                        }
                    }
                }
            }

            if (a != null) {
                Iterator<SOSEventGroups> i = a.getListOfEventGroups().iterator();
                while (i.hasNext()) {
                    SOSEventGroups evg = i.next();
                    Iterator<SchedulerEvent> iEvents = evg.getListOfEvents().iterator();
                    while (iEvents.hasNext()) {
                        SchedulerEvent event = iEvents.next();
                        System.out.println(event.getJobName() + " " + eval.getEventStatus(event));
                    }
                }
            }
            if (a.isActive(eval.getListOfActiveEvents())) {
                System.out.println(a.name + " is active");
            } else {
                System.out.println(a.name + " is NOT active");
            }
        }
    }

	public void setListOfActiveEvents(List<SchedulerEvent> listOfActiveEvents) {
		this.listOfActiveEvents = listOfActiveEvents;
	}

}