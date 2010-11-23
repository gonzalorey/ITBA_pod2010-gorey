package ar.edu.itba.pod.legajo47126.simul;

import java.io.IOException;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.rmi.RemoteException;

import org.apache.log4j.Logger;

import ar.edu.itba.pod.legajo47126.communication.ClusterAdministrationImpl;
import ar.edu.itba.pod.legajo47126.communication.ConnectionManagerImpl;
import ar.edu.itba.pod.legajo47126.communication.message.MessageFactory;
import ar.edu.itba.pod.legajo47126.node.NodeManagement;
import ar.edu.itba.pod.legajo47126.simul.coordinator.DisconnectionCoordinator;
import ar.edu.itba.pod.simul.ObjectFactoryAlternative;
import ar.edu.itba.pod.simul.communication.ConnectionManager;
import ar.edu.itba.pod.simul.communication.Message;
import ar.edu.itba.pod.simul.market.MarketManager;
import ar.edu.itba.pod.simul.simulation.SimulationManager;

import com.google.common.base.Preconditions;

public class ObjectFactoryAlternativeImpl implements ObjectFactoryAlternative {

	// instance of the log4j logger
	private static Logger logger = Logger.getLogger(ObjectFactoryAlternativeImpl.class);
	
	NodeManagement nodeManagement;
	
	public ObjectFactoryAlternativeImpl(String args[]) throws UnknownHostException, RemoteException, IOException {
		nodeManagement = new NodeManagement(args);
	}
	
	public ObjectFactoryAlternativeImpl() throws UnknownHostException, RemoteException, IOException {
		String[] args = {};
		nodeManagement = new NodeManagement(args);
	}
	
	@Override
	public void connectToGroup(String entryPointId) throws ConnectException {
		Preconditions.checkNotNull(entryPointId, "Null String received");
		Preconditions.checkState(entryPointId.equals(""), "Empty String received");

		try {
			ConnectionManagerImpl.getInstance().getClusterAdmimnistration().connectToGroup(entryPointId);
		} catch (RemoteException e) {
			logger.error("An error ocurred during the group connection");
			logger.error("Error message: " + e.getMessage());
			
			throw new ConnectException(e.getMessage());
		}
	}

	@Override
	public void createGroup() throws ConnectException {
		try {
			ConnectionManagerImpl.getInstance().getClusterAdmimnistration().createGroup();
		} catch (RemoteException e) {
			logger.error("An error ocurred during the group creation");
			logger.error("Error message: " + e.getMessage());
			
			throw new ConnectException(e.getMessage());
		}
	}

	@Override
	public void disconnect() {
		try{
			new DisconnectionCoordinator().run();
			
			// create the DISCONNECT message
			Message message = MessageFactory.DisconnectMessage(NodeManagement.getLocalNode().getNodeId());
			logger.debug("Built message [" + message + "], broadcast it");
			
			// broadcast the messsage
			ConnectionManagerImpl.getInstance().getGroupCommunication().broadcast(message);
			
			// clears the group data
			((ClusterAdministrationImpl)ConnectionManagerImpl.getInstance().getClusterAdmimnistration()).clearGroup();
			
		} catch (RemoteException e) {
			logger.error("There was an error during the disconnection of the node");
			logger.error("Error message:" + e.getMessage());
		}
	}

	@Override
	public ConnectionManager getConnectionManager() {
		try {
			return ConnectionManagerImpl.getInstance();
		} catch (RemoteException e) {
			logger.error("An error ocurred while getting the connection manager");
			logger.error("Error message: " + e.getMessage());
		}
		return null;
	}

	@Override
	public MarketManager getMarketManager() {
		return NodeManagement.getMarketManager();
	}

	@Override
	public SimulationManager getSimulationManager() {
		return NodeManagement.getSimulationManager();
	}

}
