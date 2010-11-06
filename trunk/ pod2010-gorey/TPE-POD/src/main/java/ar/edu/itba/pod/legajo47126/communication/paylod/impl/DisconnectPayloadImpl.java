package ar.edu.itba.pod.legajo47126.communication.paylod.impl;

import ar.edu.itba.pod.simul.communication.payload.DisconnectPayload;

public class DisconnectPayloadImpl implements DisconnectPayload {

	/**
	 * default serial version id
	 */
	private static final long serialVersionUID = 1L;

	private String disconectedNodeId;
	
	public DisconnectPayloadImpl(String disconectedNodeId){
		this.disconectedNodeId = disconectedNodeId;
	}
	
	@Override
	public String getDisconnectedNodeId() {
		return disconectedNodeId;
	}

}
