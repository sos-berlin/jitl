package com.sos.jitl.eventing.eventhandler;

import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
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
		File eventHandler = null;
		List<File> eventHandlerFileList = new ArrayList<File>();
		File eventHandlerFile = new File(eventHandlerFilepath);

		if (eventHandlerFile.isDirectory()) {
			if (!eventHandlerFile.canRead()) {
				throw new Exception(
						"event handler directory is not accessible: " + eventHandlerFile.getCanonicalPath());
			}
			LOGGER.debug("retrieving event handlers from directory: " + this.eventHandlerFilepath
					+ " for file specification: " + this.eventHandlerFilespec);
			if (this.eventJobChainName != null && !this.eventJobChainName.isEmpty()) {
				String fileSpec = "^" + this.eventJobChainName + "(\\..*)?\\.job_chain\\.sos.scheduler.xsl$";
				LOGGER.debug(".. looking for special event handler for job chain: " + fileSpec);
				Vector<?> specialFiles = SOSFile.getFilelist(this.eventHandlerFilepath, fileSpec, 0);
				Iterator<?> iter = specialFiles.iterator();
				while (iter.hasNext()) {
					File specialEventHandler = (File) iter.next();
					if (specialEventHandler.exists() && specialEventHandler.canRead()) {
						eventHandlerFileList.add(specialEventHandler);
						LOGGER.debug(".. using special event handler for job chain: "
								+ specialEventHandler.getCanonicalPath());
					}
				}
			}
			if (this.eventJobName != null && !this.eventJobName.isEmpty()) {
				String fileSpec = "^" + this.eventJobName + "(\\..*)?\\.job\\.sos.scheduler.xsl$";
				LOGGER.debug(".. looking for special event handler for job: " + fileSpec);
				Vector<?> specialFiles = SOSFile.getFilelist(this.eventHandlerFilepath, fileSpec, 0);
				Iterator<?> iter = specialFiles.iterator();
				while (iter.hasNext()) {
					File specialEventHandler = (File) iter.next();
					if (specialEventHandler.exists() && specialEventHandler.canRead()) {
						eventHandlerFileList.add(specialEventHandler);
						LOGGER.debug(
								".. using special event handler for job: " + specialEventHandler.getCanonicalPath());
					}
				}
			}
			if (this.eventClass != null && !this.eventClass.isEmpty()) {
				String fileSpec = "^" + this.eventClass + "(\\..*)?\\.event_class\\.sos.scheduler.xsl$";
				LOGGER.debug(".. looking for special event handlers for event class: " + fileSpec);
				Vector<?> specialFiles = SOSFile.getFilelist(this.eventHandlerFilepath, fileSpec, 0);
				Iterator<?> iter = specialFiles.iterator();
				while (iter.hasNext()) {
					File specialEventHandler = (File) iter.next();
					if (specialEventHandler.exists() && specialEventHandler.canRead()) {
						eventHandlerFileList.add(specialEventHandler);
						LOGGER.debug(".. using special event handler for event class: "
								+ specialEventHandler.getCanonicalPath());
					}
				}
			}
			eventHandlerFileList.addAll(SOSFile.getFilelist(this.eventHandlerFilepath, this.eventHandlerFilespec, 0));
			LOGGER.debug(".. adding list of default event handlers: " + this.eventHandlerFilepath + "/"
					+ this.eventHandlerFilespec);
		} else {
			if (!eventHandlerFile.canRead()) {
				throw new Exception("event handler file is not accessible: " + eventHandlerFile.getCanonicalPath());
			}
			eventHandlerFileList.add(eventHandlerFile);
		}
		HashMap<String, String> stylesheetParameters = new HashMap<String, String>();
		stylesheetParameters.put("current_date", SOSDate.getCurrentTimeAsString());
		if (this.expirationDate != null) {
			stylesheetParameters.put("expiration_date", SOSDate.getTimeAsString(this.expirationDate.getTime()));
		}
		this.events.getDocumentElement().setAttribute("current_date", SOSDate.getCurrentTimeAsString());
		this.events.getDocumentElement().setAttribute("expiration_date",
				SOSDate.getTimeAsString(this.expirationDate.getTime()));
		Iterator<File> eventHandlerFileListIterator = eventHandlerFileList.iterator();
		while (eventHandlerFileListIterator.hasNext()) {
			eventHandler = eventHandlerFileListIterator.next();
			if (eventHandler == null) {
				continue;
			}
			File stylesheetResultFile = File.createTempFile("sos", ".xml");
			stylesheetResultFile.deleteOnExit();
			this.eventHandlerResultedCommands.add(stylesheetResultFile);
			LOGGER.debug(".. processing events with stylesheet: " + eventHandler.getCanonicalPath());
			SOSXMLTransformer.transform(this.xmlDocumentToString(this.events), eventHandler, stylesheetResultFile,
					stylesheetParameters);
		}

	}

	private String xmlDocumentToString(final Document document) throws Exception {

		StringWriter out = new StringWriter();
		XMLSerializer serializer = new XMLSerializer(out, new OutputFormat(document));
		serializer.serialize(document);
		return out.toString();

	}
}
