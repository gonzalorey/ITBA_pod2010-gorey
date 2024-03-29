package ar.edu.itba.pod.legajo47126.communication.message;

import java.rmi.RemoteException;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArraySet;

import org.apache.log4j.Logger;

import ar.edu.itba.pod.legajo47126.communication.ClusterAdministrationImpl;
import ar.edu.itba.pod.legajo47126.node.NodeManagement;
import ar.edu.itba.pod.simul.communication.Message;

public class MessageRequester implements Runnable{
	
	// instance of the log4j logger
	private static Logger logger = Logger.getLogger(MessageRequester.class);
	
	private static long messageRequesterSleeptime;
	
	// default values
	private final int DEFAULT_MESSAGE_REQUEST_SLEEPTIME = 2000;
	
	NodeManagement nodeManagement;
	
	public MessageRequester(NodeManagement nodeManagement) {
		this.nodeManagement = nodeManagement;
		
		messageRequesterSleeptime = nodeManagement.getConfigFile().getProperty("MessageRequesterSleeptime", 
				DEFAULT_MESSAGE_REQUEST_SLEEPTIME);
	}
	
	@Override
	public void run() {
		logger.debug("MessageRequester started");
		
		while(!nodeManagement.shouldExit()){
			try {
				while((nodeManagement.getConnectionManager()) == null){
					// crapiest thing ever...
					try {
						Thread.sleep(100);
					} catch (InterruptedException e1) {
						logger.error("Error while sleeping...", e1);
					}
				}
				
				CopyOnWriteArraySet<String> groupNodes = ((ClusterAdministrationImpl)nodeManagement.
						getConnectionManager().getClusterAdmimnistration()).getGroupNodes();
				
				// instance the gossip probability with the maximum
				double gossipProbability = 1;
				
				// create the random generator for the gossip probability
				Random rand = new Random();
				
				// TODO maybe the order of the nodes in the groupNodes list should be altered so I don't work against the first one always
				
				for(String nodeId : groupNodes){
					if(rand.nextDouble() < gossipProbability){
						logger.debug("Requesting the new messages from node [" + nodeId + "]");
						Iterable<Message> newMessages = nodeManagement.getConnectionManager().getGroupCommunication().getListener().
									getNewMessages(nodeManagement.getLocalNode().getNodeId());
	
						// if no new messages where received, lower the chances of requesting more messages
						if(!newMessages.iterator().hasNext()){
							// lowering the gossip probability
							gossipProbability -= 1/groupNodes.size();
						} else {
							for(Message msg : newMessages){
								// do as if i'm sending me the messages
								if(nodeManagement.getConnectionManager().getGroupCommunication().getListener().onMessageArrive(msg)){
									logger.debug("Added message [" + msg + "]");
								}
							}
						}
					}
				}
			
				try {
					Thread.sleep(messageRequesterSleeptime);
				} catch (InterruptedException e) {
					logger.error("Interrupted while sleeping");
					logger.error("Error message:" + e.getMessage());
				}
				
			} catch (RemoteException e) {
				logger.error("There was an error during the message requesting");
				logger.error("Error message:" + e.getMessage());
			}
		}
	}
	
}
