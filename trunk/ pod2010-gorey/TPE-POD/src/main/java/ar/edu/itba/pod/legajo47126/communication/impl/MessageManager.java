package ar.edu.itba.pod.legajo47126.communication.impl;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;

import ar.edu.itba.pod.legajo47126.communication.paylod.impl.MessageDepurator;
import ar.edu.itba.pod.legajo47126.communication.paylod.impl.MessageProcessor;
import ar.edu.itba.pod.legajo47126.node.Node;
import ar.edu.itba.pod.simul.communication.ClusterCommunication;
import ar.edu.itba.pod.simul.communication.Message;
import ar.edu.itba.pod.simul.communication.MessageListener;

public class MessageManager implements ClusterCommunication, MessageListener{
	
	// instance of the log4j logger
	private static Logger logger = Logger.getLogger(MessageManager.class);

	// blocking queue that holds the messages during their arrival
	LinkedBlockingQueue<Message> messagesQueue;
	
	// list of broadcasted messages to be depurated every few seconds
	private LinkedBlockingQueue<Message> broadcastedMessagesQueue;
	
	public MessageManager() throws RemoteException{
		UnicastRemoteObject.exportObject(this, 0);
		
		// instance the message queue
		messagesQueue = new LinkedBlockingQueue<Message>();
		
		// instance the broadcasted messages queue
		broadcastedMessagesQueue = new LinkedBlockingQueue<Message>(100);	//TODO get it with the config file
	}
	
	public MessageManager(int queueSize) throws RemoteException{
		UnicastRemoteObject.exportObject(this, 0);
		
		// instance the message queue
		messagesQueue = new LinkedBlockingQueue<Message>(queueSize);
		
		// instance the broadcasted messages queue
		broadcastedMessagesQueue = new LinkedBlockingQueue<Message>(100);	//TODO get it with the config file
	}
	
	@Override
	public void broadcast(Message message) throws RemoteException {
		logger.debug("Broadcasting message [" + message + "]");
		
		// instance the gossip probability with the maximum
		double gossipProbability = 1;
		logger.debug("Start broadcasting with a gossip probability of " + gossipProbability);
		
		// create the random generator for the gossip probability
		Random rand = new Random();

		for(Node n : ConnectionManagerImpl.getInstance().getKnownNodes().values()){
			logger.debug("Sending the message to node [" + n + "]");
			
			if(rand.nextDouble() < gossipProbability){
				if(!n.getConnectionManager().getGroupCommunication().getListener().onMessageArrive(message)){
					// lowering the gossip probability
					gossipProbability -= 1/ConnectionManagerImpl.getInstance().getKnownNodes().size();
					logger.debug("Gossip probability lowered to " + gossipProbability);
				}
			}
		}
		
		// add the message to the broadcasted messages queue
		broadcastedMessagesQueue.add(message);
		logger.debug("Message added to the broadcasted messages queue");
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
		
		if(messagesQueue.contains(message)){
			logger.debug("Message already in the queue, reject it");
			return false;
		}
		
		if(broadcastedMessagesQueue.contains(message)){
			logger.debug("Message already broadcasted, reject it");
			return false;
		}
		
		logger.debug("Message added to the queue"); 
		messagesQueue.add(message);
		
		return true;
	}

	@Override
	public Iterable<Message> getNewMessages(String remoteNodeId) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}
	
	public void startMessageProcessing(){
		// start the message depurator to empty the list of broadcasted messages after their timestamp has expired  
		MessageDepurator messageDepurator = new MessageDepurator(broadcastedMessagesQueue);
		new Thread(messageDepurator).start();
		
		// start the message processor to process the arriving messages
		MessageProcessor messageProcessor = new MessageProcessor(messagesQueue);
		new Thread(messageProcessor).start();
	}
}
