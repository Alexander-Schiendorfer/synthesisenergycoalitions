package de.uniaugsburg.isse;

import java.util.Properties;

import de.uniaugsburg.isse.models.ConstraintRelationshipTranslator.DominanceProperty;

public class ExperimentParameters {
	private int numberOfPlants;
	private double deltaViolation;

	public int getNumberOfPlants() {
		return numberOfPlants;
	}

	public void setNumberOfPlants(int numberOfPlants) {
		this.numberOfPlants = numberOfPlants;
	}

	public double getDeltaViolation() {
		return deltaViolation;
	}

	public void setDeltaViolation(double deltaViolation) {
		this.deltaViolation = deltaViolation;
	}

	public double getDeltaPenalties() {
		return deltaPenalties;
	}

	public void setDeltaPenalties(double deltaPenalties) {
		this.deltaPenalties = deltaPenalties;
	}

	public int getDominanceProperty() {
		return dominanceProperty;
	}

	public void setDominanceProperty(int dominanceProperty) {
		this.dominanceProperty = dominanceProperty;
	}

	public int getNumberRuns() {
		return numberRuns;
	}

	public void setNumberRuns(int numberRuns) {
		this.numberRuns = numberRuns;
	}

	public int getSeed() {
		return seed;
	}

	public void setSeed(int seed) {
		this.seed = seed;
	}

	private double deltaPenalties;
	private int dominanceProperty; // 0 - SPD, 1 - DPD, 2 - TPD

	// technical stuff
	private int numberRuns;
	private int seed;
	private Properties properties;

	public Properties getProperties() {
		return properties;
	}

	public DominanceProperty getDominancePropertyAsEnum() {
		switch (dominanceProperty) {
		case 0:
			return DominanceProperty.SPD;
		case 1:
			return DominanceProperty.DPD;
		case 2:
			return DominanceProperty.TPD;
		}
		return DominanceProperty.SPD;
	}

	public void setProperties(Properties p) {
		this.properties = p;
	}
}
