package communication.impl;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import communication.interfaces.RegistryPort;

import ar.edu.itba.pod.simul.communication.ClusterAdministration;
import ar.edu.itba.pod.simul.communication.ClusterCommunication;
import ar.edu.itba.pod.simul.communication.ConnectionManager;
import ar.edu.itba.pod.simul.communication.ReferenceName;
import ar.edu.itba.pod.simul.communication.SimulationCommunication;
import ar.edu.itba.pod.simul.communication.ThreePhaseCommit;
import ar.edu.itba.pod.simul.communication.Transactionable;

public class ConnectionManagerImpl implements ConnectionManager, ReferenceName, RegistryPort {
	
	// singletone instance of the Connection Manger
	private static ConnectionManagerImpl connectionManager = null;
	
	private ConnectionManagerImpl(){
		// nothing here...
	}
	
	public static synchronized ConnectionManagerImpl getInstance(){
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
		
		ConnectionManager connectionManager = null;
		
		// connect to the registry running on nodeId 
		Registry registry = LocateRegistry.getRegistry(nodeId);
		
		// obtain the reference to his Connection Manager
		try {
			connectionManager = (ConnectionManager) registry.lookup(CONNECTION_MANAGER_NAME);
		} catch (NotBoundException e) {
			// TODO log this one
			e.printStackTrace();
			
			//throw new NoConnectionAvailableException();
		}
		
		return connectionManager;
	}
	
	@Override
	public ClusterCommunication getGroupCommunication() throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ClusterAdministration getClusterAdmimnistration() throws RemoteException {
		// TODO Auto-generated method stub
		return null;
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

}
