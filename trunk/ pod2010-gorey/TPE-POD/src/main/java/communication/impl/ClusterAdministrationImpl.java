package communication.impl;

import java.rmi.RemoteException;
import java.util.concurrent.CopyOnWriteArrayList;

import org.joda.time.DateTime;

import communication.interfaces.RegistryPort;

import ar.edu.itba.pod.simul.communication.ClusterAdministration;

public class ClusterAdministrationImpl implements ClusterAdministration, RegistryPort {

	// node id of the destination node
	private String nodeId;
	
	// name of the group that the node is connected to
	private String groupId = null; 
	
	// collection with the nodes that belong to the group 
	private CopyOnWriteArrayList<String> groupNodes = null;
	
	// port used by all the nodes belonging to the group
	//private int clusterPort;
	
	/**
	 * Instance the cluster administration with the destination node
	 * 
	 * @param nodeId node to be used as the destination to all the cluster administration operations
	 */
	public ClusterAdministrationImpl(String nodeId){
		this.nodeId = nodeId;
	}
	
	@Override
	public void createGroup() throws RemoteException {
		if (groupId != null){
			// set the group id with the current milliseconds
			long millis = new DateTime().getMillis();
			groupId = Long.toString(millis); 
			
			// instantiate the list of group nodes with a concurrent array list
			groupNodes = new CopyOnWriteArrayList<String>();
			
			// get the default port number
			//clusterPort = DEFAULT_PORT_NUMBER;
		} else {
			throw new IllegalStateException();
		}
	}
	
	@Override
	public String getGroupId() throws RemoteException {
		return groupId;
	}
	
	@Override
	public boolean isConnectedToGroup() throws RemoteException {
		return (groupId != null);
	}
		
	@Override
	public void connectToGroup(String initialNode) throws RemoteException {
		
		if(groupId != null)
			// the destination node already belongs to a group
			throw new IllegalStateException();
		
		if(initialNode == nodeId)
			// the destination node is the same as the initial node
			throw new IllegalArgumentException();
		
		// set the destination node group as the one from the initial node
		groupId = ConnectionManagerImpl.getInstance().getConnectionManager(initialNode).
				getClusterAdmimnistration().getGroupId();
		
		// tell the initial node to add the destination node
		ConnectionManagerImpl.getInstance().getConnectionManager(initialNode).
		getClusterAdmimnistration().addNewNode(nodeId);
	}
	
	@Override
	public Iterable<String> addNewNode(String newNode) throws RemoteException {
		
		if(groupId == null)
			// the destination node is not connected to a cluster
			throw new IllegalStateException();
		
		// get the new node group id
		String newNodeGroupId = ConnectionManagerImpl.getInstance().getConnectionManager(newNode).
							getClusterAdmimnistration().getGroupId();
		
		if(groupId != newNodeGroupId)
			// the destination node's group id isn't the same as the newNode's group
			throw new IllegalArgumentException();
		
		// add the node to the group nodes
		groupNodes.add(newNode);
		
		return groupNodes;
	}

	@Override
	public void disconnectFromGroup(String nodeId) throws RemoteException {
		// TODO Auto-generated method stub
	}

}
