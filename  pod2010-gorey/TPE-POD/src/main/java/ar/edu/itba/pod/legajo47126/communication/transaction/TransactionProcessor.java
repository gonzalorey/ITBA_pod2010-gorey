package ar.edu.itba.pod.legajo47126.communication.transaction;

import org.apache.log4j.Logger;

public class TransactionProcessor implements Runnable {

	// instance of the log4j logger
	private static Logger logger = Logger.getLogger(TransactionProcessor.class);

	private long timeout;
	
	private TransactionContainer transaction;
	
	public TransactionProcessor(long timeout, TransactionContainer transaction) {
		this.timeout = timeout;
	}
	
	@Override
	public void run() {
		try {
			Thread.sleep(timeout);
		} catch (InterruptedException e) {
			logger.error("The transaction was interrupted during his processing");
			logger.error("Error message: " + e.getMessage());
		}
		
	}

	public TransactionContainer getTransaction() {
		return transaction;
	}

}
