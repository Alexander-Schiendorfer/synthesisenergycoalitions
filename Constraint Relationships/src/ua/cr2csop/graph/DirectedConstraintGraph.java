package ua.cr2csop.graph;

import java.util.HashMap;
import java.util.Map;

import ua.cr2csop.constraints.Constraint;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseGraph;

/**
 * Contains the model of the constraint graph
 * 
 * @author alexander
 * 
 */
public class DirectedConstraintGraph {

	private Map<String, Constraint> allConstraints;
	private DirectedGraph<Constraint, Integer> directedGraph;

	public DirectedConstraintGraph() {
		allConstraints = new HashMap<String, Constraint>();
		directedGraph = new DirectedSparseGraph<Constraint, Integer>();
	}

	/**
	 * Offers access to the constraints of the DCG if the wanted item is not in
	 * the graph a new node gets allocated automatically
	 * 
	 * @param key
	 * @return
	 */
	public Constraint lookupOrAdd(String key) {
		if (!allConstraints.containsKey(key)) {
			allConstraints.put(key, new Constraint(key));
		}

		return allConstraints.get(key);
	}

	public boolean contains(String key) {
		return allConstraints.containsKey(key);
	}

	/**
	 * Adds an edge of two constraints to the underlying graph:
	 * predecessor>>successor
	 * 
	 * @param predecessor
	 * @param successor
	 */
	public void addEdge(Constraint predecessor, Constraint successor) {
		System.out.println("adding " + predecessor + " - " + successor);
		directedGraph.addEdge(directedGraph.getEdgeCount(), predecessor,
				successor, edu.uci.ics.jung.graph.util.EdgeType.DIRECTED);
	}

	public DirectedGraph<Constraint, Integer> getUnderlyingGraph() {
		return directedGraph;
	}
}
