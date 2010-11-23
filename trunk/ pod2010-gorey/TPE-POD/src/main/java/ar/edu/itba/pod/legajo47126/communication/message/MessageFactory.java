package ar.edu.itba.pod.legajo47126.communication.message;

import org.joda.time.DateTime;

import ar.edu.itba.pod.legajo47126.communication.paylod.DisconnectPayloadImpl;
import ar.edu.itba.pod.legajo47126.communication.paylod.NewMessageRequestPayloadImpl;
import ar.edu.itba.pod.legajo47126.communication.paylod.NewMessageResponsePayloadImpl;
import ar.edu.itba.pod.legajo47126.communication.paylod.NodeAgentLoadPayloadImpl;
import ar.edu.itba.pod.legajo47126.communication.paylod.NodeAgentLoadRequestPayloadImpl;
import ar.edu.itba.pod.legajo47126.communication.paylod.NodeMarketDataPayloadImpl;
import ar.edu.itba.pod.legajo47126.communication.paylod.NodeMarketDataRequestPayloadImpl;
import ar.edu.itba.pod.legajo47126.communication.paylod.ResourceRequestPayloadImpl;
import ar.edu.itba.pod.simul.communication.MarketData;
import ar.edu.itba.pod.simul.communication.Message;
import ar.edu.itba.pod.simul.communication.MessageType;
import ar.edu.itba.pod.simul.communication.payload.Payload;
import ar.edu.itba.pod.simul.market.Resource;

public class MessageFactory {
	
	/**
	 * Creates a DISCONNECT message (should broadcast)
	 * 
	 * @param disconnectedNodeId nodeId of the node being desconected 
	 * @return a message containing the local node as sender, the current timestamp, and the propper payload
	 */
	public static Message DisconnectMessage(String nodeId, String disconnectedNodeId){
		return new Message(nodeId, new DateTime().getMillis(), 
				MessageType.DISCONNECT, new DisconnectPayloadImpl(disconnectedNodeId));
	}
	
	// TODO write all the comments...
	
	/**
	 * Creates a NEW_MESSAGE_REQUEST message
	 * 
	 * @return a message containing the local node as sender, the current timestamp, and the propper payload 
	 */
	public static Message NewMessageRequest(String nodeId){
		return new Message(nodeId, new DateTime().getMillis(), 
				MessageType.NEW_MESSAGE_REQUEST, new NewMessageRequestPayloadImpl());
	}
	
	/**
	 * Creates a NEW_MESSAGE_RESPONSE message
	 * 
	 * @return a message containing the local node as sender, the current timestamp, and the propper payload
	 */
	public static Message NewMessageResponse(String nodeId){
		return new Message(nodeId, new DateTime().getMillis(), 
				MessageType.NEW_MESSAGE_RESPONSE, new NewMessageResponsePayloadImpl());
	}
	
	/**
	 * Creates a NODE_AGENT_LOAD message
	 * 
	 * @return a message containing the local node as sender, the current timestamp, and the propper payload
	 */
	public static Message NodeAgentLoadMessage(String nodeId, int load){
//		int load = NodeManagement.getSimulationManager().getAgentsLoad();
		
		return new Message(nodeId, new DateTime().getMillis(), 
				MessageType.NODE_AGENTS_LOAD, new NodeAgentLoadPayloadImpl(load));
	}
	
	/**
	 * Creates a NODE_AGENT_LOAD_REQUEST message. This message is sent by a coordinator in order to know node agent load
	 * 
	 * @return a message containing the local node as sender, the current timestamp, and the propper payload
	 */
	public static Message NodeAgentLoadRequestMessage(String nodeId){
		return new Message(nodeId, new DateTime().getMillis(), 
				MessageType.NODE_AGENTS_LOAD_REQUEST, new NodeAgentLoadRequestPayloadImpl());
	}
	
	public static Message NodeMarketDataMessage(String nodeId){
		MarketData marketData = null;	//TODO get the propper market data
		
		return new Message(nodeId, new DateTime().getMillis(), 
				MessageType.NODE_MARKET_DATA, new NodeMarketDataPayloadImpl(marketData));
	}
	
	public static Message NodeMarketDataRequestMessage(String nodeId){
		return new Message(nodeId, new DateTime().getMillis(), 
				MessageType.NODE_MARKET_DATA_REQUEST, new NodeMarketDataRequestPayloadImpl());
	}
	
	public static Message ResourceRequestMessage(String nodeId, Resource resource, int amountRequested){
		return new Message(nodeId, new DateTime().getMillis(), 
				MessageType.RESOURCE_REQUEST, new ResourceRequestPayloadImpl(resource, amountRequested));
	}
	
	public static Message ResourceTransferMessage(String nodeId, Payload payload){
		return new Message(nodeId, new DateTime().getMillis(), 
				MessageType.RESOURCE_TRANSFER, payload);
	}
}
