package com.sos.jitl.checkhistory.classes;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.hibernate.exceptions.SOSHibernateException;
import com.sos.jitl.reporting.db.DBItemReportTask;
import com.sos.jitl.reporting.db.DBItemReportTrigger;
import com.sos.jitl.reporting.db.ReportTaskExecutionsDBLayer;
import com.sos.jitl.reporting.db.ReportTriggerDBLayer;
import com.sos.joc.model.common.Err;
import com.sos.joc.model.common.HistoryState;
import com.sos.joc.model.common.HistoryStateText;
import com.sos.joc.model.job.TaskHistoryItem;
import com.sos.joc.model.order.OrderHistoryItem;
import com.sos.scheduler.model.answers.HistoryEntry;
import com.sos.scheduler.model.answers.JobChain.OrderHistory.Order;

public class HistoryDatabaseExecuter extends HistoryDataSource {

    private SOSHibernateSession sosHibernateSession = null;
    private static final Logger LOGGER = LoggerFactory.getLogger(HistoryDatabaseExecuter.class);

    public HistoryDatabaseExecuter(SOSHibernateSession sosHibernateSession) {
        super(null);
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
                if (listOfHistory.get(0).getStartTime() != null) {
                    historyEntry.setStartTime(listOfHistory.get(0).getStartTime().toString());
                }
                if (listOfHistory.get(0).getEndTime() != null) {
                    historyEntry.setEndTime(listOfHistory.get(0).getEndTime().toString());
                }
            }

        } finally {
            sosHibernateSession.rollback();
        }
        return historyEntry;

    }

    public Order getJobChainHistoryEntry(String state) throws SOSHibernateException {
        Order order = null;

        List<OrderHistoryItem> listOfHistory = new ArrayList<OrderHistoryItem>();

        ReportTriggerDBLayer reportTriggerDBLayer = new ReportTriggerDBLayer(sosHibernateSession);
        reportTriggerDBLayer.getFilter().setSchedulerId(schedulerId);
        reportTriggerDBLayer.getFilter().setLimit(1);
        reportTriggerDBLayer.getFilter().setOrderCriteria("historyId");
        reportTriggerDBLayer.getFilter().setSortMode("DESC");
        String[] states = state.split(",");
        for (String s : states) {
            s = s.replaceAll("'", "");
            reportTriggerDBLayer.getFilter().addState(s);
        }

        reportTriggerDBLayer.getFilter().addOrderPath(jobChainName, orderId);

        if (!"".equals(timeLimit)) {
            HistoryInterval historyInterval = historyHelper.getUTCIntervalFromTimeLimit(timeLimit);
            reportTriggerDBLayer.getFilter().setExecutedFrom(historyInterval.getFrom().toDate());
            reportTriggerDBLayer.getFilter().setExecutedTo(historyInterval.getTo().toDate());
        }
        List<DBItemReportTrigger> listOfDBItemReportTrigger;
        try {
            listOfDBItemReportTrigger = reportTriggerDBLayer.getSchedulerOrderHistoryListFromTo();

            for (DBItemReportTrigger dbItemReportTrigger : listOfDBItemReportTrigger) {
                OrderHistoryItem history = new OrderHistoryItem();

                history.setEndTime(dbItemReportTrigger.getEndTime());
                history.setHistoryId(String.valueOf(dbItemReportTrigger.getHistoryId()));
                history.setJobChain(dbItemReportTrigger.getParentName());
                history.setNode(dbItemReportTrigger.getState());
                history.setOrderId(dbItemReportTrigger.getName());
                history.setPath(dbItemReportTrigger.getFullOrderQualifier());
                history.setStartTime(dbItemReportTrigger.getStartTime());
                HistoryState historyState = new HistoryState();

                if (dbItemReportTrigger.getStartTime() != null && dbItemReportTrigger.getEndTime() == null) {
                    historyState.setSeverity(1);
                    historyState.set_text(HistoryStateText.INCOMPLETE);
                } else {
                    if (dbItemReportTrigger.getResultError()) {
                        historyState.setSeverity(2);
                        historyState.set_text(HistoryStateText.FAILED);
                    } else {
                        if (dbItemReportTrigger.getEndTime() != null && !dbItemReportTrigger.getResultError()) {
                            historyState.setSeverity(0);
                            historyState.set_text(HistoryStateText.SUCCESSFUL);
                        }
                    }

                }
                history.setState(historyState);
                history.setSurveyDate(dbItemReportTrigger.getCreated());
                listOfHistory.add(history);

            }

            if (listOfHistory.size() > 0) {
                order = new Order();
                order.setId(listOfHistory.get(0).getOrderId());
                order.setState(listOfHistory.get(0).getNode());
                order.setOrder(listOfHistory.get(0).getOrderId());
                order.setHistoryId(string2BigInteger(listOfHistory.get(0).getHistoryId()));
                order.setJobChain(listOfHistory.get(0).getJobChain());
                if (listOfHistory.get(0).getStartTime() != null) {
                    order.setStartTime(listOfHistory.get(0).getStartTime().toString());
                }
                if (listOfHistory.get(0).getEndTime() != null) {
                    order.setEndTime(listOfHistory.get(0).getEndTime().toString());
                }
            }
        } finally {
            sosHibernateSession.rollback();
        }
        return order;

    }

    public OrderHistoryItem getJobChainOrderHistoryEntry(BigInteger orderHistoryId) throws SOSHibernateException {
        List<OrderHistoryItem> listOfHistory = new ArrayList<OrderHistoryItem>();

        ReportTriggerDBLayer reportTriggerDBLayer = new ReportTriggerDBLayer(sosHibernateSession);
        reportTriggerDBLayer.getFilter().setSchedulerId(schedulerId);
        reportTriggerDBLayer.getFilter().setLimit(1);

        reportTriggerDBLayer.getFilter().addOrderHistoryId(orderHistoryId.longValue());
        List<DBItemReportTrigger> listOfDBItemReportTrigger;
        try {
            listOfDBItemReportTrigger = reportTriggerDBLayer.getSchedulerOrderHistoryListFromTo();

            for (DBItemReportTrigger dbItemReportTrigger : listOfDBItemReportTrigger) {
                OrderHistoryItem history = new OrderHistoryItem();

                history.setEndTime(dbItemReportTrigger.getEndTime());
                history.setHistoryId(String.valueOf(dbItemReportTrigger.getHistoryId()));
                history.setJobChain(dbItemReportTrigger.getParentName());
                history.setNode(dbItemReportTrigger.getState());
                history.setOrderId(dbItemReportTrigger.getName());
                history.setPath(dbItemReportTrigger.getFullOrderQualifier());
                history.setStartTime(dbItemReportTrigger.getStartTime());
                HistoryState historyState = new HistoryState();

                if (dbItemReportTrigger.getStartTime() != null && dbItemReportTrigger.getEndTime() == null) {
                    historyState.setSeverity(1);
                    historyState.set_text(HistoryStateText.INCOMPLETE);
                } else {
                    if (dbItemReportTrigger.getResultError()) {
                        historyState.setSeverity(2);
                        historyState.set_text(HistoryStateText.FAILED);
                    } else {
                        if (dbItemReportTrigger.getEndTime() != null && !dbItemReportTrigger.getResultError()) {
                            historyState.setSeverity(0);
                            historyState.set_text(HistoryStateText.SUCCESSFUL);
                        }
                    }

                }
                history.setState(historyState);
                history.setSurveyDate(dbItemReportTrigger.getCreated());
                listOfHistory.add(history);
            }

            if (listOfHistory.size() > 0) {
                return listOfHistory.get(0);
            } else {
                return null;
            }
        } finally {
            sosHibernateSession.rollback();
        }
    }

}
