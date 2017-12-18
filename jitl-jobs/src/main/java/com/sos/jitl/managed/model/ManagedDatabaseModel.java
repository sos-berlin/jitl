package com.sos.jitl.managed.model;

import java.sql.ResultSet;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.hibernate.classes.SOSHibernateSQLExecutor;
import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.jitl.managed.job.ManagedDatabaseJobOptions;

import sos.spooler.Variable_set;

public class ManagedDatabaseModel {

    private static final Logger LOGGER = LoggerFactory.getLogger(ManagedDatabaseModel.class);

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
            warning = new StringBuffer();
            SOSHibernateSQLExecutor executor = session.getSQLExecutor();
            List<String> statements = executor.getStatements(options.command.getValue());

            session.beginTransaction();
            for (String statement : statements) {
                boolean isResultListQuery = SOSHibernateSQLExecutor.isResultListQuery(statement, options.exec_returns_resultset.value());
                LOGGER.debug(String.format("isResultListQuery=%s", isResultListQuery));
                LOGGER.info(String.format("executing database statement: %s", statement));

                if (isResultListQuery) {
                    executeResultSet(executor, statement, orderParams);
                } else {
                    executor.executeUpdate(statement);
                }
            }
            session.commit();
        } catch (Exception e) {
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
            rs = executor.getResultSet(statement);

            if (options.resultset_as_parameters.value() || options.resultset_as_warning.value()) {
                Map<String, String> record = null;
                StringBuffer warn = new StringBuffer();
                int rowCount = 0;
                while (!(record = executor.nextAsStringMap(rs)).isEmpty()) {
                    rowCount++;

                    if (rowCount == 1 && isOrderJob && options.resultset_as_parameters.value()) {
                        if (orderParams == null) {
                            LOGGER.debug(String.format("[order][skip set param: orderParams=null]%s", record));
                        } else {
                            for (String key : record.keySet()) {
                                String value = record.get(key);
                                LOGGER.debug(String.format("[order][set param]%s=%s", key, value));
                                orderParams.set_var(key, value);
                            }
                        }
                    }

                    if (options.resultset_as_warning.value()) {
                        if (rowCount > 1) {
                            warn.append(", ");
                        }
                        warn.append(record);
                    } else {
                        break;
                    }
                }
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
