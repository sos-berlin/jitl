package com.sos.jitl.reporting.db;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.Transformers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.hibernate.classes.SOSHibernateConnection;
import com.sos.hibernate.classes.SOSHibernateConnection.DBMS;
import com.sos.jitl.reporting.helper.EReferenceType;
import com.sos.jitl.reporting.helper.ReportUtil;
import com.sos.scheduler.db.SchedulerInstancesDBItem;
import com.sos.scheduler.history.db.SchedulerOrderStepHistoryDBItem;


public class DBLayerReporting extends DBLayer{
	final Logger logger = LoggerFactory.getLogger(DBLayerReporting.class);
	
	/**
	 * 
	 * @param conn
	 */
	public DBLayerReporting(SOSHibernateConnection conn){
		super(conn);
	}
	
	/**
	 * 
	 * @param schedulerId
	 * @param schedulerHost
	 * @param schedulerPort
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public DBItemInventoryInstance getInventoryInstance(
			String schedulerId,
			String schedulerHost,
			Long schedulerPort) throws Exception{
		StringBuffer sql = new StringBuffer("from ")
		.append(DBITEM_INVENTORY_INSTANCES)
		.append(" where upper(schedulerId) = :schedulerId")
		.append(" and upper(hostname) = :hostname")
		.append(" and port = :port")
		.append(" order by id asc");
		
		Query query = getConnection().createQuery(sql.toString());
		query.setParameter("schedulerId",schedulerId.toUpperCase());
		query.setParameter("hostname",schedulerHost.toUpperCase());
		query.setParameter("port",schedulerPort);
		
		List<DBItemInventoryInstance> result = query.list();
		if(result.size() > 0){
			return result.get(0);
		}
		
		return null;
	}
	
	/**
	 * 
	 * @param schedulerId
	 * @param schedulerHost
	 * @param schedulerPort
	 * @param configurationDirectory
	 * @return
	 * @throws Exception
	 */
	public DBItemInventoryInstance createInventoryInstance(
			String schedulerId,
			String schedulerHost,
			Long schedulerPort,
			String configurationDirectory) throws Exception{
		
		DBItemInventoryInstance item = new DBItemInventoryInstance();
		item.setSchedulerId(schedulerId);
		item.setHostname(schedulerHost);
		item.setPort(schedulerPort);
		item.setLiveDirectory(configurationDirectory);
		item.setCreated(ReportUtil.getCurrentDateTime());
		item.setModified(ReportUtil.getCurrentDateTime());
		
		getConnection().save(item);
		return item;
	}
	
	/**
	 * 
	 * @param instanceId
	 * @param fileType
	 * @param fileName
	 * @param fileBasename
	 * @param fileDirectory
	 * @param fileCreated
	 * @param fileModified
	 * @param fileLocalCreated
	 * @param fileLocalModified
	 * @return
	 * @throws Exception
	 */
	public DBItemInventoryFile createInventoryFile(
			Long instanceId,
			String fileType,
			String fileName,
			String fileBasename,
			String fileDirectory,
			Date fileCreated,
			Date fileModified,
			Date fileLocalCreated,
			Date fileLocalModified) throws Exception{
		
		DBItemInventoryFile item = new DBItemInventoryFile();
		item.setInstanceId(instanceId);
		item.setFileType(fileType);
		item.setFileName(fileName);
		item.setFileBaseName(fileBasename);
		item.setFileDirectory(fileDirectory);
		item.setFileCreated(fileCreated);
		item.setFileModified(fileModified);
		item.setFileLocalCreated(fileLocalCreated);
		item.setFileLocalModified(fileLocalModified);
		item.setCreated(ReportUtil.getCurrentDateTime());
		item.setModified(ReportUtil.getCurrentDateTime());
		
		getConnection().save(item);
	return item;
	}
	
	/**
	 * 
	 * @param instanceId
	 * @param fileId
	 * @param jobChainName
	 * @param name
	 * @param basename
	 * @param orderId
	 * @param title
	 * @param isRuntimeDefined
	 * @return
	 * @throws Exception
	 */
	public DBItemInventoryOrder createInventoryOrder(
		Long instanceId,
		Long fileId,
		String jobChainName,
		String name,
		String basename,
		String orderId,
		String title,
		boolean isRuntimeDefined) throws Exception{
		
		DBItemInventoryOrder item = new DBItemInventoryOrder();
		item.setInstanceId(instanceId);
		item.setFileId(fileId);
		item.setJobChainName(jobChainName);
		item.setName(name);
		item.setBaseName(basename);
		item.setOrderId(orderId);
		item.setTitle(title);
		item.setIsRuntimeDefined(isRuntimeDefined);
		item.setCreated(ReportUtil.getCurrentDateTime());
		item.setModified(ReportUtil.getCurrentDateTime());
		
		getConnection().save(item);
	return item;
	}
	
	/**
	 * 
	 * @param instanceId
	 * @param fileId
	 * @param startCause
	 * @param name
	 * @param basename
	 * @param title
	 * @return
	 * @throws Exception
	 */
	public DBItemInventoryJobChain createInventoryJobChain(
		Long instanceId,
		Long fileId,
		String startCause,
		String name,
		String basename,
		String title) throws Exception{
		
		DBItemInventoryJobChain item = new DBItemInventoryJobChain();
		item.setInstanceId(instanceId);
		item.setFileId(fileId);
		item.setStartCause(startCause);
		item.setName(name);
		item.setBaseName(basename);
		item.setTitle(title);
		item.setCreated(ReportUtil.getCurrentDateTime());
		item.setModified(ReportUtil.getCurrentDateTime());
		
		getConnection().save(item);
	return item;
	}
	
	/**
	 * 
	 * @param instanceId
	 * @param jobChainId
	 * @param jobName
	 * @param ordering
	 * @param name
	 * @param state
	 * @param nextState
	 * @param errorState
	 * @param job
	 * @return
	 * @throws Exception
	 */
	public DBItemInventoryJobChainNode createInventoryJobChainNode(
		Long instanceId,
		Long jobChainId,
		String jobName,
		Long ordering,
		String name,
		String state,
		String nextState,
		String errorState,
		String job
		) throws Exception{
		
		DBItemInventoryJobChainNode item = new DBItemInventoryJobChainNode();
		
		item.setInstanceId(instanceId);
		item.setJobChainId(jobChainId);
		item.setJobName(jobName);
		item.setOrdering(ordering);
		item.setName(name);
		item.setState(state);
		item.setNextState(nextState);
		item.setErrorState(errorState);
		item.setJob(job);
		item.setCreated(ReportUtil.getCurrentDateTime());
		item.setModified(ReportUtil.getCurrentDateTime());
		
		getConnection().save(item);		
	return item;
	}
	
	/**
	 * 
	 * @param instanceId
	 * @param fileId
	 * @param name
	 * @param basename
	 * @param title
	 * @param isOrderJob
	 * @param isRuntimeDefined
	 * @return
	 * @throws Exception
	 */
	public DBItemInventoryJob createInventoryJob(
		Long instanceId,
		Long fileId,
		String name,
		String basename,
		String title,
		boolean isOrderJob,
		boolean isRuntimeDefined) throws Exception{
		
		DBItemInventoryJob item = new DBItemInventoryJob();
		item.setInstanceId(instanceId);
		item.setFileId(fileId);
		item.setName(name);
		item.setBaseName(basename);
		item.setTitle(title);
		item.setIsOrderJob(isOrderJob);
		item.setIsRuntimeDefined(isRuntimeDefined);
		item.setCreated(ReportUtil.getCurrentDateTime());
		item.setModified(ReportUtil.getCurrentDateTime());
		
		getConnection().save(item);
		return item;
	}
	
	/**
	 * 
	 * @param instanceId
	 * @param liveDirectory
	 * @return
	 * @throws Exception
	 */
	public int updateInventoryLiveDirectory(Long instanceId,String liveDirectory) throws Exception{
		StringBuffer sql = new StringBuffer();
		
		sql.append("update ")
		.append(DBITEM_INVENTORY_INSTANCES)
		.append(" set liveDirectory = :liveDirectory")
		.append(" where id = :instanceId");
		Query query = getConnection().createQuery(sql.toString());
		query.setParameter("instanceId",instanceId);
		query.setParameter("liveDirectory",liveDirectory);
		
		return query.executeUpdate();
	}
	
	/**
	 * 
	 * @param instanceId
	 */
	@SuppressWarnings("unused")
	public void cleanupInventory(Long instanceId) throws Exception{
		StringBuffer sql = new StringBuffer();
		
		sql.append("delete from ")
		.append(DBITEM_INVENTORY_ORDERS)
		.append(" where instanceId = :instanceId");
		Query query = getConnection().createQuery(sql.toString());
		query.setParameter("instanceId",instanceId);
		int r = query.executeUpdate();
		
		sql = new StringBuffer();
		sql.append("delete from ")
		.append(DBITEM_INVENTORY_JOB_CHAIN_NODES)
		.append(" where instanceId = :instanceId");
		query = getConnection().createQuery(sql.toString());
		query.setParameter("instanceId",instanceId);
		r = query.executeUpdate();
		
		sql = new StringBuffer();
		sql.append("delete from ")
		.append(DBITEM_INVENTORY_JOB_CHAINS)
		.append(" where instanceId = :instanceId");
		query = getConnection().createQuery(sql.toString());
		query.setParameter("instanceId",instanceId);
		r = query.executeUpdate();
		
		sql = new StringBuffer();
		sql.append("delete from ")
		.append(DBITEM_INVENTORY_JOBS)
		.append(" where instanceId = :instanceId");
		query = getConnection().createQuery(sql.toString());
		query.setParameter("instanceId",instanceId);
		r = query.executeUpdate();
		
		//DBITEM_INVENTORY_FILES
		sql = new StringBuffer();
		sql.append("delete from ")
		.append(DBITEM_INVENTORY_FILES)
		.append(" where instanceId = :instanceId");
		query = getConnection().createQuery(sql.toString());
		query.setParameter("instanceId",instanceId);
		r = query.executeUpdate();
	}
	
	
	
	/**
	 * 
	 * @param schedulerId
	 * @param historyId
	 * @param name
	 * @param title
	 * @param parentName
	 * @param parentBasename
	 * @param parentTitle
	 * @param state
	 * @param stateText
	 * @param startTime
	 * @param endTime
	 * @param synCompleted
	 * @return
	 * @throws Exception
	 */
	public DBItemReportTrigger createReportTrigger(
		String schedulerId,
		Long historyId,
		String name,
		String title,
		String parentName,
		String parentBasename,
		String parentTitle,
		String state,
		String stateText,
		Date startTime,
		Date endTime,
		boolean synCompleted) throws Exception{
		
		DBItemReportTrigger item = new DBItemReportTrigger();
		item.setSchedulerId(schedulerId);
		item.setHistoryId(historyId);
		item.setName(name);
		item.setTitle(title);
		item.setParentName(parentName);
		item.setParentBasename(parentBasename);
		item.setParentTitle(parentTitle);
		item.setState(state);
		item.setStateText(stateText);
		item.setStartTime(startTime);
		item.setEndTime(endTime);
		item.setSyncCompleted(synCompleted);
		item.setIsRuntimeDefined(false);
		item.setResultsCompleted(false);
		item.setSuspended(false);
		
		item.setCreated(ReportUtil.getCurrentDateTime());
		item.setModified(ReportUtil.getCurrentDateTime());
		
		getConnection().save(item);
		
	return item;
	}
	
	
	/**
	 * 
	 * @param schedulerIds
	 * @return
	 * @throws Exception
	 */
	public Criteria getUncomlitedReportTriggerHistoryIds(ArrayList<String> schedulerIds) throws Exception{
		
		//return getConnection().getSingleList(DBItemReportTriggers.class,"historyId", where);
		Criteria cr = getConnection().createSingleListCriteria(DBItemReportTrigger.class,"historyId");
		
		Criterion cr1   = Restrictions.in("schedulerId",schedulerIds);
		Criterion cr2 	= Restrictions.eq("syncCompleted",false);
		Criterion where = Restrictions.and(cr1, cr2);
		cr.add(where);
		
		cr.setReadOnly(true);
		return cr;
	}
	
	
		
	/**
	 * 
	 * @param schedulerConnection
	 * @return
	 * @throws Exception
	 */
	public Criteria getSchedulerInstancesSchedulerIds(SOSHibernateConnection schedulerConnection) throws Exception{
		
		Criteria cr = schedulerConnection.createSingleListCriteria(SchedulerInstancesDBItem.class,"schedulerId");
		cr.setReadOnly(true);
		return cr; 
	}
	
	/**
	 * 
	 * @return
	 * @throws Exception
	 */
	public int removeReportingTriggers() throws Exception{
		StringBuffer sql = new StringBuffer("delete from "+DBITEM_REPORT_TRIGGERS+" ")
		.append("where suspended = true");
		return getConnection().createQuery(sql.toString()).executeUpdate();
	}
	
	/**
	 * 
	 * @return
	 * @throws Exception
	 */
	public int removeReportingExecutions() throws Exception{
		StringBuffer sql = new StringBuffer("delete from "+DBITEM_REPORT_EXECUTIONS+" ")
		.append("where suspended = true");
		return getConnection().createQuery(sql.toString()).executeUpdate();
	}
	
	/**
	 * 
	 * @param schedulerIds
	 * @param dateFrom
	 * @param dateTo
	 * @return
	 * @throws Exception
	 */
	public int setReportingTriggersAsRemoved(List<?> schedulerIds, Date dateFrom, Date dateTo) throws Exception{
		StringBuffer sql = null;
		Query q = null;
		int result = 0;
		if (schedulerIds != null && schedulerIds.size() > 0) {
			sql = new StringBuffer("update "+DBITEM_REPORT_TRIGGERS+" ")
			.append("set suspended = true ")
			.append("where schedulerId in :schedulerId ")
			.append("and startTime <= :dateTo ");
			if(dateFrom != null){
				sql.append("and startTime >= :dateFrom");
			}
		
			q = getConnection().createQuery(sql.toString());
			q.setParameterList("schedulerId",schedulerIds);
			q.setParameter("dateTo",dateTo);		
			if(dateFrom != null){
				q.setParameter("dateFrom",dateFrom);
			}
			result = q.executeUpdate();
		}
		return result;
	}
	
	/**
	 * 
	 * @return
	 * @throws Exception
	 */
	public int setReportingExecutionsAsRemoved() throws Exception{
		StringBuffer sql = new StringBuffer("update "+DBITEM_REPORT_EXECUTIONS+" ")
		.append("set suspended = true ")
		.append("where triggerId in (select id from "+DBITEM_REPORT_TRIGGERS+" where suspended = true)");
		Query q = getConnection().createQuery(sql.toString());
		return q.executeUpdate();
	}
	
	
	/**
	 * 
	 * @return
	 * @throws Exception
	 */
	public int removeReportingTriggerResults() throws Exception{
	
		StringBuffer sql = new StringBuffer("delete from "+DBITEM_REPORT_TRIGGER_RESULTS+" ")
		.append("where triggerId in (select id from "+DBITEM_REPORT_TRIGGERS+" where suspended = true)");
		Query q = getConnection().createQuery(sql.toString());
		int result = q.executeUpdate();		
		
		return result;
	}
	
	/**
	 * 
	 * @return
	 * @throws Exception
	 */
	public int removeReportingExecutionDates() throws Exception{
		int result;
		StringBuffer sql;
		Query q;
		
		sql = new StringBuffer("delete from "+DBITEM_REPORT_EXECUTION_DATES+" ")
		.append("where referenceType = :referenceType ")
		.append("and referenceId in (select id from "+DBITEM_REPORT_TRIGGERS+" where suspended = true) ");
		q = getConnection().createQuery(sql.toString());
		q.setParameter("referenceType",EReferenceType.TRIGGER.value());
		result = q.executeUpdate();
		
		sql = new StringBuffer("delete from "+DBITEM_REPORT_EXECUTION_DATES+" ")
		.append("where referenceType = :referenceType ")
		.append("and referenceId in (select id from "+DBITEM_REPORT_EXECUTIONS+" where suspended = true) ");
		q = getConnection().createQuery(sql.toString());
		q.setParameter("referenceType",EReferenceType.EXECUTION.value());
		result+= q.executeUpdate();
		
		//not completed
		sql = new StringBuffer("delete from "+DBITEM_REPORT_EXECUTION_DATES+" ")
		.append("where referenceType = :referenceType ")
		.append("and referenceId in (select id from "+DBITEM_REPORT_TRIGGERS+" where resultsCompleted = false) ");
		q = getConnection().createQuery(sql.toString());
		q.setParameter("referenceType",EReferenceType.TRIGGER.value());
		result += q.executeUpdate();
				
		sql = new StringBuffer("delete from "+DBITEM_REPORT_EXECUTION_DATES+" ")
		.append("where referenceType = :referenceType ")
		.append("and referenceId in (select re.id from "+DBITEM_REPORT_EXECUTIONS+" re,"+DBITEM_REPORT_TRIGGERS+" rt where rt.id = re.triggerId and rt.resultsCompleted = false) ");
		q = getConnection().createQuery(sql.toString());
		q.setParameter("referenceType",EReferenceType.EXECUTION.value());
		result+= q.executeUpdate();
		return result;
	}

	/**
	 * 
	 * @param schedulerConnection
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public DBItemSchedulerVariableReporting getSchedulerVariabe(SOSHibernateConnection schedulerConnection) throws Exception {

		StringBuffer sql = new StringBuffer("from "+DBITEM_SCHEDULER_VARIABLES+" ")
		.append("where name = :name");
		
		Query q = schedulerConnection.createQuery(sql.toString());
		q.setParameter("name",TABLE_SCHEDULER_VARIABLES_REPORTING_VARIABLE);
		
		List<DBItemSchedulerVariableReporting> result = q.list();
		if(result.size() > 0){
			return result.get(0);
		}
		
		return null;
	}

	/**
	 * 
	 * @param schedulerConnection
	 * @param numericValue
	 * @param textValue
	 * @return
	 * @throws Exception
	 */
	public DBItemSchedulerVariableReporting createSchedulerVariable(
		SOSHibernateConnection schedulerConnection,
		Long numericValue,
		String textValue
			) throws Exception {

		DBItemSchedulerVariableReporting item = new DBItemSchedulerVariableReporting();
		item.setName(TABLE_SCHEDULER_VARIABLES_REPORTING_VARIABLE);
		item.setNumericValue(numericValue);
		item.setTextValue(textValue);
		
		schedulerConnection.save(item);
		return item;
	}

	/**
	 * 
	 * @param schedulerConnection
	 * @param item
	 * @throws Exception
	 */
	public void updateSchedulerVariable(
			SOSHibernateConnection schedulerConnection,
			DBItemSchedulerVariableReporting item
				) throws Exception {

		schedulerConnection.update(item);
	}
	
	/**
	 * 
	 * @param fieldName
	 * @return
	 */
	private String quote(String fieldName){
		return getConnection().quoteFieldName(fieldName);
	}
	
	/**
	 * 
	 * @param schedulerId
	 * @param name
	 * @return
	 * @throws Exception
	 */
	public String getInventoryJobChainStartCause(String schedulerId,String name) throws Exception{
		StringBuffer sql = new StringBuffer("select ijc.startCause from "+DBITEM_INVENTORY_JOB_CHAINS+" ijc,")
		.append(DBITEM_INVENTORY_INSTANCES+" ii ")
		.append("where ijc.name = :name ")
		.append("and ii.schedulerId = :schedulerId ")
	    .append("and ii.id = ijc.instanceId");
			
		Query q = getConnection().createQuery(sql.toString());
		q.setParameter("schedulerId",schedulerId);
		q.setParameter("name",name);
		
		return (String)q.uniqueResult();
	}
	
	/**
	 * 
	 * @param updateOnlyResultUncompletedEntries
	 * @return
	 * @throws Exception
	 */
	public int updateReportingExecutionFromInventory(boolean updateOnlyResultUncompletedEntries) throws Exception{
		String method = "updateReportingExecutionFromInventory";
		
		StringBuffer sql = null;
		int result = -1;
		Enum<SOSHibernateConnection.DBMS> dbms = getConnection().getDbms();
		
		try{
			//DB2 nicht getestet
			if(dbms.equals(DBMS.ORACLE) || dbms.equals(DBMS.DB2)){
				sql = new StringBuffer("update "+TABLE_REPORT_EXECUTIONS+" re ")
				.append("set (")
				.append(quote("re.TITLE"))
				.append(","+quote("re.IS_RUNTIME_DEFINED"))
				.append(") = (")
				.append("select ")
				.append(quote("ij.TITLE"))
				.append(","+quote("ij.IS_RUNTIME_DEFINED"))
				.append(" ")
				.append("from "+TABLE_INVENTORY_JOBS+" ij ")
				.append(","+TABLE_INVENTORY_INSTANCES+" ii ")
				.append(","+TABLE_REPORT_TRIGGERS+" rt ")
				.append("where "+quote("ij.INSTANCE_ID")+" = "+quote("ii.ID")+" ")
				.append("and "+quote("re.SCHEDULER_ID")+" = "+quote("ii.SCHEDULER_ID")+" ")
				.append("and "+quote("re.TRIGGER_ID")+" = "+quote("rt.ID")+" ")
				.append("and "+quote("re.NAME")+" = "+quote("ij.NAME")+" ");
				if(updateOnlyResultUncompletedEntries){
					sql.append(" ")
					.append("and "+quote("rt.RESULTS_COMPLETED")+" = 0");
				}
				sql.append(") ")
				.append("where exists(")
				.append("select ")
				.append(quote("ij.TITLE"))
				.append(","+quote("ij.IS_RUNTIME_DEFINED"))
				.append(" ")
				.append("from "+TABLE_INVENTORY_JOBS+" ij ")
				.append(","+TABLE_INVENTORY_INSTANCES+" ii ")
				.append(","+TABLE_REPORT_TRIGGERS+" rt ")
				.append("where "+quote("ij.INSTANCE_ID")+" = "+quote("ii.ID")+" ")
				.append("and "+quote("re.SCHEDULER_ID")+" = "+quote("ii.SCHEDULER_ID")+" ")
				.append("and "+quote("re.TRIGGER_ID")+" = "+quote("rt.ID")+" ")
				.append("and "+quote("re.NAME")+" = "+quote("ij.NAME")+" ");
				if(updateOnlyResultUncompletedEntries){
					sql.append(" ")
					.append("and "+quote("rt.RESULTS_COMPLETED")+" = 0");
				}
				sql.append(")");
				
				/**
				update REPORT_EXECUTIONS re 
				set ("TITLE","IS_RUNTIME_DEFINED") 
				= 
				(
				select 
				ij."TITLE",
				ij."IS_RUNTIME_DEFINED" 
				from INVENTORY_JOBS ij ,INVENTORY_INSTANCES ii ,REPORT_TRIGGERS rt 
				where ij."INSTANCE_ID" = ii."ID" 
				and re."SCHEDULER_ID" = ii."SCHEDULER_ID" 
				and re."TRIGGER_ID" = rt."ID" 
				and re."NAME" = ij."NAME" 
				and rt."RESULTS_COMPLETED" = 0
				)
				where exists
				(
				select rt."ID" from REPORT_TRIGGERS rt
				where re."TRIGGER_ID" = rt."ID" 
				and rt."RESULTS_COMPLETED" = 0
				)*/
			}
			else if(dbms.equals(DBMS.MSSQL)){
				sql = new StringBuffer("update "+TABLE_REPORT_EXECUTIONS+" ")
				.append("set "+quote(TABLE_REPORT_EXECUTIONS+".TITLE")+" = "+quote("ij.TITLE")+" ")
				.append(","+quote(TABLE_REPORT_EXECUTIONS+".IS_RUNTIME_DEFINED")+" = "+quote("ij.IS_RUNTIME_DEFINED")+" ")
				.append("from "+TABLE_REPORT_EXECUTIONS+" re ")
				.append(","+TABLE_INVENTORY_JOBS+" ij ")
				.append(","+TABLE_INVENTORY_INSTANCES+" ii ")
				.append(","+TABLE_REPORT_TRIGGERS+" rt ")
				.append("where "+quote("ij.INSTANCE_ID")+" = "+quote("ii.ID")+" ")
				.append("and "+quote("re.SCHEDULER_ID")+" = "+quote("ii.SCHEDULER_ID")+" ")
				.append("and "+quote("re.TRIGGER_ID")+" = "+quote("rt.ID")+" ")
				.append("and "+quote("re.NAME")+" = "+quote("ij.NAME")+" ");
				if(updateOnlyResultUncompletedEntries){
					sql.append("and "+quote("rt.RESULTS_COMPLETED")+" = 0");
				}
			}
			else if(dbms.equals(DBMS.MYSQL)){
				sql = new StringBuffer("update "+TABLE_REPORT_EXECUTIONS+" re ")
				.append(","+TABLE_INVENTORY_JOBS+" ij ")
				.append(","+TABLE_INVENTORY_INSTANCES+" ii ")
				.append(","+TABLE_REPORT_TRIGGERS+" rt ")
				.append("set "+quote("re.TITLE")+" = "+quote("ij.TITLE")+" ")
				.append(","+quote("re.IS_RUNTIME_DEFINED")+" = "+quote("ij.IS_RUNTIME_DEFINED")+" ")
				.append("where "+quote("ij.INSTANCE_ID")+" = "+quote("ii.ID")+" ")
				.append("and "+quote("re.SCHEDULER_ID")+" = "+quote("ii.SCHEDULER_ID")+" ")
				.append("and "+quote("re.TRIGGER_ID")+" = "+quote("rt.ID")+" ")
				.append("and "+quote("re.NAME")+" = "+quote("ij.NAME")+" ");
				if(updateOnlyResultUncompletedEntries){
					sql.append("and "+quote("rt.RESULTS_COMPLETED")+" = 0");
				}
			}
			else if(dbms.equals(DBMS.PGSQL) || dbms.equals(DBMS.SYBASE)){
				sql = new StringBuffer("update "+TABLE_REPORT_EXECUTIONS+" ")
				.append("set "+quote("TITLE")+" = "+quote("ij.TITLE")+" ")
				.append(","+quote("IS_RUNTIME_DEFINED")+" = "+quote("ij.IS_RUNTIME_DEFINED")+" ")
				.append("from "+TABLE_INVENTORY_JOBS+" ij ")
				.append(","+TABLE_INVENTORY_INSTANCES+" ii ")
				.append(","+TABLE_REPORT_TRIGGERS+" rt ")
				.append("where "+quote("ij.INSTANCE_ID")+" = "+quote("ii.ID")+" ")
				.append("and "+quote(TABLE_REPORT_EXECUTIONS+".SCHEDULER_ID")+" = "+quote("ii.SCHEDULER_ID")+" ")
				.append("and "+quote(TABLE_REPORT_EXECUTIONS+".TRIGGER_ID")+" = "+quote("rt.ID")+" ")
				.append("and "+quote(TABLE_REPORT_EXECUTIONS+".NAME")+" = "+quote("ij.NAME")+" ");
				if(updateOnlyResultUncompletedEntries){
					sql.append("and "+quote("rt.RESULTS_COMPLETED")+" = 0");
				}
				/**
				update REPORT_EXECUTIONS
				set "TITLE" = ij."TITLE"
				,"IS_RUNTIME_DEFINED" = ij."IS_RUNTIME_DEFINED"
				from INVENTORY_JOBS ij
				,INVENTORY_INSTANCES ii
				,REPORT_TRIGGERS rt
				where ij."INSTANCE_ID" = ii."ID"
				and REPORT_EXECUTIONS."SCHEDULER_ID" = ii."SCHEDULER_ID"
				and REPORT_EXECUTIONS."TRIGGER_ID" = rt."ID"
				and REPORT_EXECUTIONS."NAME" = ij."NAME"
				and rt."RESULTS_COMPLETED" = 0
				*/
			}
			else{
				logger.warn(String.format("%s: not implemented for connection %s ",
						method,
						dbms.name()));
			}
			
			if(sql != null){
				result = getConnection().createSQLQuery(sql.toString()).executeUpdate();
			}
		}
		catch(Exception ex){
			throw new Exception(String.format("%s: sql = %s, exception = %s", 
						method,sql,getException(ex).toString()));
		}
		
	return result;
	}
	
	/**
	 * 
	 * @param updateOnlyResultUncompletedEntries
	 * @return
	 * @throws Exception
	 */
	public int updateReportingTriggerFromInventory(boolean updateOnlyResultUncompletedEntries) throws Exception{
		String method = "updateReportingTriggerFromInventory";
		
		StringBuffer sql = null;
		int result = -1;
		Enum<SOSHibernateConnection.DBMS> dbms = getConnection().getDbms();
		
		try{
			if(dbms.equals(DBMS.ORACLE) || dbms.equals(DBMS.DB2)){
				sql = new StringBuffer("update "+TABLE_REPORT_TRIGGERS+" rt ")
				.append("set (")
				.append(quote("rt.TITLE"))
				.append(","+quote("rt.PARENT_TITLE"))
				.append(","+quote("rt.IS_RUNTIME_DEFINED"))
				.append(") = (")
				.append("select ")
				.append(quote("io.TITLE"))
				.append(","+quote("ijc.TITLE"))
				.append(","+quote("io.IS_RUNTIME_DEFINED"))
				.append(" ")
				.append("from "+TABLE_INVENTORY_ORDERS+" io")
				.append(", "+TABLE_INVENTORY_JOB_CHAINS+" ijc")
				.append(", "+TABLE_INVENTORY_INSTANCES+" ii ")
				.append("where "+quote("io.INSTANCE_ID")+" = "+quote("ii.ID")+" ")
				.append("and "+quote("ijc.INSTANCE_ID")+" = "+quote("ii.ID")+" ")
				.append("and "+quote("rt.SCHEDULER_ID")+" = "+quote("ii.SCHEDULER_ID")+" ")
				.append("and "+quote("rt.NAME")+" = "+quote("io.ORDER_ID")+" ")
				.append("and "+quote("rt.PARENT_NAME")+" = "+quote("ijc.NAME")+" ")
				.append(")")
				.append("where exists(")
				.append("select ")
				.append(quote("io.TITLE"))
				.append(","+quote("ijc.TITLE"))
				.append(","+quote("io.IS_RUNTIME_DEFINED"))
				.append(" ")
				.append("from "+TABLE_INVENTORY_ORDERS+" io")
				.append(", "+TABLE_INVENTORY_JOB_CHAINS+" ijc")
				.append(", "+TABLE_INVENTORY_INSTANCES+" ii ")
				.append("where "+quote("io.INSTANCE_ID")+" = "+quote("ii.ID")+" ")
				.append("and "+quote("ijc.INSTANCE_ID")+" = "+quote("ii.ID")+" ")
				.append("and "+quote("rt.SCHEDULER_ID")+" = "+quote("ii.SCHEDULER_ID")+" ")
				.append("and "+quote("rt.NAME")+" = "+quote("io.ORDER_ID")+" ")
				.append("and "+quote("rt.PARENT_NAME")+" = "+quote("ijc.NAME")+" ")
				.append(")");
				if(updateOnlyResultUncompletedEntries){
					sql.append(" ")
					.append("and "+quote("rt.RESULTS_COMPLETED")+" = 0");
				}
			}
			else if(dbms.equals(DBMS.MSSQL)){
				sql = new StringBuffer("update "+TABLE_REPORT_TRIGGERS+" ")
				.append("set ") 
				.append(quote(TABLE_REPORT_TRIGGERS+".TITLE")+" = "+quote("io.TITLE")+" ")
				.append(","+quote(TABLE_REPORT_TRIGGERS+".PARENT_TITLE")+" = "+quote("ijc.TITLE")+" ")
				.append(","+quote(TABLE_REPORT_TRIGGERS+".IS_RUNTIME_DEFINED")+" = "+quote("io.IS_RUNTIME_DEFINED")+" ")
				.append("from "+TABLE_REPORT_TRIGGERS+" rt ")
				.append(","+TABLE_INVENTORY_ORDERS+" io ")
				.append(","+TABLE_INVENTORY_JOB_CHAINS+" ijc ")
				.append(","+TABLE_INVENTORY_INSTANCES+" ii ")
				.append("where "+quote("io.INSTANCE_ID")+" = "+quote("ii.ID")+" ")
				.append("and "+quote("ijc.INSTANCE_ID")+" = "+quote("ii.ID")+" ")
				.append("and "+quote("rt.SCHEDULER_ID")+" = "+quote("ii.SCHEDULER_ID")+" ")
				.append("and "+quote("rt.NAME")+" = "+quote("io.ORDER_ID")+" ")
				.append("and "+quote("rt.PARENT_NAME")+" = "+quote("ijc.NAME")+" ");
				if(updateOnlyResultUncompletedEntries){
					sql.append("and "+quote("rt.RESULTS_COMPLETED")+" = 0");
				}
				/**
				update REPORT_TRIGGERS
				set 
				REPORT_TRIGGERS."TITLE" = io."TITLE"
				,REPORT_TRIGGERS."PARENT_TITLE" = ijc."TITLE"
				,REPORT_TRIGGERS."IS_RUNTIME_DEFINED" = io."IS_RUNTIME_DEFINED"
				from REPORT_TRIGGERS rt,INVENTORY_ORDERS io
				,INVENTORY_JOB_CHAINS ijc
				,INVENTORY_INSTANCES ii
				where io."INSTANCE_ID" = ii."ID"
				and ijc."INSTANCE_ID" = ii."ID"
				and rt."SCHEDULER_ID" = ii."SCHEDULER_ID"
				and rt."NAME" = io."ORDER_ID"
				and rt."PARENT_NAME" = ijc."NAME"
				and rt."RESULTS_COMPLETED" = 0*/
			}
			else if(dbms.equals(DBMS.MYSQL)){
				sql = new StringBuffer("update "+TABLE_REPORT_TRIGGERS+" rt ")
				.append(","+TABLE_INVENTORY_ORDERS+" io ")
				.append(","+TABLE_INVENTORY_JOB_CHAINS+" ijc ")
				.append(","+TABLE_INVENTORY_INSTANCES+" ii ")
				.append("set ") 
				.append(quote("rt.TITLE")+" = "+quote("io.TITLE")+" ")
				.append(","+quote("rt.PARENT_TITLE")+" = "+quote("ijc.TITLE")+" ")
				.append(","+quote("rt.IS_RUNTIME_DEFINED")+" = "+quote("io.IS_RUNTIME_DEFINED")+" ")
				.append("where "+quote("io.INSTANCE_ID")+" = "+quote("ii.ID")+" ")
				.append("and "+quote("ijc.INSTANCE_ID")+" = "+quote("ii.ID")+" ")
				.append("and "+quote("rt.SCHEDULER_ID")+" = "+quote("ii.SCHEDULER_ID")+" ")
				.append("and "+quote("rt.NAME")+" = "+quote("io.ORDER_ID")+" ")
				.append("and "+quote("rt.PARENT_NAME")+" = "+quote("ijc.NAME")+" ");
				if(updateOnlyResultUncompletedEntries){
					sql.append("and "+quote("rt.RESULTS_COMPLETED")+" = 0");
				}
				/**
				update REPORT_TRIGGERS rt
				,INVENTORY_ORDERS io
				,INVENTORY_JOB_CHAINS ijc
				,INVENTORY_INSTANCES ii
				set 
				rt."TITLE" = io."TITLE"
				,rt."PARENT_TITLE" = ijc."TITLE"
				,rt."IS_RUNTIME_DEFINED" = io."IS_RUNTIME_DEFINED"
				where io."INSTANCE_ID" = ii."ID"
				and ijc."INSTANCE_ID" = ii."ID"
				and rt."SCHEDULER_ID" = ii."SCHEDULER_ID"
				and rt."NAME" = io."ORDER_ID"
				and rt."PARENT_NAME" = ijc."NAME"
				and rt."RESULTS_COMPLETED" = 0
				*/
			}
			else if(dbms.equals(DBMS.PGSQL) || dbms.equals(DBMS.SYBASE)){
				sql = new StringBuffer("update "+TABLE_REPORT_TRIGGERS+" ")
				.append("set ") 
				.append(quote("TITLE")+" = "+quote("io.TITLE")+" ")
				.append(","+quote("PARENT_TITLE")+" = "+quote("ijc.TITLE")+" ")
				.append(","+quote("IS_RUNTIME_DEFINED")+" = "+quote("io.IS_RUNTIME_DEFINED")+" ")
				.append("from "+TABLE_INVENTORY_ORDERS+" io ")
				.append(","+TABLE_INVENTORY_JOB_CHAINS+" ijc ")
				.append(","+TABLE_INVENTORY_INSTANCES+" ii ")
				.append("where "+quote("io.INSTANCE_ID")+" = "+quote("ii.ID")+" ")
				.append("and "+quote("ijc.INSTANCE_ID")+" = "+quote("ii.ID")+" ")
				.append("and "+quote(TABLE_REPORT_TRIGGERS+".SCHEDULER_ID")+" = "+quote("ii.SCHEDULER_ID")+" ")
				.append("and "+quote(TABLE_REPORT_TRIGGERS+".NAME")+" = "+quote("io.ORDER_ID")+" ")
				.append("and "+quote(TABLE_REPORT_TRIGGERS+".PARENT_NAME")+" = "+quote("ijc.NAME")+" ");
				if(updateOnlyResultUncompletedEntries){
					sql.append("and "+quote(TABLE_REPORT_TRIGGERS+".RESULTS_COMPLETED")+" = 0");
				}
				/**
				update REPORT_TRIGGERS
				set 
				"TITLE" = io."TITLE"
				,"PARENT_TITLE" = ijc."TITLE"
				,"IS_RUNTIME_DEFINED" = io."IS_RUNTIME_DEFINED"
				from INVENTORY_ORDERS io
				,INVENTORY_JOB_CHAINS ijc
				,INVENTORY_INSTANCES ii
				where io."INSTANCE_ID" = ii."ID"
				and ijc."INSTANCE_ID" = ii."ID"
				and REPORT_TRIGGERS."SCHEDULER_ID" = ii."SCHEDULER_ID"
				and REPORT_TRIGGERS."NAME" = io."ORDER_ID"
				and REPORT_TRIGGERS."PARENT_NAME" = ijc."NAME"
				and REPORT_TRIGGERS."RESULTS_COMPLETED" = 0
				*/
			}
			else{
				logger.warn(String.format("%s: not implemented for connection %s ",
						method,
						dbms.name()));
			}
		
			if(sql != null){
				result = getConnection().createSQLQuery(sql.toString()).executeUpdate();
			}
		
		}
		catch(Exception ex){
			throw new Exception(String.format("%s: sql = %s, exception = %s", 
						method,sql,getException(ex).toString()));
		}
		
		return result;
	}
	
	/**
	 * 
	 * @param ex
	 * @return
	 */
	private Throwable getException(Throwable ex){
		Throwable cause = ex.getCause();
		return cause == null ? ex : cause;
	}
	
	/**
	 * 
	 * @param triggerId
	 * @return
	 * @throws Exception
	 */
	public Criteria getReportExecutions(Long triggerId) throws Exception{
		Criteria cr = getConnection().createTransform2BeanCriteria(DBItemReportExecution.class);
		cr.add(Restrictions.eq("triggerId",triggerId));
		cr.setReadOnly(true);
		
		return cr;
	}
	
	public Query getReportExecutionsX(Long triggerId) throws Exception{
		
		StringBuffer sql = new StringBuffer("from "+DBITEM_REPORT_EXECUTIONS+" ")
		.append("where triggerId = :triggerId");
		
		
		Query q = getConnection().createQuery(sql.toString());
		q.setParameter("triggerId",triggerId);
		q.setReadOnly(true);
		
		return q;
	}
	
	/**
	 * 
	 * @return
	 * @throws Exception
	 */
	public int triggerResultCompletedQuery() throws Exception{
		StringBuffer sql = new StringBuffer("update "+DBITEM_REPORT_TRIGGERS+" ")
		.append("set resultsCompleted = true ")
		.append("where resultsCompleted = false ")
		.append("and syncCompleted = true");
		return getConnection().createQuery(sql.toString()).executeUpdate();
	}
	
	/**
	 * 
	 * @return
	 * @throws Exception
	 */
	public Criteria getResultUncompletedTriggersCriteria() throws Exception{
		String[] fields =  new String[]{"id","schedulerId","historyId","parentName","startTime","endTime"};
		Criteria cr = getConnection().createCriteria(DBItemReportTrigger.class,fields);
		cr.add(Restrictions.eq("resultsCompleted",false));
		cr.setReadOnly(true);
		return cr;
	}
	
	/**
	 * 
	 * @return
	 * @throws Exception
	 */
	public Criteria getResultUncompletedTriggerExecutionsCriteria(Long triggerId) throws Exception{
		String[] fields =  new String[]{"id","schedulerId","historyId","triggerId","step","name","startTime","endTime","state","cause","error","errorCode","errorText"};
		Criteria cr = getConnection().createCriteria(DBItemReportExecution.class,fields);
		cr.add(Restrictions.eq("triggerId",triggerId));
		cr.setReadOnly(true);
		return cr;
	}
	
	/**
	 * 
	 * @param schedulerConnection
	 * @param dateFrom
	 * @param dateTo
	 * @param historyIds
	 * @return
	 * @throws Exception
	 */
	public Criteria getSchedulerHistorySteps(SOSHibernateConnection schedulerConnection, Date dateFrom, Date dateTo, ArrayList<Long> historyIds) throws Exception{
		Criteria cr = schedulerConnection.createCriteria(SchedulerOrderStepHistoryDBItem.class,"osh");
		//join
		cr.createAlias("osh.schedulerOrderHistoryDBItem","oh");
		cr.createAlias("osh.schedulerTaskHistoryDBItem","h");
		
		ProjectionList pl = Projections.projectionList();
		//select field list osh
		pl.add(Projections.property("osh.id.step").as("stepStep"));
		pl.add(Projections.property("osh.id.historyId").as("stepHistoryId"));
		pl.add(Projections.property("osh.taskId").as("stepTaskId"));
		pl.add(Projections.property("osh.startTime").as("stepStartTime"));
		pl.add(Projections.property("osh.endTime").as("stepEndTime"));
		pl.add(Projections.property("osh.state").as("stepState"));
		pl.add(Projections.property("osh.error").as("stepError"));
		pl.add(Projections.property("osh.errorCode").as("stepErrorCode"));
		pl.add(Projections.property("osh.errorText").as("stepErrorText"));
		//select field list oh
		pl.add(Projections.property("oh.historyId").as("orderHistoryId"));
		pl.add(Projections.property("oh.spoolerId").as("orderSchedulerId"));
		pl.add(Projections.property("oh.orderId").as("orderId"));
		pl.add(Projections.property("oh.jobChain").as("orderJobChain"));
		pl.add(Projections.property("oh.state").as("orderState"));
		pl.add(Projections.property("oh.stateText").as("orderStateText"));
		pl.add(Projections.property("oh.startTime").as("orderStartTime"));
		pl.add(Projections.property("oh.endTime").as("orderEndTime"));
		//select field list h
		pl.add(Projections.property("h.id").as("taskId"));
		pl.add(Projections.property("h.jobName").as("taskJobName"));
		pl.add(Projections.property("h.cause").as("taskCause"));
		cr.setProjection(pl);
	
		//where
		if(dateTo != null){
			cr.add(Restrictions.le("oh.startTime",dateTo));
			if(dateFrom != null){
				cr.add(Restrictions.ge("oh.startTime",dateFrom));
			}	
		}
		else if(historyIds != null){
			cr.add(Restrictions.in("oh.historyId", historyIds));
		}
				
		cr.setResultTransformer(Transformers.aliasToBean(DBItemSchedulerHistoryOrderStepReporting.class));
		cr.setReadOnly(true);
		
		return cr;
	}
}
