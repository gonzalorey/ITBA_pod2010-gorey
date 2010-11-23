package ar.edu.itba.pod.legajo47126.simul;

import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

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
		
	// singletone instance of the ConnectionManger
	private static SimulationManagerImpl simulationManager = null;
	
	private DistributedSimulation distributedSimulation;
	
	private boolean started;
	
	private SimulationManagerImpl() {
		TimeMapper timeMapper = TimeMappers.oneSecondEach(6, TimeUnit.HOURS);	// TODO hardcoded, maybe by parameter...
		distributedSimulation = new DistributedSimulation(timeMapper);

		logger.debug("Registering the distributed simulation");
		register(DistributedSimulation.class, distributedSimulation);
		
		started = false;
	}
	
	public static synchronized SimulationManagerImpl getInstance() {
		if(simulationManager == null){
			SimulationManagerImpl.simulationManager = new SimulationManagerImpl();
		}

		return SimulationManagerImpl.simulationManager;
	}
		
	@Override
	protected Object clone() throws CloneNotSupportedException {
		// it won't be cloned now either
		throw new CloneNotSupportedException();
	}
	
	@Override
	public void start() {
		distributedSimulation.start();
		started = true;
	}
	
	@Override
	public void shutdown() {
		distributedSimulation.shutdown();
		started = false;
	}

	@Override
	public void addAgent(Agent agent) {
		distributedSimulation.addAgent(agent);
	}
	
	@Override
	public void removeAgent(Agent agent) {
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
