package ar.edu.itba.pod.legajo47126.node;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.UnknownHostException;
import java.rmi.RemoteException;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import ar.edu.itba.pod.legajo47126.communication.impl.ConnectionManagerImpl;
import ar.edu.itba.pod.simul.communication.ConnectionManager;

public class NodeManagement {
	
	// instance of the local machine node
	private static Node localNode;
	
	// instance of the log4j logger
	private static Logger logger = Logger.getLogger(NodeManagement.class);
	
	public static void main(String[] args) {
		
		// set the basic configuration for the logger, so everything goes to stdout
		BasicConfigurator.configure();
		
		logger.info("Starting the node...");
		
		// create the local node
		try {
			localNode = new Node();
		} catch (UnknownHostException e) {
			logger.fatal("The local node couldn't be started. Aborting execution. [Message: " + e.getMessage() + "]");
			logger.debug(e.getStackTrace()[0] + ";" /*+ e.getStackTrace()[1]*/);
			//TODO see how to print several lines of the stack trace
		}
		
		System.out.println("Node '" + localNode + "' started successfully");
		
		// create the connection manager
		ConnectionManager connectionManager;
		try {
			connectionManager = ConnectionManagerImpl.getInstance();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		
		// something as not to get a warning...
		try {
			connectionManager.getClusterPort();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		console();

		System.out.println("Bye!");
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
	private enum Commands{CONNECT_GROUP, CREATE_GROUP, EXIT, WRONG_COMMAND}
	
	
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
			else if(line.startsWith("exit"))
				return;
			else
				command = Commands.WRONG_COMMAND;
			
			switch (command) {
			case CONNECT_GROUP:
				try {
					String nodeId = line.split(" ")[1];
					System.out.println("Connecting to " + nodeId);
					ConnectionManagerImpl.getInstance().getClusterAdmimnistration().connectToGroup(nodeId);
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
				
			case CREATE_GROUP:
				try {
					System.out.println("Creating group...");
					ConnectionManagerImpl.getInstance().getClusterAdmimnistration().createGroup();
					String groupId = ConnectionManagerImpl.getInstance().getClusterAdmimnistration().getGroupId();
					System.out.println("Group " + groupId + " created successfully");
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
	
			case EXIT:
				System.out.println("EXIT: " + line);
				return;
				
			case WRONG_COMMAND:
			default:
				System.out.println("Wrong command, try again");
			}
		}
	}
}
