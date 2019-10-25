package com.sos.jitl.eventhandler.plugin;

import static scala.collection.JavaConversions.mapAsJavaMap;

import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.exception.SOSNoResponseException;
import com.sos.jitl.eventhandler.handler.EventHandlerSettings;
import com.sos.jitl.eventhandler.handler.ILoopEventHandler;
import com.sos.jitl.eventhandler.plugin.notifier.Mailer;
import com.sos.jitl.eventhandler.plugin.notifier.Notifier;
import com.sos.scheduler.engine.kernel.Scheduler;
import com.sos.scheduler.engine.kernel.plugin.AbstractPlugin;
import com.sos.scheduler.engine.kernel.scheduler.SchedulerXmlCommandExecutor;
import com.sos.scheduler.engine.kernel.variable.VariableSet;

import sos.scheduler.job.JobSchedulerJob;
import sos.util.SOSString;

public class LoopEventHandlerPlugin extends AbstractPlugin {

    public static final String DUMMY_COMMAND =
            "<show_state subsystems=\"folder\" what=\"folders cluster no_subfolders\" path=\"/any/path/that/does/not/exists\" />";

    private static final Logger LOGGER = LoggerFactory.getLogger(LoopEventHandlerPlugin.class);
    private static final boolean isDebugEnabled = LOGGER.isDebugEnabled();
    private static final boolean isTraceEnabled = LOGGER.isTraceEnabled();

    private static final String CLASS_NAME = LoopEventHandlerPlugin.class.getSimpleName();
    // in seconds
    private long AWAIT_TERMINATION_TIMEOUT_EVENTHANDLER = 3;
    private long AWAIT_TERMINATION_TIMEOUT_PLUGIN = 30;

    // C++ thread
    private static List<ILoopEventHandler> activeHandlers = new ArrayList<>();

    private final Scheduler scheduler;
    private final SchedulerXmlCommandExecutor xmlCommandExecutor;
    private final VariableSet variableSet;

    private final ExecutorService threadPool;
    private String identifier;

    @SuppressWarnings("unused")
    private String schedulerParamProxyUrl;
    private String schedulerParamHibernateScheduler;
    private String schedulerParamHibernateReporting;

    private volatile Throwable exceptionOnPrepare;

    public LoopEventHandlerPlugin(Scheduler engineScheduler, SchedulerXmlCommandExecutor executor, VariableSet variables) {
        scheduler = engineScheduler;
        xmlCommandExecutor = executor;
        variableSet = variables;
        threadPool = Executors.newSingleThreadExecutor();
    }

    /** C++ thread */
    public void onPrepare(ILoopEventHandler eventHandler) {
        String method = getMethodName("onPrepare");

        readJobSchedulerVariables();

        Runnable thread = new Runnable() {

            @Override
            public void run() {
                String name = Thread.currentThread().getName();
                LOGGER.info(String.format("%s[run][thread]%s", method, name));
                try {
                    eventHandler.onPrepare(getSettings());
                } catch (Throwable e) {
                    LOGGER.error(String.format("%s[exception]%s", method, e.toString()), e);
                    exceptionOnPrepare = e;
                }
                LOGGER.info(String.format("%s[end][thread]%s", method, name));
            }
        };
        threadPool.submit(thread);

        super.onPrepare();
    }

    /** C++ thread */
    public void onActivate(ILoopEventHandler eventHandler) {
        String method = getMethodName("onActivate");

        activeHandlers.add(eventHandler);
        if (eventHandler.getSettings() == null) {
            readJobSchedulerVariables();
        }

        Mailer mailer = getMailer();
        Class<?> clazz = this.getClass();
        Runnable thread = new Runnable() {

            @Override
            public void run() {
                String name = Thread.currentThread().getName();
                LOGGER.info(String.format("%s[run][thread]%s", method, name));
                try {
                    if (exceptionOnPrepare != null) {
                        mailer.sendOnError(CLASS_NAME, "onPrepare", exceptionOnPrepare);
                        exceptionOnPrepare = null;
                    }
                    if (eventHandler.getSettings() == null) {
                        eventHandler.setSettings(getSettings());
                    }
                    eventHandler.onActivate(new Notifier(mailer, clazz));
                } catch (Throwable e) {
                    LOGGER.error(String.format("%s[exception]%s", method, e.toString()), e);
                    mailer.sendOnError(CLASS_NAME, "onActivate", e);
                }
                LOGGER.info(String.format("%s[end][thread]%s", method, name));
            }
        };
        threadPool.submit(thread);

        super.onActivate();
    }

    /** C++ thread */
    public void close() {
        String method = getMethodName("close");
        LOGGER.debug(method);

        closeEventHandlers();
        shutdownThreadPool(method, threadPool, AWAIT_TERMINATION_TIMEOUT_PLUGIN);
        super.close();
    }

    /** C++ thread */
    private Mailer getMailer() {
        // available only on onActivate
        Map<String, String> mailDefaults = mapAsJavaMap(scheduler.mailDefaults());
        Mailer mailer = new Mailer(identifier, mailDefaults);
        if (isDebugEnabled) {
            LOGGER.debug(String.format("%s[mailDefaults]%s", getMethodName("onActivate"), mailDefaults));
        }
        return mailer;
    }

    /** C++ thread */
    private void closeEventHandlers() {
        String method = getMethodName("closeEventHandlers");

        int size = activeHandlers.size();
        if (size > 0) {
            // closes http client on all plugins/event handlers
            ExecutorService threadPool = Executors.newFixedThreadPool(size);
            for (int i = 0; i < size; i++) {
                ILoopEventHandler eh = activeHandlers.get(i);
                Runnable thread = new Runnable() {

                    @Override
                    public void run() {
                        String name = Thread.currentThread().getName();
                        if (isDebugEnabled) {
                            LOGGER.debug(String.format("%s[%s][run][thread]%s", method, eh.getIdentifier(), name));
                        }
                        eh.close();
                        if (isDebugEnabled) {
                            LOGGER.debug(String.format("%s[%s][end][thread]%s", method, eh.getIdentifier(), name));
                        }
                    }
                };
                threadPool.submit(thread);
            }
            shutdownThreadPool(method, threadPool, AWAIT_TERMINATION_TIMEOUT_EVENTHANDLER);
            activeHandlers = new ArrayList<>();
        } else {
            if (isDebugEnabled) {
                LOGGER.debug(String.format("%s[skip]already closed", method));
            }
        }
    }

    /** C++ thread */
    private void shutdownThreadPool(String callerMethod, ExecutorService threadPool, long awaitTerminationTimeout) {
        try {
            threadPool.shutdown();
            // threadPool.shutdownNow();
            boolean shutdown = threadPool.awaitTermination(awaitTerminationTimeout, TimeUnit.SECONDS);
            if (isDebugEnabled) {
                if (shutdown) {
                    LOGGER.debug(String.format("%sthread has been shut down correctly", callerMethod));
                } else {
                    LOGGER.debug(String.format("%sthread has ended due to timeout of %ss on shutdown", callerMethod, awaitTerminationTimeout));
                }
            }
        } catch (InterruptedException e) {
            LOGGER.error(String.format("%s[exception]%s", callerMethod, e.toString()), e);
        }
    }

    /** JAVA thread */
    private EventHandlerSettings getSettings() throws Exception {
        String method = getMethodName("getSettings");

        if (xmlCommandExecutor == null) {
            throw new Exception("xmlCommandExecutor is null");
        }
        EventHandlerSettings settings = new EventHandlerSettings();
        for (int i = 0; i < 120; i++) {
            try {
                String answer = xmlCommandExecutor.executeXml(DUMMY_COMMAND);
                if (!SOSString.isEmpty(answer)) {
                    settings.setSchedulerAnswer(answer);
                    String state = settings.getSchedulerAnswer("/spooler/answer/state/@state");
                    if (isTraceEnabled) {
                        LOGGER.trace(String.format("%s[%s][state]%s", method, i, state));
                    }
                    if ("loading,running,waiting_for_activation,paused".contains(state)) {
                        break;
                    }
                }
            } catch (Throwable e) {
                LOGGER.error(String.format("%s[exception]%s", method, e.toString()), e);
            } finally {
                try {
                    Thread.sleep(1_000);
                } catch (Throwable e) {
                    LOGGER.error(String.format("%s[exception]%s", method, e.toString()), e);
                }
            }
        }

        if (SOSString.isEmpty(settings.getSchedulerAnswer())) {
            throw new SOSNoResponseException(String.format("%s[error]missing JobScheduler answer", method));
        }

        if (isTraceEnabled) {
            LOGGER.trace(String.format("%s[answer]%s", method, settings.getSchedulerAnswer()));
        }

        settings.setSchedulerXml(Paths.get(settings.getSchedulerAnswer("/spooler/answer/state/@config_file")));
        if (settings.getSchedulerXml() == null || !Files.exists(settings.getSchedulerXml())) {
            throw new FileNotFoundException(String.format("not found settings.xml file %s", settings.getSchedulerXml()));
        }
        settings.setConfigDirectory(settings.getSchedulerXml().getParent());

        settings.setHibernateConfigurationScheduler(getHibernateConfigurationScheduler(settings.getConfigDirectory()));
        settings.setHibernateConfigurationReporting(getHibernateConfigurationReporting(settings.getConfigDirectory(), settings
                .getHibernateConfigurationScheduler()));

        if (isDebugEnabled) {
            LOGGER.debug(String.format("%s[scheduler=%s][reporting=%s]", method, settings.getHibernateConfigurationScheduler(), settings
                    .getHibernateConfigurationReporting()));
        }

        settings.setLiveDirectory(settings.getConfigDirectory().resolve("live"));
        settings.setSchedulerId(settings.getSchedulerAnswer("/spooler/answer/state/@spooler_id"));
        settings.setHost(settings.getSchedulerAnswer("/spooler/answer/state/@host"));
        HostPort http = new HostPort(settings.getSchedulerAnswer("/spooler/answer/state/@http_port", "40444"));
        settings.setHttpHost(http.getHost());
        settings.setHttpPort(http.getPort());
        HostPort https = new HostPort(settings.getSchedulerAnswer("/spooler/answer/state/@https_port"));
        settings.setHttpsHost(https.getHost());
        settings.setHttpsPort(https.getPort());
        settings.setTcpPort(settings.getSchedulerAnswer("/spooler/answer/state/@tcp_port"));
        settings.setUdpPort(settings.getSchedulerAnswer("/spooler/answer/state/@udp_port"));
        settings.setRunningSince(settings.getSchedulerAnswer("/spooler/answer/state/@spooler_running_since"));
        settings.setTime(settings.getSchedulerAnswer("/spooler/answer/state/@time"));
        settings.setTimezone(settings.getSchedulerAnswer("/spooler/answer/state/@time_zone"));
        settings.setVersion(settings.getSchedulerAnswer("/spooler/answer/state/@version"));
        settings.setState(settings.getSchedulerAnswer("/spooler/answer/state/@state"));

        if (SOSString.isEmpty(settings.getSchedulerId())) {
            throw new Exception(String.format("%s missing @spooler_id in the scheduler answer", method));
        }
        if (SOSString.isEmpty(settings.getHost())) {
            throw new Exception(String.format("%s missing @host in the scheduler answer", method));
        }
        if (SOSString.isEmpty(settings.getHttpPort())) {
            throw new Exception(String.format("%s missing @http_port in the scheduler answer", method));
        }
        return settings;
    }

    /** JAVA thread */
    private Path getHibernateConfigurationScheduler(Path configDirectory) throws Exception {
        String method = getMethodName("getHibernateConfigurationScheduler");

        Path file = null;
        if (SOSString.isEmpty(schedulerParamHibernateScheduler)) {
            if (isTraceEnabled) {
                LOGGER.trace(String.format("%s[%s is null or empty]use %s", method, JobSchedulerJob.SCHEDULER_PARAM_HIBERNATE_SCHEDULER,
                        JobSchedulerJob.HIBERNATE_DEFAULT_FILE_NAME_SCHEDULER));
            }
            file = configDirectory.resolve(JobSchedulerJob.HIBERNATE_DEFAULT_FILE_NAME_SCHEDULER);
        } else {
            if (isTraceEnabled) {
                LOGGER.trace(String.format("%s[%s]use %s", method, JobSchedulerJob.SCHEDULER_PARAM_HIBERNATE_SCHEDULER,
                        schedulerParamHibernateScheduler));
            }
            file = Paths.get(schedulerParamHibernateScheduler);
        }
        if (!Files.exists(file)) {
            throw new FileNotFoundException(String.format("%s not found scheduler hibernate file %s", method, file.toString()));
        }
        return file;
    }

    /** JAVA thread */
    private Path getHibernateConfigurationReporting(Path configDirectory, Path hibernateScheduler) throws Exception {
        String method = getMethodName("getHibernateConfigurationReporting");

        Path file = null;
        if (SOSString.isEmpty(schedulerParamHibernateReporting)) {
            if (isTraceEnabled) {
                LOGGER.trace(String.format("%s[%s is null or empty]use %s", method, JobSchedulerJob.SCHEDULER_PARAM_HIBERNATE_REPORTING,
                        JobSchedulerJob.HIBERNATE_DEFAULT_FILE_NAME_REPORTING));
            }
            file = configDirectory.resolve(JobSchedulerJob.HIBERNATE_DEFAULT_FILE_NAME_REPORTING);

            if (!Files.exists(file)) {
                if (isTraceEnabled) {
                    LOGGER.trace(String.format("%s[%s not founded]use %s", method, JobSchedulerJob.SCHEDULER_PARAM_HIBERNATE_REPORTING,
                            hibernateScheduler));
                }
                file = hibernateScheduler;
            }
        } else {
            if (isTraceEnabled) {
                LOGGER.trace(String.format("%s[%s]use %s", method, JobSchedulerJob.SCHEDULER_PARAM_HIBERNATE_REPORTING,
                        schedulerParamHibernateReporting));
            }
            file = Paths.get(schedulerParamHibernateReporting);

            if (!Files.exists(file)) {
                throw new FileNotFoundException(String.format("%s not found reporting hibernate file %s", method, file.toString()));
            }
        }
        return file;
    }

    /** C++ thread */
    private void readJobSchedulerVariables() {
        schedulerParamProxyUrl = getJobSchedulerVariable(JobSchedulerJob.SCHEDULER_PARAM_PROXY_URL);
        schedulerParamHibernateScheduler = getJobSchedulerVariable(JobSchedulerJob.SCHEDULER_PARAM_HIBERNATE_SCHEDULER);
        schedulerParamHibernateReporting = getJobSchedulerVariable(JobSchedulerJob.SCHEDULER_PARAM_HIBERNATE_REPORTING);
    }

    /** C++ thread */
    public String getJobSchedulerVariable(String name) {
        String val = variableSet.apply(name);
        if (val != null && !val.isEmpty()) {
            return val;
        }
        return null;
    }

    private String getMethodName(String name) {
        String prefix = identifier == null ? "" : String.format("[%s]", identifier);
        return String.format("%s[%s]", prefix, name);
    }

    public void setIdentifier(String val) {
        identifier = val;
    }

    public String getIdentifier() {
        return identifier;
    }

    private class HostPort {

        private String host;
        private String port;

        /** samples: hostAdnPort = 40444, host.sos:40444, 0.0.0.0:40444, ... */
        public HostPort(String hostAdnPort) {
            if (hostAdnPort != null) {
                host = "localhost";
                String[] arr = hostAdnPort.split(":");
                switch (arr.length) {
                case 1:
                    port = hostAdnPort;
                    break;
                default:
                    if (!arr[0].equalsIgnoreCase(host) && !arr[0].equals("0.0.0.0")) {
                        host = arr[0];
                    }
                    port = arr[1];
                }
            }
        }

        public String getHost() {
            return host;
        }

        public String getPort() {
            return port;
        }
    }
}