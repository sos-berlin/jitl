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

    private static Logger logger = LoggerFactory.getLogger(UncriticalJobNodesModel.class);

    private UncriticalJobNodesJobOptions options;
    private EUncriticalJobNodesOperation operation;

    // state prefix for critical path job nodes
    private String criticalPathNegativeProcessingPrefix = "-";
    private String criticalPathPositiveProcessingPrefix = "+";

    // processing mode: a value "-" will skip job nodes with that state prefix,
    // a value "+" will skip job nodes without that state prefix
    private String criticalPathProcessingMode = "-";

    // process job chains with the following prefixes exclusively
    private String[] includePrefixedJobChains;
    // do not process job chains with the following prefixes
    private String[] excludePrefixedJobChains;

    private int numberOfModifiedJobs = 0;

    private Spooler spooler;
    private SOSSchedulerCommand schedulerCommand;

    /** @param opt
     * @throws Exception */
    public UncriticalJobNodesModel(UncriticalJobNodesJobOptions opt) throws Exception {
        options = opt;

        String o = options.operation.Value().trim();
        if (o.equalsIgnoreCase(EUncriticalJobNodesOperation.SKIP.name())) {
            operation = EUncriticalJobNodesOperation.SKIP;
        } else if (o.equalsIgnoreCase(EUncriticalJobNodesOperation.UNSKIP.name())) {
            operation = EUncriticalJobNodesOperation.UNSKIP;
        } else {
            throw new Exception(String.format("unsupported operation = %s (supported operations %s)", o, Arrays.toString(EUncriticalJobNodesOperation.values()).toLowerCase()));
        }

    }

    /** @throws Exception */
    public void process() throws Exception {
        String method = "process";

        try {
            initSender();

            logger.info(String.format("%s: operation = %s (scheduler %s:%s)", method, operation.name().toLowerCase(), options.target_scheduler_host.Value(), options.target_scheduler_port.value()));

            initProperties();

            if (operation.equals(EUncriticalJobNodesOperation.SKIP)) {
                execute(true);
            } else if (operation.equals(EUncriticalJobNodesOperation.UNSKIP)) {
                execute(false);
            }

            logger.info(String.format("%s: numberOfModifiedJobs = %s ", method, numberOfModifiedJobs));
        } catch (Exception ex) {
            throw new Exception(String.format("%s: %s", method, ex.toString()));
        }
    }

    /** Purpose: skip job nodes that are not required for critical path
     * processing a) skip job nodes with negative state prefix b) skip job nodes
     * without positive state prefix
     *
     * @throws Exception */
    private void initProperties() throws Exception {
        String method = "initProperties";

        try {
            logger.info(String.format("%s: using parameter \"processing_recursive\" = %s", method, options.processing_recursive.Value()));

            if (!SOSString.isEmpty(options.processing_prefix.Value())) {
                logger.info(String.format("%s: using parameter \"processing_prefix\" = %s", method, options.processing_prefix.Value()));

                if (options.processing_prefix.Value().startsWith(criticalPathNegativeProcessingPrefix)) {
                    criticalPathNegativeProcessingPrefix = options.processing_prefix.Value();
                    criticalPathProcessingMode = criticalPathNegativeProcessingPrefix.substring(0, 1);
                } else if (options.processing_prefix.Value().startsWith(criticalPathPositiveProcessingPrefix)) {
                    criticalPathPositiveProcessingPrefix = options.processing_prefix.Value();
                    criticalPathProcessingMode = criticalPathPositiveProcessingPrefix.substring(0, 1);
                } else {
                    throw new Exception(String.format("%s: illegal value of parameter \"processing_prefix\" = %s", method, options.processing_prefix.Value()));
                }
            }

            if (!SOSString.isEmpty(options.include_job_chains.Value())) {
                logger.info(String.format("%s: using parameter \"include_job_chains\" = %s", method, options.include_job_chains.Value()));

                includePrefixedJobChains = options.include_job_chains.Value().split(";");
            }

            if (!SOSString.isEmpty(options.exclude_job_chains.Value())) {
                logger.info(String.format("%s: using parameter \"exclude_job_chains\" = %s", method, options.exclude_job_chains.Value()));

                excludePrefixedJobChains = options.exclude_job_chains.Value().split(";");
            }

            logger.info(String.format("%s: criticalPathProcessingMode = %s", method, criticalPathProcessingMode));
        } catch (Exception ex) {
            throw new Exception(String.format("%s: %s", method, ex.toString()));
        }
    }

    /** @param con
     * @param command
     * @return
     * @throws Exception */
    private SOSXMLXPath executeXml(String command) throws Exception {
        String method = "executeXml";

        logger.debug(String.format("%s: command = %s", method, command));

        String response = null;
        if (schedulerCommand != null) {
            schedulerCommand.sendRequest(command);
            response = schedulerCommand.getResponse();
        } else if (spooler != null) {
            response = spooler.execute_xml(command);
        } else {
            throw new Exception(String.format("%s: schedulerCommand and spooler are NULL", method));
        }

        logger.debug(String.format("%s: response = %s", method, response));

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

    /** @param skip
     * @throws Exception */
    private void execute(boolean skip) throws Exception {
        String method = "execute";

        numberOfModifiedJobs = 0;
        String logIndent = "    ";
        String command = "<show_state what=\"job_chains\"/>";
        // command =
        // "<show_state subsystems=\"order folder\" what=\"folders job_chains\"/>";
        try {
            connect();

            SOSXMLXPath xpath = executeXml(command);

            NodeList jobChains = xpath.selectNodeList("/" + xpath.getRoot().getNodeName() + "/answer/state/job_chains/job_chain");
            logger.info(String.format("%s: found %s job chains ", method, jobChains.getLength()));

            int countJobChains = 0;
            for (int i = 0; i < jobChains.getLength(); ++i) {
                countJobChains++;
                Element jobChain = (Element) jobChains.item(i);
                String state = jobChain.getAttribute("state");
                String path = jobChain.getAttribute("path");

                if (!state.equalsIgnoreCase(EJobChainState.RUNNING.name())) {
                    logger.info(String.format("%s: %s) do not process job chain [%s] due to state: %s", method, countJobChains, state, path));
                    continue;
                }

                int numberOfRecursiveFolders = 1;
                boolean doSkipJobChain = false;

                if (includePrefixedJobChains != null && includePrefixedJobChains.length > 0) {
                    doSkipJobChain = true;

                    for (String prefix : includePrefixedJobChains) {
                        logger.debug(String.format("%s: %s) path = %s include = %s", method, countJobChains, path, prefix));

                        if (path.indexOf(prefix) == 0) {
                            int prefixLen = prefix.split("/").length;
                            numberOfRecursiveFolders = prefix.startsWith("/") ? prefixLen - 1 : prefixLen;
                            int pathNumberOfRecursiveFolders = path.split("/").length - 1;
                            doSkipJobChain = (options.processing_recursive.value() == false && pathNumberOfRecursiveFolders > numberOfRecursiveFolders);
                            break;
                        }
                    }

                    if (doSkipJobChain) {

                        logger.info(String.format("%s: %s) do not process job chain [%s] due to inclusion rule = %s", method, countJobChains, path, join(includePrefixedJobChains)));
                    }
                } else {
                    int pathNumberOfRecursiveFolders = path.split("/").length - 1;
                    // do not process job chains recursively
                    doSkipJobChain = (options.processing_recursive.value() == false && pathNumberOfRecursiveFolders > numberOfRecursiveFolders);
                }

                if (doSkipJobChain == false && excludePrefixedJobChains != null) {
                    for (String prefix : excludePrefixedJobChains) {
                        logger.debug(String.format("%s: %s) path = %s exclude = %s", method, countJobChains, path, prefix));

                        if (path.indexOf(prefix) == 0) {
                            logger.info(String.format("%s: %s) do not process job chain [%s] due to exclusion rule = %s", method, countJobChains, path, prefix));

                            doSkipJobChain = true;
                        }
                    }
                }

                if (doSkipJobChain) {
                    continue;
                }

                logger.info(String.format("%s: %s) processing job chain = %s", method, countJobChains, path));

                NodeList jobChainNodes = xpath.selectNodeList(jobChain, "job_chain_node[@job and string-length(@job)!=0]");

                for (int j = 0; j < jobChainNodes.getLength(); ++j) {
                    Element jobChainNode = (Element) jobChainNodes.item(j);
                    String nodeJob = jobChainNode.getAttribute("job");
                    String nodeState = jobChainNode.getAttribute("state");

                    if (SOSString.isEmpty(nodeJob)) {
                        continue;
                    }

                    if (nodeJob.toLowerCase().startsWith("/scheduler_file_order")) {
                        logger.debug(String.format("%s: %s) %s continue processing, file order element found = %s", method, countJobChains, logIndent, nodeJob));
                        continue;
                    }

                    logger.debug(String.format("%s: %s) %s job node [%s] found with state = %s", method, countJobChains, logIndent, nodeJob, nodeState));

                    boolean doSkip = false;
                    boolean doUnskip = false;

                    String criticalPathProcessingPrefix = nodeState.substring(0, (criticalPathProcessingMode.equals("-") ? criticalPathNegativeProcessingPrefix.length()
                            : criticalPathPositiveProcessingPrefix.length()));
                    logger.debug(String.format("%s: %s) %s criticalPathProcessingPrefix=%s criticalPathProcessingMode=%s criticalPathNegativeProcessingPrefix=%s criticalPathPositiveProcessingPrefix=%s", method, countJobChains, logIndent, criticalPathProcessingPrefix, criticalPathProcessingMode, criticalPathNegativeProcessingPrefix, criticalPathPositiveProcessingPrefix));

                    if (skip) {
                        // for negative processing skip nodes that are marked
                        // with "-"
                        if (criticalPathProcessingMode.equals(criticalPathNegativeProcessingPrefix.substring(0, 1))
                                && criticalPathProcessingPrefix.equals(criticalPathNegativeProcessingPrefix)) {
                            doSkip = true;
                        }

                        // for positive processing skip nodes that are not
                        // marked with "+"
                        if (criticalPathProcessingMode.equals(criticalPathPositiveProcessingPrefix.substring(0, 1))
                                && !criticalPathProcessingPrefix.equals(criticalPathPositiveProcessingPrefix)) {
                            doSkip = true;
                        }

                        if (doSkip) {
                            logger.info(String.format("%s: %s) %s skipping job node: job_chain='%s' state='%s'", method, countJobChains, logIndent, path, nodeState));

                            command = String.format("<job_chain_node.modify job_chain='%s' state='%s' action='next_state'/>", path, nodeState);
                            executeXml(command);
                            numberOfModifiedJobs++;
                        } else {
                            logger.debug(String.format("%s: %s) %s do not skipping job node: job_chain='%s' state='%s'", method, countJobChains, logIndent, path, nodeState));
                        }
                    }// skip
                    else {
                        // for negative processing unksip nodes that are marked
                        // with "-"
                        if (criticalPathProcessingMode.equals(criticalPathNegativeProcessingPrefix)
                                && criticalPathProcessingPrefix.equals(criticalPathNegativeProcessingPrefix)) {
                            doUnskip = true;
                        }

                        // for positive processing unskip nodes that are not
                        // marked with "+"
                        if (criticalPathProcessingMode.equals(criticalPathPositiveProcessingPrefix.substring(0, 1))
                                && !criticalPathProcessingPrefix.equals(criticalPathPositiveProcessingPrefix)) {
                            doUnskip = true;
                        }

                        // for positive processing unskip nodes that are marked
                        // with "+"
                        if (criticalPathProcessingMode.equals(criticalPathPositiveProcessingPrefix.substring(0, 1))
                                && criticalPathProcessingPrefix.equals(criticalPathPositiveProcessingPrefix)) {
                            doUnskip = true;
                        }

                        if (doUnskip) {
                            logger.info(String.format("%s: %s) %s unskipping job node: job_chain='%s' state='%s'", method, countJobChains, logIndent, path, nodeState));

                            command = String.format("<job_chain_node.modify job_chain='%s' state='%s' action='process'/>", path, nodeState);
                            executeXml(command);
                            numberOfModifiedJobs++;
                        } else {
                            logger.debug(String.format("%s: %s) %s do not unskipping job node: job_chain='%s' state='%s'", method, countJobChains, logIndent, path, nodeState));
                        }
                    }
                }
            }
        } catch (Exception ex) {
            throw new Exception(String.format("%s:[scheduler %s:%s] %s", method, options.target_scheduler_host.Value(), options.target_scheduler_port.value(), ex.toString()));
        } finally {
            disconnect();
        }
    }

    /**
	 * 
	 */
    private void initSender() {
        boolean createSchedulerCommand = true;
        schedulerCommand = null;
        if (spooler != null) {
            if (SOSString.isEmpty(options.target_scheduler_host.Value())) {
                options.target_scheduler_host.Value(spooler.hostname());
                createSchedulerCommand = false;
            } else {
                if (options.target_scheduler_host.Value().equalsIgnoreCase(spooler.hostname())) {
                    createSchedulerCommand = false;
                }
            }

            if (SOSString.isEmpty(options.target_scheduler_port.Value())) {
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

    /** @throws Exception */
    private void connect() throws Exception {
        String method = "connect";
        if (schedulerCommand != null) {
            logger.info(String.format("%s: connect to scheduler %s:%s", method, options.target_scheduler_host.Value(), options.target_scheduler_port.value()));
            schedulerCommand.connect(options.target_scheduler_host.Value(), options.target_scheduler_port.value());
            if (options.target_scheduler_timeout.value() > 0) {
                schedulerCommand.setTimeout(options.target_scheduler_timeout.value());
            }
        }
    }

    /**
	 * 
	 */
    private void disconnect() {
        String method = "disconnect";
        if (schedulerCommand != null) {
            try {
                logger.info(String.format("%s: disconnect from scheduler %s:%s", method, options.target_scheduler_host.Value(), options.target_scheduler_port.value()));

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
