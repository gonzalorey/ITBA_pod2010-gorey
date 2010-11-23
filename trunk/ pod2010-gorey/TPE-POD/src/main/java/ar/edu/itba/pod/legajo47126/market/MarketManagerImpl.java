package ar.edu.itba.pod.legajo47126.market;

import ar.edu.itba.pod.legajo47126.node.NodeManagement;
import ar.edu.itba.pod.simul.market.MarketInspector;
import ar.edu.itba.pod.simul.market.MarketManager;

public class MarketManagerImpl implements MarketManager {

	private DistributedMarket distributedMarket;
	
	private NodeManagement nodeManagement;
	
	public MarketManagerImpl(NodeManagement nodeManagement) {
		this.nodeManagement = nodeManagement;
	}
	
	@Override
	public void start() {
		distributedMarket = new DistributedMarket(nodeManagement);
		distributedMarket.start();
	}
	
	@Override
	public void shutdown() {
		distributedMarket.finish();
	}
	
	@Override
	public DistributedMarket market() {
		return distributedMarket;
	}
	
	@Override
	public MarketInspector inspector() {
		return market();
	}

}
