package sos.scheduler.job;

 

/**
 * @author andreas.pueschel@sos-berlin.com
 *
 * dequeue previously stored mails and try to send them
 */
public class JobSchedulerDequeueMail extends JobSchedulerJobAdapter {

    /** Attribut numOfMails: number of mails currently in queue */
    private int numOfMails = 0;
    
    
	public boolean spooler_open() {
    	
		this.numOfMails = spooler_log.mail().dequeue();

		return (this.numOfMails > 0);
	}

	
	public boolean spooler_process() {
        spooler_log.info( ".......extending JobSchedulderAdapter" );
    	
		if ( this.numOfMails > 0 ) {
			spooler_log.info( this.numOfMails + " previously stored mails were sent, mail queue is empty" );
		}

		return false;
	}

}