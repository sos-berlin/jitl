package com.sos.jitl.operations.criticalpath.model;

import java.io.Serializable;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.sos.jitl.operations.criticalpath.job.UncriticalJobNodesJobOptions;

import sos.net.SOSSchedulerCommand;
import sos.spooler.Spooler;
import sos.util.SOSString;
import sos.xml.SOSXMLXPath;

public class UncriticalJobNodesModel implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(UncriticalJobNodesModel.class);
    private UncriticalJobNodesJobOptions options;
    private EUncriticalJobNodesOperation operation;
    private String criticalPathNegativeProcessingPrefix = "-";
    private String criticalPathPositiveProcessingPrefix = "+";
    private String criticalPathProcessingMode = "-";
    private String[] includePrefixedJobChains;
    private String[] excludePrefixedJobChains;
    private int numberOfModifiedJobs = 0;
    private Spooler spooler;
    private SOSSchedulerCommand schedulerCommand;

    public UncriticalJobNodesModel(UncriticalJobNodesJobOptions opt) throws Exception {
        options = opt;
        String o = options.operation.getValue().trim();
        if (o.equalsIgnoreCase(EUncriticalJobNodesOperation.SKIP.name())) {
            operation = EUncriticalJobNodesOperation.SKIP;
        } else if (o.equalsIgnoreCase(EUncriticalJobNodesOperation.UNSKIP.name())) {
            operation = EUncriticalJobNodesOperation.UNSKIP;
        } else {
            throw new Exception(String.format("unsupported operation = %s (supported operations %s)", o,
                    Arrays.toString(EUncriticalJobNodesOperation.values()).toLowerCase()));
        }
    }

    public void process() throws Exception {
        String method = "process";
        try {
            initSender();
            LOGGER.info(String.format("%s: operation = %s (scheduler %s:%s)", method, operation.name().toLowerCase(),
                    options.target_scheduler_host.getValue(), options.target_scheduler_port.value()));
            initProperties();
            if (operation.equals(EUncriticalJobNodesOperation.SKIP)) {
                execute(true);
            } else if (operation.equals(EUncriticalJobNodesOperation.UNSKIP)) {
                execute(false);
            }
            LOGGER.info(String.format("%s: numberOfModifiedJobs = %s ", method, numberOfModifiedJobs));
        } catch (Exception ex) {
            throw new Exception(String.format("%s: %s", method, ex.toString()));
        }
    }

    private void initProperties() throws Exception {
        String method = "initProperties";
        try {
            LOGGER.info(String.format("%s: using parameter \"processing_recursive\" = %s", method, options.processing_recursive.getValue()));
            if (!SOSString.isEmpty(options.processing_prefix.getValue())) {
                LOGGER.info(String.format("%s: using parameter \"processing_prefix\" = %s", method, options.processing_prefix.getValue()));
                if (options.processing_prefix.getValue().startsWith(criticalPathNegativeProcessingPrefix)) {
                    criticalPathNegativeProcessingPrefix = options.processing_prefix.getValue();
                    criticalPathProcessingMode = criticalPathNegativeProcessingPrefix.substring(0, 1);
                } else if (options.processing_prefix.getValue().startsWith(criticalPathPositiveProcessingPrefix)) {
                    criticalPathPositiveProcessingPrefix = options.processing_prefix.getValue();
                    criticalPathProcessingMode = criticalPathPositiveProcessingPrefix.substring(0, 1);
                } else {
                    throw new Exception(String.format("%s: illegal value of parameter \"processing_prefix\" = %s", method,
                            options.processing_prefix.getValue()));
                }
            }
            if (!SOSString.isEmpty(options.include_job_chains.getValue())) {
                LOGGER.info(String.format("%s: using parameter \"include_job_chains\" = %s", method, options.include_job_chains.getValue()));
                includePrefixedJobChains = options.include_job_chains.getValue().split(";");
            }
            if (!SOSString.isEmpty(options.exclude_job_chains.getValue())) {
                LOGGER.info(String.format("%s: using parameter \"exclude_job_chains\" = %s", method, options.exclude_job_chains.getValue()));
                excludePrefixedJobChains = options.exclude_job_chains.getValue().split(";");
            }
            LOGGER.info(String.format("%s: criticalPathProcessingMode = %s", method, criticalPathProcessingMode));
        } catch (Exception ex) {
            throw new Exception(String.format("%s: %s", method, ex.toString()));
        }
    }

    private SOSXMLXPath executeXml(String command) throws Exception {
        String method = "executeXml";
        LOGGER.debug(String.format("%s: command = %s", method, command));
        String response = null;
        if (schedulerCommand != null) {
            schedulerCommand.sendRequest(command);
            response = schedulerCommand.getResponse();
        } else if (spooler != null) {
            response = spooler.execute_xml(command);
        } else {
            throw new Exception(String.format("%s: schedulerCommand and spooler are NULL", method));
        }
        LOGGER.debug(String.format("%s: response = %s", method, response));
        SOSXMLXPath xpath = new SOSXMLXPath(new StringBuffer(response));
        if (xpath.getRoot() == null) {
            throw new Exception(String.format("xpath root missing"));
        }
        Element error = (Element) xpath.selectSingleNode("/" + xpath.getRoot().getNodeName() + "/answer/ERROR");
        if (error != null) {
            String errorCode = error.getAttribute("code");
            String errorText = error.getAttribute("text");
            throw new Exception(String.format("scheduler answer[%s]: %s", errorCode, errorText));
        }
        return xpath;
    }

    private void execute(boolean skip) throws Exception {
        String method = "execute";
        numberOfModifiedJobs = 0;
        String logIndent = "    ";
        String command = "<show_state what=\"job_chains\"/>";
        try {
            connect();
            SOSXMLXPath xpath = executeXml(command);
            NodeList jobChains = xpath.selectNodeList("/" + xpath.getRoot().getNodeName() + "/answer/state/job_chains/job_chain");
            LOGGER.info(String.format("%s: found %s job chains ", method, jobChains.getLength()));
            int countJobChains = 0;
            for (int i = 0; i < jobChains.getLength(); ++i) {
                countJobChains++;
                Element jobChain = (Element) jobChains.item(i);
                String state = jobChain.getAttribute("state");
                String path = jobChain.getAttribute("path");
                if (!state.equalsIgnoreCase(EJobChainState.RUNNING.name())) {
                    LOGGER.info(String.format("%s: %s) do not process job chain [%s] due to state: %s", method, countJobChains, state, path));
                    continue;
                }
                int numberOfRecursiveFolders = 1;
                boolean doSkipJobChain = false;
                if (includePrefixedJobChains != null && includePrefixedJobChains.length > 0) {
                    doSkipJobChain = true;
                    for (String prefix : includePrefixedJobChains) {
                        LOGGER.debug(String.format("%s: %s) path = %s include = %s", method, countJobChains, path, prefix));
                        if (path.indexOf(prefix) == 0) {
                            int prefixLen = prefix.split("/").length;
                            numberOfRecursiveFolders = prefix.startsWith("/") ? prefixLen - 1 : prefixLen;
                            int pathNumberOfRecursiveFolders = path.split("/").length - 1;
                            doSkipJobChain =
                                    (options.processing_recursive.value() == false && pathNumberOfRecursiveFolders > numberOfRecursiveFolders);
                            break;
                        }
                    }
                    if (doSkipJobChain) {
                        LOGGER.info(String.format("%s: %s) do not process job chain [%s] due to inclusion rule = %s", method, countJobChains, path,
                                join(includePrefixedJobChains)));
                    }
                } else {
                    int pathNumberOfRecursiveFolders = path.split("/").length - 1;
                    doSkipJobChain = (options.processing_recursive.value() == false && pathNumberOfRecursiveFolders > numberOfRecursiveFolders);
                }
                if (!doSkipJobChain && excludePrefixedJobChains != null) {
                    for (String prefix : excludePrefixedJobChains) {
                        LOGGER.debug(String.format("%s: %s) path = %s exclude = %s", method, countJobChains, path, prefix));
                        if (path.indexOf(prefix) == 0) {
                            LOGGER.info(String.format("%s: %s) do not process job chain [%s] due to exclusion rule = %s", method, countJobChains,
                                    path, prefix));
                            doSkipJobChain = true;
                        }
                    }
                }
                if (doSkipJobChain) {
                    continue;
                }
                LOGGER.info(String.format("%s: %s) processing job chain = %s", method, countJobChains, path));
                NodeList jobChainNodes = xpath.selectNodeList(jobChain, "job_chain_node[@job and string-length(@job)!=0]");
                for (int j = 0; j < jobChainNodes.getLength(); ++j) {
                    Element jobChainNode = (Element) jobChainNodes.item(j);
                    String nodeJob = jobChainNode.getAttribute("job");
                    String nodeState = jobChainNode.getAttribute("state");
                    if (SOSString.isEmpty(nodeJob)) {
                        continue;
                    }
                    if (nodeJob.toLowerCase().startsWith("/scheduler_file_order")) {
                        LOGGER.debug(String.format("%s: %s) %s continue processing, file order element found = %s", method, countJobChains,
                                logIndent, nodeJob));
                        continue;
                    }
                    LOGGER.debug(String.format("%s: %s) %s job node [%s] found with state = %s", method, countJobChains, logIndent, nodeJob,
                            nodeState));
                    boolean doSkip = false;
                    boolean doUnskip = false;
                    String criticalPathProcessingPrefix =
                            nodeState.substring(0, "-".equals(criticalPathProcessingMode) ? criticalPathNegativeProcessingPrefix.length()
                                    : criticalPathPositiveProcessingPrefix.length());
                    LOGGER.debug(String.format(
                            "%s: %s) %s criticalPathProcessingPrefix=%s criticalPathProcessingMode=%s criticalPathNegativeProcessingPrefix=%s "
                                    + "criticalPathPositiveProcessingPrefix=%s", method, countJobChains, logIndent, criticalPathProcessingPrefix,
                            criticalPathProcessingMode, criticalPathNegativeProcessingPrefix, criticalPathPositiveProcessingPrefix));
                    if (skip) {
                        if (criticalPathProcessingMode.equals(criticalPathNegativeProcessingPrefix.substring(0, 1))
                                && criticalPathProcessingPrefix.equals(criticalPathNegativeProcessingPrefix)) {
                            doSkip = true;
                        }
                        if (criticalPathProcessingMode.equals(criticalPathPositiveProcessingPrefix.substring(0, 1))
                                && !criticalPathProcessingPrefix.equals(criticalPathPositiveProcessingPrefix)) {
                            doSkip = true;
                        }
                        if (doSkip) {
                            LOGGER.info(String.format("%s: %s) %s skipping job node: job_chain='%s' state='%s'", method, countJobChains, logIndent,
                                    path, nodeState));
                            command = String.format("<job_chain_node.modify job_chain='%s' state='%s' action='next_state'/>", path, nodeState);
                            executeXml(command);
                            numberOfModifiedJobs++;
                        } else {
                            LOGGER.debug(String.format("%s: %s) %s do not skipping job node: job_chain='%s' state='%s'", method, countJobChains,
                                    logIndent, path, nodeState));
                        }
                    } else {
                        if (criticalPathProcessingMode.equals(criticalPathNegativeProcessingPrefix)
                                && criticalPathProcessingPrefix.equals(criticalPathNegativeProcessingPrefix)) {
                            doUnskip = true;
                        }
                        if (criticalPathProcessingMode.equals(criticalPathPositiveProcessingPrefix.substring(0, 1))
                                && !criticalPathProcessingPrefix.equals(criticalPathPositiveProcessingPrefix)) {
                            doUnskip = true;
                        }
                        if (criticalPathProcessingMode.equals(criticalPathPositiveProcessingPrefix.substring(0, 1))
                                && criticalPathProcessingPrefix.equals(criticalPathPositiveProcessingPrefix)) {
                            doUnskip = true;
                        }
                        if (doUnskip) {
                            LOGGER.info(String.format("%s: %s) %s unskipping job node: job_chain='%s' state='%s'", method, countJobChains, logIndent,
                                    path, nodeState));
                            command = String.format("<job_chain_node.modify job_chain='%s' state='%s' action='process'/>", path, nodeState);
                            executeXml(command);
                            numberOfModifiedJobs++;
                        } else {
                            LOGGER.debug(String.format("%s: %s) %s do not unskipping job node: job_chain='%s' state='%s'", method, countJobChains,
                                    logIndent, path, nodeState));
                        }
                    }
                }
            }
        } catch (Exception ex) {
            throw new Exception(String.format("%s:[scheduler %s:%s] %s", method, options.target_scheduler_host.getValue(),
                    options.target_scheduler_port.value(), ex.toString()));
        } finally {
            disconnect();
        }
    }

    private void initSender() {
        boolean createSchedulerCommand = true;
        schedulerCommand = null;
        if (spooler != null) {
            if (SOSString.isEmpty(options.target_scheduler_host.getValue())) {
                options.target_scheduler_host.setValue(spooler.hostname());
                createSchedulerCommand = false;
            } else if (options.target_scheduler_host.getValue().equalsIgnoreCase(spooler.hostname())) {
                createSchedulerCommand = false;
            }
            if (SOSString.isEmpty(options.target_scheduler_port.getValue())) {
                if (spooler.tcp_port() > 0) {
                    options.target_scheduler_port.value(spooler.tcp_port());
                } else if (spooler.udp_port() > 0) {
                    options.target_scheduler_port.value(spooler.udp_port());
                }
            }
        }
        if (createSchedulerCommand) {
            schedulerCommand = new SOSSchedulerCommand();
        }
    }

    private void connect() throws Exception {
        String method = "connect";
        if (schedulerCommand != null) {
            LOGGER.info(String.format("%s: connect to scheduler %s:%s", method, options.target_scheduler_host.getValue(),
                    options.target_scheduler_port.value()));
            schedulerCommand.connect(options.target_scheduler_host.getValue(), options.target_scheduler_port.value());
            if (options.target_scheduler_timeout.value() > 0) {
                schedulerCommand.setTimeout(options.target_scheduler_timeout.value());
            }
        }
    }

    private void disconnect() {
        String method = "disconnect";
        if (schedulerCommand != null) {
            try {
                LOGGER.info(String.format("%s: disconnect from scheduler %s:%s", method, options.target_scheduler_host.getValue(),
                        options.target_scheduler_port.value()));
                schedulerCommand.disconnect();
            } catch (Exception ex) {
            }
        }
    }

    private String join(String[] arr) {
        return Arrays.toString(arr);
    }

    public void setSpooler(Spooler sp) {
        spooler = sp;
    }

}