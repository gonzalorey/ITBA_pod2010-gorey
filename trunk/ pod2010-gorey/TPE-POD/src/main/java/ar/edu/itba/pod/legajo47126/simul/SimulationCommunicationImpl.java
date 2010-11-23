package ar.edu.itba.pod.legajo47126.simul;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.log4j.Logger;

import ar.edu.itba.pod.legajo47126.node.NodeManagement;
import ar.edu.itba.pod.simul.communication.AgentDescriptor;
import ar.edu.itba.pod.simul.communication.NodeAgentLoad;
import ar.edu.itba.pod.simul.communication.SimulationCommunication;
import ar.edu.itba.pod.simul.simulation.Agent;

public class SimulationCommunicationImpl implements SimulationCommunication {
	
	// instance of the log4j logger
	private static Logger logger = Logger.getLogger(SimulationCommunicationImpl.class);
	
	public SimulationCommunicationImpl() throws RemoteException {
		UnicastRemoteObject.exportObject(this, 0);
	}
	
	@Override
	public void startAgent(AgentDescriptor descriptor) throws RemoteException {
		logger.debug("Starting agent [" +  descriptor + "]...");
		
		NodeManagement.getSimulationManager().addAgent(descriptor.build());
		logger.debug("Agent [" + descriptor + "] added to the node simulation");
	}

	@Override
	public NodeAgentLoad getMinimumNodeKnownLoad() throws RemoteException {
		
		Iterator<NodeAgentLoad> iter = NodeManagement.getNodeKnownAgentsLoad().getNodesLoad().iterator();  
		NodeAgentLoad minimumNodeKnownLoad = iter.next();
		
		while(iter.hasNext()){
			NodeAgentLoad nodeAgentLoad = iter.next();
			if(minimumNodeKnownLoad.getNumberOfAgents() > nodeAgentLoad.getNumberOfAgents())
				minimumNodeKnownLoad = nodeAgentLoad;
		}
		
		return minimumNodeKnownLoad;
	}
	
	@Override
	public void nodeLoadModified(NodeAgentLoad newLoad) throws RemoteException {
		// TODO Auto-generated method stub
		
		// Gustavo a Xin: Lo deberías usar si es que estas usando bully y un único coordinador... 
		// caso estes usando la metodología de un coordinador por evento, este método no te haría mucha falta.
	}

	@Override
	public Collection<AgentDescriptor> migrateAgents(int numberOfAgents) throws RemoteException {
		logger.debug("Migrating " + numberOfAgents + " agent" + ((numberOfAgents==1)?"":"s..."));
		
		Collection<AgentDescriptor> migratingAgents = new CopyOnWriteArrayList<AgentDescriptor>();
		for(int i = 0; i < numberOfAgents; i++){
			Agent agent = NodeManagement.getSimulationManager().getAgentsLoadQueue().remove();
			migratingAgents.add(agent.getAgentDescriptor());
			logger.debug("Agent [" + agent + "] removed from the node simulation");
		}
		
		return migratingAgents;
	}
}
