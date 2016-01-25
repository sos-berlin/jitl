package com.sos.jitl.messaging;

import java.util.HashMap;

import sos.scheduler.job.JobSchedulerJobAdapter;
import com.sos.jitl.messaging.options.MessageProducerOptions;

import com.sos.JSHelper.Exceptions.JobSchedulerException;


public class MessageProducerJobJSAdapter extends JobSchedulerJobAdapter{

    @Override
    public boolean spooler_process() throws Exception {
        MessageProducerJob job = new MessageProducerJob();
        try {
            super.spooler_process();
            MessageProducerOptions options = job.getOptions();
            HashMap<String, String> map = getSchedulerParameterAsProperties(getParameters());
            job.setJSJobUtilites(this);
            options.setAllOptions(map);
            job.setAllParams(map);
            job.execute();
        } catch (Exception e) {
            throw new JobSchedulerException("Fatal Error in MessageProducerJob:" + e.getMessage(), e);
        }
        return signalSuccess();
    }

}
