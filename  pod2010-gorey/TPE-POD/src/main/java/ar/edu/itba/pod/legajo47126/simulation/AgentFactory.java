package ar.edu.itba.pod.legajo47126.simulation;

import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

import ar.edu.itba.pod.simul.market.Resource;
import ar.edu.itba.pod.simul.simulation.Agent;
import ar.edu.itba.pod.simul.units.Factory;
import ar.edu.itba.pod.simul.units.SimpleConsumer;
import ar.edu.itba.pod.simul.units.SimpleProducer;

public class AgentFactory {
	public static Agent createProducerAgent(){
		Resource pigIron = new Resource("Mineral", "Pig Iron");
		
		return SimpleProducer.named("pig iron mine")
		.producing(2).of(pigIron)
		.every(12, TimeUnit.HOURS)
		.build();
	}
	
	public static Agent createConsumerAgent(){
		Resource pigIron = new Resource("Mineral", "Pig Iron");
		Resource copper = new Resource("Mineral", "Copper");
		Resource steel = new Resource("Alloy", "Steel");
		
		return Factory.named("steel refinery")
		.using(5, pigIron).and(2, copper)
		.producing(6, steel)
		.every(1, TimeUnit.DAYS)
		.build();
	}
	
	public static Collection<Agent> createSimulationAgents(){
		Collection<Agent> agents = new LinkedList<Agent>();
		
		Resource pigIron = new Resource("Mineral", "Pig Iron");
		Resource copper = new Resource("Mineral", "Copper");
		Resource steel = new Resource("Alloy", "Steel");
		
		Agent mine1 = SimpleProducer.named("pig iron mine")
									.producing(2).of(pigIron)
									.every(12, TimeUnit.HOURS)
									.build();
		Agent mine2 = SimpleProducer.named("copper mine")
									.producing(4).of(copper)
									.every(1, TimeUnit.DAYS)
									.build();
		Agent refinery = Factory.named("steel refinery")
									.using(5, pigIron).and(2, copper)
									.producing(6, steel)
									.every(1, TimeUnit.DAYS)
									.build();
		Agent factory = SimpleConsumer.named("factory")
									.consuming(10).of(steel)
									.every(2, TimeUnit.DAYS)
									.build();

		agents.add(mine1);
		agents.add(mine2);
		agents.add(refinery);
		agents.add(factory);
		
		return agents;
	}
}
