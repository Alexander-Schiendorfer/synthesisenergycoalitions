package ua.cr2csop.test.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;

import edu.uci.ics.jung.graph.DirectedGraph;
import ua.cr2csop.constraints.Constraint;
import ua.cr2csop.graph.DirectedConstraintGraph;
import ua.cr2csop.parser.ConstraintRelationParser;

/**
 * Offers some utility methods 
 * for testing 
 * @author alexander
 *
 */
public class TestUtil {

	
	public static DirectedConstraintGraph getTestGraph(String s) {

		// convert this to input stream for testability
		String charset = "UTF-8";
		InputStream in = new ByteArrayInputStream(s.getBytes(Charset.forName(charset)));
		
		ConstraintRelationParser crp = new ConstraintRelationParser();
		return crp.getDirectedConstraintGraph(in);
	}
}
