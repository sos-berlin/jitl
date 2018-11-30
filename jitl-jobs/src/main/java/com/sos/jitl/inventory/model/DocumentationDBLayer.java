package com.sos.jitl.inventory.model;

import org.hibernate.query.Query;

import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.hibernate.exceptions.SOSHibernateException;
import com.sos.jitl.reporting.db.DBItemDocumentation;
import com.sos.jitl.reporting.db.DBItemDocumentationUsage;
import com.sos.jitl.reporting.db.DBLayer;

public class DocumentationDBLayer extends DBLayer {

    public DocumentationDBLayer(SOSHibernateSession connection) {
        super(connection);
    }

    public DBItemDocumentation getDocumentation(String schedulerId, String path) throws SOSHibernateException {
        StringBuilder sql = new StringBuilder();
        sql.append("from ").append(DBITEM_DOCUMENTATION);
        sql.append(" where schedulerId = :schedulerId");
        sql.append(" and path = :path");
        Query<DBItemDocumentation> query = getSession().createQuery(sql.toString());
        query.setParameter("schedulerId", schedulerId);
        query.setParameter("path", path);
        return getSession().getSingleResult(query);
    }

    public DBItemDocumentationUsage getDocumentationUsageForAssignment(String schedulerId, String path, String objectType)
            throws SOSHibernateException {
        StringBuilder hql = new StringBuilder();
        hql.append("from ").append(DBITEM_DOCUMENTATION_USAGE);
        hql.append(" where schedulerId = :schedulerId");
        hql.append(" and path = :path");
        hql.append(" and objectType = :objectType");
        Query<DBItemDocumentationUsage> query = getSession().createQuery(hql.toString());
        query.setParameter("schedulerId", schedulerId);
        query.setParameter("path", path);
        query.setParameter("objectType", objectType);
        return getSession().getSingleResult(query);
    }

}
