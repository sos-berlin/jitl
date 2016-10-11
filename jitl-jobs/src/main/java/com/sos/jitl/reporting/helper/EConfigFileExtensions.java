package com.sos.jitl.reporting.helper;

public enum EConfigFileExtensions {
    ORDER("order", ".order.xml"), JOB_CHAIN("job_chain", ".job_chain.xml"), JOB("job", ".job.xml"), LOCK("lock", ".lock.xml"),
    PROCESS_CLASS("process_class", ".process_class.xml"), SCHEDULE("schedule", ".schedule.xml");

    private String type;
    private String extension;

    private EConfigFileExtensions(String configType, String configExtension) {
        type = configType;
        extension = configExtension;
    }

    public String extension() {
        return extension;
    }

    public String type() {
        return type;
    }
}
