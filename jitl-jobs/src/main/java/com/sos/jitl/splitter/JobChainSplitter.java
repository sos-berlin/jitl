package com.sos.jitl.splitter;

import static com.sos.scheduler.messages.JSMessages.JSJ_F_107;
import static com.sos.scheduler.messages.JSMessages.JSJ_I_110;
import static com.sos.scheduler.messages.JSMessages.JSJ_I_111;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Basics.JSJobUtilitiesClass;
import com.sos.JSHelper.Exceptions.JobSchedulerException;

/** \class JobChainSplitter - Workerclass for
 * "Start a parallel processing in a jobchain"
 *
 * \brief AdapterClass of JobChainSplitter for the SOSJobScheduler
 *
 * This Class JobChainSplitter is the worker-class.
 *
 * 
 *
 * see \see
 * C:\Users\KB\AppData\Local\Temp\scheduler_editor-121986169113382203.html for
 * (more) details.
 *
 * \verbatim ; mechanicaly created by
 * C:\ProgramData\sos-berlin.com\jobscheduler\
 * latestscheduler_4446\config\JOETemplates\java\xsl\JSJobDoc2JSWorkerClass.xsl
 * from http://www.sos-berlin.com at 20130315155436 \endverbatim */
public class JobChainSplitter extends JSJobUtilitiesClass<JobChainSplitterOptions> {

    private final String conClassName = "JobChainSplitter";
    private static Logger logger = Logger.getLogger(JobChainSplitter.class);

    /** \brief JobChainSplitter
     *
     * \details */
    public JobChainSplitter() {
        super(new JobChainSplitterOptions());
    }

    /** \brief Execute - Start the Execution of JobChainSplitter
     *
     * \details
     *
     * For more details see
     *
     * \see JobSchedulerAdapterClass \see JobChainSplitterMain
     *
     * \return JobChainSplitter
     *
     * @return */
    public JobChainSplitter Execute() throws Exception {
        final String conMethodName = conClassName + "::Execute";

        JSJ_I_110.toLog(conMethodName);

        try {
            Options().CheckMandatory();
            logger.debug(Options().toString());
        } catch (Exception e) {
            throw new JobSchedulerException(JSJ_F_107.get(conMethodName) + ":" + e.getMessage(), e);
        } finally {
        }

        JSJ_I_111.toLog(conMethodName);
        return this;
    }

    public void init() {
        @SuppressWarnings("unused")
        final String conMethodName = conClassName + "::init";
    }

} // class JobChainSplitter