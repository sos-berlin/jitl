package com.sos.jitl.sync;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

public class SyncNodeList {
	private static Logger		logger			= Logger.getLogger(SyncNodeList.class);

	private static final String CONST_PARAM_PART_REQUIRED_ORDERS = "_required_orders_";
	private List <SyncNode> listOfNodes;
	private int nodeIndex=0;

	public SyncNodeList() {
		super();
		listOfNodes = new ArrayList<SyncNode>();
	}


	public void addNode(SyncNode sn){
		if (listOfNodes == null){
			listOfNodes = new ArrayList<SyncNode>();
	    }
		
		listOfNodes.add(sn);		
	}

		
	public boolean isReleased(){
		boolean erg = true;
 
		for( SyncNode sn: listOfNodes ){
			erg = erg && sn.isReleased();
		}
		return erg;
	}

	public int getCount(){
		return listOfNodes.size();
	}
	
	public void setRequired(String job_chain_required){
		for( SyncNode sn: listOfNodes ){
			String prefix = sn.getSyncNodeJobchain();
			if (job_chain_required.startsWith(prefix + CONST_PARAM_PART_REQUIRED_ORDERS)){
				sn.setRequired(getRequiredFromPrefix(prefix,job_chain_required));			
			}
			
			prefix = sn.getSyncNodeJobchain() + ";" + sn.getSyncNodeState();
			if (job_chain_required.startsWith(prefix + CONST_PARAM_PART_REQUIRED_ORDERS)){
				sn.setRequired(getRequiredFromPrefix(prefix,job_chain_required));		
			}
		}
	}
	
	public String getRequiredFromPrefix(String jobchain_name, String jobchain_required){
		String erg = jobchain_required.replaceAll("^"+jobchain_name+CONST_PARAM_PART_REQUIRED_ORDERS, "");
		return erg;
	}

	public void setRequired(int required){
		for( SyncNode sn: listOfNodes ){
			if (sn.getClass() == null){
			  sn.setRequired(required);	
			}
		}
	}
	
	public void addOrder(SyncNodeWaitingOrder order,String jobchain, String state, String syncId){
		logger.debug(String.format("Adding order: %s.%s",jobchain,order.getId()));
		for( SyncNode sn: listOfNodes ){
			if (sn.getSyncNodeState().equals(state) && sn.getSyncNodeJobchainPath().equals(jobchain)){
				logger.debug("---->"+sn.getSyncNodeJobchainPath() +  ":" + sn.getSyncNodeState() );
				sn.addOrder(order,syncId);
			}
			 
		}
	}

	public List<SyncNode> getListOfNodes() {
		return listOfNodes;
	}

	public boolean eof(){
		return (nodeIndex >= getCount());
	}
	
	public SyncNode getNextSyncNode(){
		SyncNode sn = getListOfNodes().get(nodeIndex);
		nodeIndex++;
		return sn;
	}
}
