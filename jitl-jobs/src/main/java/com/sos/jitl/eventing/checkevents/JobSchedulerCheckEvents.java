package com.sos.jitl.eventing.checkevents;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.Basics.JSJobUtilities;
import com.sos.JSHelper.Basics.JSJobUtilitiesClass;
import com.sos.hibernate.classes.SOSHibernateFactory;
import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.jitl.eventing.db.SchedulerEventDBLayer;
import com.sos.jitl.eventing.db.SchedulerEventFilter;
import com.sos.jitl.reporting.db.DBLayer;

public class JobSchedulerCheckEvents extends JSJobUtilitiesClass<JobSchedulerCheckEventsOptions>
		implements JSJobUtilities {

	protected boolean exist = false;
	private static final Logger LOGGER = LoggerFactory.getLogger(JobSchedulerCheckEventsJSAdapterClass.class);

	public JobSchedulerCheckEvents() {
		super();
	}

	@Override
	public JobSchedulerCheckEventsOptions getOptions() {
		if (objOptions == null) {
			objOptions = new JobSchedulerCheckEventsOptions();
		}
		return objOptions;
	}


	private SOSHibernateSession getSession(String confFile) throws Exception {
		SOSHibernateFactory sosHibernateFactory = new SOSHibernateFactory(confFile);
		sosHibernateFactory.addClassMapping(DBLayer.getReportingClassMapping());
		sosHibernateFactory.build();
		return sosHibernateFactory.openStatelessSession();
	}
	
	public JobSchedulerCheckEvents Execute() throws Exception {
		getOptions().checkMandatory();
		LOGGER.debug(getOptions().toString());
		exist = false;
		SchedulerEventDBLayer schedulerEventDBLayer = new SchedulerEventDBLayer(getSession(objOptions.configuration_file.getValue()));
		schedulerEventDBLayer.beginTransaction();
		if (objOptions.event_condition.isDirty()) {
			if (objOptions.event_class.isDirty()) {
				exist = schedulerEventDBLayer.checkEventExists(objOptions.event_condition.getValue(),
						objOptions.event_class.getValue());
			} else {
				exist = schedulerEventDBLayer.checkEventExists(objOptions.event_condition.getValue());
			}
		} else {
			SchedulerEventFilter schedulerEventFilter = new SchedulerEventFilter();
			schedulerEventFilter.setEventClass(objOptions.event_class.getValue());
			schedulerEventFilter.setEventId(objOptions.event_id.getValue());
			if (objOptions.event_exit_code.getValue() != null && !objOptions.event_exit_code.getValue().isEmpty()) {
				schedulerEventFilter.setExitCode(Integer.parseInt(objOptions.event_exit_code.getValue()));
			}
			schedulerEventFilter.setSchedulerId(objOptions.event_scheduler_id.getValue());
			schedulerEventFilter.setRemoteSchedulerHost(objOptions.remote_scheduler_host.getValue());
			schedulerEventFilter.setRemoteSchedulerPort(objOptions.remote_scheduler_port.getValue());
			schedulerEventFilter.setJobChain(objOptions.event_job_chain.getValue());
			schedulerEventFilter.setOrderId(objOptions.event_order_id.getValue());
			schedulerEventFilter.setJobName(objOptions.event_job.getValue());
			exist = schedulerEventDBLayer.checkEventExists(schedulerEventFilter);
		}
		schedulerEventDBLayer.rollback();

		return this;
	}

	public void init() {
		doInitialize();
	}

	private void doInitialize() {
		// doInitialize
	}

	@Override
	public String replaceSchedulerVars(final String pstrString2Modify) {
		LOGGER.debug("replaceSchedulerVars as Dummy-call executed. No Instance of JobUtilites specified.");
		return pstrString2Modify;
	}

	@Override
	public void setJSParam(final String pstrKey, final String pstrValue) {
	}

	@Override
	public void setJSParam(final String pstrKey, final StringBuffer pstrValue) {
	}

	@Override
	public void setJSJobUtilites(final JSJobUtilities pobjJSJobUtilities) {
		if (pobjJSJobUtilities == null) {
			objJSJobUtilities = this;
		} else {
			objJSJobUtilities = pobjJSJobUtilities;
		}
		LOGGER.debug("objJSJobUtilities = " + objJSJobUtilities.getClass().getName());
	}

	@Override
	public String getCurrentNodeName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setStateText(final String pstrStateText) {
		// TODO Auto-generated method stub
	}

	@Override
	public void setCC(final int pintCC) {
		// TODO Auto-generated method stub
	}

	@Override
	public void setNextNodeState(String pstrNodeName) {
		// TODO Auto-generated method stub

	}

}