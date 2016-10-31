package com.sos.jitl.reporting.model.inventory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.hibernate.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import sos.util.SOSString;
import sos.xml.SOSXMLXPath;

import com.sos.hibernate.classes.SOSHibernateConnection;
import com.sos.jitl.reporting.db.DBItemInventoryAgentCluster;
import com.sos.jitl.reporting.db.DBItemInventoryAgentClusterMember;
import com.sos.jitl.reporting.db.DBItemInventoryAgentInstance;
import com.sos.jitl.reporting.db.DBItemInventoryAppliedLock;
import com.sos.jitl.reporting.db.DBItemInventoryFile;
import com.sos.jitl.reporting.db.DBItemInventoryInstance;
import com.sos.jitl.reporting.db.DBItemInventoryJob;
import com.sos.jitl.reporting.db.DBItemInventoryJobChain;
import com.sos.jitl.reporting.db.DBItemInventoryJobChainNode;
import com.sos.jitl.reporting.db.DBItemInventoryLock;
import com.sos.jitl.reporting.db.DBItemInventoryOperatingSystem;
import com.sos.jitl.reporting.db.DBItemInventoryOrder;
import com.sos.jitl.reporting.db.DBItemInventoryProcessClass;
import com.sos.jitl.reporting.db.DBItemInventorySchedule;
import com.sos.jitl.reporting.db.DBLayer;
import com.sos.jitl.reporting.helper.EConfigFileExtensions;
import com.sos.jitl.reporting.helper.ReportUtil;
import com.sos.jitl.reporting.helper.ReportXmlHelper;
import com.sos.jitl.reporting.helper.SaveOrUpdateHelper;
import com.sos.jitl.reporting.job.inventory.InventoryJobOptions;
import com.sos.jitl.reporting.model.IReportingModel;
import com.sos.jitl.reporting.model.ReportingModel;
import com.sos.jitl.runonce.data.ProcessDataUtil;

public class InventoryModel extends ReportingModel implements IReportingModel {

    private static final Logger LOGGER = LoggerFactory.getLogger(InventoryModel.class);
    private InventoryJobOptions options;
    private DBItemInventoryInstance inventoryInstance;
    private int countTotalJobs = 0;
    private int countSuccessJobs = 0;
    private int countTotalJobChains = 0;
    private int countSuccessJobChains = 0;
    private int countTotalOrders = 0;
    private int countSuccessOrders = 0;
    private int countNotFoundedJobChainJobs = 0;
    private int countTotalLocks = 0;
    private int countSuccessLocks = 0;
    private int countTotalProcessClasses = 0;
    private int countSuccessProcessClasses = 0;
    private int countTotalSchedules = 0;
    private int countSuccessSchedules = 0;
    private LinkedHashMap<String, ArrayList<String>> notFoundedJobChainJobs;
    private LinkedHashMap<String, String> errorJobChains;
    private LinkedHashMap<String, String> errorOrders;
    private LinkedHashMap<String, String> errorJobs;
    private Map<String, String> errorLocks;
    private Map<String, String> errorProcessClasses;
    private Map<String, String> errorSchedules;
    private Instant started;
    private String cachePath;

    public InventoryModel(SOSHibernateConnection reportingConn, InventoryJobOptions opt) throws Exception {
        super(reportingConn);
        this.options = opt;
        this.cachePath = options.schedulerData.getValue() + "/config/cache";
    }

    @Override
    public void process() throws Exception {
        String method = "process";
        try {
            initCounters();
            started = Instant.now();
            initInventoryInstance();
            if(inventoryInstance.getSupervisorId() != null && inventoryInstance.getSupervisorId() != DBLayer.DEFAULT_ID) {
                processConfigurationDirectory(cachePath);
            }
            processConfigurationDirectory(options.current_scheduler_configuration_directory.getValue());
            logSummary();
            resume();
            SaveOrUpdateHelper.refreshUsedInJobChains(getDbLayer());
            cleanUpInventoryAfter(started);
        } catch (Exception ex) {
            try {
                getDbLayer().getConnection().rollback();
            } catch (Exception e) {
                // no exception handling
            }
            throw new Exception(String.format("%s: %s", method, ex.toString()), ex);
        }
    }

    private void initInventoryInstance() throws Exception {
        getDbLayer().getConnection().beginTransaction();
        setInventoryInstance();
        getDbLayer().getConnection().commit();
    }

    private void cleanupInventory() throws Exception {
        String method = "cleanupInventory";
        if (inventoryInstance == null) {
            throw new Exception(String.format("%s: inventoryInstance is NULL", method));
        }
        LOGGER.info(String.format("%s: cleanup for instanceId = %s, scheduler_id = %s, host = %s:%s", method, inventoryInstance.getId(),
                inventoryInstance.getSchedulerId(), inventoryInstance.getHostname(), inventoryInstance.getPort()));
        getDbLayer().cleanupInventory(inventoryInstance.getId());
    }
    
    private void initCounters() {
        countTotalJobs = 0;
        countTotalJobChains = 0;
        countTotalOrders = 0;
        countSuccessJobs = 0;
        countSuccessJobChains = 0;
        countSuccessOrders = 0;
        countNotFoundedJobChainJobs = 0;
        countTotalLocks = 0;
        countSuccessLocks = 0;
        countTotalProcessClasses = 0;
        countSuccessProcessClasses = 0;
        countTotalSchedules = 0;
        countSuccessSchedules = 0;
        notFoundedJobChainJobs = new LinkedHashMap<String, ArrayList<String>>();
        errorJobChains = new LinkedHashMap<String, String>();
        errorOrders = new LinkedHashMap<String, String>();
        errorJobs = new LinkedHashMap<String, String>();
        errorLocks = new LinkedHashMap<String, String>();
        errorProcessClasses = new LinkedHashMap<String, String>();
        errorSchedules = new LinkedHashMap<String, String>();
    }

    private void logSummary() {
        String method = "logSummary";
        LOGGER.info(String.format("%s: inserted or updated job chains = %s (total %s, error = %s)", method, countSuccessJobChains, 
                countTotalJobChains, errorJobChains.size()));
        LOGGER.info(String.format("%s: inserted or updated orders = %s (total %s, error = %s)", method, countSuccessOrders, countTotalOrders, 
                errorOrders.size()));
        LOGGER.info(String.format("%s: inserted or updated jobs = %s (total %s, error = %s)", method, countSuccessJobs, countTotalJobs, 
                errorJobs.size()));
        LOGGER.info(String.format("%s: inserted or updated locks = %s (total %s, error = %s)", method, countSuccessLocks, countTotalLocks,
                errorLocks.size()));
        LOGGER.info(String.format("%s: inserted or updated process classes = %s (total %s, error = %s)", method, countSuccessProcessClasses,
                countTotalProcessClasses, errorProcessClasses.size()));
        LOGGER.info(String.format("%s: inserted or updated schedules = %s (total %s, error = %s)", method, countSuccessSchedules, 
                countTotalSchedules, errorSchedules.size()));
        if (!errorJobChains.isEmpty()) {
            LOGGER.info(String.format("%s:   errors by insert or update job chains:", method));
            int i = 1;
            for (Entry<String, String> entry : errorJobChains.entrySet()) {
                LOGGER.info(String.format("%s:     %s) %s: %s", method, i, entry.getKey(), entry.getValue()));
                i++;
            }
        }
        if (!errorOrders.isEmpty()) {
            LOGGER.info(String.format("%s:   errors by insert or update orders:", method));
            int i = 1;
            for (Entry<String, String> entry : errorOrders.entrySet()) {
                LOGGER.info(String.format("%s:     %s) %s: %s", method, i, entry.getKey(), entry.getValue()));
                i++;
            }
        }
        if (!errorJobs.isEmpty()) {
            LOGGER.info(String.format("%s:   errors by insert or update jobs:", method));
            int i = 1;
            for (Entry<String, String> entry : errorJobs.entrySet()) {
                LOGGER.info(String.format("%s:     %s) %s: %s", method, i, entry.getKey(), entry.getValue()));
                i++;
            }
        }
        if (!errorLocks.isEmpty()) {
            LOGGER.info(String.format("%s:   errors by insert or update locks:", method));
            int i = 1;
            for (Entry<String, String> entry : errorLocks.entrySet()) {
                LOGGER.info(String.format("%s:     %s) %s: %s", method, i, entry.getKey(), entry.getValue()));
                i++;
            }
        }
        if (!errorProcessClasses.isEmpty()) {
            LOGGER.info(String.format("%s:   errors by insert or update process classes:", method));
            int i = 1;
            for (Entry<String, String> entry : errorProcessClasses.entrySet()) {
                LOGGER.info(String.format("%s:     %s) %s: %s", method, i, entry.getKey(), entry.getValue()));
                i++;
            }
        }
        if (!errorSchedules.isEmpty()) {
            LOGGER.info(String.format("%s:   errors by insert or update schedules:", method));
            int i = 1;
            for (Entry<String, String> entry : errorSchedules.entrySet()) {
                LOGGER.info(String.format("%s:     %s) %s: %s", method, i, entry.getKey(), entry.getValue()));
                i++;
            }
        }
        if (countNotFoundedJobChainJobs > 0) {
            LOGGER.info(String.format("%s: not founded jobs on the disc (declared in the job chains) = %s", method, countNotFoundedJobChainJobs));
            int i = 1;
            for (Entry<String, ArrayList<String>> entry : notFoundedJobChainJobs.entrySet()) {
                LOGGER.info(String.format("%s:     %s) %s", method, i, entry.getKey()));
                for (int j = 0; j < entry.getValue().size(); j++) {
                    LOGGER.info(String.format("%s:         %s) %s", method, j + 1, entry.getValue().get(j)));
                }
                i++;
            }
        }
    }

    private void resume() throws Exception {
        if (countSuccessJobChains == 0 && countSuccessOrders == 0 && countSuccessJobs == 0 && countSuccessLocks == 0
                && countSuccessProcessClasses == 0 && countSuccessSchedules == 0) {
            throw new Exception(String.format("error occured: 0 job chains, orders, jobs locks, process classes or schedules inserted or updated!"));
        }
        if (!errorJobChains.isEmpty() || !errorOrders.isEmpty() || !errorJobs.isEmpty() || !errorLocks.isEmpty()
                || !errorProcessClasses.isEmpty() || !errorSchedules.isEmpty()) {
            LOGGER.warn(String.format("error occured: insert or update failed by %s job chains, %s orders, %s jobs, %s locks, %s process classes, "
                    + "%s schedules", errorJobChains.size(), errorOrders.size(), errorJobs.size(), errorLocks.size(), errorProcessClasses.size(),
                    errorSchedules.size()));
        }
    }

    private void processConfigurationDirectory(String directory) throws Exception {
        String method = "processConfigurationDirectory";
        File dir = new File(directory);
        if (!dir.exists()) {
            throw new Exception(String.format("%s: configuration directory not found. directory = %s", method, dir.getAbsolutePath()));
        }
        LOGGER.info(String.format("%s: dir = %s", method, dir.getCanonicalPath()));
        processDirectory(dir, getConfigDirectoryPathLenght(dir));
    }

    private int getConfigDirectoryPathLenght(File configDirectoiry) throws IOException {
        return configDirectoiry.getCanonicalPath().length() + 1;
    }

    private void processDirectory(File dir, int rootPathLen) throws Exception {
        String method = "processDirectory";
        try {
            LOGGER.debug(String.format("%s: dir = %s", method, dir.getCanonicalPath()));
            File[] files = dir.listFiles();
            for (File file : files) {
                if (file.canRead() && !file.isHidden()) {
                    if (file.isDirectory()) {
                        this.processDirectory(file, rootPathLen);
                    } else {
                        String f = ReportUtil.normalizeFilePath2SchedulerPath(file, rootPathLen);
                        String fLower = f.toLowerCase();
                        if (fLower.endsWith(EConfigFileExtensions.LOCK.extension())) {
                            processLock(file, f);
                        } else if (fLower.endsWith(EConfigFileExtensions.PROCESS_CLASS.extension())) {
                            processProcessClass(file, f);
                        } else if (fLower.endsWith(EConfigFileExtensions.SCHEDULE.extension())) {
                            processSchedule(file, f);
                        } else if (fLower.endsWith(EConfigFileExtensions.JOB.extension())) {
                            processJob(file, f);
                        } else if (fLower.endsWith(EConfigFileExtensions.JOB_CHAIN.extension())) {
                            processJobChain(file, f, rootPathLen);
                        } else if (fLower.endsWith(EConfigFileExtensions.ORDER.extension())) {
                            processOrder(file, f);
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new Exception(String.format("%s: directory = %s, exception = %s", method, dir.getCanonicalPath(), e.toString()), e);
        }
    }

    private void processJob(File file, String schedulerFilePath) throws Exception {
        String method = "    processJob";
        countTotalJobs++;
        try {
            DBItemInventoryJob item = createInventoryJob(file, schedulerFilePath);
            SaveOrUpdateHelper.saveOrUpdateJob(getDbLayer(), item);
            LOGGER.debug(String.format("%s: job     id = %s, jobName = %s, jobBasename = %s, title = %s, isOrderJob = %s, isRuntimeDefined = %s",
                    method, item.getId(), item.getName(), item.getBaseName(), item.getTitle(), item.getIsOrderJob(), item.getIsRuntimeDefined()));
            countSuccessJobs++;
            SOSXMLXPath xpath = new SOSXMLXPath(file.getCanonicalPath());
            if(ReportXmlHelper.hasLockUses(xpath)) {
                NodeList lockUses = ReportXmlHelper.getLockUses(xpath);
                for(int i = 0; i < lockUses.getLength(); i++) {
                    Element lockUse = (Element)lockUses.item(i); 
                    String lockName = lockUse.getAttribute("lock");
                    if(lockName.contains("/")) {
                        lockName = lockName.substring(lockName.lastIndexOf("/") +1);
                    }
                    DBItemInventoryLock lock = getLockByName(lockName);
                    if(lock != null) {
                        DBItemInventoryAppliedLock appliedLock = new DBItemInventoryAppliedLock();
                        appliedLock.setJobId(item.getId());
                        appliedLock.setLockId(lock.getId());
                        SaveOrUpdateHelper.saveOrUpdateAppliedLock(getDbLayer(), appliedLock);
                    }
                }
            }
        } catch (Exception ex) {
            getDbLayer().getConnection().rollback();
            LOGGER.warn(String.format("%s: job file cannot be inserted = %s, exception = %s ", method, file.getCanonicalPath(),
                    ex.toString()), ex);
            errorJobs.put(file.getCanonicalPath(), ex.toString());
        }

    }

    private void processJobChain(File file, String schedulerFilePath, int rootPathLen) throws Exception {
        String method = "    processJobChain";
        countTotalJobChains++;
        try {
            DBItemInventoryFile dbItemFile = processFile(file, schedulerFilePath, EConfigFileExtensions.JOB_CHAIN.type());
            DBItemInventoryJobChain item = createInventoryJobChain(file, schedulerFilePath, rootPathLen);
            Long id = SaveOrUpdateHelper.saveOrUpdateJobChain(getDbLayer(), item);
            if (id != null) {
                item.setId(id);
            }
            LOGGER.debug(String.format("%s: jobChain    id = %s, startCause = %s, jobChainName = %s, jobChainBasename = %s, title = %s", method,
                    item.getId(), item.getStartCause(), item.getName(), item.getBaseName(), item.getTitle()));
            SOSXMLXPath xpath = new SOSXMLXPath(file.getCanonicalPath());
            if (xpath.getRoot() == null) {
                throw new Exception(String.format("xpath root missing"));
            }
            NodeList nl = ReportXmlHelper.getRootChilds(xpath);
            int ordering = 1;
            for (int j = 0; j < nl.getLength(); ++j) {
                Element jobChainNodeElement = (Element) nl.item(j);
                DBItemInventoryJobChainNode nodeItem = createInventoryJobChainNode(jobChainNodeElement, file, schedulerFilePath, rootPathLen, item);
                nodeItem.setInstanceId(dbItemFile.getInstanceId());
                nodeItem.setOrdering(new Long(ordering));
                Long nodeId = SaveOrUpdateHelper.saveOrUpdateJobChainNode(getDbLayer(), nodeItem);
                if (nodeId != null) {
                    nodeItem.setId(nodeId);
                }
                ordering++;
                LOGGER.debug(String.format(
                        "%s: jobChainNode     id = %s, nodeName = %s, ordering = %s, state = %s, nextState = %s, errorState = %s, job = %s, "
                                + "jobName = %s", method, nodeItem.getId(), nodeItem.getName(), nodeItem.getOrdering(), nodeItem.getState(),
                                nodeItem.getNextState(), nodeItem.getErrorState(), nodeItem.getJob(), nodeItem.getJobName()));
            }
            countSuccessJobChains++;
        } catch (Exception ex) {
            getDbLayer().getConnection().rollback();
            LOGGER.warn(String.format("%s: job chain file cannot be inserted = %s , exception = %s", method, file.getCanonicalPath(),
                    ex.toString()), ex);
            errorJobChains.put(file.getCanonicalPath(), ex.toString());
        }
    }

    private void processOrder(File file, String schedulerFilePath) throws Exception {
        String method = "    processOrder";
        countTotalOrders++;
        try {
            DBItemInventoryOrder item = createOrder(file, schedulerFilePath);
            SaveOrUpdateHelper.saveOrUpdateOrder(getDbLayer(), item);
            LOGGER.debug(String.format("%s: order     id = %s, jobChainName = %s, orderId = %s, title = %s, isRuntimeDefined = %s", method,
                    item.getId(), item.getJobChainName(), item.getOrderId(), item.getTitle(), item.getIsRuntimeDefined()));
            countSuccessOrders++;
        } catch (Exception ex) {
            getDbLayer().getConnection().rollback();
            LOGGER.warn(String.format("%s: order file cannot be inserted = %s, exception = ", method, file.getCanonicalPath(), ex.toString()), ex);
            errorOrders.put(file.getCanonicalPath(), ex.toString());
        }
    }

    private DBItemInventoryFile processFile(File file, String fileName, String fileType) throws Exception {
        String method = "  processFile";
        String fileBasename = file.getName();
        int li = fileName.lastIndexOf("/" + fileBasename);
        // fileDirectory ist direkt im live
        String fileDirectory = li > -1 ? fileName.substring(0, li) : null;
        Date fileCreated = null;
        Date fileModified = null;
        Date fileLocalCreated = null;
        Date fileLocalModified = null;
        Path path = file.toPath();
        BasicFileAttributes attrs = null;
        try {
            attrs = Files.readAttributes(path, BasicFileAttributes.class);
            fileCreated = ReportUtil.convertFileTime2UTC(attrs.creationTime());
            fileModified = ReportUtil.convertFileTime2UTC(attrs.lastModifiedTime());
            fileLocalCreated = ReportUtil.convertFileTime2Local(attrs.creationTime());
            fileLocalModified = ReportUtil.convertFileTime2Local(attrs.lastModifiedTime());
        } catch (IOException exception) {
            LOGGER.debug(String.format("%s: cannot read file attributes. file = %s, exception = %s  ", method, file.getCanonicalPath(),
                    exception.toString()));
        }
        DBItemInventoryFile item = new DBItemInventoryFile();
        item.setInstanceId(inventoryInstance.getId());
        item.setFileType(fileType);
        item.setFileName(fileName);
        item.setFileBaseName(fileBasename);
        item.setFileDirectory(fileDirectory);
        item.setFileCreated(fileCreated);
        item.setFileModified(fileModified);
        item.setFileLocalCreated(fileLocalCreated);
        item.setFileLocalModified(fileLocalModified);
        Long id = SaveOrUpdateHelper.saveOrUpdateFile(getDbLayer(), item);
        if(item.getId() == null) {
            item.setId(id);
        }
        LOGGER.debug(String.format(
                "%s: file     id = %s, fileType = %s, fileName = %s, fileBasename = %s, fileDirectory = %s, fileCreated = %s, fileModified = %s",
                method, item.getId(), item.getFileType(), item.getFileName(), item.getFileBaseName(), item.getFileDirectory(),
                item.getFileCreated(), item.getFileModified()));
        return item;
    }

    private void setInventoryInstance() throws Exception {
        String method = "setInventoryInstance";
        DBItemInventoryInstance ii = getDbLayer().getInventoryInstance(options.current_scheduler_id.getValue(),
                options.current_scheduler_hostname.getValue(), options.current_scheduler_port.value());
        String liveDirectory = ReportUtil.normalizePath(options.current_scheduler_configuration_directory.getValue());
        if (ii == null) {
            LOGGER.debug(String.format("%s: create new instance. schedulerId = %s, hostname = %s, port = %s, configuration directory = %s", method,
                    options.current_scheduler_id.getValue(), options.current_scheduler_hostname.getValue(),
                    new Long(options.current_scheduler_port.value()), liveDirectory));
            ii = new DBItemInventoryInstance();
            ii.setSchedulerId(options.current_scheduler_id.getValue());
            ii.setHostname(options.current_scheduler_hostname.getValue());
            ii.setPort(options.current_scheduler_port.value());
            ii.setLiveDirectory(options.current_scheduler_configuration_directory.getValue());
            ii.setCreated(ReportUtil.getCurrentDateTime());
            ii.setModified(ReportUtil.getCurrentDateTime());
            /** new Items since 1.11 */
            ProcessDataUtil dataUtil = new ProcessDataUtil(options.hibernate_configuration_file.getValue(), getDbLayer().getConnection());
            DBItemInventoryOperatingSystem osItem = dataUtil.getOsData(ii);
            ii.setOsId(dataUtil.saveOrUpdateOperatingSystem(osItem));
            ii.setVersion("");
            ii.setUrl("");
            ii.setCommandUrl("http://" + options.current_scheduler_hostname.getValue() + ":" + options.current_scheduler_port.getValue());
            ii.setTimeZone("");
            ii.setClusterType("standalone");
            ii.setPrecedence(null);
            ii.setDbmsName(dataUtil.getDbmsName(options.hibernate_configuration_file.getValue()));
            ii.setDbmsVersion(dataUtil.getDbVersion(ii.getDbmsName()));
            ii.setStartedAt(new Date());
            ii.setSupervisorId(DBLayer.DEFAULT_ID);
            /** End of new Items since 1.11 */
            getDbLayer().getConnection().save(ii);
        } else {
            getDbLayer().updateInventoryLiveDirectory(ii.getId(), liveDirectory);
        }
        inventoryInstance = ii;
    }

    private Integer getJobChainNodeType(String nodeName, Element jobChainNode) {
        switch(nodeName){
        case "job_chain_node":
            if(jobChainNode.hasAttribute("job")){
                return 1;
            } else {
                return 5;
            }
        case "job_chain_node.job_chain":
            return 2;
        case "file_order_source":
            return 3;
        case "file_order_sink":
            return 4;
        case "job_chain_node.end":
            return 5;
        }
        return null;
    }
    
    private DBItemInventoryJob createInventoryJob(File file, String schedulerFilePath) throws Exception {
        DBItemInventoryFile dbItemFile = processFile(file, schedulerFilePath, EConfigFileExtensions.JOB.type());
        String name = ReportUtil.getNameFromPath(schedulerFilePath, EConfigFileExtensions.JOB);
        String basename = ReportUtil.getNameFromPath(file.getName(), EConfigFileExtensions.JOB);
        SOSXMLXPath xpath = new SOSXMLXPath(file.getCanonicalPath());
        if (xpath.getRoot() == null) {
            throw new Exception(String.format("xpath root missing"));
        }
        String title = ReportXmlHelper.getTitle(xpath);
        boolean isOrderJob = ReportXmlHelper.isOrderJob(xpath);
        boolean isRuntimeDefined = ReportXmlHelper.isRuntimeDefined(xpath);
        DBItemInventoryJob item = new DBItemInventoryJob();
        item.setInstanceId(dbItemFile.getInstanceId());
        item.setFileId(dbItemFile.getId());
        item.setName(name);
        item.setBaseName(basename);
        item.setTitle(title);
        item.setIsOrderJob(isOrderJob);
        item.setIsRuntimeDefined(isRuntimeDefined);
        item.setCreated(ReportUtil.getCurrentDateTime());
        item.setModified(ReportUtil.getCurrentDateTime());
        /** new Items since 1.11 */
        if (xpath.getRoot().hasAttribute("process_class")) {
            String processClass = ReportXmlHelper.getProcessClass(xpath);
            String processClassName = SaveOrUpdateHelper.getProcessClassName(getDbLayer(), inventoryInstance.getId(), processClass);
            DBItemInventoryProcessClass ipc = SaveOrUpdateHelper.getProcessClassIfExists(getDbLayer(), item.getInstanceId(), 
                    processClass, processClassName);
            if(ipc != null) {
                item.setProcessClass(ipc.getBasename());
                item.setProcessClassName(ipc.getName());
                item.setProcessClassId(ipc.getId());
            } else {
                item.setProcessClass(processClass);
                item.setProcessClassName(processClassName);
                item.setProcessClassId(DBLayer.DEFAULT_ID);
            }
        } else {
            item.setProcessClassId(DBLayer.DEFAULT_ID);
            item.setProcessClassName(DBLayer.DEFAULT_NAME);
        }
        String schedule = ReportXmlHelper.getScheduleFromRuntime(xpath);
        if(schedule != null && !schedule.isEmpty()) {
          String scheduleName = SaveOrUpdateHelper.getScheduleName(getDbLayer(), inventoryInstance.getId(), schedule);
          DBItemInventorySchedule is = SaveOrUpdateHelper.getScheduleIfExists(getDbLayer(), item.getInstanceId(), schedule, scheduleName);
          if(is != null) {
              item.setSchedule(is.getBasename());
              item.setScheduleName(is.getName());
              item.setScheduleId(is.getId());
          } else {
              item.setSchedule(schedule);
              item.setScheduleName(scheduleName);
              item.setScheduleId(DBLayer.DEFAULT_ID);
          }
        } else {
            item.setScheduleId(DBLayer.DEFAULT_ID);
            item.setScheduleName(DBLayer.DEFAULT_NAME);
        }
        String maxTasks = xpath.getRoot().getAttribute("tasks");
        if(maxTasks != null && !maxTasks.isEmpty()) {
            item.setMaxTasks(Integer.parseInt(maxTasks));
        } else {
            item.setMaxTasks(0);
        }
        Boolean hasDescription = ReportXmlHelper.hasDescription(xpath);
        if(hasDescription != null) {
            item.setHasDescription(ReportXmlHelper.hasDescription(xpath));
        }
        /** End of new Items */
        return item;
    }

    private DBItemInventoryJobChain createInventoryJobChain(File file, String schedulerFilePath, int rootPathLen) throws Exception {
        DBItemInventoryFile dbItemFile = processFile(file, schedulerFilePath, EConfigFileExtensions.JOB_CHAIN.type());
        String name = ReportUtil.getNameFromPath(schedulerFilePath, EConfigFileExtensions.JOB_CHAIN);
        String basename = ReportUtil.getNameFromPath(file.getName(), EConfigFileExtensions.JOB_CHAIN);
        SOSXMLXPath xpath = new SOSXMLXPath(file.getCanonicalPath());
        if (xpath.getRoot() == null) {
            throw new Exception(String.format("xpath root missing"));
        }
        String title = ReportXmlHelper.getTitle(xpath);
        String startCause = ReportXmlHelper.getJobChainStartCause(xpath);
        DBItemInventoryJobChain item = new DBItemInventoryJobChain();
        item.setInstanceId(dbItemFile.getInstanceId());
        item.setFileId(dbItemFile.getId());
        item.setStartCause(startCause);
        item.setName(name);
        item.setBaseName(basename);
        item.setTitle(title);
        item.setCreated(ReportUtil.getCurrentDateTime());
        item.setModified(ReportUtil.getCurrentDateTime());
        /** new Items since 1.11 */
        String maxOrders = xpath.getRoot().getAttribute("max_orders");
        if(maxOrders != null && !maxOrders.isEmpty()) {
            item.setMaxOrders(Integer.parseInt(maxOrders));
        }
        item.setDistributed("yes".equalsIgnoreCase(xpath.getRoot().getAttribute("distributed")));
        if (xpath.getRoot().hasAttribute("process_class")) {
            String processClass = ReportXmlHelper.getProcessClass(xpath);
            String processClassName = SaveOrUpdateHelper.getProcessClassName(getDbLayer(), inventoryInstance.getId(), processClass);
            DBItemInventoryProcessClass ipc = SaveOrUpdateHelper.getProcessClassIfExists(getDbLayer(), item.getInstanceId(), 
                    processClass, processClassName);
            if(ipc != null) {
                item.setProcessClass(ipc.getBasename());
                item.setProcessClassName(ipc.getName());
                item.setProcessClassId(ipc.getId());
            } else {
                item.setProcessClass(processClass);
                item.setProcessClassName(processClassName);
                item.setProcessClassId(DBLayer.DEFAULT_ID);
            }
        } else {
            item.setProcessClassId(DBLayer.DEFAULT_ID);
            item.setProcessClassName(DBLayer.DEFAULT_NAME);
        }
        if (xpath.getRoot().hasAttribute("file_watching_process_class")) {
            String fwProcessClass = ReportXmlHelper.getFileWatchingProcessClass(xpath);
            String fwProcessClassName = SaveOrUpdateHelper.getProcessClassName(getDbLayer(), inventoryInstance.getId(), fwProcessClass);
            DBItemInventoryProcessClass ipc = SaveOrUpdateHelper.getProcessClassIfExists(getDbLayer(), item.getInstanceId(), 
                    fwProcessClass, fwProcessClassName);
            if(ipc != null) {
                item.setFileWatchingProcessClass(ipc.getBasename());
                item.setFileWatchingProcessClassName(ipc.getName());
                item.setFileWatchingProcessClassId(ipc.getId());
            } else {
                item.setFileWatchingProcessClass(fwProcessClass);
                item.setFileWatchingProcessClassName(fwProcessClassName);
                item.setFileWatchingProcessClassId(DBLayer.DEFAULT_ID);
            }
        } else {
            item.setFileWatchingProcessClassId(DBLayer.DEFAULT_ID);
            item.setFileWatchingProcessClassName(DBLayer.DEFAULT_NAME);
        }
        /** End of new Items */
        return item;
    }
    
    private DBItemInventoryJobChainNode createInventoryJobChainNode(Element jobChainNodeElement, File file, String schedulerFilePath,
            int rootPathLen, DBItemInventoryJobChain jobChain) throws Exception {
        String jobName = null;
        String nodeName = jobChainNodeElement.getNodeName();
        String job = jobChainNodeElement.getAttribute("job");
        String state = jobChainNodeElement.getAttribute("state");
        String nextState = jobChainNodeElement.getAttribute("next_state");
        String errorState = jobChainNodeElement.getAttribute("error_state");
        if (!SOSString.isEmpty(job)) {
            File fileJob = null;
            if (job.startsWith("/")) {
                fileJob = new File(options.current_scheduler_configuration_directory.getValue(), job + EConfigFileExtensions.JOB.extension());
            } else {
                fileJob = new File(file.getParent(), job + EConfigFileExtensions.JOB.extension());
            }
            if (fileJob.exists()) {
                String np = ReportUtil.normalizeFilePath2SchedulerPath(fileJob, rootPathLen);
                jobName = ReportUtil.getNameFromPath(np, EConfigFileExtensions.JOB);
            } else {
                String fileJobPath = null;
                try {
                    fileJobPath = fileJob.getCanonicalPath();
                } catch (Exception ex) {
                    // invalid file path like "c:/tmp/1/c:/123.xml"
                    throw new Exception(String.format("(job = %s, fileJob = %s): %s", job, fileJob.getAbsolutePath(), ex.toString()));
                }
                LOGGER.warn(String.format("job = %s: job chain = %s not found on the disc = %s ", job, jobChain.getName(),
                        fileJob.getCanonicalPath()));
                ArrayList<String> al = new ArrayList<String>();
                if (notFoundedJobChainJobs.containsKey(jobChain.getName())) {
                    al = notFoundedJobChainJobs.get(jobChain.getName());
                }
                al.add(String.format("state = %s, job = %s, job path = %s", state, job, fileJobPath));
                notFoundedJobChainJobs.put(jobChain.getName(), al);
                countNotFoundedJobChainJobs++;
            }
        }
        DBItemInventoryJobChainNode nodeItem = new DBItemInventoryJobChainNode();
        nodeItem.setJobChainId(jobChain.getId());
        nodeItem.setJobName(jobName);
        nodeItem.setName(nodeName);
        nodeItem.setState(state);
        nodeItem.setNextState(nextState);
        nodeItem.setErrorState(errorState);
        nodeItem.setCreated(ReportUtil.getCurrentDateTime());
        nodeItem.setModified(ReportUtil.getCurrentDateTime());
        nodeItem.setNestedJobChainId(DBLayer.DEFAULT_ID);
        nodeItem.setNestedJobChainName(DBLayer.DEFAULT_NAME);
        /** new Items since 1.11 */
        nodeItem.setJob(job);
        DBItemInventoryJob jobDbItem = SaveOrUpdateHelper.getJobIfExists(getDbLayer(), jobChain.getInstanceId(), job, jobName);
        if(jobDbItem != null) {
            nodeItem.setJobId(jobDbItem.getId());
        } else {
            nodeItem.setJobId(DBLayer.DEFAULT_ID);
        }
        nodeItem.setNodeType(getJobChainNodeType(nodeName, jobChainNodeElement));
        switch (nodeItem.getNodeType()) {
        case 1:
            if(jobChainNodeElement.hasAttribute("delay")) {
                String delay = jobChainNodeElement.getAttribute("delay");
                if(delay != null && !delay.isEmpty()) {
                    nodeItem.setDelay(Integer.parseInt(delay));
                }
            }
            if(jobChainNodeElement.hasAttribute("on_error")) {
                nodeItem.setOnError(jobChainNodeElement.getAttribute("on_error"));
            }
            break;
        case 2:
            if (jobChainNodeElement.hasAttribute("job_chain")) {
                String jobchain = jobChainNodeElement.getAttribute("job_chain");
                String jobchainName = SaveOrUpdateHelper.getJobChainName(getDbLayer(), inventoryInstance.getId(), jobchain);
                DBItemInventoryJobChain ijc = SaveOrUpdateHelper.getJobChainIfExists(getDbLayer(), jobChain.getInstanceId(), 
                        jobchain, jobchainName);
                if(ijc != null) {
                    nodeItem.setNestedJobChain(ijc.getBaseName());
                    nodeItem.setNestedJobChainName(ijc.getName());
                    nodeItem.setNestedJobChainId(ijc.getId());
                } else {
                    nodeItem.setNestedJobChain(jobchain);
                    nodeItem.setNestedJobChainName(jobchainName);
                    nodeItem.setNestedJobChainId(DBLayer.DEFAULT_ID);
                }
            } else {
                nodeItem.setNestedJobChainId(DBLayer.DEFAULT_ID);
                nodeItem.setNestedJobChainName(DBLayer.DEFAULT_NAME);
            }
            break;
        case 3:
            nodeItem.setDirectory(jobChainNodeElement.getAttribute("directory"));
            if (jobChainNodeElement.hasAttribute("regex")) {
                nodeItem.setRegex(jobChainNodeElement.getAttribute("regex"));
            }
            break;
        case 4:
            if (jobChainNodeElement.hasAttribute("move_to")) {
                nodeItem.setMovePath(jobChainNodeElement.getAttribute("move_to"));
                nodeItem.setFileSinkOp(1);
            } else {
                nodeItem.setFileSinkOp(2);
            }
            break;
        default:
            break;
        }
        /** End of new Items */
        return nodeItem;
    }

    private DBItemInventoryOrder createOrder(File file, String schedulerFilePath) throws Exception {
        DBItemInventoryFile dbItemFile = processFile(file, schedulerFilePath, EConfigFileExtensions.ORDER.type());
        String name = ReportUtil.getNameFromPath(schedulerFilePath, EConfigFileExtensions.ORDER);
        String basename = ReportUtil.getNameFromPath(file.getName(), EConfigFileExtensions.ORDER);
        String jobChainBaseName = basename.substring(0, basename.indexOf(","));
        String directory = (dbItemFile.getFileDirectory().equals(DBLayer.DEFAULT_NAME)) ? "" : dbItemFile.getFileDirectory() + "/";
        String jobChainName = directory + jobChainBaseName;
        String orderId = basename.substring(jobChainBaseName.length() + 1);
        SOSXMLXPath xpath = new SOSXMLXPath(file.getCanonicalPath());
        if (xpath.getRoot() == null) {
            throw new Exception(String.format("xpath root missing"));
        }
        String title = ReportXmlHelper.getTitle(xpath);
        boolean isRuntimeDefined = ReportXmlHelper.isRuntimeDefined(xpath);
        DBItemInventoryOrder item = new DBItemInventoryOrder();
        item.setInstanceId(dbItemFile.getInstanceId());
        item.setFileId(dbItemFile.getId());
        item.setJobChainName(jobChainName);
        item.setName(name);
        item.setBaseName(basename);
        item.setOrderId(orderId);
        item.setTitle(title);
        item.setIsRuntimeDefined(isRuntimeDefined);
        item.setCreated(ReportUtil.getCurrentDateTime());
        item.setModified(ReportUtil.getCurrentDateTime());
        /** new Items since 1.11 */
        item.setJobChainId(DBLayer.DEFAULT_ID);
        if(xpath.getRoot().hasAttribute("state")) {
            item.setInitialState(xpath.getRoot().getAttribute("state"));
        }
        if(xpath.getRoot().hasAttribute("end_state")) {
            item.setEndState(xpath.getRoot().getAttribute("end_state"));
        }
        if(xpath.getRoot().hasAttribute("priority")) {
            String priority = xpath.getRoot().getAttribute("priority");
            if(priority != null && !priority.isEmpty()) {
                item.setPriority(Integer.parseInt(priority));
            }
        }
        String schedule = ReportXmlHelper.getScheduleFromRuntime(xpath);
        if(schedule != null && !schedule.isEmpty()) {
          String scheduleName = SaveOrUpdateHelper.getScheduleName(getDbLayer(), inventoryInstance.getId(), schedule);
          DBItemInventorySchedule is = SaveOrUpdateHelper.getScheduleIfExists(getDbLayer(), item.getInstanceId(), schedule, scheduleName);
          if(is != null) {
              item.setSchedule(is.getBasename());
              item.setScheduleName(is.getName());
              item.setScheduleId(is.getId());
          } else {
              item.setSchedule(schedule);
              item.setScheduleName(scheduleName);
              item.setScheduleId(DBLayer.DEFAULT_ID);
          }
        } else {
            item.setScheduleId(DBLayer.DEFAULT_ID);
            item.setScheduleName(DBLayer.DEFAULT_NAME);
        }
       item.setSchedule(ReportXmlHelper.getScheduleFromRuntime(xpath));
        /** End of new Items since 1.11 */
        return item; 
    }

    private DBItemInventoryLock createInventoryLock(File file, String schedulerFilePath) throws Exception {
        DBItemInventoryFile dbItemFile = processFile(file, schedulerFilePath, EConfigFileExtensions.LOCK.type());
        String name = ReportUtil.getNameFromPath(schedulerFilePath, EConfigFileExtensions.LOCK);
        String basename = ReportUtil.getNameFromPath(file.getName(), EConfigFileExtensions.LOCK);
        SOSXMLXPath xpath = new SOSXMLXPath(file.getCanonicalPath());
        if (xpath.getRoot() == null) {
            throw new Exception(String.format("xpath root missing"));
        }
        DBItemInventoryLock item = new DBItemInventoryLock();
        item.setInstanceId(dbItemFile.getInstanceId());
        item.setFileId(dbItemFile.getId());
        item.setMaxNonExclusive(ReportXmlHelper.getMaxNonExclusive(xpath));
        item.setName(name);
        item.setBasename(basename);
        return item; 
    }

    private void processLock(File file, String schedulerFilePath) throws Exception {
        countTotalLocks++;
        try {
            DBItemInventoryLock item = createInventoryLock(file, schedulerFilePath);
            SaveOrUpdateHelper.saveOrUpdateLock(getDbLayer(), item);
            countSuccessLocks++;
        } catch (Exception ex) {
            getDbLayer().getConnection().rollback();
            LOGGER.warn(String.format("processLock: lock file cannot be inserted = %s, exception = %s ", file.getCanonicalPath(),
                    ex.toString()), ex);
            errorLocks.put(file.getCanonicalPath(), ex.toString());
        }
    }

    private DBItemInventoryProcessClass createInventoryProcessClass(File file, String schedulerFilePath) throws Exception {
        DBItemInventoryFile dbItemFile = processFile(file, schedulerFilePath, EConfigFileExtensions.PROCESS_CLASS.type());
        String name = ReportUtil.getNameFromPath(schedulerFilePath, EConfigFileExtensions.PROCESS_CLASS);
        String basename = ReportUtil.getNameFromPath(file.getName(), EConfigFileExtensions.PROCESS_CLASS);
        SOSXMLXPath xpath = new SOSXMLXPath(file.getCanonicalPath());
        if (xpath.getRoot() == null) {
            throw new Exception(String.format("xpath root missing"));
        }
        DBItemInventoryProcessClass item = new DBItemInventoryProcessClass();
        item.setInstanceId(dbItemFile.getInstanceId());
        item.setFileId(dbItemFile.getId());
        item.setName(name);
        item.setBasename(basename);
        item.setMaxProcesses(ReportXmlHelper.getMaxProcesses(xpath));
        item.setHasAgents(ReportXmlHelper.hasAgents(xpath));
        return item; 
    }
    
    private void processProcessClass(File file, String schedulerFilePath) throws Exception {
        SOSXMLXPath xpath = new SOSXMLXPath(file.getCanonicalPath());
        countTotalProcessClasses++;
        try {
            DBItemInventoryProcessClass item = createInventoryProcessClass(file, schedulerFilePath);
            Long id = SaveOrUpdateHelper.saveOrUpdateProcessClass(getDbLayer(), item);
            if(item.getId() == null) {
                item.setId(id);
            }
            countSuccessProcessClasses++;
            if (item.getHasAgents()) {
                Map<String,Integer> remoteSchedulerUrls = ReportXmlHelper.getRemoteSchedulersFromProcessClass(xpath);
                if(remoteSchedulerUrls != null && !remoteSchedulerUrls.isEmpty()) {
                    String schedulingType = ReportXmlHelper.getSchedulingType(xpath);
                    if(schedulingType != null && !schedulingType.isEmpty()) {
                        processAgentCluster(remoteSchedulerUrls, schedulingType, item.getInstanceId(), item.getId());
                    } else if (remoteSchedulerUrls.size() == 1) {
                        processAgentCluster(remoteSchedulerUrls, "single", item.getInstanceId(), item.getId());
                    } else {
                        processAgentCluster(remoteSchedulerUrls, "first", item.getInstanceId(), item.getId());
                    }
                }
            }
        } catch (Exception ex) {
            getDbLayer().getConnection().rollback();
            LOGGER.warn(String.format("processProcessClass: processClass file cannot be inserted = %s, exception = %s ", file.getCanonicalPath(),
                    ex.toString()), ex);
            errorProcessClasses.put(file.getCanonicalPath(), ex.toString());
        }
    }

    private void processAgentCluster(Map<String,Integer> remoteSchedulers, String schedulingType, Long instanceId, Long processClassId)
            throws Exception {
        Integer numberOfAgents = remoteSchedulers.size();
        DBItemInventoryAgentCluster agentCluster = new DBItemInventoryAgentCluster();
        agentCluster.setInstanceId(instanceId);
        agentCluster.setProcessClassId(processClassId);
        agentCluster.setNumberOfAgents(numberOfAgents);
        agentCluster.setSchedulingType(schedulingType);
        Long clusterId = SaveOrUpdateHelper.saveOrUpdateAgentCluster(getDbLayer(), agentCluster);
        for(String agentUrl : remoteSchedulers.keySet()) {
            DBItemInventoryAgentInstance agent = getInventoryAgentInstanceFromDb(agentUrl, instanceId);
            Integer ordering = remoteSchedulers.get(agent.getUrl());
            DBItemInventoryAgentClusterMember agentClusterMember = new DBItemInventoryAgentClusterMember();
            agentClusterMember.setInstanceId(instanceId);
            agentClusterMember.setAgentClusterId(clusterId);
            agentClusterMember.setAgentInstanceId(agent.getId());
            agentClusterMember.setUrl(agent.getUrl());
            agentClusterMember.setOrdering(ordering);
            Long clusterMemberId = SaveOrUpdateHelper.saveOrUpdateAgentClusterMember(getDbLayer(), agentClusterMember);
        }
    }
    
    @SuppressWarnings("unchecked")
    private DBItemInventoryAgentInstance getInventoryAgentInstanceFromDb (String url, Long instanceId) throws Exception {
        getDbLayer().getConnection().beginTransaction();
        StringBuilder sql = new StringBuilder();
        sql.append("from ");
        sql.append(DBLayer.DBITEM_INVENTORY_AGENT_INSTANCES);
        sql.append(" where instanceId = :instanceId");
        sql.append(" and url = :url");
        Query query = getDbLayer().getConnection().createQuery(sql.toString());
        query.setParameter("instanceId", instanceId);
        query.setParameter("url", url);
        List<DBItemInventoryAgentInstance> result = query.list();
        if (result != null && !result.isEmpty()) {
            return result.get(0);
        } else {
            return null;
        }
    }

    private void processSchedule(File file, String schedulerFilePath) throws Exception {
        countTotalSchedules++;
        try {
            DBItemInventorySchedule item = createInventorySchedule(file, schedulerFilePath);
            SaveOrUpdateHelper.saveOrUpdateSchedule(getDbLayer(), item);
            countSuccessSchedules++;
        } catch (Exception ex) {
            getDbLayer().getConnection().rollback();
            LOGGER.warn(String.format("processSchedule: schedule file cannot be inserted = %s, exception = %s ", file.getCanonicalPath(),
                    ex.toString()), ex);
            errorSchedules.put(file.getCanonicalPath(), ex.toString());
        }
    }

    private DBItemInventorySchedule createInventorySchedule(File file, String schedulerFilePath) throws Exception {
        DBItemInventoryFile dbItemFile = processFile(file, schedulerFilePath, EConfigFileExtensions.SCHEDULE.type());
        String name = ReportUtil.getNameFromPath(schedulerFilePath, EConfigFileExtensions.SCHEDULE);
        String basename = ReportUtil.getNameFromPath(file.getName(), EConfigFileExtensions.SCHEDULE);
        SOSXMLXPath xpath = new SOSXMLXPath(file.getCanonicalPath());
        if (xpath.getRoot() == null) {
            throw new Exception(String.format("xpath root missing"));
        }
        DBItemInventorySchedule item = new DBItemInventorySchedule();
        item.setInstanceId(dbItemFile.getInstanceId());
        item.setFileId(dbItemFile.getId());
        item.setName(name);
        item.setBasename(basename);
        item.setTitle(ReportXmlHelper.getTitle(xpath));
        item.setSubstitute(ReportXmlHelper.getSubstitute(xpath));
        String timezone = inventoryInstance.getTimeZone();
        item.setSubstituteValidFrom(ReportXmlHelper.getSubstituteValidFromTo(xpath, "valid_from", timezone));
        item.setSubstituteValidTo(ReportXmlHelper.getSubstituteValidFromTo(xpath, "valid_to", timezone));
        DBItemInventorySchedule substituteItem = getSubstituteIfExists(item.getSubstitute(), item.getInstanceId());
        if(substituteItem != null) {
            item.setSubstituteId(substituteItem.getId());
            item.setSubstituteName(substituteItem.getName());
        } else {
            item.setSubstituteId(DBLayer.DEFAULT_ID);
            item.setSubstituteName(DBLayer.DEFAULT_NAME);
        }
        return item; 
    }

    @SuppressWarnings("unchecked")
    private DBItemInventorySchedule getSubstituteIfExists(String substitute, Long instanceId) throws Exception {
        getDbLayer().getConnection().beginTransaction();
        StringBuilder sql = new StringBuilder();
        sql.append("from ");
        sql.append(DBLayer.DBITEM_INVENTORY_SCHEDULES);
        sql.append(" where instanceId = :instanceId");
        sql.append(" and basename = :basename");
        Query query = getDbLayer().getConnection().createQuery(sql.toString());
        query.setParameter("instanceId", instanceId);
        query.setParameter("basename", substitute);
        List<DBItemInventorySchedule> result = query.list();
        if(result != null && !result.isEmpty()) {
            return result.get(0);
        } else {
            return null;
        }
    }
    
    @SuppressWarnings("unchecked")
    private void cleanUpInventoryAfter(Instant started) throws Exception {
        List<DBItemInventoryJob> oldJobs = getItemsFromDb(started, DBLayer.DBITEM_INVENTORY_JOBS);
        for (DBItemInventoryJob oldJob : oldJobs) {
            Instant modifiedLocal = Instant.ofEpochMilli(oldJob.getModified().getTime()).plusSeconds(7200);
            if (modifiedLocal.compareTo(started) < 0) {
                getDbLayer().getConnection().delete(oldJob);
            }
        }
        List<DBItemInventoryJobChain> oldJobchains = getItemsFromDb(started, DBLayer.DBITEM_INVENTORY_JOB_CHAINS);
        for (DBItemInventoryJobChain oldJobChain : oldJobchains) {
            Instant modifiedLocal = Instant.ofEpochMilli(oldJobChain.getModified().getTime()).plusSeconds(7200);
            if (modifiedLocal.compareTo(started) < 0) {
                getDbLayer().getConnection().delete(oldJobChain);
            }
        }
        List<DBItemInventoryJobChainNode> oldJobchainNodes = getItemsFromDb(started, DBLayer.DBITEM_INVENTORY_JOB_CHAIN_NODES);
        for (DBItemInventoryJobChainNode oldJobChainNode : oldJobchainNodes) {
            Instant modifiedLocal = Instant.ofEpochMilli(oldJobChainNode.getModified().getTime()).plusSeconds(7200);
            if (modifiedLocal.compareTo(started) < 0) {
                getDbLayer().getConnection().delete(oldJobChainNode);
            }
        }
        List<DBItemInventoryOrder> oldOrders = getItemsFromDb(started, DBLayer.DBITEM_INVENTORY_ORDERS);
        for (DBItemInventoryOrder oldOrder : oldOrders) {
            Instant modifiedLocal = Instant.ofEpochMilli(oldOrder.getModified().getTime()).plusSeconds(7200);
            if (modifiedLocal.compareTo(started) < 0) {
                getDbLayer().getConnection().delete(oldOrder);
            }
        }
        List<DBItemInventoryLock> oldLocks = getItemsFromDb(started, DBLayer.DBITEM_INVENTORY_LOCKS);
        for (DBItemInventoryLock oldLock : oldLocks) {
            Instant modifiedLocal = Instant.ofEpochMilli(oldLock.getModified().getTime()).plusSeconds(7200);
            if (modifiedLocal.compareTo(started) < 0) {
                getDbLayer().getConnection().delete(oldLock);
            }
        }
        List<DBItemInventoryAppliedLock> oldAppliedLocks = getAppliedLocksFromDb(started);
        for (DBItemInventoryAppliedLock oldAppliedLock : oldAppliedLocks) {
            Instant modifiedLocal = Instant.ofEpochMilli(oldAppliedLock.getModified().getTime()).plusSeconds(7200);
            if (modifiedLocal.compareTo(started) < 0) {
                getDbLayer().getConnection().delete(oldAppliedLock);
            }
        }
        List<DBItemInventorySchedule> oldSchedules = getItemsFromDb(started, DBLayer.DBITEM_INVENTORY_SCHEDULES);
        for (DBItemInventorySchedule oldSchedule : oldSchedules) {
            Instant modifiedLocal = Instant.ofEpochMilli(oldSchedule.getModified().getTime()).plusSeconds(7200);
            if (modifiedLocal.compareTo(started) < 0) {
                getDbLayer().getConnection().delete(oldSchedule);
            }
        }
        List<DBItemInventoryProcessClass> oldProcessClasses = getItemsFromDb(started, DBLayer.DBITEM_INVENTORY_PROCESS_CLASSES);
        for (DBItemInventoryProcessClass oldProcessClass : oldProcessClasses) {
            Instant modifiedLocal = Instant.ofEpochMilli(oldProcessClass.getModified().getTime()).plusSeconds(7200);
            if (modifiedLocal.compareTo(started) < 0) {
                getDbLayer().getConnection().delete(oldProcessClass);
            }
        }
        List<DBItemInventoryAgentCluster> oldAgentClusters = getItemsFromDb(started, DBLayer.DBITEM_INVENTORY_AGENT_CLUSTER);
        for (DBItemInventoryAgentCluster oldAgentCluster : oldAgentClusters) {
            Instant modifiedLocal = Instant.ofEpochMilli(oldAgentCluster.getModified().getTime()).plusSeconds(7200);
            if (modifiedLocal.compareTo(started) < 0) {
                getDbLayer().getConnection().delete(oldAgentCluster);
            }
        }
        List<DBItemInventoryAgentClusterMember> oldAgentClusterMembers = getItemsFromDb(started, DBLayer.DBITEM_INVENTORY_AGENT_CLUSTERMEMBERS);
        for (DBItemInventoryAgentClusterMember oldAgentClusterMember : oldAgentClusterMembers) {
            Instant modifiedLocal = Instant.ofEpochMilli(oldAgentClusterMember.getModified().getTime()).plusSeconds(7200);
            if (modifiedLocal.compareTo(started) < 0) {
                getDbLayer().getConnection().delete(oldAgentClusterMember);
            }
        }
    }
    
    @SuppressWarnings("rawtypes")
    private List getItemsFromDb(Instant started, String tableName) throws Exception {
        StringBuilder sql = new StringBuilder();
        sql.append("from ");
        sql.append(tableName);
        sql.append(" where instanceId = :instanceId");
        sql.append(" and modified < :modified");
        Query query = getDbLayer().getConnection().createQuery(sql.toString());
        query.setParameter("instanceId", inventoryInstance.getId());
        query.setParameter("modified", Date.from(started));
        return query.list();
    }
    
    @SuppressWarnings("rawtypes")
    private List getAppliedLocksFromDb(Instant started) throws Exception {
        StringBuilder sql = new StringBuilder();
        sql.append("from ");
        sql.append(DBLayer.DBITEM_INVENTORY_APPLIED_LOCKS);
        sql.append(" where modified < :modified");
        Query query = getDbLayer().getConnection().createQuery(sql.toString());
        query.setParameter("modified", Date.from(started));
        return query.list();
    }
    
    @SuppressWarnings("unchecked")
    private DBItemInventoryLock getLockByName(String name) throws Exception {
        StringBuilder sql = new StringBuilder();
        sql.append("from ");
        sql.append(DBLayer.DBITEM_INVENTORY_LOCKS);
        sql.append(" where basename = :basename");
        Query query = getDbLayer().getConnection().createQuery(sql.toString());
        query.setParameter("basename", name);
        List<DBItemInventoryLock> result = query.list();
        if(result != null && !result.isEmpty()) {
            return result.get(0);
        }
        return null;
    }
    
}