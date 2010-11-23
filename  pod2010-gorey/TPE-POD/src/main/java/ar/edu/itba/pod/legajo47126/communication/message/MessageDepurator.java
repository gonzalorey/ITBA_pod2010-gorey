package ar.edu.itba.pod.legajo47126.communication.message;

import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import ar.edu.itba.pod.legajo47126.node.NodeManagement;

public class MessageDepurator implements Runnable {
	
	// instance of the log4j logger
	private static Logger logger = Logger.getLogger(MessageDepurator.class);

	// list of broadcasted messages
	private LinkedBlockingQueue<MessageContainer> broadcastedMessagesQueue;
	
	// time that informs when the expiration time was reached 
	private long messageExpirationTime;
	
	private NodeManagement nodeManagement;
	
	public MessageDepurator(NodeManagement nodeManagement, LinkedBlockingQueue<MessageContainer> broadcastedMessagesQueue, long messageExpirationTime){
		this.nodeManagement = nodeManagement;
		this.broadcastedMessagesQueue = broadcastedMessagesQueue;
		this.messageExpirationTime = messageExpirationTime;
	}
	
	@Override
	public void run() {
		// instantiate the datetime object
		DateTime dateTime = new DateTime();
		
		while(!nodeManagement.shouldExit()){
			// peek the first message of the queue
			MessageContainer messageContainer = broadcastedMessagesQueue.peek();
			
			if(messageContainer != null){
				if((dateTime.getMillis() - messageContainer.getTimeStamp()) > messageExpirationTime){
					// if the message expiration time was reached by the message timestamp, remove it from the queue 
					broadcastedMessagesQueue.remove(messageContainer);
				}
			} else {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					logger.error("Interrupted while sleeping");
					logger.error("Error message:" + e.getMessage());
				}
			}
		}
	}

}
