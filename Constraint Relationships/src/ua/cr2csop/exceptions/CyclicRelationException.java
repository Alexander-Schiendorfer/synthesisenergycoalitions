package ua.cr2csop.exceptions;

import java.util.Collection;

import ua.cr2csop.constraints.Constraint;

/**
 * Indicates a cyclic relationship between constraints in a constraint relationship graph.
 * 
 * @author Jan-Philipp Stegh�fer
 */
public class CyclicRelationException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 825287877491486356L;
	
	private Collection<Constraint> candidates;

	/**
	 * Gets the constraints that potentially cause the cycle in the graph.
	 * 
	 * @return the constraints potentially causing the cycle
	 */
	public Collection<Constraint> getCandidates() {
		return candidates;
	}

	/**
	 * Sets the constraints that potentially cause the cycle in the graph.
	 * 
	 * @param candidates
	 *            the constraints that potentially cause the cycle in the graph.
	 */
	public void setCandidates(Collection<Constraint> candidates) {
		this.candidates = candidates;
	}

	/**
	 * Creates a new {@link CyclicRelationException} and assigns a list of constraints as candidates for the cycle.
	 * 
	 * @param candidates
	 *            the constraints that potentially cause the cycle in the graph
	 */
	public CyclicRelationException(Collection<Constraint> candidates) {
		super();
		this.candidates = candidates;
	}
	

}
