package sos.scheduler.db;

import static com.sos.scheduler.messages.JSMessages.JSJ_I_110;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Basics.JSJobUtilitiesClass;
import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.scheduler.messages.JSMessages;


public class JobSchedulerPLSQLJob extends JSJobUtilitiesClass <JobSchedulerPLSQLJobOptions> {
	protected static final String	conSettingDBMS_OUTPUT	= "dbmsOutput";
	private final String					conClassName		= "JobSchedulerPLSQLJob";						//$NON-NLS-1$
	private static Logger					logger				= Logger.getLogger(JobSchedulerPLSQLJob.class);

//	protected JobSchedulerPLSQLJobOptions	objOptions			= null;
//	private final JSJobUtilities			objJSJobUtilities	= this;

	private CallableStatement				cs					= null;
	private Connection						objConnection					= null;
	private DbmsOutput						dbmsOutput			= null;
	private String							strOutput			= "";
	private String							strSqlError			= "";

	/**
	 *
	 * \brief JobSchedulerPLSQLJob
	 *
	 * \details
	 *
	 */
	public JobSchedulerPLSQLJob() {
		super(new JobSchedulerPLSQLJobOptions());
	}

	/**
	 *
	 * \brief Execute - Start the Execution of JobSchedulerPLSQLJob
	 *
	 * \details
	 *
	 * For more details see
	 *
	 * \see JobSchedulerAdapterClass
	 * \see JobSchedulerPLSQLJobMain
	 *
	 * \return JobSchedulerPLSQLJob
	 *
	 * @return
	 */
	public JobSchedulerPLSQLJob Execute() throws Exception {
		final String conMethodName = conClassName + "::Execute"; //$NON-NLS-1$

		JSJ_I_110.toLog(conMethodName);

		objJSJobUtilities.setJSParam(conSettingSQL_ERROR, "");

		try {
			getOptions().CheckMandatory();
			logger.debug(getOptions().dirtyString());

			DriverManager.registerDriver(new oracle.jdbc.OracleDriver());
			objConnection = DriverManager.getConnection(objOptions.db_url.Value(), objOptions.db_user.Value(), objOptions.db_password.Value());

			// pl/sql is expecting \n as newline.
//			String plsql = objOptions.command.Value().replace("\r\n", "\n");
			String plsql = objOptions.command.unescapeXML().replace("\r\n", "\n");
			plsql = objJSJobUtilities.replaceSchedulerVars(false, plsql);

			objOptions.replaceVars(plsql);

			dbmsOutput = new DbmsOutput(objConnection);
			// TODO Option Buffersize
			dbmsOutput.enable(1000000);

			cs = objConnection.prepareCall(plsql);
			cs.execute();

		}
		catch (SQLException e) {
			logger.error(JSMessages.JSJ_F_107.get(conMethodName), e);
			String strT = String.format("SQL Exception raised. Msg='%1$s', Status='%2$s'", e.getLocalizedMessage(), e.getSQLState());
			logger.error(strT);
			strSqlError = strT;
			objJSJobUtilities.setJSParam(conSettingSQL_ERROR, strT);
			throw new JobSchedulerException(strT, e);
		}
		catch (Exception e) {
			throw new JobSchedulerException(JSMessages.JSJ_F_107.get(conMethodName), e);
		}
		finally {
			objJSJobUtilities.setJSParam(conSettingDBMS_OUTPUT, "");
			objJSJobUtilities.setJSParam(conSettingSTD_OUT_OUTPUT, "");
			strOutput = dbmsOutput.getOutput();
			if (strOutput != null) {
				objJSJobUtilities.setJSParam(conSettingDBMS_OUTPUT, strOutput);
				objJSJobUtilities.setJSParam(conSettingSTD_OUT_OUTPUT, strOutput);
				
				int intRegExpFlags = Pattern.CASE_INSENSITIVE + Pattern.MULTILINE + Pattern.DOTALL;
				String strA[] = strOutput.split("\n");
				
				boolean flgAVariableFound = false;
				String strRegExp = objOptions.VariableParserRegExpr.Value();
				if (strRegExp.length() >= 0) {
					// TODO check the number of groups. must be >= 2
					Pattern objRegExprPattern = Pattern.compile(strRegExp, intRegExpFlags);
					for (String string : strA) {
						Matcher objMatch = objRegExprPattern.matcher(string);
						if (objMatch.matches() == true) {
							objJSJobUtilities.setJSParam(objMatch.group(1), objMatch.group(2).trim());
							flgAVariableFound = true;
						}
					}
				}

				dbmsOutput.close();

				if (flgAVariableFound == false) {
					logger.info(String.format("no JS-variable definitions found using reg-exp '%1$s'.", strRegExp));
				}

				ResultSetMetaData csmd = cs.getMetaData();
				if (csmd != null) {
					int nCols;
					nCols = csmd.getColumnCount();
					for (int i = 1; i <= nCols; i++) {
						System.out.print(csmd.getColumnName(i));
						int colSize = csmd.getColumnDisplaySize(i);
						for (int k = 0; k < colSize - csmd.getColumnName(i).length(); k++)
							System.out.print(" ");
					}
					System.out.println("");
				}
			}
			if (cs != null) {
				cs.close();
				cs = null;
			}
			if (objConnection != null) {
				objConnection.close();
				objConnection = null;
			}
		}

		logger.debug(JSMessages.JSJ_I_111.get(conMethodName));
		return this;
	}

	public void init() {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::init"; //$NON-NLS-1$
		doInitialize();
	}

	private void doInitialize() {
	} // doInitialize

	public String getSqlError() {
		return strSqlError;
	}

	public String getOutput() {
		return strOutput;
	}
} // class JobSchedulerPLSQLJob