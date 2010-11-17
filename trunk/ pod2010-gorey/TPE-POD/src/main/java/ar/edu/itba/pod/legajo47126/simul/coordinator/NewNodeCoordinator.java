package ar.edu.itba.pod.legajo47126.simul.coordinator;

import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.log4j.Logger;

import ar.edu.itba.pod.legajo47126.communication.impl.ConnectionManagerImpl;
import ar.edu.itba.pod.legajo47126.node.NodeManagement;
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
		// wait for the responses of the NODE_AGENTS_LOAD_REQUEST
		try {
			logger.debug("Waiting [" + coordinatorWaitTime + "] seconds for the arrival of the NODE_AGENTS_LOAD messages");
			Thread.sleep(coordinatorWaitTime);
		} catch (InterruptedException e) {
			logger.error("Interrupted while sleeping");
			logger.error("Error message:" + e.getMessage());
		}
		
		logger.debug("Waiting time ended, redistributing the node agents load...");
		
		int totalLoad = 0;
		for(Integer load : NodeManagement.getNodeAgentsLoad().values()){
			totalLoad += load;
		}
		
		// TODO build a sorted list with the nodeAgentsLoad ordered by value, and get every value 
		// and if it's greater than the average load, migrate his agents
		
		CopyOnWriteArrayList<Agent> auxAgentsList = new CopyOnWriteArrayList<Agent>();
		
		for(String nodeId : NodeManagement.getNodeAgentsLoad().keySet()){
			logger.debug("Processing node [" + nodeId + "] with load [" + NodeManagement.getNodeAgentsLoad().get(nodeId) + "]");
			// TODO process them... distribute them among the other nodes
		}
		
	}

}
