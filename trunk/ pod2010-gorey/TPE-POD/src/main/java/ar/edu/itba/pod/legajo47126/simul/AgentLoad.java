package ar.edu.itba.pod.legajo47126.simul;


public class AgentLoad implements Comparable<AgentLoad>{

	private String nodeId;
	
	private Integer load;
	
	public AgentLoad(String nodeId, int load) {
		this.nodeId = nodeId;
		this.load = load;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		
		if (obj == null)
			return false;
		
		if (getClass() != obj.getClass())
			return false;
		
		AgentLoad other = (AgentLoad) obj;
		
		if(!other.nodeId.equals(nodeId))
			return false;
		
		return true;
	}

	@Override
	public int compareTo(AgentLoad other) {
		if(nodeId.compareTo(other.nodeId) == 0)
			return load.compareTo(other.load);
		return nodeId.compareTo(other.nodeId);
	}
}
