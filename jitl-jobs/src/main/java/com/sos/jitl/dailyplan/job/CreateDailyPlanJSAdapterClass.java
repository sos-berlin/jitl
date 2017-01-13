package com.sos.jitl.dailyplan.job;

import java.io.File;

import org.apache.log4j.Logger;

import sos.scheduler.job.JobSchedulerJobAdapter;
import sos.spooler.Spooler;

public class CreateDailyPlanJSAdapterClass extends JobSchedulerJobAdapter {

    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 4444;
    private static final String CLASSNAME = "CreateDailyScheduleJSAdapterClass";
    private static final Logger LOGGER = Logger.getLogger(CreateDailyPlanJSAdapterClass.class);

    @Override
    public boolean spooler_init() {
        final String conMethodName = CLASSNAME + "::spooler_init";
        LOGGER.debug(String.format(Messages.getMsg("JSJ-I-110"), conMethodName));
        return super.spooler_init();
    }

    @Override
    public boolean spooler_process() throws Exception {
        final String conMethodName = CLASSNAME + "::spooler_process";
        LOGGER.debug(String.format(Messages.getMsg("JSJ-I-110"), conMethodName));
        try {
            super.spooler_process();
            doProcessing();
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error(e.getMessage(), e);
            LOGGER.debug("Exception:" + e.getMessage(), e);
            return false;
        }
        return spooler_task.job().order_queue() != null;
    }

    private void doProcessing() throws Exception {
        final String conMethodName = CLASSNAME + "::doProcessing";
        LOGGER.debug(String.format(Messages.getMsg("JSJ-I-110"), conMethodName));
        CreateDailyPlan createDailyPlan = new CreateDailyPlan();
        CreateDailyPlanOptions createDailyPlanOptions = createDailyPlan.getOptions();
        createDailyPlanOptions.setAllOptions(getSchedulerParameterAsProperties());
        Spooler spooler = (Spooler) getSpoolerObject();
        createDailyPlanOptions.SchedulerHostName.isMandatory(true);
        createDailyPlanOptions.scheduler_port.isMandatory(true);
        int port = DEFAULT_PORT;
        String host = DEFAULT_HOST;
        if (createDailyPlanOptions.getItem("SchedulerTcpPortNumber") != null) {
            LOGGER.debug("port from param");
            port = createDailyPlanOptions.SchedulerTcpPortNumber.value();
        } else {
            LOGGER.debug("port from scheduler");
            port = spooler.tcp_port();
        }
        if (createDailyPlanOptions.getItem("SchedulerHostName") != null) {
            host = createDailyPlanOptions.SchedulerHostName.getValue();
        } else {
            host = spooler.hostname();
        }
        
        String configuration_file = "";
        if (createDailyPlanOptions.getItem("configuration_file") != null) {
            LOGGER.debug("configuration_file from param");
            configuration_file = createDailyPlanOptions.configuration_file.getValue();
        } else {
            LOGGER.debug("configuration_file from scheduler");
            File f = new File(new File(spooler.configuration_directory()).getParent(), "hibernate.cfg.xml");
            if (!f.exists()) {
                f = new File(new File(spooler.directory()), "config/hibernate.cfg.xml");
            }
            configuration_file = f.getAbsolutePath();
        }
        createDailyPlanOptions.configuration_file.setValue(configuration_file);
        createDailyPlanOptions.SchedulerHostName.setValue(host);
        createDailyPlanOptions.scheduler_port.value(port);
        createDailyPlanOptions.checkMandatory();
        createDailyPlan.setJSJobUtilites(this);
        createDailyPlan.setSchedulerId(spooler.id());
        createDailyPlan.setSpooler(spooler);
        createDailyPlan.Execute();
    }

}
