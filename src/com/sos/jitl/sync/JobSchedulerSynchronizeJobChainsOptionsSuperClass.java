package com.sos.jitl.sync;

import java.util.HashMap;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Annotations.JSOptionDefinition;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;
import com.sos.JSHelper.Options.JSOptionsClass;
import com.sos.JSHelper.Options.SOSOptionInteger;
import com.sos.JSHelper.Options.SOSOptionString;

/**
 * \class 		JobSchedulerSynchronizeJobChainsOptionsSuperClass - Synchronize Job Chains
 *
 * \brief
 * An Options-Super-Class with all Options. This Class will be extended by the "real" Options-class (\see JobSchedulerSynchronizeJobChainsOptions.
 * The "real" Option class will hold all the things, which are normaly overwritten at a new generation
 * of the super-class.
 *
 *

 *
 *
 * \verbatim ;
 * mechanicaly created by C:\ProgramData\sos-berlin.com\jobscheduler\scheduler_ur\config\JOETemplates\java\xsl\JSJobDoc2JSOptionSuperClass.xsl from http://www.sos-berlin.com at 20121218120331
 * \endverbatim
 * \section OptionsTable Tabelle der vorhandenen Optionen
 *
 * Tabelle mit allen Optionen
 *
 * MethodName
 * Title
 * Setting
 * Description
 * IsMandatory
 * DataType
 * InitialValue
 * TestValue
 *
 *
 *
 * \section TestData Eine Hilfe zum Erzeugen einer HashMap mit Testdaten
 *
 * Die folgenden Methode kann verwendet werden, um für einen Test eine HashMap
 * mit sinnvollen Werten für die einzelnen Optionen zu erzeugen.
 *
 * \verbatim
 private HashMap <String, String> SetJobSchedulerSSHJobOptions (HashMap <String, String> pobjHM) {
	pobjHM.put ("		JobSchedulerSynchronizeJobChainsOptionsSuperClass.auth_file", "test");  // This parameter specifies the path and name of a user's pr
		return pobjHM;
  }  //  private void SetJobSchedulerSSHJobOptions (HashMap <String, String> pobjHM)
 * \endverbatim
 */
@JSOptionClass(name = "JobSchedulerSynchronizeJobChainsOptionsSuperClass", description = "JobSchedulerSynchronizeJobChainsOptionsSuperClass")
public class JobSchedulerSynchronizeJobChainsOptionsSuperClass extends JSOptionsClass {
    /**
     *
     */
    private static final long serialVersionUID          = 2529720765502955118L;
    private final String      conClassName              = "JobSchedulerSynchronizeJobChainsOptionsSuperClass";
    @SuppressWarnings("unused")
    private static Logger     logger                    = Logger.getLogger(JobSchedulerSynchronizeJobChainsOptionsSuperClass.class);

    /**
     * \var job_chain_required_orders :
     * This parameter specifies the number of orders that are required to be present for a job chain to make orders proceed. Without specification one order is expected to be present. If e.g. three orders are specified then three orders from that chain have to be present and these three orders will simultaneously proceed having matched the synchronization criteria. The name of this parameter is created from the name of the respective job chain and the suffix _required_orders .
     *
     */
    @JSOptionDefinition(name = "job_chain_required_orders", description = "", key = "job_chain_required_orders", type = "SOSOptionInteger", mandatory = false)
    public SOSOptionInteger   job_chain_required_orders = new SOSOptionInteger(this, conClassName + ".job_chain_required_orders", // HashMap-Key
                                                                "", // Titel
                                                                "1", // InitValue
                                                                "1", // DefaultValue
                                                                false // isMandatory
                                                        );

    /**
     * \brief getjob_chain_required_orders :
     *
     * \details
     * This parameter specifies the number of orders that are required to be present for a job chain to make orders proceed. Without specification one order is expected to be present. If e.g. three orders are specified then three orders from that chain have to be present and these three orders will simultaneously proceed having matched the synchronization criteria. The name of this parameter is created from the name of the respective job chain and the suffix _required_orders .
     *
     * \return
     *
     */
    public SOSOptionInteger getjob_chain_required_orders() {
        return job_chain_required_orders;
    }

    /**
     * \brief setjob_chain_required_orders :
     *
     * \details
     * This parameter specifies the number of orders that are required to be present for a job chain to make orders proceed. Without specification one order is expected to be present. If e.g. three orders are specified then three orders from that chain have to be present and these three orders will simultaneously proceed having matched the synchronization criteria. The name of this parameter is created from the name of the respective job chain and the suffix _required_orders .
     *
     * @param job_chain_required_orders :
     */
    public void setjob_chain_required_orders(final SOSOptionInteger p_job_chain_required_orders) {
        job_chain_required_orders = p_job_chain_required_orders;
    }

    /**
     * \var job_chain_state_required_orders :
     * This parameter specifies the number of orders that are required to be present for certain state a job chain to make orders proceed. Without specification one order is expected to be present. If e.g. three orders are specified then three orders from that chain have to be present and these three orders will simultaneously proceed having matched the synchronization criteria. The name of this parameter is created from the name of the respective job chain, a ";" , the name of the state and the suffix _required_orders .
     *
     */
    @JSOptionDefinition(name = "job_chain_state_required_orders", description = "", key = "job_chain_state_required_orders", type = "SOSOptionString", mandatory = false)
    public SOSOptionString job_chain_state_required_orders = new SOSOptionString(this, conClassName + ".job_chain_state_required_orders", // HashMap-Key
                                                                   "", // Titel
                                                                   "1", // InitValue
                                                                   "1", // DefaultValue
                                                                   false // isMandatory
                                                           );

    /**
     * \brief getjob_chain_state_required_orders :
     *
     * \details
     * This parameter specifies the number of orders that are required to be present for certain state a job chain to make orders proceed. Without specification one order is expected to be present. If e.g. three orders are specified then three orders from that chain have to be present and these three orders will simultaneously proceed having matched the synchronization criteria. The name of this parameter is created from the name of the respective job chain, a ";" , the name of the state and the suffix _required_orders .
     *
     * \return
     *
     */
    public SOSOptionString getjob_chain_state_required_orders() {
        return job_chain_state_required_orders;
    }

    /**
     * \brief setjob_chain_state_required_orders :
     *
     * \details
     * This parameter specifies the number of orders that are required to be present for certain state a job chain to make orders proceed. Without specification one order is expected to be present. If e.g. three orders are specified then three orders from that chain have to be present and these three orders will simultaneously proceed having matched the synchronization criteria. The name of this parameter is created from the name of the respective job chain, a ";" , the name of the state and the suffix _required_orders .
     *
     * @param job_chain_state_required_orders :
     */
    public void setjob_chain_state_required_orders(final SOSOptionString p_job_chain_state_required_orders) {
        job_chain_state_required_orders = p_job_chain_state_required_orders;
    }

    /**
     * \var jobchains_answer :
     *
     *
     */
    @JSOptionDefinition(name = "jobchains_answer", description = "", key = "jobchains_answer", type = "SOSOptionString", mandatory = false)
    public SOSOptionString jobchains_answer = new SOSOptionString(this, conClassName + ".jobchains_answer", // HashMap-Key
                                                    "", // Titel
                                                    "", // InitValue
                                                    "", // DefaultValue
                                                    false // isMandatory
                                            );

    /**
     * \brief getjobchains_answer :
     *
     * \details
     *
     *
     * \return
     *
     */
    public SOSOptionString getjobchains_answer() {
        return jobchains_answer;
    }

    /**
     * \brief setjobchains_answer :
     *
     * \details
     *
     *
     * @param jobchains_answer :
     */
    public void setjobchains_answer(final SOSOptionString p_jobchains_answer) {
        jobchains_answer = p_jobchains_answer;
    }

    /**
     * \var jobpath :
     *
     *
     */
    @JSOptionDefinition(name = "jobpath", description = "", key = "jobpath", type = "SOSOptionString", mandatory = false)
    public SOSOptionString jobpath = new SOSOptionString(this, conClassName + ".jobpath", // HashMap-Key
                                           "", // Titel
                                           "", // InitValue
                                           "", // DefaultValue
                                           false // isMandatory
                                   );

    /**
     * \brief getjobpath :
     *
     * \details
     *
     *
     * \return
     *
     */
    public SOSOptionString getjobpath() {
        return jobpath;
    }

    /**
     * \brief setjobpath :
     *
     * \details
     *
     *
     * @param jobpath :
     */
    public void setjobpath(final SOSOptionString p_jobpath) {
        jobpath = p_jobpath;
    }

    /**
     * \var JobChainName2Synchronize :
     *
     *
     */
     @JSOptionDefinition(name = "job_chain_name2synchronize", description = "", key = "job_chain_name2synchronize", type = "SOSOptionString", mandatory = false)
     public SOSOptionString job_chain_name2synchronize = new SOSOptionString(this, conClassName + ".job_chain_name2synchronize", // HashMap-Key
                                             "", // Titel
                                             "", // InitValue
                                             "", // DefaultValue
                                             false // isMandatory
                                     );

     /**
      * \brief getjob_chain_name2synchronize :
      *
      * \details
      *
      *
      * \return
      *
      */
     public SOSOptionString getjob_chain_name2synchronize() {
         return job_chain_name2synchronize;
     }

     /**
      * \brief setjob_chain_name2synchronize :
      *
      * \details
      *
      *
      * @param job_chain_name2synchronize :
      */
     public void setjob_chain_name2synchronize(final SOSOptionString p_job_chain_name2synchronize) {
         job_chain_name2synchronize = p_job_chain_name2synchronize;
     }


     
     
     /**
      * \var job_chain_state2synchronize  :
      *
      *
      */
      @JSOptionDefinition(name = "job_chain_state2synchronize", description = "", key = "job_chain_state2synchronize", type = "SOSOptionString", mandatory = false)
      public SOSOptionString job_chain_state2synchronize = new SOSOptionString(this, conClassName + ".job_chain_state2synchronize", // HashMap-Key
                                              "", // Titel
                                              "", // InitValue
                                              "", // DefaultValue
                                              false // isMandatory
                                      );

      /**
       * \brief getjob_chain_state2synchronize :
       *
       * \details
       *
       *
       * \return
       *
       */
      public SOSOptionString getjob_chain_state2synchronize() {
          return job_chain_state2synchronize;
      }

      /**
       * \brief setjob_chain_state2synchronize :
       *
       * \details
       *
       *
       * @param job_chain_state2synchronize :
       */
      public void setjob_chain_state2synchronize(final SOSOptionString p_job_chain_state2synchronize) {
          job_chain_state2synchronize = p_job_chain_state2synchronize;
      }
     
     /**
     * \var orders_answer :
     *
     *
     */
    @JSOptionDefinition(name = "orders_answer", description = "", key = "orders_answer", type = "SOSOptionString", mandatory = false)
    public SOSOptionString orders_answer = new SOSOptionString(this, conClassName + ".orders_answer", // HashMap-Key
                                                 "", // Titel
                                                 "", // InitValue
                                                 "", // DefaultValue
                                                 false // isMandatory
                                         );

    /**
     * \brief getorders_answer :
     *
     * \details
     *
     *
     * \return
     *
     */
    public SOSOptionString getorders_answer() {
        return orders_answer;
    }

    /**
     * \brief setorders_answer :
     *
     * \details
     *
     *
     * @param orders_answer :
     */
    public void setorders_answer(final SOSOptionString p_orders_answer) {
        orders_answer = p_orders_answer;
    }

    /**
     * \var required_orders :
     * This parameter specifies the number of orders that are required to be present for each job chain to make orders proceed. Without specification one order is expected to be present. If e.g. three orders are specified then three orders from that chain have to be present and these three orders will simultaneously proceed having matched the synchronization criteria. This parameter is considered only if no parameter [job_chain]_required_orders has been specified for the current job chain.
     *
     */
    @JSOptionDefinition(name = "required_orders", description = "", key = "required_orders", type = "SOSOptionString", mandatory = false)
    public SOSOptionString required_orders = new SOSOptionString(this, conClassName + ".required_orders", // HashMap-Key
                                                   "", // Titel
                                                   "1", // InitValue
                                                   "1", // DefaultValue
                                                   false // isMandatory
                                           );

    /**
     * \brief getrequired_orders :
     *
     * \details
     * This parameter specifies the number of orders that are required to be present for each job chain to make orders proceed. Without specification one order is expected to be present. If e.g. three orders are specified then three orders from that chain have to be present and these three orders will simultaneously proceed having matched the synchronization criteria. This parameter is considered only if no parameter [job_chain]_required_orders has been specified for the current job chain.
     *
     * \return
     *
     */
    public SOSOptionString getrequired_orders() {
        return required_orders;
    }

    /**
     * \brief setrequired_orders :
     *
     * \details
     * This parameter specifies the number of orders that are required to be present for each job chain to make orders proceed. Without specification one order is expected to be present. If e.g. three orders are specified then three orders from that chain have to be present and these three orders will simultaneously proceed having matched the synchronization criteria. This parameter is considered only if no parameter [job_chain]_required_orders has been specified for the current job chain.
     *
     * @param required_orders :
     */
    public void setrequired_orders(final SOSOptionString p_required_orders) {
        required_orders = p_required_orders;
    }

    /**
     * \var setback_count :
     * This parameter can be used with the parameter setback_type and its value setback to specify the maximum number of trials to set back orders that do not match the synchronization criteria. By default the setback_type suspend will be used that suspends orders and therefore would not require an interval. For better visibility it is recommended to set this value using the element <delay _order_after_setback> instead.
     *
     */
    @JSOptionDefinition(name = "setback_count", description = "", key = "setback_count", type = "SOSOptionInteger", mandatory = false)
    public SOSOptionInteger setback_count = new SOSOptionInteger(this, conClassName + ".setback_count", // HashMap-Key
                                                  "", // Titel
                                                  "unbounded", // InitValue
                                                  "unbounded", // DefaultValue
                                                  false // isMandatory
                                          );

    /**
     * \brief getsetback_count :
     *
     * \details
     * This parameter can be used with the parameter setback_type and its value setback to specify the maximum number of trials to set back orders that do not match the synchronization criteria. By default the setback_type suspend will be used that suspends orders and therefore would not require an interval. For better visibility it is recommended to set this value using the element <delay _order_after_setback> instead.
     *
     * \return
     *
     */
    public SOSOptionInteger getsetback_count() {
        return setback_count;
    }

    /**
     * \brief setsetback_count :
     *
     * \details
     * This parameter can be used with the parameter setback_type and its value setback to specify the maximum number of trials to set back orders that do not match the synchronization criteria. By default the setback_type suspend will be used that suspends orders and therefore would not require an interval. For better visibility it is recommended to set this value using the element <delay _order_after_setback> instead.
     *
     * @param setback_count :
     */
    public void setsetback_count(final SOSOptionInteger p_setback_count) {
        setback_count = p_setback_count;
    }

    /**
     * \var setback_interval :
     * This parameter can be used with the parameter setback_type and its value setback to specify the interval in seconds, for which orders are being set back that do not match the synchronization criteria. By default the setback_type suspend will be used that suspends orders and therefore would not require an interval. For better visibility it is recommended to set this value using the element <delay _order_after_setback> instead.
     *
     */
    @JSOptionDefinition(name = "setback_interval", description = "", key = "setback_interval", type = "SOSOptionInteger", mandatory = false)
    public SOSOptionInteger setback_interval = new SOSOptionInteger(this, conClassName + ".setback_interval", // HashMap-Key
                                                     "", // Titel
                                                     "600", // InitValue
                                                     "600", // DefaultValue
                                                     false // isMandatory
                                             );

    /**
     * \brief getsetback_interval :
     *
     * \details
     * This parameter can be used with the parameter setback_type and its value setback to specify the interval in seconds, for which orders are being set back that do not match the synchronization criteria. By default the setback_type suspend will be used that suspends orders and therefore would not require an interval. For better visibility it is recommended to set this value using the element <delay _order_after_setback> instead.
     *
     * \return
     *
     */
    public SOSOptionInteger getsetback_interval() {
        return setback_interval;
    }

    /**
     * \brief setsetback_interval :
     *
     * \details
     * This parameter can be used with the parameter setback_type and its value setback to specify the interval in seconds, for which orders are being set back that do not match the synchronization criteria. By default the setback_type suspend will be used that suspends orders and therefore would not require an interval. For better visibility it is recommended to set this value using the element <delay _order_after_setback> instead.
     *
     * @param setback_interval :
     */
    public void setsetback_interval(final SOSOptionInteger p_setback_interval) {
        setback_interval = p_setback_interval;
    }

    /**
     * \var setback_type :
     * This parameter can be used in order to choose between suspend and setback for the handling of waiting orders: suspend Orders are suspended if the synchronization criteria were not matched. Such orders remain in this state for an arbitrary duration provided that they were not continued by the synchronization job. Alternatively such orders can be continued manually in the Web GUI. setback Orders are repeatedly executed as specified by the parameters setback_interval and setback_count . Should the specified interval and frequency be exceeded then the order enters an error state and might leave the job chain. Alternatively such orders can be continued manually in the Web GUI.
     *
     */
    @JSOptionDefinition(name = "setback_type", description = "", key = "setback_type", type = "SOSOptionSetBack", mandatory = false)
    public SOSOptionString setback_type = new SOSOptionString(this, conClassName + ".setback_type", // HashMap-Key
                                                "", // Titel
                                                "suspend", // InitValue
                                                "suspend", // DefaultValue
                                                false // isMandatory
                                        );

    /**
     * \brief getsetback_type :
     *
     * \details
     * This parameter can be used in order to choose between suspend and setback for the handling of waiting orders: suspend Orders are suspended if the synchronization criteria were not matched. Such orders remain in this state for an arbitrary duration provided that they were not continued by the synchronization job. Alternatively such orders can be continued manually in the Web GUI. setback Orders are repeatedly executed as specified by the parameters setback_interval and setback_count . Should the specified interval and frequency be exceeded then the order enters an error state and might leave the job chain. Alternatively such orders can be continued manually in the Web GUI.
     *
     * \return
     *
     */
    public SOSOptionString getsetback_type() {
        return setback_type;
    }

    /**
     * \brief setsetback_type :
     *
     * \details
     * This parameter can be used in order to choose between suspend and setback for the handling of waiting orders: suspend Orders are suspended if the synchronization criteria were not matched. Such orders remain in this state for an arbitrary duration provided that they were not continued by the synchronization job. Alternatively such orders can be continued manually in the Web GUI. setback Orders are repeatedly executed as specified by the parameters setback_interval and setback_count . Should the specified interval and frequency be exceeded then the order enters an error state and might leave the job chain. Alternatively such orders can be continued manually in the Web GUI.
     *
     * @param setback_type :
     */
    public void setsetback_type(final SOSOptionString p_setback_type) {
        setback_type = p_setback_type;
    }

    /**
     * \var sync_session_id :
     * If an order has the sync_session_id parameter, then this order will only be synchronized with orders which have the same value for the sync_session_id parameter. This is required if multiple groups of parallel orders run through parallel job chains. In the end, the orders will be synchronized for each group (which may have been created by a split).
     *
     */
    @JSOptionDefinition(name = "sync_session_id", description = "", key = "sync_session_id", type = "SOSOptionString", mandatory = false)
    public SOSOptionString sync_session_id = new SOSOptionString(this, conClassName + ".sync_session_id", // HashMap-Key
                                                   "", // Titel
                                                   "", // InitValue
                                                   "", // DefaultValue
                                                   false // isMandatory
                                           );

    /**
     * \brief getsync_session_id :
     *
     * \details
     * If an order has the sync_session_id parameter, then this order will only be synchronized with orders which have the same value for the sync_session_id parameter. This is required if multiple groups of parallel orders run through parallel job chains. In the end, the orders will be synchronized for each group (which may have been created by a split).
     *
     * \return
     *
     */
    public SOSOptionString getsync_session_id() {
        return sync_session_id;
    }

    /**
     * \brief setsync_session_id :
     *
     * \details
     * If an order has the sync_session_id parameter, then this order will only be synchronized with orders which have the same value for the sync_session_id parameter. This is required if multiple groups of parallel orders run through parallel job chains. In the end, the orders will be synchronized for each group (which may have been created by a split).
     *
     * @param sync_session_id :
     */
    public void setsync_session_id(final SOSOptionString p_sync_session_id) {
        sync_session_id = p_sync_session_id;
    }

    public JobSchedulerSynchronizeJobChainsOptionsSuperClass() {
        objParentClass = this.getClass();
    } // public JobSchedulerSynchronizeJobChainsOptionsSuperClass

    public JobSchedulerSynchronizeJobChainsOptionsSuperClass(final JSListener pobjListener) {
        this();
        this.registerMessageListener(pobjListener);
    } // public JobSchedulerSynchronizeJobChainsOptionsSuperClass

    //

    public JobSchedulerSynchronizeJobChainsOptionsSuperClass(final HashMap<String, String> JSSettings) throws Exception {
        this();
        this.setAllOptions(JSSettings);
    } // public JobSchedulerSynchronizeJobChainsOptionsSuperClass (HashMap JSSettings)

    /**
     * \brief getAllOptionsAsString - liefert die Werte und Beschreibung aller
     * Optionen als String
     *
     * \details
     *
     * \see toString
     * \see toOut
     */
    @SuppressWarnings("unused")
    private String getAllOptionsAsString() {
        final String conMethodName = conClassName + "::getAllOptionsAsString";
        String strT = conClassName + "\n";
        final StringBuffer strBuffer = new StringBuffer();
        // strT += IterateAllDataElementsByAnnotation(objParentClass, this,
        // JSOptionsClass.IterationTypes.toString, strBuffer);
        // strT += IterateAllDataElementsByAnnotation(objParentClass, this, 13,
        // strBuffer);
        strT += this.toString(); // fix
        //
        return strT;
    } // private String getAllOptionsAsString ()

    /**
     * \brief setAllOptions - übernimmt die OptionenWerte aus der HashMap
     *
     * \details In der als Parameter anzugebenden HashMap sind Schlüssel (Name)
     * und Wert der jeweiligen Option als Paar angegeben. Ein Beispiel für den
     * Aufbau einer solchen HashMap findet sich in der Beschreibung dieser
     * Klasse (\ref TestData "setJobSchedulerSSHJobOptions"). In dieser Routine
     * werden die Schlüssel analysiert und, falls gefunden, werden die
     * dazugehörigen Werte den Properties dieser Klasse zugewiesen.
     *
     * Nicht bekannte Schlüssel werden ignoriert.
     *
     * \see JSOptionsClass::getItem
     *
     * @param pobjJSSettings
     * @throws Exception
     */
    @Override
    public void setAllOptions(final HashMap<String, String> pobjJSSettings) throws Exception {
        @SuppressWarnings("unused")
        final String conMethodName = conClassName + "::setAllOptions";
        flgSetAllOptions = true;
        objSettings = pobjJSSettings;
        super.Settings(objSettings);
        super.setAllOptions(pobjJSSettings);
        flgSetAllOptions = false;
    } // public void setAllOptions (HashMap <String, String> JSSettings)

    /**
     * \brief CheckMandatory - prüft alle Muss-Optionen auf Werte
     *
     * \details
     * @throws Exception
     *
     * @throws Exception
     * - wird ausgelöst, wenn eine mandatory-Option keinen Wert hat
     */
    @Override
    public void CheckMandatory() throws JSExceptionMandatoryOptionMissing //
            , Exception {
        try {
            super.CheckMandatory();
        }
        catch (Exception e) {
            throw new JSExceptionMandatoryOptionMissing(e.toString());
        }
    } // public void CheckMandatory ()

    /**
     *
     * \brief CommandLineArgs - Übernehmen der Options/Settings aus der
     * Kommandozeile
     *
     * \details Die in der Kommandozeile beim Starten der Applikation
     * angegebenen Parameter werden hier in die HashMap übertragen und danach
     * den Optionen als Wert zugewiesen.
     *
     * \return void
     *
     * @param pstrArgs
     * @throws Exception
     */
    @Override
    public void CommandLineArgs(final String[] pstrArgs) throws Exception {
        super.CommandLineArgs(pstrArgs);
        this.setAllOptions(super.objSettings);
    }
} // public class JobSchedulerSynchronizeJobChainsOptionsSuperClass