package com.sos.jitl.xmleditor.db;

import org.hibernate.query.Query;

import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.jitl.reporting.db.DBItemXmlEditorObject;
import com.sos.jitl.reporting.db.DBLayer;

public class DbLayerXmlEditor extends DBLayer {

    public DbLayerXmlEditor(SOSHibernateSession session) {
        super(session);
    }

    public DBItemXmlEditorObject getObject(String schedulerId, String objectType, String name) throws Exception {
        StringBuilder hql = new StringBuilder("from ").append(DBITEM_XML_EDITOR_OBJECTS).append(" ");
        hql.append("where schedulerId=:schedulerId ");
        hql.append("and objectType=:objectType ");
        hql.append("and name=:name");

        Query<DBItemXmlEditorObject> query = getSession().createQuery(hql.toString());
        query.setParameter("schedulerId", schedulerId);
        query.setParameter("objectType", objectType);
        query.setParameter("name", name);

        return getSession().getSingleResult(query);
    }

}
