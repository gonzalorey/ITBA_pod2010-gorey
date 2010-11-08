package ar.edu.itba.pod.legajo47126.communication.paylod.impl;

import ar.edu.itba.pod.simul.communication.MarketData;
import ar.edu.itba.pod.simul.communication.payload.NodeMarketDataPayload;

public class NodeMarketDataPayloadImpl implements NodeMarketDataPayload {

	/**
	 * default serial version id
	 */
	private static final long serialVersionUID = 1L;
	
	private MarketData marketData;
	
	public NodeMarketDataPayloadImpl(MarketData marketData) {
		this.marketData = marketData;
	}
	
	@Override
	public MarketData getMarketData() {
		return marketData;
	}

}
