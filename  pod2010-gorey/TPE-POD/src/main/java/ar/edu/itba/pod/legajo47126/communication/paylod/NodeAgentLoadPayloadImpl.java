package ar.edu.itba.pod.legajo47126.communication.paylod;

import ar.edu.itba.pod.simul.communication.payload.NodeAgentLoadPayload;

public class NodeAgentLoadPayloadImpl implements NodeAgentLoadPayload {

	/**
	 * default serial version id
	 */
	private static final long serialVersionUID = 1L;
	
	private int load;
	
	public NodeAgentLoadPayloadImpl(int load) {
		this.load = load;
	}
	
	@Override
	public int getLoad() {
		return load;
	}

}
