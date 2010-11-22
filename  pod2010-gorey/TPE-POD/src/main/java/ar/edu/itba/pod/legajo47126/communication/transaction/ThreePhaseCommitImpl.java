package ar.edu.itba.pod.legajo47126.communication.transaction;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import org.apache.log4j.Logger;

import ar.edu.itba.pod.legajo47126.communication.impl.ConnectionManagerImpl;
import ar.edu.itba.pod.legajo47126.communication.impl.message.MessageFactory;
import ar.edu.itba.pod.legajo47126.node.NodeManagement;
import ar.edu.itba.pod.simul.communication.Message;
import ar.edu.itba.pod.simul.communication.ThreePhaseCommit;
import ar.edu.itba.pod.simul.communication.payload.Payload;

public class ThreePhaseCommitImpl implements ThreePhaseCommit {
	
	// instance of the log4j logger
	private static Logger logger = Logger.getLogger(ThreePhaseCommitImpl.class);

//	private enum CoordinatorSatus{SOLICITING_VOTES, COMMIT_AUTHORIZED, FINALIZING_COMMIT, DONE};
//	private enum CohortStatus{UNCERTAIN, PREPARED_TO_COMMIT, COMMITED};

	private enum ThreePhaseCommitState{START, CAN_COMMIT_DONE, PRE_COMMIT_DONE, DO_COMMIT_DONE};
	
	private String coordinatorId;
	
	private ThreePhaseCommitState state;
	
	public ThreePhaseCommitImpl() throws RemoteException {
		UnicastRemoteObject.exportObject(this, 0);
		
		state = ThreePhaseCommitState.START;
	}
	
	@Override
	public boolean canCommit(String coordinatorId, long timeout) throws RemoteException {
		if(state == ThreePhaseCommitState.START){
			this.coordinatorId = coordinatorId;
			state = ThreePhaseCommitState.CAN_COMMIT_DONE;
			try {
				Thread.sleep(timeout);
			} catch (InterruptedException e) {
				logger.error("The process was interrupted while sleeping");
				logger.error("Error message: " + e.getMessage());
				return false;
			}
			return true;
		}
		return false;
	}
	
	@Override
	public void preCommit(String coordinatorId) throws RemoteException {
		if(!this.coordinatorId.equals(coordinatorId))
			throw new IllegalArgumentException("This coordinator isn't the same that invoked the last canCommit");
		
		if(state != ThreePhaseCommitState.CAN_COMMIT_DONE)
			throw new IllegalArgumentException("The current state isn't CAN_COMMIT_DONE");
		
		state = ThreePhaseCommitState.PRE_COMMIT_DONE;
	}
	
	@Override
	public void doCommit(String coordinatorId) throws RemoteException {
		if(!this.coordinatorId.equals(coordinatorId))
			throw new IllegalArgumentException("This coordinator isn't the same that invoked the last preCommit");
		
		if(state != ThreePhaseCommitState.PRE_COMMIT_DONE)
			throw new IllegalArgumentException("The current state isn't PRE_COMMIT_DONE");
		
		Payload payload = ConnectionManagerImpl.getInstance().getConnectionManager(coordinatorId).
			getNodeCommunication().getPayload();
		Message message = MessageFactory.ResourceTransferMessage(payload);
		logger.debug("Sending the message [" + message + "] to the local node");

		// send the message to the local node
		ConnectionManagerImpl.getInstance().getGroupCommunication().send(message, NodeManagement.getLocalNode().getNodeId());
		
		state = ThreePhaseCommitState.DO_COMMIT_DONE;
	}
	
	@Override
	public void abort() throws RemoteException {
		if(state == ThreePhaseCommitState.START);
			throw new IllegalArgumentException("Method invoked before canCommit was invoked");
			
		// TODO WHAT ELSE!?!?!?!?
	}

	@Override
	public void onTimeout() throws RemoteException {
		if(state == ThreePhaseCommitState.START);
		throw new IllegalArgumentException("Method invoked before canCommit was invoked");
		
	// TODO WHAT ELSE!?!?!?!?
	}

}
