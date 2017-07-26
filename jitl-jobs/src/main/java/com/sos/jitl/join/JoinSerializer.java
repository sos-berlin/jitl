package com.sos.jitl.join;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class JoinSerializer {

    private JoinOrderList joinOrderList;
    private static final Logger LOGGER = LoggerFactory.getLogger(JoinSerializer.class);

    public JoinSerializer(String serializedObject) throws ClassNotFoundException, IOException {
        super();
        if (serializedObject == null || "".equals(serializedObject.trim())) {
            LOGGER.debug("new joinOrderList");
            joinOrderList = new JoinOrderList();
        } else {
            LOGGER.debug("joinOrderList from String");
            joinOrderList = (JoinOrderList) fromString(serializedObject);
        }
    }

    private Object fromString(String s) throws IOException, ClassNotFoundException {
        byte[] data = Base64.getDecoder().decode(s);
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
        Object o = ois.readObject();
        ois.close();
        return o;
    }

    private String object2toString(Serializable o) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(o);
        oos.close();
        return Base64.getEncoder().encodeToString(baos.toByteArray());
    }

    
    public JoinOrderList getJoinOrderList() {
        return joinOrderList;
    }

    public String getSerializedObject() throws IOException {
        return object2toString(joinOrderList);
    }

    public void addOrder(JoinOrder joinOrder) {
        joinOrderList.addOrder(joinOrder);
        
    }

    public void reset(JoinOrder joinOrder) {
        joinOrderList.reset(joinOrder);
        
    }

    public void showJoinOrderList(JoinOrder joinOrder) {
        joinOrderList.showJoinOrderList(joinOrder);
    }

}
