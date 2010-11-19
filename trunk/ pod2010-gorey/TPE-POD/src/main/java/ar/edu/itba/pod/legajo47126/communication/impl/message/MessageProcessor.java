package ar.edu.itba.pod.legajo47126.communication.impl.message;

import java.rmi.RemoteException;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;

import ar.edu.itba.pod.legajo47126.communication.impl.ClusterAdministrationImpl;
import ar.edu.itba.pod.legajo47126.communication.impl.ConnectionManagerImpl;
import ar.edu.itba.pod.legajo47126.node.NodeManagement;
import ar.edu.itba.pod.simul.communication.Message;
import ar.edu.itba.pod.simul.communication.payload.DisconnectPayload;
import ar.edu.itba.pod.simul.communication.payload.NodeAgentLoadPayload;

public class MessageProcessor implements Runnable {

	// instance of the log4j logger
	private static Logger logger = Logger.getLogger(MessageProcessor.class);
	
	// list of arriving messages waiting for process
	private LinkedBlockingQueue<MessageContainer> messagesQueue;
	
	private long messageProcessingSleepTime;
	
	public MessageProcessor(LinkedBlockingQueue<MessageContainer> messageQueue){
		this.messagesQueue = messageQueue;
		
		messageProcessingSleepTime = NodeManagement.getConfigFile().getProperty("MessageProcessingSleepTime", 1000);
	}
	
	@Override
	public void run() {
		while(true){
			try {
				// peek the first message of the queue
				MessageContainer messageContainer = messagesQueue.peek();
				
				if(messageContainer != null){
					logger.debug("Processing message [" + messageContainer + "]");
					
					switch (messageContainer.getMessage().getType()) {
						case DISCONNECT:
							logger.debug("DISCONNECT message received, broadcasting...");
							try {
								// broadcast the message
								ConnectionManagerImpl.getInstance().getGroupCommunication().broadcast(messageContainer.getMessage());
								logger.debug("Message successfully broadcasted");
							} catch (RemoteException e) {
								logger.error("The message couldn't be broadcasted");
								logger.error("Error message:" + e.getMessage());
							}
	
							DisconnectPayload disconnectPayload = (DisconnectPayload) messageContainer.getMessage().getPayload(); 
							try {
								// disconnect the node
								((ClusterAdministrationImpl)ConnectionManagerImpl.getInstance().getClusterAdmimnistration()).
									disconnectFromGroupWithoutBroadcasting(disconnectPayload.getDisconnectedNodeId());
								logger.debug("Node disconnected from the local node");
							} catch (RemoteException e) {
								logger.error("The node couldn't be disconnected");
								logger.error("Error message:" + e.getMessage());
							}
							break;
						
						case NEW_MESSAGE_REQUEST:
							logger.debug("NEW_MESSAGE_REQUEST message received");
							// TODO implement...
							break;
							
						case NEW_MESSAGE_RESPONSE:
							logger.debug("NEW_MESSAGE_RESPONSE message received");
							// TODO implement...					
							break;
							
						case NODE_AGENTS_LOAD:
							logger.debug("NODE_AGENTS_LOAD message received");
							
							// obtaining the payload and adding the load to the node agents load map
							NodeAgentLoadPayload payload = (NodeAgentLoadPayload) messageContainer.getMessage().getPayload();
							NodeManagement.getNodeAgentsLoad().setNodeLoad(messageContainer.getMessage().getNodeId(), payload.getLoad());
							logger.debug("Node [" + messageContainer.getMessage().getNodeId() + "] and load [" + payload.getLoad() + "] added to the local map");
							break;
							
						case NODE_AGENTS_LOAD_REQUEST:
							logger.debug("NODE_AGENTS_LOAD_REQUEST message received, broadcasting...");
							try {
								// broadcast the message
								ConnectionManagerImpl.getInstance().getGroupCommunication().broadcast(messageContainer.getMessage());
								logger.debug("Message successfully broadcasted");
							} catch (RemoteException e) {
								logger.error("The message couldn't be broadcasted");
								logger.error("Error message:" + e.getMessage());
							}
							
							logger.debug("Sending a NODE_AGENTS_LOAD message...");
							Message loadMessage = MessageFactory.NodeAgentLoadMessage();
							try {
								// sending the message
								ConnectionManagerImpl.getInstance().getGroupCommunication().send(loadMessage, messageContainer.getMessage().getNodeId());
								logger.debug("Message successfully sent");
							} catch (RemoteException e) {
								logger.error("The message couldn't be sent");
								logger.error("Error message:" + e.getMessage());
							}
							break;
						
						case NODE_MARKET_DATA:
							logger.debug("NODE_MARKET_DATA message received");
							// TODO implement...
							break;
							
						case NODE_MARKET_DATA_REQUEST:
							logger.debug("NODE_MARTEK_DATA_REQUEST message received");
							// TODO implement...
							break;
							
						case RESOURCE_REQUEST:
							logger.debug("RESOURCE_REQUEST message received");
							// TODO implement...
							break;
							
						case RESOURCE_TRANSFER:
							logger.debug("RESOURCE_TRANSFER message received");
							// TODO implement...
							break;
							
						case RESOURCE_TRANSFER_CANCELED:
							logger.debug("RESOURCE_TRANSFER_CANCELED message received");
							// TODO implement...
							break;
	
						default:
							logger.warn("Wrong type of message, it will be ignored");
							// TODO implement...
							break;
					}
					
					logger.debug("Message successfully processed");
					
					// remove the message from the queue (it may not be the FIRT anymore)
					messagesQueue.remove(messageContainer);
						
				} else {
					
					// if no message was waiting to be processed, sleep for a while
					try {
						Thread.sleep(messageProcessingSleepTime);
					} catch (InterruptedException e) {
						logger.error("Interrupted while sleeping");
						logger.error("Error message:" + e.getMessage());
					}
				}
			} catch (Exception e) {
				logger.error("There was an error during the message processing");
				logger.error("Error message:" +  e.getMessage());
			}
		}
	}

}
