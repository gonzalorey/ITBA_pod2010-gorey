package ar.edu.itba.pod.legajo47126.simulation;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import ar.edu.itba.pod.simul.simulation.Agent;
import ar.edu.itba.pod.simul.simulation.Simulation;
import ar.edu.itba.pod.simul.simulation.SimulationEvent;
import ar.edu.itba.pod.simul.simulation.SimulationEventHandler;
import ar.edu.itba.pod.simul.simulation.SimulationInspector;
import ar.edu.itba.pod.simul.time.TimeMapper;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

/**
 * The class was not extended from local simulation because it used a 
 * ConcurrentLinkedQueue instead of a CopyOnWriteArrayList
 * 
 * @author gorey
 *
 */
public class DistributedSimulation implements Simulation, SimulationInspector{
	
	// instance of the log4j logger
	//private static Logger logger = Logger.getLogger(SimulationManagerImpl.class);

	// Maybe the agents should be in the Simulation
	private ConcurrentLinkedQueue<Agent> localAgents;
	
	private TimeMapper timeMapper;
	
	private final CopyOnWriteArrayList<SimulationEventHandler> handlers =  new CopyOnWriteArrayList<SimulationEventHandler>();
	
	private final Map<Class<?>, Object> env = Maps.newHashMap();
	
	private SimulationManagerImpl simulationManager;
	
	public DistributedSimulation(TimeMapper timeMapper, SimulationManagerImpl simulationManagerImpl) {
		this.timeMapper = timeMapper;
		this.simulationManager = simulationManagerImpl;
		
		localAgents = new ConcurrentLinkedQueue<Agent>();
	}
	
	@Override
	public void wait(int amount, TimeUnit unit) throws InterruptedException {
		long millis = this.timeMapper.toMillis(amount, unit);
		Thread.sleep(millis);
	}

	@Override
	public void add(SimulationEventHandler handler) {
		Preconditions.checkArgument(!handlers.contains(handler), "Can't add a handler twice!");
		handlers.add(handler);
	}
	
	@Override
	public void remove(SimulationEventHandler handler) {
		Preconditions.checkArgument(handlers.contains(handler), "Handler not registered!");
		handlers.remove(handler);
	}

	@Override
	public void raise(SimulationEvent event) {
		for(SimulationEventHandler handler : handlers) {
			handler.onEvent(event);
		}

	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> T env(Class<T> param) {
		return (T) env.get(param);
	}

	@Override
	public int runningAgents() {
		return localAgents.size();
	}
	
	public void addAgent(Agent agent) {
		Preconditions.checkArgument(!agent.isAlive(), "Can't add an agent that is already started!");
		Preconditions.checkArgument(!localAgents.contains(agent), "Can't add an agent twice!");
		
		localAgents.add(agent);
		agent.onBind(this);
		
		if(simulationManager.isStarted())
			agent.start();
	}
	
	public void removeAgent(Agent agent) {
		Preconditions.checkArgument(localAgents.contains(agent), "The agent is not part of this simulation!");
		try {
			synchronized (agent) {
				agent.finish();
				agent.wait();
			}
		}
		catch (InterruptedException e) {
			throw new IllegalStateException("Interrupted while removing an agent!");
		}
		localAgents.remove(agent);
	}

	public Collection<Agent> getAgents() {
		return new CopyOnWriteArrayList<Agent>(localAgents);
	}

	public int getAgentsLoad() {
		return localAgents.size();
	}

	public ConcurrentLinkedQueue<Agent> getAgentsLoadQueue() {
		return localAgents;
	}

	public void start() {
//		for(Agent agent : localAgents){
//			agent.start();
//		}
	}

	/**
	 * Shuts down the simulation by asking agents to stop running.
	 * Note that this method doesn't send any event to the agents
	 */
	public void shutdown() {
		boolean interrupted = false;
		
		for (Agent agent : localAgents) {
			agent.finish();
		}
		for (Agent agent : localAgents) {
			try {
				agent.join();
			} catch (InterruptedException e) {
				interrupted = true;
			}
		}
		if (interrupted) {
			throw new IllegalStateException("Interrupted when shutting down agents!");
		}
		
	}
	
	public <T> void register(Class<T> type, T instance) {
		env.put(type, instance);
	}

}
