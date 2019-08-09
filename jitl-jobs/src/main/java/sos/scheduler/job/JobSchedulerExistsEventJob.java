package sos.scheduler.job;

import org.apache.xpath.XPathAPI;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import sos.spooler.Variable_set;
import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.hibernate.classes.SOSHibernateFactory;
import com.sos.hibernate.classes.SOSHibernateSession;
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

			configurationFile = getHibernateConfigurationReporting().toFile().getAbsolutePath();
			Document eventDocument = readEventsFromDB();

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

	public Document readEventsFromDB() throws Exception {
		SchedulerEventDBLayer schedulerEventDBLayer = new SchedulerEventDBLayer(getSession(configurationFile));
		if (filterSchedulerId) {
			return schedulerEventDBLayer.getEventsAsXml(scheduler_event_service_id);
		} else {
			return schedulerEventDBLayer.getEventsAsXml("");
		}
	}

	public void setConfigurationFile(String configurationFile) {
		this.configurationFile = configurationFile;
	}

}