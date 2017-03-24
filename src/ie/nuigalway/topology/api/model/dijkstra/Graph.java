package ie.nuigalway.topology.api.model.dijkstra;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Graph {

	@JsonProperty
	private List<Node> nodes;
	
	@JsonProperty
	private List<Edge> edges;

	public Graph(List<Node> n, List<Edge> e) {
		this.nodes = n;
		this.edges = e;
	}
	
	public Graph() {};
	
	public List<Node> getNodes() {
		return nodes;
	}

	public List<Edge> getEdges() {
		return edges;
	}
	
	public void setNodes(List<Node> nodes) {
		this.nodes = nodes;
	}

	public void setEdges(List<Edge> edges) {
		this.edges = edges;
	}
}
