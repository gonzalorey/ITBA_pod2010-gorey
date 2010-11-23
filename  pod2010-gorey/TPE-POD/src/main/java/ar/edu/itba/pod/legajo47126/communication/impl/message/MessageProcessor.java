package ar.edu.itba.pod.legajo47126.communication.impl.message;

import java.rmi.RemoteException;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;

import ar.edu.itba.pod.legajo47126.communication.impl.ConnectionManagerImpl;
import ar.edu.itba.pod.legajo47126.node.NodeManagement;
import ar.edu.itba.pod.simul.communication.Message;
import ar.edu.itba.pod.simul.communication.payload.DisconnectPayload;
import ar.edu.itba.pod.simul.communication.payload.NodeAgentLoadPayload;
import ar.edu.itba.pod.simul.communication.payload.ResourceRequestPayload;
import ar.edu.itba.pod.simul.communication.payload.ResourceTransferMessagePayload;

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
			MessageContainer messageContainer = null;
			try {
				// peek the first message of the queue
				messageContainer = messagesQueue.peek();
				
				if(messageContainer != null){
					logger.debug("Processing message [" + messageContainer.getMessage() + "]");
					
					switch (messageContainer.getMessage().getType()) {
						case DISCONNECT:
							logger.debug("DISCONNECT message received");
							doDisconnect(messageContainer);
							break;
						
						case NEW_MESSAGE_REQUEST:
							logger.debug("NEW_MESSAGE_REQUEST message received");
							doNewMessageRequest(messageContainer);
							break;
							
						case NEW_MESSAGE_RESPONSE:
							logger.debug("NEW_MESSAGE_RESPONSE message received");
							doNewMessageResponse(messageContainer);				
							break;
							
						case NODE_AGENTS_LOAD:
							logger.debug("NODE_AGENTS_LOAD message received");
							doNodeAgentsLoad(messageContainer);
							break;
							
						case NODE_AGENTS_LOAD_REQUEST:
							logger.debug("NODE_AGENTS_LOAD_REQUEST message received");
							doNodeAgentsLoadRequest(messageContainer);
							break;
						
						case NODE_MARKET_DATA:
							logger.debug("NODE_MARKET_DATA message received");
							doNodeMarketData(messageContainer);
							break;
							
						case NODE_MARKET_DATA_REQUEST:
							logger.debug("NODE_MARTEK_DATA_REQUEST message received");
							doNodeMarketDataRequest(messageContainer);
							break;
							
						case RESOURCE_REQUEST:
							logger.debug("RESOURCE_REQUEST message received");
							doResourceRequest(messageContainer);
							break;
							
						case RESOURCE_TRANSFER:
							logger.debug("RESOURCE_TRANSFER message received");
							doResourceTransfer(messageContainer);
							break;
							
						case RESOURCE_TRANSFER_CANCELED:
							logger.debug("RESOURCE_TRANSFER_CANCELED message received");
							doResourceTransferCanceled();
							break;
	
						default:
							logger.warn("Wrong type of message, it will be ignored");
							break;
					}
					
					logger.debug("Message successfully processed");
					
					// remove the message from the queue (it may not be the FIRT anymore)
					messagesQueue.remove(messageContainer);
					messageContainer = null;
						
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
				
				// remove the message from the queue despite the error
				if(messageContainer != null)
					messagesQueue.remove(messageContainer);
			}
		}
	}

	private void doDisconnect(MessageContainer messageContainer){
		try {
			// broadcast the message
			ConnectionManagerImpl.getInstance().getGroupCommunication().broadcast(messageContainer.getMessage());
			logger.debug("Message successfully broadcasted");
		} catch (RemoteException e) {
			logger.error("The message couldn't be broadcasted");
			logger.error("Error message: " + e.getMessage());
		}

		DisconnectPayload payload = (DisconnectPayload) messageContainer.getMessage().getPayload(); 
		try {
			// disconnect the node
			ConnectionManagerImpl.getInstance().getClusterAdmimnistration().
				disconnectFromGroup(payload.getDisconnectedNodeId());
			logger.debug("Node disconnected from the local node");
		} catch (IllegalArgumentException e) {
			logger.info("The node didn't belong to the group");
			logger.info("Error message: " + e.getMessage());
			//TODO watch out with these errors, they are everywhere with the remote exceptions...
		} catch (RemoteException e) {
			logger.error("The node couldn't be disconnected");
			logger.error("Error message: " + e.getMessage());
		}
	}
	
	private void doNewMessageRequest(MessageContainer messageContainer) {
		// TODO Auto-generated method stub
		
	}
	
	private void doNewMessageResponse(MessageContainer messageContainer) {
		// TODO Auto-generated method stub
		
	}
	
	private void doNodeAgentsLoad(MessageContainer messageContainer) {
		// obtaining the payload and adding the load to the node agents load map
		NodeAgentLoadPayload payload = (NodeAgentLoadPayload) messageContainer.getMessage().getPayload();
		NodeManagement.getNodeKnownAgentsLoad().setNodeLoad(messageContainer.getMessage().getNodeId(), payload.getLoad());
		logger.debug("Node [" + messageContainer.getMessage().getNodeId() + "] and load [" + payload.getLoad() + "] added to the local map");
	}

	private void doNodeAgentsLoadRequest(MessageContainer messageContainer) {
		try {
			// broadcast the message
			ConnectionManagerImpl.getInstance().getGroupCommunication().broadcast(messageContainer.getMessage());
			logger.debug("Message successfully broadcasted");
		} catch (RemoteException e) {
			logger.error("The message couldn't be broadcasted");
			logger.error("Error message: " + e.getMessage());
		}
		
		logger.debug("Sending a NODE_AGENTS_LOAD message...");
		Message loadMessage = MessageFactory.NodeAgentLoadMessage();
		try {
			// sending the message
			ConnectionManagerImpl.getInstance().getGroupCommunication().send(loadMessage, messageContainer.getMessage().getNodeId());
			logger.debug("Message successfully sent");
		} catch (RemoteException e) {
			logger.error("The message couldn't be sent");
			logger.error("Error message: " + e.getMessage());
		}
	}
	
	private void doNodeMarketData(MessageContainer messageContainer) {
		// TODO Auto-generated method stub
		
	}
	
	private void doNodeMarketDataRequest(MessageContainer messageContainer) {
		// TODO Auto-generated method stub
		
	}
	
	private void doResourceRequest(MessageContainer messageContainer) {
		ResourceRequestPayload payload = (ResourceRequestPayload) messageContainer.getMessage().getPayload();
		long timeout = NodeManagement.getConfigFile().getProperty("TransactionTimeout", 1000);
		try{
			// begin the transaction with the remote node
			logger.info("Beginning transaction with [" + messageContainer.getMessage().getNodeId() + 
					"] with a timeout [" + timeout + "]");
			ConnectionManagerImpl.getInstance().getNodeCommunication().beginTransaction(
					messageContainer.getMessage().getNodeId(), timeout);
			
			// exchange my resources
			logger.info("Exchanging an amount of [" + payload.getAmountRequested() + "] " +
					"of resource [" + payload.getResource() + "]");
			ConnectionManagerImpl.getInstance().getNodeCommunication().exchange(payload.getResource(), 
					payload.getAmountRequested(), NodeManagement.getLocalNode().getNodeId(), 
					messageContainer.getMessage().getNodeId());
									
			// end the transaction
			logger.info("Ending the transaction");
			ConnectionManagerImpl.getInstance().getNodeCommunication().endTransaction();
		} catch (Exception e) {
			logger.error("There was an error during the transaction, aborting...");
			logger.error("Error message: " + e.getMessage());
			
			try{
				// rollback the transactions
				logger.info("Rollback the transaction");
				ConnectionManagerImpl.getInstance().getNodeCommunication().rollback();
			} catch (Exception e1) {
				logger.error("There was an error during the transaction abort");
				logger.error("Error message: " + e1.getMessage());
			}
		}
	}
	
	private void doResourceTransfer(MessageContainer messageContainer) {
		ResourceTransferMessagePayload payload = 
			(ResourceTransferMessagePayload) messageContainer.getMessage().getPayload();
		
		
		if(payload.getDestination().equals(NodeManagement.getLocalNode().getNodeId())){
			// it was me the destination node, I should add the resources
			logger.info("Adding to the market an amount of [" + payload.getAmount() + "] of resource [" 
					+ payload.getResource() + "]...");
			NodeManagement.getManagerImpl().market().addToRemoteSelling(payload.getResource(), payload.getAmount());
			logger.debug("Resources added successfully");
			
		} else {
			// it wasn't me the destination, so I should remove the resources
			logger.info("Removing from the market an amount of [" + payload.getAmount() + "] of resource [" 
					+ payload.getResource() + "]...");
			NodeManagement.getManagerImpl().market().removeFromSelling(payload.getResource(), payload.getAmount());
			logger.debug("Resources removed successfully");
		}
	}
	
	private void doResourceTransferCanceled() {
		// TODO Auto-generated method stub
		
	}
	
}
