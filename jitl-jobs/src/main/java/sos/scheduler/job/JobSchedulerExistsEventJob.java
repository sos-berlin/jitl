package sos.scheduler.job;

import java.io.StringReader;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.xpath.XPathAPI;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import sos.connection.SOSConnection;
import sos.spooler.Spooler;
import sos.spooler.Variable_set;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.hibernate.classes.SOSHibernateFactory;
import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.jitl.eventing.db.SchedulerEventDBItem;
import com.sos.jitl.eventing.db.SchedulerEventDBLayer;
import com.sos.jitl.reporting.db.DBLayer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JobSchedulerExistsEventJob extends JobSchedulerJobAdapter {

	private static final Logger LOGGER = LoggerFactory.getLogger(JobSchedulerExistsEventJob.class);
	private String scheduler_event_service_id = "";
	private String configurationFile = "";
	private boolean filterSchedulerId = true;

	@Override
	public boolean spooler_process() throws Exception {
		super.spooler_process();
		boolean rc = true;
		try {
			// merge params
			Variable_set params = spooler.create_variable_set();
			if (spooler_task.params() != null) {
				params.merge(spooler_task.params());
			}
			if (spooler_job.order_queue() != null && spooler_task.order().params() != null) {
				params.merge(spooler_task.order().params());
			}
			String eventSpec = "";
			if (params.var("scheduler_event_spec") != null && !params.var("scheduler_event_spec").isEmpty()) {
				eventSpec = params.var("scheduler_event_spec");
			} else {
				throw new JobSchedulerException("parameter scheduler_event_spec is missing");
			}
			if (params.var("scheduler_event_service_id") != null
					&& !params.var("scheduler_event_service_id").isEmpty()) {
				scheduler_event_service_id = params.var("scheduler_event_service_id");
			} else {
				scheduler_event_service_id = spooler.id();
			}
			if (params.var("filter_scheduler_id") != null && !params.var("filter_scheduler_id").isEmpty()) {
				filterSchedulerId = "true".equalsIgnoreCase(params.var("filter_scheduler_id"));
			}
			LOGGER.debug(".. job parameter [scheduler_event_service_id]: " + scheduler_event_service_id);
			LOGGER.debug(".. job parameter [scheduler_event_spec]: " + eventSpec);
			LOGGER.debug("Checking events for: " + eventSpec);
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			Document eventDocument = docBuilder.newDocument();
			eventDocument.appendChild(eventDocument.createElement("events"));
			readEventsFromDB(getConnection(), spooler, eventDocument);
			NodeList nodes = XPathAPI.selectNodeList(eventDocument, eventSpec);
			if (nodes == null || nodes.getLength() == 0) {
				LOGGER.info("No matching events were found.");
				rc = false;
			} else {
				LOGGER.info("Matching events were found.");
				rc = true;
			}
		} catch (Exception e) {
			throw new JobSchedulerException("Error checking events: " + e, e);
		}
		return rc;
	}

	private SOSHibernateSession getSession(String confFile) throws Exception {
		SOSHibernateFactory sosHibernateFactory = new SOSHibernateFactory(confFile);
		sosHibernateFactory.addClassMapping(DBLayer.getReportingClassMapping());
		sosHibernateFactory.build();
		return sosHibernateFactory.openStatelessSession();
	}

	private void readEventsFromDB(final SOSConnection conn, final Spooler spooler, final Document eventsDoc)
			throws Exception {

		configurationFile = getHibernateConfigurationReporting().toFile().getAbsolutePath();

		SchedulerEventDBLayer schedulerEventDBLayer = new SchedulerEventDBLayer(getSession(configurationFile));
		schedulerEventDBLayer.beginTransaction();
		if (filterSchedulerId) {
			schedulerEventDBLayer.getFilter().setSchedulerIdEmpty(true);
			schedulerEventDBLayer.getFilter().setSchedulerId(scheduler_event_service_id);
		}
		schedulerEventDBLayer.getFilter().setExpires(new Date());
		schedulerEventDBLayer.delete();
		schedulerEventDBLayer.getFilter().setExpires(null);
		 
		List<SchedulerEventDBItem> eventList = schedulerEventDBLayer.getSchedulerEventList();
		schedulerEventDBLayer.commit();

		for (SchedulerEventDBItem eventItem : eventList) {
			Element event = eventsDoc.createElement("event");
			event.setAttribute("scheduler_id", eventItem.getSchedulerId());
			event.setAttribute("remote_scheduler_host", eventItem.getRemoteSchedulerHost());
			event.setAttribute("remote_scheduler_port", String.valueOf(eventItem.getRemoteSchedulerPort()));
			event.setAttribute("job_chain", eventItem.getJobChain());
			event.setAttribute("order_id", eventItem.getOrderId());
			event.setAttribute("job_name", eventItem.getJobName());
			event.setAttribute("event_class", eventItem.getEventClass());
			event.setAttribute("event_id", eventItem.getEventId());
			event.setAttribute("exit_code", eventItem.getExitCodeAsString());
			event.setAttribute("expires", eventItem.getExpiresAsString());
			event.setAttribute("created", eventItem.getCreatedAsString());
			if (eventItem.getParameters() != null && !eventItem.getParameters().isEmpty()) {
				DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
				Document eventParameters = docBuilder
						.parse(new InputSource(new StringReader(eventItem.getParameters())));
				LOGGER.debug("Importing params node...");
				Node impParameters = eventsDoc.importNode(eventParameters.getDocumentElement(), true);
				LOGGER.debug("appending params child...");
				event.appendChild(impParameters);
			}
			eventsDoc.getLastChild().appendChild(event);
		}
		LOGGER.info(eventList.size() + " events restored from database");
	}

}