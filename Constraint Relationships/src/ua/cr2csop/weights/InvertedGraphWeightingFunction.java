package ua.cr2csop.weights;

import ua.cr2csop.constraints.Constraint;
import edu.uci.ics.jung.graph.DirectedGraph;

/**
 * Interface for weighting functions operating on inverted constraint graphs.
 * 
 * @author Jan-Philipp Steghöfer
 */
public interface InvertedGraphWeightingFunction {

    /**
     * Calculates the weight of the provided node.
     * 
     * @param invertedGraph
     *            the inverted constraint graph to work on
     * @param expandedNode
     *            the node currently under investigation
     * @return the weight of the expanded node
     */
    public int calculateWeight(DirectedGraph<Constraint, Integer> invertedGraph, Constraint expandedNode);
}
