package ar.edu.itba.pod.legajo47126.communication.paylod.impl;

import ar.edu.itba.pod.simul.communication.payload.ResourceRequestPayload;
import ar.edu.itba.pod.simul.market.Resource;

public class ResourceRequestPayloadImpl implements ResourceRequestPayload {

	/**
	 * default serial version id
	 */
	private static final long serialVersionUID = 1L;
	
	private Resource resource;
	
	private int amountRequested;
	
	public ResourceRequestPayloadImpl(Resource resource, int amountRequested) {
		this.resource = resource;
		this.amountRequested = amountRequested;
	}
	
	@Override
	public int getAmountRequested() {
		return amountRequested;
	}

	@Override
	public Resource getResource() {
		return resource;
	}

}
