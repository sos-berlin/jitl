package com.sos.jitl.notification.plugins.history;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Matcher;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.hibernate.classes.SOSHibernateFactory;
import com.sos.jitl.notification.db.DBItemSchedulerMonChecks;
import com.sos.jitl.notification.db.DBItemSchedulerMonNotifications;
import com.sos.jitl.notification.db.DBItemSchedulerMonResults;
import com.sos.jitl.notification.db.DBLayer;
import com.sos.jitl.notification.db.DBLayerSchedulerMon;
import com.sos.jitl.notification.helper.EEndTimeType;
import com.sos.jitl.notification.helper.EStartTimeType;
import com.sos.jitl.notification.helper.counters.CounterCheckHistoryTimer;
import com.sos.jitl.notification.helper.elements.timer.ElementTimer;
import com.sos.jitl.notification.helper.elements.timer.ElementTimer.TimerResult;
import com.sos.jitl.notification.helper.elements.timer.ElementTimerScript;
import com.sos.jitl.notification.jobs.history.CheckHistoryJobOptions;
import com.sos.jitl.notification.model.NotificationModel;

import sos.util.SOSString;

public class CheckHistoryTimerPlugin implements ICheckHistoryPlugin {

    private static final Logger LOGGER = LoggerFactory.getLogger(CheckHistoryTimerPlugin.class);
    private static final String CHECK_COMPLETED = "[completed]";
    private static final boolean isDebugEnabled = LOGGER.isDebugEnabled();
    private static final boolean isTraceEnabled = LOGGER.isTraceEnabled();
    private CounterCheckHistoryTimer counter;
    private boolean handleTransaction = false;
    private ScriptEngineManager scriptEngineManager = new ScriptEngineManager();

    @Override
    public void onInit(LinkedHashMap<String, ElementTimer> timers, CheckHistoryJobOptions options, DBLayerSchedulerMon dbLayer) throws Exception {

    }

    @Override
    public void onExit(LinkedHashMap<String, ElementTimer> timers, CheckHistoryJobOptions options, DBLayerSchedulerMon dbLayer) throws Exception {
    }

    @Override
    public void onProcess(LinkedHashMap<String, ElementTimer> timers, List<DBItemSchedulerMonChecks> checks, CheckHistoryJobOptions options,
            DBLayerSchedulerMon dbLayer, Date dateFrom, Date dateTo) throws Exception {
        String method = "onProcess";

        if (dbLayer == null) {
            throw new Exception("dbLayer is NULL");
        }
        if (timers == null) {
            throw new Exception("timers is NULL");
        }
        int checksSize = checks == null ? 0 : checks.size();
        if (timers.size() == 0 || checksSize == 0) {
            LOGGER.info(String.format("[%s][skip][timers=%s][checks=0]", method, timers.size()));
            return;
        }

        initCountChecks();
        LOGGER.info(String.format("[%s][timers=%s][checks=%s]", method, timers.size(), checksSize));

        for (int i = 0; i < checksSize; i++) {
            DBItemSchedulerMonChecks check = checks.get(i);
            if (!timers.containsKey(check.getName())) {
                counter.addSkip();
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("[%s][skip][%s]timer definition is not found.", method, check.getName()));
                }
                continue;
            }
            ElementTimer timer = timers.get(check.getName());
            if (timer.getMinimum() == null && timer.getMaximum() == null) {
                counter.addSkip();
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("[%s][skip][%s][timer=%s]missing Minimum or Maximum elements", method, check.getName(), timer
                            .getName()));
                }
                continue;
            }
            check = checkNotification(check, dbLayer);
            if (check.getObjectType().equals(DBLayer.NOTIFICATION_OBJECT_TYPE_JOB)) {
                analyzeJobCheck((i + 1), dbLayer, check, timer);
            } else {
                analyzeJobChainCheck((i + 1), dbLayer, check, timer);
            }
        }
        LOGGER.info(String.format("[%s][created=%s][skipped=%s]", method, counter.getTotal(), counter.getSkip()));
    }

    private DBItemSchedulerMonChecks checkNotification(DBItemSchedulerMonChecks check, DBLayerSchedulerMon dbLayer) throws Exception {
        String method = "checkNotification";

        // wegen batch insert bei den Datenbanken ohne autoincrement
        if (check.getNotificationId().equals(new Long(0))) {
            if (SOSString.isEmpty(check.getResultIds())) {
                throw new Exception(String.format("[%s][could not execute check][resultIds is empty]%s", method, NotificationModel.toString(check)));
            }
            String[] arr = check.getResultIds().split(";");
            if (arr.length < 5) {
                throw new Exception(String.format("[%s][could not execute check][missing notification infos]%s", method, NotificationModel.toString(
                        check)));
            }

            DBItemSchedulerMonNotifications notification = dbLayer.getNotification(arr[0], Boolean.parseBoolean(arr[1]), new Long(arr[2]), new Long(
                    arr[3]), new Long(arr[4]));
            if (notification == null) {
                throw new Exception(String.format(
                        "[%s][could not execute check][notification not found][schedulerId=%s][standalone=%s][taskId=%s][step=%s][orderHistoryId=%s]%s",
                        method, arr[0], arr[1], arr[2], arr[3], arr[4], NotificationModel.toString(check)));
            }
            check.setNotificationId(notification.getId());
            check.setResultIds(null);
        }

        return check;
    }

    private void initCountChecks() {
        counter = new CounterCheckHistoryTimer();
    }

    private boolean containsVariables(String val) {
        return val != null && (val.contains("${") || val.contains("%"));
    }

    private String resolveParam(ElementTimerScript script, DBItemSchedulerMonResults result, String scriptValue) throws Exception {
        String method = "  resolveParam";
        if (script != null) {
            if (scriptValue != null) {
                if (!containsVariables(scriptValue)) {
                    return scriptValue;
                }
            }
            scriptValue = resolveParam(scriptValue == null ? script.getValue() : scriptValue, result.getName(), result.getValue());
            if (scriptValue != null) {
                if (isTraceEnabled) {
                    LOGGER.trace(String.format("[%s][%s]value=%s", method, script.getElementTitle(), scriptValue));
                }
            }
        }
        return scriptValue;
    }

    private ElementTimer setTimerResultScriptValue(DBItemSchedulerMonChecks check, ElementTimer timer, ElementTimerScript element,
            String elementValue) {
        String method = "    setTimerResultScriptValue";
        if (element != null) {
            try {
                Double result = null;
                if (element.isMinimum()) {
                    timer.getTimerResult().setMinimum(evalScript(element, elementValue));
                    result = timer.getTimerResult().getMinimum();
                } else {
                    timer.getTimerResult().setMaximum(evalScript(element, elementValue));
                    result = timer.getTimerResult().getMaximum();
                }
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("[%s][check name=%s, id=%s][timer=%s]%s", method, check.getName(), check.getId(), element
                            .getElementTitle(), result));
                }
            } catch (NullPointerException ex) {
                LOGGER.warn(String.format("[%s][check name=%s, id=%s][timer=%s][%s]%s", method, check.getName(), check.getId(), element
                        .getElementTitle(), elementValue, ex.toString()));
                timer.resetTimerResult();
            } catch (Exception ex) {
                if (containsVariables(element.getValue())) {
                    timer.getTimerResult().setLastErrorMessage(String.format("[check name=%s, id=%s][timer=%s][%s]%s", check.getName(), check.getId(),
                            element.getElementTitle(), elementValue, ex.getMessage()));
                    timer.getTimerResult().setReadDbResults(true);

                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("[%s][setReadDbResults=true][check name=%s, id=%s][timer=%s]", method, check.getName(), check
                                .getId(), element.getElementTitle()));
                    }
                } else {
                    LOGGER.warn(String.format("[%s][check name=%s, id=%s][timer=%s][%s]%s", method, check.getName(), check.getId(), element
                            .getElementTitle(), elementValue, ex.getMessage()));
                    timer.resetTimerResult();
                }
            }
        }
        return timer;
    }

    private ElementTimer setTimerResult(DBLayerSchedulerMon dbLayer, ElementTimer timer, DBItemSchedulerMonChecks check,
            List<DBItemSchedulerMonNotifications> steps, Long resultNotificationId, Long stepFromIndex, Long stepToIndex) throws Exception {
        String method = "  setTimerResult";
        ElementTimerScript minElement = timer.getMinimum();
        ElementTimerScript maxElement = timer.getMaximum();

        timer.createTimerResult();
        if (minElement != null) {
            timer = setTimerResultScriptValue(check, timer, minElement, minElement.getValue());
            if (timer.getTimerResult() == null) {
                return timer;
            }
        }
        if (maxElement != null) {
            timer = setTimerResultScriptValue(check, timer, maxElement, maxElement.getValue());
            if (timer.getTimerResult() == null) {
                return timer;
            }
        }

        StringBuffer resultIds = new StringBuffer();
        if (timer.getTimerResult().getReadDbResults()) {
            if (isDebugEnabled) {
                LOGGER.debug(String.format("[%s][getReadDbResults]%s", method, NotificationModel.toString(check)));
            }
            String minValue = null;
            String maxValue = null;
            int c = 0;

            if (steps == null) {
                steps = dbLayer.getOrderNotificationsByNotificationId(check.getNotificationId());
            }

            for (DBItemSchedulerMonNotifications step : steps) {
                c++;

                if (isDebugEnabled) {
                    LOGGER.debug(String.format("[%s][%s][stepFromIndex=%s][stepToIndex=%s]%s", method, c, stepFromIndex, stepToIndex,
                            NotificationModel.toString(step)));
                }

                if (step.getStep() >= stepFromIndex && step.getStep() <= stepToIndex) {
                    List<DBItemSchedulerMonResults> params = dbLayer.getNotificationResults(step.getId());
                    if (params != null) {
                        if (isDebugEnabled) {
                            LOGGER.debug(String.format("[%s][%s]params=%s", method, c, params.size()));
                        }
                        int ri = 0;
                        for (DBItemSchedulerMonResults param : params) {
                            ri++;

                            if (isDebugEnabled) {
                                LOGGER.debug(String.format("[%s][%s][%s]%s", method, c, ri, NotificationModel.toString(param)));
                            }

                            if (ri > 1) {
                                resultIds.append(";");
                            }
                            resultIds.append(param.getId());

                            if (timer.getTimerResult().getMinimum() == null) {
                                minValue = resolveParam(minElement, param, minValue);
                            }
                            if (timer.getTimerResult().getMaximum() == null) {
                                maxValue = resolveParam(maxElement, param, maxValue);
                            }
                        }
                    } else {
                        if (isDebugEnabled) {
                            LOGGER.debug(String.format("[%s][%s]params is null", method, c));
                        }
                        if (timer.getTimerResult().getLastErrorMessage() != null) {
                            LOGGER.warn(timer.getTimerResult().getLastErrorMessage());
                        }
                    }
                }
            }
            if (minValue == null && maxValue == null) {
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("[%s][skip]Minimum or Maximum not resolved", method));
                }
                timer.resetTimerResult();
                return timer;
            }

            if (timer.getTimerResult().getMinimum() == null && minValue != null) {
                timer = setTimerResultScriptValue(check, timer, minElement, minValue);
                if (timer.getTimerResult() == null) {
                    return timer;
                }
            }
            if (timer.getTimerResult().getMaximum() == null && maxValue != null) {
                timer = setTimerResultScriptValue(check, timer, maxElement, maxValue);
                if (timer.getTimerResult() == null) {
                    return timer;
                }
            }
        }
        timer.getTimerResult().setResultIds(resultIds);
        return timer;
    }

    private boolean analyzeCheck(int count, DBLayerSchedulerMon dbLayer, DBItemSchedulerMonChecks check, Long objectType) throws Exception {
        String method = "analyzeCheck][" + count;
        DBItemSchedulerMonChecks item = dbLayer.getCheck(check.getName(), check.getNotificationId(), objectType, check.getStepFrom(), check
                .getStepTo());

        if (isDebugEnabled) {
            LOGGER.debug(String.format("[%s]%s", method, SOSHibernateFactory.toString(item)));
        }
        if (item != null) {
            if (item.getChecked()) {
                counter.addSkip();
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("[%s][skip][check exist]checked=1", method));
                }
                return false;
            } else {
                if (item.getCheckText() != null && item.getCheckText().startsWith(CHECK_COMPLETED)) {
                    counter.addSkip();
                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("[%s][skip][check exist][checked=0]%s", method, item.getCheckText()));
                    }
                    return false;
                } else {
                    // compatibility: remove entries created by the previous version
                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("[%s][remove][check exist][checked=0][old]%s", method, item.getCheckText()));
                    }
                    if (handleTransaction) {
                        dbLayer.getSession().beginTransaction();
                    }
                    dbLayer.removeCheck(check.getId());
                    if (handleTransaction) {
                        dbLayer.getSession().commit();
                    }
                }
            }
        }
        return true;
    }

    private void analyzeJobCheck(int count, DBLayerSchedulerMon dbLayer, DBItemSchedulerMonChecks check, ElementTimer timer) throws Exception {
        String method = "analyzeJobCheck][" + count;

        if (isDebugEnabled) {
            LOGGER.debug(String.format("[%s]%s", method, NotificationModel.toString(check)));
        }

        if (!analyzeCheck(count, dbLayer, check, DBLayer.NOTIFICATION_OBJECT_TYPE_JOB)) {
            return;
        }

        DBItemSchedulerMonNotifications notification = dbLayer.getNotification(check.getNotificationId());
        if (notification == null) {
            counter.addSkip();
            if (isDebugEnabled) {
                LOGGER.debug(String.format("[%s][skip]not found notification ...", method));
            }
            return;
        }

        if (isDebugEnabled) {
            LOGGER.debug(String.format("[%s]%s", method, NotificationModel.toString(notification)));
        }

        if (notification.getStandalone()) {
            if (notification.getTaskStartTime() == null) {
                counter.addSkip();
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("[%s][skip][standalone]taskStartTime is NULL", method));
                }
                return;
            }
        } else {
            if (notification.getOrderStepStartTime() == null) {
                counter.addSkip();
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("[%s][skip][order]orderStepStartTime is NULL", method));
                }
                return;
            }
        }

        List<DBItemSchedulerMonNotifications> steps = new ArrayList<DBItemSchedulerMonNotifications>();
        steps.add(notification);
        timer = setTimerResult(dbLayer, timer, check, steps, notification.getId(), new Long(0), new Long(100000));
        if (timer.getTimerResult() == null) {
            counter.addSkip();
            if (isDebugEnabled) {
                LOGGER.debug(String.format("[%s][skip]timerResult is NULL", method));
            }
            return;
        }
        createJobCheck(dbLayer, check, notification, timer);
    }

    private void createJobCheck(DBLayerSchedulerMon dbLayer, DBItemSchedulerMonChecks check, DBItemSchedulerMonNotifications notification,
            ElementTimer timer) throws Exception {
        // output indent
        String method = "  createJobCheck";

        TimerResult timerResult = timer.getTimerResult();
        if (notification.getStandalone()) {
            timerResult.setStartTimeType(EStartTimeType.TASK);
            timerResult.setEndTimeType(EEndTimeType.TASK);
            timerResult.setStartTime(notification.getTaskStartTime());
            timerResult.setEndTime(notification.getTaskEndTime());
        } else {
            timerResult.setStartTimeType(EStartTimeType.ORDER_STEP);
            timerResult.setEndTimeType(EEndTimeType.ORDER_STEP);
            timerResult.setStartTime(notification.getOrderStepStartTime());
            timerResult.setEndTime(notification.getOrderStepEndTime());
        }

        if (timerResult.getEndTime() == null) {
            timerResult.setEndTimeType(EEndTimeType.CURRENT);
            timerResult.setEndTime(DBLayer.getCurrentDateTime());
        }

        Long diffSeconds = timerResult.getTimeDifferenceInSeconds();
        if (isDebugEnabled) {
            LOGGER.debug(String.format("[%s]%s", method, NotificationModel.toString(check)));
            LOGGER.debug(String.format("[%s]%s", method, NotificationModel.toString(notification)));

            LOGGER.debug(String.format("[%s][startTimeType=%s][endTimeType=%s][startTime=%s][endTime=%s]", method, timerResult.getStartTimeType(),
                    timerResult.getEndTimeType(), DBLayer.getDateAsString(timerResult.getStartTime()), DBLayer.getDateAsString(timerResult
                            .getEndTime())));
            LOGGER.debug(String.format("[%s][Maximum=%s][Minimum=%s][difference=%ss]", method, timerResult.getMaximum(), timerResult.getMinimum(),
                    diffSeconds));
        }
        StringBuilder checkText = null;
        if (timerResult.getMinimum() != null && diffSeconds < timerResult.getMinimum().doubleValue()) {
            if (!timerResult.getEndTimeType().equals(EEndTimeType.CURRENT)) {
                checkText = new StringBuilder("Execution time ");
                checkText.append(formatDoubleValue(diffSeconds.doubleValue())).append("s ");
                checkText.append("is less than the defined Minimum time ").append(formatDoubleValue(timerResult.getMinimum())).append("s.");
            }
        }
        if (timerResult.getMaximum() != null && diffSeconds > timerResult.getMaximum().doubleValue()) {
            if (timerResult.getEndTimeType().equals(EEndTimeType.CURRENT)) {
                checkText = new StringBuilder("Maximum time period ");
                checkText.append(formatDoubleValue(timerResult.getMaximum())).append("s ");
                checkText.append("is exceeded.");
            } else {
                checkText = new StringBuilder("Execution time ");
                checkText.append(formatDoubleValue(diffSeconds.doubleValue())).append("s ");
                checkText.append("is greater than the defined Maximum time ").append(formatDoubleValue(timer.getTimerResult().getMaximum())).append(
                        "s.");
            }
        }
        if (checkText == null) {
            if (timerResult.getEndTimeType().equals(EEndTimeType.CURRENT)) {
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("[%s][skip]Minimum or Maximum not reached", method));
                }
            } else {
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("[%s][skip]executed and no problems founded", method));
                }
            }
            counter.addSkip();
            return;
        }

        if (timerResult.getStartTimeType().equals(EStartTimeType.TASK)) {
            if (timerResult.getEndTimeType().equals(EEndTimeType.CURRENT)) {
                checkText.append(" Checked against the current execution time ").append(DBLayer.getDateAsString(timerResult.getEndTime())).append(
                        "(UTC). ");
                checkText.append(" Task started at ").append(DBLayer.getDateAsString(timerResult.getStartTime())).append("(UTC).");
            } else if (timerResult.getEndTimeType().equals(EEndTimeType.TASK)) {
                checkText.append(" Task started at ").append(DBLayer.getDateAsString(timerResult.getStartTime())).append("(UTC)");
                checkText.append(" and finished at ").append(DBLayer.getDateAsString(timerResult.getEndTime())).append("(UTC).");
            }
        } else if (timerResult.getStartTimeType().equals(EStartTimeType.ORDER_STEP)) {
            if (timerResult.getEndTimeType().equals(EEndTimeType.CURRENT)) {
                checkText.append(" Checked against the current execution time ").append(DBLayer.getDateAsString(timerResult.getEndTime())).append(
                        "(UTC). ");
                checkText.append(" Order step \"").append(notification.getOrderStepState()).append("\" started at ").append(DBLayer.getDateAsString(
                        timerResult.getStartTime())).append("(UTC).");
            } else if (timerResult.getEndTimeType().equals(EEndTimeType.ORDER_STEP)) {
                checkText.append(" Order step \"").append(notification.getOrderStepState()).append("\" started at ").append(DBLayer.getDateAsString(
                        timerResult.getStartTime())).append("(UTC)");
                checkText.append(" and finished at ").append(DBLayer.getDateAsString(timerResult.getEndTime())).append("(UTC).");
            }
        }

        try {
            insertCheck(dbLayer, check, timerResult, checkText, true);
            counter.addTotal();
            if (isDebugEnabled) {
                LOGGER.debug(String.format("[%s][created]%s", method, NotificationModel.toString(check)));
            }
        } catch (Exception ex) {
            if (handleTransaction) {
                try {
                    dbLayer.getSession().rollback();
                } catch (Exception e) {
                }
            }
            LOGGER.warn(ex.getMessage());
        }

    }

    private void analyzeJobChainCheck(int count, DBLayerSchedulerMon dbLayer, DBItemSchedulerMonChecks check, ElementTimer timer) throws Exception {
        String method = "analyzeJobChainCheck][" + count;

        if (isDebugEnabled) {
            LOGGER.debug(String.format("[%s]%s", method, NotificationModel.toString(check)));
        }

        if (!analyzeCheck(count, dbLayer, check, DBLayer.NOTIFICATION_OBJECT_TYPE_JOB_CHAIN)) {
            return;
        }

        Long stepFromIndex = new Long(0);
        Long stepToIndex = new Long(0);
        Long lastIndex = new Long(0);
        DBItemSchedulerMonNotifications checkNotification = null;
        DBItemSchedulerMonNotifications firstNotification = null;
        DBItemSchedulerMonNotifications stepFromNotification = null;
        DBItemSchedulerMonNotifications stepToNotification = null;
        List<DBItemSchedulerMonNotifications> steps = null;

        if (!check.getStepFrom().equals(DBLayerSchedulerMon.DEFAULT_EMPTY_NAME) || !check.getStepTo().equals(
                DBLayerSchedulerMon.DEFAULT_EMPTY_NAME)) {
            steps = dbLayer.getOrderNotificationsByNotificationId(check.getNotificationId());
            int c = 0;
            for (DBItemSchedulerMonNotifications step : steps) {
                if (c == 0) {
                    firstNotification = step;
                }
                c++;
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("[%s][step][%s]%s", method, c, NotificationModel.toString(step)));
                }
                if (step.getId().equals(check.getNotificationId())) {
                    checkNotification = step;
                }
                if (stepFromIndex.equals(new Long(0)) && step.getOrderStepState().equalsIgnoreCase(check.getStepFrom())) {
                    stepFromIndex = step.getStep();
                    stepFromNotification = step;
                }
                if (step.getOrderStepState().equalsIgnoreCase(check.getStepTo())) {
                    stepToIndex = step.getStep();
                    stepToNotification = step;
                }
                lastIndex = step.getStep();
            }

            if (checkNotification == null) {
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("[%s][checkNotification not founded]notification id=%s", method, check.getNotificationId()));
                }
                if (firstNotification == null) {
                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("[%s][skip]firstNotification is NULL", method));
                    }
                    counter.addSkip();
                    return;
                } else {
                    checkNotification = firstNotification;
                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("[%s]checkNotification set to notification id=%s", method, checkNotification.getId()));
                    }
                }
            }

            if (stepFromNotification == null && !check.getStepFrom().equals(DBLayerSchedulerMon.DEFAULT_EMPTY_NAME)) {
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("[%s][skip][step_from=%s]step not executed. try again later...", method, check.getStepFrom()));
                }
                counter.addSkip();
                return;
            }
        } else {
            checkNotification = dbLayer.getNotification(check.getNotificationId());
            if (checkNotification == null) {
                counter.addSkip();
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("[%s][skip]not found the checkNotification ...", method));
                }
                return;
            }
        }

        if (isDebugEnabled) {
            LOGGER.debug(String.format("[%s]%s", method, NotificationModel.toString(checkNotification)));
        }

        if (checkNotification.getOrderStartTime() == null) {
            counter.addSkip();
            if (isDebugEnabled) {
                LOGGER.debug(String.format("[%s][skip]getOrderStartTime is NULL", method));
            }
            return;
        }
        if (stepToIndex.equals(new Long(0))) {
            stepToIndex = lastIndex;
        }

        Long resultNotificationId = checkNotification == null ? new Long(0) : checkNotification.getId();
        timer = setTimerResult(dbLayer, timer, check, steps, resultNotificationId, stepFromIndex, stepToIndex);
        if (timer.getTimerResult() == null) {
            counter.addSkip();
            if (isDebugEnabled) {
                LOGGER.debug(String.format("[%s][skip]timerResult is NULL", method));
            }
            return;
        }
        createJobChainCheck(dbLayer, check, checkNotification, stepFromNotification, stepToNotification, timer);
    }

    private void createJobChainCheck(DBLayerSchedulerMon dbLayer, DBItemSchedulerMonChecks check, DBItemSchedulerMonNotifications resultNotification,
            DBItemSchedulerMonNotifications stepFromNotification, DBItemSchedulerMonNotifications stepToNotification, ElementTimer timer)
            throws Exception {

        // output indent
        String method = "  createJobChainCheck";
        if (isDebugEnabled) {
            LOGGER.debug(String.format("[%s][check]%s", method, NotificationModel.toString(check)));
            LOGGER.debug(String.format("[%s][resultNotification]%s", method, NotificationModel.toString(resultNotification)));
            LOGGER.debug(String.format("[%s][stepFromNotification]%s", method, NotificationModel.toString(stepFromNotification)));
            String stn = "";
            if (stepToNotification == null && !check.getStepTo().equals(DBLayerSchedulerMon.DEFAULT_EMPTY_NAME)) {
                stn = String.format("[not founded step_to=%s]", check.getStepTo());
            }
            LOGGER.debug(String.format("[%s][stepToNotification]%s%s", method, stn, NotificationModel.toString(stepToNotification)));
        }

        TimerResult timerResult = timer.getTimerResult();
        timerResult.setStartTimeType(EStartTimeType.ORDER);
        timerResult.setEndTimeType(EEndTimeType.ORDER);
        timerResult.setStartTime(resultNotification.getOrderStartTime());
        timerResult.setEndTime(resultNotification.getOrderEndTime());

        if (stepFromNotification != null) {
            if (stepFromNotification.getOrderStepStartTime() == null) {
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("[%s][skip][stepFromNotification]getOrderStepStartTime is NULL", method));
                }
                counter.addSkip();
                return;
            }
            timerResult.setStartTimeType(EStartTimeType.ORDER_STEP);
            timerResult.setStartTime(stepFromNotification.getOrderStepStartTime());
        }
        if (stepToNotification != null) {
            timerResult.setEndTimeType(EEndTimeType.ORDER_STEP);
            timerResult.setEndTime(stepToNotification.getOrderStepEndTime());
        }
        if (timerResult.getEndTime() == null) {
            timerResult.setEndTimeType(EEndTimeType.CURRENT);
            timerResult.setEndTime(DBLayer.getCurrentDateTime());
        }

        Long diffSeconds = timerResult.getTimeDifferenceInSeconds();
        if (isDebugEnabled) {
            LOGGER.debug(String.format("[%s][startTimeType=%s][endTimeType=%s][startTime=%s][endTime=%s]", method, timerResult.getStartTimeType(),
                    timerResult.getEndTimeType(), DBLayer.getDateAsString(timerResult.getStartTime()), DBLayer.getDateAsString(timerResult
                            .getEndTime())));
            LOGGER.debug(String.format("[%s][Maximum=%s][Minimum=%s][difference=%ss]", method, timerResult.getMaximum(), timerResult.getMinimum(),
                    diffSeconds));
        }

        StringBuilder checkText = null;
        boolean minimumExceeded = false;
        if (timerResult.getMinimum() != null) {
            if (diffSeconds < timerResult.getMinimum().doubleValue()) {
                if (!timerResult.getEndTimeType().equals(EEndTimeType.CURRENT)) {
                    checkText = new StringBuilder("Execution time ");
                    checkText.append(formatDoubleValue(diffSeconds.doubleValue())).append("s ");
                    checkText.append("is less than the defined Minimum time ").append(formatDoubleValue(timerResult.getMinimum())).append("s.");
                }
            } else {
                minimumExceeded = true;
            }
        }
        if (timerResult.getMaximum() != null) {
            if (diffSeconds > timerResult.getMaximum().doubleValue()) {
                if (timerResult.getEndTimeType().equals(EEndTimeType.CURRENT)) {
                    checkText = new StringBuilder("Maximum time period ");
                    checkText.append(formatDoubleValue(timerResult.getMaximum())).append("s ");
                    checkText.append("is exceeded.");
                } else {
                    checkText = new StringBuilder("Execution time ");
                    checkText.append(formatDoubleValue(diffSeconds.doubleValue())).append("s ");
                    checkText.append("is greater than the defined Maximum time ").append(formatDoubleValue(timerResult.getMaximum())).append("s.");
                }
            }
        }
        if (checkText == null) {
            boolean close = false;
            if (timerResult.getEndTimeType().equals(EEndTimeType.CURRENT)) {
                if (minimumExceeded) {
                    if (timerResult.getMaximum() == null) {
                        close = true;
                    } else {
                        if (isDebugEnabled) {
                            LOGGER.debug(String.format("[%s][skip]Minimum OK. Maximum not exceeded", method));
                        }
                    }
                } else {
                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("[%s][skip]Minimum or Maximum not exceeded", method));
                    }
                }
            } else {
                close = true;
            }
            if (close) {
                StringBuilder txt = new StringBuilder(CHECK_COMPLETED);
                txt.append("[Maximum=").append(timerResult.getMaximum()).append("]");
                txt.append("[Minimum=").append(timerResult.getMinimum()).append("]");
                txt.append("difference=").append(diffSeconds);
                insertCheck(dbLayer, check, timerResult, txt, false);
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("[%s][skip][created]%s", method, check.getCheckText()));
                }
            }
            counter.addSkip();
            return;
        }
        if (timerResult.getStartTimeType().equals(EStartTimeType.ORDER)) {
            if (timerResult.getEndTimeType().equals(EEndTimeType.CURRENT)) {
                checkText.append(" Checked against the current execution time ").append(DBLayer.getDateAsString(timerResult.getEndTime())).append(
                        "(UTC). ");
                checkText.append(" Order \"").append(resultNotification.getOrderId()).append("\" started at ").append(DBLayer.getDateAsString(
                        timerResult.getStartTime())).append("(UTC).");
            } else if (timerResult.getEndTimeType().equals(EEndTimeType.ORDER)) {
                checkText.append(" Order \"").append(resultNotification.getOrderId()).append("\" started at ").append(DBLayer.getDateAsString(
                        timerResult.getStartTime())).append("(UTC)");
                checkText.append(" and finished at ").append(DBLayer.getDateAsString(timerResult.getEndTime())).append("(UTC).");
            } else if (timerResult.getEndTimeType().equals(EEndTimeType.ORDER_STEP)) {
                checkText.append(" Order \"").append(resultNotification.getOrderId()).append("\" started at ").append(DBLayer.getDateAsString(
                        timerResult.getStartTime())).append("(UTC)");
                checkText.append(" and the order step ");
                if (stepToNotification != null) {
                    checkText.append("\"").append(stepToNotification.getOrderStepState()).append("\" ");
                }
                checkText.append("finished at ").append(DBLayer.getDateAsString(timerResult.getEndTime())).append("(UTC).");
            }
        } else if (timerResult.getStartTimeType().equals(EStartTimeType.ORDER_STEP)) {
            if (timerResult.getEndTimeType().equals(EEndTimeType.CURRENT)) {
                checkText.append(" Checked against the current execution time ").append(DBLayer.getDateAsString(timerResult.getEndTime())).append(
                        "(UTC). ");
                checkText.append(" Order step ");
                if (stepFromNotification != null) {
                    checkText.append("\"").append(stepFromNotification.getOrderStepState()).append("\" ");
                }
                checkText.append("started at ").append(DBLayer.getDateAsString(timerResult.getStartTime())).append("(UTC).");
            } else if (timerResult.getEndTimeType().equals(EEndTimeType.ORDER)) {
                checkText.append(" Order step ");
                if (stepFromNotification != null) {
                    checkText.append("\"").append(stepFromNotification.getOrderStepState()).append("\" ");
                }
                checkText.append("started at ").append(DBLayer.getDateAsString(timerResult.getStartTime())).append("(UTC)");
                checkText.append(" and the order \"").append(resultNotification.getOrderId()).append("\" finished at ").append(DBLayer
                        .getDateAsString(timerResult.getEndTime())).append("(UTC).");
            } else if (timerResult.getEndTimeType().equals(EEndTimeType.ORDER_STEP)) {
                checkText.append(" Order step ");
                if (stepFromNotification != null) {
                    checkText.append("\"").append(stepFromNotification.getOrderStepState()).append("\" ");
                }
                checkText.append("started at ").append(DBLayer.getDateAsString(timerResult.getStartTime())).append("(UTC)");
                checkText.append(" and ");
                if (stepToNotification != null && (stepFromNotification == null || !stepToNotification.getOrderStepState().equals(stepFromNotification
                        .getOrderStepState()))) {
                    checkText.append("the order step \"").append(stepToNotification.getOrderStepState()).append("\" ");
                }
                checkText.append("finished at ").append(DBLayer.getDateAsString(timerResult.getEndTime())).append("(UTC).");
            }
        }

        try {
            insertCheck(dbLayer, check, timerResult, checkText, true);
            counter.addTotal();
            if (isDebugEnabled) {
                LOGGER.debug(String.format("[%s][created]%s", method, NotificationModel.toString(check)));
            }
        } catch (Exception ex) {
            if (handleTransaction) {
                try {
                    dbLayer.getSession().rollback();
                } catch (Exception e) {
                }
            }
            LOGGER.warn(ex.getMessage());
        }
    }

    private void insertCheck(DBLayerSchedulerMon dbLayer, DBItemSchedulerMonChecks check, TimerResult timerResult, StringBuilder checkText,
            boolean checked) throws Exception {
        if (handleTransaction) {
            dbLayer.getSession().beginTransaction();
        }

        String rids = timerResult.getResultIds().toString();
        check.setResultIds(SOSString.isEmpty(rids) ? null : rids);
        check.setStepFromStartTime(timerResult.getStartTime());
        check.setStepToEndTime(timerResult.getEndTimeType().equals(EEndTimeType.CURRENT) ? null : timerResult.getEndTime());
        check.setCheckText(checkText.toString());
        check.setChecked(checked);
        check.setCreated(DBLayer.getCurrentDateTime());
        check.setModified(check.getCreated());
        dbLayer.getSession().save(check);

        if (handleTransaction) {
            dbLayer.getSession().commit();
        }
    }

    public String formatDoubleValue(Double d) {
        if (d == d.longValue()) {
            return String.valueOf(d.longValue());
        }
        String s = null;
        if (d < 0.01) {
            if (d > 0.0001) {
                s = String.format("%.4f", d);
            } else if (d > 0.000001) {
                s = String.format("%.8f", d);
            }
        } else {
            s = String.format("%.2f", d);
        }
        return s;
    }

    public Double evalScript(ElementTimerScript script, String text) throws Exception {
        if (script == null) {
            throw new NullPointerException("script is NULL");
        }
        if (text == null) {
            throw new NullPointerException("text is NULL");
        }
        ScriptEngine engine = scriptEngineManager.getEngineByName(script.getLanguage());
        if (engine == null) {
            try {
                logAvailableEngines();
            } catch (Exception ex) {
            }
            throw new NullPointerException(String.format("[%s]ScriptEngine \"%s\" is not available", script.getElementTitle(), script.getLanguage()));
        }
        return ((Number) engine.eval(text)).doubleValue();
    }

    private String resolveParam(String text, String param, String value) throws Exception {
        if (text == null || param == null || value == null) {
            return null;
        }
        // quote values with paths
        value = value.replaceAll("\\\\", "\\\\\\\\");
        value = Matcher.quoteReplacement(value);

        String result = text.replaceAll("%(?i)" + param + "%", value);
        result = result.replaceAll("\\$\\{(?i)" + param + "\\}", value);

        return result.indexOf("${") > -1 ? null : result;
    }

    public void logAvailableEngines() {
        List<ScriptEngineFactory> factories = scriptEngineManager.getEngineFactories();
        if (factories == null) {
            LOGGER.info("No available script engines were found.");
        } else {
            LOGGER.info("Available script engines:");
            for (int i = 0; i < factories.size(); i++) {
                ScriptEngineFactory factory = factories.get(i);
                String en = factory.getEngineName();
                String language = factory.getLanguageName();
                LOGGER.info(String.format("- language=%s, engineName=%s", language, en));
            }
        }
    }
}
