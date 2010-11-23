package ar.edu.itba.pod.legajo47126.simulation;

import java.util.concurrent.ConcurrentLinkedQueue;

import ar.edu.itba.pod.simul.communication.NodeAgentLoad;

public class NodeKnownAgentsLoad {

	// agents load of every known node
	private ConcurrentLinkedQueue<NodeAgentLoad> nodeAgentLoadList;
	
	// total load from every agent 
	private int totalLoad;
	
	public NodeKnownAgentsLoad() {
		reset();
	}
	
	public void reset(){
		nodeAgentLoadList = new ConcurrentLinkedQueue<NodeAgentLoad>();
		totalLoad = 0;
	}
	
	public ConcurrentLinkedQueue<NodeAgentLoad> getNodesLoad() {
		return nodeAgentLoadList;
	}
	
	public int getTotalLoad() {
		return totalLoad;
	}
	
	public void setNodeLoad(String nodeId, int load){
		
		boolean replaced = false;
		for(NodeAgentLoad nodeAgentLoad : nodeAgentLoadList){
			if(nodeAgentLoad.getNodeId().equals(nodeId)){
				nodeAgentLoad.setNumberOfAgents(load);
				totalLoad = totalLoad - nodeAgentLoad.getNumberOfAgents() + load;
				replaced = true;
			}
		}
		
		if(!replaced){
			nodeAgentLoadList.add(new NodeAgentLoad(nodeId, load));
			totalLoad += load;
		}
		
	}
}
