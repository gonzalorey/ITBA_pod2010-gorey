package ar.edu.itba.pod.legajo47126.communication;

import java.net.UnknownHostException;
import java.rmi.AccessException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import ar.edu.itba.pod.legajo47126.communication.interfaces.RegistryPort;
import ar.edu.itba.pod.legajo47126.communication.message.MessageManager;
import ar.edu.itba.pod.legajo47126.communication.transaction.TransactionManager;
import ar.edu.itba.pod.legajo47126.exceptions.NoConnectionAvailableException;
import ar.edu.itba.pod.legajo47126.exceptions.WrongNodeIDException;
import ar.edu.itba.pod.legajo47126.node.Node;
import ar.edu.itba.pod.legajo47126.node.NodeManagement;
import ar.edu.itba.pod.legajo47126.simulation.SimulationCommunicationImpl;
import ar.edu.itba.pod.simul.communication.ClusterAdministration;
import ar.edu.itba.pod.simul.communication.ClusterCommunication;
import ar.edu.itba.pod.simul.communication.ConnectionManager;
import ar.edu.itba.pod.simul.communication.ReferenceName;
import ar.edu.itba.pod.simul.communication.SimulationCommunication;
import ar.edu.itba.pod.simul.communication.ThreePhaseCommit;
import ar.edu.itba.pod.simul.communication.Transactionable;

public class ConnectionManagerImpl implements ConnectionManager, ReferenceName, RegistryPort {
	
	// instance of the log4j logger
	private static Logger logger = Logger.getLogger(ConnectionManagerImpl.class);
	
	// RMI Registry
	private Registry registry;

	// list of known nodes
	private ConcurrentHashMap<String, Node> knownNodes;
	
	// ClusterAdministration instance to handle the group connections
	private ClusterAdministration clusterAdministration;
	
	// MessageManager instance, that implements ClusterCommunication and MessageListener
	private MessageManager messageManager;
	
	private SimulationCommunication simulationCommunication;
	
	private TransactionManager transactionManager;
	
	private NodeManagement nodeManagement;
	
	public ConnectionManagerImpl(NodeManagement nodeManagement) throws RemoteException{
		UnicastRemoteObject.exportObject(this, 0);
		logger.debug("Instantiating the Connection Manager...");
		
		this.nodeManagement = nodeManagement;
		
		// instance the ClusterAdministration
		clusterAdministration = new ClusterAdministrationImpl(nodeManagement);
		logger.debug("Connection Administration initialized");
		
		simulationCommunication = new SimulationCommunicationImpl(nodeManagement);
		
		transactionManager = new TransactionManager(nodeManagement);
			
		// instance the kown nodes map
		knownNodes = new ConcurrentHashMap<String, Node>();
		
		// start the RMI Registry
		registry = startRMIRegistry();
				
		// publish the Connection Manager
		try {
			registry.bind(CONNECTION_MANAGER_NAME, this);
			logger.info("Connection Manager binded successfully");
		} catch (AccessException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (RemoteException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (AlreadyBoundException e) {
			logger.error("The object is already bounded");
			e.printStackTrace();
		}
		
		// instance the MessageManager
		messageManager = new MessageManager(nodeManagement);
		messageManager.startMessageProcessing();
		logger.debug("Message Manager initialized");
		
	}
	
	@Override
	public int getClusterPort() throws RemoteException {
		// return always the default port number
		return DEFAULT_PORT_NUMBER;
	}
	
	@Override
	public ConnectionManager getConnectionManager(String nodeId) throws RemoteException {
		logger.debug("Obtaining the Connection Manager of node [" + nodeId + "]...");
		
		Node connectingNode;
		if((connectingNode = knownNodes.get(nodeId)) != null){
			logger.debug("Node previously known");
			return connectingNode.getConnectionManager();
		}
		
		try {
			connectingNode = new Node(nodeId, nodeManagement);
		} catch (UnknownHostException e) {
			logger.error("Wrong node ID");
			logger.error("Error message:" + e.getMessage());
			throw new WrongNodeIDException();
		}
		
		// TODO see where to set the disconnection of the node
		
		// connect to the registry of the target node
		Registry registry = LocateRegistry.getRegistry(nodeId);
		logger.debug("RMI Registry obtained successfully");
		
		// set the obtained registry as the connecting node one
		connectingNode.setRegistry(registry);
		
		try{
			// obtain the reference to his ConnectionManager
			try {
				logger.debug("Obtaining the Connection Manager...");
				ConnectionManager connectionManager = (ConnectionManager) registry.lookup(CONNECTION_MANAGER_NAME);
				logger.debug("Connection Manager obtained successfully");

				// set the obtained Connection Manager as the connecting node one
				connectingNode.setConnectionManager(connectionManager);

				// add the connecting node to the kown nodes list
				knownNodes.put(connectingNode.getNodeId(), connectingNode);
				logger.debug("Node added to the list of known nodes");

				return connectionManager;
			} catch (NotBoundException e) {
				logger.error("Remote reference not bounded to the name [" + CONNECTION_MANAGER_NAME + "]");
				logger.error("Error message: " + e.getMessage());
				throw new NoConnectionAvailableException();
			} catch (Exception e) {
				logger.error("There was an error and the remote Connection Manager coudn't be found");
				logger.debug("Error message:" + e.getMessage());
				throw new NoConnectionAvailableException();
			}
		} catch (NoConnectionAvailableException e) {
			logger.debug("Disconnecting the node [" + nodeId + "]");
			
			getClusterAdmimnistration().disconnectFromGroup(nodeId);
			
			throw new NoConnectionAvailableException("There was an error with the node connection and he " +
					"was disconnected from the group");
		}
	}
	
	@Override
	public ClusterCommunication getGroupCommunication() throws RemoteException {
		return messageManager;
	}

	@Override
	public ClusterAdministration getClusterAdmimnistration() throws RemoteException {
		return clusterAdministration;
	}

	@Override
	public Transactionable getNodeCommunication() throws RemoteException {
		
		// TODO see the noAvailableConnectionException thing... :(
		return transactionManager;
	}

	@Override
	public SimulationCommunication getSimulationCommunication() throws RemoteException {
		return simulationCommunication;
	}

	@Override
	public ThreePhaseCommit getThreePhaseCommit() throws RemoteException {
		return transactionManager.getThreePhaseCommit();
	}
	
	public ConcurrentHashMap<String, Node> getKnownNodes(){
		return knownNodes;
	}

	private Registry startRMIRegistry(){
		// RMI Registry
		Registry registry = null;
		
		// get the RMI Registry
		try {
			logger.debug("Creating the RMI Registry...");
			registry = LocateRegistry.createRegistry(nodeManagement.getLocalNode().getPort());
			logger.debug("RMI Registry raised successfully");
		} catch (RemoteException e) {
			logger.warn("Aparently, the RMI Registry was already instantiated");
			logger.warn("Error message:" + e.getMessage());
			
			// try to obtain the already created RMI Registry
			try {
				logger.debug("Try to obtain the already created RMI Registry");
				registry = LocateRegistry.getRegistry();
				logger.debug("RMI Registry joined successfully");
			} catch (RemoteException e1) {
				logger.error("The RMI Registry couldn't be obtained");
				logger.error("Error message:" + e1.getMessage());
			}
		}
		
		return registry;
	}
}
