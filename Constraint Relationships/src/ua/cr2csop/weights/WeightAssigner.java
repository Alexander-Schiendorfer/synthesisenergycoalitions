package ua.cr2csop.weights;

import java.util.Set;

import ua.cr2csop.constraints.Constraint;
import ua.cr2csop.exceptions.CyclicRelationException;
import ua.cr2csop.graph.DirectedConstraintGraph;
import ua.cr2csop.util.GraphUtil;

/**
 * Abstract class that defines methods
 * for possible strategies/algorithms 
 * to assign weights to a constraint relation
 * graph
 * @author Alexander Schiendorfer
 *
 */
public abstract class WeightAssigner {
	
	/**
	 * Determines if the given constraint relation graph
	 * is defined in a consistent way i.e. there are no cycles
	 * otherwise no weight assignment possible
	 * 
	 * Subclassing weight assignment algorithms may change or extend these 
	 * consistency conditions
	 * @param dcg the current constraint graph
	 * @return if dcg contains no cycles - else throws exception
	 * @throws CyclicRelationException if dcg contains cycles -> contains culprit constraints
	 */
	protected void checkConsistency(DirectedConstraintGraph dcg) throws CyclicRelationException {
		Set<Constraint> violatingCandidates = GraphUtil.topologicalSort(dcg.getUnderlyingGraph());
		if(!violatingCandidates.isEmpty()) {
			throw new CyclicRelationException(violatingCandidates);
		}
	}
	
	public void assignWeights(DirectedConstraintGraph dcg) {
		checkConsistency(dcg);
		assignWeightsInternally(dcg);
	}
	
	/**
	 * Gets a directed constraint graph and 
	 * assigns weights such that the DCG can then
	 * be used in a weighted CSP
	 * @param dcg
	 */
	protected abstract void assignWeightsInternally(DirectedConstraintGraph dcg); 
}
