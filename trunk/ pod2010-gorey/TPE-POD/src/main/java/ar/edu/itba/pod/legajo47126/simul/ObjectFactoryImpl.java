package ar.edu.itba.pod.legajo47126.simul;

import java.io.IOException;
import java.net.UnknownHostException;
import java.rmi.RemoteException;

import org.apache.log4j.Logger;

import ar.edu.itba.pod.legajo47126.communication.ConnectionManagerImpl;
import ar.edu.itba.pod.legajo47126.node.NodeManagement;
import ar.edu.itba.pod.simul.ObjectFactory;
import ar.edu.itba.pod.simul.communication.ConnectionManager;
import ar.edu.itba.pod.simul.market.MarketManager;
import ar.edu.itba.pod.simul.simulation.SimulationManager;
import ar.edu.itba.pod.simul.time.TimeMapper;

public class ObjectFactoryImpl implements ObjectFactory {

	// instance of the log4j logger
	private static Logger logger = Logger.getLogger(NodeManagement.class);
	
	NodeManagement nodeManagement;
	
	@Override
	public ConnectionManager createConnectionManager(String localIp) {
		String[] args = {localIp};
		try {
			nodeManagement = new NodeManagement(args);
			return ConnectionManagerImpl.getInstance();
		} catch (UnknownHostException e) {
			logger.fatal("The local node couldn't be started. Aborting execution");
			logger.fatal("Error message: " + e.getMessage());
		} catch (RemoteException e) {
			logger.fatal("There was an error during the Connection Manager initialization. Aborting execution");
			logger.fatal("Error message: " + e.getMessage());
		} catch (IOException e) {
			logger.error("'node.conf' file not found. Using default configurations");
            logger.error("Error message: " + e.getMessage());
		} catch (Exception e) {
			logger.error("An unexpected exception ocurred");
            logger.error("Error message: " + e.getMessage());
		}
		
		return null;
	}

	@Override
	public ConnectionManager createConnectionManager(String localIp, String groupIp) {
		if(createConnectionManager(localIp) != null){
			try {
				ConnectionManagerImpl.getInstance().getClusterAdmimnistration().connectToGroup(groupIp);
			} catch (RemoteException e) {
				logger.error("An error ocurred while connecting to the group");
				logger.equals("Error message: " + e.getMessage());
			}
		}
		return null;
	}

	@Override
	public MarketManager getMarketManager(ConnectionManager mgr) {
//		return getMarketManager(mgr);
		return null;	// TODO
	}

	@Override
	public SimulationManager getSimulationManager(ConnectionManager mgr, TimeMapper timeMappers) {
//		return nodeManagement.getSimulationManager();
		return null;	// TODO
	}

}
