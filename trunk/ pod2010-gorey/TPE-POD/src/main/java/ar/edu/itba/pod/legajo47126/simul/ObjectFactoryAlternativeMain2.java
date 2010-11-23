package ar.edu.itba.pod.legajo47126.simul;

import java.io.IOException;
import java.net.UnknownHostException;
import java.rmi.RemoteException;

import ar.edu.itba.pod.simul.ObjectFactoryAlternative;

public class ObjectFactoryAlternativeMain2 {
	
	public static void main(String[] args) {
		try {
			ObjectFactoryAlternative obj = new ObjectFactoryAlternativeImpl(args);
			obj.getMarketManager().start();
			obj.connectToGroup("192.168.0.104");
			obj.getSimulationManager().start();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
