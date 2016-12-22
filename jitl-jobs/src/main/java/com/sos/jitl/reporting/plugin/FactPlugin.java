package com.sos.jitl.reporting.plugin;

import java.sql.Connection;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.hibernate.classes.SOSHibernateConnection;
import com.sos.jitl.reporting.db.DBLayer;
import com.sos.scheduler.engine.kernel.scheduler.SchedulerXmlCommandExecutor;
import com.sos.scheduler.engine.kernel.variable.VariableSet;

public class FactPlugin extends ReportingPlugin {

    private static final Logger LOGGER = LoggerFactory.getLogger(FactPlugin.class);
    
    @Inject
    public FactPlugin(SchedulerXmlCommandExecutor xmlCommandExecutor, VariableSet variables){
      	super(xmlCommandExecutor,variables);    	    	
    }

    @Override
    public void onPrepare(){
    	try{
    		SOSHibernateConnection connection = new SOSHibernateConnection();
    		connection.setAutoCommit(true);
            connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            connection.setIgnoreAutoCommitTransactions(true);
            connection.addClassMapping(DBLayer.getReportingClassMapping());
            
            FactEventHandler handler = new FactEventHandler();
            
    		super.executeOnPrepare(connection,handler);
    	}
    	catch(Exception ex){
    		
    	}
    }
    
    @Override
    public void onActivate() {
        try{
        	super.executeOnActivate();
        }
        catch(Exception ex){
    		
    	}
    }
    
    @Override
    public void close() {
        try{
        	super.executeClose();
        }
        catch(Exception ex){
    		
    	}
    }
  
}