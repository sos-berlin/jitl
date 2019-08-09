package com.sos.jitl.eventing.eventhandler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import sos.util.SOSDate;
import sos.util.SOSFile;
import sos.xml.SOSXMLTransformer;

public class XsltEventHandler {
	private Collection<Object> eventHandlerResultedCommands = new Vector<Object>();
	private Document events;
	private static final Logger LOGGER = LoggerFactory.getLogger(XmlEventHandler.class);
	private String eventHandlerFilepath;
	private String eventHandlerFilespec;
	private String eventJobName;
	private String eventJobChainName;
	private String eventClass;
	private Calendar expirationDate = null;

	public XsltEventHandler(Collection<Object> eventHandlerResultedCommands, Document events,
			String eventHandlerFilepath, String eventHandlerFilespec, String eventJobName, String eventJobChainName,
			String eventClass, Calendar expirationDate) {
		super();
		this.eventHandlerResultedCommands = eventHandlerResultedCommands;
		this.events = events;
		this.eventHandlerFilepath = eventHandlerFilepath;
		this.eventHandlerFilespec = eventHandlerFilespec;
		this.eventJobName = eventJobName;
		this.eventJobChainName = eventJobChainName;
		this.eventClass = eventClass;
		this.expirationDate = expirationDate;
	}

	public void getListOfCommands() throws IOException, Exception {
		List<File> eventHandlerFileList = new ArrayList<File>();
		File eventHandlerFile = new File(eventHandlerFilepath);

		if (eventHandlerFile.isDirectory()) {
			if (!eventHandlerFile.canRead()) {
				throw new Exception(
						"event handler directory is not accessible: " + eventHandlerFile.getAbsolutePath());
			}
			LOGGER.debug("retrieving event handlers from directory: " + this.eventHandlerFilepath
					+ " for file specification: " + this.eventHandlerFilespec);
			if (this.eventJobChainName != null && !this.eventJobChainName.isEmpty()) {
				String fileSpec = "^" + this.eventJobChainName + "(\\..*)?\\.job_chain\\.sos.scheduler.xsl$";
				LOGGER.debug(".. looking for special event handler for job chain: " + fileSpec);
				Vector<File> specialFiles = SOSFile.getFilelist(this.eventHandlerFilepath, fileSpec, 0);
				for (File specialEventHandler : specialFiles) {
				    if (specialEventHandler.exists() && specialEventHandler.canRead()) {
                        eventHandlerFileList.add(specialEventHandler);
                        LOGGER.debug(".. using special event handler for job chain: " + specialEventHandler.getAbsolutePath());
                    }
				}
			}
			if (this.eventJobName != null && !this.eventJobName.isEmpty()) {
				String fileSpec = "^" + this.eventJobName + "(\\..*)?\\.job\\.sos.scheduler.xsl$";
				LOGGER.debug(".. looking for special event handler for job: " + fileSpec);
				Vector<File> specialFiles = SOSFile.getFilelist(this.eventHandlerFilepath, fileSpec, 0);
				for (File specialEventHandler : specialFiles) {
					if (specialEventHandler.exists() && specialEventHandler.canRead()) {
						eventHandlerFileList.add(specialEventHandler);
						LOGGER.debug(".. using special event handler for job: " + specialEventHandler.getAbsolutePath());
					}
				}
			}
			if (this.eventClass != null && !this.eventClass.isEmpty()) {
				String fileSpec = "^" + this.eventClass + "(\\..*)?\\.event_class\\.sos.scheduler.xsl$";
				LOGGER.debug(".. looking for special event handlers for event class: " + fileSpec);
				Vector<File> specialFiles = SOSFile.getFilelist(this.eventHandlerFilepath, fileSpec, 0);
				for (File specialEventHandler : specialFiles) {
				    if (specialEventHandler.exists() && specialEventHandler.canRead()) {
                        eventHandlerFileList.add(specialEventHandler);
                        LOGGER.debug(".. using special event handler for event class: " + specialEventHandler.getAbsolutePath());
                    }
				}
			}
			eventHandlerFileList.addAll(SOSFile.getFilelist(this.eventHandlerFilepath, this.eventHandlerFilespec, 0));
			LOGGER.debug(".. adding list of default event handlers: " + this.eventHandlerFilepath + "/"
					+ this.eventHandlerFilespec);
		} else {
			if (!eventHandlerFile.canRead()) {
				throw new Exception("event handler file is not accessible: " + eventHandlerFile.getAbsolutePath());
			}
			eventHandlerFileList.add(eventHandlerFile);
		}
		Map<String, String> stylesheetParameters = new HashMap<String, String>();
		stylesheetParameters.put("current_date", SOSDate.getCurrentTimeAsString());
		if (this.expirationDate != null) {
			stylesheetParameters.put("expiration_date", SOSDate.getTimeAsString(this.expirationDate.getTime()));
		}
		this.events.getDocumentElement().setAttribute("current_date", SOSDate.getCurrentTimeAsString());
		this.events.getDocumentElement().setAttribute("expiration_date", SOSDate.getTimeAsString(this.expirationDate.getTime()));
		for (File eventHandler : eventHandlerFileList) {
			if (eventHandler == null) {
				continue;
			}
			File stylesheetResultFile = File.createTempFile("sos", ".xml");
			stylesheetResultFile.deleteOnExit();
			this.eventHandlerResultedCommands.add(stylesheetResultFile);
			LOGGER.debug(".. processing events with stylesheet: " + eventHandler.getAbsolutePath());
			SOSXMLTransformer.transform(this.events, eventHandler, stylesheetResultFile, stylesheetParameters);
		}

	}
}
