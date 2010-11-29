package ar.edu.itba.pod.legajo47126.simulation.statistics;

import java.rmi.RemoteException;

import org.apache.log4j.Logger;

import ar.edu.itba.pod.legajo47126.communication.message.MessageFactory;
import ar.edu.itba.pod.legajo47126.node.NodeManagement;
import ar.edu.itba.pod.simul.communication.Message;

public class GetStatistics implements Runnable {

	// instance of the log4j logger
	private static Logger logger = Logger.getLogger(GetStatistics.class);
	
	private NodeManagement nodeManagement;
	
	private long statisticsWaitTime;
	
	public GetStatistics(NodeManagement nodeManagement) {
		this.nodeManagement = nodeManagement;
		
		statisticsWaitTime = nodeManagement.getConfigFile().getProperty("StatisticsWaitTime", 5000);
	}
	
	@Override
	public void run() {
		logger.debug("Send the NODE_MARKET_DATA_REQUEST message to the local node");
		Message message = MessageFactory.NodeMarketDataRequestMessage(nodeManagement.getLocalNode().getNodeId());
		try {
			nodeManagement.getConnectionManager().getGroupCommunication().send(message, nodeManagement.getLocalNode().getNodeId());
		} catch (RemoteException e) {
			logger.error("There was an error while trying to send the message to the local node", e);
		}
		
		logger.debug("Broadcast a NODE_MARKET_DATA_REQUEST message");
		Message broadcastMessage = MessageFactory.NodeMarketDataRequestMessage(nodeManagement.getLocalNode().getNodeId());
		try {
			nodeManagement.getConnectionManager().getGroupCommunication().broadcast(broadcastMessage);
		} catch (RemoteException e) {
			logger.error("There was an error while trying to broadcast the message", e);
			return;
		}
		
		try {
			Thread.sleep(statisticsWaitTime);
		} catch (InterruptedException e) {
			logger.error("There was an error while waiting for the messages to arrive", e);
		}

		nodeManagement.getMarketManager().inspector();
		
		Statistics.getInstance().printOnScreen();
	}
}
