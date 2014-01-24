package de.uniaugsburg.isse.models;

public interface AnalyticalExpressionListener {

	void addDexpr(StringBuilder softConstraintBuilder);

	void addConstraints(StringBuilder sb);

}
