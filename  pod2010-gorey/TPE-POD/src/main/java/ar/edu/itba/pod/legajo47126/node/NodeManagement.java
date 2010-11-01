package ar.edu.itba.pod.legajo47126.node;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.UnknownHostException;
import java.rmi.RemoteException;

import ar.edu.itba.pod.legajo47126.communication.impl.ConnectionManagerImpl;
import ar.edu.itba.pod.simul.communication.ConnectionManager;


public class NodeManagement {
	
	// instance of the local machine node
	private static Node localNode;

	public static void main(String[] args) {
		System.out.println("Starting the node...");
		
		// create the local node
		try {
			localNode = new Node();
		} catch (UnknownHostException e) {
			System.out.println("The local Node couldn't be started. Aborting execution");
			e.printStackTrace();
			return;
		}
		
		System.out.println("Node '" + localNode + "' started successfully");
		
		// create the connection manager
		ConnectionManager connectionManager = ConnectionManagerImpl.getInstance();
		
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
	private enum Commands{HOST, PORT, EXIT, WRONG_COMMAND}
	
	
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
			if(line.startsWith("host "))
				command = Commands.HOST;
			else if(line.startsWith("port "))
				command = Commands.PORT;
			else if(line.startsWith("exit"))
				return;
			else
				command = Commands.WRONG_COMMAND;
			
			switch (command) {
			case HOST:
				System.out.println("HOST: " + line);
				break;
	
			case PORT:
				System.out.println("PORT: " + line);
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
