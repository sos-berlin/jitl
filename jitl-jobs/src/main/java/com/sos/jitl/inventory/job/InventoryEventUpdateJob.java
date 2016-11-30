package com.sos.jitl.inventory.job;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.hibernate.classes.SOSHibernateConnection;
import com.sos.jitl.inventory.data.InventoryEventUpdateUtil;


public class InventoryEventUpdateJob {  

	protected InventoryEventUpdateJobOptions objOptions = null;
	private static final Logger LOGGER = LoggerFactory.getLogger(InventoryEventUpdateJob.class);
    private SOSHibernateConnection connection;
    private String masterUrl;
    private InventoryEventUpdateUtil updateUtil;

	public InventoryEventUpdateJob(String url, SOSHibernateConnection hibernateConnection) {
	    this.masterUrl = url;
	    this.connection = hibernateConnection;
	}
 
	public InventoryEventUpdateJob execute() throws Exception {
		try { 
			updateUtil = new InventoryEventUpdateUtil(masterUrl, connection);
			updateUtil.execute();
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
            throw e;			
		} finally {
            // TODO
		}
		return this;
	}

	public void closeHttpClient() {
	    try {
	        if (updateUtil != null && updateUtil.getHttpClient() != null) {
	            updateUtil.getHttpClient().close();
	        }
        } catch (IOException e) {
            // Do Nothing
//            LOGGER.warn("Error occurred closing HttpClient: " +e.getMessage(), e);
        }
	}
}  