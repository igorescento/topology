package ie.nuigalway.topology.api.model.dijkstra;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Node {

	@JsonProperty
	private String id;
	
	@JsonProperty
	private String label;
	
	@JsonProperty
	private String type;
	
	@JsonProperty
	private String interf;

	public Node(String i, String l, String t) {
		this.id = i;
		this.label = l;
		this.type = t;
	}
	
	public Node(){};

	public String getId() {
		return id;
	}
	
	public String getLabel() {
		return label;
	}

	public String getType() {
		return type;
	}


	public String getInterf() {
		return interf;
	}
	
	public void setInterf(String a) {
		interf = a;
	}

	@Override
	public int hashCode() {
		final int num = 31;
		int res = 1;
		res = num * res + ((id == null) ? 0 : id.hashCode());
		return res;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Node n = (Node) obj;
		if (id == null) {
			if (n.id != null)
				return false;
		} else if (!id.equals(n.id))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return label + " " + type;
	}
}
