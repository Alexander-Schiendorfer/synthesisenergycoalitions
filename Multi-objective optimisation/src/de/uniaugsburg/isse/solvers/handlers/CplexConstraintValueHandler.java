package de.uniaugsburg.isse.solvers.handlers;

import ilog.opl.IloOplModel;

public interface CplexConstraintValueHandler {

	/**
	 * Gets notified with a solved model
	 * 
	 * @param model
	 */
	void process(IloOplModel model);

	/**
	 * Is called if the solver failed to provide a solution
	 */
	void notifyFail();
}
