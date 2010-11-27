package ar.edu.itba.pod.legajo47126.node;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.RemoteException;
import java.util.Collection;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;

import ar.edu.itba.pod.legajo47126.communication.ClusterAdministrationImpl;
import ar.edu.itba.pod.legajo47126.communication.ConnectionManagerImpl;
import ar.edu.itba.pod.legajo47126.communication.message.MessageFactory;
import ar.edu.itba.pod.legajo47126.simul.coordinator.DisconnectionCoordinator;
import ar.edu.itba.pod.legajo47126.simul.coordinator.NewNodeCoordinator;
import ar.edu.itba.pod.legajo47126.simulation.AgentFactory;
import ar.edu.itba.pod.legajo47126.simulation.SimulationManagerImpl;
import ar.edu.itba.pod.legajo47126.simulation.AgentFactory.ConsumersProducers;
import ar.edu.itba.pod.legajo47126.simulation.AgentFactory.SimpleConsumers;
import ar.edu.itba.pod.legajo47126.simulation.AgentFactory.SimpleProducers;
import ar.edu.itba.pod.simul.ObjectFactoryAlternative;
import ar.edu.itba.pod.simul.communication.Message;
import ar.edu.itba.pod.simul.market.Market;
import ar.edu.itba.pod.simul.market.Resource;
import ar.edu.itba.pod.simul.simulation.Agent;

public class NodeConsole {

	// instance of the log4j logger
	private static Logger logger = Logger.getLogger(NodeConsole.class);
	
	// command line parser
	private CommandLineParser cmdParser;
	private Options options;
	private HelpFormatter helpFormatter;
	
	// console command options
	private Option creategroup;
	private Option disconnect;
	private Option connect;
	
	private Option createagents;
	private Option createproducer;
	private Option createconsumer;
	private Option createconsprod;
	
	private Option startsimulation;
	private Option shutdownsimulation;
	private Option startmarket;
	private Option shutdownmarket;

	private Option help;
	private Option exit;
	
	private Option getload;			// TODO should dissapear
	private Option requestresource;	// TODO should dissapear
	private Option getknownnodes;	// TODO should dissapear
	private Option getgroupnodes;	// TODO should dissapear
	private Option send;			// TODO should dissapear
	
	public NodeConsole() {
		cmdParser = new GnuParser();
		options = new Options();
		helpFormatter = new HelpFormatter();
		
		connect = new Option("connect", "Connect to a node");
		connect.setArgs(1);
		connect.setArgName("nodeId");
		
		disconnect = new Option("disconnect", "Disconnect from the group");
		
		creategroup = new Option("creategroup", "Creates a group");
		
		createagents = new Option("createagents", "Creates a group of agents to test the simulation");
		
		createproducer = new Option("createproducer", "Creates a producer agent:\n\t1-PIG IRON MINE\n\t2-COPPER MINE");
		
		createconsumer = new Option("createconsumer", "Creates a consumer agent:\n\t1-FACTORY");
		
		createconsprod = new Option("createconsprod", "Creates a producer and consumer agent");
		
		getload = new Option("getload", "Get the node agents load");
		
		requestresource = new Option("requestresource", "Requests a resource");
		requestresource.setArgs(1);
		requestresource.setArgName("nodeId");
		
		startsimulation = new Option("startsimulation", "Starts the simulation");
		
		shutdownsimulation = new Option("shutdownsimulation", "Shuts down the simulation");
		
		startmarket = new Option("startmarket", "Starts the market and registers it in the simulation");
		
		shutdownmarket = new Option("shutdownmarket", "Shuts down the market");
		
		getknownnodes = new Option("getknownnodes", "Lists the known nodes");
		
		getgroupnodes = new Option("getgroupnodes", "Lists the group nodes");
		
		send = new Option("send", "Sends a peer-2-peer message");
		send.setArgs(1);
		send.setArgName("nodeId");
		
		help = new Option("help", "Prints the help commands");
		
		exit = new Option("exit", "Exit the application");
	}
	
	private void setDebugOptions(){
		
		// set optional argument
		createproducer.setArgs(1);
		createproducer.setArgName("numberOfAgents");
		createproducer.setOptionalArg(true);

		// set optional argument
		createconsumer.setArgs(1);
		createconsumer.setArgName("numberOfAgents");
		createconsumer.setOptionalArg(true);
		
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
	
	private void setOptions(){
		options.addOption(connect);
		options.addOption(disconnect);
		options.addOption(creategroup);

		options.addOption(createagents);
		createproducer.setArgs(1);
		createproducer.setArgName("Type of producer");
		options.addOption(createproducer);
		options.addOption(createconsumer);
		options.addOption(createconsprod);
		
		startsimulation.setArgs(1);
		startsimulation.setArgName("NumberOfSeconds");
		startsimulation.setOptionalArg(true);
		options.addOption(startsimulation);

		options.addOption(shutdownsimulation);
		
		options.addOption(startmarket);
		options.addOption(shutdownmarket);
		
		options.addOption(help);
		options.addOption(exit);
		
		options.addOption(getload);
		options.addOption(getknownnodes);
		options.addOption(getgroupnodes);
	}
	
	public void runConsole(NodeManagement nodeManagement){
	
		// set the command line options
		setDebugOptions();
		
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
						nodeManagement.getConnectionManager().getClusterAdmimnistration().connectToGroup(nodeId);
					} catch (RemoteException e) {
						logger.error("There was an error during the connection to the node " + nodeId);
						logger.error("Error message:" + e.getMessage());
					}
				} else if(cmd.hasOption(disconnect.getOpt())){
					try{
						new DisconnectionCoordinator(nodeManagement).run();
						
						// create the DISCONNECT message
						Message message = MessageFactory.DisconnectMessage(nodeManagement.getLocalNode().getNodeId(),
								nodeManagement.getLocalNode().getNodeId());
						logger.debug("Built message [" + message + "], broadcast it");
						
						// broadcast the messsage
						nodeManagement.getConnectionManager().getGroupCommunication().broadcast(message);
						
						// clears the group data
						((ClusterAdministrationImpl)nodeManagement.getConnectionManager().getClusterAdmimnistration()).clearGroup();
						
					} catch (RemoteException e) {
						logger.error("There was an error during the disconnection of the node");
						logger.error("Error message:" + e.getMessage());
					}
				} else if(cmd.hasOption(creategroup.getOpt())){
					try{
						logger.info("Creating group...");
						nodeManagement.getConnectionManager().getClusterAdmimnistration().createGroup();
						String groupId = nodeManagement.getConnectionManager().getClusterAdmimnistration().getGroupId();
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
							nodeManagement.getConnectionManager().getSimulationCommunication().startAgent(agent.getAgentDescriptor());
							logger.info("Producer agent " + agent + " successfully added to the node");
						}

						Thread thread = new Thread(new NewNodeCoordinator(nodeManagement));
						thread.start();
					} catch (RemoteException e) {
						logger.error("There was an error during the creation of the agents");
						logger.error("Error message:" + e.getMessage());
					}
				} else if(cmd.hasOption(getload.getOpt())){
					logger.info("Getting the node agents load...");
					int load = ((SimulationManagerImpl) nodeManagement.getSimulationManager()).getAgentsLoad();
					logger.info("Node agents load " + load);
				} else if(cmd.hasOption(requestresource.getOpt())){
					// get the node to communicate to
					String remoteNodeId = cmd.getOptionValue(requestresource.getOpt());
					Message message = MessageFactory.ResourceRequestMessage(nodeManagement.getLocalNode().getNodeId(),
							new Resource("Mineral", "Pig Iron"), 2);
					
					logger.info("Sending message [" + message + "] to node [" + remoteNodeId + "]");
					nodeManagement.getConnectionManager().getGroupCommunication().send(message, remoteNodeId);
					
				} else if(cmd.hasOption(startsimulation.getOpt())){
					// start the simulation
					nodeManagement.getSimulationManager().start();
					
				} else if(cmd.hasOption(getknownnodes.getOpt())){
					logger.info("Getting the known nodes list...");
					
					if(((ConnectionManagerImpl) nodeManagement.getConnectionManager()).getKnownNodes().size() == 0)
						logger.info("There are no known nodes");
					
					for(String nodeId : ((ConnectionManagerImpl) nodeManagement.getConnectionManager()).getKnownNodes().keySet()){
						logger.info(nodeId);
					}
				} else if(cmd.hasOption(getgroupnodes.getOpt())){
					logger.info("Getting the group nodes list...");
					
					if(((ClusterAdministrationImpl)nodeManagement.getConnectionManager().
							getClusterAdmimnistration()).getGroupNodes().size() == 0)
						logger.info("There are no group nodes");
					
					for(String nodeId : ((ClusterAdministrationImpl)nodeManagement.getConnectionManager().
							getClusterAdmimnistration()).getGroupNodes()){
						logger.info(nodeId);
					}
				} else if(cmd.hasOption(send.getOpt())){
					String nodeId = cmd.getOptionValue(send.getOpt()); 
					try{
						logger.info("Sending a message to node [" +  nodeId + "]...");
						nodeManagement.getConnectionManager().getGroupCommunication().send(MessageFactory.
								NewMessageRequest(nodeManagement.getLocalNode().getNodeId()), nodeId);
					} catch (RemoteException e) {
						logger.error("There was an error during the disconnection of the node " + nodeId);
						logger.error("Error message:" + e.getMessage());
					}
				} else if(cmd.hasOption(help.getOpt())){
					logger.info("Printing the help...");
					helpFormatter.printHelp("-command_name [args]", options);
				} else if(cmd.hasOption(exit.getOpt())){
					logger.info("Exiting...");
					if(((SimulationManagerImpl) nodeManagement.getSimulationManager()).isStarted())
						nodeManagement.getSimulationManager().shutdown();
					nodeManagement.getMarketManager().shutdown();
					nodeManagement.setShouldExit(true);
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
	
	public void runConsole(ObjectFactoryAlternative ofa){
		// set the command line options
		setOptions();
		
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
					logger.info("Connecting to [" + nodeId + "]...");
					try{
						ofa.connectToGroup(nodeId);
						System.out.println("Connected to group [" + ofa.getConnectionManager().getClusterAdmimnistration().getGroupId() + "]");
					} catch (Exception e) {
						logger.error("There was an error during the connection to the node " + nodeId, e);
					}
				} else if(cmd.hasOption(disconnect.getOpt())){
					logger.info("Disconnecting from group...");
					try{
						ofa.disconnect();
					} catch (Exception e) {
						logger.error("There was an error during the disconnection of the node", e);
					}
				} else if(cmd.hasOption(creategroup.getOpt())){
					logger.info("Creating group...");
					try{
						ofa.createGroup();
						System.out.println("Group [" + ofa.getConnectionManager().getClusterAdmimnistration().getGroupId() + "] created");
					} catch (Exception e) {
						logger.error("There was an error during the creation of the node group", e);
					}
				} else if(cmd.hasOption(startsimulation.getOpt())){
					if(cmd.getOptionValue(startsimulation.getOpt()) != null) {
						int seconds = Integer.valueOf(cmd.getOptionValue(startsimulation.getOpt()));
						try{
							logger.info("Starting the simulation for " + seconds + "seconds ...");
							ofa.getSimulationManager().start();
						} catch (Exception e) {
							logger.error("There was an error while trying start the simulation", e);
						}
						
						try {
							Thread.sleep(1000 * seconds);
						} catch (InterruptedException e) {
							logger.error("The sleep time was interrupted", e);
						}
						
						try{
							logger.info("Shutting down the simulation...");
							ofa.getSimulationManager().shutdown();
						} catch (Exception e) {
							logger.error("There was an error while trying start the simulation", e);
						}
					} else{
						try {
							logger.info("Starting the simulation");
							ofa.getSimulationManager().start();
						} catch (Exception e) {
							logger.error("There was an error while trying start the simulation", e);
						}
					}
				} else if(cmd.hasOption(shutdownsimulation.getOpt())){
					logger.info("Shutting down the simulation...");
					try {
						ofa.getSimulationManager().shutdown();
					} catch (Exception e) {
						logger.error("There was an error while trying to shutdown the simulation", e);
					}
				}else if(cmd.hasOption(startmarket.getOpt())){
					logger.info("Starting the market...");
					try {
						ofa.getMarketManager().start();
						ofa.getSimulationManager().register(Market.class, ofa.getMarketManager().market());
					} catch (Exception e) {
						logger.error("There was an error while trying start the market", e);
					}
				} else if(cmd.hasOption(shutdownmarket.getOpt())){
					logger.info("Shutting the simulation...");
					try {
						ofa.getMarketManager().shutdown();
					} catch (Exception e) {
						logger.error("There was an error while trying to shutdown the simulation", e);
					}
				} else if(cmd.hasOption(createagents.getOpt())){
					logger.info("Creating group agents...");
					
					Agent mine1 = AgentFactory.createSimpleProducer(SimpleProducers.PIG_IRON_MINE);
					Agent mine2 = AgentFactory.createSimpleProducer(SimpleProducers.COPPER_MINE);
					Agent factory = AgentFactory.createSimpleConsumer(SimpleConsumers.FACTORY);
					Agent refinery = AgentFactory.createConsumerProducer(ConsumersProducers.STEEL_REFINERY);
					
					ofa.getSimulationManager().addAgent(mine1);
					ofa.getSimulationManager().addAgent(mine2);
					ofa.getSimulationManager().addAgent(factory);
					ofa.getSimulationManager().addAgent(refinery);
					
					logger.info("Agents added to the simulation");
					
				} else if(cmd.hasOption(createproducer.getOpt())){
					int option;
					try{
						option = Integer.parseInt(cmd.getOptionValue(createproducer.getOpt()));
					} catch (NumberFormatException e) {
						logger.error("The argument is not a number", e);
						break;
					}
					
					Agent agent;
					if(option == 1){
						logger.info("Creating a PIG IRON MINE producer agent...");
						agent = AgentFactory.createSimpleProducer(SimpleProducers.PIG_IRON_MINE);
					} else if(option == 2){
						logger.info("Creating a COPPER MINE producer agent...");
						agent = AgentFactory.createSimpleProducer(SimpleProducers.COPPER_MINE);
					} else {
						logger.error("Argument doesn't match a propper agent");
						break;
					}
					
					try{
						ofa.getSimulationManager().addAgent(agent);
					} catch (Exception e) {
						logger.error("There was an error during the creation of the agent", e);
					}
				} else if(cmd.hasOption(createconsumer.getOpt())){
					logger.info("Creating a FACTORY consumer agent...");
					try{
						Agent agent = AgentFactory.createSimpleConsumer(SimpleConsumers.FACTORY);
						ofa.getSimulationManager().addAgent(agent);
					} catch (Exception e) {
						logger.error("There was an error during the creation of the agent", e);
					}
				} else if(cmd.hasOption(createconsprod.getOpt())){
					logger.info("Creating a STEEL REFINERY consumer producer agent...");
					try{
						Agent agent = AgentFactory.createConsumerProducer(ConsumersProducers.STEEL_REFINERY);
						ofa.getSimulationManager().addAgent(agent);
					} catch (Exception e) {
						logger.error("There was an error during the creation of the agent", e);
					}
				} else if(cmd.hasOption(getload.getOpt())){
					logger.info("Getting the node agents load... [WARNING: CAST TO LOCAL CLASS NEEDED]");
					try{
						int load = ((SimulationManagerImpl) ofa.getSimulationManager()).getAgentsLoad();
						logger.info("Node agents load " + load);
					System.out.println("Node agents load " + load);
					} catch (Exception e) {
						logger.error("There was an error while trying to obtain the node agent load", e);
					}
				} else if(cmd.hasOption(getknownnodes.getOpt())){
					logger.info("Getting the known nodes list... [WARNING: CAST TO LOCAL CLASS NEEDED]");
					try{
						if(((ConnectionManagerImpl) ofa.getConnectionManager()).getKnownNodes().size() == 0){
							logger.info("There are no known nodes");
							System.out.println("There are no group nodes");
						}

						for(String nodeId : ((ConnectionManagerImpl) ofa.getConnectionManager()).getKnownNodes().keySet()){
							logger.info(nodeId);
							System.out.println(nodeId);
						}
					}catch(Exception e){
						logger.error("There was an error while trying to get the list of known nodes", e);
					}
				} else if(cmd.hasOption(getgroupnodes.getOpt())){
					logger.info("Getting the group nodes list...");
					try{
						if(((ClusterAdministrationImpl)ofa.getConnectionManager().
								getClusterAdmimnistration()).getGroupNodes().size() == 0){
							logger.info("There are no group nodes");
							System.out.println("There are no group nodes");
						}

						for(String nodeId : ((ClusterAdministrationImpl)ofa.getConnectionManager().
								getClusterAdmimnistration()).getGroupNodes()){
							logger.info(nodeId);
							System.out.println(nodeId);
						}
					}catch(Exception e){
						logger.error("There was an error while trying to get the list of group nodes", e);
					}
				} else if(cmd.hasOption(help.getOpt())){
					logger.info("Printing the help...");
					helpFormatter.printHelp("-command_name [args]", options);
				} else if(cmd.hasOption(exit.getOpt())){
					logger.info("Exiting...");
					if(((SimulationManagerImpl) ofa.getSimulationManager()).isStarted())
						ofa.getSimulationManager().shutdown();
					ofa.getMarketManager().shutdown();
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
