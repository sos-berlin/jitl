package com.sos.jitl.classes.plugin;

import static scala.collection.JavaConversions.mapAsJavaMap;

import java.io.FileNotFoundException;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.exception.SOSInvalidDataException;
import com.sos.exception.SOSNoResponseException;
import com.sos.jitl.classes.event.EventHandlerSettings;
import com.sos.jitl.classes.event.IJobSchedulerPluginEventHandler;
import com.sos.scheduler.engine.kernel.Scheduler;
import com.sos.scheduler.engine.kernel.plugin.AbstractPlugin;
import com.sos.scheduler.engine.kernel.scheduler.SchedulerXmlCommandExecutor;
import com.sos.scheduler.engine.kernel.variable.VariableSet;

import sos.scheduler.job.JobSchedulerJob;
import sos.util.SOSString;

public class JobSchedulerEventPlugin extends AbstractPlugin {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobSchedulerEventPlugin.class);

    public static final String DUMMY_COMMAND =
            "<show_state subsystems=\"folder\" what=\"folders cluster no_subfolders\" path=\"/any/path/that/does/not/exists\" />";
    private final String className = JobSchedulerEventPlugin.class.getSimpleName();

    private final Scheduler scheduler;
    private final SchedulerXmlCommandExecutor xmlCommandExecutor;
    private final VariableSet variableSet;

    private final ExecutorService threadPool;
    private String identifier;

    private String schedulerParamProxyUrl;
    private String schedulerParamHibernateScheduler;
    private String schedulerParamHibernateReporting;

    private volatile boolean hasErrorOnPrepare = false;

    public JobSchedulerEventPlugin(Scheduler scheduler, SchedulerXmlCommandExecutor executor, VariableSet variables) {
        this.scheduler = scheduler;
        this.xmlCommandExecutor = executor;
        this.variableSet = variables;
        this.threadPool = Executors.newSingleThreadExecutor();
    }

    public void executeOnPrepare(IJobSchedulerPluginEventHandler eventHandler) {
        String method = getMethodName("executeOnPrepare");

        readJobSchedulerVariables();

        Runnable thread = new Runnable() {

            @Override
            public void run() {
                String name = Thread.currentThread().getName();
                LOGGER.info(String.format("%s: run thread %s", method, name));
                try {
                    eventHandler.onPrepare(getSettings());
                    hasErrorOnPrepare = false;
                } catch (Exception e) {
                    LOGGER.error(String.format("%s: %s", method, e.toString()), e);
                    hasErrorOnPrepare = true;
                } catch (Throwable te) {
                    LOGGER.error(String.format("%s: Throwable %s", method, te.toString()), te);
                    hasErrorOnPrepare = true;
                    throw te;
                }
                LOGGER.info(String.format("%s: end thread %s", method, name));
            }
        };
        threadPool.submit(thread);
        super.onPrepare();
    }

    public void executeOnActivate(IJobSchedulerPluginEventHandler eventHandler) {
        String method = getMethodName("executeOnActivate");

        if (eventHandler.getSettings() == null) {
            readJobSchedulerVariables();
        }
        Map<String, String> mailDefaults = mapAsJavaMap(scheduler.mailDefaults());

        Runnable thread = new Runnable() {

            @Override
            public void run() {
                String name = Thread.currentThread().getName();
                LOGGER.info(String.format("%s: run thread %s", method, name));

                PluginMailer mailer = new PluginMailer(identifier, mailDefaults);
                try {
                    if (hasErrorOnPrepare) {
                        String msg = "skip due executeOnPrepare errors";
                        LOGGER.error(String.format("%s: %s", method, msg));
                        mailer.sendOnError(className, method, String.format("%s: %s", method, msg));
                    } else {
                        if (eventHandler.getSettings() == null) {
                            eventHandler.setSettings(getSettings());
                        }
                        eventHandler.onActivate(mailer);
                    }
                } catch (Exception e) {
                    LOGGER.error(String.format("%s: %s", method, e.toString()), e);
                    mailer.sendOnError(className, method, e);
                } catch (Throwable te) {
                    LOGGER.error(String.format("%s: Throwable %s", method, te.toString()), te);
                    mailer.sendOnError(className, method, te);
                    throw te;
                }
                LOGGER.info(String.format("%s: end thread %s", method, name));
            }
        };
        threadPool.submit(thread);

        super.onActivate();
    }

    public void executeClose(IJobSchedulerPluginEventHandler eventHandler) {
        String method = getMethodName("executeClose");
        LOGGER.info(String.format("%s", method));

        eventHandler.close();
        eventHandler.awaitEnd();

        try {
            threadPool.shutdownNow();
            boolean shutdown = threadPool.awaitTermination(1L, TimeUnit.SECONDS);
            if (shutdown) {
                LOGGER.debug(String.format("%s: thread has been shut down correctly", method));
            } else {
                LOGGER.debug(String.format("%s: thread has ended due to timeout on shutdown. doesn´t wait for answer from thread", method));
            }
        } catch (InterruptedException e) {
            LOGGER.error(String.format("%s: %s", method, e.toString()), e);
        }

        super.close();
    }

    private EventHandlerSettings getSettings() throws Exception {
        String method = getMethodName("getSettings");

        EventHandlerSettings settings = new EventHandlerSettings();
        for (int i = 0; i < 120; i++) {
            try {
                String answer = executeXML(DUMMY_COMMAND);
                if (!SOSString.isEmpty(answer)) {
                    settings.setSchedulerAnswer(answer);
                    String state = settings.getSchedulerAnswer("/spooler/answer/state/@state");
                    if ("running,waiting_for_activation,paused".contains(state)) {
                        break;
                    }
                }
                Thread.sleep(1_000);
            } catch (Exception e) {
                LOGGER.error(String.format("%s: %s", method, e.toString()), e);
                Thread.sleep(1_000);
            }
        }

        if (SOSString.isEmpty(settings.getSchedulerAnswer())) {
            throw new SOSNoResponseException(String.format("%s: missing JobScheduler answer", method));
        }
        LOGGER.debug(String.format("%s: xml=%s", method, settings.getSchedulerAnswer()));

        settings.setSchedulerXml(Paths.get(settings.getSchedulerAnswer("/spooler/answer/state/@config_file")));
        if (settings.getSchedulerXml() == null || !Files.exists(settings.getSchedulerXml())) {
            throw new FileNotFoundException(String.format("not found settings.xml file %s", settings.getSchedulerXml()));
        }
        settings.setConfigDirectory(settings.getSchedulerXml().getParent());

        settings.setHibernateConfigurationScheduler(getHibernateConfigurationScheduler(settings.getConfigDirectory()));
        settings.setHibernateConfigurationReporting(getHibernateConfigurationReporting(settings.getConfigDirectory(), settings
                .getHibernateConfigurationScheduler()));

        LOGGER.debug(String.format("%s: hibernateScheduler=%s, hibernateReporting=%s", method, settings.getHibernateConfigurationScheduler(), settings
                .getHibernateConfigurationReporting()));

        settings.setLiveDirectory(settings.getConfigDirectory().resolve("live"));
        settings.setSchedulerId(settings.getSchedulerAnswer("/spooler/answer/state/@spooler_id"));
        settings.setHost(settings.getSchedulerAnswer("/spooler/answer/state/@host"));
        settings.setHttpPort(settings.getSchedulerAnswer("/spooler/answer/state/@http_port", "40444"));
        settings.setHttpsPort(settings.getSchedulerAnswer("/spooler/answer/state/@https_port"));
        settings.setTcpPort(settings.getSchedulerAnswer("/spooler/answer/state/@tcp_port"));
        settings.setUdpPort(settings.getSchedulerAnswer("/spooler/answer/state/@udp_port"));
        settings.setRunningSince(settings.getSchedulerAnswer("/spooler/answer/state/@spooler_running_since"));
        settings.setTime(settings.getSchedulerAnswer("/spooler/answer/state/@time"));
        settings.setTimezone(settings.getSchedulerAnswer("/spooler/answer/state/@time_zone"));
        settings.setVersion(settings.getSchedulerAnswer("/spooler/answer/state/@version"));
        settings.setState(settings.getSchedulerAnswer("/spooler/answer/state/@state"));

        if (SOSString.isEmpty(settings.getSchedulerId())) {
            throw new Exception(String.format("%s: missing @spooler_id in the scheduler answer", method));
        }
        if (SOSString.isEmpty(settings.getHost())) {
            throw new Exception(String.format("%s: missing @host in the scheduler answer", method));
        }
        if (SOSString.isEmpty(settings.getHttpPort())) {
            throw new Exception(String.format("%s: missing @http_port in the scheduler answer", method));
        }
        try {
            settings.setMasterUrl(getMasterUrl(settings.getHttpPort()));
        } catch (Exception e) {
            throw new SOSInvalidDataException(String.format("%s: couldn't determine JobScheduler http url %s", method, e.toString()), e);
        }
        return settings;
    }

    private Path getHibernateConfigurationScheduler(Path configDirectory) throws Exception {
        String method = getMethodName("getHibernateConfigurationScheduler");

        Path file = null;
        if (SOSString.isEmpty(this.schedulerParamHibernateScheduler)) {
            LOGGER.debug(String.format("%s: not found scheduler variable '%s'. search for default schedulerHibernate %s", method,
                    JobSchedulerJob.SCHEDULER_PARAM_HIBERNATE_SCHEDULER, JobSchedulerJob.HIBERNATE_DEFAULT_FILE_NAME_SCHEDULER));
            file = configDirectory.resolve(JobSchedulerJob.HIBERNATE_DEFAULT_FILE_NAME_SCHEDULER);
        } else {
            LOGGER.debug(String.format("%s: found scheduler variable '%s'=%s", method, JobSchedulerJob.SCHEDULER_PARAM_HIBERNATE_SCHEDULER,
                    this.schedulerParamHibernateScheduler));
            file = Paths.get(this.schedulerParamHibernateScheduler);
        }
        if (!Files.exists(file)) {
            throw new FileNotFoundException(String.format("%s: not found hibernateScheduler file %s", method, file.toString()));
        }
        return file;
    }

    private Path getHibernateConfigurationReporting(Path configDirectory, Path hibernateScheduler) throws Exception {
        String method = getMethodName("getHibernateConfigurationReporting");

        Path file = null;
        if (SOSString.isEmpty(this.schedulerParamHibernateReporting)) {
            LOGGER.debug(String.format("%s: not found scheduler variable '%s'. search for default reportingHibernate %s", method,
                    JobSchedulerJob.SCHEDULER_PARAM_HIBERNATE_REPORTING, JobSchedulerJob.HIBERNATE_DEFAULT_FILE_NAME_REPORTING));
            file = configDirectory.resolve(JobSchedulerJob.HIBERNATE_DEFAULT_FILE_NAME_REPORTING);

            if (!Files.exists(file)) {
                LOGGER.debug(String.format("%s: not foud default reportingHibernate %s. set reportingHibernate = schedulerHibernate", method,
                        JobSchedulerJob.SCHEDULER_PARAM_HIBERNATE_REPORTING));
                file = hibernateScheduler;
            }
        } else {
            LOGGER.debug(String.format("%s: found scheduler variable '%s'=%s", method, JobSchedulerJob.SCHEDULER_PARAM_HIBERNATE_REPORTING,
                    this.schedulerParamHibernateReporting));
            file = Paths.get(this.schedulerParamHibernateReporting);

            if (!Files.exists(file)) {
                throw new FileNotFoundException(String.format("%s: not found hibernateReporting file %s", method, file.toString()));
            }
        }
        return file;
    }

    private String executeXML(String xmlCommand) {
        try {
            if (xmlCommandExecutor != null) {
                return xmlCommandExecutor.executeXml(xmlCommand);
            } else {
                LOGGER.error("xmlCommandExecutor is null");
            }
        } catch (Exception e) {
        }
        return null;
    }

    private void readJobSchedulerVariables() {
        this.schedulerParamProxyUrl = getJobSchedulerVariable(JobSchedulerJob.SCHEDULER_PARAM_PROXY_URL);
        this.schedulerParamHibernateScheduler = getJobSchedulerVariable(JobSchedulerJob.SCHEDULER_PARAM_HIBERNATE_SCHEDULER);
        this.schedulerParamHibernateReporting = getJobSchedulerVariable(JobSchedulerJob.SCHEDULER_PARAM_HIBERNATE_REPORTING);
    }

    public String getJobSchedulerVariable(String name) {
        if (variableSet.apply(name) != null && !variableSet.apply(name).isEmpty()) {
            return variableSet.apply(name);
        }
        return null;
    }

    private String getMasterUrl(String httpPort) throws Exception {
        if (schedulerParamProxyUrl != null) {
            return schedulerParamProxyUrl;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("http://");
        sb.append(InetAddress.getLocalHost().getCanonicalHostName().toLowerCase());
        sb.append(":");
        sb.append(httpPort);
        return sb.toString();
    }

    private String getMethodName(String name) {
        String prefix = this.identifier == null ? "" : String.format("[%s] ", this.identifier);
        return String.format("%s%s", prefix, name);
    }

    public void setIdentifier(String val) {
        this.identifier = val;
    }

    public String getIdentifier() {
        return this.identifier;
    }

}