

package com.sos.jitl.housekeeping.rotatelog;

import static org.junit.Assert.assertEquals;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.sos.JSHelper.Basics.JSToolBox;
import com.sos.jitl.housekeeping.rotatelog.JobSchedulerRotateLog;
import com.sos.jitl.housekeeping.rotatelog.JobSchedulerRotateLogOptions;

/**
 * \class 		JobSchedulerRotateLogOptionsJUnitTest - Rotate compress and delete log files
 *
 * \brief 
 *
 *

 *
 * see \see C:\Users\KB\AppData\Local\Temp\scheduler_editor-1724231827372138737html for (more) details.
 * 
 * \verbatim ;
 * mechanicaly created by com/sos/resources/xsl/JSJobDoc2JSJUnitOptionSuperClass.xsl from http://www.sos-berlin.com at 20140906131052 
 * \endverbatim
 *
 * \section TestData Eine Hilfe zum Erzeugen einer HashMap mit Testdaten
 *
 * Die folgenden Methode kann verwendet werden, um für einen Test eine HashMap
 * mit sinnvollen Werten für die einzelnen Optionen zu erzeugen.
 *
 * \verbatim
 private HashMap <String, String> SetJobSchedulerSSHJobOptions (HashMap <String, String> pobjHM) {
	pobjHM.put ("		JobSchedulerRotateLogOptionsJUnitTest.auth_file", "test");  // This parameter specifies the path and name of a user's pr
		return pobjHM;
  }  //  private void SetJobSchedulerSSHJobOptions (HashMap <String, String> pobjHM)
 * \endverbatim
 */
public class JobSchedulerRotateLogOptionsJUnitTest extends  JSToolBox {
	private final String					conClassName						= "JobSchedulerRotateLogOptionsJUnitTest"; //$NON-NLS-1$
		@SuppressWarnings("unused") //$NON-NLS-1$
	private static Logger		logger			= Logger.getLogger(JobSchedulerRotateLogOptionsJUnitTest.class);
	private JobSchedulerRotateLog objE = null;

	protected JobSchedulerRotateLogOptions	objOptions			= null;

	public JobSchedulerRotateLogOptionsJUnitTest() {
		//
	}

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		objE = new JobSchedulerRotateLog();
		objE.registerMessageListener(this);
		objOptions = objE.Options();
		
	}

	@After
	public void tearDown() throws Exception {
	}


		

/**
 * \brief testdelete_file_age : 
 * 
 * \details
 * This parameter determines the minimum age at which archived files will be deleted. All files with names that follow the pattern scheduler-[yyyy-mm-dd-hhMMss].[schedulerId].log.gz and which are at least delete_file_age days old will be deleted. The value 0 means "Do not delete" and is the default value when this parameter is not specified.
 *
 */
    @Test
    public void testdelete_file_age() {  // SOSOptionTime
    	 objOptions.delete_file_age.Value("30");
    	 assertEquals ("", objOptions.delete_file_age.Value(),"30");
    	 assertEquals ("", objOptions.delete_file_age.getTimeAsSeconds(),30);
    	 objOptions.delete_file_age.Value("1:30");
    	 assertEquals ("", objOptions.delete_file_age.Value(),"1:30");
    	 assertEquals ("", objOptions.delete_file_age.getTimeAsSeconds(),90);
    	 objOptions.delete_file_age.Value("1:10:30");
    	 assertEquals ("", objOptions.delete_file_age.Value(),"1:10:30");
    	 assertEquals ("", objOptions.delete_file_age.getTimeAsSeconds(),30+10*60+60*60);
    	
    }

                

/**
 * \brief testdelete_file_specification : 
 * 
 * \details
 * This value of this parameter specifies a regular expression for the log files of the JobScheduler which will be deleted. Changing the default value of this regular expression allows, for example, the log files for a specific JobScheduler to be deleted, should multiple JobSchedulers be logging into the same directory. Note that log files are named according to the pattern scheduler_yyyy-mm-dd-hhmmss.<scheduler_id>.log , where <scheduler_id> is an identifier defined in the JobScheduler XML configuration file.
 *
 */
    @Test
    public void testdelete_file_specification() {  // SOSOptionRegExp
    	objOptions.delete_file_specification.Value("++^(scheduler)([0-9\\-]+).*(\\.log)(\\.gz)?$++");
    	assertEquals ("", objOptions.delete_file_specification.Value(),"++^(scheduler)([0-9\\-]+).*(\\.log)(\\.gz)?$++");
    	
    }

                

/**
 * \brief testfile_age : 
 * 
 * \details
 * This parameter determines the minimum age at which files will be compressed and saved as archives. All files with names following the pattern scheduler-[yyyy-mm-dd-hhMMss].[schedulerId].log and which are at least file_age days old will be compressed.
 *
 */
    @Test
    public void testfile_age() {  // SOSOptionTime
    	 objOptions.file_age.Value("30");
    	 assertEquals ("", objOptions.file_age.Value(),"30");
    	 assertEquals ("", objOptions.file_age.getTimeAsSeconds(),30);
    	 objOptions.file_age.Value("1:30");
    	 assertEquals ("", objOptions.file_age.Value(),"1:30");
    	 assertEquals ("", objOptions.file_age.getTimeAsSeconds(),90);
    	 objOptions.file_age.Value("1:10:30");
    	 assertEquals ("", objOptions.file_age.Value(),"1:10:30");
    	 assertEquals ("", objOptions.file_age.getTimeAsSeconds(),30+10*60+60*60);
    	
    }

                

/**
 * \brief testfile_path : 
 * 
 * \details
 * This parameter specifies a directory for the JobScheduler log files. If this parameter is not specified, then the current log directory of the JobScheduler will be used.
 *
 */
    @Test
    public void testfile_path() {  // SOSOptionFolderName
    	objOptions.file_path.Value("++./logs++");
    	assertEquals ("", objOptions.file_path.Value(),"++./logs++");
    	
    }

                

/**
 * \brief testfile_specification : 
 * 
 * \details
 * This parameter specifies a regular expression for the log files of the JobScheduler. Changing the default value of this regular expression allows, for example, the log files for a specific JobScheduler to be rotated, should multiple JobSchedulers be logging into the same directory. Note that log files are named according to the pattern scheduler_yyyy-mm-dd-hhmmss.<scheduler_id>.log , where <scheduler_id> is an identifier defined in the JobScheduler XML configuration file.
 *
 */
    @Test
    public void testfile_specification() {  // SOSOptionRegExp
    	objOptions.file_specification.Value("++^(scheduler).*([0-9\\-]+).*(\\.log)$++");
    	assertEquals ("", objOptions.file_specification.Value(),"++^(scheduler).*([0-9\\-]+).*(\\.log)$++");
    	
    }

                
        
} // public class JobSchedulerRotateLogOptionsJUnitTest