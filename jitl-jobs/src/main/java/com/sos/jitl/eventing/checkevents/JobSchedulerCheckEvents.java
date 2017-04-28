package com.sos.jitl.eventing.checkevents;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Basics.JSJobUtilities;
import com.sos.JSHelper.Basics.JSJobUtilitiesClass;
import com.sos.jitl.eventing.db.SchedulerEventDBLayer;
import com.sos.jitl.eventing.db.SchedulerEventFilter;

public class JobSchedulerCheckEvents extends JSJobUtilitiesClass<JobSchedulerCheckEventsOptions> implements JSJobUtilities {

    protected boolean exist = false;
    private static final Logger LOGGER = Logger.getLogger(JobSchedulerCheckEvents.class);

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

    public JobSchedulerCheckEvents Execute() throws Exception {
        try {
            getOptions().checkMandatory();
            LOGGER.debug(getOptions().toString());
            exist = false;
            SchedulerEventDBLayer schedulerEventDBLayer = new SchedulerEventDBLayer(objOptions.configuration_file.getValue());
            schedulerEventDBLayer.beginTransaction();
            if (objOptions.event_condition.isDirty()) {
                if (objOptions.event_class.isDirty()) {
                    exist = schedulerEventDBLayer.checkEventExists(objOptions.event_condition.getValue(), objOptions.event_class.getValue());
                } else {
                    exist = schedulerEventDBLayer.checkEventExists(objOptions.event_condition.getValue());
                }
            } else {
                SchedulerEventFilter schedulerEventFilter = new SchedulerEventFilter();
                schedulerEventFilter.setEventClass(objOptions.event_class.getValue());
                schedulerEventFilter.setEventId(objOptions.event_id.getValue());
                schedulerEventFilter.setExitCode(objOptions.event_exit_code.getValue());
                schedulerEventFilter.setSchedulerId(objOptions.event_scheduler_id.getValue());
                schedulerEventFilter.setRemoteSchedulerHost(objOptions.remote_scheduler_host.getValue());
                schedulerEventFilter.setRemoteSchedulerPort(objOptions.remote_scheduler_port.getValue());
                schedulerEventFilter.setJobChain(objOptions.event_job_chain.getValue());
                schedulerEventFilter.setOrderId(objOptions.event_order_id.getValue());
                schedulerEventFilter.setJobName(objOptions.event_job.getValue());
                exist = schedulerEventDBLayer.checkEventExists(schedulerEventFilter);
            }
            schedulerEventDBLayer.rollback();
        } catch (Exception e) {
            throw e;
        }
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