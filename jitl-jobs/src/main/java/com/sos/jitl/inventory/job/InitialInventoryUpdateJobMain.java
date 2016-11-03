package com.sos.jitl.inventory.job;

import org.apache.log4j.Logger;
import com.sos.JSHelper.Basics.JSToolBox;

public class InitialInventoryUpdateJobMain extends JSToolBox {
	protected InitialInventoryUpdateJobOptions	objOptions = null;
	private static final Logger LOGGER = Logger.getLogger(InitialInventoryUpdateJobMain.class);
 
	public final static void main(String[] args) {
		LOGGER.info("InsertOrUpdateInventoryInstanceEntries - Main"); 
		try {
			InitialInventoryUpdateJob objM = new InitialInventoryUpdateJob();
			InitialInventoryUpdateJobOptions objO = objM.getOptions();
			objO.commandLineArgs(args);
			objM.execute();
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			int intExitCode = 99;
			LOGGER.error(String.format("JSJ-E-105: main - terminated with exit-code %1$d", intExitCode), e);		
			System.exit(intExitCode);
		}
		LOGGER.info(String.format("JSJ-I-106: main - ended without errors"));		
	}

}