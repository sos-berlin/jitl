package com.sos.jitl.messaging;

import sos.scheduler.job.JobSchedulerJobAdapter;
import com.sos.jitl.messaging.options.MessageConsumerOptions;

import com.sos.JSHelper.Exceptions.JobSchedulerException;


public class MessageConsumerJobJSAdapter extends JobSchedulerJobAdapter{
    private String targetJobChains;
    private String delimiter;
    @Override
    public boolean spooler_process() throws Exception {
        
        MessageConsumerJob job = new MessageConsumerJob();
        try {
            super.spooler_process();
            MessageConsumerOptions options = job.getOptions();
            options.setAllOptions(getSchedulerParameterAsProperties(getParameters()));
            targetJobChains = options.getTargetJobChainName().Value();
            delimiter = options.getParamKeyValueDelimiter().Value();
            job.setJSJobUtilites(this);
            job.setJSCommands(this);
            job.execute();
            if(options.getExecuteXml().value()){
                executeXmlForAllTargets(job.getMessageXml());
            }
        } catch (Exception e) {
            throw new JobSchedulerException("Error occured in spooler_process of MessageConsumerJob: ", e);
        }
        return signalSuccess();
    }

    private void executeXml(String message) {
        spooler_log.debug9("execute XML started");
        String answer = spooler.execute_xml(message);
        spooler_log.debug9("Return value of executeXML: " + answer);
        spooler_log.debug9("order send");
    }
    
    private void executeXmlForAllTargets(String message) {
        if (targetJobChains.contains(delimiter) && message.contains("add_order")) {
            String [] jobChainNames = targetJobChains.split("[" + delimiter + "]");
            for (String name : jobChainNames){
                spooler_log.debug9("add_order XML will be adjusted for JobChain: " + name);
                executeXml(message.replaceFirst("job_chain='[^']*'", "job_chain='" + name + "'"));
            }            
        }else{
            executeXml(message);
        }

    }

}
