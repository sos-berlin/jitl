package com.sos.jitl.reporting.job.report;

import org.w3c.dom.Element;

import com.sos.JSHelper.Exceptions.JobSchedulerException;

import sos.scheduler.job.JobSchedulerJobAdapter;
import sos.util.SOSString;
import sos.xml.SOSXMLXPath;

public class AggregationJobJSAdapterClass extends JobSchedulerJobAdapter {

    private AggregationJob job;

    @Override
    public boolean spooler_init() {
        try {
            String answerXml = spooler.execute_xml(
                    "<show_state subsystems=\"folder\" what=\"folders cluster no_subfolders\" path=\"/any/path/that/does/not/exists\" />");
            SOSXMLXPath xPath = new SOSXMLXPath(new StringBuffer(answerXml));
            Element stateElement = (Element) xPath.selectSingleNode("/spooler/answer/state");
            if (stateElement == null) {
                throw new Exception(String.format("\"state\" element not found. answerXml = %s", answerXml));
            }

            job = new AggregationJob();
            AggregationJobOptions options = job.getOptions();
            options.setAllOptions(getSchedulerParameterAsProperties(spooler_task.order()));
            job.setJSJobUtilites(this);
            job.setJSCommands(this);
            options.current_scheduler_http_port.setValue(String.valueOf(Integer.parseInt(stateElement.getAttribute("http_port"))));
            options.current_scheduler_id.setValue(spooler.id());

            if (SOSString.isEmpty(options.hibernate_configuration_file.getValue())) {
                options.hibernate_configuration_file.setValue(getHibernateConfigurationReporting().toString());
            }
            job.init();
        } catch (Exception e) {
            throw new JobSchedulerException("Fatal Error:" + e.getMessage(), e);
        }
        return super.spooler_init();
    }

    @Override
    public boolean spooler_process() throws Exception {
        try {
            super.spooler_process();

            AggregationJobOptions options = job.getOptions();
            options.setCurrentNodeName(getCurrentNodeName(getSpoolerProcess().getOrder(), true));
            options.setAllOptions(getSchedulerParameterAsProperties(getSpoolerProcess().getOrder()));

            job.openSession();
            job.execute();

            return getSpoolerProcess().getSuccess();
        } catch (Exception e) {
            throw new JobSchedulerException("Fatal Error:" + e.getMessage(), e);
        } finally {
            job.closeSession();
        }
    }

    @Override
    public void spooler_close() throws Exception {
        if (job != null) {
            job.exit();
        }

        super.spooler_close();
    }

}
