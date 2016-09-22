package com.sos.jitl.reporting.model.inventory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import sos.util.SOSString;
import sos.xml.SOSXMLXPath;

import com.sos.hibernate.classes.SOSHibernateConnection;
import com.sos.jitl.reporting.db.DBItemInventoryFile;
import com.sos.jitl.reporting.db.DBItemInventoryInstance;
import com.sos.jitl.reporting.db.DBItemInventoryJobChainNode;
import com.sos.jitl.reporting.db.DBItemInventoryOrder;
import com.sos.jitl.reporting.db.DBItemInventoryJobChain;
import com.sos.jitl.reporting.db.DBItemInventoryJob;
import com.sos.jitl.reporting.db.DBLayer;
import com.sos.jitl.reporting.helper.EConfigFileExtensions;
import com.sos.jitl.reporting.helper.ReportUtil;
import com.sos.jitl.reporting.helper.ReportXmlHelper;
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
    private LinkedHashMap<String, ArrayList<String>> notFoundedJobChainJobs;
    private LinkedHashMap<String, String> errorJobChains;
    private LinkedHashMap<String, String> errorOrders;
    private LinkedHashMap<String, String> errorJobs;

    public InventoryModel(SOSHibernateConnection reportingConn, InventoryJobOptions opt) throws Exception {
        super(reportingConn);
        options = opt;
    }

    @Override
    public void process() throws Exception {
        String method = "process";
        try {
            initCounters();
            initInventoryInstance();
            processConfigurationDirectory(options.current_scheduler_configuration_directory.getValue());
            logSummary();
            resume();
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
        cleanupInventory();
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
        notFoundedJobChainJobs = new LinkedHashMap<String, ArrayList<String>>();
        errorJobChains = new LinkedHashMap<String, String>();
        errorOrders = new LinkedHashMap<String, String>();
        errorJobs = new LinkedHashMap<String, String>();
    }

    private void logSummary() {
        String method = "logSummary";
        LOGGER.info(String.format("%s: inserted job chains = %s (total %s, error = %s)", method, countSuccessJobChains, countTotalJobChains,
                errorJobChains.size()));
        LOGGER.info(String.format("%s: inserted orders = %s (total %s, error = %s)", method, countSuccessOrders, countTotalOrders, errorOrders.size()));
        LOGGER.info(String.format("%s: inserted jobs = %s (total %s, error = %s)", method, countSuccessJobs, countTotalJobs, errorJobs.size()));
        if (!errorJobChains.isEmpty()) {
            LOGGER.info(String.format("%s:   errors by inserting job chains:", method));
            int i = 1;
            for (Entry<String, String> entry : errorJobChains.entrySet()) {
                LOGGER.info(String.format("%s:     %s) %s: %s", method, i, entry.getKey(), entry.getValue()));
                i++;
            }
        }
        if (!errorOrders.isEmpty()) {
            LOGGER.info(String.format("%s:   errors by inserting orders:", method));
            int i = 1;
            for (Entry<String, String> entry : errorOrders.entrySet()) {
                LOGGER.info(String.format("%s:     %s) %s: %s", method, i, entry.getKey(), entry.getValue()));
                i++;
            }
        }
        if (!errorJobs.isEmpty()) {
            LOGGER.info(String.format("%s:   errors by inserting jobs:", method));
            int i = 1;
            for (Entry<String, String> entry : errorJobs.entrySet()) {
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
        if (countSuccessJobChains == 0 && countSuccessOrders == 0 && countSuccessJobs == 0) {
            throw new Exception(String.format("error occured: 0 job chains, orders or jobs inserted"));
        }
        if (!errorJobChains.isEmpty() || !errorOrders.isEmpty() || !errorJobs.isEmpty()) {
            LOGGER.warn(String.format("error occured: insert failed by %s job chains, %s orders ,%s jobs", errorJobChains.size(), errorOrders.size(),
                    errorJobs.size()));
        }
    }

    private void processConfigurationDirectory(String directory) throws Exception {
        String method = "processConfigurationDirectory";
        File dir = new File(directory);
        if (!dir.exists()) {
            throw new Exception(String.format("%s: configuration directory not found. directory = %s", method, dir.getCanonicalPath()));
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
                        if (fLower.endsWith(EConfigFileExtensions.JOB.extension())) {
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
            getDbLayer().getConnection().beginTransaction();
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
            /** new Items since 1.11 */
//            Integer usedInJobChains = 0;
//            String processClass = null;
//            String processClassName = DBLayer.DEFAULT_NAME;
//            Long processClassId = DBLayer.DEFAULT_ID;
//            if (xpath.getRoot().hasAttribute("process_class")) {
//                processClass = ReportXmlHelper.getProcessClass(xpath);
//                processClassName = schedulerFilePath + "/" + processClass;
//            }
//            String schedule = ReportXmlHelper.getSchedule(xpath);
//            String scheduleName = DBLayer.DEFAULT_NAME;
//            Long scheduleId = DBLayer.DEFAULT_ID;
//            if(schedule != null) {
//                scheduleName = schedulerFilePath + "/" + schedule;
//            }
//            Integer maxTasks = Integer.parseInt(xpath.getRoot().getAttribute("tasks"));
//            boolean hasDescription = ReportXmlHelper.hasDescription(xpath);
            /** End of new Items */
            DBItemInventoryJob item =
                    getDbLayer().createInventoryJob(dbItemFile.getInstanceId(), dbItemFile.getId(), name, basename, title, isOrderJob,
                            isRuntimeDefined/*, usedInJobChains, processClass, processClassName, processClassId, schedule, scheduleName,
                            scheduleId, maxTasks, hasDescription*/);
            LOGGER.debug(String.format("%s: job     id = %s, jobName = %s, jobBasename = %s, title = %s, isOrderJob = %s, isRuntimeDefined = %s",
                    method, item.getId(), item.getName(), item.getBaseName(), item.getTitle(), item.getIsOrderJob(), item.getIsRuntimeDefined()));
            getDbLayer().getConnection().commit();
            countSuccessJobs++;
        } catch (Exception ex) {
            getDbLayer().getConnection().rollback();
            LOGGER.warn(String.format("%s: job file cannot be inserted = %s, exception = %s ", method, file.getCanonicalPath(), ex.toString()), ex);
            errorJobs.put(file.getCanonicalPath(), ex.toString());
        }

    }

    private void processJobChain(File file, String schedulerFilePath, int rootPathLen) throws Exception {
        String method = "    processJobChain";
        countTotalJobChains++;
        try {
            getDbLayer().getConnection().beginTransaction();
            DBItemInventoryFile dbItemFile = processFile(file, schedulerFilePath, EConfigFileExtensions.JOB_CHAIN.type());
            String name = ReportUtil.getNameFromPath(schedulerFilePath, EConfigFileExtensions.JOB_CHAIN);
            String basename = ReportUtil.getNameFromPath(file.getName(), EConfigFileExtensions.JOB_CHAIN);
            SOSXMLXPath xpath = new SOSXMLXPath(file.getCanonicalPath());
            if (xpath.getRoot() == null) {
                throw new Exception(String.format("xpath root missing"));
            }
            String title = ReportXmlHelper.getTitle(xpath);
            String startCause = ReportXmlHelper.getJobChainStartCause(xpath);
            /** new Items since 1.11 */
//            Integer maxOrders = Integer.parseInt(xpath.getRoot().getAttribute("max_orders"));
//            boolean distributed = "yes".equalsIgnoreCase(xpath.getRoot().getAttribute("distributed"));
//            String processClass = null;
//            String processClassName = DBLayer.DEFAULT_NAME;
//            Long processClassId = DBLayer.DEFAULT_ID;
//            if (xpath.getRoot().hasAttribute("process_class")) {
//                processClass = ReportXmlHelper.getProcessClass(xpath);
//                processClassName = schedulerFilePath + "/" + processClass;
//            }
//            String fileWatchingProcessClass = null;
//            String fileWatchingProcessClassName = DBLayer.DEFAULT_NAME;
//            Long fileWatchingProcessClassId = DBLayer.DEFAULT_ID;
//            if (xpath.getRoot().hasAttribute("file_watching_process_class")) {
//                fileWatchingProcessClass = ReportXmlHelper.getFileWatchingProcessClass(xpath);
//                fileWatchingProcessClassName = schedulerFilePath + "/" + fileWatchingProcessClass;
//            }
            /** End of new Items */
            DBItemInventoryJobChain item =
                    getDbLayer().createInventoryJobChain(dbItemFile.getInstanceId(), dbItemFile.getId(), startCause, name, basename, title/*,
                            maxOrders, distributed, processClass, processClassName, processClassId, fileWatchingProcessClass, fileWatchingProcessClassName,
                            fileWatchingProcessClassId*/);
            LOGGER.debug(String.format("%s: jobChain    id = %s, startCause = %s, jobChainName = %s, jobChainBasename = %s, title = %s", method,
                    item.getId(), item.getStartCause(), item.getName(), item.getBaseName(), item.getTitle()));
            NodeList nl = ReportXmlHelper.getRootChilds(xpath);
            int ordering = 1;
            for (int j = 0; j < nl.getLength(); ++j) {
                Element jobChainNodeElement = (Element) nl.item(j);
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
                        LOGGER.warn(String.format("%s: job = %s (job chain = %s) not found on the disc = %s ", method, job, item.getName(),
                                fileJob.getCanonicalPath()));
                        ArrayList<String> al = new ArrayList<String>();
                        if (notFoundedJobChainJobs.containsKey(item.getName())) {
                            al = notFoundedJobChainJobs.get(item.getName());
                        }
                        al.add(String.format("state = %s, job = %s, job path = %s", state, job, fileJobPath));
                        notFoundedJobChainJobs.put(item.getName(), al);
                        countNotFoundedJobChainJobs++;
                    }
                }
                /** new Items since 1.11 */
//                Long jobId = DBLayer.DEFAULT_ID;
//                String nestedJobChain = null;
//                String nestedJobChainName = DBLayer.DEFAULT_NAME;
//                Long nestedJobChainId = DBLayer.DEFAULT_ID;
//                Integer nodeType = getJobChainNodeType(nodeName, jobChainNodeElement);
//                String onError = null;
//                Integer delay = null;
//                String directory = null;
//                String regex = null;
//                Integer fileSinkOp = null;
//                String movePath = null;
//                switch (nodeType) {
//                case 1:
//                    if(jobChainNodeElement.hasAttribute("delay")) {
//                        delay = Integer.parseInt(jobChainNodeElement.getAttribute("delay"));
//                    }
//                    if(jobChainNodeElement.hasAttribute("on_error")) {
//                        onError = jobChainNodeElement.getAttribute("on_error");
//                    }
//                    break;
//                case 2:
//                    nestedJobChain = jobChainNodeElement.getAttribute("job_chain");
//                    nestedJobChainName = schedulerFilePath + "/" + nestedJobChain;
//                    break;
//                case 3:
//                    directory = jobChainNodeElement.getAttribute("directory");
//                    if (jobChainNodeElement.hasAttribute("regex")) {
//                        regex = jobChainNodeElement.getAttribute("regex");
//                    }
//                    break;
//                case 4:
//                    if (jobChainNodeElement.hasAttribute("move_to")) {
//                        movePath = jobChainNodeElement.getAttribute("move_to");
//                    }
//                    break;
//                default:
//                    break;
//                }
                /** End of new Items */
                DBItemInventoryJobChainNode itemNode =
                        getDbLayer().createInventoryJobChainNode(dbItemFile.getInstanceId(), item.getId(), jobName, new Long(ordering), nodeName/*,
                                state, nextState, errorState, job, jobId, nestedJobChain, nestedJobChainName, nestedJobChainId, nodeType, onError,
                                delay, directory, regex, fileSinkOp, movePath*/);
                ordering++;
                LOGGER.debug(String.format(
                        "%s: jobChainNode     id = %s, nodeName = %s, ordering = %s, state = %s, nextState = %s, errorState = %s, job = %s, "
                                + "jobName = %s", method, itemNode.getId(), itemNode.getName(), itemNode.getOrdering(), itemNode.getState(),
                        itemNode.getNextState(), itemNode.getErrorState(), itemNode.getJob(), itemNode.getJobName()));
            }
            getDbLayer().getConnection().commit();
            countSuccessJobChains++;
        } catch (Exception ex) {
            getDbLayer().getConnection().rollback();
            LOGGER.warn(String.format("%s: job chain file cannot be inserted = %s , exception = %s", method, file.getCanonicalPath(), ex.toString()),
                    ex);
            errorJobChains.put(file.getCanonicalPath(), ex.toString());
        }
    }

    private void processOrder(File file, String schedulerFilePath) throws Exception {
        String method = "    processOrder";
        countTotalOrders++;
        try {
            getDbLayer().getConnection().beginTransaction();
            DBItemInventoryFile dbItemFile = processFile(file, schedulerFilePath, EConfigFileExtensions.ORDER.type());
            String name = ReportUtil.getNameFromPath(schedulerFilePath, EConfigFileExtensions.ORDER);
            String basename = ReportUtil.getNameFromPath(file.getName(), EConfigFileExtensions.ORDER);
            String jobChainBaseName = basename.substring(0, basename.indexOf(","));
            String directory =
                    (dbItemFile.getFileDirectory().equals(DBLayer.DEFAULT_NAME)) ? "" : dbItemFile.getFileDirectory() + "/";
            String jobChainName = directory + jobChainBaseName;
            String orderId = basename.substring(jobChainBaseName.length() + 1);
            SOSXMLXPath xpath = new SOSXMLXPath(file.getCanonicalPath());
            if (xpath.getRoot() == null) {
                throw new Exception(String.format("xpath root missing"));
            }
            String title = ReportXmlHelper.getTitle(xpath);
            boolean isRuntimeDefined = ReportXmlHelper.isRuntimeDefined(xpath);
            /** new Items since 1.11 */
//            Long jobChainId = DBLayer.DEFAULT_ID;
//            String initialState = null;
//            if(xpath.getRoot().hasAttribute("state")) {
//                initialState = xpath.getRoot().getAttribute("state");
//            }
//            String endState = null;
//            if(xpath.getRoot().hasAttribute("end_state")) {
//                endState = xpath.getRoot().getAttribute("end_state");
//            }
//            Integer priority = null;
//            if(xpath.getRoot().hasAttribute("priority")) {
//                priority = Integer.parseInt(xpath.getRoot().getAttribute("priority"));
//            }
//            String schedule = ReportXmlHelper.getSchedule(xpath);
//            String scheduleName = DBLayer.DEFAULT_NAME;
//            if(schedule != null) {
//                scheduleName = schedulerFilePath + "/" + schedule;
//            }
//            Long scheduleId = DBLayer.DEFAULT_ID;
            /** End of new Items since 1.11 */
            DBItemInventoryOrder item =
                    getDbLayer().createInventoryOrder(dbItemFile.getInstanceId(), dbItemFile.getId(), jobChainName, name, basename, orderId, title,
                            isRuntimeDefined/*, jobChainId, initialState, endState, priority, schedule, scheduleName, scheduleId*/);
            LOGGER.debug(String.format("%s: order     id = %s, jobChainName = %s, orderId = %s, title = %s, isRuntimeDefined = %s", method,
                    item.getId(), item.getJobChainName(), item.getOrderId(), item.getTitle(), item.getIsRuntimeDefined()));
            getDbLayer().getConnection().commit();
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
        DBItemInventoryFile item =
                getDbLayer().createInventoryFile(inventoryInstance.getId(), fileType, fileName, fileBasename, fileDirectory, fileCreated,
                        fileModified, fileLocalCreated, fileLocalModified);
        LOGGER.debug(String.format(
                "%s: file     id = %s, fileType = %s, fileName = %s, fileBasename = %s, fileDirectory = %s, fileCreated = %s, fileModified = %s",
                method, item.getId(), item.getFileType(), item.getFileName(), item.getFileBaseName(), item.getFileDirectory(), item.getFileCreated(),
                item.getFileModified()));
        return item;
    }

    private void setInventoryInstance() throws Exception {
        String method = "setInventoryInstance";
        DBItemInventoryInstance ii =
                getDbLayer().getInventoryInstance(options.current_scheduler_id.getValue(), options.current_scheduler_hostname.getValue(),
                        new Long(options.current_scheduler_port.value()));
        String liveDirectory = ReportUtil.normalizePath(options.current_scheduler_configuration_directory.getValue());
        if (ii == null) {
            LOGGER.debug(String.format("%s: create new instance. schedulerId = %s, hostname = %s, port = %s, configuration directory = %s", method,
                    options.current_scheduler_id.getValue(), options.current_scheduler_hostname.getValue(),
                    new Long(options.current_scheduler_port.value()), liveDirectory));
            /** new Items since 1.11 */
//            Long osId = DBLayer.DEFAULT_ID;
//            String version = "1.11.0-DEFAULT";
//            String commandUrl = "http://" + options.current_scheduler_hostname.getValue() + ":" + options.current_scheduler_port.getValue();
//            String url = "http://" + options.current_scheduler_hostname.getValue() + ":" + options.current_scheduler_port.getValue();
//            String timezone = "EUROPE/BERLIN";
//            String clusterType = "standalone";
//            ProcessDataUtil dataUtil = new ProcessDataUtil(options.hibernate_configuration_file.getValue());
//            Integer precedence = null;
//            String dbmsName = dataUtil.getDbmsName(options.hibernate_configuration_file.getValue());
//            String dbmsVersion = null;
//            Date startedAt = new Date();
//            Long supervisorId = DBLayer.DEFAULT_ID;
            /** End of new Items since 1.11 */
            ii =
                    getDbLayer().createInventoryInstance(options.current_scheduler_id.getValue(), options.current_scheduler_hostname.getValue(),
                            options.current_scheduler_port.value(), options.current_scheduler_configuration_directory.getValue()/*, osId, version,
                            commandUrl, url, timezone, clusterType, precedence, dbmsName, dbmsVersion, startedAt, supervisorId*/);
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
    
}