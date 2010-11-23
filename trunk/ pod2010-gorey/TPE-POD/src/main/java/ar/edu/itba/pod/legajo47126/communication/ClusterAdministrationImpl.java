package ar.edu.itba.pod.legajo47126.communication;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import ar.edu.itba.pod.legajo47126.communication.interfaces.RegistryPort;
import ar.edu.itba.pod.legajo47126.node.NodeManagement;
import ar.edu.itba.pod.legajo47126.simul.coordinator.NewNodeCoordinator;
import ar.edu.itba.pod.simul.communication.ClusterAdministration;

public class ClusterAdministrationImpl implements ClusterAdministration, RegistryPort {

	// instance of the log4j logger
	private static Logger logger = Logger.getLogger(ClusterAdministrationImpl.class);
	
	// name of the group that the node is connected to
	private String groupId = null; 
	
	// collection with the nodes that belong to the group 
	private CopyOnWriteArrayList<String> groupNodes = null;
	
	private NodeManagement nodeManagement;
	
	/**
	 * Instance the cluster administration with the destination node
	 * 
	 * @param destinationNode node to be used as the destination to all the cluster operations
	 * @throws RemoteException 
	 */
	public ClusterAdministrationImpl(NodeManagement nodeManagement) throws RemoteException{
		UnicastRemoteObject.exportObject(this, 0);
		
		this.nodeManagement = nodeManagement;
		
		// instantiate the list of group nodes with a concurrent array list
		groupNodes = new CopyOnWriteArrayList<String>();
	}
	
	@Override
	public void createGroup() throws RemoteException {
		logger.debug("Creating the group...");
		
		if (groupId == null){
			// set the group id with the current milliseconds
			long millis = new DateTime().getMillis();
			groupId = Long.toString(millis); 
		} else {
			throw new IllegalStateException("The node belongs to a group already");
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
		logger.debug("Connecting to the group of the initial node [" + initialNode + "]...");
		
		if(groupId != null)
			// the local node already belongs to a group
			throw new IllegalStateException("The local node [" + nodeManagement.getLocalNode() + 
					"] already belongs to a group");
		
		if(initialNode.equals(nodeManagement.getLocalNode().getNodeId()))
			// the local node is the same as the initial node
			throw new IllegalArgumentException("The local node [" + nodeManagement.getLocalNode() + 
					"] is the same as the initial node");
		
		logger.debug("Get the group id of the initial node [" + initialNode + "]");
		
		// set the local node group as the one from the initial node
		groupId = nodeManagement.getConnectionManager().getConnectionManager(initialNode).
				getClusterAdmimnistration().getGroupId();
		logger.debug("Local node connected to the group [" + groupId + "]. Now, tell the initial node to add him");
		
		try{
			// tell the initial node to add the local node
			Iterable<String> initialNodeGroupNodes = nodeManagement.getConnectionManager().getConnectionManager(initialNode).
			getClusterAdmimnistration().addNewNode(nodeManagement.getLocalNode().getNodeId());
			
			// adding the initial node group nodes to the groupNodes
			for(String nodeId : initialNodeGroupNodes){
				if(!nodeId.equals(nodeManagement.getLocalNode().getNodeId())){
					groupNodes.add(nodeId);
					logger.debug("Node [" + nodeId + "] added to the group nodes list");
				}
			}
			
			groupNodes.add(initialNode);
			logger.debug("Node [" + initialNode + "] added to the group nodes list");
			
		} catch (Exception e) {
			// set the group id back to the default state
			groupId = null;
			logger.error("There was an error during the addition of the local node");  
			logger.error("Error message:" + e.getMessage());
			e.printStackTrace();
			
			throw new RemoteException();
		}
		
		logger.debug("Initial node [" + initialNode + "] successfully added the " +
				"local node [" + nodeManagement.getLocalNode() + "] to the group");

		// start the coordinator thread
		NewNodeCoordinator coordinatorManager = new NewNodeCoordinator(nodeManagement);
		new Thread(coordinatorManager).start();
	}
	
	@Override
	public Iterable<String> addNewNode(String newNode) throws RemoteException {
		logger.debug("Adding a new node [" + newNode + "]...");
		
		if(groupId == null)
			// the local node is not connected to a cluster
			throw new IllegalStateException("The local node [" + nodeManagement.getLocalNode() + "]" +
					" is not connected to a group");
		
		// get the new node group id
		String newNodeGroupId = nodeManagement.getConnectionManager().getConnectionManager(newNode).
							getClusterAdmimnistration().getGroupId();
		
		logger.debug("Seeing if the local node's group is the same as the new node's group");
		
		if(!groupId.equals(newNodeGroupId))
			// the local node's group id isn't the same as the newNode's group
			throw new IllegalArgumentException("The local node's group isn't the same as" +
					"the new node's group");
		
		// obtain the random nodes to return
		CopyOnWriteArrayList<String> randomGroupNodes = getRandomNodes(groupNodes);
		
		// add the initial node to other known nodes
		addNewNodeToOtherNodes(newNode);
		
		groupNodes.add(newNode);
		logger.debug("New node " +  newNode + " added successfully to the group");
		
		return randomGroupNodes;
	}

	@Override
	public void disconnectFromGroup(String nodeId) throws RemoteException {
		logger.debug("Disconnecting the node [" +  nodeId + "] from the group");
		
		if(!groupNodes.contains(nodeId))
			throw new IllegalArgumentException("The node doesn't belong to the group");
		
		// remove the node from the group and the known nodes list 
		groupNodes.remove(nodeId);
		nodeManagement.getConnectionManager().getKnownNodes().remove(nodeId);
		logger.debug("Node removed from groupNodes and knownNodes lists");
	}
	
	private CopyOnWriteArrayList<String> getRandomNodes(CopyOnWriteArrayList<String> nodes){
		CopyOnWriteArrayList<String> randomGroupNodes = new CopyOnWriteArrayList<String>();
		
		Random rand = new Random();
		double comparator = rand.nextDouble();
		
		for(String nodeId : nodes){
			if(rand.nextDouble() < comparator)
				randomGroupNodes.add(nodeId);
		}
		
		return randomGroupNodes;
	}
	
	public CopyOnWriteArrayList<String> getGroupNodes(){
		return groupNodes;
	}
	
	private void addNewNodeToOtherNodes(String newNode) {
		logger.debug("Adding node [" +  newNode + "] to other nodes...");
		
		Random rand = new Random();
		double comparator = rand.nextDouble();
		int amountAdded = 0;
		
		for(String nodeId : groupNodes){
			if(rand.nextDouble() < comparator){
				try {
					nodeManagement.getConnectionManager().getConnectionManager(nodeId).getClusterAdmimnistration().
						addNewNode(newNode);
					logger.debug("Added to node [" + nodeId + "]");
					amountAdded++;
				} catch (RemoteException e) {
					logger.error("The node [" + newNode + "] couldn't be added to the node [" + nodeId + "]");
					logger.error("Error message:" + e.getMessage());
				}
			}
		}
		
	 	logger.debug("Node added to " + amountAdded + " node" + ((amountAdded != 1)?"s":""));
	}
	
	public void clearGroup(){
		groupId = null;
		groupNodes.clear();
	}
}
