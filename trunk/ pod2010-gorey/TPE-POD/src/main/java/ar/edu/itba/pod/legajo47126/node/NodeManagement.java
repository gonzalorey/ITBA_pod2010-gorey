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
import ar.edu.itba.pod.simul.communication.ConnectionManager;
import ar.edu.itba.pod.simul.market.Market;
import ar.edu.itba.pod.simul.market.MarketManager;
import ar.edu.itba.pod.simul.simulation.SimulationManager;
import ar.edu.itba.pod.simul.ui.ConsoleFeedbackCallback;
import ar.edu.itba.pod.simul.ui.FeedbackMarketManager;

public class NodeManagement {
	
	// instance of the log4j logger
	private static Logger logger = Logger.getLogger(NodeManagement.class);
	
	// instance of the local machine node
	private Node localNode;
	
	// configuration file with the loaded properties
	private ConfigFile configFile;
	
	// instance of the connection manager
	private ConnectionManager connectionManager;
	
	// instance of the market manager
	private MarketManager marketManager;
	
	// instance of the simulation manager
	private SimulationManager simulationManager;
	
	// load of every known node
	private NodeKnownAgentsLoad nodeKnownAgentsLoad;	// TODO should go in SimulationCommunicationImpl...
	
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
		marketManager = new MarketManagerImpl(this);
		marketManager = new FeedbackMarketManager(new ConsoleFeedbackCallback(), marketManager);
		marketManager.start();
		logger.info("Market Manager started");
		
		// obtain the reference to the market
		Market market = marketManager.market();
		
		// instance the simulation manager
		simulationManager = new SimulationManagerImpl(this);
//		simulationManager = new FeedbackSimulationManager(new ConsoleFeedbackCallback(), simulationManager);
		
		// register the market in the simulation
		simulationManager.register(Market.class, market);
		
		// instance the node agents load
		nodeKnownAgentsLoad = new NodeKnownAgentsLoad();
		
		// create the connection manager
		connectionManager = new ConnectionManagerImpl(this);
		logger.info("Connection Manager initialized successfully");
				
	}
	
	public static void main(String[] args) {
		
		// set the basic configuration for the logger, so everything goes to stdout
		BasicConfigurator.configure();	//TODO set a propper configuration file for the logger
		
		// create the Node Management
		try {
			NodeManagement nodeManagement = new NodeManagement(args);
			
			// creaate and run the console
			new NodeConsole(nodeManagement).runConsole();
			
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

		logger.info("Bye!");

	}
	
	/**
	 * Get the reference of the local node
	 * 
	 * @return a reference to the local node
	 */
	public Node getLocalNode(){
		return localNode;
	}
	
	public ConfigFile getConfigFile() {
		return configFile;
	}
	
	public ConnectionManagerImpl getConnectionManager(){
		return (ConnectionManagerImpl) connectionManager;
	}
	
	public MarketManagerImpl getMarketManager() {
		return (MarketManagerImpl)marketManager;
	}
	
	public SimulationManagerImpl getSimulationManager(){
		return (SimulationManagerImpl)simulationManager;
	}

	public NodeKnownAgentsLoad getNodeKnownAgentsLoad(){
		return nodeKnownAgentsLoad;
	}
	
}
