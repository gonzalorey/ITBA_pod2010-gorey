package ar.edu.itba.pod.legajo47126.simulation;

import java.util.concurrent.ConcurrentHashMap;

import ar.edu.itba.pod.simul.communication.TransferHistory;

public class Statistics {
	private static ConcurrentHashMap<String, Double> statistics;
	
	public static void getStatistics() {
		statistics = new ConcurrentHashMap<String, Double>();
	}
	
	public static void setStatistics(String nodeId, TransferHistory history){
		statistics.put(nodeId, history.getTransactionsPerSecond());
	}
}
