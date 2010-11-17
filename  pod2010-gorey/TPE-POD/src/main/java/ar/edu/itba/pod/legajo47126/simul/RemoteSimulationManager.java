package ar.edu.itba.pod.legajo47126.simul;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

import ar.edu.itba.pod.simul.communication.AgentDescriptor;
import ar.edu.itba.pod.simul.simulation.Agent;
import ar.edu.itba.pod.simul.simulation.Simulation;
import ar.edu.itba.pod.simul.simulation.SimulationInspector;
import ar.edu.itba.pod.simul.simulation.SimulationManager;
import ar.edu.itba.pod.thread.doc.NotThreadSafe;

@NotThreadSafe
public class RemoteSimulationManager implements SimulationManager {
	
	// Maybe the agents should be in the Simulation
	private ConcurrentHashMap<AgentDescriptor, Agent> localAgents;
		
	@Override
	public void start() {
		// TODO Auto-generated method stub
	}
	
	@Override
	public void shutdown() {
		// TODO Auto-generated method stub
	}

	@Override
	public void addAgent(Agent agent) {
		localAgents.put(agent.getAgentDescriptor(), agent);
	}
	
	@Override
	public void removeAgent(Agent agent) {
		localAgents.remove(agent.getAgentDescriptor());
	}

	@Override
	public Simulation simulation() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public SimulationInspector inspector() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public <T> void register(Class<T> type, T instance) {
		// TODO Auto-generated method stub
	}
	
	@Override
	public Collection<Agent> getAgents() {
		return localAgents.values();
	}
	
}
