package ar.edu.itba.pod.legajo47126.simulation.loadtest;

import java.io.IOException;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

import ar.edu.itba.pod.legajo47126.simul.ObjectFactoryAlternativeImpl;
import ar.edu.itba.pod.legajo47126.simulation.statistics.GetStatistics;
import ar.edu.itba.pod.simul.ObjectFactoryAlternative;
import ar.edu.itba.pod.simul.market.Market;
import ar.edu.itba.pod.simul.market.Resource;
import ar.edu.itba.pod.simul.simulation.Agent;
import ar.edu.itba.pod.simul.units.Factory;
import ar.edu.itba.pod.simul.units.SimpleConsumer;
import ar.edu.itba.pod.simul.units.SimpleProducer;

public class SingleNode {
	
	public static void main(String[] args) {
		try {
			ObjectFactoryAlternative ofa = new ObjectFactoryAlternativeImpl(args);
			
			// start the market
			ofa.getMarketManager().start();
			
			// register it
			ofa.getSimulationManager().register(Market.class, ofa.getMarketManager().market());
			
			// create a group
			ofa.createGroup();
			
			// define simulation agents
			Resource pigIron = new Resource("Mineral", "Pig Iron");
			Resource copper = new Resource("Mineral", "Copper");
			Resource steel = new Resource("Alloy", "Steel");
			Resource diamond = new Resource("Mineral", "Diamond");
			Resource gold = new Resource("Mineral", "Gold");
			
			// create the agents
			Agent mine1 = SimpleProducer.named("pig iron mine")
										.producing(2).of(pigIron)
										.every(12, TimeUnit.HOURS)
										.build();
			Agent mine2 = SimpleProducer.named("copper mine")
										.producing(4).of(copper)
										.every(1, TimeUnit.DAYS)
										.build();
			Agent mine3 = SimpleProducer.named("diamond mine")
										.producing(1).of(diamond)
										.every(2, TimeUnit.DAYS).
										build();
			Agent mine4 = SimpleProducer.named("gold mine")
										.producing(6).of(gold).
										every(6, TimeUnit.HOURS).
										build();
			Agent refinery = Factory.named("steel refinery")
										.using(5, pigIron)
										.and(2,copper)
										.and(1, diamond)
										.producing(6, steel)
										.every(1, TimeUnit.DAYS)
										.build();
			Agent factory = SimpleConsumer.named("factory")
										.consuming(10).of(steel)
										.consuming(2).of(gold)
										.every(2, TimeUnit.DAYS)
										.build();
			
			// put the agents in a queue
			ConcurrentLinkedQueue<Agent> agentsQueue = new ConcurrentLinkedQueue<Agent>();
			agentsQueue.add(mine1);
			agentsQueue.add(mine2);
			agentsQueue.add(mine3);
			agentsQueue.add(mine4);
			agentsQueue.add(refinery);
			agentsQueue.add(factory);
			
			long numberOfAgents = 0;
			while(true){
				// retrieve it from the top of the queue
				Agent aux = agentsQueue.remove();
				
				// add agent to the simulation
				ofa.getSimulationManager().addAgent(aux);
				
				System.out.println("Number of running agents: " + ++numberOfAgents);
				
				// put it in the tail
				agentsQueue.add(aux);

				Thread thread = new Thread(new GetStatistics(((ObjectFactoryAlternativeImpl) ofa).getNodeManagement()));
				thread.start();
				
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	
}
