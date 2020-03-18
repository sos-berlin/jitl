package com.sos.jitl.checkblacklist;

import java.io.ByteArrayInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import sos.scheduler.job.JobSchedulerJobAdapter;
import sos.spooler.Job;
import sos.spooler.Job_chain;
import sos.spooler.Order;
import sos.spooler.Variable_set;

import com.sos.JSHelper.Exceptions.JobSchedulerException;

public class JobSchedulerCheckBlacklistJSAdapterClass extends JobSchedulerJobAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobSchedulerCheckBlacklistJSAdapterClass.class);
    private DocumentBuilder docBuilder;
    private JobSchedulerCheckBlacklistOptions jobSchedulerCheckBlacklistOptions;
    private int counter;

    @Override
    public boolean spooler_init() {
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            docBuilder = docFactory.newDocumentBuilder();
        } catch (Exception e) {
            try {
                LOGGER.error("Error occured during initialisation: " + e);
            } catch (Exception ex) {
            }
            return false;
        }
        return super.spooler_init();
    }

    @Override
    public boolean spooler_process() throws Exception {
        try {
            super.spooler_process();
            doProcessing();
            return getSpoolerProcess().getSuccess();
        } catch (Exception e) {
            throw new JobSchedulerException("Fatal Error:" + e.getMessage(), e);
        }
    }

    private void doProcessing() throws Exception {
        jobSchedulerCheckBlacklistOptions = new JobSchedulerCheckBlacklistOptions();
        jobSchedulerCheckBlacklistOptions.setCurrentNodeName(getCurrentNodeName(getSpoolerProcess().getOrder(), true));
        jobSchedulerCheckBlacklistOptions.setAllOptions(getSchedulerParameterAsProperties(getSpoolerProcess().getOrder()));
        jobSchedulerCheckBlacklistOptions.checkMandatory();
        checkBlacklist();
    }

    private void checkBlacklist() throws Exception {
        try {
            String answer = spooler.execute_xml("<show_state what=\"job_chain_orders,blacklist\"/>");
            Document spoolerDocument = docBuilder.parse(new ByteArrayInputStream(answer.getBytes()));
            Element spoolerElement = spoolerDocument.getDocumentElement();
            Node answerNode = spoolerElement.getFirstChild();
            while (answerNode != null && answerNode.getNodeType() != Node.ELEMENT_NODE) {
                answerNode = answerNode.getNextSibling();
            }
            if (answerNode == null) {
                throw new JobSchedulerException("answer contains no xml elements, is null");
            }
            Element answerElement = (Element) answerNode;
            if (!"answer".equals(answerElement.getNodeName())) {
                throw new JobSchedulerException("element <answer> is missing");
            }
            NodeList schedulerNodes = answerElement.getElementsByTagName("blacklist");
            LOGGER.debug(schedulerNodes.getLength() + " blacklists found.");
            counter = schedulerNodes.getLength();
            if ("blacklist".equalsIgnoreCase(jobSchedulerCheckBlacklistOptions.granuality.getValue())) {
                execute("There are orders in " + schedulerNodes.getLength() + " blacklists", null);
            } else {
                for (int i = 0; i < schedulerNodes.getLength(); i++) {
                    Node blacklistNode = schedulerNodes.item(i);
                    if (blacklistNode != null && blacklistNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element blacklist = (Element) blacklistNode;
                        handleBlacklistEntry(blacklist);
                    }
                }
            }
        } catch (Exception e) {
            throw new JobSchedulerException("Error occured checking blacklists: " + e, e);
        }
    }

    private void handleBlacklistEntry(final Element blacklist) throws Exception {
        NodeList blacklistOrders = blacklist.getElementsByTagName("order");
        LOGGER.info(blacklistOrders.getLength() + " orders in blacklists found.");
        for (int i = 0; i < blacklistOrders.getLength(); i++) {
            Node orderNode = blacklistOrders.item(i);
            if (orderNode != null && orderNode.getNodeType() == Node.ELEMENT_NODE) {
                Element order = (Element) orderNode;
                BlackList b = new BlackList();
                b.job_chain = order.getAttribute("job_chain");
                b.id = "";
                b.created = "";
                if ("order".equalsIgnoreCase(jobSchedulerCheckBlacklistOptions.granuality.getValue())) {
                    b.id = order.getAttribute("id");
                    b.created = order.getAttribute("created");
                    execute("Blacklist found for job_chain:" + b.job_chain + " file=" + b.id + "; created:" + b.created, b);
                } else {
                    execute(blacklistOrders.getLength() + " order found in Blacklist for job_chain:" + jobSchedulerCheckBlacklistOptions.job_chain
                            .getValue(), b);
                    break;
                }
            }
        }
    }

    private void execute(final String s, final BlackList b) throws Exception {
        String level = jobSchedulerCheckBlacklistOptions.level.getValue();
        String job = jobSchedulerCheckBlacklistOptions.job.getValue();
        String jobChain = jobSchedulerCheckBlacklistOptions.job_chain.getValue();
        if ("info".equalsIgnoreCase(level)) {
            LOGGER.info(s);
        }
        if ("warning".equalsIgnoreCase(level)) {
            LOGGER.warn(s);
        }
        if ("error".equalsIgnoreCase(level)) {
            LOGGER.error(s);
        }
        if (counter > 0) {
            if (!"".equals(job)) {
                Job j = spooler.job(job);
                if (j != null) {
                    if (b != null) {
                        Variable_set p = spooler.create_variable_set();
                        p.merge(spooler_task.params());
                        p.set_var("filename", b.id);
                        p.set_var("blacklist_job_chain", b.job_chain);
                        p.set_var("created", b.created);
                        j.start(p);
                    } else {
                        j.start(spooler.create_variable_set());
                    }
                } else {
                    LOGGER.warn("Job: " + job + " unknown");
                }
            }
            if (!"".equalsIgnoreCase(jobChain)) {
                Job_chain jc = spooler.job_chain(jobChain);
                if (jc != null) {
                    Order o = spooler.create_order();
                    if (b != null) {
                        o.params().merge(spooler_task.params());
                        o.params().set_var("filename", b.id);
                        o.params().set_var("blacklist_job_chain", b.job_chain);
                        o.params().set_var("created", b.created);
                    }
                    jc.add_order(o);
                } else {
                    LOGGER.warn("Job_chain: " + jobChain + " unknown");
                }
            }
        }
    }

    private class BlackList {

        protected String id;
        protected String job_chain;
        protected String created;
    }

}
