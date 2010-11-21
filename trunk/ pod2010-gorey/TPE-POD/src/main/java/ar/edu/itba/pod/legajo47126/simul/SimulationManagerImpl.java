package ar.edu.itba.pod.legajo47126.simul;

import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.log4j.Logger;

import ar.edu.itba.pod.simul.simulation.Agent;
import ar.edu.itba.pod.simul.simulation.Simulation;
import ar.edu.itba.pod.simul.simulation.SimulationInspector;
import ar.edu.itba.pod.simul.simulation.SimulationManager;
import ar.edu.itba.pod.thread.doc.NotThreadSafe;

@NotThreadSafe
public class SimulationManagerImpl implements SimulationManager {
	
	// instance of the log4j logger
	private static Logger logger = Logger.getLogger(SimulationManagerImpl.class);
		
	// singletone instance of the ConnectionManger
	private static SimulationManagerImpl simulationManager = null;
	
	// Maybe the agents should be in the Simulation
	private ConcurrentLinkedQueue<Agent> localAgents;
	
	private SimulationManagerImpl() {
		localAgents = new ConcurrentLinkedQueue<Agent>();
	}
	
	public static synchronized SimulationManagerImpl getInstance() {
		if(simulationManager == null)
			SimulationManagerImpl.simulationManager = new SimulationManagerImpl();

		return SimulationManagerImpl.simulationManager;
	}
		
	@Override
	protected Object clone() throws CloneNotSupportedException {
		// it won't be cloned now either
		throw new CloneNotSupportedException();
	}
	
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
		localAgents.add(agent);
	}
	
	@Override
	public void removeAgent(Agent agent) {
		if(!localAgents.contains(agent.getAgentDescriptor()))
			logger.warn("The agent doesn't belong to the map");
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
		return new CopyOnWriteArrayList<Agent>(localAgents);
	}
	
	public int getAgentsLoad() {
		return localAgents.size();
	}
	
	public ConcurrentLinkedQueue<Agent> getAgentsLoadQueue(){
		return localAgents;
	}
}
