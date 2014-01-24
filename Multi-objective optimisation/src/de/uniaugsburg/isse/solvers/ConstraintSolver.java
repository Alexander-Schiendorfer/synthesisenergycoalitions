package de.uniaugsburg.isse.solvers;

import java.util.Collection;

public interface ConstraintSolver {
	boolean solve(String model, Collection<String> dataFiles);

	double getObjective();
}
