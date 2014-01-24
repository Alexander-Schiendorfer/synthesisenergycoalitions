package ua.cr2csop.weights.concrete;

import ua.cr2csop.constraints.Constraint;
import ua.cr2csop.weights.InvertedGraphWeightingFunction;
import edu.uci.ics.jung.graph.DirectedGraph;

/**
 * Implements a transitive predecessor dominance weighting function that asserts
 * that nodes weigh more than the sum of the weights of their direct successors.
 * <p>
 * w_i = 1 + \sum_{k \in K_{i}} w_{k}
 * </p>
 * 
 * @author Alexander Schiendorfer
 * 
 */
public class DirectPredecessorDominanceWeightingFunction implements InvertedGraphWeightingFunction {

    @Override
    public int calculateWeight(DirectedGraph<Constraint, Integer> invertedGraph, Constraint expandedNode) {
        int sumWeight = 1;

        for (Constraint c : invertedGraph.getVertices()) {
            if (invertedGraph.findEdge(c, expandedNode) != null) {
                sumWeight += c.getWeight();
            }
        }
        System.out.println("Calculated weight of " + sumWeight + " for constraint \"" + expandedNode.getName() + "\".");
        return sumWeight;
    }

}
