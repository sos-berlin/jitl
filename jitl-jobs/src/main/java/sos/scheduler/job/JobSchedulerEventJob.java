package sos.scheduler.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Vector;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import com.sos.jitl.eventing.eventhandler.EventCommandsExecuter;
import com.sos.jitl.eventing.eventhandler.XmlEventHandler;
import com.sos.jitl.eventing.eventhandler.XsltEventHandler;
import sos.scheduler.command.SOSSchedulerCommand;
import sos.spooler.Variable_set;
import sos.util.SOSDate;

import com.sos.hibernate.classes.SOSHibernateFactory;
import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.jitl.eventing.db.SchedulerEventDBItem;
import com.sos.jitl.eventing.db.SchedulerEventDBLayer;
import com.sos.jitl.eventing.db.SchedulerEventFilter;
import com.sos.jitl.reporting.db.DBLayer;
import com.sos.jitl.reporting.helper.ReportUtil;

public class JobSchedulerEventJob extends JobSchedulerJobAdapter {

	private static final String DEFAULT_EXPIRATION_PERIOD = "24:00";
	private static final String EXPIRES_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
	private static final Logger LOGGER = LoggerFactory.getLogger(JobSchedulerEventJob.class);

	private String confFile;
	private SOSHibernateSession session;
	private SchedulerEventDBLayer schedulerEventDBLayer;
	private SchedulerEventFilter filter;
	private String eventAction = "";
	private String eventHandlerFilepath = "";
	private String eventHandlerFilespec = "";
	private Variable_set parameters = null;
	private Collection<Object> eventHandlerResultedCommands = new Vector<Object>();
	private int socketTimeout = 5;
	private List<SchedulerEventDBItem> listOfEvents;
	private int httpPort;
	private Element eventParameters;

	@Override
	public boolean spooler_init() {

		try {
			httpPort = SOSSchedulerCommand.getHTTPPortFromScheduler(spooler);
		} catch (Exception e) {
			getLogger().debug3("could not read http port from scheduler.xml");
		}

		boolean rc = super.spooler_init();

		confFile = "";

		if (spooler_task.params() != null) {
			confFile = spooler_task.params().value("configuration_file");
		}
		if (spooler_job.order_queue() != null) {
			confFile = spooler_task.order().params().value("configuration_file");
		}

		if (confFile != null && !confFile.isEmpty()) {
			LOGGER.debug("configuration_file from param");
		} else {
			LOGGER.debug("configuration_file from scheduler");
			confFile = getHibernateConfigurationReporting().toFile().getAbsolutePath();
		}

		try {
			session = getSession(confFile);
			schedulerEventDBLayer = new SchedulerEventDBLayer(session);
			filter = new SchedulerEventFilter();

		} catch (Exception e) {
			LOGGER.error("Could not create session: " + e.getMessage());
			throw new RuntimeException(e);
		}

		return rc;
	}

	@Override
	public boolean spooler_process() throws Exception {
		super.spooler_process();
		boolean rc = true;
		try {

			this.parameters = spooler.create_variable_set();

			HashSet<String> jobParameterNames = new HashSet<String>();
			try {
				if (spooler_task.params() != null) {
					this.parameters.merge(spooler_task.params());
				}
				if (spooler_job.order_queue() != null) {
					this.parameters.merge(spooler_task.order().params());
				}

				if (this.parameters.value("socket_timeout") != null
						&& !this.parameters.value("socket_timeout").isEmpty()) {
					this.setSockettimeout(this.parameters.value("socket_timeout"));
					LOGGER.debug(".. parameter [socket_timeout]: " + this.parameters.value("socket_timeout"));
				} else {
					this.setSockettimeout("5");
				}
				if (this.parameters.value("action") != null && !this.parameters.value("action").isEmpty()) {
					this.setEventAction(this.parameters.value("action"));
					LOGGER.debug(".. parameter [action]: " + this.parameters.value("action"));
				} else {
					this.setEventAction("add");
				}
				jobParameterNames.add("action");
				if (this.parameters.value("scheduler_id") != null && !this.parameters.value("scheduler_id").isEmpty()) {
					filter.setSchedulerId(this.parameters.value("scheduler_id"));
					LOGGER.debug(".. parameter [scheduler_id]: " + this.parameters.value("scheduler_id"));
				} else {
					filter.setSchedulerId(spooler.id());
				}
				jobParameterNames.add("scheduler_id");
				if (this.parameters.value("spooler_id") != null && !this.parameters.value("spooler_id").isEmpty()) {
					filter.setSchedulerId(this.parameters.value("spooler_id"));
					LOGGER.debug(".. parameter [spooler_id]: " + this.parameters.value("spooler_id"));
				}
				jobParameterNames.add("spooler_id");

				if (this.parameters.value("remote_scheduler_host") != null
						&& !this.parameters.value("remote_scheduler_host").isEmpty()) {
					filter.setRemoteSchedulerHost(this.parameters.value("remote_scheduler_host"));
					LOGGER.debug(
							".. parameter [remote_scheduler_host]: " + this.parameters.value("remote_scheduler_host"));
				}
				jobParameterNames.add("remote_scheduler_host");

				if (this.parameters.value("remote_scheduler_port") != null
						&& !this.parameters.value("remote_scheduler_port").isEmpty()) {
					filter.setRemoteSchedulerPort(this.parameters.value("remote_scheduler_port"));
					LOGGER.debug(
							".. parameter [remote_scheduler_port]: " + this.parameters.value("remote_scheduler_port"));
				}
				jobParameterNames.add("remote_scheduler_port");

				if (this.parameters.value("job_chain") != null && !this.parameters.value("job_chain").isEmpty()) {
					filter.setJobChain(this.parameters.value("job_chain"));
					LOGGER.debug(".. parameter [job_chain]: " + this.parameters.value("job_chain"));
				}
				jobParameterNames.add("job_chain");

				if (this.parameters.value("order_id") != null && !this.parameters.value("order_id").isEmpty()) {
					filter.setOrderId(this.parameters.value("order_id"));
					LOGGER.debug(".. parameter [order_id]: " + this.parameters.value("order_id"));
				}
				jobParameterNames.add("order_id");

				if (this.parameters.value("job_name") != null && !this.parameters.value("job_name").isEmpty()) {
					filter.setJobName(this.parameters.value("job_name"));
					LOGGER.debug(".. parameter [job_name]: " + this.parameters.value("job_name"));
				}
				jobParameterNames.add("job_name");

				if (this.parameters.value("event_class") != null && !this.parameters.value("event_class").isEmpty()) {
					filter.setEventClass(this.parameters.value("event_class"));
					LOGGER.debug(".. parameter [event_class]: " + this.parameters.value("event_class"));
				}
				jobParameterNames.add("event_class");

				if (this.parameters.value("event_id") != null && !this.parameters.value("event_id").isEmpty()) {
					filter.setEventId(this.parameters.value("event_id"));
					LOGGER.debug(".. parameter [event_id]: " + this.parameters.value("event_id"));
				}
				jobParameterNames.add("event_id");

				if (this.parameters.value("exit_code") != null && !this.parameters.value("exit_code").isEmpty()) {
					filter.setExitCode(this.parameters.value("exit_code"));
					LOGGER.debug(".. parameter [exit_code]: " + this.parameters.value("exit_code"));
				}
				jobParameterNames.add("exit_code");

				if (this.parameters.value("expires") != null && !this.parameters.value("expires").isEmpty()) {
					filter.setExpires(this.parameters.value("expires"));

					LOGGER.debug(".. parameter [expires]: " + this.parameters.value("expires"));
					LOGGER.debug(".. --> eventExpires:" + filter.getExpires());
				}
				jobParameterNames.add("expires");

				if (this.parameters.value("created") != null && !this.parameters.value("created").isEmpty()) {
					filter.setCreated(ReportUtil.getDateFromString(this.parameters.value("created")));
					LOGGER.debug(".. parameter [created]: " + this.parameters.value("created"));
				}
				jobParameterNames.add("created");

				if (this.parameters.value("expiration_period") != null
						&& !this.parameters.value("expiration_period").isEmpty()) {
					filter.setExpirationPeriod(this.parameters.value("expiration_period"));
					LOGGER.debug(".. parameter [expiration_period]: " + this.parameters.value("expiration_period"));
				} else {
					filter.setExpirationPeriod(DEFAULT_EXPIRATION_PERIOD);
				}
				jobParameterNames.add("expiration_period");

				if (this.parameters.value("expiration_cycle") != null
						&& !this.parameters.value("expiration_cycle").isEmpty()) {
					filter.setExpirationCycle(this.parameters.value("expiration_cycle"));
					LOGGER.debug(".. parameter [expiration_cycle]: " + this.parameters.value("expiration_cycle"));
				} else {
					filter.setExpirationCycle(this.parameters.value(""));
				}
				jobParameterNames.add("expiration_cycle");

				if (this.parameters.value("event_handler_filepath") != null
						&& !this.parameters.value("event_handler_filepath").isEmpty()) {
					this.eventHandlerFilepath = this.parameters.value("event_handler_filepath");
					LOGGER.debug(".. parameter [event_handler_filepath]: "
							+ this.parameters.value("event_handler_filepath"));
				} else {
					this.eventHandlerFilepath = "./config/events";
				}
				jobParameterNames.add("event_handler_filepath");
				if (this.parameters.value("event_handler_filespec") != null
						&& !this.parameters.value("event_handler_filespec").isEmpty()) {
					this.eventHandlerFilespec = this.parameters.value("event_handler_filespec");
					LOGGER.debug(".. parameter [event_handler_filespec]: "
							+ this.parameters.value("event_handler_filespec"));
				} else {
					this.eventHandlerFilespec = "\\.sos.scheduler.xsl$";
				}
				jobParameterNames.add("event_handler_filespec");
				DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
				Document eventDocument = docBuilder.newDocument();
				this.eventParameters = eventDocument.createElement("params");
				String[] parameterNames = this.parameters.names().split(";");
				for (int i = 0; i < parameterNames.length; i++) {
					if (!jobParameterNames.contains(parameterNames[i])) {
						Element param = eventDocument.createElement("param");
						param.setAttribute("name", parameterNames[i]);
						param.setAttribute("value", parameters.value(parameterNames[i]));
						if (parameterNames[i].contains("password")) {
							getLogger().debug3("Event parameter [" + parameterNames[i] + "]: *****");
						} else {
							getLogger().debug3("Event parameter [" + parameterNames[i] + "]: "
									+ parameters.value(parameterNames[i]));
						}
						this.eventParameters.appendChild(param);
					}
				}
			} catch (Exception e) {
				LOGGER.error("error occurred processing parameters: " + e.getMessage());
				throw e;
			}

			try {
				if ("add".equalsIgnoreCase(this.eventAction)) {
					LOGGER.info("adding event: " + filter.getEventClass() + " " + filter.getEventId());
					this.addEvent();
				} else if ("remove".equalsIgnoreCase(this.eventAction)) {
					LOGGER.info("removing event: " + filter.getEventClass() + " " + filter.getEventId());
					this.removeEvent();
				} else {
					LOGGER.info("processing events");
				}
				this.getSchedulerEvents();
				this.processSchedulerEvents();
			} catch (Exception e) {
				e.printStackTrace();
				throw new Exception("error occurred processing event: " + e.getMessage());
			}
			return spooler_job.order_queue() != null ? rc : false;
		} catch (Exception e) {
			e.printStackTrace();
			if (filter.getEventId() == null) {
				LOGGER.warn("error occurred processing event handlers" + e.getMessage(), e);
			} else {
				LOGGER.warn("error occurred processing event [" + filter.getEventClass() + " " + filter.getEventId()
						+ "]: " + e.getMessage(), e);
			}
			return false;
		}
	}

 

	private void getSchedulerEvents() throws Exception {
		removeExpiredEventsFromDatabase();
		try {
			LOGGER.debug("readEventsFromDB");
			if (this.schedulerEventDBLayer != null) {
				schedulerEventDBLayer.getSession().beginTransaction();
				SchedulerEventFilter filter = new SchedulerEventFilter();
				filter.setSchedulerId(spooler.id());
				filter.setSchedulerIdEmpty(true);
				listOfEvents = schedulerEventDBLayer.getSchedulerEventList(filter);
				LOGGER.debug(String.format("%s items found in database", listOfEvents.size()));
				schedulerEventDBLayer.getSession().commit();
			}
		} catch (Exception e) {
			LOGGER.error("Failed to read events from database: " + e.getMessage());
			throw e;
		}

	}

	private SOSHibernateSession getSession(String confFile) throws Exception {
		SOSHibernateFactory sosHibernateFactory = new SOSHibernateFactory(confFile);
		sosHibernateFactory.addClassMapping(DBLayer.getReportingClassMapping());
		sosHibernateFactory.build();
		return sosHibernateFactory.openStatelessSession();
	}

	private void processSchedulerEvents() throws Exception {

 		eventHandlerResultedCommands = new LinkedHashSet<Object>();
		XmlEventHandler xmlEventHandler = new XmlEventHandler(eventHandlerResultedCommands, listOfEvents,
				eventHandlerFilepath, filter.getJobChain(), filter.getEventClass());
		xmlEventHandler.getListOfCommands();

		XsltEventHandler xsltEventHandler = new XsltEventHandler(eventHandlerResultedCommands,
				schedulerEventDBLayer.getEventsAsXml(filter.getSchedulerId(), this.listOfEvents), eventHandlerFilepath,
				this.eventHandlerFilespec, filter.getJobName(), filter.getJobChain(), filter.getEventClass(),
				filter.getExpirationDate());
		xsltEventHandler.getListOfCommands();

		EventCommandsExecuter commandsExecuter = new EventCommandsExecuter(eventHandlerResultedCommands, listOfEvents,
				spooler.hostname(), this.httpPort, this.socketTimeout);

		commandsExecuter.executeCommands();
		addEvents(commandsExecuter.getListOfAddEvents());
		removeEvents(commandsExecuter.getListOfRemoveEvents());
	}

	private Date getDefaultExpires() throws Exception {
		SchedulerEventFilter filter = new SchedulerEventFilter();
		filter.setExpirationPeriod(DEFAULT_EXPIRATION_PERIOD);
		filter.setExpirationCycle("");
		return filter.getExpires();
	}

	private void addEvents(List<SchedulerEventDBItem> listOfEvents) throws Exception {
		try {

			schedulerEventDBLayer.beginTransaction();
			for (SchedulerEventDBItem item : listOfEvents) {
				SchedulerEventFilter filter = new SchedulerEventFilter();
				filter.setEventClass(item.getEventClass());
				filter.setEventId(item.getEventId());
				filter.setExitCode(item.getExitCode());
				if (item.getExpires() == null) {
					filter.setExpires(getDefaultExpires());
				}else {
					filter.setExpires(item.getExpires());
				}
				schedulerEventDBLayer.addEvent(filter);
			}
			schedulerEventDBLayer.commit();
			if (listOfEvents.size() > 0) {
				spooler.execute_xml(schedulerEventDBLayer.getNotifyCommand());
			}

		} catch (Exception e) {
			if (this.schedulerEventDBLayer != null) {
				this.schedulerEventDBLayer.rollback();
			}
			LOGGER.error(e.getMessage());
			throw e;
		}
	}

	private void removeEvents(List<SchedulerEventDBItem> listOfEvents) throws Exception {
		try {

			schedulerEventDBLayer.beginTransaction();
			for (SchedulerEventDBItem item : listOfEvents) {
				SchedulerEventFilter removeFilter = new SchedulerEventFilter();
				item.setSchedulerId(filter.getSchedulerId());
				removeFilter.setEventClass(item.getEventClass());
				removeFilter.setEventId(item.getEventId());
				removeFilter.setExitCode(item.getExitCode());
				removeFilter.setJobChain(item.getJobChain());
				removeFilter.setJobName(item.getJobName());
				removeFilter.setOrderId(item.getOrderId());
				removeFilter.setSchedulerId(item.getSchedulerId());
				if (item.getSchedulerId() == null) {
					removeFilter.setSchedulerId(filter.getSchedulerId());
				} else {
					removeFilter.setSchedulerId(item.getSchedulerId());
				}

				schedulerEventDBLayer.delete(removeFilter);
			}
			schedulerEventDBLayer.commit();
			if (listOfEvents.size() > 0) {
				spooler.execute_xml(schedulerEventDBLayer.getNotifyCommand());
			}

		} catch (Exception e) {
			if (this.schedulerEventDBLayer != null) {
				this.schedulerEventDBLayer.rollback();
			}
			LOGGER.error(e.getMessage());
			throw e;
		}
	}

	private void addEvent() throws Exception {
		try {

			schedulerEventDBLayer.beginTransaction();
			schedulerEventDBLayer.addEvent(filter);
			schedulerEventDBLayer.commit();
			spooler.execute_xml(schedulerEventDBLayer.getNotifyCommand());

		} catch (Exception e) {
			if (this.schedulerEventDBLayer != null) {
				this.schedulerEventDBLayer.rollback();
			}
			LOGGER.error(e.getMessage());
			throw e;
		}
	}

	private void removeEvent() throws Exception {
		try {
			LOGGER.debug("Remove event from database: " + filter.getEventClass() + ":" + filter.getEventId());
			schedulerEventDBLayer.beginTransaction();
			schedulerEventDBLayer.removeEvent(filter);
			schedulerEventDBLayer.commit();
			spooler.execute_xml(schedulerEventDBLayer.getNotifyCommand());
		} catch (Exception e) {
			if (this.schedulerEventDBLayer != null) {
				this.schedulerEventDBLayer.rollback();
			}
			LOGGER.error(e.getMessage(), e);
			throw e;
		}
	}

	private void removeExpiredEventsFromDatabase() throws Exception {
		if (this.schedulerEventDBLayer != null) {
			try {
				SchedulerEventFilter filter = new SchedulerEventFilter();
				LOGGER.debug("Remove expired events from database:");
				schedulerEventDBLayer.beginTransaction();

				filter.setExpires("now_utc");
				int row = schedulerEventDBLayer.delete(filter);
				LOGGER.debug(row + " on " + filter.getExpires() + " expired events removed");
				schedulerEventDBLayer.commit();
			} catch (Exception e) {
				if (this.schedulerEventDBLayer != null) {
					this.schedulerEventDBLayer.rollback();
				}
				LOGGER.error(e.getMessage());
				throw e;
			}
		}
	}

	private void setEventAction(final String eventAction) throws Exception {
		if (!"add".equalsIgnoreCase(eventAction) && !"remove".equalsIgnoreCase(eventAction)
				&& !"process".equalsIgnoreCase(eventAction)) {
			throw new Exception("invalid action specified [add, remove, process]: " + eventAction);
		}
		this.eventAction = eventAction;
	}

	private void setSockettimeout(final String s) {
		try {
			socketTimeout = Integer.parseInt(s);
		} catch (NumberFormatException e) {
			spooler_log.warn("Illegal value for parameter socket_timeout:" + s + ". Integer expected. Using default=5");
			socketTimeout = 5;
		}
	}

}