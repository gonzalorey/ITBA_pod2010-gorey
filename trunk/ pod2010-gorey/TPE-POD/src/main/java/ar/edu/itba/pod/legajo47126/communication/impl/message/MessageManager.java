package ar.edu.itba.pod.legajo47126.communication.impl.message;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import ar.edu.itba.pod.legajo47126.communication.impl.ClusterAdministrationImpl;
import ar.edu.itba.pod.legajo47126.communication.impl.ConnectionManagerImpl;
import ar.edu.itba.pod.legajo47126.node.NodeManagement;
import ar.edu.itba.pod.simul.communication.ClusterCommunication;
import ar.edu.itba.pod.simul.communication.Message;
import ar.edu.itba.pod.simul.communication.MessageListener;

public class MessageManager implements ClusterCommunication, MessageListener{
	
	// instance of the log4j logger
	private static Logger logger = Logger.getLogger(MessageManager.class);

	// blocking queue that holds the messages during their arrival
	private LinkedBlockingQueue<MessageContainer> messagesQueue;
	
	// list of broadcasted messages to be depurated every few seconds
	private LinkedBlockingQueue<MessageContainer> broadcastedMessagesQueue;
	
	// list of the last synchronization to a node 
	private ConcurrentHashMap<String, Long> synchronizationTime; 
	
	// default values
	private final int DEFAULT_MESSAGES_QUEUE_SIZE = 1000;
	private final int DEFAULT_BROADCASTED_MESSAGES_QUEUE_SIZE = 1000;
	
	public MessageManager() throws RemoteException{
		UnicastRemoteObject.exportObject(this, 0);

		// instance the message queue
		messagesQueue = new LinkedBlockingQueue<MessageContainer>(NodeManagement.getConfigFile().
				getProperty("MessagesQueueSize", DEFAULT_MESSAGES_QUEUE_SIZE));
		
		// instance the broadcasted messages queue
		broadcastedMessagesQueue = new LinkedBlockingQueue<MessageContainer>(NodeManagement.getConfigFile().
				getProperty("BroadcastedMessagesQueueSize", DEFAULT_BROADCASTED_MESSAGES_QUEUE_SIZE));
		
		synchronizationTime = new ConcurrentHashMap<String, Long>();
	}
	
	@Override
	public void broadcast(Message message) throws RemoteException {
		logger.debug("Broadcasting message [" + message + "]");
		
		// instance the gossip probability with the maximum
		double gossipProbability = 1;
		logger.debug("Start broadcasting with a gossip probability of " + gossipProbability);
		
		// create the random generator for the gossip probability
		Random rand = new Random();
		
		for(String nodeId : ((ClusterAdministrationImpl)ConnectionManagerImpl.getInstance().getClusterAdmimnistration()).getGroupNodes()){
			if(rand.nextDouble() < gossipProbability){
				logger.debug("Sending the message to node [" + nodeId + "]");
				if(!ConnectionManagerImpl.getInstance().getConnectionManager(nodeId).getGroupCommunication()
						.getListener().onMessageArrive(message)){
					// lowering the gossip probability
					gossipProbability -= 1/((ClusterAdministrationImpl)ConnectionManagerImpl.getInstance().
							getClusterAdmimnistration()).getGroupNodes().size();
					logger.debug("Gossip probability lowered to " + gossipProbability);
				} else {
					// set the synchronization time of the node
					long timeStamp = new DateTime().getMillis();
					if(synchronizationTime.contains(nodeId)){
						synchronizationTime.replace(nodeId, timeStamp);
					} else {
						synchronizationTime.put(nodeId, timeStamp);
					}
					logger.debug("Node [" + nodeId + "] synchronized at [" + timeStamp + "]");
				}
			}
		}
		
		// add the message to the broadcasted messages queue
		broadcastedMessagesQueue.add(new MessageContainer(message));
		logger.debug("Message added to the broadcasted messages queue");
		
		logger.debug("Broadcasting ended");
	}
	
	@Override
	public boolean send(Message message, String nodeId) throws RemoteException {
		logger.debug("Sending message [" + message + "] to node [" + nodeId + "]");

		// get the connection manager of the node id and then execute his onMessageArrive
		return ConnectionManagerImpl.getInstance().getConnectionManager(nodeId).getGroupCommunication().
			getListener().onMessageArrive(message);
	}

	@Override
	public MessageListener getListener() throws RemoteException {
		return this;
	}

	@Override
	public boolean onMessageArrive(Message message) throws RemoteException {
		logger.debug("Message [" + message + "] arrived");
		
		for(MessageContainer messageContainer : messagesQueue){
			if(messageContainer.getMessage().equals(message)){
				logger.debug("Message already in the queue, reject it");
				return false;		
			}
		}
		
		for(MessageContainer messageContainer : broadcastedMessagesQueue){
			if(messageContainer.getMessage().equals(message)){
				logger.debug("Message already broadcasted, reject it");
				return false;		
			}
		}
		
		logger.debug("Message added to the queue"); 
		messagesQueue.add(new MessageContainer(message));
		
		return true;
	}

	@Override
	public Iterable<Message> getNewMessages(String remoteNodeId) throws RemoteException {
		logger.debug("Requesting the new messages from node [" + remoteNodeId + "]");
		
		LinkedBlockingQueue<Message> newMessages = new LinkedBlockingQueue<Message>();
		
		for(MessageContainer messageContainer : broadcastedMessagesQueue){
			if(!synchronizationTime.contains(remoteNodeId) || 
					synchronizationTime.get(remoteNodeId) < messageContainer.getTimeStamp()){
				newMessages.add(messageContainer.getMessage());
				logger.debug("Message [" + messageContainer.getMessage() + "] added to the new messages to send");
			}
		}
		
		// set the synchronization time of the node
		long timeStamp = new DateTime().getMillis();
		if(synchronizationTime.contains(remoteNodeId)){
			synchronizationTime.replace(remoteNodeId, timeStamp);
		} else {
			synchronizationTime.put(remoteNodeId, timeStamp);
		}
		logger.debug("Node [" + remoteNodeId + "] synchronized at [" + timeStamp + "]");
		
		return newMessages;
	}
	
	public void startMessageProcessing(){
		// start the message depurator to empty the list of broadcasted messages after their timestamp has expired  
		MessageDepurator messageDepurator = new MessageDepurator(broadcastedMessagesQueue);
		new Thread(messageDepurator).start();
		
		// start the message processor to process the arriving messages
		MessageProcessor messageProcessor = new MessageProcessor(messagesQueue);
		new Thread(messageProcessor).start();
		
		// start the message requester to get the new messages from every group node
		if(NodeManagement.getConfigFile().getProperty("MessageRequesterEnabled", false)){
			MessageRequester messageRequester = new MessageRequester();
			new Thread(messageRequester).start();
		}
	}
}
