package com.sos.jitl.agentbatchinstaller;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import sos.scheduler.misc.ParameterSubstitutor;
import sos.spooler.Order;
import sos.spooler.Variable_set;

public class JSUniversalAgentBatchInstallerProcessTemplate {
	
	private HashMap<String,String> knownParameterNames;
	private static Logger logger = Logger.getLogger(JSUniversalAgentBatchInstallerProcessTemplate.class);

	

	public JSUniversalAgentBatchInstallerProcessTemplate() {
		super();
		knownParameterNames = new HashMap<String, String>();
		knownParameterNames.put("SCHEDULER_HOME", "");
		knownParameterNames.put("SCHEDULER_HTTP_PORT", "");
		knownParameterNames.put("SCHEDULER_USER", "");
		knownParameterNames.put("SCHEDULER_IP_ADDRESS", "");
		knownParameterNames.put("SCHEDULER_LOG_DIR", "");
		knownParameterNames.put("SCHEDULER_WORK_DIR", "");
		knownParameterNames.put("SCHEDULER_PID_FILE_DIR", "");
		knownParameterNames.put("SCHEDULER_KILL_SCRIPT", "");
		knownParameterNames.put("JAVA_HOME", "");
		knownParameterNames.put("JAVA_OPTIONS", "");
 	}

	public void execute(File fileIn, File fileOut,HashMap<String,String> subst){
		ParameterSubstitutor parameterSubstitutor = new ParameterSubstitutor();

		for (Entry<String, String> entry : subst.entrySet()) {	 
			String value = entry.getValue();
			String paramName = entry.getKey().toUpperCase();
			logger.debug("---->" + paramName + "=" + value);
			if (value.length() > 0){
				parameterSubstitutor.addKey(paramName + "_value",value);
				parameterSubstitutor.addKey(paramName,String.format("%s=%s",paramName,value));
			}else{
				parameterSubstitutor.addKey(paramName,String.format("#%s=",paramName));
			}
		}
		
		
		for (Entry<String, String> entry : knownParameterNames.entrySet()) {	
			String paramName = entry.getKey().toUpperCase();
			if (subst.get(paramName) == null){
				parameterSubstitutor.addKey(paramName,String.format("#%s=",paramName));				
			}
		}
		
		
		
		try {
			parameterSubstitutor.replaceInFile(fileIn, fileOut);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void execute(String fileIn, String fileOut,Order order) {
		HashMap <String,String> subst = new HashMap<String, String>();

		Variable_set params = order.params();
		String parameterNames = params.names();
		String keys[] = parameterNames.split(";");
		for (String paramName : keys) {
			if (paramName.startsWith("agent_options.")){
				String value = order.params().value(paramName);
				paramName = paramName.replaceFirst("^agent_options\\.(.*)$", "$1");
				paramName = paramName.toUpperCase();
				subst.put(paramName, value);
			}
			
		}
		execute(new File(fileIn), new File(fileOut),subst);
	}
	
}
