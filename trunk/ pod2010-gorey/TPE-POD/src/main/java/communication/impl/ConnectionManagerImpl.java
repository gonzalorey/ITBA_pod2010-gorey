package communication.impl;

import java.rmi.RemoteException;

import ar.edu.itba.pod.simul.communication.ClusterAdministration;
import ar.edu.itba.pod.simul.communication.ClusterCommunication;
import ar.edu.itba.pod.simul.communication.ConnectionManager;
import ar.edu.itba.pod.simul.communication.SimulationCommunication;
import ar.edu.itba.pod.simul.communication.ThreePhaseCommit;
import ar.edu.itba.pod.simul.communication.Transactionable;

public class ConnectionManagerImpl implements ConnectionManager {

	@Override
	public ClusterAdministration getClusterAdmimnistration()
			throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getClusterPort() throws RemoteException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public ConnectionManager getConnectionManager(String arg0)
			throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ClusterCommunication getGroupCommunication() throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Transactionable getNodeCommunication() throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SimulationCommunication getSimulationCommunication()
			throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ThreePhaseCommit getThreePhaseCommit() throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

}
