package com.sos.jitl.notification.plugins.history;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sos.util.SOSString;

import com.sos.jitl.notification.db.DBItemSchedulerMonChecks;
import com.sos.jitl.notification.db.DBItemSchedulerMonNotifications;
import com.sos.jitl.notification.db.DBItemSchedulerMonResults;
import com.sos.jitl.notification.db.DBLayer;
import com.sos.jitl.notification.db.DBLayerSchedulerMon;
import com.sos.jitl.notification.helper.CounterCheckHistoryTimer;
import com.sos.jitl.notification.helper.EEndTimeType;
import com.sos.jitl.notification.helper.EStartTimeType;
import com.sos.jitl.notification.helper.ElementTimer;
import com.sos.jitl.notification.helper.ElementTimer.TimerResult;
import com.sos.jitl.notification.helper.ElementTimerScript;
import com.sos.jitl.notification.jobs.history.CheckHistoryJobOptions;
import com.sos.jitl.reporting.helper.ReportUtil;

public class CheckHistoryTimerPlugin implements ICheckHistoryPlugin {

    private static final Logger LOGGER = LoggerFactory.getLogger(CheckHistoryTimerPlugin.class);
    private CounterCheckHistoryTimer counter;
    private boolean handleTransaction = false;
    private ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
    private long maxUncompletedCheckLifetimeInMinutes = 1440;// 1day

    @Override
    public void onInit(LinkedHashMap<String, ElementTimer> timers, CheckHistoryJobOptions options, DBLayerSchedulerMon dbLayer) throws Exception {

    }

    @Override
    public void onExit(LinkedHashMap<String, ElementTimer> timers, CheckHistoryJobOptions options, DBLayerSchedulerMon dbLayer) throws Exception {
    }

    @Override
    public void onProcess(LinkedHashMap<String, ElementTimer> timers, CheckHistoryJobOptions options, DBLayerSchedulerMon dbLayer, Date dateFrom,
            Date dateTo) throws Exception {
        String method = "onProcess";

        if (dbLayer == null) {
            throw new Exception("dbLayer is NULL");
        }
        if (timers == null) {
            throw new Exception("timers is NULL");
        }

        if (timers.size() == 0) {
            LOGGER.info(String.format("%s: skip. found 0 timers definitions", method));
            return;
        }

        initCountChecks();

        Optional<Integer> largeResultFetchSize = Optional.empty();
        try {
            int fetchSize = options.large_result_fetch_size.value();
            if (fetchSize != -1) {
                largeResultFetchSize = Optional.of(fetchSize);
            }
        } catch (Exception ex) {
        }

        List<DBItemSchedulerMonChecks> result = dbLayer.getSchedulerMonChecksForSetTimer(largeResultFetchSize);
        LOGGER.info(String.format("%s: found %s timer definitions and %s timers for check in the db", method, timers.size(), result.size()));

        for (int i = 0; i < result.size(); i++) {
            DBItemSchedulerMonChecks check = result.get(i);
            if (!timers.containsKey(check.getName())) {
                counter.addSkip();
                LOGGER.debug(String.format("%s:[skip check][%s]timer definition is not found.", method, check.getName()));
                continue;
            }
            ElementTimer timer = timers.get(check.getName());
            if (timer.getMinimum() == null && timer.getMaximum() == null) {
                counter.addSkip();
                LOGGER.debug(String.format("%s:[skip check][%s]timer %s have not the Minimum or Maximum elements.", method, check.getName(), timer
                        .getName()));
                continue;
            }
            check = checkNotification(check, dbLayer);
            if (check.getObjectType().equals(DBLayer.NOTIFICATION_OBJECT_TYPE_JOB)) {
                analyzeJobCheck(dbLayer, check, timer);
            } else {
                analyzeJobChainCheck(dbLayer, check, timer);
            }
        }

        LOGGER.info(String.format("%s: checks created=%s, removed=%s, skipped=%s, checks for rerun=%s", method, counter.getTotal(), counter
                .getRemove(), counter.getSkip(), counter.getRerun()));
    }

    private DBItemSchedulerMonChecks checkNotification(DBItemSchedulerMonChecks check, DBLayerSchedulerMon dbLayer) throws Exception {
        String method = "checkNotification";

        // wegen batch insert bei den Datenbanken ohne autoincrement
        if (check.getNotificationId().equals(new Long(0))) {
            if (SOSString.isEmpty(check.getResultIds())) {
                throw new Exception(String.format("%s: could not execute check(id=%s): notificationId=0, resultIds is empty", method, check.getId()));
            }
            String[] arr = check.getResultIds().split(";");
            if (arr.length < 5) {
                throw new Exception(String.format("%s: could not execute check(id=%s): missing notification infos. resultIds=%s", method, check
                        .getId(), check.getResultIds()));
            }

            DBItemSchedulerMonNotifications notification = dbLayer.getNotification(arr[0], Boolean.parseBoolean(arr[1]), new Long(arr[2]), new Long(
                    arr[3]), new Long(arr[4]), false);
            if (notification == null) {
                throw new Exception(String.format(
                        "%s: could not execute check(id=%s): notification not found, schedulerId=%s, standalone=%s, taskId=%s, step=%s, orderHistoryId=%s",
                        method, check.getId(), arr[0], arr[1], arr[2], arr[3], arr[4]));
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

    private void removeCheck(DBLayerSchedulerMon dbLayer, DBItemSchedulerMonChecks check) throws Exception {
        if (handleTransaction) {
            dbLayer.getSession().beginTransaction();
        }
        dbLayer.removeCheck(check.getId());
        if (handleTransaction) {
            dbLayer.getSession().commit();
        }
        counter.addRemove();
    }

    private void removeCheckIfTooOld(DBLayerSchedulerMon dbLayer, DBItemSchedulerMonChecks check) throws Exception {
        String method = "removeCheckIfTooOld";
        if (check.getStepFromStartTime() != null) {
            Date currentDateTime = DBLayer.getCurrentDateTime();
            long diffAsMinutes = (currentDateTime.getTime() - check.getStepFromStartTime().getTime()) / 1000 / 60;
            if (diffAsMinutes > maxUncompletedCheckLifetimeInMinutes) {
                LOGGER.info(String.format(
                        "%s:[remove uncompleted check older as %s minutes][check name=%s, id=%s, stepStartTime=%s][current time=%s]", method,
                        maxUncompletedCheckLifetimeInMinutes, check.getName(), check.getId(), ReportUtil.getDateAsString(check
                                .getStepFromStartTime()), ReportUtil.getDateAsString(currentDateTime)));
                removeCheck(dbLayer, check);
            }
        }
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
                LOGGER.debug(String.format("%s:[%s]value=%s", method, script.getElementTitle(), scriptValue));
            }
        }
        return scriptValue;
    }

    private ElementTimer setTimerResultScriptValue(DBItemSchedulerMonChecks check, ElementTimer timer, ElementTimerScript element,
            String elementValue) {
        String method = "setTimerResultScriptValue";
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
                LOGGER.debug(String.format("%s:[check name=%s, id=%s][timer %s]%s", method, check.getName(), check.getId(), element.getElementTitle(),
                        result));
            } catch (NullPointerException ex) {
                LOGGER.warn(String.format("%s:[check name=%s, id=%s][timer %s]%s:%s", method, check.getName(), check.getId(), element
                        .getElementTitle(), elementValue, ex.getMessage()));
                timer.resetTimerResult();
            } catch (Exception ex) {
                if (containsVariables(element.getValue())) {
                    LOGGER.debug(String.format("%s:[setReadDbResults][check name=%s, id=%s][timer %s]", method, check.getName(), check.getId(),
                            element.getElementTitle()));

                    timer.getTimerResult().setLastErrorMessage(String.format("[check name=%s, id=%s][timer %s]%s:%s", check.getName(), check.getId(),
                            element.getElementTitle(), elementValue, ex.getMessage()));
                    timer.getTimerResult().setReadDbResults(true);
                } else {
                    LOGGER.warn(String.format("%s:[check name=%s, id=%s][timer %s]%s:%s", method, check.getName(), check.getId(), element
                            .getElementTitle(), elementValue, ex.getMessage()));
                    timer.resetTimerResult();
                }
            }
        }
        return timer;
    }

    private ElementTimer setTimerResult(DBLayerSchedulerMon dbLayer, ElementTimer timer, DBItemSchedulerMonChecks check,
            List<DBItemSchedulerMonNotifications> steps, Long resultNotificationId, Long stepFromIndex, Long stepToIndex) throws Exception {
        String method = "setTimerResult";
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
            LOGGER.debug(String.format("%s:[get db results][check name=%s, id=%s, notificationId=%s]", method, check.getName(), check.getId(), check
                    .getNotificationId()));

            String minValue = null;
            String maxValue = null;
            for (DBItemSchedulerMonNotifications step : steps) {
                if (step.getStep() >= stepFromIndex && step.getStep() <= stepToIndex) {
                    LOGGER.debug(String.format(
                            "%s:[get db results][check name=%s, id=%s, notificationId=%s][step id=%s, step=%s, jobChainName=%s, jobName=%s]", method,
                            check.getName(), check.getId(), check.getNotificationId(), step.getId(), step.getStep(), step.getJobChainName(), step
                                    .getJobName()));
                    List<DBItemSchedulerMonResults> params = dbLayer.getNotificationResults(step.getId());
                    if (params != null) {
                        LOGGER.debug(String.format(
                                "%s:[found %s results][check name=%s, id=%s, notificationId=%s][step id=%s, step=%s, jobChainName=%s, jobName=%s]",
                                method, params.size(), check.getName(), check.getId(), check.getNotificationId(), step.getId(), step.getStep(), step
                                        .getJobChainName(), step.getJobName()));
                        int ri = 0;
                        for (DBItemSchedulerMonResults param : params) {
                            ri++;
                            if (ri > 1) {
                                resultIds.append(";");
                            }
                            resultIds.append(param.getId());

                            LOGGER.debug(String.format("%s:    param=%s, value=%s", method, param.getName(), param.getValue()));

                            if (timer.getTimerResult().getMinimum() == null) {
                                minValue = resolveParam(minElement, param, minValue);
                            }
                            if (timer.getTimerResult().getMaximum() == null) {
                                maxValue = resolveParam(maxElement, param, maxValue);
                            }
                        }
                    } else {
                        LOGGER.debug(String.format(
                                "%s:[not found db results][check name=%s, id=%s, notificationId=%s][step id=%s, step=%s, jobChainName=%s, jobName=%s]",
                                method, check.getName(), check.getId(), check.getNotificationId(), step.getId(), step.getStep(), step
                                        .getJobChainName(), step.getJobName()));
                        if (timer.getTimerResult().getLastErrorMessage() != null) {
                            LOGGER.warn(timer.getTimerResult().getLastErrorMessage());
                        }
                    }
                }
            }
            if (minValue == null && maxValue == null) {
                LOGGER.info(String.format("%s:[skip][Minimum or Maximum not resolved][check name=%s, id=%s, notificationId=%s]", method, check
                        .getName(), check.getId(), check.getNotificationId()));
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

    private void analyzeJobCheck(DBLayerSchedulerMon dbLayer, DBItemSchedulerMonChecks check, ElementTimer timer) throws Exception {
        String method = "analyzeJobCheck";

        LOGGER.debug(String.format("%s:[check name=%s, id=%s, notificationId=%s]", method, check.getName(), check.getId(), check
                .getNotificationId()));

        DBItemSchedulerMonNotifications notification = dbLayer.getNotification(check.getNotificationId());
        if (notification == null) {
            counter.addSkip();
            LOGGER.warn(String.format("[skip][not found notification][check name=%s, id=%s, notificationId=%s]remove check ...", check.getName(),
                    check.getId(), check.getNotificationId()));
            removeCheck(dbLayer, check);
            return;
        }

        if (notification.getStandalone()) {
            if (notification.getTaskStartTime() == null) {
                counter.addSkip();
                LOGGER.debug(String.format("[skip][standalone notification][taskStartTime is NULL][check name=%s, id=%s]", check.getName(),
                        notification.getId()));
                removeCheckIfTooOld(dbLayer, check);
                return;
            }
        } else {
            if (notification.getOrderStepStartTime() == null) {
                counter.addSkip();
                LOGGER.debug(String.format("[skip][order notification][orderStepStartTime is NULL][check name=%s, id=%s]", check.getName(),
                        notification.getId()));
                removeCheckIfTooOld(dbLayer, check);
                return;
            }
        }

        List<DBItemSchedulerMonNotifications> steps = new ArrayList<DBItemSchedulerMonNotifications>();
        steps.add(notification);
        timer = setTimerResult(dbLayer, timer, check, steps, notification.getId(), new Long(0), new Long(100000));
        if (timer.getTimerResult() == null) {
            counter.addSkip();
            LOGGER.warn(String.format("[skip][timerResult is NULL][check name=%s, id=%s][notification id=%s, jobName=%s]", check.getName(), check
                    .getId(), notification.getId(), notification.getJobName()));
            removeCheckIfTooOld(dbLayer, check);
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
            timerResult.setStartTime(notification.getOrderStartTime());
            timerResult.setEndTime(notification.getOrderStepEndTime());
        }

        if (timerResult.getEndTime() == null) {
            timerResult.setEndTimeType(EEndTimeType.CURRENT);
            timerResult.setEndTime(DBLayer.getCurrentDateTime());
        }

        Long diffSeconds = timerResult.getTimeDifferenceInSeconds();
        LOGGER.debug(String.format("%s: id=%s(notification.id=%s), difference=%ss, startTimeType=%s, endTimeType=%s, startTime=%s, endTime=%s",
                method, check.getId(), notification.getId(), diffSeconds, timerResult.getStartTimeType(), timerResult.getEndTimeType(), DBLayer
                        .getDateAsString(timerResult.getStartTime()), DBLayer.getDateAsString(timerResult.getEndTime())));

        String checkText = null;
        String checkTextTime = "";
        if (timerResult.getEndTimeType() == null) {
            LOGGER.info(String.format("%s: endTimeType is NULL", method));
        } else {
            if (timerResult.getStartTimeType().equals(EStartTimeType.TASK)) {
                if (timerResult.getEndTimeType().equals(EEndTimeType.CURRENT)) {
                    checkTextTime = String.format("task started at %s(UTC) and is not yet finished... checked vs. current datetime %s(UTC).", DBLayer
                            .getDateAsString(timerResult.getStartTime()), DBLayer.getDateAsString(timerResult.getEndTime()));
                } else if (timerResult.getEndTimeType().equals(EEndTimeType.TASK)) {
                    checkTextTime = String.format("task started at %s(UTC) and finished at %s(UTC)", DBLayer.getDateAsString(timerResult
                            .getStartTime()), DBLayer.getDateAsString(timerResult.getEndTime()));
                }
            } else if (timerResult.getStartTimeType().equals(EStartTimeType.ORDER_STEP)) {
                if (timerResult.getEndTimeType().equals(EEndTimeType.CURRENT)) {
                    checkTextTime = String.format("job step %s started at %s(UTC) and is not yet finished... checked vs. current datetime %s(UTC).",
                            notification.getOrderStepState(), DBLayer.getDateAsString(timerResult.getStartTime()), DBLayer.getDateAsString(timerResult
                                    .getEndTime()));
                } else if (timerResult.getEndTimeType().equals(EEndTimeType.ORDER_STEP)) {
                    checkTextTime = String.format("job step %s started at %s(UTC) and finished at %s(UTC)", notification.getOrderStepState(), DBLayer
                            .getDateAsString(timerResult.getStartTime()), DBLayer.getDateAsString(timerResult.getEndTime()));
                }

            }
        }

        if (timerResult.getMinimum() != null && diffSeconds < timerResult.getMinimum().doubleValue()) {
            String newVal = formatDoubleValue(timerResult.getMinimum());
            checkText = String.format("execution time %ss is less than the defined minimum time %ss. %s", formatDoubleValue(diffSeconds
                    .doubleValue()), newVal, checkTextTime);
        }
        if (timerResult.getMaximum() != null && diffSeconds > timerResult.getMaximum().doubleValue()) {
            String newVal = formatDoubleValue(timer.getTimerResult().getMaximum());
            checkText = String.format("execution time %ss is greater than the defined maximum time %ss. %s", formatDoubleValue(diffSeconds
                    .doubleValue()), newVal, checkTextTime);
        }

        if (checkText == null) {
            if (!timerResult.getEndTimeType().equals(EEndTimeType.CURRENT)) {
                LOGGER.debug(String.format("%s:[remove][check name=%s, id=%s,startTimeType=%s, endTimeType=%s]executed and found no problems.",
                        method, check.getName(), check.getId(), timerResult.getStartTimeType(), timerResult.getEndTimeType()));
                removeCheck(dbLayer, check);
            }
        } else {
            try {
                if (handleTransaction) {
                    dbLayer.getSession().beginTransaction();
                }
                if (timerResult.getEndTimeType().equals(EEndTimeType.CURRENT) && check.getCheckText() == null) {
                    checkText = String.format("not set as checked. do one rerun. %s", checkText);

                    LOGGER.debug(String.format("%s:[rerun][check name=%s, id=%s, text=%s, resultIds=%s]", method, check.getName(), check.getId(),
                            checkText, timer.getTimerResult().getResultIds()));

                    dbLayer.setNotificationCheckForRerun(check, timerResult.getStartTime(), timerResult.getEndTime(), checkText, timer
                            .getTimerResult().getResultIds().toString());

                    counter.addRerun();
                } else {
                    LOGGER.debug(String.format("%s:[update][check name=%s, id=%s, text=%s, resultIds=%s]", method, check.getName(), check.getId(),
                            checkText, timer.getTimerResult().getResultIds()));

                    dbLayer.setNotificationCheck(check, timerResult.getStartTime(), timerResult.getEndTime(), checkText, timer.getTimerResult()
                            .getResultIds().toString());

                    counter.addTotal();
                }
                if (handleTransaction) {
                    dbLayer.getSession().commit();
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
    }

    private void analyzeJobChainCheck(DBLayerSchedulerMon dbLayer, DBItemSchedulerMonChecks check, ElementTimer timer) throws Exception {
        String method = "analyzeJobChainCheck";

        LOGGER.debug(String.format("%s:[check name=%s, id=%s, stepFrom=%s, stepTo=%s, notificationId=%s]", method, check.getName(), check.getId(),
                check.getStepFrom(), check.getStepTo(), check.getNotificationId()));

        Long stepFromIndex = new Long(0);
        Long stepToIndex = new Long(0);
        Long lastIndex = new Long(0);
        DBItemSchedulerMonNotifications minNotification = null;
        DBItemSchedulerMonNotifications stepFromNotification = null;
        DBItemSchedulerMonNotifications stepToNotification = null;

        List<DBItemSchedulerMonNotifications> steps = dbLayer.getNotificationOrderSteps(check.getNotificationId());
        for (DBItemSchedulerMonNotifications step : steps) {
            if (step.getId().equals(check.getNotificationId())) {
                minNotification = step;
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

        if (minNotification == null) {
            return;
        }
        if (minNotification.getOrderStartTime() == null) {
            counter.addSkip();
            LOGGER.debug(String.format("[skip][getOrderStartTime is NULL][minNotification id=%s]", minNotification.getId()));
            removeCheckIfTooOld(dbLayer, check);
            return;
        }
        if (stepToIndex.equals(new Long(0))) {
            stepToIndex = lastIndex;
        }

        Long resultNotificationId = minNotification == null ? new Long(0) : minNotification.getId();
        timer = setTimerResult(dbLayer, timer, check, steps, resultNotificationId, stepFromIndex, stepToIndex);
        if (timer.getTimerResult() == null) {
            counter.addSkip();
            LOGGER.warn(String.format("[skip][timerResult is NULL][check name=%s, id=%s, notificationId=%s]", check.getName(), check.getId(), check
                    .getNotificationId()));
            removeCheckIfTooOld(dbLayer, check);
            return;
        }
        createJobChainCheck(dbLayer, check, minNotification, stepFromNotification, stepToNotification, timer);
    }

    private void createJobChainCheck(DBLayerSchedulerMon dbLayer, DBItemSchedulerMonChecks check, DBItemSchedulerMonNotifications resultNotification,
            DBItemSchedulerMonNotifications stepFromNotification, DBItemSchedulerMonNotifications stepToNotification, ElementTimer timer)
            throws Exception {

        // output indent
        String method = "  createJobChainCheck";

        TimerResult timerResult = timer.getTimerResult();
        timerResult.setStartTimeType(EStartTimeType.ORDER);
        timerResult.setEndTimeType(EEndTimeType.ORDER);

        Long resultNotificationId = resultNotification == null ? new Long(0) : resultNotification.getId();
        Date stepFromStartTime = resultNotification.getOrderStartTime();
        Date stepToEndTime = resultNotification.getOrderEndTime();

        if (stepFromNotification == null) {
            timerResult.setStartTime(resultNotification.getOrderStartTime());
        } else {
            timerResult.setStartTimeType(EStartTimeType.ORDER_STEP);
            timerResult.setStartTime(stepFromNotification.getOrderStepStartTime());

            stepFromStartTime = timerResult.getStartTime();
            if (timerResult.getStartTime() == null) {
                LOGGER.debug(String.format("%s:[skip][getOrderStepStartTime is NULL][stepFromNotification id=%s]", method, stepFromNotification
                        .getId()));
                return;
            }
        }
        if (stepToNotification == null) {
            timerResult.setEndTime(resultNotification.getOrderEndTime());
        } else {
            timerResult.setEndTimeType(EEndTimeType.ORDER_STEP);
            timerResult.setEndTime(stepToNotification.getOrderStepEndTime());
            stepToEndTime = timerResult.getEndTime();
        }

        if (timerResult.getEndTime() == null) {
            timerResult.setEndTimeType(EEndTimeType.CURRENT);
            timerResult.setEndTime(DBLayer.getCurrentDateTime());
        }

        Long diffSeconds = timerResult.getTimeDifferenceInSeconds();
        LOGGER.debug(String.format("%s: id=%s(resultNotificationId=%s), difference=%ss, startTimeType=%s, endTimeType=%s, startTime=%s, endTime=%s",
                method, check.getId(), resultNotificationId, diffSeconds, timerResult.getStartTimeType(), timerResult.getEndTimeType(), DBLayer
                        .getDateAsString(timerResult.getStartTime()), DBLayer.getDateAsString(timerResult.getEndTime())));

        String checkText = null;
        String checkTextTime = "";
        if (timerResult.getEndTimeType() == null) {
            LOGGER.info(String.format("%s: endTimeType is NULL", method));
        } else {
            if (timerResult.getStartTimeType().equals(EStartTimeType.ORDER)) {
                if (timerResult.getEndTimeType().equals(EEndTimeType.CURRENT)) {
                    checkTextTime = String.format("order started at %s(UTC) and is not yet finished... checked vs. current datetime %s(UTC).", DBLayer
                            .getDateAsString(timerResult.getStartTime()), DBLayer.getDateAsString(timerResult.getEndTime()));
                } else if (timerResult.getEndTimeType().equals(EEndTimeType.ORDER)) {
                    checkTextTime = String.format("order started at %s(UTC) and finished at %s(UTC)", DBLayer.getDateAsString(timerResult
                            .getStartTime()), DBLayer.getDateAsString(timerResult.getEndTime()));
                } else if (timerResult.getEndTimeType().equals(EEndTimeType.ORDER_STEP)) {
                    checkTextTime = String.format("order started at %s(UTC) and step %s finished at %s(UTC)", DBLayer.getDateAsString(timerResult
                            .getStartTime()), stepToNotification.getOrderStepState(), DBLayer.getDateAsString(timerResult.getEndTime()));
                }
            } else if (timerResult.getStartTimeType().equals(EStartTimeType.ORDER_STEP)) {
                if (timerResult.getEndTimeType().equals(EEndTimeType.CURRENT)) {
                    checkTextTime = String.format("step %s started at %s(UTC) and is not yet finished... checked vs. current datetime %s(UTC).",
                            stepFromNotification.getOrderStepState(), DBLayer.getDateAsString(timerResult.getStartTime()), DBLayer.getDateAsString(
                                    timerResult.getEndTime()));
                } else if (timerResult.getEndTimeType().equals(EEndTimeType.ORDER)) {
                    checkTextTime = String.format("step %s started at %s(UTC) and order finished at %s(UTC)", stepFromNotification
                            .getOrderStepState(), DBLayer.getDateAsString(timerResult.getStartTime()), DBLayer.getDateAsString(timerResult
                                    .getEndTime()));
                } else if (timerResult.getEndTimeType().equals(EEndTimeType.ORDER_STEP)) {
                    checkTextTime = String.format("step %s started at %s(UTC) and step %s finished at %s(UTC)", stepFromNotification
                            .getOrderStepState(), DBLayer.getDateAsString(timerResult.getStartTime()), stepToNotification.getOrderStepState(), DBLayer
                                    .getDateAsString(timerResult.getEndTime()));
                }

            }
        }

        if (timer.getTimerResult().getMinimum() != null && diffSeconds < timer.getTimerResult().getMinimum().doubleValue()) {
            String newVal = formatDoubleValue(timer.getTimerResult().getMinimum());
            checkText = String.format("execution time %ss is less than the defined minimum time %ss. %s", formatDoubleValue(diffSeconds
                    .doubleValue()), newVal, checkTextTime);
        }
        if (timer.getTimerResult().getMaximum() != null && diffSeconds > timer.getTimerResult().getMaximum().doubleValue()) {
            String newVal = formatDoubleValue(timer.getTimerResult().getMaximum());
            checkText = String.format("execution time %ss is greater than the defined maximum time %ss. %s", formatDoubleValue(diffSeconds
                    .doubleValue()), newVal, checkTextTime);
        }

        if (checkText == null) {
            if (!timerResult.getEndTimeType().equals(EEndTimeType.CURRENT)) {
                if (handleTransaction) {
                    dbLayer.getSession().beginTransaction();
                }
                dbLayer.removeCheck(check.getId());

                if (handleTransaction) {
                    dbLayer.getSession().commit();
                }
                LOGGER.debug(String.format("%s:[remove][check name=%s, id=%s, startTimeType=%s endTimeType=%s]executed and found no problems", method,
                        check.getName(), check.getId(), timerResult.getStartTimeType(), timerResult.getEndTimeType()));
                counter.addRemove();
            }
        } else {
            try {
                if (handleTransaction) {
                    dbLayer.getSession().beginTransaction();
                }
                if (timerResult.getEndTimeType().equals(EEndTimeType.CURRENT) && check.getCheckText() == null) {
                    checkText = String.format("not set as checked. do one rerun. %s", checkText);

                    LOGGER.debug(String.format("%s:[rerun][check name=%s, id=%s, text=%s, resultIds=%s]", method, check.getName(), check.getId(),
                            checkText, timer.getTimerResult().getResultIds()));

                    dbLayer.setNotificationCheckForRerun(check, stepFromStartTime, stepToEndTime, checkText, timer.getTimerResult().getResultIds()
                            .toString());

                    counter.addRerun();
                } else {
                    LOGGER.debug(String.format("%s:[update][check name=%s, id=%s, text=%s, resultIds=%s]", method, check.getName(), check.getId(),
                            checkText, timer.getTimerResult().getResultIds()));

                    dbLayer.setNotificationCheck(check, stepFromStartTime, stepToEndTime, checkText, timer.getTimerResult().getResultIds()
                            .toString());

                    counter.addTotal();
                }
                if (handleTransaction) {
                    dbLayer.getSession().commit();
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
    }

    public String formatDoubleValue(Double d) {
        String s = String.format("%.2f", d);
        if (d < 0.01) { // weitere Prüfung damit keine 0.00 in der Ausgabe steht
            if (d > 0.0001) {
                s = String.format("%.4f", d);
            } else if (d > 0.000001) {
                s = String.format("%.8f", d);
            }
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
