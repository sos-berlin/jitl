package com.sos.jitl.sync;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
  
public class SyncNodeContainer {
	
	private static Logger		logger			= Logger.getLogger(SyncNodeContainer.class);


	private static final String XPATH_CURRENT_JOB_CHAIN = "//order[@id = '%s'][@job_chain = '%s']/payload/params/param[@name='sync_session_id']";
	private static final String ATTRIBUTE_PARAMETER_VALUE = "value";
	private static final String ATTRIBUTE_JOB_CHAIN = "job_chain";
	private static final String ATTRIBUTE_ORDER_ID = "id";
	private static final String ATTRIBUTE_STATE = "state";
	private static final String XPATH_FOR_ORDERS = "//order_queue/order";
	private static final String XPATH_FOR_JOB_CHAINS = "//job_chains/job_chain/job_chain_node[@job = '%s']";
	private String jobpath;
	private String syncId="";
	private SyncNodeList listOfSyncNodes;
 
	public void getNodes(String xml) throws Exception{
		//logger.debug(String.format("adding nodes for sync job: %s", jobpath));
		listOfSyncNodes = new SyncNodeList();
		SyncXmlReader xmlReader = new SyncXmlReader(xml,String.format(XPATH_FOR_JOB_CHAINS,jobpath));
		while (!xmlReader.eof()){
			//logger.debug("reading next node");
			xmlReader.getNext();
			SyncNode sn = new SyncNode();
		    sn.setSyncNodeJobchainName( xmlReader.getAttributeValueFromParent("name"));
	        sn.setSyncNodeJobchainPath( xmlReader.getAttributeValueFromParent("path"));
	      	sn.setSyncNodeState( xmlReader.getAttributeValue("state"));
	      	//logger.debug(String.format("adding node chain: %s state: %s", sn.getSyncNodeJobchainPath(),sn.getSyncNodeState()));
	      	listOfSyncNodes.addNode(sn);
	   }		
 	}
	
	public void getOrders(String xml) throws Exception{		
		SyncXmlReader xmlReader = new SyncXmlReader(xml,XPATH_FOR_ORDERS);
		while (!xmlReader.eof()){
			xmlReader.getNext();
	        String id =  xmlReader.getAttributeValue(ATTRIBUTE_ORDER_ID);
	        String chain =  xmlReader.getAttributeValue(ATTRIBUTE_JOB_CHAIN);
	        String state =  xmlReader.getAttributeValue(ATTRIBUTE_STATE);
      	    String orderSyncId = xmlReader.getAttributeValueFromXpath(String.format(XPATH_CURRENT_JOB_CHAIN,id,chain),ATTRIBUTE_PARAMETER_VALUE);
 		    SyncNodeWaitingOrder o = new SyncNodeWaitingOrder(id,orderSyncId); 
 		    listOfSyncNodes.addOrder(o,chain,state,this.syncId);
		}
	               
	}
	
	
	public void setJobpath(String jobpath) {
		this.jobpath = jobpath;
	}

	
	public void setSyncId(String syncId) {
		this.syncId = syncId;
	}

	public SyncNodeList getListOfSyncNodes() {
		return listOfSyncNodes;
	}
	
	public boolean isReleased(){
		return listOfSyncNodes.isReleased();
	}

	public void setRequiredOrders(HashMap <String,String> schedulerParameters){
		Iterator <String> ii = schedulerParameters.keySet().iterator();
		
		if (schedulerParameters.get("required_orders") != null){
			String requiredOrders = schedulerParameters.get("required_orders");
			try{
				listOfSyncNodes.setRequired(Integer.parseInt(requiredOrders));
			}catch(NumberFormatException e){
				logger.warn(String.format("Could not convert %s to int",requiredOrders));
			}
		}

		while (ii.hasNext()){
			String key = ii.next();
			if (key.contains("_required_orders")){
				//logger.debug(String.format("setting %s",key + "_" + schedulerParameters.get(key)));
				listOfSyncNodes.setRequired(key + "_" + schedulerParameters.get(key));
			}
      	}

	}
	
	public boolean eof(){
		return getListOfSyncNodes().eof();
	}
	
	public SyncNode  getNextSyncNode(){
		  return getListOfSyncNodes().getNextSyncNode();
	}
}
