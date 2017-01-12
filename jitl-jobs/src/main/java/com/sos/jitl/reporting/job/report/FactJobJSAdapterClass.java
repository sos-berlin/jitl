package com.sos.jitl.reporting.job.report;

import sos.scheduler.job.JobSchedulerJobAdapter;
import sos.spooler.Order;
import sos.spooler.Variable_set;
import sos.util.SOSString;
import sos.xml.SOSXMLXPath;

import org.w3c.dom.Element;

import com.sos.JSHelper.Exceptions.JobSchedulerException;

public class FactJobJSAdapterClass extends JobSchedulerJobAdapter {
	private int schedulerHttpPort;
    
    @Override
    public boolean spooler_init(){
       try {
           String answerXml = spooler.execute_xml("<show_state subsystems=\"folder\" what=\"folders cluster no_subfolders\" path=\"/any/path/that/does/not/exists\" />");
           SOSXMLXPath xPath = new SOSXMLXPath(new StringBuffer(answerXml));
           Element stateElement = (Element)xPath.selectSingleNode("/spooler/answer/state");
           if(stateElement == null){
               throw new Exception(String.format("\"state\" element not found. answerXml = %s",answerXml));
           }
           this.schedulerHttpPort = Integer.parseInt(stateElement.getAttribute("http_port"));
           
       } catch (Exception e) {
           throw new JobSchedulerException("Fatal Error:" + e.getMessage(), e);
       }
       return super.spooler_init();
    }
    
    public void setVariable(String name, String value) throws Exception {
        Order order = spooler_task.order();
        Variable_set params = spooler.create_variable_set();
        params.merge(spooler_task.params());
        params.merge(order.params());
        order.params().set_var(name, value);
    }

    @Override
    public boolean spooler_process() throws Exception {
        FactJob job = new FactJob();
        try {
            super.spooler_process();
            FactJobOptions options = job.getOptions();
            options.setCurrentNodeName(this.getCurrentNodeName());
            options.setAllOptions(getSchedulerParameterAsProperties(getParameters()));
            job.setJSJobUtilites(this);
            job.setJSCommands(this);
            
            if (SOSString.isEmpty(options.current_scheduler_id.getValue())) {
                options.current_scheduler_id.setValue(spooler.id());
            }
            options.current_scheduler_http_port.setValue(String.valueOf(this.schedulerHttpPort));
            
            job.init();
            job.execute();
            if (job.getModel().getCounterOrderSync().getTriggers() > 0 
            	|| job.getModel().getCounterOrderSyncUncompleted().getTriggers() > 0
            	|| job.getModel().getCounterStandaloneSync().getExecutions() > 0
            	|| job.getModel().getCounterStandaloneSyncUncompleted().getExecutions()>0) {
                setVariable(AggregationJobOptions.VARIABLE_EXECUTE_AGGREGATION, "true");
            } else {
                setVariable(AggregationJobOptions.VARIABLE_EXECUTE_AGGREGATION, "false");
            }
        } catch (Exception e) {
            throw new JobSchedulerException("Fatal Error:" + e.getMessage(), e);
        } finally {
            job.exit();
        }
        return signalSuccess();
    }

}