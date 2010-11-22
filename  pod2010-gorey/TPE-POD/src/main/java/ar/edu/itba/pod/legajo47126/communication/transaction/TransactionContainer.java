package ar.edu.itba.pod.legajo47126.communication.transaction;

import org.joda.time.DateTime;

import ar.edu.itba.pod.legajo47126.communication.paylod.impl.ResourceTransferMessagePayloadImpl;
import ar.edu.itba.pod.simul.communication.payload.Payload;
import ar.edu.itba.pod.simul.market.Resource;

public class TransactionContainer {
	
	private String sourceNodeId;
	
	private String destinationNodeId;
	
	private long timestamp;
	
	private boolean transactionDone;
	
	private Resource resource;
	
	private int amount;
	
	public TransactionContainer(String sourceNodeId, String destinationNodeId) {
		// transaction nodes
		this.sourceNodeId = sourceNodeId;
		this.destinationNodeId = destinationNodeId;
		
		// timestamp of the transaction
		this.timestamp = new DateTime().getMillis();
		
		// state of the transaction
		this.transactionDone = false;
		this.resource = null;
		this.amount = 0;
	}

	// TODO see if they need to be synchronized, I don't think so...
	
	public synchronized String getSourceNodeId() {
		return sourceNodeId;
	}

	public synchronized String getDestinationNodeId() {
		return destinationNodeId;
	}

	public synchronized long getTimestamp() {
		return timestamp;
	}

	public synchronized boolean isTransactionDone() {
		return transactionDone;
	}

	public synchronized void setTransactionDone(boolean transactionDone) {
		this.transactionDone = transactionDone;
	}

	public synchronized Resource getResource() {
		return resource;
	}

	public synchronized void setResource(Resource resource, int amount) {
		this.resource = resource;
		this.amount = amount;
	}

	public synchronized int getAmount() {
		return amount;
	}
	
	public synchronized Payload getPayload(){
		return new ResourceTransferMessagePayloadImpl(resource, amount, sourceNodeId, destinationNodeId);
	}
}
