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
	
	protected final Multiset<ResourceStock> remotelySelling = ConcurrentHashMultiset.create();
	
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
				for (ResourceStock seller : remotelySelling) {
					if (buyer.resource().equals(seller.resource())) {
						transferRemote(buyer, seller);
						logger.info("Selling resource [" + seller.resource() + "] to the remote [" + buyer.name() + "]");
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
	
//	@Override
//	protected void matchBothEnds() {
//		logger.debug("Matching both ends...");
//		
//		for (ResourceStock buyer : buying) {
//			for (ResourceStock seller : selling) {
//				if (buyer.resource().equals(seller.resource())) {
//					transfer(buyer, seller);
//					logger.info("Selling " + buyer.resource() + " to " + seller.name());
//				}
//			}
//		
//			// the buyer amount could have been altered
//			if(buying.count(buyer) != 0){  
//				for (ResourceStock seller : remotelySelling) {
//					if (buyer.resource().equals(seller.resource())) {
//						transferRemote(buyer, seller);
//						logger.info("Selling " + buyer.resource() + " to the remote " + seller.name());
//					}
//				}
//				
//				// the buyer amount could have been altered
//				int amount = buying.count(buyer);
//				if(amount != 0){
//					Message message = MessageFactory.ResourceRequestMessage(nodeManagement.getLocalNode().getNodeId(),
//							buyer.resource(), amount);
//					logger.info("Sending broadcast message for buying an amount of " + amount + " of " + buyer.resource());
//					try {
//						logger.debug("Broadcasting message [" + message + "]");
//						nodeManagement.getConnectionManager().getGroupCommunication().broadcast(message);
//					} catch (RemoteException e) {
//						logger.error("An error ocurred while trying to broadcast the message");
//						logger.error("Error message: " + e.getMessage());
//					}
//				}
//			}
//		}
//	}
	
	protected int transferRemote(ResourceStock buyer, ResourceStock seller) {
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
						seller.remove(transfer);
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
					logTransfer(seller, buyer, transfer);
					
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
	
	public void addToRemotelySelling(Resource resource, int amount){
		for(ResourceStock seller : remotelySelling){
			if(seller.resource().equals(resource)){
				remotelySelling.setCount(seller, remotelySelling.count(seller) + amount);
				break;
			}
		}
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
	
	
	public void feedbackLogger(ResourceStock stock, int amount, String op) {
		logger.info(stock.name() + " --> " + op + ": " + amount + " of " + stock.resource() + "(stock: " + stock.current() + ")");
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
