package com.sos.jitl.sync;

import java.util.HashMap;
import java.util.Iterator;

import org.apache.log4j.Logger;

public class SyncNodeContainer {

	private static Logger		logger						= Logger.getLogger(SyncNodeContainer.class);

    private static final String XPATH_CURRENT_JOB_CHAIN          = "//order[@id = '%s'][@job_chain = '%s']/payload/params/param[@name='sync_session_id']";
    private static final String XPATH_CURRENT_JOB_CHAIN_CONTEXT  = "//order[@id = '%s'][@job_chain = '%s']/payload/params/param[@name='sync_node_context']";
	private static final String	ATTRIBUTE_PARAMETER_VALUE	= "value";
	private static final String	ATTRIBUTE_JOB_CHAIN			= "job_chain";
	private static final String	ATTRIBUTE_ORDER_ID			= "id";
	private static final String	ATTRIBUTE_STATE				= "state";
	private static final String	ATTRIBUTE_END_STATE			= "end_state";
	private static final String	XPATH_FOR_ORDERS			= "//order_queue/order";
    private static final String XPATH_FOR_ALL_JOB_CHAINS        = "//job_chains/job_chain/job_chain_node[@job = '%s']";
    private static final String XPATH_FOR_ONE_JOB_CHAIN        = "//job_chains/job_chain[@path = '%s']/job_chain_node[@job = '%s']";
    private static final String XPATH_FOR_ONE_JOB_CHAIN_STATE        = "//job_chains/job_chain[@path = '%s']/job_chain_node[@job = '%s' and @state='%s']";
    private String              jobpath;
    private String              syncNodeContext ="";
    private String              syncNodeContextJobChain ="";
    private String              syncNodeContextState ="";
//private String				syncId						= "";
	private SyncNodeList		listOfSyncNodes;

	public void getNodes(final String xml) throws Exception {
		//logger.debug(String.format("adding nodes for sync job: %s", jobpath));
		listOfSyncNodes = new SyncNodeList();
        SyncXmlReader xmlReader = null;
		if (syncNodeContext.equals("")) {
            logger.debug("looking for sync nodes in all jobchains");
	        xmlReader = new SyncXmlReader(xml, String.format(XPATH_FOR_ALL_JOB_CHAINS, jobpath));
		}else {
		    if (syncNodeContextState.equals("")) {
	            logger.debug(String.format("looking for sync nodes in jobchain: %s",syncNodeContextJobChain));
	            logger.debug(String.format(XPATH_FOR_ONE_JOB_CHAIN, syncNodeContextJobChain,jobpath));
	            xmlReader = new SyncXmlReader(xml, String.format(XPATH_FOR_ONE_JOB_CHAIN, syncNodeContextJobChain,jobpath));
		        
		    }else {
	            logger.debug(String.format("looking for sync node in jobchain: %s in state %s",syncNodeContextJobChain,syncNodeContextState));
	            logger.debug(String.format(XPATH_FOR_ONE_JOB_CHAIN_STATE, syncNodeContext,jobpath,syncNodeContextState));
	            xmlReader = new SyncXmlReader(xml, String.format(XPATH_FOR_ONE_JOB_CHAIN_STATE, syncNodeContextJobChain,jobpath,syncNodeContextState));
		    }
		}

		while (!xmlReader.eof()) {
			logger.debug("reading next node");
			xmlReader.getNext();
			SyncNode sn = new SyncNode();
			sn.setSyncNodeJobchainName(xmlReader.getAttributeValueFromParent("name"));
			sn.setSyncNodeJobchainPath(xmlReader.getAttributeValueFromParent("path"));
			sn.setSyncNodeState(xmlReader.getAttributeValue("state"));
            logger.debug(String.format("adding node chain: %s state: %s", sn.getSyncNodeJobchainPath(), sn.getSyncNodeState()));
	        listOfSyncNodes.addNode(sn);
		}
	}

	public void getOrders(final String xml) throws Exception {
		logger.debug("xml in getOrders = " + xml);
		SyncXmlReader xmlReader = new SyncXmlReader(xml, XPATH_FOR_ORDERS);
		while (!xmlReader.eof()) {
			xmlReader.getNext();
			String id = xmlReader.getAttributeValue(ATTRIBUTE_ORDER_ID);
			String chain = xmlReader.getAttributeValue(ATTRIBUTE_JOB_CHAIN);
			String state = xmlReader.getAttributeValue(ATTRIBUTE_STATE);
            String orderSyncId = xmlReader.getAttributeValueFromXpath(String.format(XPATH_CURRENT_JOB_CHAIN, id, chain), ATTRIBUTE_PARAMETER_VALUE);
            
            String orderContext = xmlReader.getAttributeValueFromXpath(String.format(XPATH_CURRENT_JOB_CHAIN_CONTEXT, id, chain), ATTRIBUTE_PARAMETER_VALUE);
            orderContext = this.normalizeContext(orderContext);
                    
            
            //Add only orders with the same context 
            //Possible optimization: Change xpath to read only context nodes.
            if (orderContext.equals(this.syncNodeContext)) {
      			SyncNodeWaitingOrder o = new SyncNodeWaitingOrder(id, orderSyncId);
    			o.setEndState(xmlReader.getAttributeValue(ATTRIBUTE_END_STATE));
    			listOfSyncNodes.addOrder(o, chain, state, orderSyncId);
            }
		}
	}

	public void setJobpath(final String jobpath) {
		this.jobpath = jobpath;
	}

//	public void setSyncId(final String syncId) {
//		this.syncId = syncId;
//	}

	public SyncNodeList getListOfSyncNodes() {
		return listOfSyncNodes;
	}
	
	public SyncNode getNode(String jobChain, String state) {
	    SyncNode sn = listOfSyncNodes.getNode(jobChain,state);
	    return sn; 
 	}

	public SyncNode getFirstNotReleasedNode() {
	   SyncNode sn = listOfSyncNodes.getFirstNotReleasedNode();
	   return sn; 
	}
	
	public int getNumberOfWaitingNodes() {
	    return listOfSyncNodes.getNumberOfWaitingNodes();
	}
	
	public boolean isReleased() {
		return listOfSyncNodes.isReleased();
	}

	public void setRequiredOrders(final HashMap<String, String> schedulerParameters) {
		Iterator<String> ii = schedulerParameters.keySet().iterator();

		if (schedulerParameters.get(SyncNodeList.CONST_PARAM_REQUIRED_ORDERS) != null) {
			String requiredOrders = schedulerParameters.get(SyncNodeList.CONST_PARAM_REQUIRED_ORDERS);
			try {
				listOfSyncNodes.setRequired(Integer.parseInt(requiredOrders));
			}
			catch (NumberFormatException e) {
				logger.warn(String.format("Could not convert %s to int", requiredOrders));
			}
		}

		/**
		 * hier kommt der absolute hack. variable parameternamen. wozu?
		 * *Der Job sollte kompatibel zur alten Implementierung sein. ur 17.7.2013
		 *
		 * Der Wert wird grottig zusammengebaut um dann später genauso grenzwertig wieder isoliert zu werden.
		 * So ein Scheiß! kb
		 * Junit Tests müssen bei so einer Änderung angepasst werden.ur 5.2.2014
		 */
		while (ii.hasNext()) {
			String key = ii.next();
			if (key.contains(SyncNodeList.CONST_PARAM_PART_REQUIRED_ORDERS)) {
				logger.debug(String.format("key = %s, setting %s", key, key + schedulerParameters.get(key)));
				listOfSyncNodes.setRequired(key + schedulerParameters.get(key));
			}
		}

	}

	public boolean eof() {
		return getListOfSyncNodes().eof();
	}

	public SyncNode getNextSyncNode() {
		return getListOfSyncNodes().getNextSyncNode();
	}

	private String normalizeContext(String s) {
	    s = s.trim();
        if (!s.startsWith("/") && s.length() > 0){
            s = "/" + s;
        }
        return s;
	}

	public void setSyncNodeContext(String syncNodeContext) {
        syncNodeContext = normalizeContext(syncNodeContext);
                
        this.syncNodeContext = syncNodeContext;
        String s[] = syncNodeContext.split(",");
        if (s.length == 1){
           this.syncNodeContextJobChain=syncNodeContext;
           this.syncNodeContextState="";
        }else {
            this.syncNodeContextJobChain=s[0];
            this.syncNodeContextState=s[1];
        }
        
    }
}
