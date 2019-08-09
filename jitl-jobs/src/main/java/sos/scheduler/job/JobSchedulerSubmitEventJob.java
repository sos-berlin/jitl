package sos.scheduler.job;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import com.sos.JSHelper.Exceptions.JobSchedulerException;

import sos.scheduler.command.SOSSchedulerCommand;
import sos.spooler.Job;
import sos.spooler.Log;
import sos.spooler.Spooler;
import sos.spooler.Supervisor_client;
import sos.spooler.Task;
import sos.spooler.Variable_set;
import sos.util.SOSDate;
import sos.xml.SOSXMLXPath;

public class JobSchedulerSubmitEventJob extends JobSchedulerJobAdapter {

	private static final String PARAM_SCHEDULER_DEL_EVENTS = "del_events";
	private static final Logger LOGGER = Logger.getLogger(JobSchedulerSubmitEventJob.class);
	private static final String PARAM_SCHEDULER_EVENT_HANDLER_PORT = "scheduler_event_handler_port";
	private static final String PARAM_SCHEDULER_EVENT_HANDLER_HOST = "scheduler_event_handler_host";
	private static final String PARAM_SCHEDULER_EVENT_EXPIRATION_PERIOD = "scheduler_event_expiration_period";
	private static final String PARAM_SCHEDULER_EVENT_EXPIRATION_CYCLE = "scheduler_event_expiration_cycle";
	private static final String PARAM_SCHEDULER_EVENT_EXPIRES = "scheduler_event_expires";
	private static final String PARAM_SCHEDULER_EVENT_EXIT_CODE = "scheduler_event_exit_code";
	private static final String PARAM_SCHEDULER_EVENT_JOB = "scheduler_event_job";
	private static final String PARAM_SCHEDULER_EVENT_JOB_CHAIN = "scheduler_event_job_chain";
	private static final String PARAM_SUPERVISOR_JOB_CHAIN = "supervisor_job_chain";
	private static final String PARAM_SCHEDULER_EVENT_ID = "scheduler_event_id";
	private static final String PARAM_SCHEDULER_EVENT_CLASS = "scheduler_event_class";
	private static final String ACTION_REMOVE = "remove";
	private static final String PARAMETER_SCHEDULER_EVENT_ACTION = "scheduler_event_action";
	private static Log logger;

	@Override
	public boolean spooler_process() {
		try {
			processEvent(spooler, spooler_job, spooler_task, spooler_log);
		} catch (Exception e) {
			LOGGER.error("Error occured in event job: " + e.getMessage(), e);
			spooler_log.warn("Error occured in event job: " + e);
			return signalFailure();
		}
		return signalSuccess();
	}

	protected static void processEvent(final Spooler spooler, final Job spooler_job, final Task spooler_task,
			final Log spooler_log) throws Exception {
		logger = spooler_log;
		boolean orderJob = !(spooler_task.job().order_queue() == null);
		String supervisorJobChain = "/sos/events/scheduler_event_service";
		String jobChain = "";
		String orderId = "";
		File jobPath = new File(spooler_job.name());
		String jobName = jobPath.getName();
		String schedulerHost = spooler.hostname();
		String schedulerHTTPPort = "" + SOSSchedulerCommand.getHTTPPortFromScheduler(spooler);
		String eventHandlerHost = "";
		int eventHandlerHTTPPort = 0;
		String action = "add";
		List<Element> eventParameters = new ArrayList<Element>();
		String expires = "";
		String expCycle = "";
		String expPeriod = "";
		String exitCode = "" + spooler_task.exit_code();
		String eventClass = "";
		String eventId = "" + spooler_task.id();
		Variable_set parameters = spooler.create_variable_set();
		parameters.merge(spooler_task.params());
		if (orderJob) {
			jobChain = spooler_task.order().job_chain().name();
			orderId = spooler_task.order().id();
			parameters.merge(spooler_task.order().params());
			eventId = orderId;
		}
		try {
			HashSet<String> parameterNames = new HashSet<String>();
			spooler_log.debug1("reading parameters:");
			eventClass = parameters.var(PARAM_SCHEDULER_EVENT_CLASS);
			if (eventClass == null || eventClass.isEmpty()) {
				throw new JobSchedulerException("Parameter scheduler_event_class is missing.");
			}
			if (parameters.var(PARAMETER_SCHEDULER_EVENT_ACTION) != null
					&& !parameters.var(PARAMETER_SCHEDULER_EVENT_ACTION).isEmpty()) {
				action = parameters.var(PARAMETER_SCHEDULER_EVENT_ACTION);
				spooler_log.debug1("...parameter[scheduler_event_action]: " + action);
				parameterNames.add(PARAMETER_SCHEDULER_EVENT_ACTION);
			}
			if (ACTION_REMOVE.equalsIgnoreCase(action)) {
				orderId = "";
				jobChain = "";
				jobName = "";
				schedulerHost = "";
				schedulerHTTPPort = "";
				exitCode = "";
				eventId = "";

				if (parameters.var("scheduler_event_host") != null
						&& !parameters.var("scheduler_event_host").isEmpty()) {
					schedulerHost = parameters.var("scheduler_event_host");
					spooler_log.debug1("...parameter[scheduler_event_host]: " + schedulerHost);
					parameterNames.add("scheduler_event_host");
				}
				if (parameters.var("scheduler_event_port") != null
						&& !parameters.var("scheduler_event_port").isEmpty()) {
					schedulerHTTPPort = parameters.var("scheduler_event_port");
					spooler_log.debug1("...parameter[scheduler_event_port]: " + schedulerHTTPPort);
					parameterNames.add("scheduler_event_port");
				}
				if (parameters.var(PARAM_SCHEDULER_EVENT_EXIT_CODE) != null
						&& !parameters.var(PARAM_SCHEDULER_EVENT_EXIT_CODE).isEmpty()) {
					exitCode = parameters.var(PARAM_SCHEDULER_EVENT_EXIT_CODE);
					spooler_log.debug1("...parameter[scheduler_event_exit_code]: " + exitCode);
					parameterNames.add(PARAM_SCHEDULER_EVENT_EXIT_CODE);
				}
			}
			parameterNames.add(PARAM_SCHEDULER_EVENT_CLASS);
			spooler_log.debug1("...parameter[scheduler_event_class]: " + eventClass);

			if (parameters.var(PARAM_SCHEDULER_EVENT_JOB) != null
					&& !parameters.var(PARAM_SCHEDULER_EVENT_JOB).isEmpty()) {
				jobName = parameters.var(PARAM_SCHEDULER_EVENT_JOB);
				spooler_log.debug1("...parameter[scheduler_event_job]: " + jobName);
				parameterNames.add(PARAM_SCHEDULER_EVENT_JOB);
			}

			if (parameters.var(PARAM_SCHEDULER_EVENT_JOB_CHAIN) != null
					&& !parameters.var(PARAM_SCHEDULER_EVENT_JOB_CHAIN).isEmpty()) {
				jobChain = parameters.var(PARAM_SCHEDULER_EVENT_JOB_CHAIN);
				spooler_log.debug1("...parameter[scheduler_event_job_chain]: " + jobChain);
				parameterNames.add(PARAM_SCHEDULER_EVENT_JOB_CHAIN);
			}

			if (parameters.var(PARAM_SCHEDULER_EVENT_ID) != null
					&& !parameters.var(PARAM_SCHEDULER_EVENT_ID).isEmpty()) {
				eventId = parameters.var(PARAM_SCHEDULER_EVENT_ID);
				spooler_log.debug1("...parameter[scheduler_event_id]: " + eventId);
				parameterNames.add(PARAM_SCHEDULER_EVENT_ID);
			}
			if (parameters.var(PARAM_SUPERVISOR_JOB_CHAIN) != null
					&& !parameters.var(PARAM_SUPERVISOR_JOB_CHAIN).isEmpty()) {
				supervisorJobChain = parameters.var(PARAM_SUPERVISOR_JOB_CHAIN);
				spooler_log.debug1("...parameter[supervisor_job_chain]: " + supervisorJobChain);
				parameterNames.add(PARAM_SUPERVISOR_JOB_CHAIN);
			}
			if (parameters.var(PARAM_SCHEDULER_EVENT_EXPIRES) != null
					&& !parameters.var(PARAM_SCHEDULER_EVENT_EXPIRES).isEmpty()) {
				expires = parameters.var(PARAM_SCHEDULER_EVENT_EXPIRES);
				spooler_log.debug1("...parameter[scheduler_event_expires]: " + expires);
				parameterNames.add(PARAM_SCHEDULER_EVENT_EXPIRES);
			}
			if (parameters.var(PARAM_SCHEDULER_EVENT_EXPIRATION_CYCLE) != null
					&& !parameters.var(PARAM_SCHEDULER_EVENT_EXPIRATION_CYCLE).isEmpty()) {
				expCycle = parameters.var(PARAM_SCHEDULER_EVENT_EXPIRATION_CYCLE);
				spooler_log.debug1("...parameter[scheduler_event_expiration_cycle]: " + expCycle);
				parameterNames.add(PARAM_SCHEDULER_EVENT_EXPIRATION_CYCLE);
			}
			if (parameters.var(PARAM_SCHEDULER_EVENT_EXPIRATION_PERIOD) != null
					&& !parameters.var(PARAM_SCHEDULER_EVENT_EXPIRATION_PERIOD).isEmpty()) {
				expPeriod = parameters.var(PARAM_SCHEDULER_EVENT_EXPIRATION_PERIOD);
				spooler_log.debug1("...parameter[scheduler_event_expiration_period]: " + expPeriod);
				parameterNames.add(PARAM_SCHEDULER_EVENT_EXPIRATION_PERIOD);
			}
			if (parameters.var(PARAM_SCHEDULER_EVENT_HANDLER_HOST) != null
					&& !parameters.var(PARAM_SCHEDULER_EVENT_HANDLER_HOST).isEmpty()) {
				eventHandlerHost = parameters.var(PARAM_SCHEDULER_EVENT_HANDLER_HOST);
				spooler_log.debug1("...parameter[scheduler_event_handler_host]: " + eventHandlerHost);
				parameterNames.add(PARAM_SCHEDULER_EVENT_HANDLER_HOST);
			}

			if (parameters.var(PARAM_SCHEDULER_EVENT_HANDLER_PORT) != null
					&& !parameters.var(PARAM_SCHEDULER_EVENT_HANDLER_PORT).isEmpty()) {
				eventHandlerHTTPPort = Integer.parseInt(parameters.var(PARAM_SCHEDULER_EVENT_HANDLER_PORT));
				spooler_log.debug1("...parameter[scheduler_event_handler_port]: " + eventHandlerHTTPPort);
				parameterNames.add(PARAM_SCHEDULER_EVENT_HANDLER_PORT);
			}

			// use all other parameters as event parameters:
			String[] paramNames = parameters.names().split(";");
			for (String paramName : paramNames) {
				if (!parameterNames.contains(paramName)) {
					String paramValue = parameters.var(paramName);
					spooler_log.debug1("...event parameter[" + paramName + "]: " + paramValue);
					eventParameters.add(DocumentHelper.createElement("param").addAttribute("name", paramName).addAttribute("value", paramValue));
				}
			}
		} catch (Exception e) {
			throw new JobSchedulerException("Error reading parameters: " + e, e);
		}
		try {
			String[] strA = eventId.split(";");
			for (String strEventID : strA) {
				String addOrder = createAddOrder(eventClass, strEventID, jobChain, orderId, jobName, schedulerHost,
						schedulerHTTPPort, action, expires, expCycle, expPeriod, exitCode, eventParameters,
						supervisorJobChain);
				submitToEventService(addOrder, spooler_log, spooler, eventHandlerHost, eventHandlerHTTPPort);
			}
			// Check for del_events
			if (parameters.var(PARAM_SCHEDULER_DEL_EVENTS) != null
					&& !parameters.var(PARAM_SCHEDULER_DEL_EVENTS).isEmpty()) {
				String strEvents2Delete = parameters.var(PARAM_SCHEDULER_DEL_EVENTS);
				strA = strEvents2Delete.split(";");
				action = ACTION_REMOVE;
				expires = "";
				for (String strEventID : strA) {
					String addOrder = createAddOrder(eventClass, strEventID, jobChain, orderId, jobName, schedulerHost,
							schedulerHTTPPort, action, expires, expCycle, expPeriod, exitCode, eventParameters,
							supervisorJobChain);
					submitToEventService(addOrder, spooler_log, spooler, eventHandlerHost, eventHandlerHTTPPort);
				}
			}
		} catch (Exception e) {
			throw new JobSchedulerException("Error submitting event order: " + e, e);
		}
	}

	private static String createAddOrder(final String eventClass, final String eventId, final String jobChain,
			final String orderId, final String jobName, final String schedulerHost, final String schedulerHTTPPort,
			final String action, final String expires, String expirationCycle, String expirationPeriod,
			final String exitCode, final List<Element> eventParameters, final String supervisorJobChain) throws Exception {
		try {
		    Element addOrderElement = DocumentHelper.createElement("add_order").addAttribute("job_chain", supervisorJobChain);
		    Document addOrderDocument = DocumentHelper.createDocument(addOrderElement);
		    addOrderDocument.setXMLEncoding("iso-8859-1");
		    Element paramsElement = addOrderElement.addElement("params");
			addParam(paramsElement, "action", action);
			addParam(paramsElement, "remote_scheduler_host", schedulerHost);
			addParam(paramsElement, "remote_scheduler_port", "" + schedulerHTTPPort);
			addParam(paramsElement, "job_chain", jobChain);
			addParam(paramsElement, "order_id", orderId);
			addParam(paramsElement, "job_name", jobName);
			addParam(paramsElement, "event_class", eventClass);
			addParam(paramsElement, "event_id", eventId);
			addParam(paramsElement, "exit_code", exitCode);
			addParam(paramsElement, "created", SOSDate.getCurrentTimeAsString());
			addParam(paramsElement, "expires", expires);
			addParam(paramsElement, "expiration_cycle", expirationCycle);
			addParam(paramsElement, "expiration_period", expirationPeriod);
			for (Element eventParam : eventParameters) {
			    paramsElement.add(eventParam);
			}
			String strOrdertxt = addOrderDocument.asXML();
			logger.debug(strOrdertxt);
			return strOrdertxt;
		} catch (Exception e) {
			throw new JobSchedulerException("Error creating add_order xml: " + e.getMessage(), e);
		}
	}

	private static void addParam(final Element paramsElement, final String name, final String value) {
		if (value != null && !value.isEmpty()) {
		    paramsElement.addElement("param").addAttribute("name", name).addAttribute("value", value);
		}
	}

	private static void submitToEventService(String xml, final Log spooler_log, final Spooler spooler,
			final String host, final int port) throws Exception {
		try {
			if (xml.indexOf("<?xml") == -1) {
				xml = "<?xml version=\"1.0\" encoding=\"iso-8859-1\"?>" + xml;
			}
			spooler_log.debug7("Sending xml:\n" + xml);
			Supervisor_client supervisor = null;
			try {
				supervisor = spooler.supervisor_client();
			} catch (Exception e) {
				// there is no supervisor
			}
			String answer;
			if (!host.isEmpty() && port != 0) {
				SOSSchedulerCommand schedulerCommand = new SOSSchedulerCommand();
				schedulerCommand.setHost(host);
				schedulerCommand.setPort(port);
				schedulerCommand.setProtocol("http");
				spooler_log.debug1(".. connecting to JobScheduler " + schedulerCommand.getHost() + ":"
						+ schedulerCommand.getPort());
				schedulerCommand.connect();
				schedulerCommand.sendRequest(xml);
				answer = schedulerCommand.getResponse();
			} else if (supervisor != null && supervisor.hostname() != null && !supervisor.hostname().isEmpty()) {
				SOSSchedulerCommand schedulerCommand = new SOSSchedulerCommand();
				schedulerCommand.setHost(supervisor.hostname());
				schedulerCommand.setPort(supervisor.tcp_port());
				schedulerCommand.setProtocol("http");
				spooler_log.debug1(".. connecting to JobScheduler " + schedulerCommand.getHost() + ":"
						+ schedulerCommand.getPort());
				schedulerCommand.connect();
				schedulerCommand.sendRequest(xml);
				answer = schedulerCommand.getResponse();
			} else {
				spooler_log.info("No supervisor configured, submitting event to this JobScheduler.");
				answer = spooler.execute_xml(xml);
			}
			SOSXMLXPath xAnswer = new SOSXMLXPath(new StringBuffer(answer));
			String errorText = xAnswer.selectSingleNodeValue("//ERROR/@text");
			if (errorText != null && !errorText.isEmpty()) {
				throw new JobSchedulerException("supervisor returned error: " + errorText);
			}
		} catch (Exception e) {
			throw new JobSchedulerException("Failed to submit event: " + e, e);
		}
	}

}