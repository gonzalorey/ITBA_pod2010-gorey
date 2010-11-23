package ar.edu.itba.pod.legajo47126.exceptions;

import java.rmi.RemoteException;

public class NoConnectionAvailableException extends RemoteException{

	public NoConnectionAvailableException(String string) {
		super(string);
	}
	
	public NoConnectionAvailableException() {
		super();
	}

	/**
	 * default serial version ID
	 */
	private static final long serialVersionUID = 1L;
}
