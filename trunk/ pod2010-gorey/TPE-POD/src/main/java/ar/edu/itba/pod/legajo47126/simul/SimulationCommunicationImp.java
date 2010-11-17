package ar.edu.itba.pod.legajo47126.simul;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.log4j.Logger;

import ar.edu.itba.pod.legajo47126.node.NodeManagement;
import ar.edu.itba.pod.simul.communication.AgentDescriptor;
import ar.edu.itba.pod.simul.communication.NodeAgentLoad;
import ar.edu.itba.pod.simul.communication.SimulationCommunication;
import ar.edu.itba.pod.simul.simulation.Agent;
import ar.edu.itba.pod.simul.simulation.SimulationManager;

public class SimulationCommunicationImp implements SimulationCommunication {
	
	// instance of the log4j logger
	private static Logger logger = Logger.getLogger(NodeManagement.class);
	
	// the current simulation manager
	SimulationManager simulationManager;
	
	public SimulationCommunicationImp(SimulationManager simulationManager) {
		this.simulationManager = simulationManager;
	}
	
	@Override
	public void startAgent(AgentDescriptor descriptor) throws RemoteException {
		logger.debug("Starting agent with the descriptor [" +  descriptor + "]...");
		
		// TODO I could ask the simulation manager to fetch the agent for me...
		Collection<Agent> simulationAgents =  simulationManager.getAgents();
		for(Agent agent : simulationAgents){
			if(agent.getAgentDescriptor().equals(descriptor)){
				agent.start();
				logger.debug("Agent [" + agent + "] started");
				break;
			}
		}
	}

	@Override
	public NodeAgentLoad getMinimumNodeKnownLoad() throws RemoteException {
		// TODO Auto-generated method stub
		// call this method only when a new agent is loaded, and add it to the minimum load node
		return null;
	}
	
	@Override
	public void nodeLoadModified(NodeAgentLoad newLoad) throws RemoteException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Collection<AgentDescriptor> migrateAgents(int numberOfAgents) throws RemoteException {
		logger.debug("Migrating " + numberOfAgents + " agent" + ((numberOfAgents==1)?"":"s..."));
		
		Collection<AgentDescriptor> migratingAgents = new CopyOnWriteArrayList<AgentDescriptor>();
		for(Agent agent : simulationManager.getAgents()){
			if(numberOfAgents == migratingAgents.size())
				break;
			
			migratingAgents.add(agent.getAgentDescriptor());
			
			// removing agent from the simulation
			simulationManager.removeAgent(agent);
			logger.debug("Agent [" + agent + "] removed from the simulation");
		}
		
		return migratingAgents;
	}

}
