package ar.edu.itba.pod.legajo47126.simul;

import java.net.ConnectException;
import java.rmi.RemoteException;

import org.apache.log4j.Logger;

import ar.edu.itba.pod.legajo47126.communication.ConnectionManagerImpl;
import ar.edu.itba.pod.simul.ObjectFactoryAlternative;
import ar.edu.itba.pod.simul.communication.ConnectionManager;
import ar.edu.itba.pod.simul.market.MarketManager;
import ar.edu.itba.pod.simul.simulation.SimulationManager;

import com.google.common.base.Preconditions;

public class ObjectFactoryAlternativeImpl implements ObjectFactoryAlternative {

	// instance of the log4j logger
	private static Logger logger = Logger.getLogger(ObjectFactoryAlternativeImpl.class);
	
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
//		ConnectionManagerImpl.getInstance().getClusterAdmimnistration().disconnectFromGroup()

	}

	@Override
	public ConnectionManager getConnectionManager() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MarketManager getMarketManager() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SimulationManager getSimulationManager() {
		// TODO Auto-generated method stub
		return null;
	}

}
