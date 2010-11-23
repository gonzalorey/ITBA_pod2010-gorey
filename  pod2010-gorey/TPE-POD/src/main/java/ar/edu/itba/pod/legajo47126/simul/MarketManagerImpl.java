package ar.edu.itba.pod.legajo47126.simul;

import ar.edu.itba.pod.simul.market.MarketInspector;
import ar.edu.itba.pod.simul.market.MarketManager;

public class MarketManagerImpl implements MarketManager {

	DistributedMarket distributedMarket;
	
	@Override
	public void start() {
		distributedMarket.start();
	}
	
	@Override
	public void shutdown() {
		distributedMarket.finish();
	}
	
	public MarketManagerImpl() {
		distributedMarket = new DistributedMarket();
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
