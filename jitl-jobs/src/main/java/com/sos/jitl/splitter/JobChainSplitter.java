package com.sos.jitl.splitter;

import static com.sos.scheduler.messages.JSMessages.JSJ_F_107;
import static com.sos.scheduler.messages.JSMessages.JSJ_I_110;
import static com.sos.scheduler.messages.JSMessages.JSJ_I_111;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Basics.JSJobUtilitiesClass;
import com.sos.JSHelper.Exceptions.JobSchedulerException;

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
            getOptions().CheckMandatory();
            logger.debug(getOptions().toString());
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