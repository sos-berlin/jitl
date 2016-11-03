package com.sos.jitl.inventory.job;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import com.sos.JSHelper.Basics.JSToolBox;
import com.sos.JSHelper.Listener.JSListenerClass;
import com.sos.jitl.inventory.job.InitialInventoryUpdateJob;
import com.sos.jitl.inventory.job.InitialInventoryUpdateJobOptions;


public class InitialInventoryUpdateJobJUnitTest extends JSToolBox {

	private static final Logger	LOGGER = Logger.getLogger(InitialInventoryUpdateJobJUnitTest.class);
	private static final String SCHEDULER_HIBERNATE_CONFIGURATION = "C:/tmp/embedded_test/hibernate.cfg.xml";
    private static final String ANSWER_XML =
            "<?xml version='1.0' encoding='ISO-8859-1'?><spooler><answer time='2016-09-14T09:37:30.440Z'>"
            + "<state config_file='C:/sp/jobscheduler_1.10.6-SNAPSHOT/scheduler_4444/config/scheduler.xml' db='jdbc "
            + "-id=spooler -class=oracle.jdbc.driver.OracleDriver jdbc:oracle:thin:@//8of9:1521/test -user=scheduler' "
            + "host='SP' http_port='4404' id='scheduler_4444' "
            + "log_file='C:/sp/jobscheduler_1.10.6-SNAPSHOT/scheduler_4444/logs/scheduler-2016-09-09-063058.scheduler_4444.log' loop='12865' "
            + "pid='14552' spooler_id='scheduler_4444' spooler_running_since='2016-09-09T06:30:59Z' state='running' tcp_port='4444' "
            + "time='2016-09-14T09:37:30.441Z' time_zone='Europe/Berlin' udp_port='4444' version='1.10.6-SNAPSHOT' "
            + "version_commit_hash='35cf6b29f055a24bdaf0ed6ac44b26f077e9646f' wait_until='2016-09-14T09:48:23.238Z' waits='4549'>"
            + "<folder path='/'><file_based state='active'><requisites/></file_based><process_classes><process_class max_processes='30' "
            + "path='' processes='0'><file_based state='active'><requisites/></file_based></process_class><process_class max_processes='10' "
            + "name='single' path='/single' processes='0'><file_based state='active'><requisites/></file_based></process_class>"
            + "<process_class max_processes='10' name='multi' path='/multi' processes='0'><file_based state='active'><requisites/></file_based>"
            + "</process_class></process_classes><folders><folder name='OrderJob' path='/OrderJob'>"
            + "<file_based file='C:/sp/jobscheduler_1.10.6-SNAPSHOT/scheduler_4444/config/live/OrderJob' "
            + "last_write_time='2016-09-05T12:01:35.000Z' state='active'><requisites/></file_based></folder><folder name='check_history' "
            + "path='/check_history'><file_based file='C:/sp/jobscheduler_1.10.6-SNAPSHOT/scheduler_4444/config/live/check_history' "
            + "last_write_time='2016-09-07T07:24:37.000Z' state='active'><requisites/></file_based></folder><folder name='sos' path='/sos'>"
            + "<file_based file='C:/sp/jobscheduler_1.10.6-SNAPSHOT/scheduler_4444/config/live/sos' last_write_time='2016-08-31T08:22:36.000Z' "
            + "state='active'><requisites/></file_based><folders><folder name='dailyschedule' path='/sos/dailyschedule'><file_based "
            + "file='C:/sp/jobscheduler_1.10.6-SNAPSHOT/scheduler_4444/config/live/sos/dailyschedule' last_write_time='2016-08-31T08:22:35.000Z' "
            + "state='active'><requisites/></file_based></folder><folder name='housekeeping' path='/sos/housekeeping'><file_based "
            + "file='C:/sp/jobscheduler_1.10.6-SNAPSHOT/scheduler_4444/config/live/sos/housekeeping' last_write_time='2016-08-31T08:22:36.000Z' "
            + "state='active'><requisites/></file_based></folder><folder name='operations' path='/sos/operations'><file_based "
            + "file='C:/sp/jobscheduler_1.10.6-SNAPSHOT/scheduler_4444/config/live/sos/operations' last_write_time='2016-08-31T08:22:23.000Z' "
            + "state='active'><requisites/></file_based><folders><folder name='criticalpath' path='/sos/operations/criticalpath'><file_based "
            + "file='C:/sp/jobscheduler_1.10.6-SNAPSHOT/scheduler_4444/config/live/sos/operations/criticalpath' "
            + "last_write_time='2016-08-31T08:22:29.000Z' state='active'><requisites/></file_based></folder></folders></folder>"
            + "<folder name='reporting' path='/sos/reporting'><file_based "
            + "file='C:/sp/jobscheduler_1.10.6-SNAPSHOT/scheduler_4444/config/live/sos/reporting' last_write_time='2016-08-31T08:22:35.000Z' "
            + "state='active'><requisites/></file_based></folder><folder name='jade' path='/sos/jade'><file_based "
            + "file='C:/sp/jobscheduler_1.10.6-SNAPSHOT/scheduler_4444/config/live/sos/jade' last_write_time='2016-08-31T08:22:33.000Z' "
            + "state='active'><requisites/></file_based></folder><folder name='jitl' path='/sos/jitl'><file_based "
            + "file='C:/sp/jobscheduler_1.10.6-SNAPSHOT/scheduler_4444/config/live/sos/jitl' last_write_time='2016-08-31T08:22:34.000Z' "
            + "state='active'><requisites/></file_based></folder><folder name='notification' path='/sos/notification'><file_based "
            + "file='C:/sp/jobscheduler_1.10.6-SNAPSHOT/scheduler_4444/config/live/sos/notification' last_write_time='2016-08-31T08:22:33.000Z' "
            + "state='active'><requisites/></file_based></folder></folders></folder><folder name='test_JS-1473' path='/test_JS-1473'>"
            + "<file_based file='C:/sp/jobscheduler_1.10.6-SNAPSHOT/scheduler_4444/config/live/test_JS-1473' "
            + "last_write_time='2016-09-08T07:43:13.000Z' state='active'><requisites/></file_based></folder></folders></folder>"
            + "<subprocesses/><remote_schedulers active='0' count='0'/><http_server/><connections><connection operation_type='HTTP' "
            + "received_bytes='25573' responses='125' sent_bytes='507380' state='ready/receiving'><peer host_ip='192.11.0.50' port='59453'/>"
            + "</connection><connection operation_type='HTTP' received_bytes='15085' responses='76' sent_bytes='499559' state='ready/receiving'>"
            + "<peer host_ip='192.11.0.50' port='59454'/></connection><connection operation_type='HTTP' received_bytes='411' responses='0' "
            + "sent_bytes='0' state='processing/ready'><peer host_ip='127.0.0.1' port='59588'/><operation><http_operation/></operation>"
            + "</connection></connections></state></answer></spooler>";
    private InitialInventoryUpdateJob entriesJob = null;
    private InitialInventoryUpdateJobOptions options = null;
	
	public InitialInventoryUpdateJobJUnitTest() {
		//
	}

	@Before
	public void setUp() throws Exception {
		entriesJob = new InitialInventoryUpdateJob();
		entriesJob.registerMessageListener(this);
		options = entriesJob.getOptions();
		options.registerMessageListener(this);
		JSListenerClass.bolLogDebugInformation = true;
		JSListenerClass.intMaxDebugLevel = 9;
	}

	@Test
	public void testExecute() throws Exception {
	    entriesJob.getOptions().schedulerHibernateConfigurationFile.setValue(SCHEDULER_HIBERNATE_CONFIGURATION);
	    entriesJob.setAnswerXml(ANSWER_XML);
		entriesJob.execute();
	}

}   
