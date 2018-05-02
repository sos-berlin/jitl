package sos.scheduler.job;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.xalan.xslt.EnvironmentCheck;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import sos.scheduler.command.SOSSchedulerCommand;
import sos.scheduler.consoleviews.events.SOSActions;
import sos.scheduler.consoleviews.events.SOSEvaluateEvents;
import sos.spooler.Variable_set;
import sos.util.ParameterSubstitutor;
import sos.util.SOSDate;
import sos.util.SOSFile;
import sos.util.SOSSchedulerLogger;
import sos.xml.SOSXMLTransformer;
import sos.xml.SOSXMLXPath;

import com.sos.classes.CustomEventsUtil;
import com.sos.hibernate.classes.SOSHibernateFactory;
import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.jitl.eventing.db.SchedulerEventDBItem;
import com.sos.jitl.eventing.db.SchedulerEventDBLayer;
import com.sos.jitl.reporting.db.DBLayer;
import com.sos.jitl.reporting.helper.ReportUtil;

/** @author andreas pueschel */
public class JobSchedulerEventJob extends JobSchedulerJob {

	private String confFile;
	private SOSHibernateSession session;
	private SchedulerEventDBLayer schedulerEventDBLayer;
	protected SOSSchedulerLogger sossosLogger = null;

	private static final String NEVER_DATE = "2999-01-01 00:00:00";
	private String eventAction = "";
	private String eventSchedulerId = "";
	private String eventRemoteSchedulerHost = "";
	private String eventRemoteSchedulerPort = "";
	private String eventJobChainName = "";
	private String eventOrderId = "";
	private String eventJobName = "";
	private String eventClass = "";
	private String eventId = "";
	private String eventExitCode = "";
	private String eventExpires = "";
	private String eventCreated = "";
	private Element eventParameters = null;
	private String expirationPeriod = "";
	private String expirationCycle = "";
	private Calendar expirationDate = null;
	private String eventHandlerFilepath = "";
	private String eventHandlerFilespec = "";
	private Variable_set parameters = null;
	private Document events = null;
	private Collection<File> eventHandlerFileList = new LinkedHashSet<File>();
	private Iterator<File> eventHandlerFileListIterator = null;
	private Collection<Object> eventHandlerResultFileList = new Vector<Object>();
	private Iterator<Object> eventHandlerResultFileListIterator = null;
	private int socket_timeout = 5;
	private ParameterSubstitutor parameterSubstitutor;
	private int httpPort;

	@Override
	public boolean spooler_init() {
		try {
			sossosLogger = new SOSSchedulerLogger(spooler.log());
			httpPort = SOSSchedulerCommand.getHTTPPortFromScheduler(spooler);
		} catch (Exception e) {
			sosLogger.debug9("could not read http port from scheduler.xml");
		}

		boolean rc = super.spooler_init();
		EnvironmentCheck ec = new EnvironmentCheck();
		StringWriter sWri = new StringWriter();
		PrintWriter pWri = new PrintWriter(sWri);
		confFile = "";

		if (spooler_task.params() != null) {
			confFile = spooler_task.params().value("configuration_file");
		}
		if (spooler_job.order_queue() != null) {
			confFile = spooler_task.order().params().value("configuration_file");
		}

		if (confFile != null && !confFile.isEmpty()) {
			sosLogger.debug9("configuration_file from param");
		} else {
			sosLogger.debug9("configuration_file from scheduler");
			confFile = getHibernateConfigurationReporting().toFile().getAbsolutePath();
		}

		try {
			session = getSession(confFile);
			schedulerEventDBLayer = new SchedulerEventDBLayer(session);

		} catch (Exception e) {
			sosLogger.error("Could not create session: " + e.getMessage());
			throw new RuntimeException(e);
		}

		ec.checkEnvironment(pWri);
		pWri.close();
		sosLogger.debug9("Checking Xalan environment...");
		sosLogger.debug9(sWri.toString());
		return rc;
	}

	@Override
	public boolean spooler_process() {
		boolean rc = true;
		try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			Document eventDocument = docBuilder.newDocument();
			eventDocument.appendChild(eventDocument.createElement("events"));
			this.setEvents(eventDocument);
			this.setParameters(spooler.create_variable_set());
			if (this.getParameters().value("action") != null
					&& "refresh".equalsIgnoreCase(this.getParameters().value("action"))) {
				spooler.variables().set_var(JobSchedulerConstants.EVENTS_VARIABLE_NAME, "");
				this.getSchedulerEvents();
				return true;
			}
			// fetch events from global JobScheduler variable
			this.getSchedulerEvents();
			HashSet<String> jobParameterNames = new HashSet<String>();
			try {
				if (spooler_task.params() != null) {
					this.getParameters().merge(spooler_task.params());
				}
				if (spooler_job.order_queue() != null) {
					this.getParameters().merge(spooler_task.order().params());
				}
				// event processing parameters
				if (this.getParameters().value("socket_timeout") != null
						&& !this.getParameters().value("socket_timeout").isEmpty()) {
					this.setSockettimeout(this.getParameters().value("socket_timeout"));
					sosLogger.debug9(".. parameter [socket_timeout]: " + socket_timeout);
				} else {
					this.setSockettimeout("5");
				}
				if (this.getParameters().value("action") != null && !this.getParameters().value("action").isEmpty()) {
					this.setEventAction(this.getParameters().value("action"));
					sosLogger.debug9(".. parameter [action]: " + this.getEventAction());
				} else {
					this.setEventAction("add");
				}
				jobParameterNames.add("action");
				if (this.getParameters().value("scheduler_id") != null
						&& !this.getParameters().value("scheduler_id").isEmpty()) {
					this.setEventSchedulerId(this.getParameters().value("scheduler_id"));
					sosLogger.debug9(".. parameter [scheduler_id]: " + this.getEventSchedulerId());
				} else {
					this.setEventSchedulerId(spooler.id());
				}
				jobParameterNames.add("scheduler_id");
				if (this.getParameters().value("spooler_id") != null
						&& !this.getParameters().value("spooler_id").isEmpty()) {
					this.setEventSchedulerId(this.getParameters().value("spooler_id"));
					sosLogger.debug9(".. parameter [spooler_id]: " + this.getEventSchedulerId());
				}
				jobParameterNames.add("spooler_id");
				// standard parameters
				if (this.getParameters().value("remote_scheduler_host") != null
						&& !this.getParameters().value("remote_scheduler_host").isEmpty()) {
					this.setEventRemoteSchedulerHost(this.getParameters().value("remote_scheduler_host"));
					sosLogger.debug9(".. parameter [remote_scheduler_host]: " + this.getEventRemoteSchedulerHost());
				} else {
					this.setEventRemoteSchedulerHost("");
				}
				jobParameterNames.add("remote_scheduler_host");
				if (this.getParameters().value("remote_scheduler_port") != null
						&& !this.getParameters().value("remote_scheduler_port").isEmpty()) {
					this.setEventRemoteSchedulerPort(this.getParameters().value("remote_scheduler_port"));
					sosLogger.debug9(".. parameter [remote_scheduler_port]: " + this.getEventRemoteSchedulerPort());
				} else {
					this.setEventRemoteSchedulerPort("0");
				}
				jobParameterNames.add("remote_scheduler_port");
				if (this.getParameters().value("job_chain") != null
						&& !this.getParameters().value("job_chain").isEmpty()) {
					this.setEventJobChainName(this.getParameters().value("job_chain"));
					sosLogger.debug9(".. parameter [job_chain]: " + this.getEventJobChainName());
				} else {
					this.setEventJobChainName("");
				}
				jobParameterNames.add("job_chain");
				if (this.getParameters().value("order_id") != null
						&& !this.getParameters().value("order_id").isEmpty()) {
					this.setEventOrderId(this.getParameters().value("order_id"));
					sosLogger.debug9(".. parameter [order_id]: " + this.getEventOrderId());
				} else {
					this.setEventOrderId("");
				}
				jobParameterNames.add("order_id");
				if (this.getParameters().value("job_name") != null
						&& !this.getParameters().value("job_name").isEmpty()) {
					this.setEventJobName(this.getParameters().value("job_name"));
					sosLogger.debug9(".. parameter [job_name]: " + this.getEventJobName());
				} else {
					this.setEventJobName("");
				}
				jobParameterNames.add("job_name");
				if (this.getParameters().value("event_class") != null
						&& !this.getParameters().value("event_class").isEmpty()) {
					this.setEventClass(this.getParameters().value("event_class"));
					sosLogger.debug9(".. parameter [event_class]: " + this.getEventClass());
				} else {
					this.setEventClass("");
				}
				jobParameterNames.add("event_class");
				if (this.getParameters().value("event_id") != null
						&& !this.getParameters().value("event_id").isEmpty()) {
					this.setEventId(this.getParameters().value("event_id"));
					sosLogger.debug9(".. parameter [event_id]: " + this.getEventId());
				} else {
					this.setEventId("");
				}
				jobParameterNames.add("event_id");
				if (this.getParameters().value("exit_code") != null
						&& !this.getParameters().value("exit_code").isEmpty()) {
					this.setEventExitCode(this.getParameters().value("exit_code"));
					sosLogger.debug9(".. parameter [exit_code]: " + this.getEventExitCode());
				} else {
					this.setEventExitCode("");
				}
				jobParameterNames.add("exit_code");
				if (this.getParameters().value("expires") != null && !this.getParameters().value("expires").isEmpty()) {
					this.setEventExpires(this.getParameters().value("expires"));
					sosLogger.debug9(".. parameter [expires]: " + this.getEventExpires());
				} else {
					this.setEventExpires("");
				}
				jobParameterNames.add("expires");
				if (this.getParameters().value("created") != null && !this.getParameters().value("created").isEmpty()) {
					this.setEventCreated(this.getParameters().value("created"));
					sosLogger.debug9(".. parameter [created]: " + this.getEventCreated());
				} else {
					this.setEventCreated(SOSDate.getCurrentTimeAsString());
				}
				jobParameterNames.add("created");
				if (this.getParameters().value("expiration_period") != null
						&& !this.getParameters().value("expiration_period").isEmpty()) {
					this.setExpirationPeriod(this.getParameters().value("expiration_period"));
					sosLogger.debug9(".. parameter [expiration_period]: " + this.getExpirationPeriod());
				} else {
					this.setExpirationPeriod("24:00:00");
				}
				jobParameterNames.add("expiration_period");
				if (this.getParameters().value("expiration_cycle") != null
						&& !this.getParameters().value("expiration_cycle").isEmpty()) {
					this.setExpirationCycle(this.getParameters().value("expiration_cycle"));
					sosLogger.debug9(".. parameter [expiration_cycle]: " + this.getExpirationCycle());
				} else {
					this.setExpirationCycle("");
				}
				jobParameterNames.add("expiration_cycle");
				if (this.getParameters().value("event_handler_filepath") != null
						&& !this.getParameters().value("event_handler_filepath").isEmpty()) {
					this.setEventHandlerFilepath(this.getParameters().value("event_handler_filepath"));
					sosLogger.debug9(".. parameter [event_handler_filepath]: " + this.getEventHandlerFilepath());
				} else {
					this.setEventHandlerFilepath("./config/events");
				}
				jobParameterNames.add("event_handler_filepath");
				if (this.getParameters().value("event_handler_filespec") != null
						&& !this.getParameters().value("event_handler_filespec").isEmpty()) {
					this.setEventHandlerFilespec(this.getParameters().value("event_handler_filespec"));
					sosLogger.debug9(".. parameter [event_handler_filespec]: " + this.getEventHandlerFilespec());
				} else {
					this.setEventHandlerFilespec("\\.sos.scheduler.xsl$");
				}
				jobParameterNames.add("event_handler_filespec");
				this.setEventParameters(getEvents().createElement("params"));
				String[] parameterNames = this.getParameters().names().split(";");
				for (int i = 0; i < parameterNames.length; i++) {
					if (!jobParameterNames.contains(parameterNames[i])) {
						Element param = getEvents().createElement("param");
						param.setAttribute("name", parameterNames[i]);
						param.setAttribute("value", this.getParameters().value(parameterNames[i]));
						if (parameterNames[i].contains("password")) {
							sosLogger.debug9("Event parameter [" + parameterNames[i] + "]: *****");
						} else {
							sosLogger.debug9("Event parameter [" + parameterNames[i] + "]: "
									+ this.getParameters().value(parameterNames[i]));
						}
						this.getEventParameters().appendChild(param);
					}
				}
			} catch (Exception e) {
				sosLogger.error("error occurred processing parameters: " + e.getMessage());
				throw e;
			}
			this.setExpirationDate(calculateExpirationDate(expirationCycle, expirationPeriod));
			CustomEventsUtil customEventsUtil = new CustomEventsUtil(JobSchedulerEventJob.class.getName());
			try {
				if ("add".equalsIgnoreCase(this.getEventAction())) {
					sosLogger.info("adding event: " + this.getEventClass() + " " + this.getEventId());
					this.addEvent();
					customEventsUtil.addEvent("CustomEventAdded");
				} else if ("remove".equalsIgnoreCase(this.getEventAction())) {
					sosLogger.info("removing event: " + this.getEventClass() + " " + this.getEventId());
					this.removeEvent();
					customEventsUtil.addEvent("CustomEventDeleted");
				} else {
					sosLogger.info("processing events");
				}
				this.processSchedulerEvents();
				this.putSchedulerEvents();
				spooler.execute_xml(customEventsUtil.getEventCommandAsXml());
			} catch (Exception e) {
				throw new Exception("error occurred processing event: " + e.getMessage());
			}
			return spooler_job.order_queue() != null ? rc : false;
		} catch (Exception e) {
			spooler_log.warn("error occurred processing event [" + this.getEventClass() + " " + this.getEventId()
					+ "]: " + e.getMessage());
			return false;
		}
	}

	protected static Calendar calculateExpirationDate(final String expirationCycle, final String expirationPeriod)
			throws Exception {
		Calendar cal = Calendar.getInstance();
		try {
			cal.setTime(SOSDate.getCurrentTime());
			if (expirationCycle.indexOf(":") > -1) {
				String[] timeArray = expirationCycle.split(":");
				int hours = Integer.parseInt(timeArray[0]);
				int minutes = Integer.parseInt(timeArray[1]);
				int seconds = 0;
				if (timeArray.length > 2) {
					seconds = Integer.parseInt(timeArray[2]);
				}
				cal.set(Calendar.HOUR_OF_DAY, hours);
				cal.set(Calendar.MINUTE, minutes);
				cal.set(Calendar.SECOND, seconds);
				if (cal.after(SOSDate.getCurrentTime())) {
					cal.add(Calendar.DAY_OF_MONTH, 1);
				}
			} else if (expirationPeriod.indexOf(":") > -1) {
				String[] timeArray = expirationPeriod.split(":");
				int hours = Integer.parseInt(timeArray[0]);
				int minutes = Integer.parseInt(timeArray[1]);
				int seconds = 0;
				if (timeArray.length > 2) {
					seconds = Integer.parseInt(timeArray[2]);
				}
				if (hours > 0) {
					cal.add(Calendar.HOUR_OF_DAY, hours);
				}
				if (minutes > 0) {
					cal.add(Calendar.MINUTE, minutes);
				}
				if (seconds > 0) {
					cal.add(Calendar.SECOND, seconds);
				}
			} else if (!expirationPeriod.isEmpty()) {
				cal.add(Calendar.SECOND, Integer.parseInt(expirationPeriod));
			}
		} catch (Exception e) {
			throw e;
		}
		return cal;
	}

	private void getSchedulerEvents() throws Exception {
		try {
			String eventSet = spooler.var(JobSchedulerConstants.EVENTS_VARIABLE_NAME);
			removeExpiredEventsFromDatabase();

			if ((eventSet == null || eventSet.isEmpty())) {
				readEventsFromDB(spooler.id(), getEvents());
			} else {
				eventSet = eventSet.replaceAll(String.valueOf((char) 254), "<").replaceAll(String.valueOf((char) 255),
						">");
				sosLogger.debug9("current event set: " + eventSet);
				if (eventSet.isEmpty()) {
					return;
				}
				DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
				Document eventDocument = docBuilder.parse(new InputSource(new StringReader(eventSet)));
				this.setEvents(eventDocument);
				sosLogger.debug9("looking for //events/event");
				NodeList nodes = org.apache.xpath.XPathAPI.selectNodeList(this.getEvents(), "//events/event");
				sosLogger.debug9("nodes.getLength(): " + nodes.getLength());
				int activeNodeCount = 0;
				int expiredNodeCount = 0;
				for (int i = 0; i < nodes.getLength(); i++) {
					Node node = nodes.item(i);
					if (node == null || node.getNodeType() != Node.ELEMENT_NODE) {
						continue;
					}
					Node curEventExpires = node.getAttributes().getNamedItem("expires");
					if (curEventExpires == null || curEventExpires.getNodeValue() == null
							|| curEventExpires.getNodeValue().isEmpty()
							|| "never".equalsIgnoreCase(curEventExpires.getNodeValue())) {
						activeNodeCount++;
						continue;
					}
					Calendar expiresDate = GregorianCalendar.getInstance();
					Calendar now = GregorianCalendar.getInstance();
					expiresDate.setTime(SOSDate.getTime(curEventExpires.getNodeValue()));
					if (expiresDate.before(now)) {
						sosLogger.debug9("Found expired event");
						this.getEvents().getFirstChild().removeChild(node);
						removeEventFromDatabase(node);
						sosLogger.debug9("event removed");
						expiredNodeCount++;
					} else {
						activeNodeCount++;
					}
				}
				sosLogger.info(activeNodeCount + " events are active, " + expiredNodeCount + " events have expired");
			}
		} catch (Exception e) {
			sosLogger.error("events fetched with errors: " + e.getMessage());
			throw e;
		}
	}

	private SOSHibernateSession getSession(String confFile) throws Exception {
		SOSHibernateFactory sosHibernateFactory = new SOSHibernateFactory(confFile);
		sosHibernateFactory.addClassMapping(DBLayer.getReportingClassMapping());
		sosHibernateFactory.build();
		return sosHibernateFactory.openStatelessSession();
	}

	private void readEventsFromDB(String schedulerId, final Document eventsDoc) throws Exception {
		try {
			sosLogger.debug9("readEventsFromDB");
			if (this.schedulerEventDBLayer != null) {
				schedulerEventDBLayer.getSession().beginTransaction();
				schedulerEventDBLayer.resetFilter();
				schedulerEventDBLayer.getFilter().setSchedulerId(schedulerId);
				schedulerEventDBLayer.getFilter().setSchedulerIdEmpty(true);
				List<SchedulerEventDBItem> listOfEvents = schedulerEventDBLayer.getSchedulerEventList();
				sosLogger.debug9(String.format("%s items found in database", listOfEvents.size()));

				for (SchedulerEventDBItem item : listOfEvents) {
					Element event = eventsDoc.createElement("event");
					event.setAttribute("scheduler_id", item.getSchedulerId());
					event.setAttribute("remote_scheduler_host", item.getRemoteSchedulerHost());
					event.setAttribute("remote_scheduler_port", item.getRemoteSchedulerPortAsString());
					event.setAttribute("job_chain", item.getJobChain());
					event.setAttribute("order_id", item.getOrderId());
					event.setAttribute("job_name", item.getJobName());
					event.setAttribute("event_class", item.getEventClass());
					event.setAttribute("event_id", item.getEventId());
					event.setAttribute("exit_code", item.getExitCodeAsString());
					event.setAttribute("expires", item.getExpiresAsString());
					event.setAttribute("created", item.getCreatedAsString());
					if (item.getParameters() != null && !item.getParameters().isEmpty()) {
						DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
						DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
						Document eventParameters = docBuilder
								.parse(new InputSource(new StringReader(item.getParameters())));
						sosLogger.debug9("Importing params node...");
						Node impParameters = eventsDoc.importNode(eventParameters.getDocumentElement(), true);
						sosLogger.debug9("appending params child...");
						event.appendChild(impParameters);
					}
					eventsDoc.getLastChild().appendChild(event);
				}
				schedulerEventDBLayer.getSession().commit();
			}
		} catch (Exception e) {
			sosLogger.error("Failed to read events from database: " + e.getMessage());
			throw e;
		}
	}

	private void putSchedulerEvents() throws Exception {
		try {
			String eventsString = this.xmlDocumentToString(this.getEvents());
			sosLogger.debug9("Updating events: " + eventsString);
			spooler.set_var(JobSchedulerConstants.EVENTS_VARIABLE_NAME, eventsString
					.replaceAll("<", String.valueOf((char) 254)).replaceAll(">", String.valueOf((char) 255)));
		} catch (Exception e) {
			sosLogger.error("events updated with errors: " + e.getMessage());
			throw e;
		}
	}

	private boolean analyseMonitorEventHandler(final String fileSpec, final String fileSpecLog) throws Exception {
		boolean erg = false;
		sosLogger.debug9(".. looking for special event handler for: " + fileSpecLog + " " + fileSpec);
		Vector<File> specialFiles = SOSFile.getFilelist(this.getEventHandlerFilepath(), fileSpec, 0);
		Iterator<File> iter = specialFiles.iterator();
		while (iter.hasNext()) {
			File actionEventHandler = iter.next();
			boolean ignore = false;
			if ("".equals(fileSpecLog)) {
				String filename = actionEventHandler.getAbsolutePath();
				ignore = (filename.endsWith(".job.actions.xml") || filename.endsWith(".job_chain.actions.xml") || filename.endsWith(".event_class.actions.xml"));
			}
			if (!ignore && actionEventHandler.exists() && actionEventHandler.canRead()) {
				erg = true;
				sosLogger.debug9(".. analysing action event handler: " + actionEventHandler.getCanonicalPath());
				SOSEvaluateEvents eval = new SOSEvaluateEvents(spooler.hostname(), httpPort);
				try {
					eval.setActiveEvents(this.getEvents());
					eval.readConfigurationFile(actionEventHandler);
				} catch (Exception e) {
					sosLogger.error(e.getMessage());
				}
				Iterator<SOSActions> iActions = eval.getListOfActions().iterator();
				while (iActions.hasNext()) {
					SOSActions a = iActions.next();
					sosLogger.debug9(".... checking action " + a.getName());
					if (a.isActive(eval.getListOfActiveEvents())) {
						sosLogger.debug9(".... added action:" + a.getName());
						this.getEventHandlerResultFileList().add(a.getCommands());
						NodeList commands = XPathAPI.selectNodeList(a.getCommands(),
								"command | remove_event | add_event");
						for (int i = 0; i < commands.getLength(); i++) {
							Node n = commands.item(i);
							if ("command".equals(n.getNodeName()) || "remove_event".equals(n.getNodeName())
									|| "add_event".equals(n.getNodeName())) {
								sosLogger.debug9(".. " + n.getNodeName() + " was added");
								NamedNodeMap attr = n.getAttributes();
								if (attr != null) {
									for (int ii = 0; ii < attr.getLength(); ii++) {
										sosLogger.debug9("...." + attr.item(ii).getNodeName() + "="
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

	private String getText(final Node n) {
		if (n != null) {
			return n.getNodeValue();
		} else {
			return "";
		}
	}

	private void getParametersFromEvents() throws Exception {
		sosLogger.debug9("executing getParametersFromEvents....");
		Document doc = this.getEvents();
		NodeList events = doc.getElementsByTagName("event");
		sosLogger.debug9("events length: " + events.getLength());
		for (int i = 0; i < events.getLength(); i++) {
			Node event = events.item(i);
			NamedNodeMap attr = event.getAttributes();
			String event_class = getText(attr.getNamedItem("event_class"));
			String event_id = getText(attr.getNamedItem("event_id"));
			sosLogger.debug9("event_class:" + event_class);
			sosLogger.debug9("event_id:" + event_id);
			NodeList parameters = XPathAPI.selectNodeList(event, "params/param");
			sosLogger.debug9("parameter length: " + parameters.getLength());
			if (parameters != null && parameters.getLength() > 0) {
				for (int ii = 0; ii < parameters.getLength(); ii++) {
					Node eventParam = parameters.item(ii);
					NamedNodeMap paramAttr = eventParam.getAttributes();
					String param_name = getText(paramAttr.getNamedItem("name"));
					String param_value = getText(paramAttr.getNamedItem("value"));
					parameterSubstitutor.addKey(event_class + "." + event_id + "." + param_name, param_value);
					parameterSubstitutor.addKey(event_class + ".*." + param_name, param_value);
					parameterSubstitutor.addKey(event_id + "." + param_name, param_value);
					parameterSubstitutor.addKey(param_name, param_value);
					sosLogger.debug9(event_class + "." + event_id + "." + param_name + "=" + param_value);
				}
			}
		}
	}

	private void getMonitorEventHandler() throws IOException, Exception {
		boolean fileFound = false;
		File eventHandlerFile = new File(this.getEventHandlerFilepath());
		if (eventHandlerFile.isDirectory()) {
			if (!eventHandlerFile.canRead()) {
				throw new Exception(
						"event handler directory is not accessible: " + eventHandlerFile.getCanonicalPath());
			}
			this.getLogger().debug6("retrieving event handlers from directory: " + this.getEventHandlerFilepath()
					+ " for file specification: Action");
			String fileSpec = "";
			String fileSpecLog = "";
			if (!this.getEventJobChainName().isEmpty()) {
				fileSpec = "^" + this.getEventJobChainName() + "(\\..*)?\\.job_chain\\.actions.xml$";
				fileSpecLog = "job_chain";
				fileFound = analyseMonitorEventHandler(fileSpec, fileSpecLog);
			}
			if (!fileFound && !this.getEventClass().isEmpty()) {
				fileSpec = "^" + this.getEventClass() + "(\\..*)?\\.event_class\\.actions.xml$";
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

	private void processSchedulerEvents() throws Exception {
		File eventHandler = null;
		parameterSubstitutor = new ParameterSubstitutor();
		parameterSubstitutor.addKey("current_date", SOSDate.getCurrentTimeAsString());
		getParametersFromEvents();
		try {
			HashMap<File, File> eventHandlerResultReference = new HashMap<File, File>();
			try {
				sosLogger.debug9(".. current events for event handler processing:");
				sosLogger.debug9(this.xmlDocumentToString(this.getEvents()));
				this.setEventHandlerResultFileList(new LinkedHashSet<Object>());
				this.setEventHandlerFileList(new LinkedHashSet<File>());
				this.getMonitorEventHandler();
				File eventHandlerFile = new File(this.getEventHandlerFilepath());
				if (eventHandlerFile.isDirectory()) {
					if (!eventHandlerFile.canRead()) {
						throw new Exception(
								"event handler directory is not accessible: " + eventHandlerFile.getCanonicalPath());
					}
					sosLogger.debug9("retrieving event handlers from directory: " + this.getEventHandlerFilepath()
							+ " for file specification: " + this.getEventHandlerFilespec());
					if (!this.getEventJobChainName().isEmpty()) {
						String fileSpec = this.getEventJobChainName() + "(\\..*)?\\.job_chain\\.sos.scheduler.xsl$";
						sosLogger.debug9(".. looking for special event handler for job chain: " + fileSpec);
						Vector<?> specialFiles = SOSFile.getFilelist(this.getEventHandlerFilepath(), fileSpec, 0);
						Iterator<?> iter = specialFiles.iterator();
						while (iter.hasNext()) {
							File specialEventHandler = (File) iter.next();
							if (specialEventHandler.exists() && specialEventHandler.canRead()) {
								this.getEventHandlerFileList().add(specialEventHandler);
								sosLogger.debug9(".. using special event handler for job chain: "
										+ specialEventHandler.getCanonicalPath());
							}
						}
					}
					if (!this.getEventJobName().isEmpty()) {
						String fileSpec = this.getEventJobName() + "(\\..*)?\\.job\\.sos.scheduler.xsl$";
						sosLogger.debug9(".. looking for special event handler for job: " + fileSpec);
						Vector<?> specialFiles = SOSFile.getFilelist(this.getEventHandlerFilepath(), fileSpec, 0);
						Iterator<?> iter = specialFiles.iterator();
						while (iter.hasNext()) {
							File specialEventHandler = (File) iter.next();
							if (specialEventHandler.exists() && specialEventHandler.canRead()) {
								this.getEventHandlerFileList().add(specialEventHandler);
								sosLogger.debug9(".. using special event handler for job: "
										+ specialEventHandler.getCanonicalPath());
							}
						}
					}
					if (!this.getEventClass().isEmpty()) {
						String fileSpec = this.getEventClass() + "(\\..*)?\\.event_class\\.sos.scheduler.xsl$";
						sosLogger.debug9(".. looking for special event handlers for event class: " + fileSpec);
						Vector<?> specialFiles = SOSFile.getFilelist(this.getEventHandlerFilepath(), fileSpec, 0);
						Iterator<?> iter = specialFiles.iterator();
						while (iter.hasNext()) {
							File specialEventHandler = (File) iter.next();
							if (specialEventHandler.exists() && specialEventHandler.canRead()) {
								this.getEventHandlerFileList().add(specialEventHandler);
								sosLogger.debug9(".. using special event handler for event class: "
										+ specialEventHandler.getCanonicalPath());
							}
						}
					}
					this.getEventHandlerFileList().addAll(
							SOSFile.getFilelist(this.getEventHandlerFilepath(), this.getEventHandlerFilespec(), 0));
					sosLogger.debug9(".. adding list of default event handlers: " + this.getEventHandlerFilepath() + "/"
							+ this.getEventHandlerFilespec());
				} else {
					if (!eventHandlerFile.canRead()) {
						throw new Exception(
								"event handler file is not accessible: " + eventHandlerFile.getCanonicalPath());
					}
					this.getEventHandlerFileList().add(eventHandlerFile);
				}
				HashMap<String, String> stylesheetParameters = new HashMap<String, String>();
				stylesheetParameters.put("current_date", SOSDate.getCurrentTimeAsString());
				stylesheetParameters.put("expiration_date",
						SOSDate.getTimeAsString(this.getExpirationDate().getTime()));
				getEvents().getDocumentElement().setAttribute("current_date", SOSDate.getCurrentTimeAsString());
				getEvents().getDocumentElement().setAttribute("expiration_date",
						SOSDate.getTimeAsString(this.getExpirationDate().getTime()));
				this.setEventHandlerFileListIterator(this.getEventHandlerFileList().iterator());
				while (this.getEventHandlerFileListIterator().hasNext()) {
					eventHandler = this.getEventHandlerFileListIterator().next();
					if (eventHandler == null) {
						continue;
					}
					File stylesheetResultFile = File.createTempFile("sos", ".xml");
					stylesheetResultFile.deleteOnExit();
					this.getEventHandlerResultFileList().add(stylesheetResultFile);
					eventHandlerResultReference.put(stylesheetResultFile, eventHandler);
					sosLogger.debug9(".. processing events with stylesheet: " + eventHandler.getCanonicalPath());
					SOSXMLTransformer.transform(this.xmlDocumentToString(this.getEvents()), eventHandler,
							stylesheetResultFile, stylesheetParameters);
				}
			} catch (Exception e) {
				sosLogger.error("error occurred processing event handler"
						+ (eventHandler != null ? " [" + eventHandler.getCanonicalPath() + "]" : "") + ": "
						+ e.getMessage());
				throw e;
			}
			try {
				this.setEventHandlerResultFileListIterator(this.getEventHandlerResultFileList().iterator());
				while (this.getEventHandlerResultFileListIterator().hasNext()) {
					NodeList commands = null;
					Object result = this.getEventHandlerResultFileListIterator().next();
					if (result instanceof File) {
						File resultFile = (File) result;
						if (resultFile == null) {
							continue;
						}
						File eventHandlerFile = eventHandlerResultReference.get(resultFile);
						sosLogger.debug9(
								".. content of result file for event handler: " + eventHandlerFile.getCanonicalPath());
						sosLogger.debug9(this.getFileContent(resultFile).toString());
						DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
						DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
						Document eventDocument = docBuilder
								.parse(new InputSource(new StringReader(this.getFileContent(resultFile).toString())));
						Element eCommands = eventDocument.getDocumentElement();
						if (eCommands != null) {
							String debug = eCommands.getAttribute("debug");
							if ("true".equalsIgnoreCase(debug)) {
								logTransformation(resultFile, eventHandlerFile);
							}
						}
						commands = XPathAPI.selectNodeList(eventDocument, "//command");
					} else {
						commands = XPathAPI.selectNodeList((Node) result, "command");
					}
					for (int i = 0; i < commands.getLength(); i++) {
						Node command = commands.item(i);
						NamedNodeMap commandAttributes = command.getAttributes();
						String commandHost = spooler.hostname();
						String commandPort = Integer.toString(httpPort);
						String commandProtocol = "http";
						for (int j = 0; j < commandAttributes.getLength(); j++) {
							if ("scheduler_host".equals(commandAttributes.item(j).getNodeName())
									&& !commandAttributes.item(j).getNodeValue().isEmpty()) {
								sosLogger
										.debug9("using host from command: " + commandAttributes.item(j).getNodeValue());
								commandHost = commandAttributes.item(j).getNodeValue();
							}
							if ("scheduler_port".equals(commandAttributes.item(j).getNodeName())
									&& !commandAttributes.item(j).getNodeValue().isEmpty()) {
								commandPort = commandAttributes.item(j).getNodeValue();
							}
							if ("protocol".equals(commandAttributes.item(j).getNodeName())
									&& !commandAttributes.item(j).getNodeValue().isEmpty()) {
								commandProtocol = commandAttributes.item(j).getNodeValue();
							}
						}
						SOSSchedulerCommand schedulerCommand = new SOSSchedulerCommand();
						schedulerCommand.setTimeout(socket_timeout);
						if (!commandHost.isEmpty()) {
							schedulerCommand.setHost(commandHost);
							if (!commandPort.isEmpty()) {
								schedulerCommand.setPort(Integer.parseInt(commandPort));
							} else {
								throw new Exception(
										"empty port has been specified by event handler response for commands");
							}
						} else {
							throw new Exception(
									"empty JobScheduler ID or host and port have been specified by event handler response for commands");
						}
						if (!commandProtocol.isEmpty()) {
							schedulerCommand.setProtocol(commandProtocol);
						}
						try {
							sosLogger.debug9(".. connecting to JobScheduler " + schedulerCommand.getHost() + ":"
									+ schedulerCommand.getPort());
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
								sosLogger.info(".. sending command to remote JobScheduler [" + commandHost + ":"
										+ commandPort + "]: " + commandRequest);
								schedulerCommand.sendRequest(commandRequest);
								SOSXMLXPath answer = new SOSXMLXPath(new StringBuffer(schedulerCommand.getResponse()));
								String errorText = answer.selectSingleNodeValue("//ERROR/@text");
								if (errorText != null && !errorText.isEmpty()) {
									throw new Exception("could not send command to remote JobScheduler [" + commandHost
											+ ":" + commandPort + "]: " + errorText);
								}
							}
						} catch (Exception e) {
							sosLogger.error("Error contacting remote JobScheduler: " + e);
							throw e;
						} finally {
							try {
								schedulerCommand.disconnect();
							} catch (Exception ex) {
								//
							}
						}
					}
				}
			} catch (Exception e) {
				sosLogger.error("could not execute command: " + e.getMessage());
				throw e;
			}
			try {
				this.setEventHandlerResultFileListIterator(this.getEventHandlerResultFileList().iterator());
				while (this.getEventHandlerResultFileListIterator().hasNext()) {
					Object result = this.getEventHandlerResultFileListIterator().next();
					NodeList commands = null;
					if (result instanceof File) {
						File resultFile = (File) result;
						if (resultFile == null) {
							continue;
						}
						DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
						DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
						Document eventDocument = docBuilder
								.parse(new InputSource(new StringReader(this.getFileContent(resultFile).toString())));
						commands = XPathAPI.selectNodeList(eventDocument, "//remove_event");
					} else {
						commands = XPathAPI.selectNodeList((Node) result, "remove_event");
					}
					sosLogger.debug9("-->" + commands.getLength() + " events should be deleted");
					for (int i = 0; i < commands.getLength(); i++) {
						if (commands.item(i) == null) {
							continue;
						}
						if (commands.item(i).getNodeType() == Node.ELEMENT_NODE) {
							this.removeEvents(commands.item(i).getChildNodes());
						}
					}
				}
			} catch (Exception e) {
				sosLogger.error("could not remove event caused by event handler: " + e.getMessage());
				throw e;
			}
			try {
				this.setEventHandlerResultFileListIterator(this.getEventHandlerResultFileList().iterator());
				while (this.getEventHandlerResultFileListIterator().hasNext()) {
					Object result = this.getEventHandlerResultFileListIterator().next();
					NodeList commands = null;
					if (result instanceof File) {
						File resultFile = (File) result;
						if (resultFile == null) {
							continue;
						}
						DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
						DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
						Document eventDocument = docBuilder
								.parse(new InputSource(new StringReader(this.getFileContent(resultFile).toString())));
						commands = XPathAPI.selectNodeList(eventDocument, "//add_event/event");
					} else {
						commands = XPathAPI.selectNodeList((Node) result, "add_event/event");
					}
					for (int i = 0; i < commands.getLength(); i++) {
						if (commands.item(i) == null) {
							continue;
						}
						if (commands.item(i).getNodeType() == Node.ELEMENT_NODE) {
							NamedNodeMap attributes = commands.item(i).getAttributes();
							if (attributes == null) {
								continue;
							}
							Element event = this.getEvents().createElement("event");
							for (int j = 0; j < attributes.getLength(); j++) {
								if ("event_name".equalsIgnoreCase(attributes.item(j).getNodeName())) {
									continue;
								}
								event.setAttribute(attributes.item(j).getNodeName(), attributes.item(j).getNodeValue());
							}
							this.addEvent(event);
						}
					}
				}
			} catch (Exception e) {
				sosLogger.error("could not remove event caused by event handler: " + e.getMessage());
				throw e;
			}
		} catch (Exception e) {
			sosLogger.error("events processed with errors: " + e.getMessage());
			throw e;
		} finally {
			try {
				this.setEventHandlerResultFileListIterator(this.getEventHandlerResultFileList().iterator());
				while (this.getEventHandlerResultFileListIterator().hasNext()) {
					Object result = this.getEventHandlerResultFileListIterator().next();
					if (result instanceof File) {
						File resultFile = (File) result;
						if (resultFile != null && !resultFile.delete()) {
							resultFile.deleteOnExit();
						}
					}
				}
			} catch (Exception e) {
				sosLogger.warn("could not delete temporary file: " + e.getMessage());
			}
		}
	}

	private void logTransformation(final File resultFile, final File stylesheetFile) throws Exception {
		try {
			File logDir = new File(spooler.log_dir());
			File eventLogDir = new File(logDir, "events");
			if (!eventLogDir.exists()) {
				sosLogger.info("creating event log dir: " + eventLogDir.getAbsolutePath());
				if (!eventLogDir.mkdir()) {
					sosLogger.warn("directory [" + eventLogDir.getAbsolutePath() + "] could not be created.");
					return;
				}
			}
			File stylesheetLogDir = new File(eventLogDir, stylesheetFile.getName());
			if (!stylesheetLogDir.exists()) {
				sosLogger.info("creating stylesheet log dir: " + stylesheetLogDir.getAbsolutePath());
				if (!stylesheetLogDir.mkdir()) {
					sosLogger.warn("directory [" + stylesheetLogDir.getAbsolutePath() + "] could not be created.");
					return;
				}
			}
			String timeStamp = SOSDate.getCurrentDateAsString("yyyy-MM-dd_HHmmss_SSS");
			File resultLogFile = new File(stylesheetLogDir, "events_" + timeStamp + "_result.xml");
			SOSFile.copyFile(resultFile, resultLogFile);
			File eventFile = new File(stylesheetLogDir, "events_" + timeStamp + ".xml");
			// write current events to eventFile
			OutputStream fout = new FileOutputStream(eventFile, false);
			OutputStreamWriter out = new OutputStreamWriter(fout, "UTF-8");
			OutputFormat format = new OutputFormat(getEvents());
			format.setEncoding("UTF-8");
			format.setIndenting(true);
			format.setIndent(2);
			XMLSerializer serializer = new XMLSerializer(out, format);
			serializer.serialize(getEvents());
			out.close();
			sosLogger.debug9("current events logged to: " + eventFile.getAbsolutePath());
			sosLogger.debug9("transformation result logged to: " + resultLogFile.getAbsolutePath());
		} catch (Exception e) {
			sosLogger.error("Error logging Transformation result: " + e);
			throw e;
		}
	}

	private void addEvent() throws Exception {
		try {
			sosLogger.debug9(".. constructing event: schedulerId=" + this.getEventSchedulerId() + ", eventClass="
					+ this.getEventClass() + ", eventId=" + this.getEventId());
			Element event = this.getEvents().createElement("event");
			event.setAttribute("scheduler_id", this.getEventSchedulerId());
			event.setAttribute("remote_scheduler_host", this.getEventRemoteSchedulerHost());
			event.setAttribute("remote_scheduler_port", this.getEventRemoteSchedulerPort());
			event.setAttribute("job_chain", this.getEventJobChainName());
			event.setAttribute("order_id", this.getEventOrderId());
			event.setAttribute("job_name", this.getEventJobName());
			event.setAttribute("event_class", this.getEventClass());
			event.setAttribute("event_id", this.getEventId());
			event.setAttribute("exit_code", this.getEventExitCode());
			event.setAttribute("created", this.getEventCreated());
			if (getEventExpires().isEmpty() || "default".equalsIgnoreCase(getEventExpires())) {
				event.setAttribute("expires", SOSDate.getTimeAsString(this.getExpirationDate().getTime()));
			} else {
				event.setAttribute("expires", this.getEventExpires());
			}
			if (this.getEventParameters() != null && this.getEventParameters().getChildNodes().getLength() > 0) {
				event.appendChild(this.getEventParameters());
			}
			this.addEvent(event);
		} catch (Exception e) {
			sosLogger.error(e.getMessage());
			throw e;
		}
	}

	private void addEvent(final Element event) throws Exception {
		addEvent(event, true);
	}

	private void addEvent(final Element event, final boolean replace) throws Exception {
		if (event.getAttribute("scheduler_id").isEmpty()) {
			event.setAttribute("scheduler_id", spooler.id());
		}
		if (replace) {
			Element dummyParent = getEvents().createElement("events");
			Element remEv = (Element) event.cloneNode(true);
			dummyParent.appendChild(remEv);
			sosLogger.debug9("remEv: " + remEv);
			sosLogger.debug9("remEv.getParentNode(): " + remEv.getParentNode());
			remEv.removeAttribute("created");
			remEv.removeAttribute("expires");
			removeEvents(remEv.getParentNode().getChildNodes());
		}
		try {
			String curEventSchedulerId = this.getAttributeValue(event, "scheduler_id");
			String curEventRemoteSchedulerHost = this.getAttributeValue(event, "remote_scheduler_host");
			String curEventRemoteSchedulerPort = this.getAttributeValue(event, "remote_scheduler_port");
			String curEventClass = this.getAttributeValue(event, "event_class");
			String curEventId = this.getAttributeValue(event, "event_id");
			Integer curEventExitCode;
			try {
				curEventExitCode = Integer.parseInt(this.getAttributeValue(event, "exit_code"));
			} catch (NumberFormatException e) {
				curEventExitCode = 0;
			}
			String curEventJobChainName = this.getAttributeValue(event, "job_chain");
			String curEventOrderId = this.getAttributeValue(event, "order_id");
			String curEventJobName = this.getAttributeValue(event, "job_name");
			String curEventCreated = this.getAttributeValue(event, "created");
			String curExpiration_period = this.getAttributeValue(event, "expiration_period");
			String curExpiration_cycle = this.getAttributeValue(event, "expiration_cycle");
			sosLogger.debug9(".. --> curExpiration_period:" + curExpiration_period);
			sosLogger.debug9(".. --> curExpiration_cycle:" + curExpiration_cycle);
			if (curExpiration_period == null || curExpiration_period.isEmpty()) {
				curExpiration_period = this.getExpirationPeriod();
			}
			if (curExpiration_cycle == null || curExpiration_cycle.isEmpty()) {
				curExpiration_cycle = this.getExpirationCycle();
			}
			String curEventExpires = this.getAttributeValue(event, "expires");
			sosLogger.debug9(".. --> curEventExpires:" + curEventExpires);
			if (curEventExpires == null || curEventExpires.isEmpty()) {
				Calendar cal = calculateExpirationDate(curExpiration_cycle, curExpiration_period);
				SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				try {
					curEventExpires = format.format(cal.getTime());
					sosLogger.debug9(".. --> curEventExpires:" + curEventExpires);
				} catch (Exception pe) {
					this.sosLogger
							.warn(cal.getTime().toString() + " is not a valid Date. Expires will be set to default");
				}
			}

			if ("never".equalsIgnoreCase(curEventExpires)) {
				curEventExpires = NEVER_DATE;
				sosLogger.debug9(".. --> curEventExpires:" + curEventExpires);
			}
			sosLogger.info(".. adding event ...: scheduler id=" + curEventSchedulerId + ", event class=" + curEventClass
					+ ", event id=" + curEventId + ", exit code=" + curEventExitCode + ", job chain="
					+ curEventJobChainName + ", order id=" + curEventOrderId + ", job=" + curEventJobName);
			if (curEventId.isEmpty()) {
				throw new Exception("Empty event_id is not allowed.");
			}
			this.getEvents().getLastChild().appendChild(event);
			if (this.session != null) {

				this.session.beginTransaction();
				String paramsString = "";
				NodeList nodes = XPathAPI.selectNodeList(event, "params");
				if (nodes != null && nodes.getLength() > 0) {
					sosLogger.debug9("Event has parameters. Storing parameters...");
					Element params = (Element) nodes.item(0);
					paramsString = xmlElementToString(params);
					sosLogger.debug9(paramsString);
				}

				SchedulerEventDBItem schedulerEventDBItem = new SchedulerEventDBItem();
				schedulerEventDBItem.setSchedulerId(curEventSchedulerId);
				schedulerEventDBItem.setEventClass(curEventClass);
				schedulerEventDBItem.setEventId(curEventId);
				schedulerEventDBItem.setExitCode(curEventExitCode);
				schedulerEventDBItem.setExpires(ReportUtil.getDateFromString(curEventExpires));
				schedulerEventDBItem.setJobChain(curEventJobChainName);
				schedulerEventDBItem.setJobName(curEventJobName);
				schedulerEventDBItem.setParameters(paramsString);
				schedulerEventDBItem.setOrderId(curEventOrderId);
				schedulerEventDBItem.setRemoteSchedulerHost(curEventRemoteSchedulerHost);
				if (!curEventRemoteSchedulerPort.isEmpty()) {
					schedulerEventDBItem.setRemoteSchedulerPort(Integer.valueOf(curEventRemoteSchedulerPort));
				}
				if (!curEventCreated.isEmpty()) {
					schedulerEventDBItem.setCreated(ReportUtil.getDateFromString(curEventCreated));
				} else {
					schedulerEventDBItem.setCreated(new Date());
				}

				this.schedulerEventDBLayer.insertItem(schedulerEventDBItem);
				this.session.commit();
			}
		} catch (Exception e) {
			if (this.schedulerEventDBLayer != null) {
				this.schedulerEventDBLayer.rollback();
			}
			sosLogger.error(e.getMessage());
			throw e;
		}
	}

	private void removeEvents(final NodeList events1) throws Exception {
		try {
			for (int i = 0; i < events1.getLength(); i++) {
				if (events1.item(i) == null || events1.item(i).getNodeType() != Node.ELEMENT_NODE) {
					continue;
				}
				String xquery = "//event[";
				String and = "";
				Element event = (Element) events1.item(i);
				String curEventClass = event.getAttribute("event_class");
				if (curEventClass != null
						&& curEventClass.equalsIgnoreCase(JobSchedulerConstants.EVENT_CLASS_ALL_EVENTS)) {
					removeAllEvents();
					return;
				}
				boolean hasAttributes = false;
				NamedNodeMap eventAttributeList = event.getAttributes();
				for (int j = 0; j < eventAttributeList.getLength(); j++) {
					Node eventAttribute = eventAttributeList.item(j);
					if (eventAttribute == null || "event_name".equalsIgnoreCase(eventAttribute.getNodeName())) {
						continue;
					}
					String value = eventAttribute.getNodeValue();
					if (!"event_title".equals(eventAttribute.getNodeName()) && value != null && !value.isEmpty()) {
						hasAttributes = true;
						xquery += and + "@" + eventAttribute.getNodeName() + "='" + eventAttribute.getNodeValue() + "'";
						and = " and ";
					}
				}
				xquery += "]";
				if (!hasAttributes) {
					sosLogger.warn("current event has no attributes. Removal (of all elements) will not be performed.");
					continue;
				}
				sosLogger.debug9("xquery to remove events: " + xquery);
				NodeList nodes = XPathAPI.selectNodeList(this.getEvents(), xquery);
				for (int j = 0; j < nodes.getLength(); j++) {
					if (nodes.item(j) == null) {
						continue;
					} else if (nodes.item(j).getNodeType() == Node.ELEMENT_NODE) {
						this.removeEvent(nodes.item(j));
					}
				}
			}
		} catch (Exception e) {
			sosLogger.error(e.getMessage());
			throw e;
		}
	}

	private void removeAllEvents() throws Exception {
		try {
			sosLogger
					.info("event class is: " + JobSchedulerConstants.EVENT_CLASS_ALL_EVENTS + ". Removing all events.");
			Document eventDocument = getEvents();
			eventDocument.removeChild(eventDocument.getFirstChild());
			eventDocument.appendChild(eventDocument.createElement("events"));
			if (this.schedulerEventDBLayer != null) {
				this.schedulerEventDBLayer.beginTransaction();
				schedulerEventDBLayer.resetFilter();
				this.schedulerEventDBLayer.delete();
				this.schedulerEventDBLayer.commit();
			}
			this.setEventClass("");
		} catch (Exception e) {
			sosLogger.error("Error removing all events: " + e);
			throw e;
		}
	}

	private void removeEvent() throws Exception {
		try {
			sosLogger.debug9(
					".. constructing event: eventClass=" + this.getEventClass() + ", eventId=" + this.getEventId());
			Element event = this.getEvents().createElement("event");
			event.setAttribute("event_class", this.getEventClass());
			event.setAttribute("event_id", this.getEventId());
			event.setAttribute("scheduler_id", this.getEventSchedulerId());
			event.setAttribute("remote_scheduler_host", this.getEventRemoteSchedulerHost());
			if (!"0".equalsIgnoreCase(this.getEventRemoteSchedulerPort())) {
				event.setAttribute("remote_scheduler_port", this.getEventRemoteSchedulerPort());
			}
			event.setAttribute("job_chain", this.getEventJobChainName());
			event.setAttribute("order_id", this.getEventOrderId());
			event.setAttribute("job_name", this.getEventJobName());
			if (!this.getEventExitCode().isEmpty()) {
				event.setAttribute("exit_code", this.getEventExitCode());
			}
			Element dummyParent = getEvents().createElement("events");
			dummyParent.appendChild(event);
			this.removeEvents(event.getParentNode().getChildNodes());

		} catch (Exception e) {
			sosLogger.error(e.getMessage());
			throw e;
		}
	}

	private void removeEventFromDatabase(Node event) throws Exception {
		if (this.schedulerEventDBLayer != null) {
			try {
				sosLogger.debug9("Remove event from database: " + this.getAttributeValue(event, "event_class") + ":"
						+ this.getAttributeValue(event, "event_id"));
				schedulerEventDBLayer.beginTransaction();
				schedulerEventDBLayer.resetFilter();
				schedulerEventDBLayer.getFilter().setSchedulerId(this.getAttributeValue(event, "scheduler_id"));
				schedulerEventDBLayer.getFilter().setJobChain(this.getAttributeValue(event, "job_chain"));
				schedulerEventDBLayer.getFilter().setOrderId(this.getAttributeValue(event, "order_id"));
				schedulerEventDBLayer.getFilter().setJobName(this.getAttributeValue(event, "job_name"));
				schedulerEventDBLayer.getFilter().setEventClass(this.getAttributeValue(event, "event_class"));
				schedulerEventDBLayer.getFilter().setEventId(this.getAttributeValue(event, "event_id"));
				try {
					schedulerEventDBLayer.getFilter()
							.setExitCode(Integer.parseInt(this.getAttributeValue(event, "exit_code")));
				} catch (NumberFormatException e) {

				}
				schedulerEventDBLayer.delete();
				schedulerEventDBLayer.commit();
			} catch (Exception e) {
				if (this.schedulerEventDBLayer != null) {
					this.schedulerEventDBLayer.rollback();
				}
				sosLogger.error(e.getMessage());
				throw e;
			}
		}
	}

	private void removeExpiredEventsFromDatabase() throws Exception {
		if (this.schedulerEventDBLayer != null) {
			try {
				sosLogger.debug9("Remove expired events from database:");
				schedulerEventDBLayer.beginTransaction();
				schedulerEventDBLayer.resetFilter();
				Date d = new Date();
				schedulerEventDBLayer.getFilter().setExpiresTo(d);
				int row = schedulerEventDBLayer.delete();
				sosLogger.debug9(row + " on " + d + " expired events removed");
				schedulerEventDBLayer.commit();
			} catch (Exception e) {
				if (this.schedulerEventDBLayer != null) {
					this.schedulerEventDBLayer.rollback();
				}
				sosLogger.error(e.getMessage());
				throw e;
			}
		}
	}

	private void removeEvent(final Node event) throws Exception {
		try {
			String curEventSchedulerId = this.getAttributeValue(event, "scheduler_id");
			String curEventClass = this.getAttributeValue(event, "event_class");
			String curEventId = this.getAttributeValue(event, "event_id");
			String curEventExitCode = this.getAttributeValue(event, "exit_code");
			String curEventJobChainName = this.getAttributeValue(event, "job_chain");
			String curEventOrderId = this.getAttributeValue(event, "order_id");
			String curEventJobName = this.getAttributeValue(event, "job_name");
			sosLogger.info(".. removing event ...: scheduler id=" + curEventSchedulerId + ", event class="
					+ curEventClass + ", event id=" + curEventId + ", exit code=" + curEventExitCode + ", job chain="
					+ curEventJobChainName + ", order id=" + curEventOrderId + ", job=" + curEventJobName);
			Node nEvents = this.getEvents().getFirstChild();
			sosLogger.debug9("Events Name: " + nEvents.getLocalName());
			sosLogger.debug9("Events size: " + nEvents.getChildNodes().getLength());
			this.getEvents().getFirstChild().removeChild(event);
			sosLogger.debug9("Events size: " + nEvents.getChildNodes().getLength());

			removeEventFromDatabase(event);

		} catch (Exception e) {
			sosLogger.error(e.getMessage());
			throw new Exception(e);
		}
	}

	private String getAttributeValue(final Node node, final String namedItem) throws Exception {
		try {
			if (node.getAttributes() == null || node.getAttributes().getLength() == 0) {
				return "";
			}
			if (node.getAttributes().getNamedItem(namedItem) == null) {
				return "";
			}
			return node.getAttributes().getNamedItem(namedItem).getNodeValue();
		} catch (Exception e) {
			sosLogger.error("error occurred reading attribute value: " + e.getMessage());
			throw e;
		}
	}

	private String xmlNodeToString(final Node node) throws Exception {
		try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			Document document = docBuilder.newDocument();
			document.appendChild(document.importNode(node, true));
			StringWriter out = new StringWriter();
			XMLSerializer serializer = new XMLSerializer(out, new OutputFormat(document));
			serializer.serialize(document);
			return out.toString();
		} catch (Exception e) {
			sosLogger.error("error occurred transforming node: " + e.getMessage());
			throw e;
		}
	}

	private String xmlDocumentToString(final Document document) throws Exception {
		try {
			StringWriter out = new StringWriter();
			XMLSerializer serializer = new XMLSerializer(out, new OutputFormat(document));
			serializer.serialize(document);
			return out.toString();
		} catch (Exception e) {
			sosLogger.error("error occurred transforming document: " + e.getMessage());
			throw e;
		}
	}

	private String xmlElementToString(final Element element) throws Exception {
		try {
			StringWriter out = new StringWriter();
			XMLSerializer serializer = new XMLSerializer(out, new OutputFormat());
			serializer.serialize(element);
			return out.toString();
		} catch (Exception e) {
			sosLogger.error("error occurred transforming document: " + e.getMessage());
			throw e;
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
			sosLogger.error(
					"error occurred reading content of file [" + file.getCanonicalPath() + "]: " + e.getMessage());
			throw e;
		} finally {
			if (in != null) {
				in.close();
				in = null;
			}
		}
	}

	private Variable_set getParameters() {
		return parameters;
	}

	private void setParameters(final Variable_set parameters) {
		this.parameters = parameters;
	}

	private String getEventClass() {
		return eventClass;
	}

	private void setEventClass(final String eventClass) {
		this.eventClass = eventClass;
	}

	private String getEventExpires() {
		return eventExpires;
	}

	private void setEventExpires(final String eventExpires) {
		this.eventExpires = eventExpires;
	}

	private String getEventId() {
		return eventId;
	}

	private void setEventId(final String eventId) {
		this.eventId = eventId;
	}

	private Element getEventParameters() {
		return eventParameters;
	}

	private void setEventParameters(final Element eventParameters) {
		this.eventParameters = eventParameters;
	}

	private String getEventAction() {
		return eventAction;
	}

	private void setEventAction(final String eventAction) throws Exception {
		if (!"add".equalsIgnoreCase(eventAction) && !"remove".equalsIgnoreCase(eventAction)
				&& !"process".equalsIgnoreCase(eventAction)) {
			throw new Exception("invalid action specified [add, remove, process]: " + eventAction);
		}
		this.eventAction = eventAction;
	}

	private Document getEvents() {
		return events;
	}

	private void setEvents(final Document events) {
		this.events = events;
	}

	private String getEventSchedulerId() {
		return eventSchedulerId;
	}

	private void setEventSchedulerId(final String eventSchedulerId) {
		this.eventSchedulerId = eventSchedulerId;
	}

	private String getExpirationPeriod() {
		return expirationPeriod;
	}

	private void setExpirationPeriod(final String expirationPeriod) {
		this.expirationPeriod = expirationPeriod;
	}

	private String getEventJobChainName() {
		return eventJobChainName;
	}

	private void setEventJobChainName(final String eventJobChainName) {
		this.eventJobChainName = eventJobChainName;
	}

	private String getEventJobName() {
		return eventJobName;
	}

	private void setEventJobName(final String eventJobName) {
		this.eventJobName = eventJobName;
	}

	private String getEventHandlerFilepath() {
		return eventHandlerFilepath;
	}

	private void setEventHandlerFilepath(final String eventHandlerFilepath) {
		this.eventHandlerFilepath = eventHandlerFilepath;
	}

	private String getEventHandlerFilespec() {
		return eventHandlerFilespec;
	}

	private void setEventHandlerFilespec(final String eventHandlerFilespec) {
		this.eventHandlerFilespec = eventHandlerFilespec;
	}

	private Collection<File> getEventHandlerFileList() {
		return eventHandlerFileList;
	}

	private void setEventHandlerFileList(final Collection<File> eventHandlerFileList) {
		this.eventHandlerFileList = eventHandlerFileList;
	}

	private Iterator<File> getEventHandlerFileListIterator() {
		return eventHandlerFileListIterator;
	}

	private void setEventHandlerFileListIterator(final Iterator<File> eventHandlerFileListIterator) {
		this.eventHandlerFileListIterator = eventHandlerFileListIterator;
	}

	private Collection<Object> getEventHandlerResultFileList() {
		return eventHandlerResultFileList;
	}

	private void setEventHandlerResultFileList(final Collection<Object> eventHandlerResultFileList) {
		this.eventHandlerResultFileList = eventHandlerResultFileList;
	}

	private Calendar getExpirationDate() {
		return expirationDate;
	}

	private void setExpirationDate(final Calendar expirationDate) {
		this.expirationDate = expirationDate;
	}

	private Iterator<Object> getEventHandlerResultFileListIterator() {
		return eventHandlerResultFileListIterator;
	}

	private void setEventHandlerResultFileListIterator(final Iterator<Object> eventHandlerResultFileListIterator) {
		this.eventHandlerResultFileListIterator = eventHandlerResultFileListIterator;
	}

	private String getEventRemoteSchedulerHost() {
		return eventRemoteSchedulerHost;
	}

	private void setEventRemoteSchedulerHost(final String eventRemoteSchedulerHost) {
		this.eventRemoteSchedulerHost = eventRemoteSchedulerHost;
	}

	private String getEventRemoteSchedulerPort() {
		return eventRemoteSchedulerPort;
	}

	private void setEventRemoteSchedulerPort(final String eventRemoteSchedulerPort) {
		this.eventRemoteSchedulerPort = eventRemoteSchedulerPort;
	}

	private String getEventExitCode() {
		return eventExitCode;
	}

	private void setEventExitCode(final String eventExitCode) {
		this.eventExitCode = eventExitCode;
	}

	private String getEventOrderId() {
		return eventOrderId;
	}

	private void setEventOrderId(final String eventOrderId) {
		this.eventOrderId = eventOrderId;
	}

	private String getEventCreated() {
		return eventCreated;
	}

	private void setEventCreated(final String eventCreated) {
		this.eventCreated = eventCreated;
	}

	private String getExpirationCycle() {
		return expirationCycle;
	}

	private void setExpirationCycle(final String expirationCycle) {
		this.expirationCycle = expirationCycle;
	}

	private void setSockettimeout(final String s) {
		try {
			socket_timeout = Integer.parseInt(s);
		} catch (NumberFormatException e) {
			spooler_log.warn("Illegal value for parameter socket_timeout:" + s + ". Integer expected. Using default=5");
			socket_timeout = 5;
		}
	}

}