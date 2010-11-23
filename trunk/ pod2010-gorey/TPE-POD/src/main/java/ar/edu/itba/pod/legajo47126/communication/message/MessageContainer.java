package ar.edu.itba.pod.legajo47126.communication.message;

import org.joda.time.DateTime;

import ar.edu.itba.pod.simul.communication.Message;

public class MessageContainer {
	
	// message contained
	private Message message;
	
	// timestamp with the message arrival time
	private long timeStamp;
	
	public MessageContainer(Message message) {
		this.message = message;
		this.timeStamp = new DateTime().getMillis();
	}

	public Message getMessage() {
		return message;
	}

	public long getTimeStamp() {
		return timeStamp;
	}
	
}
