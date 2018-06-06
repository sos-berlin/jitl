package com.sos.jitl.eventing.eventhandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.apache.xpath.XPathAPI;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sos.jitl.eventing.db.SchedulerEventDBItem;

import sos.util.SOSFile;

public class XmlEventHandler {
	private Collection<Object> eventHandlerResultedCommands = new Vector<Object>();
	private List<SchedulerEvent> listOfActiveEvents;
	private static final Logger LOGGER = LoggerFactory.getLogger(XmlEventHandler.class);
	private String eventHandlerFilepath;
	private String eventJobChainName;
	private String eventClass;

	public XmlEventHandler(Collection<Object> eventHandlerResultedCommands, List<SchedulerEventDBItem> listOfEvents,
			String eventHandlerFilepath, String eventJobChainName, String eventClass)  {
		super();
		this.eventHandlerResultedCommands = eventHandlerResultedCommands;
		this.listOfActiveEvents = getActiveSchedulerEventList(listOfEvents);
		this.eventHandlerFilepath = eventHandlerFilepath;
		this.eventJobChainName = eventJobChainName;
		this.eventClass = eventClass;
	}
	
	private List<SchedulerEvent> getActiveSchedulerEventList(List<SchedulerEventDBItem> listOfEvents)   {

 		List<SchedulerEvent> listOfActiveEvents = new ArrayList<SchedulerEvent>();
		for (SchedulerEventDBItem item : listOfEvents) {
			SchedulerEvent schedulerEvent = new SchedulerEvent(item);
			listOfActiveEvents.add(schedulerEvent);
		}
		return listOfActiveEvents;
	}


	private boolean analyseMonitorEventHandler(final String fileSpec, final String fileSpecLog) throws Exception {
		boolean erg = false;
		LOGGER.debug(".. looking for special event handler for: " + fileSpecLog + " " + fileSpec);
		Vector<File> specialFiles = SOSFile.getFilelist(this.eventHandlerFilepath, fileSpec, 0);
		Iterator<File> iter = specialFiles.iterator();
		while (iter.hasNext()) {
			File actionEventHandler = iter.next();
			boolean ignore = false;
			if ("".equals(fileSpecLog)) {
				String filename = actionEventHandler.getAbsolutePath();
				ignore = (filename.endsWith(".job.actions.xml") || filename.endsWith(".job_chain.actions.xml")
						|| filename.endsWith(".event_class.actions.xml"));
			}
			if (!ignore && actionEventHandler.exists() && actionEventHandler.canRead()) {
				erg = true;
				LOGGER.debug(".. analysing action event handler: " + actionEventHandler.getCanonicalPath());
				SOSEvaluateEvents eval = new SOSEvaluateEvents();
				try {
					eval.setListOfActiveEvents(this.listOfActiveEvents);
					eval.readConfigurationFile(actionEventHandler);
				} catch (Exception e) {
					LOGGER.error(e.getMessage(), e);
				}
				Iterator<SOSActions> iActions = eval.getListOfActions().iterator();
				while (iActions.hasNext()) {
					SOSActions a = iActions.next();
					LOGGER.debug(".... checking action " + a.getName());
					if (a.isActive(eval.getListOfActiveEvents())) {
						LOGGER.debug(".... added action:" + a.getName());
						this.eventHandlerResultedCommands.add(a.getCommands());
						// Next only for the log output.
						NodeList commands = XPathAPI.selectNodeList(a.getCommands(),
								"command | remove_event | add_event");

						for (int i = 0; i < commands.getLength(); i++) {
							Node n = commands.item(i);
							if ("command".equals(n.getNodeName()) || "remove_event".equals(n.getNodeName())
									|| "add_event".equals(n.getNodeName())) {
								LOGGER.debug(".. " + n.getNodeName() + " was added");
								NamedNodeMap attr = n.getAttributes();
								if (attr != null) {
									for (int ii = 0; ii < attr.getLength(); ii++) {
										LOGGER.debug("...." + attr.item(ii).getNodeName() + "="
												+ attr.item(ii).getNodeValue());
									}
								}
							}
						}
					}
				}
			}
		}
		return erg;
	}

	public void getListOfCommands() throws IOException, Exception {
		boolean fileFound = false;
		File eventHandlerFile = new File(this.eventHandlerFilepath);
		if (eventHandlerFile.isDirectory()) {
			if (!eventHandlerFile.canRead()) {
				throw new Exception(
						"event handler directory is not accessible: " + eventHandlerFile.getCanonicalPath());
			}
			LOGGER.debug("retrieving event handlers from directory: " + this.eventHandlerFilepath
					+ " for file specification: Action");
			String fileSpec = "";
			String fileSpecLog = "";
			if (this.eventJobChainName != null && !this.eventJobChainName.isEmpty()) {
				fileSpec = "^" + this.eventJobChainName + "(\\..*)?\\.job_chain\\.actions.xml$";
				fileSpecLog = "job_chain";
				fileFound = analyseMonitorEventHandler(fileSpec, fileSpecLog);
			}
			if (!fileFound && this.eventClass != null && !this.eventClass.isEmpty()) {
				fileSpec = "^" + this.eventClass + "(\\..*)?\\.event_class\\.actions.xml$";
				fileSpecLog = "event_class";
				fileFound = analyseMonitorEventHandler(fileSpec, fileSpecLog);
			}
			if (!fileFound) {
				fileSpec = "(\\..*)?\\.actions\\.xml$";
				fileSpecLog = "";
				fileFound = analyseMonitorEventHandler(fileSpec, fileSpecLog);
			}
		}
	}

}
