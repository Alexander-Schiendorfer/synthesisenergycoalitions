package de.uniaugsburg.isse;

import ilog.concert.IloException;
import ilog.opl.IloOplModel;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import de.uniaugsburg.isse.optimisation.Parameters;
import de.uniaugsburg.isse.solvers.ConstraintSolver;
import de.uniaugsburg.isse.solvers.CplexSolver;
import de.uniaugsburg.isse.solvers.handlers.CplexConstraintValueHandler;

/**
 * Takes a CPLEX model (e.g., formulated as weighted CSOP), and creates the subsequent models to perform a
 * multi-objective optimisation
 * 
 * @author alexander
 * 
 */
public class ThreeStepParetoOptimization {

	public static class PenaltyHandler implements CplexConstraintValueHandler {

		@Override
		public void process(IloOplModel model) {
			double violation = -1, penaltySum = -1;
			try {
				violation = model.getCplex().getValue(model.getElement("violation").asNumExpr());
				penaltySum = model.getCplex().getValue(model.getElement("penaltySum").asNumExpr());
				double violatedConstraints = model.getCplex().getValue(model.getElement("sumConstraintViolations").asNumExpr());

				System.out.println("Penalty: " + penaltySum + " / Violation: " + violation + " / violated constraints: " + violatedConstraints);
			} catch (IloException e) {
				e.printStackTrace();
			}

		}

		@Override
		public void notifyFail() {
			// TODO Auto-generated method stub

		}

	}

	private ConstraintSolver cs;

	public ConstraintSolver getCs() {
		return cs;
	}

	public void setCs(ConstraintSolver cs) {
		this.cs = cs;
	}

	private final String primaryResultLiteral = "%PRIMARY_RESULT_PREVIOUS%";
	private final String secondaryResultLiteral = "%SECONDARY_RESULT_PREVIOUS%";
	private double primaryObjective;
	private double secondaryObjective;

	public Collection<String> getOptimisationModels(String fileName, Parameters params) {
		// first separate into identifier lines and constraint lines and objective
		List<String> identifierLines = new LinkedList<String>();
		String objective = "";
		List<String> constraintLines = new LinkedList<String>();

		File f = null;
		Scanner sc = null;
		// will contain the CPLEX models
		Collection<String> models = new ArrayList<String>(3);

		try {
			sc = new Scanner((f = new File(fileName)));
			boolean constraintSection = false;
			while (sc.hasNextLine()) {
				String newLine = sc.nextLine();
				if (newLine.contains("minimize") || newLine.contains("maximize")) {
					objective = newLine;
					continue;
				}
				if (newLine.contains("subject to")) {
					constraintSection = true;
					continue;
				}
				if (constraintSection) {
					if (newLine.contains("};")) // terminating string
						break;
					constraintLines.add(newLine);
				} else if (objective.equals("")) // must not have been set yet
					identifierLines.add(newLine);
			}

			// 1. first model is the original one - nothing to do
			StringBuilder builder = new StringBuilder();
			for (String identString : identifierLines) {
				builder.append(identString + "\n");
			}
			builder.append(params.getPrimaryObjective() + "\n");
			builder.append("subject to {\n");
			for (String constraintString : constraintLines) {
				builder.append(constraintString + "\n");
			}
			builder.append("};\n");
			models.add(builder.toString());

			// 2. then we restrict this to be less than x*res
			builder = new StringBuilder();
			for (String identString : identifierLines) {
				builder.append(identString + "\n");
			}
			// now add the artificial expressions
			String toleranceVar = "primary_tolerance";
			String resVar = "primary_result";

			String primaryToleranceExpression = "float " + toleranceVar + " = " + params.getPrimaryTolerance() + ";\n" + "float " + resVar + " = "
					+ primaryResultLiteral + ";\n";
			builder.append(primaryToleranceExpression);

			builder.append(params.getSecondaryObjective() + "\n");
			builder.append("subject to {\n");
			String compareSign = "<=";
			if (params.getPrimaryTolerance() < 1) // minimization problem
				compareSign = ">=";
			String primaryConstraint = params.extractPrimaryExpression() + " " + compareSign + " " + toleranceVar + " * " + resVar + ";\n";
			builder.append(primaryConstraint);

			for (String constraintString : constraintLines) {
				builder.append(constraintString + "\n");
			}
			builder.append("};\n");
			models.add(builder.toString());

			// 3. now restrict with secondary penalty
			builder = new StringBuilder();
			for (String identString : identifierLines) {
				builder.append(identString + "\n");
			}
			// now add the artificial expressions
			toleranceVar = "secondary_tolerance";
			resVar = "secondary_result";

			builder.append("float " + toleranceVar + " = " + params.getSecondaryTolerance() + ";\n");
			builder.append("float " + resVar + " = " + secondaryResultLiteral + ";\n");
			builder.append(primaryToleranceExpression);
			builder.append(params.getPrimaryObjective() + "\n");
			builder.append("subject to {\n");
			compareSign = "<=";
			if (params.getPrimaryTolerance() < 1) // minimization problem
				compareSign = ">=";
			builder.append(params.extractSecondaryExpression() + " " + compareSign + " " + toleranceVar + " * " + resVar + ";\n");
			// still keep primary constraints
			builder.append(primaryConstraint);
			for (String constraintString : constraintLines) {
				builder.append(constraintString + "\n");
			}
			builder.append("};\n");
			models.add(builder.toString());

		} catch (IOException ioe) {
			ioe.printStackTrace();
		} finally {
			if (sc != null)
				sc.close();
		}
		return models;
	}

	public void optimise(Collection<String> models, Collection<String> dataFiles) {

		boolean optimisePrimary = true;
		for (String model : models) {
			File f = null;
			FileWriter fw = null;
			try {
				f = File.createTempFile("step_model", ".mod");
				fw = new FileWriter(f);
				if (model.contains(primaryResultLiteral)) {
					model = model.replace(primaryResultLiteral, Double.toString(primaryObjective));
				}
				if (model.contains(secondaryResultLiteral)) {
					model = model.replace(secondaryResultLiteral, Double.toString(secondaryObjective));
				}
				// System.out.println(model);
				System.out.println("------------------------------------------------");
				fw.write(model);
				fw.close();

				System.out.println("solving : " + f.getAbsolutePath());
				if (cs.solve(f.getAbsolutePath(), dataFiles)) {
					System.out.println("Result was: " + cs.getObjective());
					if (optimisePrimary) {
						primaryObjective = cs.getObjective();
					} else {
						secondaryObjective = cs.getObjective();
					}
					optimisePrimary = !(optimisePrimary);
				}
				f.delete();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (fw != null) {
					try {
						fw.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	public static void main(String[] args) {
		ThreeStepParetoOptimization tspo = new ThreeStepParetoOptimization();
		CplexSolver cs = new CplexSolver();
		cs.setTimeLimit(30);
		// handlers for ourselves
		cs.addConstraintValueHandler(new PenaltyHandler());
		Parameters params = new Parameters();
		params.setPrimaryObjective("minimize violation;");
		params.setSecondaryObjective("minimize penaltySum;");
		params.setPrimaryTolerance(1.2);
		params.setSecondaryTolerance(1.2);

		tspo.setConstraintSolver(cs);
		Collection<String> models = tspo.getOptimisationModels("TestModel.mod", params);

		tspo.optimise(models, null);
	}

	private void setConstraintSolver(ConstraintSolver cs) {
		this.cs = cs;
	}

}
