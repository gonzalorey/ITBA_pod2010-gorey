package ar.edu.itba.pod.legajo47126.node;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.UnknownHostException;
import java.rmi.RemoteException;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import ar.edu.itba.pod.legajo47126.communication.impl.ConnectionManagerImpl;
import ar.edu.itba.pod.legajo47126.communication.paylod.impl.DisconnectPayloadImpl;
import ar.edu.itba.pod.legajo47126.configuration.Configuration;
import ar.edu.itba.pod.simul.communication.Message;
import ar.edu.itba.pod.simul.communication.MessageType;

public class NodeManagement {
	
	// instance of the local machine node
	private static Node localNode;
	
	// instance of the log4j logger
	private static Logger logger = Logger.getLogger(NodeManagement.class);
	
	public static void main(String[] args) {
		
		// set the basic configuration for the logger, so everything goes to stdout
		BasicConfigurator.configure();
		
		// configuration class to get the properties from the config file
		Configuration conf;
		try {
			conf = new Configuration("node.conf");
			logger.info("Obtained from conf: '" + conf.getProperty("pepe") + "'");
		} catch (IOException e) {
			logger.error("'node.conf' file not found. Using default configurations");
			e.printStackTrace();
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
			logger.debug("[Message: " + e.getMessage() + "] - " + e.getStackTrace()[0] + ";" /*+ e.getStackTrace()[1]*/);
			
			//TODO see how to print several lines of the stack trace
			
			return;
		}
		
		// create the connection manager
		try {
			ConnectionManagerImpl.getInstance();
			logger.info("Connection Manager initialized successfully");
		} catch (RemoteException e) {
			logger.fatal("There was an error during the Connection Manager initialization. Aborting execution");
			e.printStackTrace();
			
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
	
	// console commands
	private enum Commands{CONNECT_GROUP, CREATE_GROUP, SEND, EXIT, WRONG_COMMAND}
	
	
	public static void console(){
	
		// reader from the standard input stream
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		
		while(true){
			
			System.out.print(">");
			
			// read the line
			String line;
			try {
				line = br.readLine();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			}
			
			// get the inserted command
			Commands command;
			if(line.startsWith("connect "))
				command = Commands.CONNECT_GROUP;
			else if(line.startsWith("create "))
				command = Commands.CREATE_GROUP;
			else if(line.startsWith("send "))
				command = Commands.SEND;
			else if(line.startsWith("exit"))
				return;
			else
				command = Commands.WRONG_COMMAND;
			
			switch (command) {
			case CONNECT_GROUP:
				try {
					String nodeId = line.split(" ")[1];
					logger.info("Connecting to " + nodeId);
					ConnectionManagerImpl.getInstance().getClusterAdmimnistration().connectToGroup(nodeId);
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
				
			case CREATE_GROUP:
				try {
					logger.info("Creating group...");
					ConnectionManagerImpl.getInstance().getClusterAdmimnistration().createGroup();
					String groupId = ConnectionManagerImpl.getInstance().getClusterAdmimnistration().getGroupId();
					logger.info("Group " + groupId + " created successfully");
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
	
			case SEND:
				try {
					String nodeId = line.split(" ")[1];
					Message msg = new Message(localNode.getNodeId(), 1, MessageType.DISCONNECT, new DisconnectPayloadImpl("22")); 
					logger.info("Sending message [" + msg + "] to node [" + nodeId + "]");
					if(ConnectionManagerImpl.getInstance().getGroupCommunication().send(msg, nodeId))
						logger.info("Message sent successfully");
					else
						logger.error("There was an error during the message sending");
				} catch (Exception e) {
					// TODO: handle exception
				}
				break;
				
			case EXIT:
				logger.info("EXIT: " + line);
				return;
				
			case WRONG_COMMAND:
			default:
				logger.info("Wrong command, try again");
			}
		}
	}
}
