package de.uniaugsburg.isse;

import ilog.concert.IloException;
import ilog.cplex.IloCplex.Status;
import ilog.opl.IloOplModel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Scanner;

import de.uniaugsburg.isse.data.PowerplantReader;
import de.uniaugsburg.isse.data.ResidualLoadReader;
import de.uniaugsburg.isse.models.AnalyticalExpressionListener;
import de.uniaugsburg.isse.models.ConstraintRelationshipTranslator;
import de.uniaugsburg.isse.models.ModelSynthesiser;
import de.uniaugsburg.isse.models.data.ConstraintCrawler;
import de.uniaugsburg.isse.models.data.ConstraintSet;
import de.uniaugsburg.isse.models.data.naive.CplexConstraintCrawler;
import de.uniaugsburg.isse.models.data.naive.NaiveOPLOrganisationalTemplateFactory;
import de.uniaugsburg.isse.optimisation.Parameters;
import de.uniaugsburg.isse.solvers.CplexSolver;
import de.uniaugsburg.isse.solvers.handlers.CplexConstraintValueHandler;
import de.uniaugsburg.isse.statistics.ExperimentStatistics;
import de.uniaugsburg.isse.syntax.CplexModelSyntaxProvider;
import de.uniaugsburg.isse.util.ExperimentUtils;

/**
 * Represents a single experiment for model synthesis
 * 
 * @author Alexander Schiendorfer
 * 
 */
public class SingleExperiment {

	public static class ConstraintRelationshipsAnalysisHandler implements AnalyticalExpressionListener {

		@Override
		public void addDexpr(StringBuilder softConstraintBuilder) {
			softConstraintBuilder.append("int childWeights[softConstraints] = ...;\n");
			softConstraintBuilder.append("dexpr int predsPerViolatedConstraint[c in softConstraints] = childWeights[c]*constraintViolated[c];\n");
			softConstraintBuilder.append("dexpr float predsPerViolatedSum = (sum(c in softConstraints) predsPerViolatedConstraint[c]) ;\n");

			softConstraintBuilder.append("dexpr float predsPerViolatedAvg = predsPerViolatedSum / (card(softConstraints) * card(TIMERANGE)) ;\n");
		}

		@Override
		public void addConstraints(StringBuilder sb) {
			sb.append("predsPerViolatedAvg >= 0;\n");
			sb.append("predsPerViolatedSum >= 0;\n");
			sb.append("forall (c in softConstraints) {  predsPerViolatedConstraint[c] >= 0; }\n");

		}
	}

	public static class AnalysisHandler implements CplexConstraintValueHandler {
		private boolean solved;
		private final double[] subOptimal;
		private final double[] violatedConstraints;
		private final double[] violation;
		private final double[] penaltySum;
		private final double[] predsAvg;
		private final int totalConstraints;
		private int index;

		public AnalysisHandler(ConstraintSet constraintSet, int timeHorizon) {
			index = 0;
			// prepare for three optimisation runs
			violatedConstraints = new double[3];
			violation = new double[3];
			penaltySum = new double[3];
			predsAvg = new double[3];
			subOptimal = new double[3];
			totalConstraints = constraintSet.getSoftConstraints().size() * timeHorizon;
		}

		public boolean isSolved() {
			return solved;
		}

		public void setSolved(boolean solved) {
			this.solved = solved;
		}

		@Override
		public void process(IloOplModel model) {
			solved = true;
			try {
				violation[index] = model.getCplex().getValue(model.getElement("violation").asNumExpr());
				penaltySum[index] = model.getCplex().getValue(model.getElement("penaltySum").asNumExpr());
				violatedConstraints[index] = model.getCplex().getValue(model.getElement("sumConstraintViolations").asNumExpr());
				predsAvg[index] = model.getCplex().getValue(model.getElement("predsPerViolatedSum").asNumExpr()) / totalConstraints;
				System.out.println("Penalty: " + penaltySum[index] + " / Violation: " + violation[index] + " / violated constraints: "
						+ violatedConstraints[index] + " / preds avg " + predsAvg[index]);
				if (model.getCplex().getStatus() == Status.Optimal) {
					subOptimal[index] = 0.0;
				} else {
					subOptimal[index] = 1.0;
				}
				++index;
			} catch (IloException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void notifyFail() {
			this.solved = false;
		}

	}

	private Double[] residualLoad;
	public final int timeHorizon = 10;
	private final String residualLoadFile = "data/SchwabenNetzlast_ScenariosMay2012_2011_10.csv";
	private final String biomassFile = "data/schwaben_2012_05_300kw_biomasse.csv";
	private final String gasFile = "data/schwaben_2012_05_100kw_gas.csv";
	private Map<String, Double> pMaxMap;
	private final String organisationalTemplateFile = "unitcommitment.mod";
	private final String synthesisedModel = "generated/synthesised.mod";
	private final String relationShipModel = "generated/relationship_model.mod";
	private final String dataFile = "generated/data.dat";
	private final String dataFileSoftConstraints = "generated/dataSoftConstraints.dat";
	private Map<String, Map<String, String>> initialStates;

	private ExperimentStatistics statistics;

	public ExperimentStatistics getStatistics() {
		return statistics;
	}

	public void setStatistics(ExperimentStatistics statistics) {
		this.statistics = statistics;
	}

	public void run(ExperimentParameters params) {
		pMaxMap = new HashMap<String, Double>();
		RandomManager.initialize(params.getSeed());

		initialStates = new HashMap<String, Map<String, String>>();
		Collection<String> powerPlantModelFiles = setupPowerPlantModelFiles();
		Collection<String> dataFiles = new ArrayList<String>(1);
		dataFiles.add(dataFile);

		// create power plant pool
		for (int runIndex = 0; runIndex < params.getNumberRuns(); ++runIndex) {
			Collection<String> subset = ExperimentUtils.drawSamples(powerPlantModelFiles, params.getNumberOfPlants());
			// find demand for subset and write into organisational template

			ResidualLoadReader rlr = new ResidualLoadReader();
			residualLoad = rlr.readLoad(residualLoadFile);

			// pick a subset of the residual load
			Double[] residualLoadActual = getSubsetResidualLoad(residualLoad, timeHorizon);

			// scale residual load for the selected power plants
			normaliseResidualLoad(residualLoadActual, subset);
			writeDemandFile(residualLoadActual);

			// get synthesised model
			ModelSynthesiser ms = new ModelSynthesiser();
			ms.setFactory(new NaiveOPLOrganisationalTemplateFactory(new File(organisationalTemplateFile)));
			ms.setModelSyntaxProvider(new CplexModelSyntaxProvider());
			Map<String, Map<String, String>> subsetInitialStates = getInitialStates(initialStates, subset);
			ms.setInitialValues(subsetInitialStates);

			String synthesisedModelContent = ms.synthesise(organisationalTemplateFile, subset);
			writeFile(synthesisedModel, synthesisedModelContent);
			// make constraint relationship transformation
			ConstraintRelationshipTranslator crt = new ConstraintRelationshipTranslator();
			crt.setDominanceProperty(params.getDominancePropertyAsEnum());
			crt.addAnalyticalExpressionListener(new ConstraintRelationshipsAnalysisHandler());
			String relationshipModelContent = crt.translate(new File(synthesisedModel));
			writeFile(relationShipModel, relationshipModelContent);

			// get soft constraints for further evaluation
			ConstraintCrawler cc = new CplexConstraintCrawler(new File(relationShipModel));
			ConstraintSet constraintSet = cc.readConstraintSet();
			// write data file with soft constraint weights
			String softConstraintsDataFileContent = addSoftConstraintPredecessorCounts(constraintSet);
			writeFile(dataFileSoftConstraints, softConstraintsDataFileContent);
			dataFiles.add(dataFileSoftConstraints);

			// perform three-way optimisation
			Parameters optParams = new Parameters();
			optParams.setPrimaryObjective("minimize violation;");
			optParams.setSecondaryObjective("minimize penaltySum;");
			optParams.setPrimaryTolerance(params.getDeltaViolation());
			optParams.setSecondaryTolerance(params.getDeltaPenalties());

			ThreeStepParetoOptimization tspo = new ThreeStepParetoOptimization();
			Collection<String> models = tspo.getOptimisationModels(relationShipModel, optParams);
			CplexSolver cs = new CplexSolver();
			cs.setTimeLimit(120); // 2 mins
			// handlers for ourselves
			AnalysisHandler handler = new AnalysisHandler(constraintSet, timeHorizon);
			cs.addConstraintValueHandler(handler);
			tspo.setCs(cs);
			tspo.optimise(models, dataFiles);
			// collect parameters
			if (handler.isSolved()) {
				statistics.addValues(ExperimentStatistics.P, handler.penaltySum);
				statistics.addValues(ExperimentStatistics.V, handler.violation);
				statistics.addValues(ExperimentStatistics.VC, handler.violatedConstraints);
				statistics.addValues(ExperimentStatistics.PRA, handler.predsAvg);
				statistics.addValues(ExperimentStatistics.SO, handler.subOptimal);
				statistics.addValue(ExperimentStatistics.PDIFF, handler.penaltySum[2] / handler.penaltySum[0]);
				statistics.addValue(ExperimentStatistics.VDIFF, handler.violation[2] / handler.violation[0]);

				double suboptimals = 0;
				for (double val : handler.subOptimal) {
					suboptimals += val;
				}
				statistics.subOptimals += Math.round(suboptimals);
			} else {
				statistics.increaseFailCounter();
			}
			dataFiles.remove(dataFileSoftConstraints);
		}
	}

	private Map<String, Map<String, String>> getInitialStates(Map<String, Map<String, String>> initialStates2, Collection<String> subset) {
		HashMap<String, Map<String, String>> newSet = new HashMap<String, Map<String, String>>();
		for (String s : subset) {
			newSet.put(s, initialStates2.get(s));
		}
		return newSet;
	}

	private String addSoftConstraintPredecessorCounts(ConstraintSet cs) {
		StringBuilder sb = new StringBuilder("childWeights = #[\n");
		boolean first = true;
		for (String softConstraint : cs.getSoftConstraints()) {
			if (first)
				first = false;
			else
				sb.append(", \n");
			sb.append(softConstraint + " : " + cs.getPredecessorCount(softConstraint));
		}
		sb.append("]#;");
		return sb.toString();
	}

	private void writeDemandFile(Double[] residualLoadActual) {
		StringBuilder builder = new StringBuilder("LAST_SIMULATION_STEP = " + (timeHorizon - 1));
		builder.append(";\n");
		builder.append("loadCurve = [ ");
		boolean first = true;
		for (int i = 0; i < residualLoadActual.length; ++i) {
			if (first)
				first = false;
			else
				builder.append(", ");
			builder.append(residualLoadActual[i]);
		}
		builder.append("];\n");
		writeFile(dataFile, builder.toString());
	}

	private void writeFile(String fileName, String synthesizedModel) {
		FileWriter fw;
		try {
			fw = new FileWriter(new File(fileName));
			fw.write(synthesizedModel);
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void normaliseResidualLoad(Double[] residualLoadActual, Collection<String> subset) {
		// norming residual load
		double maxProduction = 0;
		for (String selectedPlant : subset) {
			Double Pmax = pMaxMap.get(selectedPlant);
			maxProduction += Pmax;
		}

		double maxLoad = Double.NEGATIVE_INFINITY;
		double minLoad = Double.POSITIVE_INFINITY;

		for (int i = 0; i < residualLoadActual.length; ++i) {
			if (residualLoadActual[i] < minLoad)
				minLoad = residualLoadActual[i];
			if (residualLoadActual[i] > maxLoad)
				maxLoad = residualLoadActual[i];
		}

		if (maxProduction < minLoad) {
			// let max production be 10% greater than maxLoad
			double newMax = maxProduction / 1.1;
			double percMin = minLoad / maxLoad;
			double newMin = newMax * percMin;
			double rangeOrig = maxLoad - minLoad, rangeNew = newMax - newMin;

			for (int i = 0; i < residualLoadActual.length; ++i) {
				residualLoadActual[i] = newMin + rangeNew * ((residualLoadActual[i] - minLoad) / rangeOrig);
			}
			minLoad = newMin;
			maxLoad = newMax;
		}
	}

	private Double[] getSubsetResidualLoad(Double[] residualLoad2, int timeHorizon2) {
		int startIndex = RandomManager.getInt(residualLoad2.length - timeHorizon2 + 1);
		Double[] newArray = new Double[timeHorizon2];
		for (int i = 0; i < timeHorizon2; ++i) {
			newArray[i] = residualLoad2[startIndex + i];
		}
		return newArray;
	}

	/**
	 * Sets up an array of power plant models (based on the typical ones) and stores it
	 * 
	 * @return
	 */
	private Collection<String> setupPowerPlantModelFiles() {
		PowerplantReader pr = new PowerplantReader();
		Collection<Double> biomassMaxima = pr.readPlantNominalCapacities(biomassFile);
		Collection<Double> gasPlantMaxima = pr.readPlantNominalCapacities(gasFile);

		Collection<String> powerPlantModels = new LinkedList<String>();

		// first type A plants, i.e. gas
		String typeAplantTemplate = readFile("a.mod");
		int index = 0;

		for (Double gasMaximum : gasPlantMaxima) {
			double PMinCoeff = RandomManager.getDouble(0.2, 0.3);
			double PMin = gasMaximum * PMinCoeff;
			double rateOfChange = RandomManager.getDouble(0.17, 0.2);
			double rateOfChangePref = RandomManager.getDouble(0.10, 0.16);
			double rateOfChangeOpt = RandomManager.getDouble(0.08, 0.95);

			String writeModel = typeAplantTemplate.replace("%P_MIN%", Double.toString(PMin));
			writeModel = writeModel.replace("%P_MAX%", Double.toString(gasMaximum));
			writeModel = writeModel.replace("%RATE_OF_CHANGE%", Double.toString(rateOfChange));
			writeModel = writeModel.replace("%RaTE_OF_CHANGE_PREF%", Double.toString(rateOfChangePref));
			writeModel = writeModel.replace("%RaTe_OF_CHANGE_OPT%", Double.toString(rateOfChangeOpt));
			String generatedModelFileName = "generated/plant_gas_" + index + ".mod";
			writeFile(generatedModelFileName, writeModel);
			pMaxMap.put(generatedModelFileName, gasMaximum);
			powerPlantModels.add(generatedModelFileName);

			// generate adequate initial state
			// probability for being on .6
			Map<String, String> initialState = new HashMap<String, String>();
			double pOn = 0.8;
			if (RandomManager.getBoolean(pOn)) {
				double pNow = RandomManager.getDouble(PMin, gasMaximum);
				initialState.put("production", Double.toString(pNow));
			} else
				initialState.put("production", "0.0");
			initialStates.put(generatedModelFileName, initialState);
			++index;
		}

		String typeBplantTemplate = readFile("b.mod");
		String typeCplantTemplate = readFile("c.mod");

		for (Double biomassMaximum : biomassMaxima) {
			double PMinCoeff = RandomManager.getDouble(0.2, 0.3);
			double PMin = biomassMaximum * PMinCoeff;
			double rateOfChange = RandomManager.getDouble(0.17, 0.2);
			double rateOfChangePref = RandomManager.getDouble(0.10, 0.15);
			String writeModel = null;
			Map<String, String> initialState = new HashMap<String, String>();

			if (RandomManager.getBoolean(0.5)) {
				// type B
				writeModel = typeBplantTemplate.replace("%P_MIN%", Double.toString(PMin));
				writeModel = writeModel.replace("%P_MAX%", Double.toString(biomassMaximum));
				int minUpTime = RandomManager.getInt(2, 5);
				writeModel = writeModel.replace("%MIN_UP_TIME%", Integer.toString(minUpTime));
				writeModel = writeModel.replace("%FIXED_CHANGE%", Double.toString(biomassMaximum * rateOfChange));

				// numbers try to approximate some fictional desirable properties / not entirely relevant
				double accFactMax = RandomManager.getDouble(.9, .95);
				double accFactMin = RandomManager.getDouble(.55, .6);
				writeModel = writeModel.replace("%ACC_MIN%", Double.toString(biomassMaximum * accFactMin));
				writeModel = writeModel.replace("%ACC_MAX%", Double.toString(biomassMaximum * accFactMax));

				double goodFactMax = RandomManager.getDouble(.83, .88);
				double goodFactMin = RandomManager.getDouble(.62, .68);
				writeModel = writeModel.replace("%GOOD_MIN%", Double.toString(biomassMaximum * goodFactMin));
				writeModel = writeModel.replace("%GOOD_MAX%", Double.toString(biomassMaximum * goodFactMax));

				double optFactMax = RandomManager.getDouble(.78, .82);
				double optFactMin = RandomManager.getDouble(.7, .73);
				writeModel = writeModel.replace("%IDEAL_MIN%", Double.toString(biomassMaximum * optFactMin));
				writeModel = writeModel.replace("%IDEAL_MAX%", Double.toString(biomassMaximum * optFactMax));

				// initial state
				double pOn = 0.8;
				if (RandomManager.getBoolean(pOn)) {
					double pNow = RandomManager.getDouble(PMin, biomassMaximum);
					initialState.put("production", Double.toString(pNow));
					int initialUpTime = RandomManager.getInt(2, 5);
					initialState.put("consRunning", Integer.toString(initialUpTime));
					initialState.put("consStopping", "0");
				} else {
					initialState.put("production", "0.0");
					int initialDownTime = RandomManager.getInt(2, 5);
					initialState.put("consRunning", "0");
					initialState.put("consStopping", Integer.toString(initialDownTime));
				}
			} else {
				// type C
				writeModel = typeCplantTemplate.replace("%P_MIN%", Double.toString(PMin));
				writeModel = writeModel.replace("%P_MAX%", Double.toString(biomassMaximum));
				int minUpTime = RandomManager.getInt(2, 5);
				writeModel = writeModel.replace("%MIN_UP_TIME%", Integer.toString(minUpTime));
				int minDownTime = RandomManager.getInt(2, 5);
				writeModel = writeModel.replace("%MIN_DOWN_TIME%", Integer.toString(minDownTime));

				writeModel = writeModel.replace("%RATE_OF_CHANGE%", Double.toString(rateOfChange));
				writeModel = writeModel.replace("%RATEOF_CHANGE_OPT%", Double.toString(rateOfChangePref));

				int hotTime = RandomManager.getInt(1, 3);
				int coldTime = RandomManager.getInt(4, 6);
				writeModel = writeModel.replace("%HOT_TIME%", Integer.toString(hotTime));
				writeModel = writeModel.replace("%COLD_TIME%", Integer.toString(coldTime));

				// numbers try to approximate some fictional desirable properties / not entirely relevant
				double goodFactMax = RandomManager.getDouble(.83, .88);
				double goodFactMin = RandomManager.getDouble(.62, .68);
				writeModel = writeModel.replace("%GOOD_MIN%", Double.toString(biomassMaximum * goodFactMin));
				writeModel = writeModel.replace("%GOOD_MAX%", Double.toString(biomassMaximum * goodFactMax));

				double optFactMax = RandomManager.getDouble(.78, .82);
				double optFactMin = RandomManager.getDouble(.7, .73);
				writeModel = writeModel.replace("%IDEAL_MIN%", Double.toString(biomassMaximum * optFactMin));
				writeModel = writeModel.replace("%IDEAL_MAX%", Double.toString(biomassMaximum * optFactMax));

				// initial state
				double pOn = 0.8;
				initialState.put("signal", "0");
				if (RandomManager.getBoolean(pOn)) {
					double pNow = RandomManager.getDouble(PMin, biomassMaximum);
					initialState.put("production", Double.toString(pNow));
					int initialUpTime = RandomManager.getInt(2, 5);
					initialState.put("consRunning", Integer.toString(initialUpTime));
					initialState.put("consStopping", "0");
					initialState.put("countdown", "0");
					initialState.put("signal", "0");
					initialState.put("powerPlantState", "3");
				} else {
					initialState.put("production", "0.0");
					int initialDownTime = RandomManager.getInt(2, 5);
					initialState.put("consRunning", "0");
					initialState.put("consStopping", Integer.toString(initialDownTime));
					initialState.put("countdown", "0");
					initialState.put("signal", "0");
					initialState.put("powerPlantState", "0");
				}
			}
			String generatedModelFileName = "generated/plant_bio_" + index + ".mod";
			writeFile(generatedModelFileName, writeModel);
			pMaxMap.put(generatedModelFileName, biomassMaximum);
			powerPlantModels.add(generatedModelFileName);

			initialStates.put(generatedModelFileName, initialState);
			++index;

		}
		return powerPlantModels;
	}

	private String readFile(String string) {
		StringBuilder sb = new StringBuilder();
		Scanner sc = null;
		try {
			sc = new Scanner(new File(string));

			while (sc.hasNextLine()) {
				sb.append(sc.nextLine() + "\n");
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			if (sc != null)
				sc.close();
		}
		return sb.toString();
	}

	public static void main(String[] args) {
		SingleExperiment exp = new SingleExperiment();
		ExperimentStatistics stats = new ExperimentStatistics();
		exp.setStatistics(stats);
		ExperimentParameters params = new ExperimentParameters();
		params.setDeltaPenalties(1.1);
		params.setDeltaViolation(1.1);
		params.setDominanceProperty(0);
		params.setNumberOfPlants(5);
		params.setNumberRuns(5);
		params.setSeed(1337);
		exp.run(params);
		System.out.println(stats.toCsv());
	}

}
