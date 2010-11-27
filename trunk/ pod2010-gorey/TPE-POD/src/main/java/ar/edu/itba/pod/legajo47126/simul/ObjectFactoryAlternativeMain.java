package ar.edu.itba.pod.legajo47126.simul;

import java.io.IOException;
import java.net.UnknownHostException;
import java.rmi.RemoteException;

import org.apache.log4j.Logger;

import ar.edu.itba.pod.legajo47126.node.NodeConsole;
import ar.edu.itba.pod.simul.ObjectFactoryAlternative;

public class ObjectFactoryAlternativeMain {
	
	// instance of the log4j logger
	private static Logger logger = Logger.getLogger(ObjectFactoryAlternativeMain.class);
	
	public static void main(String[] args) {
		try {
			ObjectFactoryAlternative ofa = new ObjectFactoryAlternativeImpl(args);
			
			new NodeConsole().runConsole(ofa);
//			
//			ofa.getMarketManager().start();
//			ofa.getSimulationManager().register(Market.class, ofa.getMarketManager().market());
//			
//			Agent mine1 = AgentFactory.createSimpleProducer(SimpleProducers.PIG_IRON_MINE);
//			Agent mine2 = AgentFactory.createSimpleProducer(SimpleProducers.COPPER_MINE);
//			Agent factory = AgentFactory.createSimpleConsumer(SimpleConsumers.FACTORY);
//			Agent refinery = AgentFactory.createConsumerProducer(ConsumersProducers.STEEL_REFINERY);
//			
//			ofa.getSimulationManager().addAgent(mine1);
//			ofa.getSimulationManager().addAgent(mine2);
//			ofa.getSimulationManager().addAgent(factory);
//			ofa.getSimulationManager().addAgent(refinery);
//			
//			ofa.getSimulationManager().start();
//			try {
//				Thread.sleep(1000 * 20);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//			ofa.getSimulationManager().shutdown();
//			
//			ofa.getMarketManager().shutdown();
//			
			logger.warn("Calling a local method (cast needed)");
			((ObjectFactoryAlternativeImpl) ofa).closeObject();
			
		} catch (UnknownHostException e) {
			logger.error("Unknown host, aborting...", e);
		} catch (RemoteException e) {
			logger.error("Remote exception triggered, aborting...", e);
		} catch (IOException e) {
			logger.error("IO exception triggered, aborting...", e);
		}
		
		System.out.println("Bye!");
	}
}
