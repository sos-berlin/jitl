package com.sos.jitl.managed.model;

import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.hibernate.classes.SOSHibernateSQLExecutor;
import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.jitl.managed.job.ManagedDatabaseJobOptions;

import sos.spooler.Variable_set;
import sos.util.SOSString;

public class ManagedDatabaseModel {

    private static final Logger LOGGER = LoggerFactory.getLogger(ManagedDatabaseModel.class);
    private static final boolean isDebugEnabled = LOGGER.isDebugEnabled();

    public static final String PARAMETER_NAME_VALUE = "name_value";

    private SOSHibernateSession session;
    private ManagedDatabaseJobOptions options;
    private boolean isOrderJob;
    private Variable_set orderParams;
    private StringBuffer warning;

    public ManagedDatabaseModel(final SOSHibernateSession sess, final ManagedDatabaseJobOptions opt, boolean isOrder, final Variable_set op)
            throws Exception {
        session = sess;
        options = opt;
        isOrderJob = isOrder;
        orderParams = op;
    }

    public void process() throws Exception {

        try {
            if (SOSString.isEmpty(options.command.getValue())) {
                throw new Exception("command is empty.");
            }

            warning = new StringBuffer();
            SOSHibernateSQLExecutor executor = session.getSQLExecutor();

            List<String> statements = null;
            try {
                Path path = Paths.get(options.command.getValue().trim());
                if (Files.notExists(path)) {
                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("[load from file][%s]file not found", path));
                    }
                    throw new FileNotFoundException(String.format("[%s]file not found", path));
                }
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("[load from file]%s", path));
                }
                statements = executor.getStatements(path);
            } catch (Throwable e) { // catch (InvalidPathException | NullPointerException ex) {
                statements = executor.getStatements(options.command.getValue());
            }

            session.beginTransaction();
            for (String statement : statements) {
                boolean isResultListQuery = SOSHibernateSQLExecutor.isResultListQuery(statement, options.exec_returns_resultset.value());
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("isResultListQuery=%s", isResultListQuery));
                }
                LOGGER.info(String.format("executing database statement: %s", statement));

                if (isResultListQuery) {
                    executeResultSet(executor, statement, orderParams);
                } else {
                    executor.executeUpdate(statement);
                }
            }
            session.commit();
        } catch (

        Exception e) {
            try {
                session.rollback();
            } catch (Throwable ex) {
            }
            throw e;
        }
    }

    private void executeResultSet(SOSHibernateSQLExecutor executor, String statement, Variable_set orderParams) throws Exception {
        ResultSet rs = null;
        try {
            boolean checkResultSet = !options.resultset_as_parameters.getValue().equalsIgnoreCase("false");
            boolean isParamValue = options.resultset_as_parameters.getValue().equals(PARAMETER_NAME_VALUE);
            if (isDebugEnabled) {
                LOGGER.debug(String.format("isOrderJob=%s, checkResultSet=%s, isParamValue=%s", isOrderJob, checkResultSet, isParamValue));
            }
            rs = executor.getResultSet(statement);

            if (checkResultSet || options.resultset_as_warning.value()) {
                StringBuffer warn = new StringBuffer();
                int rowCount = 0;
                Map<String, String> record = null;
                while (!(record = executor.nextAsStringMap(rs)).isEmpty()) {
                    rowCount++;

                    if (isOrderJob && checkResultSet) {
                        if (orderParams == null) {
                            if (isDebugEnabled) {
                                LOGGER.debug(String.format("[order][skip set param: orderParams=null]%s", record));
                            }
                        } else {
                            if (isParamValue) {
                                String paramKey = null;
                                String paramValue = null;

                                int columnCounter = 0;
                                for (String key : record.keySet()) {
                                    columnCounter++;
                                    if (columnCounter == 1) {
                                        paramKey = record.get(key);
                                    } else if (columnCounter == 2) {
                                        paramValue = record.get(key);
                                    } else {
                                        break;
                                    }
                                }
                                if (paramKey != null && paramValue != null) {
                                    if (isDebugEnabled) {
                                        LOGGER.debug(String.format("[order][set param]%s=%s", paramKey, paramValue));
                                    }
                                    orderParams.set_var(paramKey, paramValue);
                                }
                            } else {
                                if (rowCount == 1) {
                                    for (String key : record.keySet()) {
                                        String value = record.get(key);
                                        if (isDebugEnabled) {
                                            LOGGER.debug(String.format("[order][set param]%s=%s", key, value));
                                        }
                                        orderParams.set_var(key, value);
                                    }
                                }
                            }
                        }
                    }

                    if (options.resultset_as_warning.value()) {
                        if (rowCount > 1) {
                            warn.append(", ");
                        }
                        warn.append(record);
                    }
                }// while

                if (warn.length() > 0) {
                    if (warning.length() > 0) {
                        warning.append("; ");
                    }
                    warning.append(warn);
                }
            }
        } catch (Exception e) {
            throw e;
        } finally {
            executor.close(rs);
        }
    }

    public String getWarning() {
        return warning.length() == 0 ? null : String.format("execution terminated with warning: %s", warning);
    }
}
