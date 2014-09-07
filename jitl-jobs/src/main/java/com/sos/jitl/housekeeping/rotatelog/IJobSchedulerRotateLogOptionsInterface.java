package com.sos.jitl.housekeeping.rotatelog;
import com.sos.JSHelper.Options.SOSOptionFolderName;
import com.sos.JSHelper.Options.SOSOptionRegExp;
import com.sos.JSHelper.Options.SOSOptionTime;

/**
* \interface IJobSchedulerRotateLogOptionsInterface - Interface for Rotate compress and delete log files
*
* \brief
*
* 
*
* see \see C:\Users\KB\AppData\Local\Temp\scheduler_editor-2994573971419391617html for (more) details.
*
* \verbatim ;
* mechanicaly created by com/sos/resources/xsl/JSJobDoc2JSOptionInterface.xsl	from http://www.sos-berlin.com at 20140906124600
* \endverbatim
* */
public interface IJobSchedulerRotateLogOptionsInterface {

	/**
	* \brief getdelete_file_age:
	*
	* \details
	*
	This parameter determines the minimum age at which archived files will be deleted. All files with names that follow the pattern scheduler-[yyyy-mm-dd-hhMMss].[schedulerId].log.gz and which are at least delete_file_age days old will be deleted. The value 0 means "Do not delete" and is the default value when this parameter is not specified.
	*
	* \return 
	*
	*/
	public abstract SOSOptionTime delete_file_age();

	/**
	* \brief set				delete_file_age			:			
	*
	* \details
	* This parameter determines the minimum age at which archived files will be deleted. All files with names that follow the pattern scheduler-[yyyy-mm-dd-hhMMss].[schedulerId].log.gz and which are at least delete_file_age days old will be deleted. The value 0 means "Do not delete" and is the default value when this parameter is not specified.
	*
	* @param				delete_file_age : 
	*/
	public abstract void delete_file_age(SOSOptionTime p_delete_file_age);

	/**
	* \brief getdelete_file_specification:
	*
	* \details
	*
	This value of this parameter specifies a regular expression for the log files of the JobScheduler which will be deleted. Changing the default value of this regular expression allows, for example, the log files for a specific JobScheduler to be deleted, should multiple JobSchedulers be logging into the same directory. Note that log files are named according to the pattern scheduler_yyyy-mm-dd-hhmmss.<scheduler_id>.log , where <scheduler_id> is an identifier defined in the JobScheduler XML configuration file.
	*
	* \return 
	*
	*/
	public abstract SOSOptionRegExp delete_file_specification();

	/**
	* \brief set				delete_file_specification			:			
	*
	* \details
	* This value of this parameter specifies a regular expression for the log files of the JobScheduler which will be deleted. Changing the default value of this regular expression allows, for example, the log files for a specific JobScheduler to be deleted, should multiple JobSchedulers be logging into the same directory. Note that log files are named according to the pattern scheduler_yyyy-mm-dd-hhmmss.<scheduler_id>.log , where <scheduler_id> is an identifier defined in the JobScheduler XML configuration file.
	*
	* @param				delete_file_specification : 
	*/
	public abstract void delete_file_specification(SOSOptionRegExp p_delete_file_specification);

	/**
	* \brief getfile_age:
	*
	* \details
	*
	This parameter determines the minimum age at which files will be compressed and saved as archives. All files with names following the pattern scheduler-[yyyy-mm-dd-hhMMss].[schedulerId].log and which are at least file_age days old will be compressed.
	*
	* \return 
	*
	*/
	public abstract SOSOptionTime file_age();

	/**
	* \brief set				file_age			:			
	*
	* \details
	* This parameter determines the minimum age at which files will be compressed and saved as archives. All files with names following the pattern scheduler-[yyyy-mm-dd-hhMMss].[schedulerId].log and which are at least file_age days old will be compressed.
	*
	* @param				file_age : 
	*/
	public abstract void file_age(SOSOptionTime p_file_age);

	/**
	* \brief getfile_path:
	*
	* \details
	*
	This parameter specifies a directory for the JobScheduler log files. If this parameter is not specified, then the current log directory of the JobScheduler will be used.
	*
	* \return 
	*
	*/
	public abstract SOSOptionFolderName file_path();

	/**
	* \brief set				file_path			:			
	*
	* \details
	* This parameter specifies a directory for the JobScheduler log files. If this parameter is not specified, then the current log directory of the JobScheduler will be used.
	*
	* @param				file_path : 
	*/
	public abstract void file_path(SOSOptionFolderName p_file_path);

	/**
	* \brief getfile_specification:
	*
	* \details
	*
	This parameter specifies a regular expression for the log files of the JobScheduler. Changing the default value of this regular expression allows, for example, the log files for a specific JobScheduler to be rotated, should multiple JobSchedulers be logging into the same directory. Note that log files are named according to the pattern scheduler_yyyy-mm-dd-hhmmss.<scheduler_id>.log , where <scheduler_id> is an identifier defined in the JobScheduler XML configuration file.
	*
	* \return 
	*
	*/
	public abstract SOSOptionRegExp file_specification();

	/**
	* \brief set				file_specification			:			
	*
	* \details
	* This parameter specifies a regular expression for the log files of the JobScheduler. Changing the default value of this regular expression allows, for example, the log files for a specific JobScheduler to be rotated, should multiple JobSchedulers be logging into the same directory. Note that log files are named according to the pattern scheduler_yyyy-mm-dd-hhmmss.<scheduler_id>.log , where <scheduler_id> is an identifier defined in the JobScheduler XML configuration file.
	*
	* @param				file_specification : 
	*/
	public abstract void file_specification(SOSOptionRegExp p_file_specification);

} // public interface		IJobSchedulerRotateLogOptionsInterface