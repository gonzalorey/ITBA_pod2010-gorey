package ar.edu.itba.pod.legajo47126.simulation.statistics;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.concurrent.ConcurrentHashMap;

import ar.edu.itba.pod.simul.communication.TransferHistory;

public class Statistics {
	
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
		if(statisticsMap.contains(nodeId))
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
		
		String pathname = "Statistics.txt";
		File file = new File(pathname);
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		}
		OutputStreamWriter osw = new OutputStreamWriter(fos);
		BufferedWriter bw = new BufferedWriter(osw);
		
		for(String nodeId : statisticsMap.keySet()){
			try {
				bw.write("Node [" + nodeId + "] had [" + statisticsMap.get(nodeId) + "] trans/sec");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
