package ar.edu.itba.pod.legajo47126.simul.coordinator;

import java.rmi.RemoteException;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.log4j.Logger;

import ar.edu.itba.pod.legajo47126.communication.impl.ConnectionManagerImpl;
import ar.edu.itba.pod.legajo47126.node.NodeManagement;
import ar.edu.itba.pod.simul.communication.AgentDescriptor;
import ar.edu.itba.pod.simul.simulation.Agent;

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
	
	public NewNodeCoordinator() {
		this.coordinatorWaitTime = NodeManagement.getConfigFile().getProperty("CoordinatorWaitTime", 10000);
	}
	
	@Override
	public void run() {

		// reset the node agents load
		NodeManagement.getNodeAgentsLoad().reset();
		
		// wait for the responses of the NODE_AGENTS_LOAD_REQUEST
		try {
			logger.debug("Waiting [" + coordinatorWaitTime + "] seconds for the arrival of the NODE_AGENTS_LOAD messages");
			Thread.sleep(coordinatorWaitTime);
		} catch (InterruptedException e) {
			logger.error("Interrupted while sleeping");
			logger.error("Error message:" + e.getMessage());
		}
		
		logger.debug("Waiting time ended, redistributing the node agents load...");
		
		// TODO build a sorted list with the nodeAgentsLoad ordered by value, and get every value 
		// and if it's greater than the average load, migrate his agents
		
//		double loadPerNode = (double)(NodeManagement.getNodeAgentsLoad().getTotalLoad()) 
//			/ NodeManagement.getNodeAgentsLoad().getNodesLoad().size();
//		
//		CopyOnWriteArrayList<Agent> remainingAgents = new CopyOnWriteArrayList<Agent>();
//		CopyOnWriteArrayList<String> nodesLowOnAgents = new CopyOnWriteArrayList<String>();
//		
//		String nodeId;
//		int numberOfNodeRemainingAgents;
//		Iterator<String> iter = NodeManagement.getNodeAgentsLoad().getNodesLoad().keySet().iterator();
//		if(!iter.hasNext()){
//			return;
//		}
//		
//		nodeId = iter.next();
//		numberOfNodeRemainingAgents = NodeManagement.getNodeAgentsLoad().getNodesLoad().get(nodeId)
//			- (int)Math.ceil(loadPerNode); 
//		if(numberOfNodeRemainingAgents > 0){
//			for(AgentDescriptor agentDescriptor : ConnectionManagerImpl.getInstance().getConnectionManager(nodeId).
//					getSimulationCommunication().migrateAgents(numberOfNodeRemainingAgents)){
//				remainingAgents.add(agentDescriptor.build());
//			}
//		} else {
//			nodesLowOnAgents.add(nodeId);
//		}
//		
//		while(iter.hasNext()){
//			nodeId = iter.next();
//			numberOfNodeRemainingAgents = NodeManagement.getNodeAgentsLoad().getNodesLoad().get(nodeId)
//				- (int)Math.floor(loadPerNode);
//			if(numberOfNodeRemainingAgents > 0){
//				for(AgentDescriptor agentDescriptor : ConnectionManagerImpl.getInstance().getConnectionManager(nodeId).
//						getSimulationCommunication().migrateAgents(numberOfNodeRemainingAgents)){
//					remainingAgents.add(agentDescriptor.build());
//				}
//			} else {
//				for(Agent agent : remainingAgents){
//					// TODO give him some of the agents
//				}
//				nodesLowOnAgents.add(nodeId);
//			}
//		}
//		
//		for(String nodeId : NodeManagement.getNodeAgentsLoad().getNodesLoad().keySet()){
//			if(NodeManagement.getNodeAgentsLoad().getNodesLoad().get(nodeId) > loadPerNode){
//				
//			}
//				
//		}
		
		int loadPerNode = NodeManagement.getNodeAgentsLoad().getTotalLoad() 
			/ NodeManagement.getNodeAgentsLoad().getNodesLoad().size();
	
		CopyOnWriteArrayList<Agent> remainingAgents = new CopyOnWriteArrayList<Agent>();
		CopyOnWriteArrayList<String> lowOnAgentsNodes = new CopyOnWriteArrayList<String>();
		for(String nodeId : NodeManagement.getNodeAgentsLoad().getNodesLoad().keySet()){
			int numberOfNodeRemainingAgents = NodeManagement.getNodeAgentsLoad().
				getNodesLoad().get(nodeId) - loadPerNode;
			
			if(numberOfNodeRemainingAgents > 0){
				try {
					// obtain all his agents and add them to the remaining agents list
					for(AgentDescriptor agentDescriptor : ConnectionManagerImpl.getInstance().getConnectionManager(nodeId).
							getSimulationCommunication().migrateAgents(numberOfNodeRemainingAgents)){
						remainingAgents.add(agentDescriptor.build());
					}
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else if(numberOfNodeRemainingAgents < 0) {
				// add him to the list of low on agents nodes
				lowOnAgentsNodes.add(nodeId);
			}
		}
		
		for(String nodeId : lowOnAgentsNodes){
			int numberOfNodeRemainingAgents = NodeManagement.getNodeAgentsLoad().
			getNodesLoad().get(nodeId) - loadPerNode;
			
			giveAgents(nodeId, numberOfNodeRemainingAgents, remainingAgents);
		}
		
		if(remainingAgents.size() == 1){
			logger.debug("One node remaining, give it to the first node");
			
			giveAgents(NodeManagement.getNodeAgentsLoad().getNodesLoad().keySet().iterator().next(), 
					remainingAgents.size(), remainingAgents);
		}
		
	}

	private void giveAgents(String nodeId, int numberOfNodeRemainingAgents, CopyOnWriteArrayList<Agent> remainingAgents) {
		// TODO Auto-generated method stub
		
	}

}
