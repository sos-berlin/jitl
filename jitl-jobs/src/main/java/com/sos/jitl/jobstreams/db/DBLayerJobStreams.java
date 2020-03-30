package com.sos.jitl.jobstreams.db;

import java.util.Date;
import java.util.List;

import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.hibernate.exceptions.SOSHibernateException;
import com.sos.joc.model.common.NameValuePair;
import com.sos.joc.model.jobstreams.JobStream;
import com.sos.joc.model.jobstreams.JobStreamStarter;
import com.sos.joc.model.jobstreams.JobStreamStarters;

public class DBLayerJobStreams {

    private static final Logger LOGGER = LoggerFactory.getLogger(DBLayerJobStreams.class);
    private static final String DBItemJobStream = DBItemJobStream.class.getSimpleName();
    private final SOSHibernateSession sosHibernateSession;

    public DBLayerJobStreams(SOSHibernateSession session) {
        this.sosHibernateSession = session;
    }

    public DBItemJobStream getJobStreamsDbItem(final Long id) throws SOSHibernateException {
        return (DBItemJobStream) sosHibernateSession.get(DBItemJobStream.class, id);
    }

    public FilterJobStreams resetFilter() {
        FilterJobStreams filter = new FilterJobStreams();
        filter.setJobStream("");
        filter.setStatus("");
        
        return filter;
    }

    private String getWhere(FilterJobStreams filter) {
        String where = "1=1";
        String and = " and ";

        if (filter.getSchedulerId() != null && !"".equals(filter.getSchedulerId())) {

            where += and + " schedulerId = :schedulerId";

            and = " and ";
        }

        if (filter.getStatus() != null && !"".equals(filter.getStatus())) {
            where += and + " status = :status";
            and = " and ";
        }

        if (filter.getJobStream() != null && !"".equals(filter.getJobStream())) {
            where += and + " jobStream  = :jobStream";
            and = " and ";
        }
        
        if (filter.getFolder() != null && !"".equals(filter.getFolder())) {
            where += and + " folder  = :folder";
            and = " and ";
        }

        if (!"".equals(where.trim())) {
            where = " where " + where;
        }
        return where;
    }

    private <T> Query<T> bindParameters(FilterJobStreams filter, Query<T> query) {
        if (filter.getSchedulerId() != null && !"".equals(filter.getSchedulerId())) {
            query.setParameter("schedulerId", filter.getSchedulerId());
        }
        if (filter.getJobStream() != null && !"".equals(filter.getJobStream())) {
            query.setParameter("jobStream", filter.getJobStream());
        }
        if (filter.getFolder() != null && !"".equals(filter.getFolder())) {
            query.setParameter("folder", filter.getFolder());
        }
        if (filter.getStatus() != null && !"".equals(filter.getStatus())) {
            query.setParameter("status", filter.getStatus());
        }

        return query;
    }

    public List<DBItemJobStream> getJobStreamsList(FilterJobStreams filter, final int limit) throws SOSHibernateException {
        String q = "  from " + DBItemJobStream + getWhere(filter);

        Query<DBItemJobStream> query = sosHibernateSession.createQuery(q);
        query = bindParameters(filter, query);

        if (limit > 0) {
            query.setMaxResults(limit);
        }
        return sosHibernateSession.getResultList(query);
    }

    public Integer deleteCascading(FilterJobStreams filter) throws SOSHibernateException {

        int row = 0;
        String hql = "";
        DBLayerJobStreamStarters dbLayerJobStreamStarters = new DBLayerJobStreamStarters(sosHibernateSession);
        DBLayerJobStreamParameters dbLayerJobStreamParameters = new DBLayerJobStreamParameters(sosHibernateSession);
        DBLayerJobStreamsStarterJobs dbLayerJobStreamsStarterJobs = new DBLayerJobStreamsStarterJobs(sosHibernateSession);

        List<DBItemJobStream> lJobStreams = getJobStreamsList(filter, 0);
        for (DBItemJobStream dbItemJobStream : lJobStreams) {
            FilterJobStreamStarters filterJobStreamStarters = new FilterJobStreamStarters();
            filterJobStreamStarters.setJobStreamId(dbItemJobStream.getId());
            List<DBItemJobStreamStarter> lStarters = dbLayerJobStreamStarters.getJobStreamStartersList(filterJobStreamStarters, 0);
           
            for (DBItemJobStreamStarter dbItemJobStreamStarter : lStarters) {
                FilterJobStreamStarterJobs filterJobStreamStarterJobs = new FilterJobStreamStarterJobs();
                filterJobStreamStarterJobs.setJobStreamStarter(dbItemJobStreamStarter.getId());
                dbLayerJobStreamsStarterJobs.deleteCascading(filterJobStreamStarterJobs);          
                }
            
            
            for (DBItemJobStreamStarter dbItemJobStreamStarter : lStarters) {
                FilterJobStreamParameters filterJobStreamParameters = new FilterJobStreamParameters();
                filterJobStreamParameters.setJobStreamStarterId(dbItemJobStreamStarter.getId());
                dbLayerJobStreamParameters.delete(filterJobStreamParameters);
              
            }
            dbLayerJobStreamStarters.delete(filterJobStreamStarters);
        }

        hql = "delete from " + DBItemJobStream + getWhere(filter);
        Query<DBItemJobStream> query = sosHibernateSession.createQuery(hql);
        query = bindParameters(filter, query);

        row = sosHibernateSession.executeUpdate(query);
        return row;
    }

    public Long store(DBItemJobStream jsJobStream) throws SOSHibernateException {
        FilterJobStreams filter = new FilterJobStreams();
        filter.setJobStream(jsJobStream.getJobStream());
        filter.setSchedulerId(jsJobStream.getSchedulerId());
        deleteCascading(filter);
        sosHibernateSession.save(jsJobStream);
        return jsJobStream.getId();

    }

}