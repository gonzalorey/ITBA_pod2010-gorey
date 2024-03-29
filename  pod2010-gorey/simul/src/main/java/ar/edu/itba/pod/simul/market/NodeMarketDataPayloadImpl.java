package ar.edu.itba.pod.simul.market;

import ar.edu.itba.pod.simul.communication.MarketData;
import ar.edu.itba.pod.simul.communication.payload.NodeMarketDataPayload;


/**
 * Message sent by nodes with their market transfer data
 * @author POD
 *
 */
public class NodeMarketDataPayloadImpl implements NodeMarketDataPayload {
	/**
	 * Generated serial version ID
	 */
	private static final long serialVersionUID = -3056133863035374280L;
	private final MarketData marketData;

	/**
	 * @param marketData node market data
	 */
	public NodeMarketDataPayloadImpl(MarketData marketData) {
		this.marketData = marketData;
	}
	
	@Override
	public MarketData getMarketData() {
		return marketData;
	}

}