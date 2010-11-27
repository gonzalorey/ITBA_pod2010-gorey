package ar.edu.itba.pod.legajo47126.node;

import java.io.IOException;
import java.net.UnknownHostException;
import java.rmi.RemoteException;

import org.apache.log4j.Logger;

import ar.edu.itba.pod.legajo47126.communication.ConnectionManagerImpl;
import ar.edu.itba.pod.legajo47126.configuration.ConfigFile;
import ar.edu.itba.pod.legajo47126.market.MarketManagerImpl;
import ar.edu.itba.pod.legajo47126.simulation.SimulationManagerImpl;
import ar.edu.itba.pod.simul.communication.ConnectionManager;
import ar.edu.itba.pod.simul.market.MarketManager;
import ar.edu.itba.pod.simul.simulation.SimulationManager;

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
		
		// configuration class to get the properties from the config file		
//		URL uri = NodeManagement.class.getResource("node.conf");
//		configFile = new ConfigFile(uri.getPath());
		
		marketManager = new MarketManagerImpl(this);

		// instance the simulation manager
		simulationManager = new SimulationManagerImpl(this);

		// create the connection manager
		connectionManager = new ConnectionManagerImpl(this);
		logger.debug("Connection Manager initialized successfully");
		
		shouldExit = false;
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
