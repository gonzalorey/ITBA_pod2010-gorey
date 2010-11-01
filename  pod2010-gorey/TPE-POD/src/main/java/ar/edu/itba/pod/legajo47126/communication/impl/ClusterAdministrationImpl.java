package ar.edu.itba.pod.legajo47126.communication.impl;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.CopyOnWriteArrayList;

import org.joda.time.DateTime;

import ar.edu.itba.pod.legajo47126.communication.interfaces.RegistryPort;
import ar.edu.itba.pod.legajo47126.node.Node;
import ar.edu.itba.pod.simul.communication.ClusterAdministration;

public class ClusterAdministrationImpl implements ClusterAdministration, RegistryPort {

	// destination node to be used during the cluster operations
	private Node destinationNode = null;
	
	// name of the group that the node is connected to
	private String groupId = null; 
	
	// collection with the nodes that belong to the group 
	private CopyOnWriteArrayList<String> groupNodes = null;
	
	/**
	 * Instance the cluster administration with the destination node
	 * 
	 * @param destinationNode node to be used as the destination to all the cluster operations
	 * @throws RemoteException 
	 */
	public ClusterAdministrationImpl(Node destinationNode) throws RemoteException{
		UnicastRemoteObject.exportObject(this, 0);
		
		this.destinationNode = destinationNode;
	}
	
	@Override
	public void createGroup() throws RemoteException {
		if (groupId == null){
			// set the group id with the current milliseconds
			long millis = new DateTime().getMillis();
			groupId = Long.toString(millis); 
			
			// instantiate the list of group nodes with a concurrent array list
			groupNodes = new CopyOnWriteArrayList<String>();
		
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
		
		System.out.println("connecting to group...");
		if(groupId != null)
			// the destination node already belongs to a group
			throw new IllegalStateException();
		
		System.out.println("I don't belong to a group");
		if(initialNode.equals(destinationNode.getNodeId()))
			// the destination node is the same as the initial node
			throw new IllegalArgumentException();
		
		System.out.println("get the group id of " + initialNode);
		// set the destination node group as the one from the initial node
		groupId = ConnectionManagerImpl.getInstance().getConnectionManager(initialNode).
				getClusterAdmimnistration().getGroupId();
		
		System.out.println("I'm connected to " + groupId + ". Now, tell him to add me");
		// tell the initial node to add the destination node
		ConnectionManagerImpl.getInstance().getConnectionManager(initialNode).
		getClusterAdmimnistration().addNewNode(destinationNode.getNodeId());
		
		System.out.println("connected ^_^");
	}
	
	@Override
	public Iterable<String> addNewNode(String newNode) throws RemoteException {
		
		System.out.println("adding new node...");
		if(groupId == null)
			// the destination node is not connected to a cluster
			throw new IllegalStateException();
		
		System.out.println("the destination node is connected to the group " + groupId);
		// get the new node group id
		String newNodeGroupId = ConnectionManagerImpl.getInstance().getConnectionManager(newNode).
							getClusterAdmimnistration().getGroupId();
		
		System.out.println("seeing if the destination group id is the same as " + newNodeGroupId);
		if(!groupId.equals(newNodeGroupId))
			// the destination node's group id isn't the same as the newNode's group
			throw new IllegalArgumentException();
		
		System.out.println("adding the node");
		
		// add the node to the group nodes
		groupNodes.add(newNode);
		
		System.out.println("tadaaaaaaaa");
		return groupNodes;
	}

	@Override
	public void disconnectFromGroup(String nodeId) throws RemoteException {
		// TODO Auto-generated method stub
	}

}
