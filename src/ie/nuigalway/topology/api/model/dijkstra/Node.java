package ie.nuigalway.topology.api.model.dijkstra;

public class Node {

	final private String id;
	final private String label;
	final private String type;

	public Node(String i, String l, String t) {
		this.id = i;
		this.label = l;
		this.type = t;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return label;
	}

	public String getType() {
		return type;
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
