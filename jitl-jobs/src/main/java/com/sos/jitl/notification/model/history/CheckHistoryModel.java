package com.sos.jitl.notification.model.history;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.hibernate.exceptions.SOSHibernateException;
import com.sos.jitl.notification.db.DBItemSchedulerMonChecks;
import com.sos.jitl.notification.db.DBItemSchedulerMonNotifications;
import com.sos.jitl.notification.db.DBLayer;
import com.sos.jitl.notification.db.DBLayerSchedulerMon;
import com.sos.jitl.notification.helper.NotificationReportExecution;
import com.sos.jitl.notification.helper.NotificationXmlHelper;
import com.sos.jitl.notification.helper.counters.CounterCheckHistory;
import com.sos.jitl.notification.helper.elements.timer.ElementTimer;
import com.sos.jitl.notification.helper.elements.timer.ElementTimerJob;
import com.sos.jitl.notification.helper.elements.timer.ElementTimerJobChain;
import com.sos.jitl.notification.jobs.history.CheckHistoryJobOptions;
import com.sos.jitl.notification.model.INotificationModel;
import com.sos.jitl.notification.model.NotificationModel;
import com.sos.jitl.notification.plugins.history.CheckHistoryTimerPlugin;
import com.sos.jitl.notification.plugins.history.ICheckHistoryPlugin;

import sos.util.SOSString;
import sos.xml.SOSXMLXPath;

public class CheckHistoryModel extends NotificationModel implements INotificationModel {

    private static final Logger LOGGER = LoggerFactory.getLogger(CheckHistoryModel.class);
    private static final boolean isDebugEnabled = LOGGER.isDebugEnabled();
    private static final boolean isTraceEnabled = LOGGER.isTraceEnabled();
    private CheckHistoryJobOptions options;
    private LinkedHashMap<String, ElementTimer> timers = null;
    private LinkedHashMap<String, ArrayList<String>> jobChains = null;
    private LinkedHashMap<String, ArrayList<String>> jobs = null;
    private boolean checkInsertJobChainNotifications = true;
    private boolean checkInsertJobNotifications = true;
    private List<ICheckHistoryPlugin> plugins = null;
    private boolean executeChecks = false;
    private CounterCheckHistory counter;

    public CheckHistoryModel(SOSHibernateSession session, CheckHistoryJobOptions opt) throws Exception {
        super(session);
        options = opt;
    }

    public void init() throws Exception {
        initConfig();
        registerPlugins();
        pluginsOnInit(timers, options, getDbLayer());
    }

    private void initCounters() {
        counter = new CounterCheckHistory();
    }

    public void initConfig() throws Exception {
        String method = "initConfig";
        plugins = new ArrayList<ICheckHistoryPlugin>();
        timers = new LinkedHashMap<String, ElementTimer>();
        jobChains = new LinkedHashMap<String, ArrayList<String>>();
        jobs = new LinkedHashMap<String, ArrayList<String>>();

        File schemaFile = new File(options.schema_configuration_file.getValue());
        if (!schemaFile.exists()) {
            throw new Exception(String.format("[%s][schema file not found]%s", method, normalizePath(schemaFile)));
        }
        readConfigFiles();
    }

    private void readConfigFiles() throws Exception {
        String method = "readConfigFiles";
        jobChains = new LinkedHashMap<String, ArrayList<String>>();
        jobs = new LinkedHashMap<String, ArrayList<String>>();
        timers = new LinkedHashMap<String, ElementTimer>();
        checkInsertJobChainNotifications = true;
        checkInsertJobNotifications = true;

        int counter = 0;
        File dir = new File(options.configuration_dir.getValue());
        if (dir.exists()) {
            File[] files = getAllConfigurationFiles(dir);
            if (isDebugEnabled) {
                LOGGER.debug((String.format("[%s][%s]found %s files", method, dir.getCanonicalPath(), files.length)));
            }
            for (int i = 0; i < files.length; i++) {
                counter++;
                setConfigFromFile(counter, files[i]);
            }
        } else {
            if (isDebugEnabled) {
                LOGGER.debug(String.format("[%s][%s]configuration dir not found", method, dir.getCanonicalPath()));
            }
        }
        File defaultConfiguration = new File(options.default_configuration_file.getValue());
        if (defaultConfiguration.exists()) {
            counter++;
            setConfigFromFile(counter, defaultConfiguration);
        } else {
            if (isDebugEnabled) {
                LOGGER.debug(String.format("[%s][%s]default configuration file not found", method, defaultConfiguration.getCanonicalPath()));
            }
        }
        if (counter == 0) {
            throw new Exception("not found configuration files");
        }

        if (jobChains.isEmpty() && jobs.isEmpty() && timers.isEmpty()) {
            executeChecks = false;
            if (isDebugEnabled) {
                LOGGER.debug(String.format("[%s][skip]Job, JobChain, TimerRef elements are not defined", method));
            }
        } else {
            executeChecks = true;
        }
    }

    private void setConfigFromFile(int counter, File f) throws Exception {
        String method = "setConfigFromFile";
        String cp = f.getCanonicalPath();
        LOGGER.info(String.format("[%s][%s]%s", method, counter, cp));
        SOSXMLXPath xpath = null;
        try {
            xpath = new SOSXMLXPath(cp);
        } catch (Exception e) {
            throw new Exception(String.format("[%s][%s][SOSXMLXPath][%s]%s", method, counter, normalizePath(f), e.toString()), e);
        }
        setConfigAllJobChains(xpath);
        setConfigAllJobs(xpath);
        setConfigTimers(xpath);
    }

    private void setConfigTimers(SOSXMLXPath xpath) throws Exception {
        NodeList nlTimers = NotificationXmlHelper.selectTimerDefinitions(xpath);
        for (int j = 0; j < nlTimers.getLength(); j++) {
            Node n = nlTimers.item(j);
            String name = NotificationXmlHelper.getTimerName((Element) n);
            if (name != null && !timers.containsKey(name)) {
                timers.put(name, new ElementTimer(n));
            }
        }
    }

    private void setConfigAllJobs(SOSXMLXPath xpath) throws Exception {
        if (!checkInsertJobNotifications) {
            return;
        }
        NodeList notificationJobs = NotificationXmlHelper.selectNotificationJobDefinitions(xpath);
        setConfigJobs(xpath, notificationJobs);
        if (checkInsertJobNotifications) {
            NodeList timerJobs = NotificationXmlHelper.selectTimerJobDefinitions(xpath);
            setConfigJobs(xpath, timerJobs);
        }
    }

    private void setConfigJobs(SOSXMLXPath xpath, NodeList nlJobs) throws Exception {
        for (int j = 0; j < nlJobs.getLength(); j++) {
            Element job = (Element) nlJobs.item(j);
            String schedulerId = NotificationXmlHelper.getSchedulerId(job);
            String name = NotificationXmlHelper.getJobName(job);
            schedulerId = SOSString.isEmpty(schedulerId) ? DBLayerSchedulerMon.DEFAULT_EMPTY_NAME : schedulerId;
            name = SOSString.isEmpty(name) ? DBLayerSchedulerMon.DEFAULT_EMPTY_NAME : name;
            ArrayList<String> al = new ArrayList<String>();
            if (schedulerId.equals(DBLayerSchedulerMon.DEFAULT_EMPTY_NAME) && name.equals(DBLayerSchedulerMon.DEFAULT_EMPTY_NAME)) {
                jobs = new LinkedHashMap<String, ArrayList<String>>();
                al.add(name);
                jobs.put(schedulerId, al);
                checkInsertJobNotifications = false;
                return;
            }
            if (jobs.containsKey(schedulerId)) {
                al = jobs.get(schedulerId);
            }
            if (!al.contains(name)) {
                al.add(name);
            }
            jobs.put(schedulerId, al);
        }
    }

    private void setConfigAllJobChains(SOSXMLXPath xpath) throws Exception {
        NodeList notificationJobChains = NotificationXmlHelper.selectNotificationJobChainDefinitions(xpath);
        setConfigJobChains(xpath, notificationJobChains);
        if (checkInsertJobChainNotifications) {
            NodeList timerJobChains = NotificationXmlHelper.selectTimerJobChainDefinitions(xpath);
            setConfigJobChains(xpath, timerJobChains);
        }
    }

    private void setConfigJobChains(SOSXMLXPath xpath, NodeList nlJobChains) throws Exception {
        for (int j = 0; j < nlJobChains.getLength(); j++) {
            Element jobChain = (Element) nlJobChains.item(j);
            String schedulerId = NotificationXmlHelper.getSchedulerId(jobChain);
            String name = NotificationXmlHelper.getJobChainName(jobChain);
            schedulerId = SOSString.isEmpty(schedulerId) ? DBLayerSchedulerMon.DEFAULT_EMPTY_NAME : schedulerId;
            name = SOSString.isEmpty(name) ? DBLayerSchedulerMon.DEFAULT_EMPTY_NAME : name;
            ArrayList<String> al = new ArrayList<String>();
            if (schedulerId.equals(DBLayerSchedulerMon.DEFAULT_EMPTY_NAME) && name.equals(DBLayerSchedulerMon.DEFAULT_EMPTY_NAME)) {
                jobChains = new LinkedHashMap<String, ArrayList<String>>();
                al.add(name);
                jobChains.put(schedulerId, al);
                checkInsertJobChainNotifications = false;
                return;
            }
            if (jobChains.containsKey(schedulerId)) {
                al = jobChains.get(schedulerId);
            }
            if (!al.contains(name)) {
                al.add(name);
            }
            jobChains.put(schedulerId, al);
        }
    }

    private boolean checkDoInsert(CounterCheckHistory counter, NotificationReportExecution execution, boolean checkJobChains, boolean checkJobs)
            throws Exception {
        // Indent for the output
        String method = "[" + counter.getTotal() + "][checkDoInsert]";

        if ((jobs == null || jobs.isEmpty()) && (jobChains == null || jobChains.isEmpty())) {
            if (isDebugEnabled) {
                LOGGER.debug(String.format("%s[skip]missing jobs and job chain definitions", method));
            }
            return false;
        }
        if (checkJobChains) {
            if (!checkInsertJobChainNotifications) {
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("%s[skip]checkInsertJobChainNotifications=false", method));
                }
                return true;
            }
            Set<Map.Entry<String, ArrayList<String>>> set = jobChains.entrySet();
            for (Map.Entry<String, ArrayList<String>> jc : set) {
                String schedulerId = jc.getKey();
                ArrayList<String> jobChainsFromSet = jc.getValue();
                boolean doCheckJobChains = true;
                if (!schedulerId.equals(DBLayerSchedulerMon.DEFAULT_EMPTY_NAME)) {
                    try {
                        if (!execution.getSchedulerId().matches(schedulerId)) {
                            if (isDebugEnabled) {
                                LOGGER.debug(String.format("%s[jobChains][checkJobChains=%s][schedulerId not match][%s][%s]", method, checkJobChains,
                                        execution.getSchedulerId(), schedulerId));
                            }
                            doCheckJobChains = false;
                        }
                    } catch (Exception ex) {
                        throw new Exception(String.format("%s[jobChains][check with configured scheduler_id=%s]%s", method, schedulerId, ex));
                    }
                }
                if (doCheckJobChains) {
                    for (int i = 0; i < jobChainsFromSet.size(); i++) {
                        String jobChainName = jobChainsFromSet.get(i);

                        // jobChain = Matcher.quoteReplacement(jobChain);
                        if (jobChainName.equals(DBLayerSchedulerMon.DEFAULT_EMPTY_NAME)) {
                            if (isDebugEnabled) {
                                LOGGER.debug(String.format("%s[jobChains][match][%s][%s][%s][%s]", method, execution.getSchedulerId(), schedulerId,
                                        execution.getJobChainName(), jobChainName));
                            }
                            return true;
                        }
                        try {
                            jobChainName = normalizeRegex(jobChainName);
                            if (execution.getJobChainName().matches(jobChainName)) {
                                if (isDebugEnabled) {
                                    LOGGER.debug(String.format("%s[jobChains][match][%s][%s][%s][%s]", method, execution.getSchedulerId(),
                                            schedulerId, execution.getJobChainName(), jobChainName));
                                }
                                return true;
                            } else {
                                if (isDebugEnabled) {
                                    LOGGER.debug(String.format("%s[jobChains][not match][%s][%s]", method, execution.getJobChainName(),
                                            jobChainName));
                                }
                            }
                        } catch (Exception ex) {
                            throw new Exception(String.format("%s[jobChains][check with configured scheduler_id=%s, name=%s]%s", method, schedulerId,
                                    jobChainName, ex));
                        }
                    }
                }
            }
        }

        if (checkJobs) {
            if (!checkInsertJobNotifications) {
                LOGGER.debug(String.format("%s[skip]checkInsertJobNotifications=false", method, counter.getTotal()));
                return true;
            }
            Set<Map.Entry<String, ArrayList<String>>> set = jobs.entrySet();
            for (Map.Entry<String, ArrayList<String>> jc : set) {
                String schedulerId = jc.getKey();
                ArrayList<String> jobsFromSet = jc.getValue();
                boolean doCheckJobs = true;
                if (!schedulerId.equals(DBLayerSchedulerMon.DEFAULT_EMPTY_NAME)) {
                    try {
                        if (!execution.getSchedulerId().matches(schedulerId)) {
                            if (isDebugEnabled) {
                                LOGGER.debug(String.format("%s[jobs][checkJobs=%s][schedulerId not match][%s][%s]", method, checkJobs, execution
                                        .getSchedulerId(), schedulerId));
                            }
                            doCheckJobs = false;
                        }
                    } catch (Exception ex) {
                        throw new Exception(String.format("%s[jobs][check with configured scheduler_id=%s]%s", method, schedulerId, ex));
                    }
                }
                if (doCheckJobs) {
                    for (int i = 0; i < jobsFromSet.size(); i++) {
                        String job = jobsFromSet.get(i);
                        if (job.equals(DBLayerSchedulerMon.DEFAULT_EMPTY_NAME)) {
                            if (isDebugEnabled) {
                                LOGGER.debug(String.format("%s[jobs][match][%s][%s][%s][%s]", method, execution.getSchedulerId(), schedulerId,
                                        execution.getJobName(), job));
                            }
                            return true;
                        }
                        try {
                            job = normalizeRegex(job);
                            if (execution.getJobName().matches(job)) {
                                if (isDebugEnabled) {
                                    LOGGER.debug(String.format("%s[jobs][match][%s][%s][%s][%s]", method, execution.getSchedulerId(), schedulerId,
                                            execution.getJobName(), job));
                                }
                                return true;
                            } else {
                                if (isDebugEnabled) {
                                    LOGGER.debug(String.format("%s[jobs][not match][%s][%s]", method, execution.getJobName(), job));
                                }
                            }
                        } catch (Exception ex) {
                            throw new Exception(String.format("%s[jobs][check with configured scheduler_id=%s, name=%s]%s", method, schedulerId, job,
                                    ex));
                        }
                    }
                }
            }
        }
        return false;
    }

    @Override
    public void process() throws Exception {

    }

    public void process(NotificationReportExecution item, boolean checkJobChains, boolean checkJobs) throws Exception {
        String method = "process";

        if (!executeChecks) {
            return;
        }

        initCounters();
        boolean success = true;
        if (!checkDoInsert(counter, item, checkJobChains, checkJobs)) {
            counter.addSkip();
            return;
        }

        List<DBItemSchedulerMonChecks> checks = null;
        try {
            if (isDebugEnabled) {
                LOGGER.debug(String.format("[%s][%s][%s]%s", method, counter.getTotal(), item.getStandalone() ? "standalone" : "order",
                        NotificationModel.toString(item)));
            }

            List<DBItemSchedulerMonNotifications> dbItems = getDbLayer().getNotificationsWithDummyStep(item.getSchedulerId(), item.getStandalone(),
                    item.getTaskId(), item.getStep(), item.getOrderHistoryId());
            DBItemSchedulerMonNotifications dbItem = null;
            boolean hasStepError = item.getError();
            if (dbItems == null || dbItems.size() == 0) {
                dbItem = getDbLayer().createNotification(item.getSchedulerId(), item.getStandalone(), item.getTaskId(), item.getStep(), item
                        .getOrderHistoryId(), item.getJobChainName(), item.getJobChainTitle(), item.getOrderId(), item.getOrderTitle(), item
                                .getOrderStartTime(), item.getOrderEndTime(), item.getOrderStepState(), item.getOrderStepStartTime(), item
                                        .getOrderStepEndTime(), item.getJobName(), item.getJobTitle(), item.getTaskStartTime(), item.getTaskEndTime(),
                        false, new Long(item.getReturnCode() == null ? 0 : item.getReturnCode()), item.getAgentUrl(), item.getClusterMemberId(),
                        hasStepError, item.getErrorCode(), item.getErrorText());

                getDbLayer().getSession().save(dbItem);
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("[%s][%s][insert]%s", method, counter.getTotal(), NotificationModel.toString(dbItem)));
                }
                counter.addInsert();

            } else {
                // already inserted
                if (dbItems.size() == 2) {// by the StoreResult Job. orig. Step + NOTIFICATION_DUMMY_MAX_STEP
                    DBItemSchedulerMonNotifications toDeleteDbItem = null;
                    for (int i = 0; i < dbItems.size(); i++) {
                        DBItemSchedulerMonNotifications tmpDbItem = dbItems.get(i);
                        if (isDebugEnabled) {
                            LOGGER.debug(String.format("[%s][%s][update][before][%s]%s", method, counter.getTotal(), i, NotificationModel.toString(
                                    tmpDbItem)));
                        }

                        if (tmpDbItem.getStep().equals(DBLayer.NOTIFICATION_DUMMY_MAX_STEP)) {
                            toDeleteDbItem = tmpDbItem;
                        } else {
                            if (tmpDbItem.getStep().equals(item.getStep())) {
                                dbItem = tmpDbItem;
                            }
                        }
                    }

                    if (dbItem == null) { // not possible
                        dbItem = getDbLayer().getNotification(item.getSchedulerId(), item.getStandalone(), item.getTaskId(), item.getStep(), item
                                .getOrderHistoryId());
                    }

                    if (toDeleteDbItem != null) {
                        if (dbItem != null) {
                            try {
                                getDbLayer().updateNotificationResults(dbItem.getId(), toDeleteDbItem.getId());
                            } catch (Exception e) {
                                LOGGER.warn(String.format("[%s][%s][update results]%s", method, counter.getTotal(), e.toString()), e);
                            }
                        }
                        getDbLayer().removeNotification(toDeleteDbItem);
                    }

                } else {
                    dbItem = dbItems.get(0); // orig. Step or NOTIFICATION_DUMMY_MAX_STEP
                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("[%s][%s][update][before]%s", method, counter.getTotal(), NotificationModel.toString(dbItem)));

                    }
                }

                if (dbItem == null) {
                    LOGGER.warn(String.format("[%s][%s][update][not found notification]%s", method, counter.getTotal(), NotificationModel.toString(
                            item)));
                    return;
                }

                dbItem.setJobChainName(item.getJobChainName());
                dbItem.setJobChainTitle(item.getJobChainTitle());
                dbItem.setOrderId(item.getOrderId());
                dbItem.setOrderTitle(item.getOrderTitle());
                dbItem.setOrderStartTime(item.getOrderStartTime());
                dbItem.setOrderEndTime(item.getOrderEndTime());
                dbItem.setStep(item.getStep());
                dbItem.setOrderStepState(item.getOrderStepState());
                dbItem.setOrderStepStartTime(item.getOrderStepStartTime());
                dbItem.setOrderStepEndTime(item.getOrderStepEndTime());
                dbItem.setJobName(item.getJobName());
                dbItem.setJobTitle(item.getJobTitle());
                dbItem.setTaskStartTime(item.getTaskStartTime());
                dbItem.setTaskEndTime(item.getTaskEndTime());
                dbItem.setReturnCode(new Long(item.getReturnCode() == null ? 0 : item.getReturnCode()));
                dbItem.setAgentUrl(item.getAgentUrl());
                dbItem.setClusterMemberId(item.getClusterMemberId());
                // hatte error und wird auf nicht error gesetzt
                dbItem.setRecovered(dbItem.getError() && !hasStepError);
                dbItem.setError(hasStepError);
                dbItem.setErrorCode(item.getErrorCode());
                dbItem.setErrorText(item.getErrorText());
                dbItem.setModified(DBLayer.getCurrentDateTime());
                getDbLayer().getSession().update(dbItem);

                if (isDebugEnabled) {
                    LOGGER.debug(String.format("[%s][%s][update][after]%s", method, counter.getTotal(), NotificationModel.toString(dbItem)));
                }
                counter.addUpdate();
            }

            if (dbItem != null && dbItem.getOrderEndTime() != null && dbItem.getOrderHistoryId() > 0) {
                int hits = getDbLayer().setNotificationsOrderEndTime(dbItem.getSchedulerId(), dbItem.getOrderHistoryId(), dbItem.getOrderEndTime());
                if (isDebugEnabled) {
                    LOGGER.debug(String.format(
                            "[%s][%s][update][after][setOrderEndTime][schedulerId=%s][orderHistoryId=%s][orderEndTime=%s]%s updated", method, counter
                                    .getTotal(), dbItem.getSchedulerId(), dbItem.getOrderHistoryId(), dbItem.getOrderEndTime(), hits));
                }
            }

            checks = createTimers(dbItem);
        } catch (Exception ex) {
            success = false;
            throw new Exception(String.format("[%s]%s", method, ex.toString()), ex);
        }

        if (success) {
            pluginsOnProcess(timers, checks, options, getDbLayer(), null, null);
        }

    }

    private void createJobTimers(List<DBItemSchedulerMonChecks> checks, DBItemSchedulerMonNotifications dbItem, String timerName,
            ArrayList<ElementTimerJob> timerJobs) throws Exception {
        String method = "  [" + counter.getTotal() + "][createJobTimers]";
        for (int i = 0; i < timerJobs.size(); i++) {
            ElementTimerJob job = timerJobs.get(i);
            String schedulerId = job.getSchedulerId();
            String jobName = job.getName();
            boolean insert = true;
            if (!schedulerId.equals(DBLayerSchedulerMon.DEFAULT_EMPTY_NAME) && !dbItem.getSchedulerId().matches(schedulerId)) {
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("%s[%s][schedulerId not match][%s][%s]", method, timerName, dbItem.getSchedulerId(), schedulerId));
                }

                insert = false;
            }
            jobName = normalizeRegex(jobName);
            if (insert && !jobName.equals(DBLayerSchedulerMon.DEFAULT_EMPTY_NAME) && !dbItem.getJobName().matches(jobName)) {
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("%s[%s][jobName not match][%s][%s]", method, timerName, dbItem.getJobName(), jobName));
                }
                insert = false;
            }
            if (insert) {
                counter.addInsertTimer();
                DBItemSchedulerMonChecks item = null;

                if (dbItem.getStandalone()) {
                    item = createCheck(timerName, dbItem, DBLayer.DEFAULT_EMPTY_NAME, DBLayer.DEFAULT_EMPTY_NAME, dbItem.getTaskStartTime(), dbItem
                            .getTaskEndTime(), DBLayer.NOTIFICATION_OBJECT_TYPE_JOB);
                } else {
                    item = createCheck(timerName, dbItem, DBLayer.DEFAULT_EMPTY_NAME, DBLayer.DEFAULT_EMPTY_NAME, dbItem.getOrderStepStartTime(),
                            dbItem.getOrderStepEndTime(), DBLayer.NOTIFICATION_OBJECT_TYPE_JOB);
                }
                checks.add(item);
                LOGGER.debug(String.format("%s[%s][createCheck]%s", method, timerName, NotificationModel.toString(item)));
            }
        }
    }

    public DBItemSchedulerMonChecks createCheck(String name, DBItemSchedulerMonNotifications notification, String stepFrom, String stepTo,
            Date stepFromStartTime, Date stepToEndTime, Long objectType) throws SOSHibernateException {

        Long notificationId = notification.getId();
        // NULL wegen batch Insert bei den Datenbanken, die kein Autoincrement
        // haben (Oracle ...)
        DBItemSchedulerMonChecks item = null;
        if (notificationId == null || notificationId.equals(new Long(0))) {
            item = new DBItemSchedulerMonChecks();
            item.setName(name);

            notificationId = new Long(0);
            item.setResultIds(notification.getSchedulerId() + ";" + (notification.getStandalone() ? "true" : "false") + ";" + notification.getTaskId()
                    + ";" + notification.getStep() + ";" + notification.getOrderHistoryId());
            item.setNotificationId(notificationId);
            item.setStepFrom(stepFrom);
            item.setStepTo(stepTo);
            item.setStepFromStartTime(stepFromStartTime);
            item.setStepToEndTime(stepToEndTime);
            item.setChecked(false);
            item.setObjectType(objectType);
            item.setCreated(DBLayer.getCurrentDateTime());
            item.setModified(DBLayer.getCurrentDateTime());
        } else {
            item = new DBItemSchedulerMonChecks();
            item.setName(name);
            item.setNotificationId(notificationId);
            item.setStepFrom(stepFrom);
            item.setStepTo(stepTo);
            item.setStepFromStartTime(stepFromStartTime);
            item.setStepToEndTime(stepToEndTime);
            item.setChecked(false);
            item.setObjectType(objectType);
            item.setCreated(DBLayer.getCurrentDateTime());
            item.setModified(DBLayer.getCurrentDateTime());
        }
        return item;
    }

    private void createJobChainTimers(List<DBItemSchedulerMonChecks> checks, DBItemSchedulerMonNotifications dbItem, String timerName,
            ArrayList<ElementTimerJobChain> timerJobChains) throws Exception {
        // output indent
        String method = "  [" + counter.getTotal() + "][createJobChainTimers]";
        for (int i = 0; i < timerJobChains.size(); i++) {
            ElementTimerJobChain jobChain = timerJobChains.get(i);
            String schedulerId = jobChain.getSchedulerId();
            String jobChainName = jobChain.getName();
            boolean insert = true;
            if (!schedulerId.equals(DBLayerSchedulerMon.DEFAULT_EMPTY_NAME) && !dbItem.getSchedulerId().matches(schedulerId)) {
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("%s[%s][schedulerId not match][%s][%s]", method, timerName, dbItem.getSchedulerId(), schedulerId));
                }
                insert = false;
            }
            jobChainName = normalizeRegex(jobChainName);
            if (insert && !jobChainName.equals(DBLayerSchedulerMon.DEFAULT_EMPTY_NAME) && !dbItem.getJobChainName().matches(jobChainName)) {
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("%s[%s][jobChainName not match][%s][%s]", method, timerName, dbItem.getJobChainName(), jobChainName));
                }

                insert = false;
            }
            if (insert) {
                counter.addInsertTimer();
                DBItemSchedulerMonChecks item = createCheck(timerName, dbItem, jobChain.getStepFrom(), jobChain.getStepTo(), dbItem
                        .getOrderStartTime(), dbItem.getOrderEndTime(), DBLayer.NOTIFICATION_OBJECT_TYPE_JOB_CHAIN);

                checks.add(item);
                LOGGER.debug(String.format("%s[%s][createCheck]%s", method, timerName, NotificationModel.toString(item)));
            }
        }
    }

    private List<DBItemSchedulerMonChecks> createTimers(DBItemSchedulerMonNotifications dbItem) throws Exception {
        // output indent
        String method = "  [" + counter.getTotal() + "][createTimers]";
        if (timers == null || timers.isEmpty()) {
            if (isDebugEnabled) {
                LOGGER.debug(String.format("%s[skip]timers is null or empty", method));
            }
            return null;
        }
        ArrayList<DBItemSchedulerMonChecks> checks = new ArrayList<>();
        Set<Map.Entry<String, ElementTimer>> set = this.timers.entrySet();
        for (Map.Entry<String, ElementTimer> me : set) {
            String timerName = me.getKey();
            ElementTimer timer = me.getValue();

            ArrayList<ElementTimerJob> timerJobs = timer.getJobs();
            ArrayList<ElementTimerJobChain> timerJobChains = timer.getJobChains();
            if (timerJobs.isEmpty() && timerJobChains.isEmpty()) {
                LOGGER.warn(String.format("%s[timer=%s]not found configured JobChains or Jobs", method, timerName));
                continue;
            }

            if (!timerJobs.isEmpty()) {
                createJobTimers(checks, dbItem, timerName, timerJobs);
            }
            if (!dbItem.getStandalone()) {
                if (!timerJobChains.isEmpty()) {
                    createJobChainTimers(checks, dbItem, timerName, timerJobChains);
                }
            }
        }
        return checks;
    }

    private void pluginsOnInit(LinkedHashMap<String, ElementTimer> timers, CheckHistoryJobOptions options, DBLayerSchedulerMon dbLayer) {
        for (ICheckHistoryPlugin plugin : plugins) {
            try {
                plugin.onInit(timers, options, dbLayer);
            } catch (Exception ex) {
                LOGGER.warn(String.format("[pluginsOnInit]%s", ex.getMessage()), ex);
            }
        }
    }

    @SuppressWarnings("unused")
    private void pluginsOnExit(LinkedHashMap<String, ElementTimer> timers, CheckHistoryJobOptions options, DBLayerSchedulerMon dbLayer) {
        for (ICheckHistoryPlugin plugin : plugins) {
            try {
                plugin.onExit(timers, options, dbLayer);
            } catch (Exception ex) {
                LOGGER.warn(String.format("[pluginsOnExit]%s", ex.getMessage()), ex);
            }
        }
    }

    private void pluginsOnProcess(LinkedHashMap<String, ElementTimer> timers, List<DBItemSchedulerMonChecks> checks, CheckHistoryJobOptions options,
            DBLayerSchedulerMon dbLayer, Date dateFrom, Date dateTo) {
        for (ICheckHistoryPlugin plugin : plugins) {
            try {
                plugin.onProcess(timers, checks, options, dbLayer, dateFrom, dateTo);
            } catch (Exception ex) {
                LOGGER.warn(String.format("[pluginsOnProcess]%s", ex.getMessage()), ex);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void registerPlugins() throws Exception {
        String method = "registerPlugins";
        plugins = new ArrayList<ICheckHistoryPlugin>();
        // if (SOSString.isEmpty(this.options.plugins.getValue())) {
        this.options.plugins.setValue(CheckHistoryTimerPlugin.class.getName());
        // }
        String[] arr = this.options.plugins.getValue().trim().split(";");
        for (int i = 0; i < arr.length; i++) {
            try {
                Class<ICheckHistoryPlugin> c = (Class<ICheckHistoryPlugin>) Class.forName(arr[i].trim());
                addPlugin(c.newInstance());
                if (isTraceEnabled) {
                    LOGGER.trace(String.format("[%s][registered]%s", method, arr[i]));
                }
            } catch (Exception ex) {
                LOGGER.error(String.format("[%s][cannot be registered][%s]%s", method, arr[i], ex.getMessage()), ex);
            }
        }

        if (isTraceEnabled) {
            LOGGER.trace(String.format("[%s]registered=%s", method, plugins.size()));
        }
    }

    public static String normalizePath(String val) {
        if (val != null && val.startsWith("/")) {
            val = val.substring(1);
        }
        return val;
    }

    public static String normalizeRegex(String val) {
        if (val != null) {
            if (val.startsWith("/")) {
                val = val.substring(1);
            } else if (val.startsWith("^/")) {
                val = val.substring(0, 1) + val.substring(2);
            } else if (val.startsWith("^(/")) {
                val = val.substring(0, 2) + val.substring(3);
            }
        }
        return val;
    }

    public void addPlugin(ICheckHistoryPlugin handler) {
        plugins.add(handler);
    }

    public void resetPlugins() {
        plugins = new ArrayList<ICheckHistoryPlugin>();
    }

    public boolean executeChecks() {
        return executeChecks;
    }
}
