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
	private static Logger logger = Logger.getLogger(NodeManagement.class);
	
	protected final Multiset<ResourceStock> remoteSelling = ConcurrentHashMultiset.create();
	
	private NodeManagement nodeManagement;
	
	public DistributedMarket(NodeManagement nodeManagement) {
		super();
		this.nodeManagement = nodeManagement;
	}
	
	@Override
	protected void matchBothEnds() {
		logger.debug("Matching both ends...");
		
		for (ResourceStock buyer : buying) {
			for (ResourceStock seller : selling) {
				if (buyer.resource().equals(seller.resource())) {
					transfer(buyer, seller);
				}
			}
		
			// the buyer amount could have been altered
			if(buying.count(buyer) != 0){  
				for (ResourceStock seller : remoteSelling) {
					if (buyer.resource().equals(seller.resource())) {
						transferRemote(buyer, seller);
					}
				}
			}
			
			// the buyer amount could have been altered
			int amount = buying.count(buyer);
			if(amount != 0){
				Message message = MessageFactory.ResourceRequestMessage(nodeManagement.getLocalNode().getNodeId(),
						buyer.resource(), amount);
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
	
	protected int transferRemote(ResourceStock buyer, ResourceStock seller) {
		while(true) {
			int wanted = buying.count(buyer);
			int available = remoteSelling.count(seller);
			int transfer = Math.min(available, wanted);
			
			if (transfer == 0) {
				return 0;
			}
	
			boolean procured = remoteSelling.setCount(seller, available, available - transfer);
			if (procured) {
				boolean sent = buying.setCount(buyer, wanted, wanted - transfer);
				if (sent) {
					try {
						seller.remove(transfer);
					}
					catch (RuntimeException e) {
						remoteSelling.add(seller, transfer);
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
					remoteSelling.add(seller, transfer);
				}
			}
			// Reaching here mean we hit a race condition. Try again.
		}
	}
	
	public void addToRemoteSelling(Resource resource, int amount){
		for(ResourceStock seller : remoteSelling){
			if(seller.resource().equals(resource)){
				remoteSelling.setCount(seller, remoteSelling.count(seller) + amount);
				break;
			}
		}
	}
	
	public void removeFromSelling(Resource resource, int amount){
		for(ResourceStock seller : selling){
			if(seller.resource().equals(resource)){
				selling.setCount(seller, selling.count(seller) + amount);
				seller.remove(amount);
				break;
			}
		}
	}
	
}
