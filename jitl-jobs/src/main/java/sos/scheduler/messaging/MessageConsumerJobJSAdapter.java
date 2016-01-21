package sos.scheduler.messaging;

import sos.scheduler.job.JobSchedulerJobAdapter;
import sos.scheduler.messaging.options.MessageConsumerOptions;

import com.sos.JSHelper.Exceptions.JobSchedulerException;


public class MessageConsumerJobJSAdapter extends JobSchedulerJobAdapter{

    @Override
    public boolean spooler_process() throws Exception {
        
        MessageConsumerJob job = new MessageConsumerJob();
        try {
            super.spooler_process();
            MessageConsumerOptions options = job.getOptions();
            options.setAllOptions(getSchedulerParameterAsProperties(getParameters()));
            job.setJSJobUtilites(this);
            job.setJSCommands(this);
            job.execute();
            String message = job.getMessageXml();
            if(message.contains("add_order")){
                executeXml(message);
            }
        } catch (Exception e) {
            throw new JobSchedulerException("Fatal Error in MessageConsumerJob: ", e);
        }
        return signalSuccess();
    }

    private void executeXml(String message) {
        spooler_log.debug9("execute XML started");
        String answer = spooler.execute_xml(message);
        spooler_log.debug9("Return value of executeXML: " + answer);
        spooler_log.debug9("order send");
    }

}
