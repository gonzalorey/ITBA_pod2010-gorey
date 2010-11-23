package ar.edu.itba.pod.legajo47126.node;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.registry.Registry;

import ar.edu.itba.pod.legajo47126.communication.interfaces.RegistryPort;
import ar.edu.itba.pod.simul.communication.ConnectionManager;
import ar.edu.itba.pod.simul.communication.ReferenceName;


public class Node implements ReferenceName, RegistryPort{
	
	// connection data
	private InetAddress inetAddress;
	private int port;
	
	// node Id identifying the node
	private String nodeId;
	
	// RMI Registry of the node
	private Registry registry;
	
	// Connection Manager of the node
	private ConnectionManager connectionManager;

	// list with the agents in the node
	//private CopyOnWriteArrayList<Agent> agents;	//TODO uncomment
	
	// load amount
	private int load;
	
	/**
	 * A node identifying the local machine
	 * 
	 * @param address address of the host to use as entry point
	 * @param port port to listen to incoming connections
	 * @throws UnknownHostException
	 */
	@Deprecated
	public Node(String address, int port) throws UnknownHostException {
		// get the address and port
		this.inetAddress = InetAddress.getByName(address);
		this.port = port;
		
		// build the nodeId
		this.nodeId = this.inetAddress.getHostAddress();
	}
	
	/**
	 * A node identifying the local machine
	 * It uses the local host and default RMI port 1099 to listen to incoming connections
	 * 
	 * @param address address of the host to use as entry point
	 * @throws UnknownHostException
	 */
	public Node(String address) throws UnknownHostException {
		// get the address and port
		this.inetAddress = InetAddress.getByName(address);
		this.port = DEFAULT_PORT_NUMBER;
		
		// build the nodeId
		this.nodeId = this.inetAddress.getHostAddress();
	}
	
	/**
	 * A node identifying the local machine
	 * 
	 * @param port port of the host to use as entry point
	 * @throws UnknownHostException
	 */
	public Node(int port) throws UnknownHostException {
		// get the address and port
		this.inetAddress = InetAddress.getLocalHost();
		this.port = port;
		
		// build the nodeId
		this.nodeId = this.inetAddress.getHostAddress();
	}
	
	/**
	 * A node identifying the local machine
	 * It uses the local host and default RMI port 1099 to listen to incoming connections
	 * 
	 * @throws UnknownHostException
	 */
	public Node() throws UnknownHostException{
		// get the address and port
		this.inetAddress = InetAddress.getLocalHost();
		this.port = DEFAULT_PORT_NUMBER;
		
		// build the nodeId
		this.nodeId = this.inetAddress.getHostAddress();
	}
	
	/**
	 * NodeId representation of the node
	 * 
	 * @return String with the nodeId of the node
	 */
	public String getNodeId(){
		return nodeId;
	}

	/**
	 * Returns the InetAddress of the node
	 * 
	 * @return InetAddress object of the node
	 */
	public InetAddress getInetAddress() {
		return inetAddress;
	}
	
	/**
	 * Gets the host name for this IP address.
	 * 
	 * @return the host name for this IP address, or if the operation is not allowed 
	 * by the security check, the textual representation of the IP address.
	 */
	public String getDnsName() {
		return inetAddress.getHostName();
	}
	
	/**
	 * Returns the IP address string in textual presentation.
	 * 
	 * @return the raw IP address in a string format.
	 */
	public String getHostAddress(){
		return inetAddress.getHostAddress();
	}

	/**
	 * Returns the port 
	 * 
	 * @return the port where the node will listen
	 */
	public int getPort() {
		return port;
	}

	public Registry getRegistry() {
		return registry;
	}

	public void setRegistry(Registry registry) {
		this.registry = registry;
	}

	public ConnectionManager getConnectionManager() {
		return connectionManager;
	}

	public void setConnectionManager(ConnectionManager connectionManager) {
		this.connectionManager = connectionManager;
	}

	public int getLoad() {
		return load;
	}

	public void setLoad(int load) {
		this.load = load;
	}

	@Deprecated
	public static Node parse(String nodeId) throws NumberFormatException, UnknownHostException{
		// split the node to get the address and the port
		String[] aux = nodeId.split(":");
		
		// create and return the new instance of the node
		return new Node(aux[0], Integer.valueOf(aux[1]));
	}
	
	@Override
	public String toString() {
		return nodeId;
	}
}
