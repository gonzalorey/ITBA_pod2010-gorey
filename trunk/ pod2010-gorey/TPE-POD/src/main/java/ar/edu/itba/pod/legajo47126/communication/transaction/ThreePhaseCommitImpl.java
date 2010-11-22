package ar.edu.itba.pod.legajo47126.communication.transaction;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Timer;
import java.util.TimerTask;

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

	private enum ThreePhaseCommitState{START, CAN_COMMIT_DONE, PRE_COMMIT_DONE, DO_COMMIT_DONE};
	
	private String coordinatorId;
	
	private ThreePhaseCommitState state;
	
	public ThreePhaseCommitImpl() throws RemoteException {
		UnicastRemoteObject.exportObject(this, 0);
		
		state = ThreePhaseCommitState.START;
	}
	
	@Override
	public boolean canCommit(String coordinatorId, long timeout) throws RemoteException {
		logger.debug("CanCommit with coordinator [" + coordinatorId + "] and timeout [" + timeout + "]...");
		
		if(state == ThreePhaseCommitState.START){
			this.coordinatorId = coordinatorId;
			
			// launch a schedule task that will execute the onTimeout function after a timeout delay
			logger.debug("Starting a schedule task that will take action after a timeout delay");
			new Timer().schedule(new ScheduleTask(), timeout);
			
			// set the next state
			logger.debug("Setting next state CAN_COMMIT_DONE");
			state = ThreePhaseCommitState.CAN_COMMIT_DONE;
			
			return true;
		}
		return false;
	}
	
	@Override
	public void preCommit(String coordinatorId) throws RemoteException {
		logger.debug("PreCommit with coordinator [" + coordinatorId + "]...");
		
		if(!this.coordinatorId.equals(coordinatorId))
			throw new IllegalArgumentException("This coordinator isn't the same that invoked the last canCommit");
		
		if(state != ThreePhaseCommitState.CAN_COMMIT_DONE)
			throw new IllegalArgumentException("The current state isn't CAN_COMMIT_DONE");
		
		// set the next state
		logger.debug("Setting next state PRE_COMMIT_DONE");
		state = ThreePhaseCommitState.PRE_COMMIT_DONE;
	}
	
	@Override
	public void doCommit(String coordinatorId) throws RemoteException {
		logger.debug("DoCommit with coordinator [" + coordinatorId + "]...");
		
		if(!this.coordinatorId.equals(coordinatorId))
			throw new IllegalArgumentException("This coordinator isn't the same that invoked the last preCommit");
		
		if(state != ThreePhaseCommitState.PRE_COMMIT_DONE)
			throw new IllegalArgumentException("The current state isn't PRE_COMMIT_DONE");
		
		Payload payload = ConnectionManagerImpl.getInstance().getConnectionManager(coordinatorId).
			getNodeCommunication().getPayload();
		Message message = MessageFactory.ResourceTransferMessage(payload);

		// send the message to the local node
		logger.debug("Sending the message [" + message + "] to the local node");
		ConnectionManagerImpl.getInstance().getGroupCommunication().send(message, NodeManagement.getLocalNode().getNodeId());
		
		// set the next state
		logger.debug("Setting next state DO_COMMIT_DONE");
		state = ThreePhaseCommitState.DO_COMMIT_DONE;
	}
	
	@Override
	public void abort() throws RemoteException {
		logger.debug("Abort arrived...");
		
		if(state == ThreePhaseCommitState.START)
			throw new IllegalArgumentException("Method invoked before canCommit was invoked");
			
		// set the initial state
		state = ThreePhaseCommitState.START;
	}

	@Override
	public void onTimeout() throws RemoteException {
		logger.debug("OnTimeout executed, processing according to the current state...");
		
		switch (state) {
		case START:
			logger.debug("START state, throw exception");
			throw new IllegalArgumentException("Method invoked before canCommit was invoked");
		case CAN_COMMIT_DONE:
			// the timeout arrived before the preCommit was done, so abort
			logger.debug("CAN_COMMIT_DONE state, abort transaction");
			abort();
			break;
		case PRE_COMMIT_DONE:
			// the timeout arrived after the preCommit was done, so commit
			logger.debug("PRE_COMMIT_DONE state, call doCommit");
			doCommit(coordinatorId);
			break;
		case DO_COMMIT_DONE:
			// the timeout arrived after the doCommit was done, so set the next state as the initial
			logger.debug("DO_COMMIT_DONE state, set initial state START");
			state = ThreePhaseCommitState.START;
			break;
		default:
			break;
		}
	}
	
	private class ScheduleTask extends TimerTask{

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
