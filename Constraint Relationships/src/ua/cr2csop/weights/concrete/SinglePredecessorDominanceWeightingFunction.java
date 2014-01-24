package ua.cr2csop.weights.concrete;

import ua.cr2csop.constraints.Constraint;
import ua.cr2csop.weights.InvertedGraphWeightingFunction;
import edu.uci.ics.jung.graph.DirectedGraph;

/**
 * Implements a transitive predecessor dominance weighting function that asserts
 * that nodes weigh more than the heaviest of their direct successors.
 * <p>
 * w_i = 1 + max_{w}(K_{i})
 * </p>
 * 
 * @author Alexander Schiendorfer
 * 
 */
public class SinglePredecessorDominanceWeightingFunction implements InvertedGraphWeightingFunction {

    @Override
    public int calculateWeight(DirectedGraph<Constraint, Integer> invertedGraph, Constraint expandedNode) {
        int maxWeight = 0;

        for (Constraint c : invertedGraph.getVertices()) {
            if (invertedGraph.findEdge(c, expandedNode) != null) {
                if (maxWeight < c.getWeight()) {
                    maxWeight = c.getWeight();
                }
            }
        }
        System.out.println("Calculated weight of " + (1 + maxWeight) + " for constraint \"" + expandedNode.getName()
                + "\".");
        return 1 + maxWeight;
    }

}
