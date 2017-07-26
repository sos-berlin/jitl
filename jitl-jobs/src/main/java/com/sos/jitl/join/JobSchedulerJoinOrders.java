package com.sos.jitl.join;

import com.sos.jitl.join.JobSchedulerJoinOrders;
import com.sos.jitl.join.JobSchedulerJoinOrdersOptions;
import com.sos.JSHelper.Basics.JSJobUtilitiesClass;
import com.sos.scheduler.messages.JSMessages;
import com.sos.JSHelper.Basics.JSJobUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JobSchedulerJoinOrders extends JSJobUtilitiesClass<JobSchedulerJoinOrdersOptions> {

    protected JobSchedulerJoinOrdersOptions jobSchedulerJoinOrdersOptions = null;
    private static final String CLASSNAME = "JobSchedulerJoinOrders";
    private static final Logger LOGGER = LoggerFactory.getLogger(JobSchedulerJoinOrders.class);
    private JSJobUtilities objJSJobUtilities = this;
    private JoinOrder joinOrder;
    private String joinOrderListString;
    private boolean resumeAllOrders;
    private JoinSerializer joinSerializer;
    
    public JobSchedulerJoinOrders() {
        super(new JobSchedulerJoinOrdersOptions());
    }

    public JobSchedulerJoinOrdersOptions getOptions() {
        if (jobSchedulerJoinOrdersOptions == null) {
            jobSchedulerJoinOrdersOptions = new JobSchedulerJoinOrdersOptions();
        }
        return jobSchedulerJoinOrdersOptions;
    }

    public JobSchedulerJoinOrdersOptions getOptions(final JobSchedulerJoinOrdersOptions pobjOptions) {
        jobSchedulerJoinOrdersOptions = pobjOptions;
        return jobSchedulerJoinOrdersOptions;
    }

    public JobSchedulerJoinOrders execute() throws Exception {
        final String METHODNAME = CLASSNAME + "::execute";
        LOGGER.debug(String.format(JSMessages.JSJ_I_110.get(), METHODNAME));
        try {
            getOptions().checkMandatory();
            LOGGER.debug(getOptions().toString());
            joinSerializer = new JoinSerializer(joinOrderListString);
            LOGGER.debug(String.format("adding orders %s", joinOrder.getOrderId()));
            joinSerializer.addOrder(joinOrder);
            LOGGER.debug(String.format("%s orders found", joinSerializer.getJoinOrderList().size(joinOrder)));
            resumeAllOrders = (getOptions().required_orders.value() <= joinSerializer.getJoinOrderList().size(joinOrder));
            LOGGER.debug(String.format("%s required -- %s found --> %s", getOptions().required_orders.value(),joinSerializer.getJoinOrderList().size(joinOrder),resumeAllOrders));

        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            LOGGER.error(String.format(JSMessages.JSJ_F_107.get(), METHODNAME), e);
            throw e;
        } finally {
            LOGGER.debug(String.format(JSMessages.JSJ_I_111.get(), METHODNAME));
        }
        return this;
    }

    @Override
    public String replaceSchedulerVars(String pstrString2Modify) {
        LOGGER.debug("replaceSchedulerVars as Dummy-call executed. No Instance of JobUtilites specified.");
        return pstrString2Modify;
    }

    public void setJSJobUtilites(JSJobUtilities pobjJSJobUtilities) {
        if (pobjJSJobUtilities == null) {
            objJSJobUtilities = this;
        } else {
            objJSJobUtilities = pobjJSJobUtilities;
        }
        LOGGER.debug("objJSJobUtilities = " + objJSJobUtilities.getClass().getName());
    }

    public JoinOrder getJoinOrder() {
        return joinOrder;
    }

    public void setJoinOrder(JoinOrder joinOrder) {
        this.joinOrder = joinOrder;
    }

    public void setJoinOrderListString(String joinOrderListString) {
        this.joinOrderListString = joinOrderListString;
    }

    
    public boolean isResumeAllOrders() {
        return resumeAllOrders;
    }

    
    public JoinSerializer getJoinSerializer() {
        return joinSerializer;
    }

    public JoinOrder getMainOrder(JoinOrder joinOrder) {
        return joinSerializer.getJoinOrderList().getMainOrder(joinOrder);
    }

}
