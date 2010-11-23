package ar.edu.itba.pod.legajo47126.node;

import java.io.IOException;
import java.net.UnknownHostException;
import java.rmi.RemoteException;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import ar.edu.itba.pod.legajo47126.communication.ConnectionManagerImpl;
import ar.edu.itba.pod.legajo47126.configuration.ConfigFile;
import ar.edu.itba.pod.legajo47126.simul.MarketManagerImpl;
import ar.edu.itba.pod.legajo47126.simul.SimulationManagerImpl;
import ar.edu.itba.pod.simul.market.Market;
import ar.edu.itba.pod.simul.market.MarketManager;
import ar.edu.itba.pod.simul.simulation.SimulationManager;
import ar.edu.itba.pod.simul.ui.ConsoleFeedbackCallback;
import ar.edu.itba.pod.simul.ui.FeedbackMarketManager;

public class NodeManagement {
	
	// instance of the log4j logger
	private static Logger logger = Logger.getLogger(NodeManagement.class);
	
	// instance of the local machine node
	private static Node localNode;
	
	// configuration file with the loaded properties
	private static ConfigFile configFile;
	
	// instance of the market manager
	private static MarketManager marketManager;
	
	// instance of the simulation manager
	private static SimulationManager simulationManager;
	
	// load of every known node
	private static NodeKnownAgentsLoad nodeKnownAgentsLoad;	// TODO should go in SimulationCommunicationImpl...
	
	public NodeManagement(String[] args) throws UnknownHostException, IOException, RemoteException {
		
		if(args.length == 1)
			localNode = new Node(args[0]);
		else
			localNode = new Node();
		logger.info("Node '" + localNode + "' started successfully");

		// configuration class to get the properties from the config file
		String configFileName = "node.conf";
		configFile = new ConfigFile(configFileName);
		
		logger.info("Starting the Market Manager...");
		marketManager = new MarketManagerImpl();
		marketManager = new FeedbackMarketManager(new ConsoleFeedbackCallback(), marketManager);
		marketManager.start();
		logger.info("Market Manager started");
		
		// obtain the reference to the market
		Market market = marketManager.market();
		
		// instance the simulation manager
		simulationManager = new SimulationManagerImpl();
//		simulationManager = new FeedbackSimulationManager(callback, simulationManager);
		
		// register the market in the simulation
		simulationManager.register(Market.class, market);
		
		// instance the node agents load
		nodeKnownAgentsLoad = new NodeKnownAgentsLoad();
		
		// create the connection manager
		ConnectionManagerImpl.getInstance();
		logger.info("Connection Manager initialized successfully");
				
		// creaate and run the console
		new NodeConsole().runConsole();

		logger.info("Bye!");
	}
	
	public static void main(String[] args) {
		
		// set the basic configuration for the logger, so everything goes to stdout
		BasicConfigurator.configure();	//TODO set a propper configuration file for the logger
		
		// create the Node Management
		try {
			new NodeManagement(args);
		} catch (UnknownHostException e) {
			logger.fatal("The local node couldn't be started. Aborting execution");
			logger.fatal("Error message:" + e.getMessage());
			return;
		} catch (RemoteException e) {
			logger.fatal("There was an error during the Connection Manager initialization. Aborting execution");
			logger.fatal("Error message:" + e.getMessage());
			return;
		} catch (IOException e) {
			logger.error("'node.conf' file not found. Using default configurations");
            logger.error("Error message:" + e.getMessage());
		} catch (Exception e) {
			logger.error("An unexpected exception ocurred");
            logger.error("Error message:" + e.getMessage());
		}
	}
	
	/**
	 * Get the reference of the local node
	 * 
	 * @return a reference to the local node
	 */
	public static Node getLocalNode(){
		return localNode;
	}
	
	public static ConfigFile getConfigFile() {
		return configFile;
	}
	
	public static MarketManagerImpl getMarketManager() {
		return (MarketManagerImpl)marketManager;
	}
	
	public static SimulationManagerImpl getSimulationManager(){
		return (SimulationManagerImpl)simulationManager;
	}

	public static NodeKnownAgentsLoad getNodeKnownAgentsLoad(){
		return nodeKnownAgentsLoad;
	}
	
}
