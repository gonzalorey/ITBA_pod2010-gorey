package ar.edu.itba.pod.legajo47126.communication.transaction;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import org.apache.log4j.Logger;

import ar.edu.itba.pod.legajo47126.communication.impl.ConnectionManagerImpl;
import ar.edu.itba.pod.legajo47126.node.NodeManagement;
import ar.edu.itba.pod.simul.communication.ThreePhaseCommit;
import ar.edu.itba.pod.simul.communication.Transactionable;
import ar.edu.itba.pod.simul.communication.payload.Payload;
import ar.edu.itba.pod.simul.market.Resource;

public class TransactionManager implements Transactionable {
	
	// instance of the log4j logger
	private static Logger logger = Logger.getLogger(TransactionManager.class);
	
	private TransactionContainer transaction = null; 
	
	private long transactionAcceptingTimeout;
	
	private long transactionProcessingTimeout;
	
	private long canCommitTimeout;
	
	private ThreePhaseCommit localThreePhaseCommit;
	
	public TransactionManager() throws RemoteException {
		UnicastRemoteObject.exportObject(this, 0);
		
		transactionAcceptingTimeout = NodeManagement.getConfigFile().getProperty("TransactionAcceptingTimeout", 200);
		transactionProcessingTimeout = NodeManagement.getConfigFile().getProperty("TransactionProcessingTimeout", 1000);
		canCommitTimeout = NodeManagement.getConfigFile().getProperty("CanCommitTimeout", 300);
		
		// create the three phase commit of the transaction
		localThreePhaseCommit = new ThreePhaseCommitImpl();
	}
	
	@Override
	public void beginTransaction(String remoteNodeId, long timeout) throws RemoteException {
		logger.debug("Beginning a transaction...");
		
		if(isTransactioning()){
			throw new IllegalStateException("The node is already in a transaction context");
		}
		
		try {
			// accept the connection from the other side
			ConnectionManagerImpl.getInstance().getConnectionManager(remoteNodeId).
				getNodeCommunication().acceptTransaction(NodeManagement.getLocalNode().getNodeId());
		} catch (IllegalStateException e) {
			logger.error("The transaction start event ended by timeout");
			logger.error("Error message: " + e.getMessage());
		}

		setTransaction(NodeManagement.getLocalNode().getNodeId(), remoteNodeId);
		
		Thread thread = new Thread(new TransactionProcessor(transactionProcessingTimeout, transaction));
		thread.start();
	}

	@Override
	public void acceptTransaction(String remoteNodeId) throws RemoteException {
		if(!isTransactioning()){
			try {
				Thread.sleep(transactionAcceptingTimeout);
			} catch (InterruptedException e) {
				logger.error("The transaction wait was interrupted");
				logger.error("Error message: " + e.getMessage());
			}
			
			if(!isTransactioning()){
				throw new IllegalStateException("The transaction ended in timeout");
			}
		}
	
		// set the transaction
		setTransaction(remoteNodeId, NodeManagement.getLocalNode().getNodeId());
	}

	@Override
	public void endTransaction() throws RemoteException {
		if(!isTransactioning())
			throw new IllegalStateException("No transaction was running");
		
		// clear the transaction to accept more
		clearTransaction();		// TODO should it go before the threephasecommit?

		// do the three phase commit
		doThreePhaseCommit();
		
		// if this is the origin node, end the transaction in the destiny node
		if(NodeManagement.getLocalNode().getNodeId().equals(transaction.getOriginNodeId())){
			ConnectionManagerImpl.getInstance().getConnectionManager(transaction.getDestinyNodeId()).
				getNodeCommunication().endTransaction();
		}
	}

	@Override
	public void exchange(Resource resource, int amount, String sourceNode,
			String destinationNode) throws RemoteException {
		// TODO Auto-generated method stub

	}

	@Override
	public Payload getPayload() throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void rollback() throws RemoteException {
		// TODO Auto-generated method stub

	}
	
	private synchronized void setTransaction(String originNodeId, String destinyNodeId){
		transaction = new TransactionContainer(originNodeId, destinyNodeId); 
	}
	
	private synchronized void clearTransaction(){
		transaction = null;
	}
	
	private synchronized boolean isTransactioning(){
		return (transaction != null);
	}

	private void doThreePhaseCommit() throws RemoteException{
		ThreePhaseCommit remoteThreePhaseCommit = ConnectionManagerImpl.getInstance().
			getConnectionManager(transaction.getDestinyNodeId()).getThreePhaseCommit();
		
		if(!localThreePhaseCommit.canCommit(transaction.getOriginNodeId(), canCommitTimeout)){
			logger.error("The local node couldn't start the Three-Phase-Commit");
			return;
		}
			
		if(!remoteThreePhaseCommit.canCommit(transaction.getOriginNodeId(), canCommitTimeout)){
			logger.error("The remote node couldn't start the Three-Phase-Commit");
			return;
		}
		
		try{
			localThreePhaseCommit.preCommit(transaction.getOriginNodeId());
		} catch (Exception e) {
			logger.error("There was an error in the local Three-Phase-Commit on the preCommit state, aborting...");
			logger.error("Error message: " + e.getMessage());
			localThreePhaseCommit.abort();
		}
		
		try{
			remoteThreePhaseCommit.preCommit(transaction.getOriginNodeId());
		} catch (Exception e) {
			logger.error("There was an error in the remote Three-Phase-Commit on the preCommit state, aborting...");
			logger.error("Error message: " + e.getMessage());
			remoteThreePhaseCommit.abort();
		}
		
		try{
			localThreePhaseCommit.doCommit(transaction.getOriginNodeId());
		} catch (Exception e) {
			logger.error("There was an error in the local Three-Phase-Commit on the doCommit state, aborting...");
			logger.error("Error message: " + e.getMessage());
			localThreePhaseCommit.abort();
		}
		
		try{
			remoteThreePhaseCommit.doCommit(transaction.getOriginNodeId());
		} catch (Exception e) {
			logger.error("There was an error in the remote Three-Phase-Commit on the doCommit state, aborting...");
			logger.error("Error message: " + e.getMessage());
			remoteThreePhaseCommit.abort();
		}
	}
	
	public ThreePhaseCommit getThreePhaseCommit(){
		return localThreePhaseCommit;
	}
	
}
