package ar.edu.itba.pod.legajo47126.simul;

import java.util.concurrent.TimeUnit;

import ar.edu.itba.pod.simul.simulation.Simulation;
import ar.edu.itba.pod.simul.simulation.SimulationEvent;
import ar.edu.itba.pod.simul.simulation.SimulationEventHandler;
import ar.edu.itba.pod.simul.simulation.SimulationInspector;

public class DistributedSimulation implements Simulation, SimulationInspector{
	
	@Override
	public void wait(int amount, TimeUnit unit) throws InterruptedException {
		// TODO Auto-generated method stub

	}

	@Override
	public void add(SimulationEventHandler handler) {
		// TODO Auto-generated method stub

	}
	
	@Override
	public void remove(SimulationEventHandler handler) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void raise(SimulationEvent event) {
		// TODO Auto-generated method stub

	}
	
	@Override
	public <T> T env(Class<T> param) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int runningAgents() {
		// TODO Auto-generated method stub
		return 0;
	}

}
