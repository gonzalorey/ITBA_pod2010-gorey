package ar.edu.itba.pod.legajo47126.communication.impl.message;

import java.rmi.RemoteException;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;

import ar.edu.itba.pod.legajo47126.communication.impl.ConnectionManagerImpl;
import ar.edu.itba.pod.legajo47126.node.NodeManagement;
import ar.edu.itba.pod.simul.communication.Message;

public class MessageProcessor implements Runnable {

	// instance of the log4j logger
	private static Logger logger = Logger.getLogger(MessageProcessor.class);
	
	// list of arriving messages waiting for process
	private LinkedBlockingQueue<Message> messagesQueue;
	
	private long messageProcessingSleepTime;
	
	public MessageProcessor(LinkedBlockingQueue<Message> messageQueue){
		this.messagesQueue = messageQueue;
		
		messageProcessingSleepTime = NodeManagement.getConfigFile().getProperty("MessageProcessingSleepTime", 1000);
	}
	
	@Override
	public void run() {
		while(true){
			// peek the first message of the queue
			Message message = messagesQueue.peek();
			
			if(message != null){
				logger.debug("Processing message [" + message + "]");
				
				switch (message.getType()) {
					case DISCONNECT:
						logger.debug("DISCONNECT message received, broadcasting...");
						try {
							// broadcast the message
							ConnectionManagerImpl.getInstance().getGroupCommunication().broadcast(message);
							
							// TODO disconnect the node
						} catch (RemoteException e) {
							logger.error("The message couldn't be broadcasted");
							logger.error("Error message:" + e.getMessage());
						}
						break;
						
					case NEW_MESSAGE_REQUEST:
						logger.debug("NEW_MESSAGE_REQUEST message received");
						break;
						
					case NODE_AGENTS_LOAD_REQUEST:
						logger.debug("NODE_AGENTS_LOAD_REQUEST message received, broadcasting...");
						try {
							// broadcast the message
							ConnectionManagerImpl.getInstance().getGroupCommunication().broadcast(message);
							
							// TODO send a NODE_AGENT_LOAD message
						} catch (RemoteException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						break;

					default:
						logger.warn("Wrong type of message, it will be ignored");
						break;
				}
				
				logger.debug("Message successfully processed");
				
				// remove the message from the queue (it may not be the FIRT anymore)
				messagesQueue.remove(message);
					
			} else {
				
				// if no message was waiting to be processed, sleep for a while
				try {
					Thread.sleep(messageProcessingSleepTime);
				} catch (InterruptedException e) {
					logger.error("Interrupted while sleeping");
					logger.error("Error message:" + e.getMessage());
				}
			}
		}
	}

}
