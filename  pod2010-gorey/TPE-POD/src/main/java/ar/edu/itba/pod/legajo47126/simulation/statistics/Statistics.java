package ar.edu.itba.pod.legajo47126.simulation.statistics;

import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import ar.edu.itba.pod.simul.communication.TransferHistory;

public class Statistics {
	
	// instance of the log4j logger
	private static Logger logger = Logger.getLogger(Statistics.class);
	
	private static Statistics statistics;
	
	private ConcurrentHashMap<String, Double> statisticsMap;
	
	private Statistics() {
		statisticsMap = new ConcurrentHashMap<String, Double>();
	}
	
	public static Statistics getInstance(){
		if(statistics == null)
			statistics = new Statistics();
		
		return statistics;
	}
	
	public ConcurrentHashMap<String, Double> getStatistics() {
		return statisticsMap;
	}
	
	public void addStatistics(String nodeId, TransferHistory history){
		if(statisticsMap.containsKey(nodeId))
			statisticsMap.replace(nodeId, history.getTransactionsPerSecond());
		else
			statisticsMap.put(nodeId, history.getTransactionsPerSecond());
	}
	
	public void printOnScreen(){
		for(String nodeId : statisticsMap.keySet()){
			System.out.println("Node [" + nodeId + "] had [" + statisticsMap.get(nodeId) + "] trans/sec");
		}
	}
	
	public void saveToFile(){
		for(String nodeId : statisticsMap.keySet()){
			logger.fatal("[STATISTICS] Node [" + nodeId + "] had [" + statisticsMap.get(nodeId) + "] trans/sec");
		}
	}
}
