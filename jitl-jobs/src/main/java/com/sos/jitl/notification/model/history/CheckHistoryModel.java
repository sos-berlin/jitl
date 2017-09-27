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
import com.sos.jitl.notification.db.DBItemSchedulerMonNotifications;
import com.sos.jitl.notification.db.DBLayer;
import com.sos.jitl.notification.db.DBLayerSchedulerMon;
import com.sos.jitl.notification.helper.CounterCheckHistory;
import com.sos.jitl.notification.helper.ElementTimer;
import com.sos.jitl.notification.helper.ElementTimerJob;
import com.sos.jitl.notification.helper.ElementTimerJobChain;
import com.sos.jitl.notification.helper.NotificationReportExecution;
import com.sos.jitl.notification.helper.NotificationXmlHelper;
import com.sos.jitl.notification.jobs.history.CheckHistoryJobOptions;
import com.sos.jitl.notification.model.INotificationModel;
import com.sos.jitl.notification.model.NotificationModel;
import com.sos.jitl.notification.plugins.history.CheckHistoryTimerPlugin;
import com.sos.jitl.notification.plugins.history.ICheckHistoryPlugin;

import sos.util.SOSString;
import sos.xml.SOSXMLXPath;

public class CheckHistoryModel extends NotificationModel implements INotificationModel {

    private static final Logger LOGGER = LoggerFactory.getLogger(CheckHistoryModel.class);
    private CheckHistoryJobOptions options;
    private LinkedHashMap<String, ElementTimer> timers = null;
    private LinkedHashMap<String, ArrayList<String>> jobChains = null;
    private LinkedHashMap<String, ArrayList<String>> jobs = null;
    private boolean checkInsertJobChainNotifications = true;
    private boolean checkInsertJobNotifications = true;
    private List<ICheckHistoryPlugin> plugins = null;
    private CounterCheckHistory counter;

    public CheckHistoryModel(SOSHibernateSession conn, CheckHistoryJobOptions opt) throws Exception {
        super(conn);
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
        plugins = new ArrayList<ICheckHistoryPlugin>();
        timers = new LinkedHashMap<String, ElementTimer>();
        jobChains = new LinkedHashMap<String, ArrayList<String>>();
        jobs = new LinkedHashMap<String, ArrayList<String>>();
        File dir = null;
        File schemaFile = new File(options.schema_configuration_file.getValue());
        if (!schemaFile.exists()) {
            throw new Exception(String.format("schema file not found: %s", schemaFile.getAbsolutePath()));
        }
        if (SOSString.isEmpty(this.options.configuration_dir.getValue())) {
            dir = new File(this.options.configuration_dir.getValue());
        } else {
            dir = schemaFile.getParentFile().getAbsoluteFile();
        }
        if (!dir.exists()) {
            throw new Exception(String.format("configuration dir not found: %s", dir.getAbsolutePath()));
        }
        LOGGER.debug(String.format("schemaFile=%s, configDir=%s", schemaFile, dir.getAbsolutePath()));
        readConfigFiles(dir);
    }

    private void readConfigFiles(File dir) throws Exception {
        String method = "readConfigFiles";
        jobChains = new LinkedHashMap<String, ArrayList<String>>();
        jobs = new LinkedHashMap<String, ArrayList<String>>();
        timers = new LinkedHashMap<String, ElementTimer>();
        checkInsertJobChainNotifications = true;
        checkInsertJobNotifications = true;
        File[] files = getAllConfigurationFiles(dir);
        if (files.length == 0) {
            throw new Exception(String.format("%s: configuration files not found. directory : %s", method, dir.getAbsolutePath()));
        }
        for (int i = 0; i < files.length; i++) {
            File f = files[i];
            LOGGER.debug(String.format("%s: read configuration file %s", method, f.getCanonicalPath()));
            SOSXMLXPath xpath = null;
            try {
                xpath = new SOSXMLXPath(f.getCanonicalPath());
            } catch (Exception e) {
                throw new Exception(String.format("SOSXMLXPath error[%s]:%s", f.getCanonicalPath(), e.toString()), e);
            }
            setConfigAllJobChains(xpath);
            setConfigAllJobs(xpath);
            setConfigTimers(xpath);
        }
        if (jobChains.isEmpty() && jobs.isEmpty() && timers.isEmpty()) {
            throw new Exception(String.format("%s: jobChains or jobs or timers definitions not founded", method));
        }
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

    private boolean checkInsertNotification(CounterCheckHistory counter, NotificationReportExecution execution, boolean checkJobChains,
            boolean checkJobs) throws Exception {
        // Indent for the output
        String method = "checkInsertNotification";
        if ((jobs == null || jobs.isEmpty()) && (jobChains == null || jobChains.isEmpty())) {
            LOGGER.debug(String.format("%s: %s)[skip]not found configured Jobs or JobChains", method, counter.getTotal()));
            return false;
        }
        if (checkJobChains) {
            if (!checkInsertJobChainNotifications) {
                LOGGER.debug(String.format("%s: %s)[skip]checkInsertJobChainNotifications=false", method, counter.getTotal()));
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
                            LOGGER.debug(String.format(
                                    "%s: %s)[check jobChains][not match schedulerId]-[configured scheduler_id=%s][db schedulerId=%s]", method, counter
                                            .getTotal(), schedulerId, execution.getSchedulerId()));
                            doCheckJobChains = false;
                        }
                    } catch (Exception ex) {
                        throw new Exception(String.format("%s: %s)[check jobChains][exception]-[configured scheduler_id=%s][db schedulerId=%s]: %s",
                                method, counter.getTotal(), schedulerId, execution.getSchedulerId(), ex));
                    }
                }
                if (doCheckJobChains) {
                    for (int i = 0; i < jobChainsFromSet.size(); i++) {
                        String jobChainName = jobChainsFromSet.get(i);

                        // jobChain = Matcher.quoteReplacement(jobChain);
                        if (jobChainName.equals(DBLayerSchedulerMon.DEFAULT_EMPTY_NAME)) {
                            LOGGER.debug(String.format("%s: %s)[check jobChains][matches]-[configured JobChain name=%s][db jobChainName=%s]", method,
                                    counter.getTotal(), jobChainName, execution.getJobChainName()));
                            return true;
                        }
                        try {
                            jobChainName = normalizeRegex(jobChainName);
                            if (execution.getJobChainName().matches(jobChainName)) {
                                LOGGER.debug(String.format("%s: %s)[check jobChains][matches]-[configured JobChain name=%s][db jobChainName=%s]",
                                        method, counter.getTotal(), jobChainName, execution.getJobChainName()));
                                return true;
                            } else {
                                LOGGER.debug(String.format(
                                        "%s: %s)[check jobChains][not match JobChain name]-[configured JobChain name=%s][db jobChainName=%s]", method,
                                        counter.getTotal(), jobChainName, execution.getJobChainName()));
                            }
                        } catch (Exception ex) {
                            throw new Exception(String.format(
                                    "%s: %s)[check jobChains][exception]-[configured JobChain name=%s][db jobChainName=%s]: %s", method, counter
                                            .getTotal(), jobChainName, execution.getJobChainName(), ex));
                        }
                    }
                }
            }
        }

        if (checkJobs) {
            if (!checkInsertJobNotifications) {
                LOGGER.debug(String.format("%s: %s)[skip]checkInsertJobNotifications=false", method, counter.getTotal()));
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
                            LOGGER.debug(String.format("%s: %s)[check jobs][not match schedulerId]-[configured scheduler_id=%s][db schedulerId=%s]",
                                    method, counter.getTotal(), schedulerId, execution.getSchedulerId()));
                            doCheckJobs = false;
                        }
                    } catch (Exception ex) {
                        throw new Exception(String.format("%s: %s)[check jobs][exception]-[configured scheduler_id=%s][db schedulerId=%s]: %s",
                                method, counter.getTotal(), schedulerId, execution.getSchedulerId(), ex));
                    }
                }
                if (doCheckJobs) {
                    for (int i = 0; i < jobsFromSet.size(); i++) {
                        String job = jobsFromSet.get(i);
                        if (job.equals(DBLayerSchedulerMon.DEFAULT_EMPTY_NAME)) {
                            LOGGER.debug(String.format("%s: %s)[check jobs][matches]-[configured Job name=%s][db jobName=%s]", method, counter
                                    .getTotal(), job, execution.getJobName()));
                            return true;
                        }
                        try {
                            job = normalizeRegex(job);
                            if (execution.getJobName().matches(job)) {
                                LOGGER.debug(String.format("%s: %s)[check jobs][matches]-[configured Job name=%s][db jobName=%s]", method, counter
                                        .getTotal(), job, execution.getJobName()));
                                return true;
                            } else {
                                LOGGER.debug(String.format("%s: %s)[check jobs][not match Job name]-[configured Job name=%s][db jobName=%s]", method,
                                        counter.getTotal(), job, execution.getJobName()));
                            }
                        } catch (Exception ex) {
                            throw new Exception(String.format("%s: %s)[check jobs][exception]-[configured Job name=%s][db jobName=%s]: %s", method,
                                    counter.getTotal(), job, execution.getJobName(), ex));
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

        initCounters();
        boolean success = true;
        if (!checkInsertNotification(counter, item, checkJobChains, checkJobs)) {
            counter.addSkip();
            LOGGER.debug(String.format(
                    "%s: %s)[skip][checkInsertNotification no matches]order schedulerId=%s, jobChain=%s, orderId=%s, jobName=%s, step=%s, stepState=%s, checkJobChains=%s, checkJobs=%s",
                    method, counter.getTotal(), item.getSchedulerId(), item.getJobChainName(), item.getOrderId(), item.getJobName(), item.getStep(),
                    item.getOrderStepState(), checkJobChains, checkJobs));
            return;
        }

        try {
            DBItemSchedulerMonNotifications dbItem = this.getDbLayer().getNotification(item.getSchedulerId(), item.getStandalone(), item.getTaskId(),
                    item.getStep(), item.getOrderHistoryId(), true);
            boolean hasStepError = item.getError();
            if (dbItem == null) {
                counter.addInsert();
                if (item.getStandalone()) {
                    LOGGER.debug(String.format("%s: %s)[insert][standalone]schedulerId=%s, jobName=%s", method, counter.getTotal(), item
                            .getSchedulerId(), item.getJobName()));
                } else {
                    LOGGER.debug(String.format("%s: %s)[insert][order]schedulerId=%s, jobChain=%s, orderId=%s, jobName=%s, step=%s, stepState=%s",
                            method, counter.getTotal(), item.getSchedulerId(), item.getJobChainName(), item.getOrderId(), item.getJobName(), item
                                    .getStep(), item.getOrderStepState()));
                }
                dbItem = getDbLayer().createNotification(item.getSchedulerId(), item.getStandalone(), item.getTaskId(), item.getStep(), item
                        .getOrderHistoryId(), item.getJobChainName(), item.getJobChainTitle(), item.getOrderId(), item.getOrderTitle(), item
                                .getOrderStartTime(), item.getOrderEndTime(), item.getOrderStepState(), item.getOrderStepStartTime(), item
                                        .getOrderStepEndTime(), item.getJobName(), item.getJobTitle(), item.getTaskStartTime(), item.getTaskEndTime(),
                        false, new Long(item.getReturnCode() == null ? 0 : item.getReturnCode()), item.getAgentUrl(), item.getClusterMemberId(),
                        hasStepError, item.getErrorCode(), item.getErrorText());

                getDbLayer().getSession().save(dbItem);

            } else {
                counter.addUpdate();
                // kann inserted sein durch StoreResult Job
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
                if (dbItem.getStandalone()) {
                    LOGGER.debug(String.format("%s: %s)[update][standalone]notificationId=%s, schedulerId=%s, jobName=%s", method, counter.getTotal(),
                            dbItem.getId(), dbItem.getSchedulerId(), dbItem.getJobName()));
                } else {
                    LOGGER.debug(String.format(
                            "%s: %s)[update][order]notificationId=%s, schedulerId=%s, jobChain=%s, orderId=%s, jobName=%s, step=%s, "
                                    + "stepState=%s", method, counter.getTotal(), dbItem.getId(), dbItem.getSchedulerId(), dbItem.getJobChainName(),
                            dbItem.getOrderId(), dbItem.getJobName(), dbItem.getStep(), dbItem.getOrderStepState()));
                }
                getDbLayer().getSession().update(dbItem);
            }

            if (!item.getStandalone() && !item.getError() && item.getOrderStepEndTime() != null && item.getStep() > new Long(1)) {
                getDbLayer().setRecovered(item.getOrderHistoryId(), item.getStep(), item.getOrderStepState());
            }

            insertTimer(counter, dbItem);
        } catch (Exception ex) {
            success = false;
            throw new Exception(String.format("%s: %s", method, ex.toString()), ex);
        }

        if (success) {
            pluginsOnProcess(timers, options, getDbLayer(), null, null);
        }

    }

    private CounterCheckHistory insertTimerJobs(CounterCheckHistory counter, DBItemSchedulerMonNotifications dbItem, String timerName,
            ArrayList<ElementTimerJob> timerJobs) throws Exception {
        // output indent
        String method = "  insertTimerJobs";
        for (int i = 0; i < timerJobs.size(); i++) {
            ElementTimerJob job = timerJobs.get(i);
            String schedulerId = job.getSchedulerId();
            String jobName = job.getName();
            boolean insert = true;
            if (!schedulerId.equals(DBLayerSchedulerMon.DEFAULT_EMPTY_NAME) && !dbItem.getSchedulerId().matches(schedulerId)) {
                LOGGER.debug(String.format(
                        "%s: %s)[timer name=%s][not match schedulerId]-[configured scheduler_id=%s][db schedulerId=%s][notification id=%s, jobName=%s]",
                        method, counter.getTotal(), timerName, schedulerId, dbItem.getSchedulerId(), dbItem.getId(), dbItem.getJobName()));
                insert = false;
            }
            jobName = normalizeRegex(jobName);
            if (insert && !jobName.equals(DBLayerSchedulerMon.DEFAULT_EMPTY_NAME) && !dbItem.getJobName().matches(jobName)) {
                LOGGER.debug(String.format(
                        "%s: %s)[timer name=%s][not match Job name]-[configured TimerJob name=%s][db jobName=%s][notification id=%s]", method, counter
                                .getTotal(), timerName, jobName, dbItem.getJobName(), dbItem.getId()));
                insert = false;
            }
            if (insert) {
                counter.addInsertTimer();
                LOGGER.debug(String.format(
                        "%s: %s)[timer name=%s][insert]-[configured TimerJob name=%s][db jobName=%s][notification id=%s, schedulerId=%s]", method,
                        counter.getTotal(), timerName, jobName, dbItem.getJobName(), dbItem.getId(), dbItem.getSchedulerId()));
                getDbLayer().createCheck(timerName, dbItem, DBLayer.DEFAULT_EMPTY_NAME, DBLayer.DEFAULT_EMPTY_NAME, dbItem.getTaskStartTime(), dbItem
                        .getTaskEndTime(), DBLayer.NOTIFICATION_OBJECT_TYPE_JOB);
            }
        }

        return counter;
    }

    private CounterCheckHistory insertTimerJobChains(CounterCheckHistory counter, DBItemSchedulerMonNotifications dbItem, String timerName,
            ArrayList<ElementTimerJobChain> timerJobChains) throws Exception {
        // output indent
        String method = "  insertTimerJobChains";
        // only first notification (step 1)
        if (dbItem.getStep().equals(new Long(1))) {
            for (int i = 0; i < timerJobChains.size(); i++) {
                ElementTimerJobChain jobChain = timerJobChains.get(i);
                String schedulerId = jobChain.getSchedulerId();
                String jobChainName = jobChain.getName();
                boolean insert = true;
                if (!schedulerId.equals(DBLayerSchedulerMon.DEFAULT_EMPTY_NAME) && !dbItem.getSchedulerId().matches(schedulerId)) {
                    LOGGER.debug(String.format("%s: %s)[timer name=%s][not match schedulerId]-[configured scheduler_id=%s][db schedulerId=%s]"
                            + "[notification id=%s, jobChain=%s, step=%s, stepState=%s, stepFrom=%s, stepTo=%s]", method, counter.getTotal(),
                            timerName, schedulerId, dbItem.getSchedulerId(), dbItem.getId(), dbItem.getJobChainName(), dbItem.getStep(), dbItem
                                    .getOrderStepState(), jobChain.getStepFrom(), jobChain.getStepTo()));
                    insert = false;
                }
                jobChainName = normalizeRegex(jobChainName);
                if (insert && !jobChainName.equals(DBLayerSchedulerMon.DEFAULT_EMPTY_NAME) && !dbItem.getJobChainName().matches(jobChainName)) {
                    LOGGER.debug(String.format(
                            "%s: %s)[timer name=%s][not match JobChain name]-[configured TimerJobChain name=%s][db jobChainName=%s]"
                                    + "[notification id=%s, schedulerId=%s, step=%s, stepState=%s, stepFrom=%s, stepTo=%s]", method, counter
                                            .getTotal(), timerName, jobChainName, dbItem.getJobChainName(), dbItem.getId(), dbItem.getSchedulerId(),
                            dbItem.getStep(), dbItem.getOrderStepState(), jobChain.getStepFrom(), jobChain.getStepTo()));
                    insert = false;
                }
                if (insert) {
                    counter.addInsertTimer();
                    LOGGER.debug(String.format(
                            "%s: %s)[timer name=%s][insert]-[configured TimerJobChain name=%s][db jobChainName=%s][notification id=%s, schedulerId=%s, step=%s, "
                                    + "stepState=%s, stepFrom=%s, stepTo=%s]", method, counter.getTotal(), timerName, jobChainName, dbItem
                                            .getJobChainName(), dbItem.getId(), dbItem.getSchedulerId(), dbItem.getStep(), dbItem.getOrderStepState(),
                            jobChain.getStepFrom(), jobChain.getStepTo()));
                    getDbLayer().createCheck(timerName, dbItem, jobChain.getStepFrom(), jobChain.getStepTo(), dbItem.getOrderStartTime(), dbItem
                            .getOrderEndTime(), DBLayer.NOTIFICATION_OBJECT_TYPE_JOB_CHAIN);
                }
            }

        } else {
            LOGGER.debug(String.format(
                    "%s: %s)[timer name=%s][skip insert]-step is not equals 1[notification id=%s, schedulerId=%s, jobChain=%s, step=%s, stepState=%s]",
                    method, counter.getTotal(), timerName, dbItem.getId(), dbItem.getSchedulerId(), dbItem.getJobChainName(), dbItem.getStep(), dbItem
                            .getOrderStepState()));
        }
        return counter;
    }

    private void insertTimer(CounterCheckHistory counter, DBItemSchedulerMonNotifications dbItem) throws Exception {
        // output indent
        String method = "  insertTimer";
        if (timers == null) {
            LOGGER.debug(String.format(
                    "%s: %s) skip do check. timers is null. notificationId=%s (schedulerId=%s, jobChain=%s, step=%s, stepState=%s)", method, counter
                            .getTotal(), dbItem.getId(), dbItem.getSchedulerId(), dbItem.getJobChainName(), dbItem.getStep(), dbItem
                                    .getOrderStepState()));
            return;
        }
        Set<Map.Entry<String, ElementTimer>> set = this.timers.entrySet();
        for (Map.Entry<String, ElementTimer> me : set) {
            String timerName = me.getKey();
            ElementTimer timer = me.getValue();

            ArrayList<ElementTimerJob> timerJobs = timer.getJobs();
            ArrayList<ElementTimerJobChain> timerJobChains = timer.getJobChains();
            if (timerJobs.isEmpty() && timerJobChains.isEmpty()) {
                LOGGER.warn(String.format("%s: %s)[timer name=%s]not found configured JobChains or Jobs.", method, counter.getTotal(), timerName));
                continue;
            }

            if (!timerJobs.isEmpty()) {
                counter = insertTimerJobs(counter, dbItem, timerName, timerJobs);
            }
            if (!dbItem.getStandalone()) {
                if (!timerJobChains.isEmpty()) {
                    counter = insertTimerJobChains(counter, dbItem, timerName, timerJobChains);
                }
            }
        }
    }

    private void pluginsOnInit(LinkedHashMap<String, ElementTimer> timers, CheckHistoryJobOptions options, DBLayerSchedulerMon dbLayer) {
        for (ICheckHistoryPlugin plugin : plugins) {
            try {
                plugin.onInit(timers, options, dbLayer);
            } catch (Exception ex) {
                LOGGER.warn(ex.getMessage());
            }
        }
    }

    @SuppressWarnings("unused")
    private void pluginsOnExit(LinkedHashMap<String, ElementTimer> timers, CheckHistoryJobOptions options, DBLayerSchedulerMon dbLayer) {
        for (ICheckHistoryPlugin plugin : plugins) {
            try {
                plugin.onExit(timers, options, dbLayer);
            } catch (Exception ex) {
                LOGGER.warn(ex.getMessage());
            }
        }
    }

    private void pluginsOnProcess(LinkedHashMap<String, ElementTimer> timers, CheckHistoryJobOptions options, DBLayerSchedulerMon dbLayer,
            Date dateFrom, Date dateTo) {
        for (ICheckHistoryPlugin plugin : plugins) {
            try {
                plugin.onProcess(timers, options, dbLayer, dateFrom, dateTo);
            } catch (Exception ex) {
                LOGGER.warn(String.format("plugin.onProcess: %s", ex.getMessage()));
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void registerPlugins() throws Exception {
        plugins = new ArrayList<ICheckHistoryPlugin>();
        // if (SOSString.isEmpty(this.options.plugins.getValue())) {
        this.options.plugins.setValue(CheckHistoryTimerPlugin.class.getName());
        // }
        String[] arr = this.options.plugins.getValue().trim().split(";");
        for (int i = 0; i < arr.length; i++) {
            try {
                Class<ICheckHistoryPlugin> c = (Class<ICheckHistoryPlugin>) Class.forName(arr[i].trim());
                addPlugin(c.newInstance());
                LOGGER.debug(String.format("plugin created=%s", arr[i]));
            } catch (Exception ex) {
                LOGGER.error(String.format("plugin cannot be registered(%s): %s", arr[i], ex.getMessage()));
            }
        }

        LOGGER.debug(String.format("plugins registered=%s", plugins.size()));
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

}
