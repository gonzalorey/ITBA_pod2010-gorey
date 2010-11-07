package ar.edu.itba.pod.legajo47126.communication.paylod.impl;

import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import ar.edu.itba.pod.legajo47126.node.NodeManagement;
import ar.edu.itba.pod.simul.communication.Message;

public class MessageDepurator implements Runnable {
	
	// instance of the log4j logger
	private static Logger logger = Logger.getLogger(MessageDepurator.class);

	// list of broadcasted messages
	private LinkedBlockingQueue<Message> broadcastedMessagesQueue;
	
	// time that informs when the expiration time was reached 
	private long messageExpirationTime;
	
	public MessageDepurator(LinkedBlockingQueue<Message> broadcastedMessagesQueue){
		this.broadcastedMessagesQueue = broadcastedMessagesQueue;
		
		this.messageExpirationTime = NodeManagement.getConfigFile().getProperty("MessageExpirationTime", 2000);
	}
	
	@Override
	public void run() {
		// instantiate the datetime object
		DateTime dateTime = new DateTime();
		
		while(true){
			// peek the first message of the queue
			Message message = broadcastedMessagesQueue.peek();
			
			if(message != null){
				if((dateTime.getMillis() - message.getTimeStamp()) > messageExpirationTime){
					// if the message expiration time was reached by the message timestamp, remove it from the queue 
					broadcastedMessagesQueue.remove(message);
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
