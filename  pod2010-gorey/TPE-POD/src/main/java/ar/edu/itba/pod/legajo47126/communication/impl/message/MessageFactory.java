package ar.edu.itba.pod.legajo47126.communication.impl.message;

import org.joda.time.DateTime;

import ar.edu.itba.pod.legajo47126.communication.paylod.impl.DisconnectPayloadImpl;
import ar.edu.itba.pod.legajo47126.communication.paylod.impl.NewMessageRequestPayloadImpl;
import ar.edu.itba.pod.legajo47126.communication.paylod.impl.NewMessageResponsePayloadImpl;
import ar.edu.itba.pod.legajo47126.communication.paylod.impl.NodeAgentLoadPayloadImpl;
import ar.edu.itba.pod.legajo47126.communication.paylod.impl.NodeAgentLoadRequestPayloadImpl;
import ar.edu.itba.pod.legajo47126.communication.paylod.impl.NodeMarketDataPayloadImpl;
import ar.edu.itba.pod.legajo47126.communication.paylod.impl.NodeMarketDataRequestPayloadImpl;
import ar.edu.itba.pod.legajo47126.communication.paylod.impl.ResourceRequestPayloadImpl;
import ar.edu.itba.pod.legajo47126.communication.paylod.impl.ResourceTransferMessagePayloadImpl;
import ar.edu.itba.pod.legajo47126.node.NodeManagement;
import ar.edu.itba.pod.simul.communication.MarketData;
import ar.edu.itba.pod.simul.communication.Message;
import ar.edu.itba.pod.simul.communication.MessageType;
import ar.edu.itba.pod.simul.market.Resource;

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
	
	// TODO write all the comments...
	
	public static Message NewMessageRequest(){
		return new Message(NodeManagement.getLocalNode().getNodeId(), new DateTime().getMillis(), 
				MessageType.NEW_MESSAGE_REQUEST, new NewMessageRequestPayloadImpl());
	}
	
	public static Message NewMessageResponse(){
		return new Message(NodeManagement.getLocalNode().getNodeId(), new DateTime().getMillis(), 
				MessageType.NEW_MESSAGE_RESPONSE, new NewMessageResponsePayloadImpl());
	}
	
	public static Message NodeAgentLoadMessage(){
		int load = 0; // TODO get the propper load
		
		return new Message(NodeManagement.getLocalNode().getNodeId(), new DateTime().getMillis(), 
				MessageType.NODE_AGENTS_LOAD, new NodeAgentLoadPayloadImpl(load));
	}
	
	public static Message NodeAgentLoadRequestMessage(){
		return new Message(NodeManagement.getLocalNode().getNodeId(), new DateTime().getMillis(), 
				MessageType.NODE_AGENTS_LOAD_REQUEST, new NodeAgentLoadRequestPayloadImpl());
	}
	
	public static Message NodeMarketDataMessage(){
		MarketData marketData = null;	//TODO get the propper market data
		
		return new Message(NodeManagement.getLocalNode().getNodeId(), new DateTime().getMillis(), 
				MessageType.NODE_MARKET_DATA, new NodeMarketDataPayloadImpl(marketData));
	}
	
	public static Message NodeMarketDataRequestMessage(){
		return new Message(NodeManagement.getLocalNode().getNodeId(), new DateTime().getMillis(), 
				MessageType.NODE_MARKET_DATA_REQUEST, new NodeMarketDataRequestPayloadImpl());
	}
	
	public static Message ResourceRequestMessage(){
		Resource resource = null;	//TODO get the propper values
		int amountRequested = 0;
		
		return new Message(NodeManagement.getLocalNode().getNodeId(), new DateTime().getMillis(), 
				MessageType.RESOURCE_REQUEST, new ResourceRequestPayloadImpl(resource, amountRequested));
	}
	
	public static Message ResourceTransferMessage(){
		Resource resource = null;	//TODO get the propper values
		int amount = 0;
		String source = null;
		String destination = null;
	
		return new Message(NodeManagement.getLocalNode().getNodeId(), new DateTime().getMillis(), 
				MessageType.RESOURCE_TRANSFER, new ResourceTransferMessagePayloadImpl(resource, amount, source, destination));
	}
}
