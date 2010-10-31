package node;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.AccessException;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import ar.edu.itba.pod.simul.communication.ConnectionManager;
import ar.edu.itba.pod.simul.communication.ReferenceName;

import communication.impl.ConnectionManagerImpl;
import communication.interfaces.RegistryPort;

public class Node implements ReferenceName, RegistryPort{
	
	// connection data
	private String dnsName;
	private InetAddress address;
	private int port; 
	
	public static void main(String[] args) {
		System.out.println("Starting the node...");
		
		// create the local node
		Node localNode; 
		try {
			localNode = new Node();
		} catch (UnknownHostException e) {
			System.out.println("The local Node couldn't be started. Aborting execution");
			e.printStackTrace();
			return;
		}
		
		System.out.println("Node '" + localNode.getDnsName() + "' started successfully");
		
		// start the RMI Registry
		Registry registry = startRMIRegistry();
		
		// create the connection manager
		ConnectionManager connectionManager = new ConnectionManagerImpl();
		
		System.out.println("Connection Manager created successfully. Publishing it...");
		
		// publish the connection
		try {
			registry.bind(CONNECTION_MANAGER_NAME, connectionManager);
		} catch (AccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (AlreadyBoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("Connection Manager published successfully");
		
		System.out.println("Bye!");
		return;
	}
	
	public static Registry startRMIRegistry(){
		// RMI Registry
		Registry registry = null;
		
		try {
			// create the RMI Registry
			registry = LocateRegistry.createRegistry(DEFAULT_PORT_NUMBER);
			System.out.println("RMI Registry raised successfully");
		} catch (RemoteException e) {
			// aparently the rmiregistry was already instantiated
			e.printStackTrace();
			
			try {
				// try to obtain the already created RMI Registry
				registry = LocateRegistry.getRegistry();
				System.out.println("RMI Registry joined successfully");
			} catch (RemoteException e1) {
				e1.printStackTrace();
			}
		}
		
		return registry;
	}
	
	public Node(String address, int port) throws UnknownHostException {
		// get the address and port
		this.address = InetAddress.getByName(address);
		this.port = port;

		// set the domain name or the string representation of the address in case it's not available
		this.dnsName = this.address.getHostName();
	}
	
	public Node() throws UnknownHostException{
		// get the address and port
		this.address = InetAddress.getLocalHost();
		this.port = DEFAULT_PORT_NUMBER;
		
		// set the domain name or the string representation of the address in case it's not available
		this.dnsName = this.address.getHostName();
	}

	public String getDnsName() {
		return dnsName;
	}

	public void setDnsName(String dnsName) {
		this.dnsName = dnsName;
	}

	public InetAddress getAddress() {
		return address;
	}

	public void setAddress(InetAddress address) {
		this.address = address;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}
	
	
}
