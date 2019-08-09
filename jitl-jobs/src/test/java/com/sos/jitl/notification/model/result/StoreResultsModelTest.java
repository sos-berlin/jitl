package com.sos.jitl.notification.model.result;

import org.apache.log4j.Logger;

import com.sos.jitl.notification.jobs.result.StoreResultsJobOptions;
import com.sos.jitl.notification.model.ConfigTest;
import com.sos.jitl.notification.model.ModelTest;

public class StoreResultsModelTest extends ModelTest {

    private static Logger LOGGER = Logger.getLogger(StoreResultsModelTest.class);

    public static void main(String[] args) throws Exception {
        StoreResultsModelTest t = new StoreResultsModelTest();

        StoreResultsJobOptions opt = new StoreResultsJobOptions();
        opt.scheduler_notification_hibernate_configuration_file.setValue(ConfigTest.HIBERNATE_CONFIGURATION_FILE);
        opt.mon_results_scheduler_id.setValue("my_scheduler_id");
        opt.mon_results_task_id.value(17600149);
        opt.mon_results_order_step_state.setValue("moveCSV");
        opt.mon_results_job_chain_name.setValue("orders_setback/Move");
        opt.mon_results_order_id.setValue("Get");
        opt.mon_results_standalone.value(false);

        try {
            LOGGER.info("START --");

            t.init(opt.scheduler_notification_hibernate_configuration_file.getValue());
            StoreResultsModel model = new StoreResultsModel(t.getSession(), opt);
            model.process();

            LOGGER.info("END --");
        } catch (Exception ex) {
            throw ex;
        } finally {
            t.exit();
        }
    }
}
