package ar.edu.itba.pod.simul;

import java.net.ConnectException;

import ar.edu.itba.pod.simul.communication.ConnectionManager;
import ar.edu.itba.pod.simul.market.MarketManager;
import ar.edu.itba.pod.simul.simulation.SimulationManager;

/**
 * Alternative ObjectFactory
 */
public interface ObjectFactoryAlternative {
	
	/**
	 * Creates a group
	 * 
 	 * @throws ConnectionException If there was a problem while trying to create a group
	 */
	public void createGroup() throws ConnectException;
	
	/**
	 * Connects this node to the cluster using the given entry point.
	 *
	 * @param entryPointId The node's id that will be used as an entry point. Must not be null or an empty string.
	 * @throws NullPointerException If the entry point id is null.
	 * @throws IllegalArgumentException If the entry point id is an empty string.
	 * @throws ConnectionException If there was a problem while trying to establish the connection.
	 */
	public void connectToGroup(String entryPointId) throws ConnectException;
	
	/**
	 * Obtains the <code>ConnectionManager</code> of this node.
	 * 
	 * @return This node's <code>ConnectionManager</code>.
	 */
	public ConnectionManager getConnectionManager();
	
	/**
	 * Obtains the market manager.
	 * 
	 * @return A <code>MarketManager</code> object.
	 */
	public MarketManager getMarketManager();

	/**
	 * Obtains the simulation manager
	 * 
	 * @return A <code>SimulationManager</code> object.
	 */
	public SimulationManager getSimulationManager();
	
	/**
	 * Disconnects this node from the cluster.
	 * <p>
	 * It MUST be called before the shutdown method of the
	 * market and the simulation is called.
	 */
	public void disconnect();
}
