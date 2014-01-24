package usage.example;

import org.junit.Before;
import org.junit.Test;

import ua.cr2csop.constraints.Constraint;
import ua.cr2csop.graph.DirectedConstraintGraph;
import ua.cr2csop.test.util.TestUtil;
import ua.cr2csop.weights.BfsWeightAssigner;
import ua.cr2csop.weights.concrete.DirectPredecessorDominanceWeightingFunction;
import ua.cr2csop.weights.concrete.SinglePredecessorDominanceWeightingFunction;
import ua.cr2csop.weights.concrete.TransitivePredecessorDominanceWeightingFunction;

/**
 * This class performs basic operations on a simple constraint relationships graph
 * 
 * <pre>
 *           a
 *         b   c
 *             d
 * </pre>
 * 
 * @author Alexander Schiendorfer
 * 
 */
public class SimpleUsageTest {
	private DirectedConstraintGraph graph; // the actual graph
	private BfsWeightAssigner weightAssigner; // assigns weights
	private Constraint a;
	private Constraint b;
	private Constraint c;
	private Constraint d;

	@Before
	public void setUp() throws Exception {
		StringBuilder sb = new StringBuilder();

		// format the relationships as x >> y for x >_R y (x is more important than y)
		sb.append("a>>b\n");
		sb.append("a>>c\n");
		sb.append("c>>d\n");

		String source = sb.toString();

		// parses and builds the graph, wraps string to inputstream handling -> you can use ConstraintRelationParser if
		// reading from a file
		graph = TestUtil.getTestGraph(source);

	}

	private void weightAndPrint(BfsWeightAssigner weightAssigner2) {
		System.out.println("-------------------");
		weightAssigner2.assignWeights(graph);
		for (Constraint c : graph.getUnderlyingGraph().getVertices()) {
			System.out.println(c);
		}
		System.out.println("-------------------");
	}

	@Test
	public void test() {
		// 1. do single predecessor dominance
		weightAssigner = new BfsWeightAssigner(new SinglePredecessorDominanceWeightingFunction());
		weightAndPrint(weightAssigner);

		// 2. do direct predecessor dominance
		weightAssigner = new BfsWeightAssigner(new DirectPredecessorDominanceWeightingFunction());
		weightAndPrint(weightAssigner);

		// 3. do transitive predecessor dominance
		weightAssigner = new BfsWeightAssigner(new TransitivePredecessorDominanceWeightingFunction());
		weightAndPrint(weightAssigner);
	}

}
