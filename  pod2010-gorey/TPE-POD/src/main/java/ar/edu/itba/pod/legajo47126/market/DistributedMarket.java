package ar.edu.itba.pod.legajo47126.market;

import java.rmi.RemoteException;

import org.apache.log4j.Logger;

import ar.edu.itba.pod.legajo47126.communication.message.MessageFactory;
import ar.edu.itba.pod.legajo47126.node.NodeManagement;
import ar.edu.itba.pod.simul.communication.Message;
import ar.edu.itba.pod.simul.local.LocalMarket;
import ar.edu.itba.pod.simul.market.Resource;
import ar.edu.itba.pod.simul.market.ResourceStock;

import com.google.common.collect.ConcurrentHashMultiset;
import com.google.common.collect.Multiset;

public class DistributedMarket extends LocalMarket {
	
	// instance of the log4j logger
	private static Logger logger = Logger.getLogger(DistributedMarket.class);
	
	protected final Multiset<Resource> remotelySelling = ConcurrentHashMultiset.create();
	
	private NodeManagement nodeManagement;
	
	public DistributedMarket(NodeManagement nodeManagement) {
		super();
		this.nodeManagement = nodeManagement;
	}
	
	@Override
	protected void matchBothEnds() {
		logger.info("Matching both ends...");
		
		for (ResourceStock buyer : buying) {
			boolean transfered = false;
			for (ResourceStock seller : selling) {
				if (buyer.resource().equals(seller.resource())) {
					logger.info("Selling resource [" + seller.resource() + "] to [" + buyer.name() + "]");
					transfer(buyer, seller);
					transfered = true;
				}
			}
			
			if(!transfered){
				for (Resource seller : remotelySelling) {
					if (buyer.resource().equals(seller)) {
						transferRemote(buyer, seller);
						logger.info("Selling resource [" + seller + "] to the remote [" + buyer.name() + "]");
					}
				}
			}
		}
		
		for(ResourceStock buyer : buying.elementSet()){
			// the buyer amount could have been altered
			int amount = buying.count(buyer);
			if(amount != 0){
				Message message = MessageFactory.ResourceRequestMessage(nodeManagement.getLocalNode().getNodeId(),
						buyer.resource(), amount);
				logger.info("Sending broadcast message for buying an amount of [" + amount + "] of [" + buyer.resource() + "]");
				try {
					logger.debug("Broadcasting message [" + message + "]");
					nodeManagement.getConnectionManager().getGroupCommunication().broadcast(message);
				} catch (RemoteException e) {
					logger.error("An error ocurred while trying to broadcast the message");
					logger.error("Error message: " + e.getMessage());
				}
			}
		}
	}
	
	protected int transferRemote(ResourceStock buyer, Resource seller) {
		while(true) {
			int wanted = buying.count(buyer);
			int available = remotelySelling.count(seller);
			int transfer = Math.min(available, wanted);
			
			if (transfer == 0) {
				return 0;
			}
	
			boolean procured = remotelySelling.setCount(seller, available, available - transfer);
			if (procured) {
				boolean sent = buying.setCount(buyer, wanted, wanted - transfer);
				if (sent) {
					try {
						buyer.add(transfer);
					}
					catch (RuntimeException e) {
						remotelySelling.add(seller, transfer);
						buying.remove(buyer, transfer);
						continue;
					}
					try {
						buyer.add(transfer);
					}
					catch (RuntimeException e) {
						// market takes care of what was sold. 
						// TODO: To fully solve this case, 2PCommit or 3PC is required. Is it worth?   
						buying.remove(buyer, transfer);
						continue;
					}
					logTransferRemote(seller, buyer, transfer);
					
					return transfer;
				}
				else {
					// Compensation. restore what we took from the order!
					remotelySelling.add(seller, transfer);
				}
			}
			// Reaching here mean we hit a race condition. Try again.
		}
	}
	
	public void logTransferRemote(Resource from, ResourceStock to, int amount) {
		transactionCount++;
		createhistoryRemote(from, to, amount);		
	}
	
	protected void createhistoryRemote(Resource resource, ResourceStock to, int amount) {
		createHistory(resource.name(), to.name(), resource, amount);
	}
	
	public void addToRemotelySelling(Resource resource, int amount){
//		for(ResourceStock seller : selling){
//			if(seller.resource().equals(resource)){
//				selling.setCount(seller, selling.count(seller) + amount);
//				break;
//			}
//		}
		remotelySelling.setCount(resource, remotelySelling.count(resource) + amount);
	}
	
	public void removeFromSelling(Resource resource, int amount){
		for(ResourceStock seller : selling){
			if(seller.resource().equals(resource) && (selling.count(seller) - amount >= 0)){
				selling.setCount(seller, selling.count(seller) - amount);
				seller.remove(amount);
				break;
			}
		}
	}
	
	public int getLocalStock(Resource resource, int amount){
		int localStock = 0;
		logger.info("Looking for an amount of [" + amount + "] of resource [" + resource + "]");
		for(ResourceStock seller : selling.elementSet()){
			logger.info("An amount of [" + selling.count(seller) + "] of resource [" + seller.resource() + "]");
			if(seller.resource().equals(resource)){
				localStock = selling.count(seller);
				if(amount < localStock)
					localStock = amount;
				logger.info("Found local stock of [" + localStock + "]");
				break;
			}
		}
		
		return localStock;
	}
	
	public void feedbackLogger(ResourceStock stock, int amount, String op) {
		logger.info(stock.name() + " --> " + op + ": " + amount + " of " + stock.resource() + "(stock: " + stock.current() + ")");
		System.out.println(stock.name() + " --> " + op + ": " + amount + " of " + stock.resource() + "(stock: " + stock.current() + ")");
	}

	@Override
	public void offer(ResourceStock stock, int maxQuantity) {
		feedbackLogger(stock, maxQuantity, "OFFER");
		super.offer(stock, maxQuantity);
	}
	
	@Override
	public void offerMore(ResourceStock stock, int amount) {
		feedbackLogger(stock, amount, "OFFER MORE");
		super.offerMore(stock, amount);
	}
	
	@Override
	public void request(ResourceStock stock, int maxQuantity) {
		feedbackLogger(stock, maxQuantity, "REQUEST");
		super.request(stock, maxQuantity);
	}
	
	@Override
	public void requestMore(ResourceStock stock, int amount) {
		feedbackLogger(stock, amount, "REQUEST MORE");
		super.requestMore(stock, amount);
	}
	
}
