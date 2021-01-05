package com.sos.jitl.jobstreams.db;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.hibernate.exceptions.SOSHibernateException;
import com.sos.jitl.dailyplan.db.Calendar2DB;
import com.sos.jitl.dailyplan.db.DailyPlanDBLayer;
import com.sos.jitl.jobstreams.classes.JobStreamScheduler;
import com.sos.joc.model.common.NameValuePair;
import com.sos.joc.model.jobstreams.JobStreamJob;
import com.sos.joc.model.jobstreams.JobStreamStarter;
import com.sos.joc.model.jobstreams.JobStreamStarters;
import com.sos.joc.model.joe.schedule.RunTime;

public class DBLayerJobStreamStarters {

    private static final Logger LOGGER = LoggerFactory.getLogger(DBLayerJobStreamStarters.class);
    private static final String MAX_DATE = "01-01-2038";

    private static final String DBItemJobStreamStarter = DBItemJobStreamStarter.class.getSimpleName();
    private final SOSHibernateSession sosHibernateSession;

    public DBLayerJobStreamStarters(SOSHibernateSession session) {
        this.sosHibernateSession = session;
    }

    public DBItemJobStreamStarter getJobStreamStartersDbItem(final Long id) throws SOSHibernateException {
        if (id == null) {
            return null;
        } else {
            return (DBItemJobStreamStarter) sosHibernateSession.get(DBItemJobStreamStarter.class, id);
        }
    }

    public FilterJobStreamStarters resetFilter() {
        FilterJobStreamStarters filter = new FilterJobStreamStarters();
        filter.setStatus("");
        return filter;
    }

    private String getWhere(FilterJobStreamStarters filter) {
        String where = " ";
        String and = " ";

        if (filter.getStatus() != null && !"".equals(filter.getStatus())) {
            where += and + " status = :status";
            and = " and ";
        }

        if (filter.getTitle() != null && !"".equals(filter.getTitle())) {
            where += and + " title = :title";
            and = " and ";
        }

        if (filter.getId() != null) {
            where += and + " id  = :id";
            and = " and ";
        }
        if (filter.getJobStreamId() != null) {
            where += and + " jobStream  = :jobStream";
            and = " and ";
        }

        if (!"".equals(where.trim())) {
            where = " where " + where;
        }
        return where;
    }

    private <T> Query<T> bindParameters(FilterJobStreamStarters filter, Query<T> query) {
        if (filter.getId() != null) {
            query.setParameter("id", filter.getId());
        }

        if (filter.getStatus() != null && !"".equals(filter.getStatus())) {
            query.setParameter("status", filter.getStatus());
        }
        if (filter.getTitle() != null && !"".equals(filter.getTitle())) {
            query.setParameter("title", filter.getTitle());
        }
        if (filter.getJobStreamId() != null) {
            query.setParameter("jobStream", filter.getJobStreamId());
        }

        return query;
    }

    public List<DBItemJobStreamStarter> getJobStreamStartersList(FilterJobStreamStarters filter, final int limit) throws SOSHibernateException {
        String q = "  from " + DBItemJobStreamStarter + getWhere(filter);

        Query<DBItemJobStreamStarter> query = sosHibernateSession.createQuery(q);
        query = bindParameters(filter, query);

        if (limit > 0) {
            query.setMaxResults(limit);
        }
        return sosHibernateSession.getResultList(query);
    }

    public Integer delete(FilterJobStreamStarters filter) throws SOSHibernateException {

        int row = 0;
        String hql = "";

        hql = "delete from " + DBItemJobStreamStarter + getWhere(filter);
        Query<DBItemJobStreamStarter> query = sosHibernateSession.createQuery(hql);
        query = bindParameters(filter, query);

        row = sosHibernateSession.executeUpdate(query);
        return row;
    }

    public void save(DBItemJobStreamStarter dbItemJobStreamStarter) throws SOSHibernateException {
        sosHibernateSession.save(dbItemJobStreamStarter);
    }

    public void update(DBItemJobStreamStarter dbItemJobStreamStarter) throws SOSHibernateException {
        this.sosHibernateSession.update(dbItemJobStreamStarter);
    }

    public Date getNextStartTime(ObjectMapper objectMapper, String timeZone, String runTimeString) throws JsonParseException, JsonMappingException,
            JsonProcessingException, IOException, Exception {
        JobStreamScheduler jobStreamScheduler = new JobStreamScheduler(timeZone);
        RunTime runTime = null;
        if (runTimeString != null) {
            runTime = objectMapper.readValue(runTimeString, RunTime.class);
        }
        if (runTime != null) {
            Calendar c = Calendar.getInstance();
            Date from = new Date();
            Date to = new Date();
            c.setTime(to);
            c.add(Calendar.DATE, 3);
            to = c.getTime();
            SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
            String max = MAX_DATE;
            Date maxDate = formatter.parse(max);

            do {
                jobStreamScheduler.schedule(from, to, runTime, true);
                c.setTime(to);
                c.add(Calendar.MONTH, 1);
                from = to;
                to = c.getTime();
            } while (jobStreamScheduler.getListOfStartTimes().isEmpty() && to.before(maxDate));
        }

        Date now = new Date();
        if (jobStreamScheduler.getListOfStartTimes() != null) {
            for (Long start : jobStreamScheduler.getListOfStartTimes()) {
                if (start > now.getTime()) {
                    return new Date(start);
                }
            }
        }
        return null;
    }

    public void saveOrUpdate(JobStreamStarters jobStreamStarters, String timezone) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        DBLayerJobStreamsStarterJobs dbLayerJobStreamsStarterJobs = new DBLayerJobStreamsStarterJobs(sosHibernateSession);
        Calendar2DB calendar2Db = new Calendar2DB(sosHibernateSession, jobStreamStarters.getJobschedulerId());
        boolean isNew = false;

        for (JobStreamStarter jobStreamStarter : jobStreamStarters.getJobstreamStarters()) {

            Long oldId = jobStreamStarter.getJobStreamStarterId();

            DBItemJobStreamStarter dbItemJobStreamStarter = getJobStreamStartersDbItem(oldId);
            if (dbItemJobStreamStarter == null) {
                isNew = true;
                dbItemJobStreamStarter = new DBItemJobStreamStarter();
                dbItemJobStreamStarter.setCreated(new Date());
            }

            dbItemJobStreamStarter.setJobStream(jobStreamStarters.getJobStreamId());
            dbItemJobStreamStarter.setEndOfJobStream(jobStreamStarter.getEndOfJobStream());
            dbItemJobStreamStarter.setRequiredJob(jobStreamStarter.getRequiredJob());
            dbItemJobStreamStarter.setTitle(jobStreamStarter.getTitle());
            dbItemJobStreamStarter.setState(jobStreamStarter.getState());

            if (jobStreamStarter.getRunTime() != null) {
                dbItemJobStreamStarter.setRunTime(objectMapper.writeValueAsString(jobStreamStarter.getRunTime()));
            }
            dbItemJobStreamStarter.setNextStart(getNextStartTime(objectMapper, timezone, dbItemJobStreamStarter.getRunTime()));

            if (isNew) {
                save(dbItemJobStreamStarter);
            } else {
                update(dbItemJobStreamStarter);
            }

            jobStreamStarter.setJobStreamStarterId(dbItemJobStreamStarter.getId());

            DailyPlanDBLayer dailyPlanDBLayer = new DailyPlanDBLayer(sosHibernateSession);
            dailyPlanDBLayer.getFilter().setJobStreamStarterId(oldId);
            dailyPlanDBLayer.delete(false);

            for (JobStreamJob jobStreamJob : jobStreamStarter.getJobs()) {
                DBItemJobStreamStarterJob dbItemJobStreamStarterJob = new DBItemJobStreamStarterJob();
                dbItemJobStreamStarterJob.setCreated(new Date());
                dbItemJobStreamStarterJob.setDelay(jobStreamJob.getStartDelay());
                dbItemJobStreamStarterJob.setJob(jobStreamJob.getJob());
                dbItemJobStreamStarterJob.setJobStreamStarter(dbItemJobStreamStarter.getId());
                if (jobStreamJob.getSkipOutCondition() == null) {
                    dbItemJobStreamStarterJob.setSkipOutCondition(false);
                } else {
                    dbItemJobStreamStarterJob.setSkipOutCondition(jobStreamJob.getSkipOutCondition());
                }
                Long newJobId = dbLayerJobStreamsStarterJobs.store(dbItemJobStreamStarterJob);
                jobStreamJob.setJobId(newJobId);
            }

            for (NameValuePair param : jobStreamStarter.getParams()) {
                DBItemJobStreamParameter dbItemJobStreamParameter = new DBItemJobStreamParameter();
                dbItemJobStreamParameter.setCreated(new Date());
                dbItemJobStreamParameter.setJobStreamStarter(dbItemJobStreamStarter.getId());
                if (param.getName() != null) {
                    dbItemJobStreamParameter.setName(param.getName());
                    if (param.getValue() == null) {
                        param.setValue("");
                    }
                    dbItemJobStreamParameter.setValue(param.getValue());
                    sosHibernateSession.save(dbItemJobStreamParameter);
                }
            }
        }
        FilterJobStreams filterJobStreams = new FilterJobStreams();
        filterJobStreams.setJobStreamId(jobStreamStarters.getJobStreamId());
        calendar2Db.processJobStreamStarterFilter(filterJobStreams, timezone);
    }

    public void deleteStarters(JobStreamStarters jobStreamStarters, String timezone) throws Exception {
        DBLayerJobStreamParameters dbLayerJobStreamParameters = new DBLayerJobStreamParameters(sosHibernateSession);
        DBLayerJobStreamsStarterJobs dbLayerJobStreamsStarterJobs = new DBLayerJobStreamsStarterJobs(sosHibernateSession);
        Calendar2DB calendar2Db = new Calendar2DB(sosHibernateSession, jobStreamStarters.getJobschedulerId());

        for (JobStreamStarter jobStreamStarter : jobStreamStarters.getJobstreamStarters()) {

            FilterJobStreamStarters filterJobStreamStarters = new FilterJobStreamStarters();
            filterJobStreamStarters.setJobStreamId(jobStreamStarters.getJobStreamId());
            List<DBItemJobStreamStarter> lStarters = getJobStreamStartersList(filterJobStreamStarters, 0);
            if (lStarters.size() > 1) {

                DailyPlanDBLayer dailyPlanDBLayer = new DailyPlanDBLayer(sosHibernateSession);
                dailyPlanDBLayer.getFilter().setJobStreamStarterId(jobStreamStarter.getJobStreamStarterId());
                dailyPlanDBLayer.delete(false);

                filterJobStreamStarters.setId(jobStreamStarter.getJobStreamStarterId());
                lStarters = getJobStreamStartersList(filterJobStreamStarters, 0);
                for (DBItemJobStreamStarter dbItemJobStreamStarter : lStarters) {
                    FilterJobStreamStarterJobs filterJobStreamStarterJobs = new FilterJobStreamStarterJobs();
                    filterJobStreamStarterJobs.setJobStreamStarter(dbItemJobStreamStarter.getId());
                    dbLayerJobStreamsStarterJobs.delete(filterJobStreamStarterJobs);
                }

                for (DBItemJobStreamStarter dbItemJobStreamStarter : lStarters) {
                    FilterJobStreamParameters filterJobStreamParameters = new FilterJobStreamParameters();
                    filterJobStreamParameters.setJobStreamStarterId(dbItemJobStreamStarter.getId());
                    dbLayerJobStreamParameters.delete(filterJobStreamParameters);
                }

                this.delete(filterJobStreamStarters);
            }

        }
        FilterJobStreams filterJobStreams = new FilterJobStreams();
        filterJobStreams.setJobStreamId(jobStreamStarters.getJobStreamId());
        calendar2Db.processJobStreamStarterFilter(filterJobStreams, timezone);
    }
}