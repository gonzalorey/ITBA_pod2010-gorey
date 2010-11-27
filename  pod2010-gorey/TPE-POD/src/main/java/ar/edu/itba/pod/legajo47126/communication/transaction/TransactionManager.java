package ar.edu.itba.pod.legajo47126.communication.transaction;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;

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

	private long canCommitTimeout;

	private ThreePhaseCommit localThreePhaseCommit;
	
	private NodeManagement nodeManagement;
	
	Timer timer;

	public TransactionManager(NodeManagement nodeManagement) throws RemoteException {
		UnicastRemoteObject.exportObject(this, 0);
		
		this.nodeManagement = nodeManagement;

		transactionAcceptingTimeout = nodeManagement.getConfigFile()
				.getProperty("TransactionAcceptingTimeout", 200);
		canCommitTimeout = nodeManagement.getConfigFile().getProperty(
				"CanCommitTimeout", 300);

		// create the three phase commit of the transaction
		localThreePhaseCommit = new ThreePhaseCommitImpl(nodeManagement);
	}

	@Override
	public void beginTransaction(String remoteNodeId, long timeout)
			throws RemoteException {
		logger.debug("Beginning a transaction with node [" + remoteNodeId
				+ "] and timeout [" + timeout + "]...");

		if (isTransactioning()) {
			logger.error("A transaction is currently running");
			throw new IllegalStateException(
					"The node is already in a transaction context");
		}

		// accept the connection from the other side
		logger.debug("Call the transaction accept of the destination node");
		nodeManagement.getConnectionManager().getConnectionManager(remoteNodeId)
				.getNodeCommunication().acceptTransaction(
						nodeManagement.getLocalNode().getNodeId());

		logger.debug("Transaction successfully accepted");
		setTransaction(nodeManagement.getLocalNode().getNodeId(), remoteNodeId);

		// launch a schedule task that will execute the onTimeout function after
		// a timeout delay
		logger.debug("Starting a schedule task that will take action after a timeout delay");
		timer = new Timer();
		timer.schedule(new ScheduleTask(), timeout);
		
	}

	@Override
	public void acceptTransaction(String remoteNodeId) throws RemoteException {
		logger.debug("Accepting a transaction request from node ["
				+ remoteNodeId + "]...");

		if (isTransactioning()) {
			logger.debug("A transaction is currently running, sleep "
					+ transactionAcceptingTimeout + "msec");
			try {
				Thread.sleep(transactionAcceptingTimeout);
			} catch (InterruptedException e) {
				logger.error("The transaction wait was interrupted");
				logger.error("Error message: " + e.getMessage());
			}

			if (isTransactioning()) {
				logger.error("Transaction accept time ended by timeout");
				throw new IllegalStateException(
						"The transaction ended in timeout");
			}
		}

		// set the transaction
		setTransaction(remoteNodeId, nodeManagement.getLocalNode().getNodeId());
		logger.debug("Transaction accepted");
	}

	@Override
	public void endTransaction() throws RemoteException {
		logger.debug("Ending the transaction...");

		if (!isTransactioning()) {
			logger.error("No transaction is currently running");
			throw new IllegalStateException("No transaction currently running");
		}

		// do the three phase commit
		doThreePhaseCommit();
		logger.debug("Three-Phase-Commit successfully finished");

		// if this is the source node, end the destination node transaction
		if (transaction.getSourceNodeId().equals((nodeManagement.getLocalNode().getNodeId()))) {
			logger.debug("Call the end transaction of the destination node");
			nodeManagement.getConnectionManager().getConnectionManager(
					transaction.getDestinationNodeId()).getNodeCommunication()
					.endTransaction();
		}

		// clear the transaction to accept more
		clearTransaction();
		logger.debug("Transaction successfully ended");
	}

	@Override
	public void exchange(Resource resource, int amount, String sourceNode,
			String destinationNode) throws RemoteException {
		logger.debug("Exchanging an amount of [" + amount
				+ "] of the resource [" + resource + "] from node ["
				+ sourceNode + "] to node [" + destinationNode + "]");

		if (!isTransactioning()) {
			logger.debug("No transaction is currently running");
			throw new IllegalStateException("No transaction currently running");
		}

		if (sourceNode.equals(destinationNode)) {
			logger.debug("The source node and the destination node are the same");
			throw new IllegalStateException(
					"The source node and the destination node are the same");
		}

		transaction.setResource(resource, amount);

		// if this is the source node, do the exchange of the destination node
		if (transaction.getSourceNodeId().equals(nodeManagement.getLocalNode().getNodeId())) {
			logger.debug("Call the exchange of the destination node");
			nodeManagement.getConnectionManager().getConnectionManager(transaction.getDestinationNodeId()).
				getNodeCommunication().exchange(resource,amount, sourceNode, destinationNode);
		}

		transaction.setTransactionDone(true);
		logger.debug("Exchange successfully done");
	}

	@Override
	public Payload getPayload() throws RemoteException {
		logger.debug("Getting the payload...");

		if (!isTransactioning()) {
			logger.debug("No transaction is currently running");
			throw new IllegalStateException(
					"No transaction is currently running");
		}

		if (!transaction.isTransactionDone()) {
			logger.debug("The transaction hasn't been finished");
			throw new IllegalStateException(
					"The transaction hasn't been finished");
		}

		return transaction.getPayload();
	}

	@Override
	public void rollback() throws RemoteException {
		logger.debug("Rollback the transaction...");

		if (transaction.isTransactionDone()) {
			logger.debug("A transaction is currently running");
			throw new IllegalStateException(
					"A transaction is currently running");
		}

		try {
			localThreePhaseCommit.abort();
		} catch (Exception e) {
			logger.error("An error ocurred during the revert");
			logger.error("Error message: " + e.getMessage());

			throw new RuntimeException("An error ocurred during the revert");
		}
		
		clearTransaction();
		
		// if this is the source node, do the rollback of the destination node
		if (transaction.getSourceNodeId().equals(nodeManagement.getLocalNode().getNodeId())) {
			logger.debug("Call the exchange of the destination node");
			nodeManagement.getConnectionManager().getConnectionManager(transaction.getDestinationNodeId()).
				getNodeCommunication().rollback();
		}
		
		logger.debug("Rollback successfully finished");
	}

	// TODO see if these methods should be eliminated
	private synchronized void setTransaction(String sourceNodeId,
			String destinationNodeId) {
		transaction = new TransactionContainer(sourceNodeId, destinationNodeId);
	}

	private synchronized boolean isTransactioning() {
		return (transaction != null);
	}

	private synchronized void clearTransaction() {
		transaction = null;
	}

	private void doThreePhaseCommit() throws RemoteException {
		logger.debug("Doing the Three-Phase-Commit...");

		ThreePhaseCommit remoteThreePhaseCommit = nodeManagement.getConnectionManager().
			getConnectionManager(transaction.getDestinationNodeId())
				.getThreePhaseCommit();

		if (!localThreePhaseCommit.canCommit(transaction.getSourceNodeId(),canCommitTimeout)) {
			logger.error("The local node couldn't start the Three-Phase-Commit");
			timer.cancel();
			return;
		} 
		logger.debug("CanCommit accepted by the local node");

		if (!remoteThreePhaseCommit.canCommit(transaction.getSourceNodeId(),canCommitTimeout)) {
			logger.error("The remote node couldn't start the Three-Phase-Commit");
			timer.cancel();
			return;
		}
		logger.debug("CanCommit accepted by the remote node");

		try {
			localThreePhaseCommit.preCommit(transaction.getSourceNodeId());
		} catch (Exception e) {
			logger.error("There was an error in the local Three-Phase-Commit on the preCommit state, aborting...");
			logger.error("Error message: " + e.getMessage());
			timer.cancel();
			localThreePhaseCommit.abort();
		}
		logger.debug("PreCommit finished by the local node");
		
		try {
			remoteThreePhaseCommit.preCommit(transaction.getSourceNodeId());
		} catch (Exception e) {
			logger.error("There was an error in the remote Three-Phase-Commit on the preCommit state, aborting...");
			logger.error("Error message: " + e.getMessage());
			timer.cancel();
			remoteThreePhaseCommit.abort();
		}
		logger.debug("PreCommit finished by the remote node");
		
		try {
			localThreePhaseCommit.doCommit(transaction.getSourceNodeId());
		} catch (Exception e) {
			logger.error("There was an error in the local Three-Phase-Commit on the doCommit state, aborting...");
			logger.error("Error message: " + e.getMessage());
			timer.cancel();
			localThreePhaseCommit.abort();
		}
		logger.debug("DoCommit finished by the local node");

		try {
			remoteThreePhaseCommit.doCommit(transaction.getSourceNodeId());
		} catch (Exception e) {
			logger.error("There was an error in the remote Three-Phase-Commit on the doCommit state, aborting...");
			logger.error("Error message: " + e.getMessage());
			timer.cancel();
			remoteThreePhaseCommit.abort();
		}
		logger.debug("DoCommit finished by the remote node");
		logger.debug("Three-Phase-Commit finished successfully");
		
		timer.cancel();
	}

	public ThreePhaseCommit getThreePhaseCommit() {
		return localThreePhaseCommit;
	}

	private void onTimeout() throws RemoteException {
		logger.debug("OnTimeout executed, processing according to the current state");

		if(!transaction.isTransactionDone()){
			logger.warn("The transaction couldn't be finished, rollback");
			rollback();
		}
	}

	private class ScheduleTask extends TimerTask {

		@Override
		public void run() {
			try {
				onTimeout();
			} catch (RemoteException e) {
				logger.error("An error ocurred during the onTimeout execution");
				logger.error("Error message: " + e.getMessage());
			}
		}
	}
}
