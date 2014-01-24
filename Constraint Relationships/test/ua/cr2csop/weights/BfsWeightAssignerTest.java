package ua.cr2csop.weights;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ua.cr2csop.constraints.Constraint;
import ua.cr2csop.graph.DirectedConstraintGraph;
import ua.cr2csop.test.util.TestUtil;
import ua.cr2csop.util.GraphUtil;
import ua.cr2csop.weights.concrete.TransitivePredecessorDominanceWeightingFunction;
import edu.uci.ics.jung.graph.DirectedGraph;

/**
 * Tests the total dominance assigner and its helper methods
 * 
 * @author alexander
 * 
 */
public class BfsWeightAssignerTest {

	private BfsWeightAssigner tda;
	private DirectedConstraintGraph dcg;
	private DirectedGraph<Constraint, Integer> graph;
	private Constraint a;
	private Constraint b;
	private Constraint c;
	private Constraint d;

	@Before
	public void setUp() throws Exception {
		tda = new BfsWeightAssigner(new TransitivePredecessorDominanceWeightingFunction());

		StringBuilder sb = new StringBuilder("a>>b>>c\n");
		sb.append("b>>c>>d");
		String s = sb.toString();

		dcg = TestUtil.getTestGraph(s);
		graph = dcg.getUnderlyingGraph();

		a = dcg.lookupOrAdd("a");
		b = dcg.lookupOrAdd("b");
		c = dcg.lookupOrAdd("c");
		d = dcg.lookupOrAdd("d");
	}

	@Test
	public final void testAssignWeights() {
		tda.assignWeights(dcg);
		for (Constraint c : dcg.getUnderlyingGraph().getVertices()) {
			System.out.println(c);
		}

		// using example of soft-corridors.pdf
		StringBuilder sb = new StringBuilder("a>>b>>h\n");
		sb.append("b>>d>>g\n");
		sb.append("a>>c>>d\n");
		sb.append("b>>e\n");
		sb.append("b>>f\n");

		String s = sb.toString();
		System.out.println("------------------");
		dcg = TestUtil.getTestGraph(s);
		tda.assignWeights(dcg);
		for (Constraint c : dcg.getUnderlyingGraph().getVertices()) {
			System.out.println(c);
		}
	}

	@Test
	public final void testInsertRootNode() throws InstantiationException, IllegalAccessException {
		DirectedGraph<Constraint, Integer> invertedGraph = (DirectedGraph<Constraint, Integer>) GraphUtil.getInvertedGraph(graph);
		Constraint startNode = new Constraint("root", 0);

		tda.insertRootNode(invertedGraph, startNode);

		// asserting that the proper edges have been added along with the root
		// node
		Assert.assertTrue(GraphUtil.hasEdge(invertedGraph, startNode, d));
		Assert.assertFalse(GraphUtil.hasEdge(invertedGraph, startNode, a));
		Assert.assertFalse(GraphUtil.hasEdge(invertedGraph, startNode, b));
		Assert.assertFalse(GraphUtil.hasEdge(invertedGraph, startNode, c));
	}

}
