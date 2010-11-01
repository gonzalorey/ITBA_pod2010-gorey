package node;

import java.net.UnknownHostException;
import java.rmi.RemoteException;

import ar.edu.itba.pod.simul.communication.ConnectionManager;

import communication.impl.ConnectionManagerImpl;

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
		
		System.out.println("Node '" + localNode.getDnsName() + "' started successfully");
		
		// create the connection manager
		ConnectionManager connectionManager = ConnectionManagerImpl.getInstance();
		
		// something as not to get a warning...
		try {
			connectionManager.getClusterPort();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("Connection Manager created successfully. Publishing it...");
		
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
}
