package ie.nuigalway.topology.api.model.dijkstra;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Dijkstra {

	private List<Node> nodes;
	private List<Edge> edges;
	private Set<Node> settled;
	private Set<Node> unSettled;
	private Map<Node, Node> predecessors;
	private Map<Node, Integer> distance;

	public Dijkstra(Graph graph) {

		this.nodes = new ArrayList<Node>(graph.getNodes());
		this.edges = new ArrayList<Edge>(graph.getEdges());
	}

	/**
	 * Method to run / execute Dijkstra algorithm
	 */
	public void run(Node source) {
		settled = new HashSet<Node>();
		unSettled = new HashSet<Node>();
		distance = new HashMap<Node, Integer>();
		predecessors = new HashMap<Node, Node>();
		distance.put(source, 0);
		unSettled.add(source);
		while (unSettled.size() > 0) {
			Node node = getMin(unSettled);
			settled.add(node);
			unSettled.remove(node);
			findMinDistances(node);
		}
	}

	/**
	 * Generate a reduced tree from original graph without redundant edges
	 */
	public Graph getSyncTree(Graph g){

		List<Node> nodes = g.getNodes();
		List<Edge> edges = g.getEdges();

		Iterator<Node> iter = nodes.iterator();
		Iterator<Edge> iterV = edges.iterator();

		//iterate over nodes and check if all present in reduced graph
		while (iter.hasNext()) {
			Node n = iter.next();
			if(!predecessors.containsKey(n) && !predecessors.containsValue(n)){
				iter.remove();
			}
		}

		//iterate over edges and remove redundant ones in both directions
		while (iterV.hasNext()) {
			Edge e = iterV.next();

			if(predecessors.get(e.getSource()) != null){
				if(predecessors.get(e.getSource()).equals(e.getDestination()) || (predecessors.get(e.getDestination()).equals(e.getSource()))) {

				}
				else {
					iterV.remove();
				}
			}
			else {
				System.out.println("NULL POINTER @ ROOT: " + e.getSource().getId());
			}
		}

		Graph newGraph = new Graph(nodes, edges);

		return newGraph;
	}

	/**
	 * Method to find minimal distance for selected node
	 */
	private void findMinDistances(Node n) {
		List<Node> adjacentNodes = getNeighbors(n);
		for (Node dest : adjacentNodes) {
			if (getShortestDistance(dest) > getShortestDistance(n)
					+ getDistance(n, dest)) {
				distance.put(dest, getShortestDistance(n)
						+ getDistance(n, dest));
				predecessors.put(dest, n);
				unSettled.add(dest);
			}
		}
	}

	/**
	 * Method to get the distance between selected nodes
	 */
	private int getDistance(Node n, Node dest) {
		for (Edge edge : edges) {
			if (edge.getSource().equals(n)
					&& edge.getDestination().equals(dest)) {
				return edge.getMetric();
			}
		}
		throw new RuntimeException("Runtime exception.");
	}

	/**
	 * Method to return neighbors
	 */
	private List<Node> getNeighbors(Node n) {
		List<Node> neighbors = new ArrayList<Node>();
		for (Edge e : edges) {
			if (e.getSource().equals(n)
					&& !isSettled(e.getDestination())) {
				neighbors.add(e.getDestination());
			}
		}
		return neighbors;
	}

	/**
	 * Method to get minimum distance
	 */
	private Node getMin(Set<Node> nodes) {

		Node min = null;

		for (Node n : nodes) {
			if (min == null) {
				min = n;
			} else {
				if (getShortestDistance(n) < getShortestDistance(min)) {
					min = n;
				}
			}
		}
		return min;
	}

	/**
	 * Method to check if node is settled
	 */
	private boolean isSettled(Node n) {
		return settled.contains(n);
	}

	/**
	 * Private method to return shortest distance
	 */
	private int getShortestDistance(Node destination) {
		Integer d = distance.get(destination);
		if (d == null) {
			return Integer.MAX_VALUE;
		} else {
			return d;
		}
	}

	/**
	 * Method to return a path to destination node
	 */
	public LinkedList<Node> getPath(Node t) {
		LinkedList<Node> path = new LinkedList<Node>();
		Node inBetween = t;
		// if path exists
		if (predecessors.get(inBetween) == null) {
			return null;
		}
		path.add(inBetween);
		while (predecessors.get(inBetween) != null) {
			inBetween = predecessors.get(inBetween);
			path.add(inBetween);
		}
		// reverse to get the correct order
		Collections.reverse(path);
		return path;
	}
}
