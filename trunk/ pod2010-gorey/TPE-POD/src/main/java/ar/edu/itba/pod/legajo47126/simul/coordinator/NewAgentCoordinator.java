package ar.edu.itba.pod.legajo47126.simul.coordinator;

import java.rmi.RemoteException;

import org.apache.log4j.Logger;

import ar.edu.itba.pod.legajo47126.communication.impl.ConnectionManagerImpl;
import ar.edu.itba.pod.legajo47126.communication.impl.message.MessageFactory;
import ar.edu.itba.pod.legajo47126.node.NodeManagement;
import ar.edu.itba.pod.legajo47126.simul.SimulationManagerImpl;
import ar.edu.itba.pod.simul.communication.Message;
import ar.edu.itba.pod.simul.communication.NodeAgentLoad;
import ar.edu.itba.pod.simul.simulation.Agent;

public class NewAgentCoordinator implements Runnable{

	// instance of the log4j logger
	private static Logger logger = Logger.getLogger(NewAgentCoordinator.class);
	
	private int coordinatorWaitTime;
	
	private Agent newAgent;
	
	public NewAgentCoordinator(Agent newAgent) {
		this.newAgent = newAgent;
		
		coordinatorWaitTime = NodeManagement.getConfigFile().getProperty("CoordinatorWaitTime", 10000);
	}
	
	@Override
	public void run() {
		
		// reset the node agents load
		NodeManagement.getNodeKnownAgentsLoad().reset();
		
		// broadcast a message saying that the local node is the new coordinator
		logger.debug("Start coordinating, inform all the others");
		Message message = MessageFactory.NodeAgentLoadRequestMessage();
		try {
			ConnectionManagerImpl.getInstance().getGroupCommunication().broadcast(message);
		} catch (RemoteException e) {
			logger.error("There was an error during the coordination broadcast");
			logger.error("Error message:" + e.getMessage());
		}
		
		// wait for the responses of the NODE_AGENTS_LOAD_REQUEST
		try {
			logger.debug("Waiting [" + coordinatorWaitTime + "] seconds for the arrival of the NODE_AGENTS_LOAD messages");
			Thread.sleep(coordinatorWaitTime);
		} catch (InterruptedException e) {
			logger.error("Interrupted while sleeping");
			logger.error("Error message:" + e.getMessage());
		}
		
		logger.debug("Waiting time ended, redistributing the node agents load...");
		
		// added the local node load to the list
		NodeManagement.getNodeKnownAgentsLoad().setNodeLoad(NodeManagement.getLocalNode().getNodeId(), 
				SimulationManagerImpl.getInstance().getAgentsLoad());
		
		NodeAgentLoad nodeAgentLoad;
		try {
			nodeAgentLoad = ConnectionManagerImpl.getInstance().getSimulationCommunication().getMinimumNodeKnownLoad();
		} catch (RemoteException e) {
			logger.error("There was an error during the minimum node known load calculation");
			logger.error("Error message:" + e.getMessage());
			
			logger.info("Coordination ended");
			return;
		}
		
		if(nodeAgentLoad == null){
			logger.error("No minimum node known load obtained");
			return;
		}
		else
			logger.debug("Obtained the node [" + nodeAgentLoad.getNodeId() + "] with the minimum load of " + nodeAgentLoad.getNumberOfAgents());
		
		// start it in the remote node
		try {
			ConnectionManagerImpl.getInstance().getConnectionManager(nodeAgentLoad.getNodeId()).
				getSimulationCommunication().startAgent(newAgent.getAgentDescriptor());
		} catch (RemoteException e) {
			logger.error("There was an error during the start of the new agent in the node [" + nodeAgentLoad.getNodeId() + "]");
			logger.error("Error message:" + e.getMessage());
		}
		
		logger.debug("Coordination ended");
	}

}
