package com.sos.jitl.reporting.job.report;

import sos.scheduler.job.JobSchedulerJobAdapter;
import sos.xml.SOSXMLXPath;
import org.w3c.dom.Element;

import com.sos.JSHelper.Exceptions.JobSchedulerException;

public class AggregationJobJSAdapterClass extends JobSchedulerJobAdapter {
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
    
    @Override
    public boolean spooler_process() throws Exception {
        AggregationJob job = new AggregationJob();
        try {
            super.spooler_process();

            job = new AggregationJob();
            AggregationJobOptions options = job.getOptions();
            options.setCurrentNodeName(this.getCurrentNodeName());
            options.setAllOptions(getSchedulerParameterAsProperties(getParameters()));
            job.setJSJobUtilites(this);
            job.setJSCommands(this);
            options.current_scheduler_http_port.setValue(String.valueOf(this.schedulerHttpPort));
                        
            job.init();
            job.execute();
        } catch (Exception e) {
            throw new JobSchedulerException("Fatal Error:" + e.getMessage(), e);
        } finally {
            job.exit();
        }
        return signalSuccess();

    }

}
