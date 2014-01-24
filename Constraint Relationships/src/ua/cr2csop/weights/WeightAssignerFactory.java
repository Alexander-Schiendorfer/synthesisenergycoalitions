package ua.cr2csop.weights;

import ua.cr2csop.weights.concrete.TransitivePredecessorDominanceWeightingFunction;

/**
 * Factory class to manage different weight assigners
 * 
 * @author Alexander Schiendorfer
 * 
 */
public class WeightAssignerFactory {

    /**
     * Returns a {@link BfsWeightAssigner} using a
     * {@link TransitivePredecessorDominanceWeightingFunction}.
     * 
     * @returna a breadth-first weight assigner using a
     *          transitive-predecessor-dominance weighting function
     */
    public static WeightAssigner getWeightAssigner() {
        // Temporarily only BFS-total dominance weight assigner
        return new BfsWeightAssigner(new TransitivePredecessorDominanceWeightingFunction());
    }

    /**
     * Returns a {@link BfsWeightAssigner} using the provided weighting
     * function. The weighting function needs to implement
     * {@link InvertedGraphWeightingFunction} and thus work on inverted graphs
     * used by the BFS.
     * 
     * @param weightingFunction
     *            the weighting function to be used.
     * @return a breadth-first weight assigner using the provided weighting
     *         function
     */
    public static WeightAssigner getWeightAssigner(InvertedGraphWeightingFunction weightingFunction) {
        return new BfsWeightAssigner(weightingFunction);
    }
}
