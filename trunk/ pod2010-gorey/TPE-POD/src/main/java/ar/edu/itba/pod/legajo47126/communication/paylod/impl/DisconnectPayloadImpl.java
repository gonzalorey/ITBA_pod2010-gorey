package ar.edu.itba.pod.legajo47126.communication.paylod.impl;

import ar.edu.itba.pod.simul.communication.payload.DisconnectPayload;

public class DisconnectPayloadImpl implements DisconnectPayload {

	/**
	 * default serial version id
	 */
	private static final long serialVersionUID = 1L;

	private String disconnectedNodeId;
	
	public DisconnectPayloadImpl(String disconnectedNodeId){
		this.disconnectedNodeId = disconnectedNodeId;
	}
	
	@Override
	public String getDisconnectedNodeId() {
		return disconnectedNodeId;
	}

}
