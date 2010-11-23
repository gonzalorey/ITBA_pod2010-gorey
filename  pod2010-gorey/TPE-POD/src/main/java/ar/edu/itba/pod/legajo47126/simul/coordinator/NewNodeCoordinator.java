package ar.edu.itba.pod.legajo47126.simul.coordinator;

import java.rmi.RemoteException;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.log4j.Logger;

import ar.edu.itba.pod.legajo47126.communication.message.MessageFactory;
import ar.edu.itba.pod.legajo47126.node.NodeManagement;
import ar.edu.itba.pod.legajo47126.simulation.NodeKnownAgentsLoad;
import ar.edu.itba.pod.legajo47126.simulation.SimulationCommunicationImpl;
import ar.edu.itba.pod.simul.communication.AgentDescriptor;
import ar.edu.itba.pod.simul.communication.Message;
import ar.edu.itba.pod.simul.communication.NodeAgentLoad;

/**
 * Class called after a NODE_AGENT_LOAD_REQUEST message was sent, in order to
 * get the agent loads of each group node and redistribute them. This class must
 * be called using a thread in order to avoid blocking the colling service.
 * 
 * @author gorey
 *
 */
public class NewNodeCoordinator implements Runnable{
	
	// instance of the log4j logger
	private static Logger logger = Logger.getLogger(NewNodeCoordinator.class);

	private int coordinatorWaitTime;
	
	private NodeManagement nodeManagement;
	
	public NewNodeCoordinator(NodeManagement nodeManagement) {
		this.nodeManagement = nodeManagement;
		this.coordinatorWaitTime = nodeManagement.getConfigFile().getProperty("CoordinatorWaitTime", 10000);
	}
	
	@Override
	public void run() {

		// get the node agents load
		NodeKnownAgentsLoad nodeKnownAgentsLoad;
		try {
			nodeKnownAgentsLoad = ((SimulationCommunicationImpl) nodeManagement.getConnectionManager().
					getSimulationCommunication()).getNodeKnownAgentsLoad();
		} catch (RemoteException e) {
			logger.error("There was an error while trying to get the node known agents load");
			logger.error("Error message: " + e.getMessage());
			return;
		}
		nodeKnownAgentsLoad.reset();
		
		// broadcast a message saying that the local node is the new coordinator
		logger.debug("Start coordinating, inform all the others");
		Message message = MessageFactory.NodeAgentLoadRequestMessage(nodeManagement.getLocalNode().getNodeId());
		try {
			nodeManagement.getConnectionManager().getGroupCommunication().broadcast(message);
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
		nodeKnownAgentsLoad.setNodeLoad(nodeManagement.getLocalNode().getNodeId(), 
				nodeManagement.getSimulationManager().getAgentsLoad());
		
		if(nodeKnownAgentsLoad.getTotalLoad() == 0 || 
				nodeKnownAgentsLoad.getNodesLoad().size() == 0){
			logger.debug("No nodes to distribute the load, coordination ended");
			return;
		}
		
		int loadPerNode = nodeKnownAgentsLoad.getTotalLoad() 
			/ nodeKnownAgentsLoad.getNodesLoad().size();
	
		ConcurrentLinkedQueue<AgentDescriptor> remainingAgents = new ConcurrentLinkedQueue<AgentDescriptor>();
		ConcurrentLinkedQueue<NodeAgentLoad> lowOnAgentsNodes = new ConcurrentLinkedQueue<NodeAgentLoad>();

		for(NodeAgentLoad nodeAgentLoad : nodeKnownAgentsLoad.getNodesLoad()){
			int numberOfNodeRemainingAgents = nodeAgentLoad.getNumberOfAgents() - loadPerNode;
			
			if(numberOfNodeRemainingAgents > 0){
				try {
					// obtain all his agents and add them to the remaining agents list
					for(AgentDescriptor agentDescriptor : nodeManagement.getConnectionManager().getConnectionManager(nodeAgentLoad.getNodeId()).
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
				
				//TODO what should I do in this case, what to do with this remaining nodes
			}
		}
		
		if(remainingAgents.size() > 0){
			logger.debug(remainingAgents.size() + " remaining, give them to the local node");
			
			try {
				giveAgents(nodeManagement.getLocalNode().getNodeId(), remainingAgents.size(), remainingAgents);
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
			nodeManagement.getConnectionManager().getConnectionManager(nodeId).
				getSimulationCommunication().startAgent(agentDescriptor);
			
			// remove the agent from the queue
			remainingAgents.remove(agentDescriptor);
		}
	}

}
