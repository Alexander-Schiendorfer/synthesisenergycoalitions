package ua.cr2csop.util;

import static org.junit.Assert.*;

import java.util.Set;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import ua.cr2csop.constraints.Constraint;
import ua.cr2csop.graph.DirectedConstraintGraph;
import ua.cr2csop.test.util.TestUtil;
import ua.cr2csop.util.GraphUtil;

import edu.uci.ics.jung.graph.DirectedGraph;

/**
 * Tests the util methods for JUNG graphs
 * with a sample instance
 * @author alexander
 *
 */
public class GraphUtilTest {
	
	private DirectedGraph<Constraint, Integer> graph;
	private Constraint a;
	private Constraint b;
	private Constraint c;
	private Constraint d;
	
	@Before
	public void setUp() throws Exception {
		StringBuilder sb = new StringBuilder("a>>b>>c\n");
		sb.append("b>>c>>d");
		String s = sb.toString();
		
		DirectedConstraintGraph dcg = TestUtil.getTestGraph(s);
		graph = dcg.getUnderlyingGraph();
		
		a = dcg.lookupOrAdd("a");
		b = dcg.lookupOrAdd("b");
		c = dcg.lookupOrAdd("c");
		d = dcg.lookupOrAdd("d");
	}

	@Test
	public final void testHasEdge() {
		// asserting existing edges
		Assert.assertTrue(GraphUtil.hasEdge(graph, a, b));
		Assert.assertTrue(GraphUtil.hasEdge(graph, b, c));
		Assert.assertTrue(GraphUtil.hasEdge(graph, c, d));
		
		// asserting some non-existing edges
		Assert.assertFalse(GraphUtil.hasEdge(graph, b, a));
		Assert.assertFalse(GraphUtil.hasEdge(graph, c, b));
		Assert.assertFalse(GraphUtil.hasEdge(graph, c, c));
		Assert.assertFalse(GraphUtil.hasEdge(graph, c, a));
		Assert.assertFalse(GraphUtil.hasEdge(graph, b, d));
	}

	@Test
	public final void testGetGraphCopy() throws InstantiationException, IllegalAccessException {
		DirectedGraph<Constraint, Integer> copiedGraph = (DirectedGraph<Constraint, Integer>) GraphUtil.getGraphCopy(graph);
		// assert that every vertex exists
		for(Constraint c : graph.getVertices()) {
			Assert.assertTrue(copiedGraph.containsVertex(c));
		}
		
		Assert.assertTrue(GraphUtil.hasEdge(copiedGraph, a, b));
		Assert.assertTrue(GraphUtil.hasEdge(copiedGraph, b, c));
		Assert.assertTrue(GraphUtil.hasEdge(copiedGraph, c, d));
	}

	@Test
	public final void testGetInvertedGraph() throws InstantiationException, IllegalAccessException {
		DirectedGraph<Constraint, Integer> invertedGraph = (DirectedGraph<Constraint, Integer>) GraphUtil.getInvertedGraph(graph);
		// assert that every vertex exists
		for(Constraint c : graph.getVertices()) {
			Assert.assertTrue(invertedGraph.containsVertex(c));
		}
		
		Assert.assertTrue(GraphUtil.hasEdge(invertedGraph, b, a));
		Assert.assertTrue(GraphUtil.hasEdge(invertedGraph, c, b));
		Assert.assertTrue(GraphUtil.hasEdge(invertedGraph, d, c));
	}

	@Test
	public final void testTopologicalSort() {
		// correct case -> no cycle
		Set<Constraint> candidates = GraphUtil.topologicalSort(graph);
		Assert.assertTrue(candidates.isEmpty());
		
		// incorrect case -> dcg contains a cycle
		StringBuilder sb = new StringBuilder("a>>b>>c\n");
		sb.append("b>>c>>a");
		String s = sb.toString();
		
		DirectedConstraintGraph dcg2 = TestUtil.getTestGraph(s);
		DirectedGraph<Constraint, Integer> graph2 = dcg2.getUnderlyingGraph();
		
		candidates = GraphUtil.topologicalSort(graph2);
		Assert.assertFalse(candidates.isEmpty());
	}
}
