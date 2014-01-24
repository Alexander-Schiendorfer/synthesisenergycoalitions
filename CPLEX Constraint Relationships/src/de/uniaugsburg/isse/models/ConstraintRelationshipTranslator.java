package de.uniaugsburg.isse.models;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ua.cr2csop.constraints.Constraint;
import ua.cr2csop.graph.DirectedConstraintGraph;
import ua.cr2csop.parser.ConstraintRelationParser;
import ua.cr2csop.weights.BfsWeightAssigner;
import ua.cr2csop.weights.InvertedGraphWeightingFunction;
import ua.cr2csop.weights.concrete.DirectPredecessorDominanceWeightingFunction;
import ua.cr2csop.weights.concrete.SinglePredecessorDominanceWeightingFunction;
import ua.cr2csop.weights.concrete.TransitivePredecessorDominanceWeightingFunction;

/**
 * Takes a CPLEX model annotated with constraint relationships and transforms it into a CPLEX model does not handle
 * comments properly yet, so avoid having a comment like /* constraint_a: bla; * which would also match
 * 
 * @author Alexander Schiendorfer
 * 
 */
public class ConstraintRelationshipTranslator {

	public static enum DominanceProperty {
		SPD, DPD, TPD
	}

	private final String SOFT_CONSTRAINT_DELIMITER = "SOFT-CONSTRAINTS";
	private final Set<String> softConstraints;
	private final boolean useAnalyticalInfo = true; // use information about violated constraints as dexprs
	private DominanceProperty dominanceProperty = DominanceProperty.TPD;

	public void setDominanceProperty(DominanceProperty dominanceProperty) {
		this.dominanceProperty = dominanceProperty;
	}

	private final Collection<AnalyticalExpressionListener> analyticalExpressionListeners;

	public boolean isUseAnalyticalInfo() {
		return useAnalyticalInfo;
	}

	public DominanceProperty getDominanceProperty() {
		return dominanceProperty;
	}

	public ConstraintRelationshipTranslator() {
		softConstraints = new TreeSet<String>();
		analyticalExpressionListeners = new LinkedList<AnalyticalExpressionListener>();
	}

	public void addAnalyticalExpressionListener(AnalyticalExpressionListener ael) {
		this.analyticalExpressionListeners.add(ael);
	}

	public String translate(File cplexFile) {
		StringBuilder sb = new StringBuilder();
		StringBuilder crBuilder = new StringBuilder();

		Scanner sc = null;
		List<String> identifierLines = new LinkedList<String>();
		List<String> constraintAndObjectiveLines = new LinkedList<String>();
		boolean useForIdentifier = true;

		try {
			sc = new Scanner(cplexFile);
			boolean softConstraintsActivated = false;
			while (sc.hasNextLine()) {
				String newLine = sc.nextLine();
				if (newLine.contains("minimize") || newLine.contains("maximize")) {
					useForIdentifier = false;
				}
				if (newLine.contains("subject to")) {
					useForIdentifier = false;
				}
				if (useForIdentifier) {
					identifierLines.add(newLine);
				} else {
					constraintAndObjectiveLines.add(newLine);
				}

				if (softConstraintsActivated && !newLine.contains(SOFT_CONSTRAINT_DELIMITER)) {
					if (newLine.contains(">>")) {
						newLine = newLine.replaceAll(">>", ">").replaceAll(";", "");
						StringTokenizer tok = new StringTokenizer(newLine.trim(), " >");
						String first = tok.nextToken();
						String second = tok.nextToken();
						softConstraints.add(first.trim());
						softConstraints.add(second.trim());
					} else
						softConstraints.add(newLine.trim());
					crBuilder.append(newLine.replaceAll(";", "") + "\n");
				}
				if (newLine.contains(SOFT_CONSTRAINT_DELIMITER))
					softConstraintsActivated = !softConstraintsActivated;
			}

			// soft constraint business

			// convert this to input stream for testability
			String charset = "UTF-8";
			InputStream in = new ByteArrayInputStream(crBuilder.toString().getBytes(Charset.forName(charset)));

			ConstraintRelationParser crp = new ConstraintRelationParser();
			DirectedConstraintGraph constraintRelationshipGraph = crp.getDirectedConstraintGraph(in);

			// find weights
			BfsWeightAssigner weightAssigner = null;
			InvertedGraphWeightingFunction weightingFunction = null;
			switch (dominanceProperty) {
			case SPD:
				weightingFunction = new SinglePredecessorDominanceWeightingFunction();
				break;
			case DPD:
				weightingFunction = new DirectPredecessorDominanceWeightingFunction();
				break;
			case TPD:
				weightingFunction = new TransitivePredecessorDominanceWeightingFunction();
				break;
			}
			weightAssigner = new BfsWeightAssigner(weightingFunction);
			weightAssigner.assignWeights(constraintRelationshipGraph);

			StringBuilder softConstraintBuilder = new StringBuilder("{string} softConstraints = {");
			boolean first = true;
			for (String softConstraint : softConstraints) {
				if (first)
					first = false;
				else
					softConstraintBuilder.append(", ");
				softConstraintBuilder.append("\"" + softConstraint + "\"");
			}
			softConstraintBuilder.append("};\n");
			softConstraintBuilder.append("dvar int+ penalties[softConstraints][TIMERANGE];\n");
			softConstraintBuilder.append("dexpr float penaltySum = sum(t in TIMERANGE, c in softConstraints) penalties[c][t];\n");
			if (useAnalyticalInfo) {
				softConstraintBuilder.append("dexpr float penaltyPerStep[t in TIMERANGE] = sum(c in softConstraints) penalties[c][t];\n");
				softConstraintBuilder.append("dexpr int penaltyCount[c in softConstraints][t in TIMERANGE] = (penalties[c][t] >= 1);\n");
				softConstraintBuilder.append("dexpr int constraintViolated[c in softConstraints] = sum(t in TIMERANGE) penaltyCount[c][t];\n");
				softConstraintBuilder.append("dexpr float sumConstraintViolations = sum(t in TIMERANGE, c in softConstraints) penaltyCount[c][t];\n");
				for (AnalyticalExpressionListener ael : analyticalExpressionListeners) {
					ael.addDexpr(softConstraintBuilder);
				}
			}

			for (String identString : identifierLines) {
				sb.append(identString + "\n");
			}
			sb.append(softConstraintBuilder.toString());
			boolean appendedAnalyticals = false;
			boolean appendNextStep = false;
			for (String constraintObjectiveString : constraintAndObjectiveLines) {
				if (appendNextStep) {
					if (useAnalyticalInfo) {
						sb.append("penaltySum >= 0;\n");
						sb.append("sumConstraintViolations >= 0;\n");
						sb.append("forall(c in softConstraints) { 	constraintViolated[c] >= 0; }\n");
						for (AnalyticalExpressionListener ael : analyticalExpressionListeners) {
							ael.addConstraints(sb);
						}
					}
					appendNextStep = false;
				}
				sb.append(constraintObjectiveString + "\n");
				if (constraintObjectiveString.contains("subject to") && !appendedAnalyticals) {
					appendedAnalyticals = true;
					appendNextStep = true;
				}
			}
			String newModel = sb.toString();
			for (String softConstraint : softConstraints) {
				Pattern softConstraintPattern = Pattern.compile(softConstraint + "\\:(.*?)\\;", Pattern.DOTALL);
				Matcher m = softConstraintPattern.matcher(newModel);
				if (!m.find()) {
					throw new CplexConstraintRelationshipException("Did not find a constraint for " + softConstraint + " in file " + cplexFile.getName());
				} else {
					String constraintContent = m.group(1);
					String penalty = "penalties[\"" + softConstraint + "\"][t]";

					Constraint c = constraintRelationshipGraph.lookupOrAdd(softConstraint);

					int weight = c.getWeight();
					String penaltyRespectingConstraint = "(" + constraintContent + " && " + penalty + " == 0) || (!(" + constraintContent + ") && " + penalty
							+ " == " + weight + ");";

					newModel = newModel.replace(softConstraint + ":" + constraintContent + ";", softConstraint + ":" + penaltyRespectingConstraint);
				}
			}
			return newModel;
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} finally {
			if (sc != null)
				sc.close();
		}
		return sb.toString();
	}

	public static void main(String[] args) {
		Pattern MY_PATTERN = Pattern.compile("(minimize|maximize)(\\s)*\\:(.*?)\\;", Pattern.DOTALL);
		Matcher m = MY_PATTERN.matcher("minimize: abd;");
		while (m.find()) {
			System.out.println(m.group(3));
		}
		String s = "constraint_a: abd;";
		s = s.replaceAll("constraint_a\\:(.*?)\\;", "constraint_a:frank;");

		ConstraintRelationshipTranslator crt = new ConstraintRelationshipTranslator();
		s = crt.translate(new File("testmodel.mod"));
		System.out.println(s);
	}
}
