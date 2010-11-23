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
import ar.edu.itba.pod.legajo47126.communication.message.MessageFactory;
import ar.edu.itba.pod.legajo47126.simul.coordinator.DisconnectionCoordinator;
import ar.edu.itba.pod.legajo47126.simul.coordinator.NewAgentCoordinator;
import ar.edu.itba.pod.legajo47126.simul.coordinator.NewNodeCoordinator;
import ar.edu.itba.pod.legajo47126.simulation.AgentFactory;
import ar.edu.itba.pod.simul.communication.Message;
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
	private Option connect;
	private Option creategroup;
	private Option createagents;
	private Option createproducer;
	private Option createconsumer;
	private Option getload;
	private Option requestresource;
	private Option startsimulation;
	private Option getknownnodes;	// TODO should dissapear
	private Option getgroupnodes;	// TODO should dissapear
	private Option send;			// TODO should dissapear
	private Option disconnect;		// TODO should dissapear
	private Option help;
	private Option exit;
	
	private NodeManagement nodeManagement;
	
	public NodeConsole(NodeManagement nodeManagement) {
		this.nodeManagement = nodeManagement;
	}
	
	private void startOptions(){
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
	
	public void runConsole(){
	
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
				} else if(cmd.hasOption(createproducer.getOpt())){
					logger.info("Creating a producer agent...");

					int numberOfAgents = 1;
					if(cmd.getOptionValue(createproducer.getOpt()) != null) {
						numberOfAgents = Integer.valueOf(cmd.getOptionValue(createproducer.getOpt()));
					}
					
					if(numberOfAgents == 1){
						Agent agent = AgentFactory.createProducerAgent();
						Thread thread = new Thread(new NewAgentCoordinator(nodeManagement,agent));
						thread.start();
					} else {
						try{
							for(int i = 0; i < numberOfAgents; i++){
								Agent agent = AgentFactory.createProducerAgent();
								nodeManagement.getConnectionManager().getSimulationCommunication().startAgent(agent.getAgentDescriptor());
								logger.info("Producer agent " + agent + " successfully added to the node");
							}
							
							Thread thread = new Thread(new NewNodeCoordinator(nodeManagement));
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
						Thread thread = new Thread(new NewAgentCoordinator(nodeManagement, agent));
						thread.start();
					} else {
						try{
							for(int i = 0; i < numberOfAgents; i++){
								Agent agent = AgentFactory.createConsumerAgent();
								nodeManagement.getConnectionManager().getSimulationCommunication().startAgent(agent.getAgentDescriptor());
								logger.info("Consumer agent " + agent + " successfully added to the node");
							}
							
							Thread thread = new Thread(new NewNodeCoordinator(nodeManagement));
							thread.start();
						} catch (RemoteException e) {
							logger.error("There was an error during the creation of the agent");
							logger.error("Error message:" + e.getMessage());
						}
					}
				} else if(cmd.hasOption(getload.getOpt())){
					logger.info("Getting the node agents load...");
					int load = nodeManagement.getSimulationManager().getAgentsLoad();
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
					
					if(nodeManagement.getConnectionManager().getKnownNodes().size() == 0)
						logger.info("There are no known nodes");
					
					for(String nodeId : nodeManagement.getConnectionManager().getKnownNodes().keySet()){
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
					if(nodeManagement.getSimulationManager().isStarted())
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
}
