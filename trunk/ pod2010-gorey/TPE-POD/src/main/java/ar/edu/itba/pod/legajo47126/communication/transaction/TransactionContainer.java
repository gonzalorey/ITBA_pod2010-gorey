package ar.edu.itba.pod.legajo47126.communication.transaction;

import org.joda.time.DateTime;

public class TransactionContainer {
	
	private String originNodeId;
	
	private String destinyNodeId;
	
	private long timestamp;
	
	private boolean transactionFinished; 
	
	public TransactionContainer(String originNodeId, String destinyNodeId) {
		this.originNodeId = originNodeId;
		this.destinyNodeId = destinyNodeId;
		this.timestamp = new DateTime().getMillis();
		this.transactionFinished = false;
	}

	public String getOriginNodeId() {
		return originNodeId;
	}

	public String getDestinyNodeId() {
		return destinyNodeId;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public boolean isTransactionFinished() {
		return transactionFinished;
	}

	public void setTransactionFinished(boolean transactionFinished) {
		this.transactionFinished = transactionFinished;
	}
}
