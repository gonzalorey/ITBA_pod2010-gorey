package ar.edu.itba.pod.legajo47126.communication.impl.message;

import org.joda.time.DateTime;

import ar.edu.itba.pod.legajo47126.communication.paylod.impl.DisconnectPayloadImpl;
import ar.edu.itba.pod.legajo47126.node.NodeManagement;
import ar.edu.itba.pod.simul.communication.Message;
import ar.edu.itba.pod.simul.communication.MessageType;

public class MessageFactory {
	
	/**
	 * Creates a DISCONNECT message
	 * 
	 * @param disconnectedNodeId nodeId of the node being desconected 
	 * @return a message containing the local node as sender, the current timestamp, and the propper payload
	 */
	public static Message DisconnectMessage(String disconnectedNodeId){
		return new Message(NodeManagement.getLocalNode().getNodeId(), new DateTime().getMillis(), 
				MessageType.DISCONNECT, new DisconnectPayloadImpl(disconnectedNodeId));
	}
}
