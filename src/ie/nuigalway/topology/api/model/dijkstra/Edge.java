package ie.nuigalway.topology.api.model.dijkstra;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Edge {

	@JsonProperty
	private String id;
	
	@JsonProperty
	private Node source;
	
	@JsonProperty
	private Node destination;
	
	@JsonProperty
	private int metric;
	
	@JsonProperty
	private String type;
	
	@JsonProperty
	private String mask;
	
	public Edge(String i, Node s, Node d, int m, String t, String mask) {
		this.id = i;
		this.source = s;
		this.destination = d;
		this.metric = m;
		this.type = t;
		this.mask = mask;
	}
	
	public Edge() {};
	
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

	public String getMask() {
		return mask;
	}
	
	@Override
	public String toString() {
		return source.getLabel() + " " + destination.getLabel();
	}
}
