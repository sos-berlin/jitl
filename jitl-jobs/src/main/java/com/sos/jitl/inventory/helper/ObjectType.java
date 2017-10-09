
package com.sos.jitl.inventory.helper;

import java.util.HashMap;
import java.util.Map;

public enum ObjectType {

    JOB("JOB"),
    ORDER("ORDER"),
    SCHEDULE("SCHEDULE");
    private final String value;
    private final static Map<String, ObjectType> CONSTANTS = new HashMap<String, ObjectType>();

    static {
        for (ObjectType c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private ObjectType(String value) {
        this.value = value;
    }

    public String toString() {
        return this.value;
    }

    public static ObjectType fromValue(String value) {
        ObjectType constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}