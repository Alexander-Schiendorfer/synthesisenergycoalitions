package ua.cr2csop.parser;


import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.HashMap;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import edu.uci.ics.jung.graph.DirectedGraph;

import ua.cr2csop.constraints.Constraint;
import ua.cr2csop.graph.DirectedConstraintGraph;
import ua.cr2csop.parser.ConstraintRelationParser;
import ua.cr2csop.test.util.TestUtil;
import ua.cr2csop.util.GraphUtil;

/**
 * Tests if the parser works correctly
 * on a small graph instance
 * @author alexander
 *
 */
public class ConstraintRelationParserTest {

	private ConstraintRelationParser parser;
	
	@Before
	public void setUp() throws Exception {
		parser = new ConstraintRelationParser();
	}

	@Test
	public void testBaseCase() {
		StringBuilder sb = new StringBuilder("a>>b>>c\n");
		sb.append("b>>c>>d");
		String s = sb.toString();
		
		// convert this to input stream for testability
		String charset = "UTF-8";
		InputStream in = new ByteArrayInputStream(s.getBytes(Charset.forName(charset)));
		
		DirectedConstraintGraph dcg = parser.getDirectedConstraintGraph(in);
		DirectedGraph<Constraint, Integer> dg = dcg.getUnderlyingGraph();
		
		// edge b >> c gets inserted only once
		Assert.assertEquals(3, dg.getEdgeCount());
		Assert.assertEquals(4, dg.getVertexCount());
		
		HashMap<String, Constraint> constraints = new HashMap<String, Constraint>();
		
		for(Constraint c : dg.getVertices()) {
			constraints.put(c.getName(), c);
		}
		
		Assert.assertTrue(GraphUtil.hasEdge(dg, constraints.get("a"), constraints.get("b")));
		Assert.assertFalse(GraphUtil.hasEdge(dg, constraints.get("b"), constraints.get("a")));
	}
	
	@Test
	public void testMultipleConstraints() {
		StringBuilder sb = new StringBuilder("a>>b\n");
		sb.append("c>>b");
		
		DirectedConstraintGraph dcg = TestUtil.getTestGraph(sb.toString());
		
		// b only inserted once
		Assert.assertEquals(3, dcg.getUnderlyingGraph().getVertexCount());
	}

}
