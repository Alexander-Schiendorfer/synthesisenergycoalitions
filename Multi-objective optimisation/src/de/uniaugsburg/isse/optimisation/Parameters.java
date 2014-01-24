package de.uniaugsburg.isse.optimisation;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class holds all parameters for the three step optimisation process in particular
 * 
 * @author alexander
 * 
 */
public class Parameters {

	// CPLEX formulated primary objective, (use decision expressions: minimize violation;)
	private String primaryObjective;
	Pattern MY_PATTERN = Pattern.compile("(minimize|maximize)(.*?)\\;", Pattern.DOTALL);

	public String getPrimaryObjective() {
		return primaryObjective;
	}

	public void setPrimaryObjective(String primaryObjective) {
		this.primaryObjective = primaryObjective;
	}

	public String getSecondaryObjective() {
		return secondaryObjective;
	}

	public void setSecondaryObjective(String secondaryObjective) {
		this.secondaryObjective = secondaryObjective;
	}

	public double getPrimaryTolerance() {
		return primaryTolerance;
	}

	public void setPrimaryTolerance(double primaryTolerance) {
		this.primaryTolerance = primaryTolerance;
	}

	public double getSecondaryTolerance() {
		return secondaryTolerance;
	}

	public void setSecondaryTolerance(double secondaryTolerance) {
		this.secondaryTolerance = secondaryTolerance;
	}

	// CPLEX formulated secondary objective, (like minimize penalty;)
	private String secondaryObjective;
	// use res' = res * primaryTolerance (e.g. 1.1)
	private double primaryTolerance;
	// use res' = res * secondaryTolerance (e.g. 1.0) for penalties
	private double secondaryTolerance;

	public String extractPrimaryExpression() {

		Matcher m = MY_PATTERN.matcher(primaryObjective);
		if (m.find())
			return m.group(2);
		return null;
	}

	public String extractSecondaryExpression() {
		Matcher m = MY_PATTERN.matcher(secondaryObjective);
		if (m.find())
			return m.group(2);
		return null;
	}
}
