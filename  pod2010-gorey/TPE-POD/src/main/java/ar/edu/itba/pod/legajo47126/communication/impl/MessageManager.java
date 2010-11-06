package ar.edu.itba.pod.legajo47126.communication.impl;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;

import ar.edu.itba.pod.legajo47126.communication.paylod.impl.MessageProcessor;
import ar.edu.itba.pod.simul.communication.ClusterCommunication;
import ar.edu.itba.pod.simul.communication.Message;
import ar.edu.itba.pod.simul.communication.MessageListener;

public class MessageManager implements ClusterCommunication, MessageListener{
	
	// instance of the log4j logger
	private static Logger logger = Logger.getLogger(MessageManager.class);

	// blocking queue that holds the messages during their arrival
	LinkedBlockingQueue<Message> messageQueue;
	
	public MessageManager() throws RemoteException{
		UnicastRemoteObject.exportObject(this, 0);
		
		// instance the message queue
		messageQueue = new LinkedBlockingQueue<Message>();		
	}
	
	public MessageManager(int queueSize) throws RemoteException{
		UnicastRemoteObject.exportObject(this, 0);
		
		// instance the message queue
		messageQueue = new LinkedBlockingQueue<Message>(queueSize);
	}
	
	@Override
	public void broadcast(Message message) throws RemoteException {
		// TODO Auto-generated method stub
		
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean onMessageArrive(Message message) throws RemoteException {
		logger.debug("Message [" + message + "] arrived");
		
		if(messageQueue.contains(message)){
			logger.debug("Message already in the queue, reject it");
			return false;
		}
		
		logger.debug("Message added to the queue"); 
		messageQueue.add(message);
		
		return true;
	}

	@Override
	public Iterable<Message> getNewMessages(String remoteNodeId) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}
	
	public void startMessageProcessor(){
		MessageProcessor messageProcessor = new MessageProcessor(messageQueue);
		
		Thread thread = new Thread(messageProcessor);
		thread.start();
	}
}
