package ar.edu.itba.pod.legajo47126.communication.impl;

import java.rmi.AccessException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import org.apache.log4j.Logger;

import ar.edu.itba.pod.legajo47126.communication.interfaces.RegistryPort;
import ar.edu.itba.pod.legajo47126.node.NodeManagement;
import ar.edu.itba.pod.simul.communication.ClusterAdministration;
import ar.edu.itba.pod.simul.communication.ClusterCommunication;
import ar.edu.itba.pod.simul.communication.ConnectionManager;
import ar.edu.itba.pod.simul.communication.ReferenceName;
import ar.edu.itba.pod.simul.communication.SimulationCommunication;
import ar.edu.itba.pod.simul.communication.ThreePhaseCommit;
import ar.edu.itba.pod.simul.communication.Transactionable;

public class ConnectionManagerImpl implements ConnectionManager, ReferenceName, RegistryPort {
	
	// singletone instance of the ConnectionManger
	private static ConnectionManagerImpl connectionManager = null;
	
	// RMI Registry
	private Registry registry;
	
	//TODO add a list of the known registries, so I don't have to locate one every time
	
	// instance of the log4j logger
	private static Logger logger = Logger.getLogger(ConnectionManagerImpl.class);
	
	// ClusterAdministration instance to handle the group connections
	private ClusterAdministration clusterAdministration;
	
	// MessageManager instance, that implements ClusterCommunication and MessageListener
	private MessageManager messageManager;
	
	private ConnectionManagerImpl() throws RemoteException{
		UnicastRemoteObject.exportObject(this, 0);
		logger.debug("Instantiating the Connection Manager...");
		
		// start the RMI Registry
		registry = startRMIRegistry();
		
		// instance the ClusterAdministration
		clusterAdministration = new ClusterAdministrationImpl(NodeManagement.getLocalNode());
		
		// instance the MessageManager
		messageManager = new MessageManager();
		messageManager.startMessageProcessor();
		
		// publish the ConnectionManager
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
	}
	
	public static synchronized ConnectionManagerImpl getInstance() throws RemoteException{
		if(connectionManager == null)
			ConnectionManagerImpl.connectionManager = new ConnectionManagerImpl();
		
		return ConnectionManagerImpl.connectionManager;
	}
	
	@Override
	protected Object clone() throws CloneNotSupportedException {
		// it won't be cloned now either
		throw new CloneNotSupportedException();
	}
	
	@Override
	public int getClusterPort() throws RemoteException {
		// return always the default port number
		return DEFAULT_PORT_NUMBER;
	}
	
	@Override
	public ConnectionManager getConnectionManager(String nodeId) throws RemoteException {
		logger.debug("Obtaining the RMI Registry from node [" + nodeId + "]...");
		
		// connect to the registry of the target node
		Registry registry = LocateRegistry.getRegistry(nodeId);
		logger.debug("RMI Registry obtained successfully");
		
		// obtain the reference to his ConnectionManager
		try {
			logger.debug("Obtaining the ConnectionManager...");
			ConnectionManager connectionManager = (ConnectionManager) registry.lookup(CONNECTION_MANAGER_NAME);
			logger.debug("Connection Manager obtained successfully");
			
			return connectionManager;
		} catch (NotBoundException e) {
			logger.error("Remote reference not bounded to the name [" + CONNECTION_MANAGER_NAME + "]");
			e.printStackTrace();
			//TODO make NoConnectionAvailableException();
			//throw new NoConnectionAvailableException();
			throw new RemoteException();
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SimulationCommunication getSimulationCommunication() throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ThreePhaseCommit getThreePhaseCommit() throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	private Registry startRMIRegistry(){
		// RMI Registry
		Registry registry = null;
		
		// get the RMI Registry
		try {
			logger.debug("Create the RMI Registry...");
			registry = LocateRegistry.createRegistry(NodeManagement.getLocalNode().getPort());
			logger.debug("RMI Registry raised successfully");
		} catch (RemoteException e) {
			logger.warn("Aparently, the RMI Registry was already instantiated");
			e.printStackTrace();
			
			// try to obtain the already created RMI Registry
			try {
				logger.debug("Try to obtain the already created RMI Registry");
				registry = LocateRegistry.getRegistry();
				logger.debug("RMI Registry joined successfully");
			} catch (RemoteException e1) {
				logger.error("The RMI Registry couldn't be obtained");
				e1.printStackTrace();
			}
		}
		
		return registry;
	}
}
