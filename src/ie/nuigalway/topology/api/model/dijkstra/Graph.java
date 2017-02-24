package ie.nuigalway.topology.api.model.dijkstra;

import java.util.List;

public class Graph {

	private List<Node> nodes;
	private List<Edge> edges;

	public Graph(List<Node> n, List<Edge> e) {
		this.nodes = n;
		this.edges = e;
	}

	public List<Node> getNodes() {
		return nodes;
	}

	public List<Edge> getEdges() {
		return edges;
	}
}
