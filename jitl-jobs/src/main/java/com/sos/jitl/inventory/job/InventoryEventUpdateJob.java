package com.sos.jitl.inventory.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.hibernate.classes.SOSHibernateConnection;
import com.sos.jitl.inventory.data.InventoryEventUpdateUtil;


public class InventoryEventUpdateJob {  

	protected InventoryEventUpdateJobOptions objOptions = null;
	private static final Logger LOGGER = LoggerFactory.getLogger(InventoryEventUpdateJob.class);
    private SOSHibernateConnection connection;
    private String masterUrl;

	public InventoryEventUpdateJob(String url, SOSHibernateConnection hibernateConnection) {
	    this.masterUrl = url;
	    this.connection = hibernateConnection;
	}
 
	public InventoryEventUpdateJob execute() throws Exception {
		try { 
			InventoryEventUpdateUtil updateUtil = new InventoryEventUpdateUtil(masterUrl, connection);
			updateUtil.execute();
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
            throw e;			
		} finally {
            // TODO
		}
		return this;
	}

}  