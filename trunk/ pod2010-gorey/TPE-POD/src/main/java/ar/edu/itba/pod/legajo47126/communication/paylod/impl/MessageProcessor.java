package ar.edu.itba.pod.legajo47126.communication.paylod.impl;

import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;

import ar.edu.itba.pod.simul.communication.Message;

public class MessageProcessor implements Runnable {

	// instance of the log4j logger
	private static Logger logger = Logger.getLogger(MessageProcessor.class);
	
	LinkedBlockingQueue<Message> messageQueue;
	
	public MessageProcessor(LinkedBlockingQueue<Message> messageQueue){
		this.messageQueue = messageQueue;
	}
	
	@Override
	public void run() {
		while(true){
			Message message = messageQueue.peek();
			if(message != null){
				logger.info("Processing message [" + message + "]");
				
				//TODO process the message
			}
			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				logger.error("Interrupted while sleeping");
				e.printStackTrace();
			}
		}
	}

}
