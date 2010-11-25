package ar.edu.itba.pod.legajo47126.node;

import java.io.IOException;
import java.net.URL;
import java.net.UnknownHostException;
import java.rmi.RemoteException;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import ar.edu.itba.pod.legajo47126.communication.ConnectionManagerImpl;
import ar.edu.itba.pod.legajo47126.configuration.ConfigFile;
import ar.edu.itba.pod.legajo47126.market.MarketManagerImpl;
import ar.edu.itba.pod.legajo47126.simulation.SimulationManagerImpl;
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
	
	// instance of the Connection Manager
	private ConnectionManager connectionManager;
	
	// instance of the Market Manager
	private MarketManager marketManager;
	
	// instance of the Simulation Manager
	private SimulationManager simulationManager;
	
	private boolean shouldExit;
	
	public NodeManagement(String[] args) throws UnknownHostException, IOException, RemoteException {
		
		if(args.length == 1)
			localNode = new Node(args[0], this);
		else
			localNode = new Node(this);
		logger.info("Node '" + localNode + "' started successfully");

		// configuration class to get the properties from the config file
		String configFileName = "node.conf";
		configFile = new ConfigFile(configFileName);
		
//		URL uri = NodeManagement.class.getResource("node.conf");
//		configFile = new ConfigFile(uri.getPath());
		
		marketManager = new MarketManagerImpl(this);
		marketManager = new FeedbackMarketManager(new ConsoleFeedbackCallback(), marketManager);	// TODO get rid of the fucking Feedbacks and use your own!!!!!!
		marketManager.start();
		
		// obtain the reference to the market
		Market market = marketManager.market();
		
		// instance the simulation manager
		simulationManager = new SimulationManagerImpl(this);
//		simulationManager = new FeedbackSimulationManager(new ConsoleFeedbackCallback(), simulationManager);
		
		// register the market in the simulation
		simulationManager.register(Market.class, market);
		
		// create the connection manager
		connectionManager = new ConnectionManagerImpl(this);
		logger.info("Connection Manager initialized successfully");
		
		shouldExit = false;
	}
	
	public static void main(String[] args) {
		
		// set the basic configuration for the logger, so everything goes to stdout
//		BasicConfigurator.configure();
		
		URL uri = NodeManagement.class.getResource("log4j.config");
		PropertyConfigurator.configure(uri);
		
		// create the Node Management
		try {
			NodeManagement nodeManagement = new NodeManagement(args);
			
			// creaate and run the console
			new NodeConsole().runConsole(nodeManagement);
			
		} catch (UnknownHostException e) {
			logger.fatal("The local node couldn't be started. Aborting execution");
			logger.fatal("Error message:" + e.getMessage(), e);
			return;
		} catch (RemoteException e) {
			logger.fatal("There was an error during the Connection Manager initialization. Aborting execution");
			logger.fatal("Error message:" + e.getMessage(), e);
			return;
		} catch (IOException e) {
			logger.error("'node.conf' file not found. Using default configurations", e);
            logger.error("Error message:" + e.getMessage(), e);
		} catch (Exception e) {
			logger.error("An unexpected exception ocurred");
            logger.error("Error message:" + e.getMessage(), e);
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
	
	public ConnectionManager getConnectionManager(){
		return connectionManager;
	}
	
	public MarketManager getMarketManager() {
		return marketManager;
	}
	
	public SimulationManager getSimulationManager(){
		return simulationManager;
	}

	public boolean shouldExit() {
		return shouldExit;
	}

	public void setShouldExit(boolean state) {
		shouldExit = state;
	}
}
