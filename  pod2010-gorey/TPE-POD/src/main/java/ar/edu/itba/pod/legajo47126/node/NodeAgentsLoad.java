package ar.edu.itba.pod.legajo47126.node;

import java.util.concurrent.ConcurrentHashMap;

public class NodeAgentsLoad {

	// load of every known node
	private ConcurrentHashMap<String, Integer> nodesLoad;
	
	// total load from every agent 
	private int totalLoad;
	
	public NodeAgentsLoad() {
		reset();
	}
	
	public void reset(){
		nodesLoad = new ConcurrentHashMap<String, Integer>();
		totalLoad = 0;
	}
	
	public ConcurrentHashMap<String, Integer> getNodesLoad() {
		return nodesLoad;
	}
	
	public int getTotalLoad() {
		return totalLoad;
	}
	
	public void setNodeLoad(String nodeId, int load){
		if(nodesLoad.contains(nodeId)){
			// rest the old load
			totalLoad -= nodesLoad.get(nodeId);
			
			// replace it in the nodes load map
			nodesLoad.replace(nodeId, load);
			
			// add it to the total load
			totalLoad += load;
		} else {
			nodesLoad.put(nodeId, load);
			totalLoad += load;
		}
		
	} 
}
