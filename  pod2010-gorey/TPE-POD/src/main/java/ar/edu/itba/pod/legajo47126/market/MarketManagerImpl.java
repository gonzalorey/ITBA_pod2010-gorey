package ar.edu.itba.pod.legajo47126.market;

import org.apache.log4j.Logger;

import ar.edu.itba.pod.legajo47126.node.NodeManagement;
import ar.edu.itba.pod.simul.market.MarketInspector;
import ar.edu.itba.pod.simul.market.MarketManager;

public class MarketManagerImpl implements MarketManager {

	// instance of the log4j logger
	private static Logger logger = Logger.getLogger(MarketManagerImpl.class);
	
	private DistributedMarket distributedMarket;
	
	private NodeManagement nodeManagement;
	
	public MarketManagerImpl(NodeManagement nodeManagement) {
		this.nodeManagement = nodeManagement;
	}
	
	@Override
	public void start() {
		logger.info("Opening market...");
		System.out.println("Opening market...");
		distributedMarket = new DistributedMarket(nodeManagement);
		distributedMarket.start();
		logger.info("Market opened");
		System.out.println("Market opened");
	}
	
	@Override
	public void shutdown() {
		logger.info("Closing market...");
		System.out.println("Closing market");
		distributedMarket.finish();
		logger.info("Market closed");
		System.out.println("Market closed");
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
