package com.sos.jitl.checkhistory.classes;

import java.math.BigInteger;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.jitl.reporting.db.DBItemReportTask;
import com.sos.jitl.reporting.db.ReportTaskExecutionsDBLayer;
import com.sos.joc.model.common.Err;
import com.sos.joc.model.common.HistoryState;
import com.sos.joc.model.common.HistoryStateText;
import com.sos.joc.model.job.TaskHistoryItem;
import com.sos.joc.model.order.OrderHistoryItem;
import com.sos.scheduler.model.answers.HistoryEntry;
import com.sos.scheduler.model.answers.JobChain.OrderHistory.Order;

public class HistoryDatabaseExecuter extends HistoryDataSource {

    private static final String JOB_STRING_FOR_WEBSERVICE = "{'jobschedulerId':'%s','limit':1,'jobs':[{'job':'%s'}],'historyStates':";
    private static final String JOB_CHAIN_ORDER_STRING_FOR_WEBSERVICE =
            "{'jobschedulerId':'%s','limit':1,'orders':[{'jobChain':'%s','orderId':'%s'}],'historyStates':";
    private static final String JOB_CHAIN_STRING_FOR_WEBSERVICE = "{'jobschedulerId':'%s','limit':1,'orders':[{'jobChain':'%s'}],'historyStates':";
    private static final String JOB_CHAIN_ORDER_HISTORY_STRING_FOR_WEBSERVICE = "{'jobschedulerId':'%s','historyIds':['%s']}";
    private SOSHibernateSession sosHibernateSession = null;

    private static final Logger LOGGER = LoggerFactory.getLogger(HistoryDatabaseExecuter.class);

    public HistoryDatabaseExecuter(SOSHibernateSession sosHibernateSession) {
        super("", "");
        this.sosHibernateSession = sosHibernateSession;
    }

    protected HistoryEntry getJobHistoryEntry(String state) throws Exception {
        HistoryEntry historyEntry = null;
        try {

            List<TaskHistoryItem> listOfHistory = new ArrayList<TaskHistoryItem>();

            ReportTaskExecutionsDBLayer reportTaskExecutionsDBLayer = new ReportTaskExecutionsDBLayer(sosHibernateSession);
            sosHibernateSession.beginTransaction();
            reportTaskExecutionsDBLayer.getFilter().setLimit(1);
            reportTaskExecutionsDBLayer.getFilter().setOrderCriteria("historyId");
            reportTaskExecutionsDBLayer.getFilter().setSortMode("DESC");
            reportTaskExecutionsDBLayer.getFilter().setSchedulerId(schedulerId);
            if (!"".equals(timeLimit)) {
                HistoryInterval historyInterval = historyHelper.getUTCIntervalFromTimeLimit(timeLimit);
                historyInterval.getUtcFrom();
                reportTaskExecutionsDBLayer.getFilter().setExecutedFrom(historyInterval.getFrom().toDate());
                reportTaskExecutionsDBLayer.getFilter().setExecutedTo(historyInterval.getTo().toDate());
            }

            String[] states = state.split(",");
            for (String s : states) {
                s = s.replaceAll("'", "");
                reportTaskExecutionsDBLayer.getFilter().addState(s);
            }
            reportTaskExecutionsDBLayer.getFilter().addJobPath(jobName);

            List<DBItemReportTask> listOfDBItemReportTaskDBItems = new ArrayList<DBItemReportTask>();

            listOfDBItemReportTaskDBItems = reportTaskExecutionsDBLayer.getSchedulerHistoryListFromTo();

            if (listOfDBItemReportTaskDBItems != null) {
                for (DBItemReportTask dbItemReportTask : listOfDBItemReportTaskDBItems) {
                    TaskHistoryItem taskHistoryItem = new TaskHistoryItem();

                    taskHistoryItem.setJobschedulerId(dbItemReportTask.getSchedulerId());
                    taskHistoryItem.setAgent(dbItemReportTask.getAgentUrl());
                    taskHistoryItem.setClusterMember(dbItemReportTask.getClusterMemberId());
                    taskHistoryItem.setEndTime(dbItemReportTask.getEndTime());
                    if (dbItemReportTask.getError()) {
                        Err error = new Err();
                        error.setCode(dbItemReportTask.getErrorCode());
                        error.setMessage(dbItemReportTask.getErrorText());
                        taskHistoryItem.setError(error);
                    }

                    taskHistoryItem.setExitCode(dbItemReportTask.getExitCode());
                    taskHistoryItem.setJob(dbItemReportTask.getName());
                    taskHistoryItem.setStartTime(dbItemReportTask.getStartTime());

                    HistoryState historyState = new HistoryState();
                    if (dbItemReportTask.isSuccessFull()) {
                        historyState.setSeverity(0);
                        historyState.set_text(HistoryStateText.SUCCESSFUL);
                    }
                    if (dbItemReportTask.isInComplete()) {
                        historyState.setSeverity(1);
                        historyState.set_text(HistoryStateText.INCOMPLETE);
                    }
                    if (dbItemReportTask.isFailed()) {
                        historyState.setSeverity(2);
                        historyState.set_text(HistoryStateText.FAILED);
                    }
                    taskHistoryItem.setState(historyState);
                    taskHistoryItem.setSurveyDate(dbItemReportTask.getCreated());

                    taskHistoryItem.setTaskId(dbItemReportTask.getHistoryIdAsString());

                    listOfHistory.add(taskHistoryItem);
                }
            }
            if (listOfHistory.size() > 0) {
                historyEntry = new HistoryEntry();

                historyEntry.setError(BigInteger.valueOf(0));
                if (listOfHistory.get(0).getError() != null) {
                    historyEntry.setError(BigInteger.valueOf(1));
                    historyEntry.setErrorCode(listOfHistory.get(0).getError().getCode());
                    historyEntry.setErrorText(listOfHistory.get(0).getError().getMessage());
                }

                historyEntry.setExitCode(BigInteger.valueOf(listOfHistory.get(0).getExitCode()));
                historyEntry.setTaskId(string2BigInteger(listOfHistory.get(0).getTaskId()));
                historyEntry.setId(string2BigInteger(listOfHistory.get(0).getTaskId()));
                historyEntry.setJobName(listOfHistory.get(0).getJob());
                historyEntry.setStartTime(listOfHistory.get(0).getStartTime().toString());
                historyEntry.setEndTime(listOfHistory.get(0).getEndTime().toString());
            }

        } finally {
            sosHibernateSession.rollback();
        }
        return historyEntry;

    }

    public Order getJobChainHistoryEntry(String state) throws Exception {
        if (accessToken.isEmpty()) {
            throw new Exception("AccessToken is empty. Login not executed");
        }

        String body;

        if (!"".equals(timeLimit)) {
            HistoryInterval historyInterval = historyHelper.getUTCIntervalFromTimeLimit(timeLimit);
            historyInterval.getUtcFrom();
            if (orderId == null || orderId.isEmpty()) {
                body = String.format(JOB_CHAIN_STRING_FOR_WEBSERVICE, schedulerId, jobChainName) + "[" + state + "],'dateFrom':'" + historyInterval
                        .getUtcFrom() + "','dateTo':'" + historyInterval.getUtcTo() + "'}";
            } else {
                body = String.format(JOB_CHAIN_ORDER_STRING_FOR_WEBSERVICE, schedulerId, jobChainName, orderId) + "[" + state + "],'dateFrom':'"
                        + historyInterval.getUtcFrom() + "','dateTo':'" + historyInterval.getUtcTo() + "'}";
            }
        } else {
            if (orderId == null || orderId.isEmpty()) {
                body = String.format(JOB_CHAIN_STRING_FOR_WEBSERVICE, schedulerId, jobChainName) + "[" + state + "]}";
            } else {
                body = String.format(JOB_CHAIN_ORDER_STRING_FOR_WEBSERVICE, schedulerId, jobChainName, orderId) + "[" + state + "]}";
            }
        }

        body = body.replace("'", "\"");

        String answer = jobSchedulerRestApiClient.postRestService(new URI(jocUrl + "/orders/history"), body);
        Order o = json2JobChainHistoryEntry(answer);
        if (o.getHistoryId() == null) {
            return null;
        } else {
            return o;
        }
    }

    public OrderHistoryItem getJobChainOrderHistoryEntry(BigInteger orderHistoryId) throws Exception {
        if (accessToken.isEmpty()) {
            throw new Exception("AccessToken is empty. Login not executed");
        }

        String body = String.format(JOB_CHAIN_ORDER_HISTORY_STRING_FOR_WEBSERVICE, schedulerId, orderHistoryId);
        body = body.replace("'", "\"");

        String answer = jobSchedulerRestApiClient.postRestService(new URI(jocUrl + "/orders/history"), body);
        OrderHistoryItem o = json2JobChainOrderHistoryEntry(answer);
        if (o.getHistoryId() == null) {
            return null;
        } else {
            return o;
        }
    }

}
