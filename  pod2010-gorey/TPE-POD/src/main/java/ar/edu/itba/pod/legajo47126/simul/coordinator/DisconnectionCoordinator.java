package ar.edu.itba.pod.legajo47126.simul.coordinator;

import java.rmi.RemoteException;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.log4j.Logger;

import ar.edu.itba.pod.legajo47126.communication.ConnectionManagerImpl;
import ar.edu.itba.pod.legajo47126.communication.message.MessageFactory;
import ar.edu.itba.pod.legajo47126.node.NodeManagement;
import ar.edu.itba.pod.simul.communication.AgentDescriptor;
import ar.edu.itba.pod.simul.communication.Message;
import ar.edu.itba.pod.simul.communication.NodeAgentLoad;

public class DisconnectionCoordinator implements Runnable{
	
	// instance of the log4j logger
	private static Logger logger = Logger.getLogger(NewAgentCoordinator.class);
	
	private int coordinatorWaitTime;
	
	public DisconnectionCoordinator() {
		coordinatorWaitTime = NodeManagement.getConfigFile().getProperty("CoordinatorWaitTime", 10000);
	}

	@Override
	public void run() {
		
		if(NodeManagement.getSimulationManager().getAgentsLoad() == 0){
			logger.debug("Nothing to distribute, coordination ended");
			return;
		}
		
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
		
		if(NodeManagement.getNodeKnownAgentsLoad().getTotalLoad() == 0 || 
				NodeManagement.getNodeKnownAgentsLoad().getNodesLoad().size() == 0){
			logger.debug("No nodes to distribute the load, coordination ended");
			return;
		}
		
		int loadPerNode = (NodeManagement.getNodeKnownAgentsLoad().getTotalLoad() 
			+ NodeManagement.getSimulationManager().getAgentsLoad())
			/ NodeManagement.getNodeKnownAgentsLoad().getNodesLoad().size();
	
		ConcurrentLinkedQueue<AgentDescriptor> remainingAgents = new ConcurrentLinkedQueue<AgentDescriptor>();
		ConcurrentLinkedQueue<NodeAgentLoad> lowOnAgentsNodes = new ConcurrentLinkedQueue<NodeAgentLoad>();

		// migrate the local node agents
		try {
			for(AgentDescriptor agentDescriptor : ConnectionManagerImpl.getInstance().getSimulationCommunication().
					migrateAgents(NodeManagement.getSimulationManager().getAgentsLoad())){
				remainingAgents.add(agentDescriptor);
			}
		} catch (RemoteException e) {
			logger.error("There was an error during the migration of the local node agents");
			logger.error("Error message:" +  e.getMessage());
		}

		for(NodeAgentLoad nodeAgentLoad : NodeManagement.getNodeKnownAgentsLoad().getNodesLoad()){
			int numberOfNodeRemainingAgents = nodeAgentLoad.getNumberOfAgents() - loadPerNode;
			
			if(numberOfNodeRemainingAgents > 0){
				try {
					// obtain all his agents and add them to the remaining agents list
					for(AgentDescriptor agentDescriptor : ConnectionManagerImpl.getInstance().getConnectionManager(nodeAgentLoad.getNodeId()).
							getSimulationCommunication().migrateAgents(numberOfNodeRemainingAgents)){
						remainingAgents.add(agentDescriptor);
					}
				} catch (RemoteException e) {
					logger.error("There was an error during the migration of the node [" + nodeAgentLoad.getNodeId() + "] agents");
					logger.error("Error message:" + e.getMessage());
				}
			} else if(numberOfNodeRemainingAgents < 0) {
				// add him to the list of low on agents nodes
				lowOnAgentsNodes.add(nodeAgentLoad);
			}
		}
		
		for(NodeAgentLoad nodeAgentLoad : lowOnAgentsNodes){
			int numberOfNodeRemainingAgents = loadPerNode - nodeAgentLoad.getNumberOfAgents();
			
			try {
				giveAgents(nodeAgentLoad.getNodeId(), numberOfNodeRemainingAgents, remainingAgents);
			} catch (RemoteException e) {
				logger.debug("There was an error and the agent/s couldn't be added to the node");
				logger.debug("Error message:" + e.getMessage());
			}
		}
		
		if(remainingAgents.size() > 0){
			logger.debug(remainingAgents.size() + " remaining, give them the first in the list");
			
			try {
				giveAgents(NodeManagement.getNodeKnownAgentsLoad().getNodesLoad().peek().getNodeId(), 
						remainingAgents.size(), remainingAgents);
			} catch (RemoteException e) {
				logger.debug("There was an error and the agent/s couldn't be added to the node");
				logger.debug("Error message:" + e.getMessage());
			}
		}
		
		logger.debug("Coordination ended");
	}

	private void giveAgents(String nodeId, int numberOfNodeRemainingAgents, ConcurrentLinkedQueue<AgentDescriptor> remainingAgents) throws RemoteException {
		logger.debug("Giving " + numberOfNodeRemainingAgents + " agents to the node [" + nodeId + "]");
		
		for(int i = 0; i < numberOfNodeRemainingAgents; i++){
			// take the first agent from the queue
			AgentDescriptor agentDescriptor = remainingAgents.peek();
			
			// start it in the remote node
			ConnectionManagerImpl.getInstance().getConnectionManager(nodeId).
				getSimulationCommunication().startAgent(agentDescriptor);
			
			// remove the agent from the queue
			remainingAgents.remove(agentDescriptor);
		}
	}
	
}
