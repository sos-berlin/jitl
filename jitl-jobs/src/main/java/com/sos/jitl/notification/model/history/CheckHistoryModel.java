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
    private boolean checkInsertNotifications = true;
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
        checkInsertNotifications = true;
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
        if (!checkInsertNotifications) {
            return;
        }
        NodeList notificationJobs = NotificationXmlHelper.selectNotificationJobDefinitions(xpath);
        setConfigJobs(xpath, notificationJobs);
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
                checkInsertNotifications = false;
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
        if (checkInsertNotifications) {
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
                checkInsertNotifications = false;
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

    private boolean checkInsertNotification(CounterCheckHistory counter, NotificationReportExecution execution) throws Exception {
        // Indent for the output
        String method = "  checkInsertNotification";
        LOGGER.debug(String.format("%s: %s) checkInsertNotifications = %s", method, counter.getTotal(), checkInsertNotifications));
        if (!checkInsertNotifications) {
            return true;
        }
        if ((jobs == null || jobs.isEmpty()) && (jobChains == null || jobChains.isEmpty())) {
            return false;
        }
        LOGGER.debug(String.format("%s: %s) jobChains: schedulerId = %s, jobChain = %s, taskJobName = %s", method, counter.getTotal(), execution
                .getSchedulerId(), execution.getJobChainName(), execution.getJobName()));
        Set<Map.Entry<String, ArrayList<String>>> set = jobChains.entrySet();
        for (Map.Entry<String, ArrayList<String>> jc : set) {
            String schedulerId = jc.getKey();
            ArrayList<String> jobChainsFromSet = jc.getValue();
            boolean checkJobChains = true;
            if (!schedulerId.equals(DBLayerSchedulerMon.DEFAULT_EMPTY_NAME)) {
                try {
                    if (!execution.getSchedulerId().matches(schedulerId)) {
                        checkJobChains = false;
                    }
                } catch (Exception ex) {
                    throw new Exception(String.format("%s: %s) jobChains: check with configured scheduler_id = %s: %s", method, counter.getTotal(),
                            schedulerId, ex));
                }
            }
            if (checkJobChains) {
                for (int i = 0; i < jobChainsFromSet.size(); i++) {
                    String jobChainName = jobChainsFromSet.get(i);

                    // jobChain = Matcher.quoteReplacement(jobChain);
                    if (jobChainName.equals(DBLayerSchedulerMon.DEFAULT_EMPTY_NAME)) {
                        LOGGER.debug(String.format("%s: %s) jobChains: db JobChain = %s match with configured jobChain = %s", method, counter
                                .getTotal(), execution.getJobChainName(), jobChainName));
                        return true;
                    }
                    try {
                        jobChainName = normalizePath(jobChainName);
                        if (execution.getJobChainName().matches(jobChainName)) {
                            LOGGER.debug(String.format("%s: %s) jobChains: db JobChain = %s match with configured jobChain = %s", method, counter
                                    .getTotal(), execution.getJobChainName(), jobChainName));
                            return true;
                        } else {
                            LOGGER.debug(String.format("%s: %s) jobChains: db JobChain = %s not match with configured jobChain = %s", method, counter
                                    .getTotal(), execution.getJobChainName(), jobChainName));
                        }
                    } catch (Exception ex) {
                        throw new Exception(String.format("%s: %s) jobChains: check with configured scheduler_id = %s, name = %s: %s", method, counter
                                .getTotal(), schedulerId, jobChainName, ex));
                    }
                }
            }
        }

        set = jobs.entrySet();
        LOGGER.debug(String.format("%s: %s) jobs: schedulerId = %s, jobChain = %s, taskJobName = %s", method, counter.getTotal(), execution
                .getSchedulerId(), execution.getJobChainName(), execution.getJobName()));
        for (Map.Entry<String, ArrayList<String>> jc : set) {
            String schedulerId = jc.getKey();
            ArrayList<String> jobsFromSet = jc.getValue();
            boolean checkJobs = true;
            if (!schedulerId.equals(DBLayerSchedulerMon.DEFAULT_EMPTY_NAME)) {
                try {
                    if (!execution.getSchedulerId().matches(schedulerId)) {
                        checkJobs = false;
                    }
                } catch (Exception ex) {
                    throw new Exception(String.format("%s: %s) jobs: check with configured scheduler_id = %s: %s", method, counter.getTotal(),
                            schedulerId, ex));
                }
            }
            if (checkJobs) {
                for (int i = 0; i < jobsFromSet.size(); i++) {
                    String job = jobsFromSet.get(i);
                    LOGGER.debug(String.format("%s: %s) jobs: check with configured: schedulerId = %s, job = %s", method, counter.getTotal(),
                            schedulerId, job));
                    if (job.equals(DBLayerSchedulerMon.DEFAULT_EMPTY_NAME)) {
                        return true;
                    }
                    try {
                        job = normalizePath(job);
                        if (execution.getJobName().matches(job)) {
                            LOGGER.debug(String.format("%s: %s) job: db Job = %s match with configured job = %s", method, counter.getTotal(),
                                    execution.getJobName(), job));
                            return true;
                        }
                    } catch (Exception ex) {
                        throw new Exception(String.format("%s: %s) jobs: check with configured scheduler_id = %s, name = %s: %s", method, counter
                                .getTotal(), schedulerId, job, ex));
                    }
                }
            }
        }

        return false;
    }

    @Override
    public void process() throws Exception {

    }

    public void process(NotificationReportExecution item) throws Exception {
        String method = "process";

        initCounters();
        boolean success = true;
        if (!this.checkInsertNotification(counter, item)) {
            counter.addSkip();
            LOGGER.debug(String.format(
                    "%s: %s) skip insert notification. order schedulerId = %s, jobChain = %s, order id = %s, step = %s, step state = %s", method,
                    counter.getTotal(), item.getSchedulerId(), item.getJobChainName(), item.getOrderId(), item.getStep(), item.getOrderStepState()));
            return;
        }

        try {
            DBItemSchedulerMonNotifications dbItem = this.getDbLayer().getNotification(item.getSchedulerId(), false, item.getTaskId(), item.getStep(),
                    item.getOrderHistoryId(), true);
            boolean hasStepError = item.getError();
            if (dbItem == null) {
                counter.addInsert();
                LOGGER.debug(String.format(
                        "%s: %s) create new notification. order schedulerId = %s, jobChain = %s, order id = %s, step = %s, step state = %s", method,
                        counter.getTotal(), item.getSchedulerId(), item.getJobChainName(), item.getOrderId(), item.getStep(), item
                                .getOrderStepState()));
                dbItem = getDbLayer().createNotification(item.getSchedulerId(), false, item.getTaskId(), item.getStep(), item.getOrderHistoryId(),
                        item.getJobChainName(), item.getJobChainTitle(), item.getOrderId(), item.getOrderTitle(), item.getOrderStartTime(), item
                                .getOrderEndTime(), item.getOrderStepState(), item.getOrderStepStartTime(), item.getOrderStepEndTime(), item
                                        .getJobName(), item.getJobTitle(), item.getTaskStartTime(), item.getTaskEndTime(), false, new Long(item
                                                .getReturnCode() == null ? 0 : item.getReturnCode()), item.getAgentUrl(), item.getClusterMemberId(),
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
                LOGGER.debug(String.format(
                        "%s: %s) update notification. notification id = %s, order schedulerId = %s, jobChain = %s, order id = %s, step = %s, "
                                + "step state = %s", method, counter.getTotal(), dbItem.getId(), dbItem.getSchedulerId(), dbItem.getJobChainName(),
                        dbItem.getOrderId(), dbItem.getStep(), dbItem.getOrderStepState()));
                getDbLayer().getSession().update(dbItem);
            }

            if (!item.getError() && item.getOrderStepEndTime() != null && item.getStep() > new Long(1)) {
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

    private void insertTimer(CounterCheckHistory counter, DBItemSchedulerMonNotifications dbItem) throws Exception {
        // output indent
        String method = "  insertTimer";
        if (timers == null) {
            LOGGER.debug(String.format(
                    "%s: %s) skip do check. timers is null. notification.id = %s (scheduler = %s, jobChain = %s, step = %s, step state = %s)", method,
                    counter.getTotal(), dbItem.getId(), dbItem.getSchedulerId(), dbItem.getJobChainName(), dbItem.getStep(), dbItem
                            .getOrderStepState()));
            return;
        }
        // only first notification (step 1)
        if (dbItem.getStep().equals(new Long(1))) {
            Set<Map.Entry<String, ElementTimer>> set = this.timers.entrySet();
            for (Map.Entry<String, ElementTimer> me : set) {
                String timerName = me.getKey();
                ElementTimer timer = me.getValue();
                ArrayList<ElementTimerJobChain> timerJobChains = timer.getJobChains();
                if (timerJobChains.isEmpty()) {
                    LOGGER.warn(String.format(
                            "%s: %s) timer = %s. timer JobChains not found. notification.id = %s (scheduler = %s, jobChain = %s, step = %s, "
                                    + "step state = %s)", method, counter.getTotal(), timerName, timerJobChains.size(), dbItem.getId(), dbItem
                                            .getSchedulerId(), dbItem.getJobChainName(), dbItem.getStep(), dbItem.getOrderStepState()));
                    continue;
                }
                for (int i = 0; i < timerJobChains.size(); i++) {
                    ElementTimerJobChain jobChain = timerJobChains.get(i);
                    String schedulerId = jobChain.getSchedulerId();
                    String jobChainName = jobChain.getName();
                    boolean insert = true;
                    if (!schedulerId.equals(DBLayerSchedulerMon.DEFAULT_EMPTY_NAME) && !dbItem.getSchedulerId().matches(schedulerId)) {
                        LOGGER.debug(String.format("%s: %s) skip insert check. notification.schedulerId \"%s\" not match timer schedulerId \"%s\" "
                                + "( timer  name = %s, notification.id = %s (jobChain = %s, step = %s, step state = %s), stepFrom = %s, stepTo = %s ",
                                method, counter.getTotal(), dbItem.getSchedulerId(), schedulerId, timerName, dbItem.getId(), dbItem.getJobChainName(),
                                dbItem.getStep(), dbItem.getOrderStepState(), jobChain.getStepFrom(), jobChain.getStepTo()));
                        insert = false;
                    }
                    if (insert && !jobChainName.equals(DBLayerSchedulerMon.DEFAULT_EMPTY_NAME) && !dbItem.getJobChainName().matches(normalizePath(
                            jobChainName))) {
                        LOGGER.debug(String.format("%s: %s) skip insert check. notification.jobChain \"%s\" not match timer job chain \"%s\" "
                                + "( timer  name = %s, notification.id = %s (scheduler = %s, step = %s, step state = %s), stepFrom = %s, stepTo = %s ",
                                method, counter.getTotal(), dbItem.getJobChainName(), jobChainName, timerName, dbItem.getId(), dbItem
                                        .getSchedulerId(), dbItem.getStep(), dbItem.getOrderStepState(), jobChain.getStepFrom(), jobChain
                                                .getStepTo()));
                        insert = false;
                    }
                    if (insert) {
                        counter.addInsertTimer();
                        LOGGER.debug(String.format("%s: %s) insert check. name = %s, notification.id = %s (scheduler = %s, jobChain = %s, step = %s, "
                                + "step state = %s), stepFrom = %s, stepTo = %s ", method, counter.getTotal(), timerName, dbItem.getId(), dbItem
                                        .getSchedulerId(), dbItem.getJobChainName(), dbItem.getStep(), dbItem.getOrderStepState(), jobChain
                                                .getStepFrom(), jobChain.getStepTo()));
                        getDbLayer().createCheck(timerName, dbItem, jobChain.getStepFrom(), jobChain.getStepTo(), dbItem.getOrderStartTime(), dbItem
                                .getOrderEndTime());
                    } else {
                        LOGGER.debug(String.format(
                                "%s: %s) not inserted. timer (name = %s, schedulerId = %s, jobChain = %s, stepFrom = %s, stepTo = %s),  "
                                        + "notification (id = %s, jobChain = %s, step = %s, step state = %s)", method, counter.getTotal(), timerName,
                                jobChain.getSchedulerId(), jobChain.getName(), jobChain.getStepFrom(), jobChain.getStepTo(), dbItem.getId(), dbItem
                                        .getJobChainName(), dbItem.getStep(), dbItem.getOrderStepState()));
                    }
                }
            }
        } else {
            LOGGER.debug(String.format(
                    "%s: %s) skip do check. step is not equals 1. notification.id = %s (scheduler = %s, jobChain = %s, step = %s, step state = %s)",
                    method, counter.getTotal(), dbItem.getId(), dbItem.getSchedulerId(), dbItem.getJobChainName(), dbItem.getStep(), dbItem
                            .getOrderStepState()));
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
                LOGGER.debug(String.format("plugin created = %s", arr[i]));
            } catch (Exception ex) {
                LOGGER.error(String.format("plugin cannot be registered(%s) : %s", arr[i], ex.getMessage()));
            }
        }

        LOGGER.debug(String.format("plugins registered = %s", plugins.size()));
    }

    public static String normalizePath(String val) {
        if (val != null && val.startsWith("/")) {
            val = val.substring(1);
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
