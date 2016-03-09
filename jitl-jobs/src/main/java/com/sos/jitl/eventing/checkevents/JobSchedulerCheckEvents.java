package com.sos.jitl.eventing.checkevents;

import java.io.File;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Basics.JSJobUtilities;
import com.sos.JSHelper.Basics.JSJobUtilitiesClass;
import com.sos.eventing.db.SchedulerEventDBLayer;
import com.sos.eventing.db.SchedulerEventFilter;

public class JobSchedulerCheckEvents extends JSJobUtilitiesClass<JobSchedulerCheckEventsOptions> implements JSJobUtilities {

    protected boolean exist = false;
    private static final Logger LOGGER = Logger.getLogger(JobSchedulerCheckEvents.class);

    public JobSchedulerCheckEvents() {
        super();
    }

    @Override
    public JobSchedulerCheckEventsOptions Options() {
        if (objOptions == null) {
            objOptions = new JobSchedulerCheckEventsOptions();
        }
        return objOptions;
    }

    public JobSchedulerCheckEvents Execute() throws Exception {
        try {
            Options().CheckMandatory();
            LOGGER.debug(Options().toString());
            exist = false;
            SchedulerEventDBLayer schedulerEventDBLayer = new SchedulerEventDBLayer(new File(objOptions.configuration_file.Value()));
            if (objOptions.event_condition.isDirty()) {
                if (objOptions.event_class.isDirty()) {
                    exist = schedulerEventDBLayer.checkEventExists(objOptions.event_condition.Value(), objOptions.event_class.Value());
                } else {
                    exist = schedulerEventDBLayer.checkEventExists(objOptions.event_condition.Value());
                }
            } else {
                SchedulerEventFilter schedulerEventFilter = new SchedulerEventFilter();
                schedulerEventFilter.setEventClass(objOptions.event_class.Value());
                schedulerEventFilter.setEventId(objOptions.event_id.Value());
                schedulerEventFilter.setExitCode(objOptions.event_exit_code.Value());
                schedulerEventFilter.setSchedulerId(objOptions.event_scheduler_id.Value());
                schedulerEventFilter.setRemoteSchedulerHost(objOptions.remote_scheduler_host.Value());
                schedulerEventFilter.setRemoteSchedulerPort(objOptions.remote_scheduler_port.Value());
                schedulerEventFilter.setJobChain(objOptions.event_job_chain.Value());
                schedulerEventFilter.setOrderId(objOptions.event_order_id.Value());
                schedulerEventFilter.setJobName(objOptions.event_job.Value());
                exist = schedulerEventDBLayer.checkEventExists(schedulerEventFilter);
            }
        } catch (Exception e) {
            throw e;
        } finally {
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
    public String myReplaceAll(final String pstrSourceString, final String pstrReplaceWhat, final String pstrReplaceWith) {
        String newReplacement = pstrReplaceWith.replaceAll("\\$", "\\\\\\$");
        return pstrSourceString.replaceAll("(?m)" + pstrReplaceWhat, newReplacement);
    }

    @Override
    public String replaceSchedulerVars(final boolean isWindows, final String pstrString2Modify) {
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