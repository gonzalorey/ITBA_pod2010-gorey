package ar.edu.itba.pod.legajo47126.simulation;

import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import ar.edu.itba.pod.legajo47126.node.NodeManagement;
import ar.edu.itba.pod.simul.simulation.Agent;
import ar.edu.itba.pod.simul.simulation.SimulationInspector;
import ar.edu.itba.pod.simul.simulation.SimulationManager;
import ar.edu.itba.pod.simul.time.TimeMapper;
import ar.edu.itba.pod.simul.time.TimeMappers;
import ar.edu.itba.pod.thread.doc.NotThreadSafe;

import com.google.common.base.Preconditions;

@NotThreadSafe
public class SimulationManagerImpl implements SimulationManager {
	
	// instance of the log4j logger
	private static Logger logger = Logger.getLogger(SimulationManagerImpl.class);
		
	private DistributedSimulation distributedSimulation;
	
	private boolean started = false;
	
	// instance of the node management
	NodeManagement nodeManagement;
	
	public SimulationManagerImpl(NodeManagement nodeManagement) {
		this.nodeManagement = nodeManagement;
		int amountOfHoursPerSecond = nodeManagement.getConfigFile().getProperty("AmountOfHoursPerSecond", 6); 
		TimeMapper timeMapper = TimeMappers.oneSecondEach(amountOfHoursPerSecond, TimeUnit.HOURS);
		distributedSimulation = new DistributedSimulation(timeMapper);
	}
	
	@Override
	public void start() {
		logger.info("Starting simulation...");
		distributedSimulation.start();
		started = true;
		logger.info("Simulation started");
	}
	
	@Override
	public void shutdown() {
		logger.info("Ending simulation...");
		simulation().shutdown();
		started = false;
		logger.info("Simulation ended");
	}

	@Override
	public void addAgent(Agent agent) {
		logger.info("Adding agent...");
		distributedSimulation.addAgent(agent);
	}
	
	@Override
	public void removeAgent(Agent agent) {
		logger.info("Removing agent...");
		distributedSimulation.removeAgent(agent);
	}

	@Override
	public DistributedSimulation simulation() {
		Preconditions.checkState(this.started, "No simulation has been started!");
		return distributedSimulation;
	}
	
	@Override
	public SimulationInspector inspector() {
		return simulation();
	}
	
	@Override
	public <T> void register(Class<T> type, T instance) {
		logger.info("Registering type " +  type.getSimpleName());
		distributedSimulation.register(type, instance);
	}
	
	@Override
	public Collection<Agent> getAgents() {
		return distributedSimulation.getAgents();
	}
	
	public int getAgentsLoad() {
		return distributedSimulation.getAgentsLoad();
	}
	
	public ConcurrentLinkedQueue<Agent> getAgentsLoadQueue(){
		return distributedSimulation.getAgentsLoadQueue();
	}
}
