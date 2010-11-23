package ar.edu.itba.pod.legajo47126.node;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.util.Collection;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import ar.edu.itba.pod.legajo47126.communication.impl.ClusterAdministrationImpl;
import ar.edu.itba.pod.legajo47126.communication.impl.ConnectionManagerImpl;
import ar.edu.itba.pod.legajo47126.communication.impl.message.MessageFactory;
import ar.edu.itba.pod.legajo47126.configuration.ConfigFile;
import ar.edu.itba.pod.legajo47126.simul.AgentFactory;
import ar.edu.itba.pod.legajo47126.simul.MarketManagerImpl;
import ar.edu.itba.pod.legajo47126.simul.SimulationManagerImpl;
import ar.edu.itba.pod.legajo47126.simul.coordinator.DisconnectionCoordinator;
import ar.edu.itba.pod.legajo47126.simul.coordinator.NewAgentCoordinator;
import ar.edu.itba.pod.legajo47126.simul.coordinator.NewNodeCoordinator;
import ar.edu.itba.pod.simul.communication.Message;
import ar.edu.itba.pod.simul.market.Market;
import ar.edu.itba.pod.simul.market.MarketManager;
import ar.edu.itba.pod.simul.market.Resource;
import ar.edu.itba.pod.simul.simulation.Agent;
import ar.edu.itba.pod.simul.simulation.SimulationManager;
import ar.edu.itba.pod.simul.ui.ConsoleFeedbackCallback;
import ar.edu.itba.pod.simul.ui.FeedbackCallback;
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
	
	public static void main(String[] args) {
		// set the basic configuration for the logger, so everything goes to stdout
		BasicConfigurator.configure();	//TODO set a propper configuration file for the logger
		
		// configuration class to get the properties from the config file
		try {
			String configFileName = "node.conf";		// TODO maybe it should be get by a parameter
			configFile = new ConfigFile(configFileName);
			logger.info("Obtained from conf: '" + configFile.getProperty("pepe", "pepe_default") + "'");
		} catch (IOException e) {
			logger.error("'node.conf' file not found. Using default configurations");
			logger.error("Error message:" + e.getMessage());
			return;
		}
		
		// create the local node
		try {
			if(args.length == 2)
				localNode = new Node(args[0], Integer.valueOf(args[1]));
			else if(args.length == 1)
				localNode = new Node(args[0]);
			else
				localNode = new Node();
			logger.info("Node '" + localNode + "' started successfully");
		} catch (UnknownHostException e) {
			logger.fatal("The local node couldn't be started. Aborting execution");
			logger.fatal("Error message:" + e.getMessage());
			
			//TODO see how to print several lines of the stack trace
			
			return;
		}

		FeedbackCallback callback = new ConsoleFeedbackCallback();
		
		logger.info("Starting the Market Manager...");
		marketManager = new MarketManagerImpl();
		marketManager = new FeedbackMarketManager(callback, marketManager);
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
		try {
			ConnectionManagerImpl.getInstance();
			logger.info("Connection Manager initialized successfully");
		} catch (RemoteException e) {
			logger.fatal("There was an error during the Connection Manager initialization. Aborting execution");
			logger.fatal("Error message:" + e.getMessage());
			
			return;
		}
				
		console();

		logger.info("Bye!");
		
		return;
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

	// command line parser
	private static CommandLineParser cmdParser;
	private static Options options;
	private static HelpFormatter helpFormatter;
	
	// console command options
	private static Option connect;
	private static Option creategroup;
	private static Option createagents;
	private static Option createproducer;
	private static Option createconsumer;
	private static Option getload;
	private static Option requestresource;
	private static Option startsimulation;
	private static Option getknownnodes;	// TODO should dissapear
	private static Option getgroupnodes;	// TODO should dissapear
	private static Option send;				// TODO should dissapear
	private static Option disconnect;		// TODO should dissapear
	private static Option help;
	private static Option exit;
	
	private static void startOptions(){
		cmdParser = new GnuParser();
		options = new Options();
		helpFormatter = new HelpFormatter();
		
		connect = new Option("connect", "Connect to a node");
		connect.setArgs(1);
		connect.setArgName("nodeId");
		
		disconnect = new Option("disconnect", "Disconnect from the group");
		
		creategroup = new Option("creategroup", "Creates a group");
		
		createagents = new Option("createagents", "Creates a group of agents to test the simulation");
		
		createproducer = new Option("createproducer", "Creates an producer agent");
		createproducer.setArgs(1);
		createproducer.setArgName("numberOfAgents");
		createproducer.setOptionalArg(true);
		
		createconsumer = new Option("createconsumer", "Creates an consumer agent");
		createconsumer.setArgs(1);
		createconsumer.setArgName("numberOfAgents");
		createconsumer.setOptionalArg(true);
		
		getload = new Option("getload", "Get the node agents load");
		
		requestresource = new Option("requestresource", "Requests a resource");
		requestresource.setArgs(1);
		requestresource.setArgName("nodeId");
		
		startsimulation = new Option("startsimulation", "Starts the simulation");
		
		getknownnodes = new Option("getknownnodes", "Lists the known nodes");
		
		getgroupnodes = new Option("getgroupnodes", "Lists the group nodes");
		
		send = new Option("send", "Sends a peer-2-peer message");
		send.setArgs(1);
		send.setArgName("nodeId");
		
		help = new Option("help", "Prints the help commands");
		
		exit = new Option("exit", "Exit the application");
		
		options.addOption(connect);
		options.addOption(disconnect);
		options.addOption(creategroup);
		options.addOption(createagents);
		options.addOption(createproducer);
		options.addOption(createconsumer);
		options.addOption(getload);
		options.addOption(requestresource);
		options.addOption(startsimulation);
		options.addOption(getknownnodes);
		options.addOption(getgroupnodes);
		options.addOption(send);
		options.addOption(help);
		options.addOption(exit);

	}
	
	public static void console(){
	
		// start the command line options
		startOptions();
		
		helpFormatter.printHelp("-command_name [args]", options);
	
		// reader from the standard input stream
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		
		while(true){
			try {
				System.out.print(">");
				
				String line = br.readLine();
				String args[] = line.split(" ");

				// command line reader
				CommandLine cmd = cmdParser.parse(options, args);
				
				if(cmd.hasOption(connect.getOpt())){
					String nodeId = cmd.getOptionValue(connect.getOpt());
					try{
						logger.info("Connecting to [" + nodeId + "]...");
						ConnectionManagerImpl.getInstance().getClusterAdmimnistration().connectToGroup(nodeId);
					} catch (RemoteException e) {
						logger.error("There was an error during the connection to the node " + nodeId);
						logger.error("Error message:" + e.getMessage());
					}
				} else if(cmd.hasOption(disconnect.getOpt())){
					try{
						new DisconnectionCoordinator().run();
						
						// create the DISCONNECT message
						Message message = MessageFactory.DisconnectMessage(NodeManagement.getLocalNode().getNodeId());
						logger.debug("Built message [" + message + "], broadcast it");
						
						// broadcast the messsage
						ConnectionManagerImpl.getInstance().getGroupCommunication().broadcast(message);
						
						// clears the group data
						((ClusterAdministrationImpl)ConnectionManagerImpl.getInstance().getClusterAdmimnistration()).clearGroup();
						
					} catch (RemoteException e) {
						logger.error("There was an error during the disconnection of the node");
						logger.error("Error message:" + e.getMessage());
					}
				} else if(cmd.hasOption(creategroup.getOpt())){
					try{
						logger.info("Creating group...");
						ConnectionManagerImpl.getInstance().getClusterAdmimnistration().createGroup();
						String groupId = ConnectionManagerImpl.getInstance().getClusterAdmimnistration().getGroupId();
						logger.info("Group " + groupId + " created successfully");
					} catch (RemoteException e) {
						logger.error("There was an error during the creation of the node group");
						logger.error("Error message:" + e.getMessage());
					}
				} else if(cmd.hasOption(createagents.getOpt())){
					logger.info("Creating agents to test the simulation...");
					
					try{
						Collection<Agent> simulationAgents = AgentFactory.createSimulationAgents();
						for(Agent agent : simulationAgents){
							ConnectionManagerImpl.getInstance().getSimulationCommunication().startAgent(agent.getAgentDescriptor());
							logger.info("Producer agent " + agent + " successfully added to the node");
						}

						Thread thread = new Thread(new NewNodeCoordinator());
						thread.start();
					} catch (RemoteException e) {
						logger.error("There was an error during the creation of the agents");
						logger.error("Error message:" + e.getMessage());
					}
				} else if(cmd.hasOption(createproducer.getOpt())){
					logger.info("Creating a producer agent...");

					int numberOfAgents = 1;
					if(cmd.getOptionValue(createproducer.getOpt()) != null) {
						numberOfAgents = Integer.valueOf(cmd.getOptionValue(createproducer.getOpt()));
					}
					
					if(numberOfAgents == 1){
						Agent agent = AgentFactory.createProducerAgent();
						Thread thread = new Thread(new NewAgentCoordinator(agent));
						thread.start();
					} else {
						try{
							for(int i = 0; i < numberOfAgents; i++){
								Agent agent = AgentFactory.createProducerAgent();
								ConnectionManagerImpl.getInstance().getSimulationCommunication().startAgent(agent.getAgentDescriptor());
								logger.info("Producer agent " + agent + " successfully added to the node");
							}
							
							Thread thread = new Thread(new NewNodeCoordinator());
							thread.start();
						} catch (RemoteException e) {
							logger.error("There was an error during the creation of the agent");
							logger.error("Error message:" + e.getMessage());
						}
					}
				} else if(cmd.hasOption(createconsumer.getOpt())){
					logger.info("Creating a consumer agent...");

					int numberOfAgents = 1;
					if(cmd.getOptionValue(createconsumer.getOpt()) != null) {
						numberOfAgents = Integer.valueOf(cmd.getOptionValue(createconsumer.getOpt()));
					}
					
					if(numberOfAgents == 1){
						Agent agent = AgentFactory.createConsumerAgent();
						Thread thread = new Thread(new NewAgentCoordinator(agent));
						thread.start();
					} else {
						try{
							for(int i = 0; i < numberOfAgents; i++){
								Agent agent = AgentFactory.createConsumerAgent();
								ConnectionManagerImpl.getInstance().getSimulationCommunication().startAgent(agent.getAgentDescriptor());
								logger.info("Consumer agent " + agent + " successfully added to the node");
							}
							
							Thread thread = new Thread(new NewNodeCoordinator());
							thread.start();
						} catch (RemoteException e) {
							logger.error("There was an error during the creation of the agent");
							logger.error("Error message:" + e.getMessage());
						}
					}
				} else if(cmd.hasOption(getload.getOpt())){
					logger.info("Getting the node agents load...");
					int load = NodeManagement.getSimulationManager().getAgentsLoad();
					logger.info("Node agents load " + load);
				} else if(cmd.hasOption(requestresource.getOpt())){
					// get the node to communicate to
					String remoteNodeId = cmd.getOptionValue(requestresource.getOpt());
					Message message = MessageFactory.ResourceRequestMessage(new Resource("Mineral", "Pig Iron"), 2);
					
					logger.info("Sending message [" + message + "] to node [" + remoteNodeId + "]");
					ConnectionManagerImpl.getInstance().getGroupCommunication().send(message, remoteNodeId);
					
				} else if(cmd.hasOption(startsimulation.getOpt())){
					// start the simulation
					simulationManager.start();
					
				} else if(cmd.hasOption(getknownnodes.getOpt())){
					logger.info("Getting the known nodes list...");
					
					if(ConnectionManagerImpl.getInstance().getKnownNodes().size() == 0)
						logger.info("There are no known nodes");
					
					for(String nodeId : ConnectionManagerImpl.getInstance().getKnownNodes().keySet()){
						logger.info(nodeId);
					}
				} else if(cmd.hasOption(getgroupnodes.getOpt())){
					logger.info("Getting the group nodes list...");
					
					if(((ClusterAdministrationImpl)ConnectionManagerImpl.getInstance().
							getClusterAdmimnistration()).getGroupNodes().size() == 0)
						logger.info("There are no group nodes");
					
					for(String nodeId : ((ClusterAdministrationImpl)ConnectionManagerImpl.getInstance().
							getClusterAdmimnistration()).getGroupNodes()){
						logger.info(nodeId);
					}
				} else if(cmd.hasOption(send.getOpt())){
					String nodeId = cmd.getOptionValue(send.getOpt()); 
					try{
						logger.info("Sending a message to node [" +  nodeId + "]...");
						ConnectionManagerImpl.getInstance().getGroupCommunication().send(MessageFactory.NewMessageRequest(), nodeId);
					} catch (RemoteException e) {
						logger.error("There was an error during the disconnection of the node " + nodeId);
						logger.error("Error message:" + e.getMessage());
					}
				} else if(cmd.hasOption(help.getOpt())){
					logger.info("Printing the help...");
					helpFormatter.printHelp("-command_name [args]", options);
				} else if(cmd.hasOption(exit.getOpt())){
					logger.info("Exiting...");
					NodeManagement.getSimulationManager().shutdown();
					marketManager.shutdown();
					break;
				} else{
					logger.info("Wrong command, type -help for more information");
				}
				
				
			} catch (IOException e) {
				logger.error("There was an error, please try again");
				logger.error("Error message:" + e.getMessage());
			} catch (ParseException e) {
				logger.info("Wrong command, type -help for more information");
			}
		}
		
	}
	
	
}
