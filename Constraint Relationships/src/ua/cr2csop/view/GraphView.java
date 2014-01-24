package ua.cr2csop.view;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JFrame;

import edu.uci.ics.jung.algorithms.layout.DAGLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import ua.cr2csop.constraints.Constraint;
import ua.cr2csop.graph.DirectedConstraintGraph;

/**
 * Implements a view component
 * for a directed constraint graph
 * 
 * @author alexander
 *
 */
public class GraphView {
	private DirectedConstraintGraph constraintGraph; // model
	private JFrame frame;                            // internal Swing component
	
	/**
	 * creates a new JFrame and shows the constraint graph
	 * as previously set
	 */
	public void display() {
		if (constraintGraph == null)
			return;
		
		Layout<Constraint, Integer> graphLayout = new DAGLayout<Constraint, Integer>(constraintGraph.getUnderlyingGraph());
		graphLayout.setSize(new Dimension(1000, 800)); // sets the initial size of the space

		VisualizationViewer<Constraint, Integer> vv = new VisualizationViewer<Constraint, Integer>(graphLayout);
		vv.setPreferredSize(new Dimension(1200, 900)); // Sets the viewing area size
		vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller<Constraint>());

		frame = new JFrame("Constraint Weighter Tool");
		frame.setBounds(0, 0, 900, 800);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		frame.getContentPane().add(vv, BorderLayout.CENTER);
		frame.pack();
		frame.setVisible(true);
	}
	
	public void hide() {
		frame.setVisible(false);
	}
	
	public DirectedConstraintGraph getConstraintGraph() {
		return constraintGraph;
	}

	public void setConstraintGraph(DirectedConstraintGraph constraintGraph) {
		this.constraintGraph = constraintGraph;
	}

}
