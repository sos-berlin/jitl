package sos.scheduler.messaging;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import sos.scheduler.messaging.options.MessageConsumerOptions;

import com.sos.JSHelper.Basics.JSJobUtilitiesClass;
import com.sos.JSHelper.Listener.JSListenerClass;


public class TestMessageConsumer extends JSJobUtilitiesClass<MessageConsumerOptions> {

    private static final Logger LOGGER = Logger.getLogger(TestMessageConsumer.class);
    private static MessageConsumerJob consumerJob = null;
    private static MessageConsumerOptions consumerOptions = null;
    private static final String TEST_MESSAGE_HOST = "galadriel.sos";
    private static final String TEST_MESSAGE_PORT = "61616";
    private static final String TEST_MESSAGE_PROTOCOL = "tcp";
    private static final String TEST_MESSAGE_QUEUE_NAME = "JobChainQueue";
    private static final String TEST_MESSAGE = "<add_order job_chain='test-2-ssh-jobs/Two_ssh_jobs' at='now'>"
            + "<params>"
            + "<param name='host' value='homer.sos'/>"
            + "<param name='port' value='22'/>"
            + "<param name='user' value='test'/>"
            + "<param name='auth_method' value='password'/>"
            + "<param name='password' value='12345'/>"
            + "<param name='command' value='echo command send over MessageQueue!'/>"
            + "</params>"
            + "</add_order>";

    public TestMessageConsumer() {
        super(new MessageConsumerOptions());
        initializeClazz();
    }

    public static void initializeClazz() {
        consumerJob = new MessageConsumerJob();
        consumerOptions = consumerJob.getOptions();
        JSListenerClass.bolLogDebugInformation = true;
        JSListenerClass.intMaxDebugLevel = 9;
        if (!Logger.getRootLogger().getAllAppenders().hasMoreElements()) {
            BasicConfigurator.configure();
        }
        LOGGER.setLevel(Level.DEBUG);
    }

    @Before
    public void beforeMethode() {
        consumerJob.setJSJobUtilites(this);
    }

    @Test
    public void testReadFromQueue(){
        consumerOptions.setMessagingProtocol(TEST_MESSAGE_PROTOCOL);
        consumerOptions.setMessagingServerHostName(TEST_MESSAGE_HOST);
        consumerOptions.setMessagingServerPort(TEST_MESSAGE_PORT);
        consumerOptions.setMessagingQueueName(TEST_MESSAGE_QUEUE_NAME);
        try {
            consumerJob.execute();
            String receivedMessageXml = consumerJob.getMessageXml();
            assertTrue("Message received!", !receivedMessageXml.isEmpty());
            assertEquals("TEST_MESSAGE and received message are equal: ", TEST_MESSAGE, receivedMessageXml);
        } catch (Exception e) {
            LOGGER.error("Error executing JUnit ConsumerJob! ", e);
        }
    }
}
