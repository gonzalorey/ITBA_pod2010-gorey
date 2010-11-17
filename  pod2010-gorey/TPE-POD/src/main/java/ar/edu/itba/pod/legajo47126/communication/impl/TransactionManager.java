package ar.edu.itba.pod.legajo47126.communication.impl;

import java.rmi.RemoteException;

import ar.edu.itba.pod.simul.communication.Transactionable;
import ar.edu.itba.pod.simul.communication.payload.Payload;
import ar.edu.itba.pod.simul.market.Resource;

public class TransactionManager implements Transactionable {
	
	@Override
	public void beginTransaction(String remoteNodeId, long timeout)
	throws RemoteException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void acceptTransaction(String remoteNodeId) throws RemoteException {
		// TODO Auto-generated method stub

	}

	@Override
	public void endTransaction() throws RemoteException {
		// TODO Auto-generated method stub

	}

	@Override
	public void exchange(Resource resource, int amount, String sourceNode,
			String destinationNode) throws RemoteException {
		// TODO Auto-generated method stub

	}

	@Override
	public Payload getPayload() throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void rollback() throws RemoteException {
		// TODO Auto-generated method stub

	}
}
