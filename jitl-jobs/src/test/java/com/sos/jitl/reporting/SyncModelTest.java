package com.sos.jitl.reporting;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.SQLQuery;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.criterion.Restrictions;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.QueryParameters;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.engine.SessionImplementor;
import org.hibernate.impl.CriteriaImpl;
import org.hibernate.loader.criteria.CriteriaJoinWalker;
import org.hibernate.loader.criteria.CriteriaQueryTranslator;
import org.hibernate.persister.entity.OuterJoinLoadable;
import org.hibernate.type.Type;

import com.sos.hibernate.classes.SOSHibernateBatchProcessor;
import com.sos.hibernate.classes.SOSHibernateConnection;
import com.sos.hibernate.classes.SOSHibernateResultSetProcessor;
import com.sos.jitl.reporting.db.DBItemInventoryInstance;
import com.sos.jitl.reporting.db.DBItemReportExecution;
import com.sos.jitl.reporting.db.DBItemReportTrigger;
import com.sos.jitl.reporting.db.DBLayer;
import com.sos.jitl.reporting.helper.ReportUtil;
import com.sos.jitl.reporting.job.report.SyncJobOptions;
import com.sos.jitl.reporting.model.report.SyncModel;

public class SyncModelTest {

	private static Logger		logger			= Logger.getLogger(SyncModelTest.class);
	
	private SOSHibernateConnection reportingConnection;
	private SOSHibernateConnection schedulerConnection;
	
	private SyncJobOptions options;

	public SyncModelTest(SyncJobOptions opt) {
		this.options = opt; 
	}

	public void init() throws Exception {
		try{
			reportingConnection = new SOSHibernateConnection(options.hibernate_configuration_file.Value());
			reportingConnection.setConnectionIdentifier("reporting");
			reportingConnection.setAutoCommit(options.connection_autocommit.value());
			reportingConnection.setIgnoreAutoCommitTransactions(true);
			reportingConnection.setTransactionIsolation(options.connection_transaction_isolation.value());
			reportingConnection.setUseOpenStatelessSession(true);
			reportingConnection.addClassMapping(DBLayer.getInventoryClassMapping());
			reportingConnection.addClassMapping(DBLayer.getReportingClassMapping());
			reportingConnection.connect();
		}
		catch(Exception ex){
			throw new Exception(String.format("reporting connection: %s",
					ex.toString()));
		}
		
		try{
			schedulerConnection = new SOSHibernateConnection(options.hibernate_configuration_file_scheduler.Value());
			schedulerConnection.setConnectionIdentifier("scheduler");
			schedulerConnection.setAutoCommit(options.connection_autocommit_scheduler.value());
			schedulerConnection.setIgnoreAutoCommitTransactions(true);
			schedulerConnection.setTransactionIsolation(options.connection_transaction_isolation_scheduler.value());
			schedulerConnection.setUseOpenStatelessSession(true);
			schedulerConnection.addClassMapping(DBLayer.getSchedulerClassMapping());
			schedulerConnection.connect();
		}
		catch(Exception ex){
			throw new Exception(String.format("scheduler connection: %s",
					ex.toString()));
		}
		
	

	}

	public void exit() {
		if (reportingConnection!= null) {
			reportingConnection.disconnect();
		}
		if (schedulerConnection != null) {
			schedulerConnection.disconnect();
		}
	
	}
	
	public void testUtils() throws Exception{
		int a = ReportUtil.resolveAge2Minutes("10");
		System.out.println("AAAA = "+a);
	}
	
	public void testConnCreateCriteria() throws Exception{
		Criteria cr = reportingConnection.createCriteria(DBItemInventoryInstance.class);
		List<?> result = cr.list();
		for(Object o : result){
			DBItemInventoryInstance item = (DBItemInventoryInstance)o;
			logger.info("testConnCreateCriteria : "+item.getId()+" = "+item.getSchedulerId()+" "+item.getLiveDirectory());
		}
	}
	
	public void createSingleListCriteria() throws Exception{
		Criteria cr = reportingConnection.createSingleListCriteria(DBItemInventoryInstance.class,"schedulerId");
		List<?> result = cr.list();
		for(Object o : result){
			String schedulerId = (String)o;
			logger.info("createSingleListCriteria : "+schedulerId);
		}
	}
	
	public void createTransform2BeanCriteria() throws Exception{
		Criteria cr = reportingConnection.createTransform2BeanCriteria(DBItemInventoryInstance.class, new String[]{"schedulerId","liveDirectory"});
		List<?> result = cr.list();
		for(Object o : result){
			DBItemInventoryInstance item = (DBItemInventoryInstance)o;
			logger.info("createTransform2BeanCriteria : "+item.getSchedulerId());
		}
	}
	
	public void createCriteria() throws Exception{
		Criteria cr = reportingConnection.createCriteria(DBItemInventoryInstance.class, new String[]{"schedulerId","liveDirectory"});
		List<?> result = cr.list();
		for(Object o : result){
			Object[] ob = (Object[])o;
			String schedulerId = ob[0].toString();
			String liveDirectory = ob[1].toString();
			
			logger.info("createCriteria : "+schedulerId+" = "+liveDirectory);
		}
	}
	
	
	public void testConn() throws Exception{
		testConnCreateCriteria();
		createTransform2BeanCriteria();
		createSingleListCriteria();
		createCriteria();
		
		
		/**
		SQLQuery q = model.getDbLayer().getConnection().createSQLQuery("delete from report_triggers");
		
		model.getDbLayer().getConnection().beginTransaction();
		System.out.println("AAAAAAA  = "+q.executeUpdate());
		model.getDbLayer().getConnection().rollback();
		*/
		
		/**
		Date dateTo = ReportUtil.getCurrentDateTime();
		Date dateFrom = ReportUtil.getDateTimeMinusMinutes(dateTo,new Long(60));
		
		List<String> schedulerIds = new ArrayList();
		schedulerIds.add("123");
		schedulerIds.add("345");
		try{
			model.getDbLayer().getReportingConnection().beginTransaction();
			model.getDbLayer().removeReportingEntries(schedulerIds, dateFrom, dateTo);
			model.getDbLayer().getReportingConnection().commit();
		}
		catch(Exception ex){
			model.getDbLayer().getReportingConnection().rollback();
			throw ex;
		}*/
		//model.getXXX(dateFrom,dateTo);
		
		/**
		Criteria cr = model.getDbLayer().getSchedulerHistorySteps(dateFrom,dateTo,null);
		//cr = model.getDbLayer().getSchedulerHistorySteps(null,null,new Long[]{new Long(1),new Long(2)});
		List<?> result = cr.list();
		for(Object obj : result){
			DBItemSchedulerHistoryOrderStepReporting item = (DBItemSchedulerHistoryOrderStepReporting)obj;
			System.out.println("AAAA = "+item.getOrderId()+" = "+item.getStepHistoryId());
		}*/
		
		/**
		List<?> l = model.getDbLayer().getSchedulerInstancesSchedulerIds();
		for(Object sii : l){
			String si = (String)sii;
			System.out.println("AAAAAAAAAAAAAA = "+ si);
		}
		
		
		List<?> ll = model.getDbLayer().getUncomlitedReportTriggerHistoryIds(l);
		for(Object sii : ll){
			Long si = (Long)sii;
			System.out.println("BBBBBBBBBBB = "+ si);
		}
		*/
		
		
		
		//Connection c = imt.reportingConnection.getJdbcConnection();
		//imt.reportingConnection.logConfigurationProperties();
		//System.out.println("end: dateFrom = "+dateFrom+" dateTo = "+dateTo);
		
	}
	
	public void batchJdbc() throws Exception{
		Dialect d = reportingConnection.getDialect();
		
		/**
		Dialect dd = imt.schedulerConnection.getDialect();
		//System.out.println("AAAA = "+dd.getSequenceNextValString(DBLayer.SEQUENCE_INVENTORY_INSTANCES));
		*/
		String seq = d.getSequenceNextValString(DBLayer.TABLE_INVENTORY_INSTANCES_SEQUENCE).replace("select ","");
		
		StringBuffer sql = new StringBuffer();
		sql.append("insert into "+DBLayer.TABLE_INVENTORY_INSTANCES+" (ID,SCHEDULER_ID,HOSTNAME,PORT,LIVE_DIRECTORY,CREATED,MODIFIED)  ");
		sql.append("values ("+seq+",?,?,?,?,?,?)");
		PreparedStatement st = null;
		
		try{
			st = reportingConnection.jdbcConnectionPrepareStatement(sql.toString());
		
			for(int i=0;i<100;i++){
				if(i % 10 == 0){
					st.executeBatch();
				}
				
				st.setString(1,"xx");
				st.setString(2,"xx");
				st.setInt(3,i);
				st.setString(4,"xx");
				st.setDate(5,new java.sql.Date(0));
				st.setDate(6,new java.sql.Date(0));
				st.addBatch();
			}
			st.executeBatch();
		}
		catch(SQLException ex){
			throw new Exception(ex.getNextException());
		}
		finally{
			st.close();
		}
	}
	
	public void batchSOSHibernate() throws Exception{
		
		reportingConnection.beginTransaction();
		SOSHibernateBatchProcessor bp = new SOSHibernateBatchProcessor(reportingConnection);
		
		try{
			//bp.createInsertBatch(DBItemReportTriggerResults.class);
			
			/**
			DBItemReportTriggerResults item = new DBItemReportTriggerResults();
			
			item.setSchedulerId("x_");
			item.setHistoryId(new Long(1));
			item.setTriggerId(new Long(1));
			item.setStartCause("x");
			item.setSteps(new Long(1));
			item.setError(false);
			item.setCreated(ReportUtil.getCurrentDateTime());
			item.setModified(ReportUtil.getCurrentDateTime());
			bp.addBatch(item);
			*/
			bp.createInsertBatch(DBItemInventoryInstance.class);

			
			for(int i=0;i<2;i++){
				DBItemInventoryInstance item = new DBItemInventoryInstance();
				item.setSchedulerId("x_"+i);
				item.setHostname("xx_"+i);
				item.setPort(new Long(i));
				item.setLiveDirectory("x");
				item.setCreated(ReportUtil.getCurrentDateTime());
				item.setModified(ReportUtil.getCurrentDateTime());
				
				//reportingConnection.save(item);
				bp.addBatch(item);
			}
			int[] r = bp.executeBatch();
			
			reportingConnection.commit();
			//logger.info("aaaaaaaaaaaaaaa");
			//reportingConnection.getJdbcConnection();
			
		}
		catch(Exception ex){
			reportingConnection.rollback();
			throw ex;
		}
		finally{
			bp.close();
		}
		
		
	}
	public void batchHibernate() throws Exception{
		Dialect d = reportingConnection.getDialect();
		
		StringBuffer sql = new StringBuffer();
		sql.append("insert into "+DBLayer.TABLE_INVENTORY_INSTANCES+" (\"ID\",\"SCHEDULER_ID\",\"HOSTNAME\",\"PORT\",\"LIVE_DIRECTORY\",\"CREATED\",\"MODIFIED\")  ");
		sql.append("values ("+d.getSequenceNextValString(DBLayer.TABLE_INVENTORY_INSTANCES_SEQUENCE).replace("select ","")+",:schedulerId,:hostname,:port,:liveDirectory,:created,:modified)");
		
		
		SQLQuery q = reportingConnection.createSQLQuery(sql.toString());
		q.setParameter("schedulerId","xxx");
		q.setParameter("hostname","xxx");
		q.setParameter("port",new Long(1111));
		q.setParameter("liveDirectory","xxx");
		q.setTimestamp("created",ReportUtil.getCurrentDateTime());
		q.setTimestamp("modified",ReportUtil.getCurrentDateTime());
		
		q.executeUpdate();
		
		/**
		sql.append("insert into "+DBLayer.DBITEM_INVENTORY_INSTANCES+" ")
		.append("(id,schedulerId,hostname,port,liveDirectory,created,modified) ")
		.append("select ")
		.append(":id,:schedulerId,:hostname,:port,:liveDirectory,:created,:modified from "+DBLayer.DBITEM_INVENTORY_INSTANCES);
		
		Query q = model.getDbLayer().getConnection().createQuery(sql.toString());
		q.setParameter("id",new Long(0));
		q.setParameter("schedulerId","xxx");
		q.setParameter("hostname","xxx");
		q.setParameter("port",new Long(1111));
		q.setParameter("liveDirectory","xxx");
		q.setTimestamp("created",ReportUtil.getCurrentDateTime());
		q.setTimestamp("modified",ReportUtil.getCurrentDateTime());
		
		//q.executeUpdate();
		*/
		
		/**
		sql = new StringBuffer();
		sql.append("select :schedulerID from "+DBLayer.DBITEM_INVENTORY_INSTANCES+" c ");
		Query q = model.getDbLayer().getConnection().createQuery(sql.toString());
		
		q.setParameter("schedulerId","xxx");
		q.list();
		*/
	
		
	}
	
	public List<?> resultSet2Bean(ResultSet rs,Class<?> cls){       
        ArrayList<Object> lists = new ArrayList<Object>();     
        Object bean = null;      
        
        try {           
            ResultSetMetaData rsmd = rs.getMetaData();          
            int cols = rsmd.getColumnCount();           
            while(rs.next()){               
                bean = cls.newInstance();               
                for (int i = 0; i < cols; i++) {                 
                    String elename = rsmd.getColumnName(i+1);                   
                    Method getele = cls.getMethod("get"+elename);                   
                    Method setele = cls.getMethod("set" +elename,getele.getReturnType());                   
                    String value = rs.getString(i+1);                   
                    setele.invoke(bean, value);             
                }               
                lists.add(bean);            
            }       
        } 
        catch (Exception e) {         
            e.printStackTrace();        
        }       
        return lists;   
    }  
	
	public void testScrollJdbc() throws Exception{
		Statement st = null;
		ResultSet rs = null;
		
		Statement st2 = null;
		ResultSet rs2 = null;
		int total = 0;
		int total2 = 0;
		try{
			st = reportingConnection.getJdbcConnection().createStatement(ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
			rs = st.executeQuery("select ID from REPORT_TRIGGERS where RESULTS_COMPLETED = 0");
			rs.setFetchSize(Integer.MIN_VALUE);
			
			//st2 = reportingConnection.getJdbcConnection().prepareStatement("select ID,TRIGGER_ID from REPORT_EXECUTIONS where TRIGGER_ID = ?",ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
			
			while(rs.next()){
				total++;
				long id = rs.getLong(1);
				try{
					//st2.setLong(1,id);
					st2 = reportingConnection.getJdbcConnection().createStatement(ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
					rs2 = st2.executeQuery("select ID,TRIGGER_ID from REPORT_EXECUTIONS where TRIGGER_ID = "+id);
					while(rs2.next()){
						total2++;
						logger.info("   BBB = "+id+" = "+rs2.getLong(1)+" = "+rs2.getLong(2));
					}
				}
				catch(Exception ex){
					throw ex;
				}
				finally{
					if(rs2!= null)rs2.close();
					if(st2!= null)st2.close();
				}
			}
		}
		catch(Exception ex){
			throw ex;
		}
		finally{
			if(rs!= null)rs.close();
			if(st!= null)st.close();
		}
		
		logger.info("AAAAAA "+total);
	}
	
	public void testSOSResultSetProcessor(SyncModel model) throws Exception{
		SOSHibernateResultSetProcessor rspTriggers = new SOSHibernateResultSetProcessor(model.getDbLayer().getConnection());
		SOSHibernateResultSetProcessor rspExecutions = new SOSHibernateResultSetProcessor(model.getDbLayer().getConnection());
		
		try{
			Criteria crTriggers = model.getDbLayer().getResultUncompletedTriggersCriteria();
			ResultSet rsTriggers = rspTriggers.createResultSet(crTriggers,ScrollMode.FORWARD_ONLY);
			
			while(rsTriggers.next()){
				DBItemReportTrigger item = (DBItemReportTrigger)rspTriggers.get();
				
				logger.info("XXX = "+item.getId()+" = "+item.getSchedulerId()+" = "+item.getHistoryId()+" = "+item.getSyncCompleted());
				logger.info("       "+item.getStartTime()+" = "+item.getEndTime());
				
				try{
					Criteria crExecutions = model.getDbLayer().getResultUncompletedTriggerExecutionsCriteria(item.getId());
					ResultSet rsExecutions = rspExecutions.createResultSet(crExecutions,ScrollMode.FORWARD_ONLY);
					while(rsExecutions.next()){
						DBItemReportExecution itemExecutions = (DBItemReportExecution)rspExecutions.get();
						logger.info("       YYYY = "+itemExecutions.getId()+" = "+itemExecutions.getTriggerId()+" = "+itemExecutions.getSchedulerId()+" = "+itemExecutions.getHistoryId());
										
					}
					
					
				}
				catch(Exception ex){
					throw ex;
				}
				finally{
					rspExecutions.close();
				}
				
			}
			
		}
		catch(Exception ex){
			throw ex;
		}
		finally{
			rspTriggers.close();
		}
		
	}
	
	public void testScrollJdbcTransform() throws Exception{
		Statement st = null;
		ResultSet rs = null;
		
		Statement st2 = null;
		ResultSet rs2 = null;
		int total = 0;
		int total2 = 0;
		int cols  = 0;
		try{
			st = reportingConnection.getJdbcConnection().createStatement(ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
			rs = st.executeQuery("select ID,START_TIME from REPORT_TRIGGERS where RESULTS_COMPLETED = 0 limit 10");
			rs.setFetchSize(Integer.MIN_VALUE);
			
			ResultSetMetaData md = rs.getMetaData();
			cols = md.getColumnCount();     
			ArrayList<String> ar = new ArrayList<String>();
			for (int i = 0; i < cols; i++) {                 
                ar.add(md.getColumnName(i+1));
			}
			
			Class<?> cls = DBItemReportTrigger.class;
			
			Method[] ms = cls.getDeclaredMethods();
		    
			
			//TODO Methods ausserhalb der Schleife berechnen und HashMap mit fieldName und setMethod liefern
			
			while(rs.next()){
				Object bean = cls.newInstance();     
				for (Method m : ms) {
	            	if(m.getName().startsWith("get")){
	            		Column c = m.getAnnotation(Column.class);
	            		if(c != null){
	            			String setterName = m.getName().replace("get","set");
	            		
	            			String fieldName = c.name().replaceAll("`","");
	            			if(ar.contains(fieldName)){
	            				logger.info("   AAAAAA "+fieldName+" = "+m.getReturnType()+" = "+setterName);
	            			
	            				Method setele = cls.getMethod(setterName,m.getReturnType());
	            				String value = "";
	            				if(m.getReturnType().equals(Long.class)){
	            					setele.invoke(bean,rs.getLong(fieldName)); 
	            				}
	            				else if(m.getReturnType().equals(Timestamp.class)){
	            					setele.invoke(bean,rs.getTimestamp(fieldName)); 
	            				}
	            				else if(m.getReturnType().equals(Date.class)){
	            					setele.invoke(bean,rs.getDate(fieldName)); 
	            				}
	            			//rs.getString(i+1);                   
	                        
	            			}
	            		
	            		}
	            	}
		    	}
				
				DBItemReportTrigger rt = (DBItemReportTrigger)bean;
				logger.info("ID = "+rt.getId()+" startTime = "+rt.getStartTime());
			}
			
		}
		catch(Exception ex){
			throw ex;
		}
		finally{
			if(rs!= null)rs.close();
			if(st!= null)st.close();
		}
		
		logger.info("AAAAAA "+total+" = "+cols);
	}
	
	
	public void testScrollJdbcPrepared() throws Exception{
		String sql ="select ID from REPORT_TRIGGERS where RESULTS_COMPLETED = 0";
		PreparedStatement st = reportingConnection.getJdbcConnection().prepareStatement(sql,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
		ResultSet rs = null;
		
		String sql2 = "select ID,TRIGGER_ID from REPORT_EXECUTIONS where TRIGGER_ID = ?";
		PreparedStatement st2 = reportingConnection.getJdbcConnection().prepareStatement(sql2,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
		ResultSet rs2 = null;
		
		int total = 0;
		int total2 = 0;
		
		try{
			rs = st.executeQuery();
			rs.setFetchSize(Integer.MIN_VALUE);
			
			//st2 = reportingConnection.getJdbcConnection().prepareStatement("select ID,TRIGGER_ID from REPORT_EXECUTIONS where TRIGGER_ID = ?",ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
			
			while(rs.next()){
				total++;
				long id = rs.getLong(1);
				try{
					st2.setLong(1,id);
					rs2 = st2.executeQuery();
					while(rs2.next()){
						total2++;
						logger.info("   BBB = "+id+" = "+rs2.getLong(1)+" = "+rs2.getLong(2));
					}
				}
				catch(Exception ex){
					throw ex;
				}
				finally{
					if(rs2!= null)rs2.close();
					if(st2!= null)st2.close();
				}
			}
		}
		catch(Exception ex){
			throw ex;
		}
		finally{
			if(rs!= null)rs.close();
			if(st!= null)st.close();
		}
		
		logger.info("AAAAAA "+total);
	}
	
	
	public void testScrollJdbcStatementAndPrepared() throws Exception{
		String sql ="select ID from REPORT_TRIGGERS where RESULTS_COMPLETED = 0";
		Statement st = reportingConnection.getJdbcConnection().createStatement(ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
		ResultSet rs = null;
		
		String sql2 = "select ID,TRIGGER_ID from REPORT_EXECUTIONS where TRIGGER_ID = ?";
		//PreparedStatement st2 = reportingConnection.getJdbcConnection().prepareStatement(sql2,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
		ResultSet rs2 = null;
		
		int total = 0;
		int total2 = 0;
		
		try{
			rs = st.executeQuery(sql);
			rs.setFetchSize(Integer.MIN_VALUE);
			
			//st2 = reportingConnection.getJdbcConnection().prepareStatement("select ID,TRIGGER_ID from REPORT_EXECUTIONS where TRIGGER_ID = ?",ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
			PreparedStatement st2 = reportingConnection.getJdbcConnection().prepareStatement(sql2,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
				
			while(rs.next()){
				total++;
				long id = rs.getLong(1);
				try{
					st2.setLong(1,id);
					rs2 = st2.executeQuery();
					while(rs2.next()){
						total2++;
						logger.info("   BBB = "+id+" = "+rs2.getLong(1)+" = "+rs2.getLong(2));
					}
				}
				catch(Exception ex){
					throw ex;
				}
				finally{
					if(rs2!= null)rs2.close();
					if(st2!= null)st2.close();
				}
			}
		}
		catch(Exception ex){
			throw ex;
		}
		finally{
			if(rs!= null)rs.close();
			if(st!= null)st.close();
		}
		
		logger.info("AAAAAA "+total);
	}
	
	public void testScroll(SyncModel model) throws Exception{
		
		model.getDbLayer().getConnection().beginTransaction();
		Criteria cr = model.getDbLayer().getResultUncompletedTriggersCriteria();
		ScrollableResults sr = cr.scroll(ScrollMode.FORWARD_ONLY);
		int countTotal = 0;
		
		try{
			while (sr.next()) {
				countTotal++;
				DBItemReportTrigger trigger = (DBItemReportTrigger) sr.get(0);
				if(trigger == null || trigger.getId() == null){
					throw new Exception("trigger or trigger.getId() is NULL");
				}
		
				//model.getDbLayer().getConnection().beginTransaction();
				Criteria q = model.getDbLayer().getReportExecutions(trigger.getId());
				ScrollableResults sr2 = q.scroll(ScrollMode.SCROLL_INSENSITIVE);
				while(sr2.next()){
					
				}
				sr2.close();
				//model.getDbLayer().getConnection().commit();
				//List<?> l = q.list(); 
				//srEx = crEx.scroll(ScrollMode.FORWARD_ONLY);
				//while (srEx.next()) {
				//for(Object obj : crEx.list()){
				//}
				
				//for(DBItemReportExecutions execution : trigger.getExecutions()){
				//}
			}
			model.getDbLayer().getConnection().commit();
			
		}
		catch(Exception ex){
			model.getDbLayer().getConnection().rollback();
			throw ex;
		}
		finally{
			sr.close();
		}
		
		logger.info("AAAAAA "+countTotal);
		
	}

	public void testScroll2(SyncModel model) throws Exception{
		
		model.getDbLayer().getConnection().beginTransaction();
		Criteria cr = model.getDbLayer().getResultUncompletedTriggersCriteria();
		ScrollableResults sr = cr.scroll(ScrollMode.FORWARD_ONLY);
		int countTotal = 0;
		
		try{
			while (sr.next()) {
				countTotal++;
				DBItemReportTrigger trigger = (DBItemReportTrigger) sr.get(0);
				if(trigger == null || trigger.getId() == null){
					throw new Exception("trigger or trigger.getId() is NULL");
				}
		
				SQLQuery q = model.getDbLayer().getConnection().createSQLQuery("select ID,TRIGGER_ID from REPORT_EXECUTIONS where TRIGGER_ID = 1");
				//q.list();
			}
			model.getDbLayer().getConnection().commit();
			
		}
		catch(Exception ex){
			model.getDbLayer().getConnection().rollback();
			throw ex;
		}
		finally{
			sr.close();
		}
		
		logger.info("AAAAAA "+countTotal);
		
	}

	public void criteria2sql() throws Exception{
		Criteria cr = reportingConnection.createCriteria(DBItemReportTrigger.class, new String[]{"id","startTime"});
		//Criteria cr = reportingConnection.createCriteria(DBItemReportTriggers.class,"re");
		
		/**
		ProjectionList pl = Projections.projectionList();
		//select field list osh
		pl.add(Projections.property("id").as("id"));
		pl.add(Projections.property("startTime").as("startTime"));
		cr.setProjection(pl);
		cr.setResultTransformer(AliasToEntityMapResultTransformer.INSTANCE);
		*/
		
		//Criterion cr2 = Restrictions.eq("resultsCompleted",false);
		cr.add(Restrictions.eq("resultsCompleted",false));
		cr.add(Restrictions.eq("name","1234"));
		cr.add(Restrictions.eq("historyId",new Long(5)));
		cr.add(Restrictions.eq("endTime",new Date()));
		
		
		String sql = reportingConnection.getSqlStringFromCriteria(cr);
		logger.info("XXXX = "+sql);
		
		CriteriaImpl criteriaImpl = (CriteriaImpl)cr;
		SessionImplementor session = criteriaImpl.getSession();
		SessionFactoryImplementor factory = session.getFactory();
		CriteriaQueryTranslator translator = new CriteriaQueryTranslator(factory,criteriaImpl,criteriaImpl.getEntityOrClassName(),CriteriaQueryTranslator.ROOT_SQL_ALIAS);
		
		String where = translator.getWhereCondition();
		
		
		String[] al = translator.getProjectedAliases();
		for(String a : al){
			logger.info("MM1 : "+a);
		}
		
		al = translator.getProjectedColumnAliases();
		for(String a : al){
			logger.info("MM2 : "+a);
		}
		
		logger.info("MM3 : "+translator.getColumn(cr,"startTime"));
		logger.info("MM4 : "+translator.getEntityName(cr,"startTime"));
		logger.info("MM5 : "+translator.getPropertyName("startTime"));
		
		/**
		BeanInfo info = Introspector.getBeanInfo(DBItemReportTriggers.class.getClass());
		PropertyDescriptor[] props = info.getPropertyDescriptors();
	    for(PropertyDescriptor prop : props){
	    	logger.info("MM6 : "+prop.getName()+" = "+prop.getReadMethod().getName());
	    }*/
		
	    Method getter = new PropertyDescriptor("startTime", DBItemReportTrigger.class).getReadMethod();
	    Method setter = new PropertyDescriptor("startTime", DBItemReportTrigger.class).getWriteMethod();
	    logger.info("MM6 : "+getter.getName()+" = "+setter.getName());
		    
	    
		/**
		int h = 0;
		StringBuffer whereNew = new StringBuffer();
		for (int index = 0; index < where.length(); index++) {
	        if (where.charAt(index) == '?') {
	        	++index;
	        	//whereNew.append(where.substring(0))
	        	logger.info("CCC = "+index+" = "+where.substring(0,index-1));
	        }
	     }
		*/
		logger.info("YYY = "+translator.getWhereCondition());
		QueryParameters qp = translator.getQueryParameters();
		logger.info("LLL = "+qp);
		
		Type[] types = qp.getPositionalParameterTypes();
		Object[] values = qp.getPositionalParameterValues();
		
		for(int i=0;i<values.length;i++){
			int index = where.indexOf("?");
			if(index > 0){
				String val = reportingConnection.quote(types[i],values[i]);
				
				where = where.replaceFirst("\\?",val);
				logger.info("ccc : "+where);
				logger.info(val);
			}
		}
		logger.info("cccX : "+where);
		
		String[] implementors = factory.getImplementors( criteriaImpl.getEntityOrClassName() );
		CriteriaJoinWalker walker = new CriteriaJoinWalker((OuterJoinLoadable)factory.getEntityPersister(implementors[0]), 
                translator,
                factory, 
                criteriaImpl, 
                criteriaImpl.getEntityOrClassName(), 
                session.getLoadQueryInfluencers());
		
		String newS = walker.getSQLString().replace(translator.getWhereCondition(),where);
		logger.info("cccX : "+newS);
		
	}
		
	
	public static void main(String[] args) throws Exception {

		String config = "D:/Arbeit/scheduler/jobscheduler_data/re-dell_4646_snap_1_8/config/";
		SyncJobOptions opt = new SyncJobOptions();
		
		opt.hibernate_configuration_file_scheduler.Value(config+"hibernate_reporting_scheduler.cfg.xml");
		opt.hibernate_configuration_file.Value(config+"hibernate_reporting.cfg.xml");
	
		
		opt.connection_autocommit.value(false);
		/**
		opt.connection_transaction_isolation.value(Connection.TRANSACTION_READ_COMMITTED);
		opt.connection_autocommit_scheduler.value(false);
		opt.connection_transaction_isolation_scheduler.value(Connection.TRANSACTION_READ_COMMITTED);
		*/
		
		String age = "199d";//60*24*1;
		opt.max_history_age.Value(age);
		//opt.max_uncompleted_age.value(-1);
		
		SyncModelTest imt = new SyncModelTest(opt);

		try {
			imt.init();

			SyncModel model = new SyncModel(
					imt.reportingConnection,
					imt.schedulerConnection,
					imt.options);
			
			//model.process();
			//imt.criteria2sql();
			imt.testSOSResultSetProcessor(model);
			
			//Map namedParams = translator.get.getNamedParams();
			
			//Iterator subcriterias = ((CriteriaImpl)cr).iterateSubcriteria();
			/**
			Iterator subcriterias = ((CriteriaImpl)cr).iterateExpressionEntries();
		    
			while ( subcriterias.hasNext() )
			 {
			 Criteria subcriteria = ( Criteria ) subcriterias.next();
			  logger.info("YYY : "+subcriteria.toString());
			 	if ( subcriteria.getAlias().equals( alias ) )
						            {
						                return subcriteria;
						            }
			 
			 }*/
			
			
			
			//imt.testScroll(model);
			//imt.testSOSResultSetProcessor();		
			//imt.testScrollJdbcStatementAndPrepared();
			
			//model.createReportingResults();
			//imt.testConn();
			//imt.batchJdbc();
			
			//imt.batchSOSHibernate();
			
			//DBItemInventoryInstances item = new DBItemInventoryInstances();
			//item.setSchedulerId("roberto");
			//imt.reportingConnection.addToBatch(null,item);
			
			/**
			Criteria cr = model.getDbLayer().getResultUncompletedTriggers();
			ScrollableResults sr = cr.scroll(ScrollMode.FORWARD_ONLY);
			//Query updateTriggerQuery = model.getDbLayer().createTriggerResultCompletedQuery();
			while (sr.next()) {
				DBItemReportTriggers trigger = (DBItemReportTriggers) sr.get(0);
			}*/
			
			
			/**
			Criteria cr = model.getDbLayer().getResultUncompletedTriggersX();
			ScrollableResults sr = cr.scroll(ScrollMode.FORWARD_ONLY);
			
				while (sr.next()) {
					DBItemReportTriggers trigger = (DBItemReportTriggers) sr
							.get(0);
			}*/
			
			
			System.out.println(String.format("scheduler: autoCommit = %s, configuredAutoCommit = %s",
					imt.schedulerConnection.getAutoCommit(),
					imt.schedulerConnection.getConfiguredAutoCommit()));
			
			System.out.println(String.format("scheduler: isolation = %s, configuredIsolation = %s",
					imt.schedulerConnection.getTransactionIsolation(),
					imt.schedulerConnection.getConfiguredTransactionIsolation()));
			
			System.out.println(String.format("reporting: autoCommit = %s, configuredAutoCommit = %s",
					imt.reportingConnection.getAutoCommit(),
					imt.reportingConnection.getConfiguredAutoCommit()));
			
			System.out.println(String.format("reporting: isolation = %s, configuredIsolation = %s",
					imt.reportingConnection.getTransactionIsolation(),
					imt.reportingConnection.getConfiguredTransactionIsolation()));
			
			System.out.println("end");
			
		} catch (Exception ex) {
			throw ex;
		} finally {
			imt.exit();
		}

	}
}
