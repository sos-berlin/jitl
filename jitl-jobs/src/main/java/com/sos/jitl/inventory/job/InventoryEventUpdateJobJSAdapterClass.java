package com.sos.jitl.inventory.job;

import java.net.InetAddress;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import sos.scheduler.job.JobSchedulerJobAdapter;
import sos.xml.SOSXMLXPath;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.hibernate.classes.SOSHibernateConnection;
import com.sos.jitl.reporting.db.DBLayer;

public class InventoryEventUpdateJobJSAdapterClass extends JobSchedulerJobAdapter {
	private static final Logger LOGGER = LoggerFactory.getLogger(InventoryEventUpdateJobJSAdapterClass.class);
	private static final String HIBERNATE_CFG = "hibernate.cfg.xml";
    private static final String COMMAND = 
            "<show_state subsystems=\"folder\" what=\"folders no_subfolders\" path=\"/any/path/that/does/not/exists\" />";
	private ExecutorService singleThreadExecutor;
	private String url;
	private	Path hibernateCfgPath;
	private InventoryEventUpdateJob updateJob;
	
	@Override
	public boolean spooler_init() {
        try {
            url = getUrlFromJobScheduler();
            LOGGER.debug("URL of the JobScheduler running this Job: " + url);
//            System.out.println("URL of the JobScheduler running this Job: " + url);
            hibernateCfgPath = Paths.get(spooler.directory(), "config", HIBERNATE_CFG);
            LOGGER.debug("Hibernate config file path: " + hibernateCfgPath);
//            System.out.println("Hibernate config file path: " + hibernateCfgPath);
            singleThreadExecutor = Executors.newSingleThreadExecutor();
            Runnable runnableThread = new Runnable() {
                @Override
                public void run() {
                    try {
                        doProcessing();
                    } catch (Exception e) {
                        LOGGER.error(e.getMessage(), e);
//                        e.printStackTrace();
                    }
                }
            };
            singleThreadExecutor.execute(runnableThread);
        } catch (Exception e) {
            throw new JobSchedulerException("Fatal Error:" + e.getMessage(), e);
        }
		return super.spooler_init();
	}

	@Override
	public void spooler_exit() {
		singleThreadExecutor.shutdownNow();
        try {
            if (updateJob != null) {
                updateJob.closeHttpClient();
            }
            boolean shutdown = singleThreadExecutor.awaitTermination(1L, TimeUnit.SECONDS);
            if(shutdown) {
                LOGGER.debug("Thread has been shut down correctly.");
            } else {
                LOGGER.debug("Thread has ended due to timeout on shutdown. Doesn´t wait for answer from thread.");
            }
        } catch (InterruptedException e) {
            LOGGER.error(e.getMessage(), e);
//            e.printStackTrace();
        }
		super.spooler_exit();
	}

	private void doProcessing() throws Exception {
        updateJob = new InventoryEventUpdateJob(url, initDbConnection(hibernateCfgPath));
		updateJob.execute();
	}  

	private SOSHibernateConnection initDbConnection(Path hibernateCfgPath) throws Exception {
        SOSHibernateConnection connection = new SOSHibernateConnection(hibernateCfgPath.toString());
        connection.setAutoCommit(false);
        connection.setIgnoreAutoCommitTransactions(true);
        connection.addClassMapping(DBLayer.getInventoryClassMapping());
        connection.connect();
        return connection;
	}
	
	private String getUrlFromJobScheduler() throws Exception {
	    StringBuilder strb = new StringBuilder();
	    strb.append("http://");
	    String answerXml = spooler.execute_xml(COMMAND);
        SOSXMLXPath xPath = new SOSXMLXPath(new StringBuffer(answerXml));
        Node stateNode = xPath.selectSingleNode("/spooler/answer/state");
        Element stateElement = (Element) stateNode;
        strb.append(InetAddress.getLocalHost().getCanonicalHostName().toLowerCase());
        strb.append(":");
        String httpPort = stateElement.getAttribute("http_port");
        if(httpPort != null && !httpPort.isEmpty()) {
            strb.append(httpPort);
        } else {
            strb.append("4444");
        }
        return strb.toString();
	}
}