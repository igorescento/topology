package ie.nuigalway.topology.api.model.dijkstra;

public class Edge  {

	private final String id;
	private final Node source;
	private final Node destination;
	private final int metric;
	private final String type;

	public Edge(String i, Node s, Node d, int m, String t) {
		this.id = i;
		this.source = s;
		this.destination = d;
		this.metric = m;
		this.type = t;
	}

	public String getId() {
		return id;
	}
	public Node getDestination() {
		return destination;
	}

	public Node getSource() {
		return source;
	}
	
	public int getMetric() {
		return metric;
	}
	
	public String getType() {
		return type;
	}

	@Override
	public String toString() {
		return source.getName() + " " + destination.getName();
	}
}
