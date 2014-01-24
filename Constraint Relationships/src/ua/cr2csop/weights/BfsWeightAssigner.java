package ua.cr2csop.weights;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import ua.cr2csop.constraints.Constraint;
import ua.cr2csop.graph.DirectedConstraintGraph;
import ua.cr2csop.util.GraphUtil;
import edu.uci.ics.jung.graph.DirectedGraph;

/**
 * Provides general functionality for weight assignment operating with
 * breadth-first search.
 * 
 * Uses breadth-first search from an auxiliary root node (pointing to the leafs
 * of the original graph) to the most important constraints by first inverting
 * the constraint graph
 * 
 * @author Alexander Schiendorfer
 */
public class BfsWeightAssigner extends WeightAssigner {

    private final InvertedGraphWeightingFunction weightingFunction;

    public BfsWeightAssigner(InvertedGraphWeightingFunction weightingFunction) {
        this.weightingFunction = weightingFunction;
    }

    @Override
    public void assignWeightsInternally(DirectedConstraintGraph dcg) {
        DirectedGraph<Constraint, Integer> graph = dcg.getUnderlyingGraph();
        DirectedGraph<Constraint, Integer> invertedGraph;

        // Reset weights for all constraints in the graph
        for (Constraint c : graph.getVertices()) {
            c.setWeight(1);
        }

        // 1. invert graph
        try {
            invertedGraph = (DirectedGraph<Constraint, Integer>) GraphUtil.getInvertedGraph(graph);
        } catch (InstantiationException e) {
            throw new RuntimeException("Error at creating inverted graph", e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Error at creating inverted graph", e);
        }

        // insert auxiliary start node for BFS
        Constraint startNode = new Constraint("root", 0);
        insertRootNode(invertedGraph, startNode);

        // perform breadth first search to assign weights
        breadthFirstSearch(invertedGraph, startNode);
    }

    private void breadthFirstSearch(DirectedGraph<Constraint, Integer> invertedGraph, Constraint startNode) {
        Queue<Constraint> frontier = new LinkedList<Constraint>();
        Constraint expandedNode = null;

        frontier.add(startNode);
        // helper data structure for more efficient neighbor access
        Map<Constraint, List<Constraint>> neighborStructure = GraphUtil.getNeighborStructure(invertedGraph);

        while (!frontier.isEmpty()) {
            expandedNode = frontier.poll();

            // add predecessors to frontier (= more important nodes)
            for (Constraint predecessor : neighborStructure.get(expandedNode)) {
                frontier.add(predecessor);
            }

            if (expandedNode == startNode) // root node does not need to get
                                           // summarized values
                continue;

            int sumWeight = weightingFunction.calculateWeight(invertedGraph, expandedNode);
            expandedNode.setWeight(Math.max(expandedNode.getWeight(), sumWeight));
        }

    }

    /**
     * Inserts the root node as auxiliary starting point for breadth first
     * search towards most important nodes in dcg - operates on already inverted
     * graph package visibility for testing
     * 
     * @param invertedGraph
     * @param startNode
     */
    void insertRootNode(DirectedGraph<Constraint, Integer> invertedGraph, Constraint startNode) {
        Collection<Constraint> leaves = new ArrayList<Constraint>();

        // collect all Constraints that have no incoming edges (in the already
        // inverted graph)
        for (Constraint c : invertedGraph.getVertices()) {
            if (invertedGraph.inDegree(c) == 0)
                leaves.add(c);
        }

        invertedGraph.addVertex(startNode);
        for (Constraint c : leaves) {
            invertedGraph.addEdge(invertedGraph.getEdgeCount(), startNode, c);
        }
    }

}
