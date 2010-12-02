package ar.edu.itba.pod.legajo47126.simul;

import java.io.IOException;
import java.net.UnknownHostException;
import java.rmi.RemoteException;

import org.apache.log4j.Logger;

import ar.edu.itba.pod.legajo47126.node.NodeConsole;
import ar.edu.itba.pod.simul.ObjectFactoryAlternative;

public class ObjectFactoryAlternativeMain {
	
	// instance of the log4j logger
	private static Logger logger = Logger.getLogger(ObjectFactoryAlternativeMain.class);
	
	public static void main(String[] args) {
		try {
//			try {
//				//Setting security manager
//				SecurityManager sm = new SecurityManager();
//				System.setSecurityManager(sm);
//			} catch (SecurityException e) {
//				System.err.println("Error: could not set security manager: " + e);
//			}
			
			ObjectFactoryAlternative ofa = new ObjectFactoryAlternativeImpl(args);
			
			new NodeConsole().runConsole(ofa);

			logger.warn("Calling a local method (cast needed)");
			((ObjectFactoryAlternativeImpl) ofa).closeObject();
			
		} catch (UnknownHostException e) {
			logger.error("Unknown host, aborting...", e);
		} catch (RemoteException e) {
			logger.error("Remote exception triggered, aborting...", e);
		} catch (IOException e) {
			logger.error("IO exception triggered, aborting...", e);
		}
		
		System.out.println("Bye!");
	}
}
