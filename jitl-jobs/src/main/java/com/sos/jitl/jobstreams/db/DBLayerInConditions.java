package com.sos.jitl.jobstreams.db;

import java.nio.file.Paths;
import java.util.Date;
import java.util.List;

import org.hibernate.query.Query;
import org.hibernate.transform.Transformers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.hibernate.exceptions.SOSHibernateException;
import com.sos.joc.model.jobstreams.InCondition;
import com.sos.joc.model.jobstreams.InConditions;
import com.sos.joc.model.jobstreams.JobInCondition;

public class DBLayerInConditions {

    private static final Logger LOGGER = LoggerFactory.getLogger(DBLayerInConditions.class);
    private static final String DBItemInCondition = DBItemInCondition.class.getSimpleName();
    private static final String DBItemInConditionCommand = DBItemInConditionCommand.class.getSimpleName();
    private final SOSHibernateSession sosHibernateSession;

    public DBLayerInConditions(SOSHibernateSession session) {
        this.sosHibernateSession = session;
    }

    public DBItemInCondition getConditionsDbItem(final Long id) throws Exception {
        return (DBItemInCondition) sosHibernateSession.get(DBItemInCondition.class, id);
    }

    public FilterInConditions resetFilter() {
        FilterInConditions filter = new FilterInConditions();
        filter.setJobSchedulerId("");
        filter.setJob("");
        filter.setJobStream("");
        return filter;
    }

    private String getWhere(FilterInConditions filter) {
        String where = "";
        String and = "";

        if (filter.getJobSchedulerId() != null && !"".equals(filter.getJobSchedulerId())) {
            where += and + " i.schedulerId = :schedulerId";
            and = " and ";
        }

        if (filter.getJob() != null && !"".equals(filter.getJob())) {
            where += and + " i.job = :job";
            and = " and ";
        }

        if (filter.getJobStream() != null && !"".equals(filter.getJobStream())) {
            where += and + " i.jobStream = :jobStream";
            and = " and ";
        }
        if (filter.getFolder() != null && !"".equals(filter.getFolder())) {
            where += and + " i.folder = :folder";
            and = " and ";
        }
        if (filter.getJoin() != null && !"".equals(filter.getJoin())) {
            where += and + filter.getJoin();
            and = " and ";
        }

        if (!"".equals(where.trim())) {
            where = "where " + where;
        }

        return where;
    }

    private <T> Query<T> bindParameters(FilterInConditions filter, Query<T> query) {
        if (filter.getJobSchedulerId() != null && !"".equals(filter.getJobSchedulerId())) {
            query.setParameter("schedulerId", filter.getJobSchedulerId());
        }
        if (filter.getJob() != null && !"".equals(filter.getJob())) {
            query.setParameter("job", filter.getJob());
        }
        if (filter.getFolder() != null && !"".equals(filter.getFolder())) {
            query.setParameter("folder", filter.getFolder());
        }

        if (filter.getJobStream() != null && !"".equals(filter.getJobStream())) {
            query.setParameter("jobStream", filter.getJobStream());
        }

        return query;
    }

    public List<DBItemInConditionWithCommand> getInConditionsList(FilterInConditions filter, final int limit) throws SOSHibernateException {
        filter.setJoin("i.id=c.inConditionId");
        String q =
                "select i.id as incId,i.schedulerId as jobSchedulerId,i.job as job,i.expression as expression,i.markExpression as markExpression,i.skipOutCondition as skipOutCondition,"
                        + "i.jobStream as jobStream,i.folder as folder,i.nextPeriod as nextPeriod,c.id as commandId,c.inConditionId as inConditionId,c.command as command,c.commandParam  as commandParam from "
                        + DBItemInCondition + " i, " + DBItemInConditionCommand + " c " + getWhere(filter);
        Query<DBItemInConditionWithCommand> query = sosHibernateSession.createQuery(q);
        query.setResultTransformer(Transformers.aliasToBean(DBItemInConditionWithCommand.class));
        query = bindParameters(filter, query);

        if (limit > 0) {
            query.setMaxResults(limit);
        }
        return sosHibernateSession.getResultList(query);
    }

    public List<DBItemInCondition> getSimpleInConditionsList(FilterInConditions filter, final int limit) throws SOSHibernateException {
        String q = "  from " + DBItemInCondition + " i  " + getWhere(filter);
        Query<DBItemInCondition> query = sosHibernateSession.createQuery(q);
        query = bindParameters(filter, query);

        if (limit > 0) {
            query.setMaxResults(limit);
        }
        return sosHibernateSession.getResultList(query);
    }

    public int delete(FilterInConditions filterInConditions) throws SOSHibernateException {
        String hql = "delete from " + DBItemInCondition + " i " + getWhere(filterInConditions);
        int row = 0;
        Query<DBItemInCondition> query = sosHibernateSession.createQuery(hql);
        query = bindParameters(filterInConditions, query);

        row = sosHibernateSession.executeUpdate(query);
        return row;
    }

    private Boolean getBoolean(Boolean b, Boolean efault) {
        if (b == null) {
            return efault;
        } else {
            return b;
        }

    }

    public void deleteCascading(FilterInConditions filterInConditions) throws SOSHibernateException {
        DBLayerInConditionCommands dbLayerInConditionCommands = new DBLayerInConditionCommands(sosHibernateSession);
        DBLayerConsumedInConditions dbLayerConsumedInConditions = new DBLayerConsumedInConditions(sosHibernateSession);

        FilterInConditionCommands filterInConditionCommands = new FilterInConditionCommands();
        filterInConditionCommands.setJobStream(filterInConditions.getJobStream());
        filterInConditionCommands.setFolder(filterInConditions.getFolder());
        dbLayerInConditionCommands.deleteCommandWithInConditions(filterInConditionCommands);

        FilterConsumedInConditions filterConsumedInConditions = new FilterConsumedInConditions();
        filterConsumedInConditions.setJobStream(filterInConditions.getJobStream());
        filterConsumedInConditions.setFolder(filterInConditions.getFolder());
        dbLayerConsumedInConditions.deleteConsumedInConditions(filterConsumedInConditions);

        delete(filterInConditions);
    }

    public void deleteInsert(InConditions inConditions) throws SOSHibernateException {

        DBLayerInConditionCommands dbLayerInConditionCommands = new DBLayerInConditionCommands(sosHibernateSession);
        DBLayerConsumedInConditions dbLayerConsumedInConditions = new DBLayerConsumedInConditions(sosHibernateSession);
        for (JobInCondition jobInCondition : inConditions.getJobsInconditions()) {

            if ("".equals(jobInCondition.getJob())) {
                continue;
            }
            String folder = Paths.get(jobInCondition.getJob()).getParent().toString().replace('\\', '/');

            DBLayerInConditions dbLayerInConditions = new DBLayerInConditions(sosHibernateSession);
            FilterInConditions filterInConditions = new FilterInConditions();
            filterInConditions.setJob(jobInCondition.getJob());
            filterInConditions.setJobSchedulerId(inConditions.getJobschedulerId());
            List<DBItemInCondition> listOfInCondititinos = dbLayerInConditions.getSimpleInConditionsList(filterInConditions, 0);

            delete(filterInConditions);

            for (InCondition inCondition : jobInCondition.getInconditions()) {
                Long oldId = inCondition.getId();
                DBItemInCondition dbItemInCondition = new DBItemInCondition();
                String expression = inCondition.getConditionExpression().getExpression();
                if (expression == null || expression.isEmpty()) {
                    expression = "false";
                }
                dbItemInCondition.setExpression(expression);
                dbItemInCondition.setJob(jobInCondition.getJob());
                dbItemInCondition.setSchedulerId(inConditions.getJobschedulerId());
                // dbItemInCondition.setJobStream(Paths.get(inCondition.getJobStream()).getFileName().toString());
                dbItemInCondition.setJobStream(inCondition.getJobStream());
                dbItemInCondition.setFolder(folder);
                dbItemInCondition.setMarkExpression(getBoolean(inCondition.getMarkExpression(), true));
                dbItemInCondition.setSkipOutCondition(getBoolean(inCondition.getSkipOutCondition(), false));
                dbItemInCondition.setCreated(new Date());
                sosHibernateSession.save(dbItemInCondition);
                dbLayerInConditionCommands.deleteInsert(dbItemInCondition, inCondition);
                Long newId = dbItemInCondition.getId();
                if (oldId != null) {
                    dbLayerConsumedInConditions.updateConsumedInCondition(oldId, newId);
                }
            }

            for (DBItemInCondition dbItemInCondition : listOfInCondititinos) {
                FilterInConditionCommands filterInConditionCommands = new FilterInConditionCommands();
                filterInConditionCommands.setInConditionId(dbItemInCondition.getId());
                dbLayerInConditionCommands.deleteByInConditionId(filterInConditionCommands);

                FilterConsumedInConditions filterConsumedInConditions = new FilterConsumedInConditions();
                filterConsumedInConditions.setInConditionId(dbItemInCondition.getId());
                dbLayerConsumedInConditions.deleteByInConditionId(filterConsumedInConditions);
            }

        }
    }

}