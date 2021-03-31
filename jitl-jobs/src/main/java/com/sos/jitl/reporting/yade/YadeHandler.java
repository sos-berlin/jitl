package com.sos.jitl.reporting.yade;

import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.hibernate.exceptions.SOSHibernateException;
import com.sos.jade.db.DBItemYadeFiles;
import com.sos.jade.db.DBItemYadeProtocols;
import com.sos.jade.db.DBItemYadeTransfers;
import com.sos.jade.db.history.YadeEngineTransferResult;
import com.sos.jitl.reporting.db.DBItemSchedulerHistory;
import com.sos.yade.commons.Yade;
import com.sos.yade.commons.result.YadeTransferResultEntry;
import com.sos.yade.commons.result.YadeTransferResultProtocol;
import com.sos.yade.commons.result.YadeTransferResultSerializer;

import sos.util.SOSString;

public class YadeHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(YadeHandler.class);

    private static final String DBITEM_YADE_PROTOCOLS = DBItemYadeProtocols.class.getSimpleName();

    private final ConcurrentHashMap<String, Long> protocols;

    public YadeHandler() {
        this.protocols = new ConcurrentHashMap<String, Long>();
    }

    public Long process(SOSHibernateSession session, DBItemSchedulerHistory schedulerTask) throws SOSHibernateException {
        String serialized = StringUtils.strip(schedulerTask.getTransferHistory(), "'");
        if (SOSString.isEmpty(serialized)) {
            return null;
        }

        String logMsg = String.format("[%s][job name=%s, taskId=%s", schedulerTask.getSpoolerId(), schedulerTask.getJobName(), schedulerTask.getId());
        try {
            YadeTransferResultSerializer<YadeEngineTransferResult> serializer = new YadeTransferResultSerializer<YadeEngineTransferResult>();
            YadeEngineTransferResult result = serializer.deserialize(serialized);
            if (result == null) {
                return null;
            }

            Long transferId = saveTransfer(session, result);
            saveTransferEntries(session, transferId, result.getEntries());
            return transferId;
        } catch (SOSHibernateException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.error(String.format("[%s]%s", logMsg, e.toString()), e);
        }
        return null;
    }

    private Long saveTransfer(SOSHibernateSession session, YadeEngineTransferResult result) throws SOSHibernateException {
        DBItemYadeTransfers item = new DBItemYadeTransfers();
        item.setMandator(result.getMandator());
        item.setJobschedulerId(result.getJobschedulerId());
        item.setTaskId(result.getTaskId());
        item.setJob(result.getJob());
        item.setOrderId(result.getOrderId());
        item.setJobChain(result.getJobChain());
        item.setJobChainNode(result.getJobChainNode());

        item.setSourceProtocolId(getProtocolId(session, result.getSource()));
        item.setTargetProtocolId(getProtocolId(session, result.getTarget()));
        item.setJumpProtocolId(getProtocolId(session, result.getJump()));
        item.setMandator(result.getMandator());
        item.setOperation(Yade.TransferOperation.fromValue(result.getOperation()).intValue());
        item.setProfileName(result.getProfile());
        item.setStart(Date.from(result.getStart()));
        item.setEnd(Date.from(result.getEnd()));
        item.setNumOfFiles(result.getEntries() == null ? 0L : result.getEntries().size());
        item.setState(SOSString.isEmpty(result.getErrorMessage()) ? Yade.TransferState.SUCCESSFUL.intValue() : Yade.TransferState.FAILED.intValue());
        item.setErrorMessage(result.getErrorMessage());
        item.setModified(new Date());

        session.save(item);
        return item.getId();
    }

    private void saveTransferEntries(SOSHibernateSession session, Long transferId, List<YadeTransferResultEntry> entries)
            throws SOSHibernateException {
        if (entries == null || entries.size() == 0) {
            return;
        }

        for (YadeTransferResultEntry entry : entries) {
            DBItemYadeFiles item = new DBItemYadeFiles();
            item.setTransferId(transferId);
            item.setInterventionTransferId(null);
            item.setSourcePath(entry.getSource());
            item.setTargetPath(entry.getTarget());
            item.setSize(entry.getSize());
            item.setModificationDate(getUTCFromTimestamp(entry.getModificationDate()));
            item.setState(Yade.TransferEntryState.fromValue(entry.getState()).intValue());
            item.setIntegrityHash(entry.getIntegrityHash());
            item.setErrorMessage(entry.getErrorMessage());
            if (!SOSString.isEmpty(item.getErrorMessage())) {
                item.setErrorCode("ERRORCODE");
            }
            item.setModified(new Date());

            session.save(item);
        }
    }

    private Long getProtocolId(SOSHibernateSession session, YadeTransferResultProtocol protocol) {
        if (protocol == null) {
            return null;
        }

        Integer protocolIntVal = Yade.TransferProtocol.fromValue(protocol.getProtocol()).intValue();
        String key = new StringBuilder(protocol.getHost()).append(protocol.getPort()).append(protocolIntVal).append(protocol.getAccount()).toString();

        // TODO deleted protocols handling ...
        if (protocols.containsKey(key)) {
            return protocols.get(key);
        }

        Long id = 0L;
        boolean run = true;
        int count = 0;
        while (run) {
            count = count + 1;
            try {
                id = getProtocolId(session, protocol.getHost(), protocol.getPort(), protocolIntVal, protocol.getAccount());
                if (id == null) {
                    DBItemYadeProtocols item = new DBItemYadeProtocols();
                    item.setHostname(protocol.getHost());
                    item.setPort(protocol.getPort());
                    item.setProtocol(protocolIntVal);
                    item.setAccount(protocol.getAccount());

                    session.save(item);
                    id = item.getId();
                }
                protocols.put(key, id);
                return id;
            } catch (SOSHibernateException e) {
                if (count >= 3) {
                    run = false;
                } else {
                    try {
                        Thread.sleep(2 * 1_000);
                    } catch (InterruptedException e1) {
                    }
                }
            }
        }
        return id;
    }

    private Long getProtocolId(SOSHibernateSession session, String hostname, Integer port, Integer protocol, String account)
            throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("select id ");
        hql.append("from ").append(DBITEM_YADE_PROTOCOLS).append(" ");
        hql.append("where hostname=:hostname ");
        hql.append("and port=:port ");
        hql.append("and protocol=:protocol ");
        hql.append("and account=:account");

        Query<Long> query = session.createQuery(hql.toString());
        query.setParameter("hostname", hostname);
        query.setParameter("port", port);
        query.setParameter("protocol", protocol);
        query.setParameter("account", account);

        List<Long> result = session.getResultList(query);
        if (result != null && result.size() > 0) {
            return result.get(0);
        }
        return null;
    }

    // TODO check this method
    private Date getUTCFromTimestamp(long timestamp) {
        if (timestamp < 0L) {
            return null;
        }
        return new Date(timestamp - TimeZone.getDefault().getOffset(timestamp));
    }

}
