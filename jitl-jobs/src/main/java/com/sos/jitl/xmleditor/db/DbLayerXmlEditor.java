package com.sos.jitl.xmleditor.db;

import java.util.List;
import java.util.Map;

import org.hibernate.query.Query;

import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.jitl.reporting.db.DBItemXmlEditorObject;
import com.sos.jitl.reporting.db.DBLayer;
import com.sos.joc.model.xmleditor.common.ObjectType;

import sos.util.SOSString;

public class DbLayerXmlEditor extends DBLayer {

    public DbLayerXmlEditor(SOSHibernateSession session) {
        super(session);
    }

    public boolean deleteOtherObject(Long id) throws Exception {
        // JOC uses autoCommit=true, executeUpdate is not supported
        StringBuilder hql = new StringBuilder("from ").append(DBITEM_XML_EDITOR_OBJECTS).append(" ");
        hql.append("where id=:id ");
        hql.append("and objectType=:objectType");
        Query<DBItemXmlEditorObject> query = getSession().createQuery(hql.toString());
        query.setParameter("id", id);
        query.setParameter("objectType", ObjectType.OTHER.name());

        DBItemXmlEditorObject item = getSession().getSingleResult(query);
        if (item != null) {
            getSession().delete(item);
            return true;
        }
        return false;
    }

    public DBItemXmlEditorObject getObject(Long id) throws Exception {
        StringBuilder hql = new StringBuilder("from ").append(DBITEM_XML_EDITOR_OBJECTS).append(" ");
        hql.append("where id=:id");

        Query<DBItemXmlEditorObject> query = getSession().createQuery(hql.toString());
        query.setParameter("id", id);
        return getSession().getSingleResult(query);
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

    public List<Map<String, Object>> getObjectProperties(String schedulerId, String objectType, String properties, String orderBy) throws Exception {
        StringBuilder hql = new StringBuilder("select new map(").append(properties).append(") from ").append(DBITEM_XML_EDITOR_OBJECTS).append(" ");
        hql.append("where schedulerId=:schedulerId ");
        hql.append("and objectType=:objectType ");
        if (!SOSString.isEmpty(orderBy)) {
            hql.append(orderBy);
        }

        Query<Map<String, Object>> query = getSession().createQuery(hql.toString());
        query.setParameter("schedulerId", schedulerId);
        query.setParameter("objectType", objectType);
        return getSession().getResultList(query);
    }

}
