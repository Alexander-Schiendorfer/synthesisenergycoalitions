package ua.cr2csop.parser;

import java.io.InputStream;
import java.util.Scanner;
import java.util.StringTokenizer;

import ua.cr2csop.constraints.Constraint;
import ua.cr2csop.graph.DirectedConstraintGraph;

/**
 * Takes string input describing constraint relations from different sources and
 * creates a DAG respective to the constraint order
 * 
 * @author Alexander Schiendorfer
 * 
 */
public class ConstraintRelationParser {

	/**
	 * Builds a directed constraint graph based on the constraint relation
	 * definitions found in the input stream
	 * 
	 * Expects the input to contain one lines for every constraint relation each
	 * constraint relation is defined as a1>>ai>>an for n constraints resulting
	 * in precedes-edges (a1,a2), (a2, a3) ... (an-1,an)
	 * 
	 * @param in
	 * @return
	 */
	public DirectedConstraintGraph getDirectedConstraintGraph(InputStream in) {
		DirectedConstraintGraph dcg = new DirectedConstraintGraph();

		Scanner sc = new Scanner(in);
		while (sc.hasNextLine()) {
			String line = sc.nextLine();
			readConstraintRelationList(line, dcg);
		}
		sc.close();
		return dcg;
	}

	/**
	 * Creates a list of relations between constraints from the given String.
	 * 
	 * The string constraints relationships in the form A>>B>>C. The function
	 * creates a two element array for each such relation, in this case [A,B]
	 * and [B,C]. These arrays are then added to a list that is finally
	 * returned.
	 * 
	 * @param String
	 *            s
	 * @return ArrayList relationList
	 */
	private void readConstraintRelationList(String s,
			DirectedConstraintGraph dcg) {
		StringTokenizer st = new StringTokenizer(s, ">>", false);
		Constraint currentPredecessor = dcg.lookupOrAdd(st.nextToken().trim());
		Constraint currentSuccessor = null;

		while (st.hasMoreTokens()) {
			currentSuccessor = dcg.lookupOrAdd(st.nextToken().trim());
			dcg.addEdge(currentPredecessor, currentSuccessor);
			currentPredecessor = currentSuccessor;
		}
	}
}
