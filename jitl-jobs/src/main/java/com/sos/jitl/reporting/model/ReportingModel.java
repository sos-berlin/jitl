package com.sos.jitl.reporting.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.hibernate.classes.SOSHibernateConnection;
import com.sos.jitl.reporting.db.DBLayerReporting;

public class ReportingModel {
	private Logger logger = LoggerFactory.getLogger(ReportingModel.class);
	private DBLayerReporting dbLayer;
	
	/**
	 * 
	 * @param reportingConn
	 * @throws Exception
	 */
	public ReportingModel(SOSHibernateConnection reportingConn) throws Exception{
		if(reportingConn == null){
			throw new Exception("reporingConn is null");
		}
		dbLayer = new DBLayerReporting(reportingConn);
	}
	
	/**
	 * 
	 * @return
	 */
	public DBLayerReporting getDbLayer(){
		return dbLayer;
	}
	
}
