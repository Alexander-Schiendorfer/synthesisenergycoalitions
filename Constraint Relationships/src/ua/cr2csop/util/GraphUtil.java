package ua.cr2csop.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import ua.cr2csop.constraints.Constraint;
import ua.cr2csop.exceptions.CyclicRelationException;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.Graph;

/**
 * Contains utility method that facilitate
 * working with JUNG graphs
 * @author alexander
 *
 */
public class GraphUtil {
	
	/**
	 * Returns true iff an edge from vertex1 to vertex2 in graph g exists
	 * only checks in one direction
	 * @param graph
	 * @param vertex1
	 * @param vertex2
	 * @return
	 */
	public static <V, E> boolean hasEdge(Graph<V, E> graph, V vertex1, V vertex2) {
		if(!(graph.containsVertex(vertex1) && graph.containsVertex(vertex2)))
			return false;
		return (graph.findEdge(vertex1, vertex2) != null);

	}
	
	/**
	 * Util method that produces a copy of the concrete graph
	 * uses reflection to instantiate a new object and adds
	 * all edges from the orginal graph
	 * 
	 * Does not copy the vertices! (yet)
	 * @param graph
	 * @return a deep-copied graph (without copying vertices)
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static <V, E> Graph<V, E> getGraphCopy(Graph<V,E> graph) throws InstantiationException, IllegalAccessException {
		Class<? extends Graph> concreteClass = graph.getClass(); 

		Graph<V,E> copiedGraph = (Graph<V,E>) concreteClass.newInstance();
		
		for(V vertex : graph.getVertices()) {
			copiedGraph.addVertex(vertex);
		}
		
		for(V vertex : copiedGraph.getVertices()) {
			// TODO quadratic runtime -> improve!
			for (V vertex2 : copiedGraph.getVertices()) {
				E edge = graph.findEdge(vertex, vertex2);
				if(edge != null) {					
					copiedGraph.addEdge(edge, vertex, vertex2);
				}
 			}
		}
		
		return copiedGraph;
	}
	
	/**
	 * Returns an inverted graph - i.e. all edges are inverted
	 * (a, b) \in g <=> (b, a) \in g'
	 * @param graph
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <V, E> Graph<V, E> getInvertedGraph(Graph<V,E> graph) throws InstantiationException, IllegalAccessException {
		Class<? extends Graph> concreteClass = graph.getClass(); 
		Graph<V,E> invertedGraph = (Graph<V,E>) concreteClass.newInstance();
		
		for(V vertex : graph.getVertices()) {
			invertedGraph.addVertex(vertex);
		}
		
		for(V vertex : invertedGraph.getVertices()) {
			// TODO quadratic runtime -> improve!
			for (V vertex2 : invertedGraph.getVertices()) {
				E edge = graph.findEdge(vertex, vertex2);
				if(edge != null) {					
					invertedGraph.addEdge(edge, vertex2, vertex);
				}
 			}
		}
		return invertedGraph;
	}
	
	/**
	 * Performs a topological sorting of the graph described by the lists of constraints and relationships. If the graph
	 * contains a cycle, returns a non-empty set of constraints whose relationships are responsible for the cycle.
	 * 
	 * @param constraintList
	 *            a list of constraints in the graph
	 * @param relationList
	 *            a list of relationships between the constraints
	 * @return a non-empty set of constraints if a cycle exists, <code>null</code> otherwise
	 */
	@SuppressWarnings("unchecked")
	public static  <V, E>  Set<V> topologicalSort(Graph<V, E> graph) {
		Graph<V, E> h;
		try {
			h = (Graph<V, E>) GraphUtil.getGraphCopy(graph);
		} catch (InstantiationException e) {
			throw new RuntimeException("Could not generate a copy of graph.", e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Could not generate a copy of graph.", e);
		}
		
		Collection<V> sorted = new ArrayList<V>(); // contains sorted vertices
		Queue<V> candidates = new LinkedList<V>(); // contains candidate vertices to be added to the
		// sorted vertices

		Collection<E> edges = new ArrayList<E>(h.getEdges()); // a local, modifiable copy of the edgesList

		// add vertices that have no incoming edges to the candidate list
		for (V vertex : h.getVertices()) {
			if (h.getInEdges(vertex).isEmpty()) {
				candidates.add(vertex);
			}
		}
		/*
		 * while there are vertices in the candidate list
		 * remove the first vertex from the candidate list
		 * remove all of its outgoing edges
		 * remove the edges from the edge list
		 * if this gets new vertices without outgoing edges add them to the candidate list
		 * 
		 * if there are still edges in the edge list at the end the graph contained a circle.
		 * no topological sorting is possible -> return false 
		 */

		while (!candidates.isEmpty()) {

			V v = candidates.poll();
			sorted.add(v);

			// /Collection<Integer> outEdges = h.getOutEdges(c);
			Queue<E> outEdges = new LinkedList<E>(h.getOutEdges(v));
			ArrayList<V> neighbors = new ArrayList<V>();
			for (E edge : outEdges) {
				edges.remove(edge);
				neighbors.add(h.getOpposite(v, edge));
				h.removeEdge(edge);
			}

			for (V neighbor : neighbors) {
				if (h.getInEdges(neighbor).isEmpty())
					candidates.add(neighbor);
			}
		}
		
		// TODO does not deliver the correct result (Set is empty even if there are circles)
		if(edges.isEmpty()) {
			System.out.println("No circle");
		} else 
			System.out.println("circle");

		return new HashSet<V>(candidates);
	}

	/**
	 * Builds a helper data structure to browse faster 
	 * through the neighbors of a graph 
	 * @param invertedGraph
	 * @return a map of lists of vertices for a given vertex
	 */
	public static <V,E> Map<V, List<V>> getNeighborStructure(
			Graph<V, E> invertedGraph) {
		Map<V, List<V>> map = new HashMap<V, List<V>>();
		
		for(V v : invertedGraph.getVertices()) {
			ArrayList<V> neighbors = new ArrayList<V>();
			for(V v2 : invertedGraph.getVertices()) {
				if(invertedGraph.findEdge(v, v2) != null) {
					neighbors.add(v2);
				}
			}
			map.put(v, neighbors);
		}
		
		return map;
	}
}
