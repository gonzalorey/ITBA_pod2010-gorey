package ar.edu.itba.pod.legajo47126.communication.paylod.impl;

import ar.edu.itba.pod.simul.communication.payload.ResourceTransferMessagePayload;
import ar.edu.itba.pod.simul.market.Resource;

public class ResourceTransferMessagePayloadImpl implements ResourceTransferMessagePayload {

	/**
	 * default serial version id
	 */
	private static final long serialVersionUID = 1L;
	
	private int amount;
	
	private String destination;
	
	private Resource resource;
	
	private String source;
	
	public ResourceTransferMessagePayloadImpl(Resource resource, int amount, String source, String destination) {
		this.resource = resource;
		this.amount = amount;
		this.source = source;
		this.destination = destination;
	}
	
	@Override
	public int getAmount() {
		return amount;
	}

	@Override
	public String getDestination() {
		return destination;
	}

	@Override
	public Resource getResource() {
		return resource;
	}

	@Override
	public String getSource() {
		return source;
	}

}
