package ua.cr2csop.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Collection;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;

import ua.cr2csop.constraints.Constraint;
import ua.cr2csop.graph.DirectedConstraintGraph;
import ua.cr2csop.transformer.concrete.OplTransformer;
import ua.cr2csop.weights.WeightAssigner;
import ua.cr2csop.weights.WeightAssignerFactory;
import ua.cr2csop.weights.concrete.DirectPredecessorDominanceWeightingFunction;
import ua.cr2csop.weights.concrete.SinglePredecessorDominanceWeightingFunction;
import ua.cr2csop.weights.concrete.TransitivePredecessorDominanceWeightingFunction;
import edu.uci.ics.jung.algorithms.layout.DAGLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.DelegateForest;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.Forest;
import edu.uci.ics.jung.graph.util.Pair;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ScalingControl;
import edu.uci.ics.jung.visualization.decorators.EdgeShape;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;

public class ConstraintGraphView {
    private DirectedConstraintGraph constraintGraph;
    private JFrame frame;
    private final OplTransformer transformer;

    private VisualizationViewer<Constraint, Integer> visualizationViewer;
    private Layout<Constraint, Integer> graphLayout;
    private Forest<Constraint, Integer> treeGraph;
    private GraphZoomScrollPane zspane;
    private JComboBox modeComboBox;
    private DefaultModalGraphMouse<Constraint, Integer> graphMouse;
    private JButton plus;
    private JButton minus;
    private JSplitPane mainSplitPane;

    /**
     * Initializes a new Constraint Graph View. Does not initialize the GUI.
     * 
     * @see #display()
     * @param transformer
     *            the {@link OplTransformer} to use for saving the graph
     */
    public ConstraintGraphView(OplTransformer transformer) {
        this.transformer = transformer;
    }

    /**
     * Creates all required GUI components and displays the window.
     */
    public void display() {
        graphMouse = new DefaultModalGraphMouse<Constraint, Integer>();
        initVisualization();

        final JPanel zoomPanel = new JPanel(new GridLayout(1, 0));
        zoomPanel.setBorder(BorderFactory.createTitledBorder("Zoom"));
        zoomPanel.add(plus);
        zoomPanel.add(minus);

        // Button to save the modified model
        JButton saveButton = new JButton("Save File");
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveFile();
            }
        });

        // JPanel containing a TextArea
        final JTextArea latexCodeArea = new JTextArea();
        JButton createTex = new JButton("Refresh LaTeX code");
        createTex.setToolTipText("Refreshes the LaTex xygraph");
        createTex.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                latexCodeArea.setText(xygraph(graphLayout));
            }
        });
        latexCodeArea.setText(xygraph(graphLayout));
        JPanel xygraphPanel = new JPanel();
        xygraphPanel.add(latexCodeArea);

        JPanel weightingFunctionPanel = new JPanel();
        JLabel weightingFunctionLabel = new JLabel("Weighting Function:");
        JComboBox weightingFunctionCombo = new JComboBox(new String[] { "Transitive-Predecessor-Dominance",
                "Direct-Predecessor-Dominance", "Max-Predecessor-Dominance" });
        weightingFunctionCombo.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent evt) {
                // Get the affected item
                Object item = evt.getItem();

                if (evt.getStateChange() == ItemEvent.SELECTED) {
                    if (item.equals("Transitive-Predecessor-Dominance")) {
                        System.out.println("Combobox selection changed to Transitive-Predecessor-Dominance");
                        WeightAssigner weightAssigner = WeightAssignerFactory
                                .getWeightAssigner(new TransitivePredecessorDominanceWeightingFunction());
                        weightAssigner.assignWeights(constraintGraph);
                    } else if (item.equals("Direct-Predecessor-Dominance")) {
                        System.out.println("Combobox selection changed to Direct-Predecessor-Dominance");
                        WeightAssigner weightAssigner = WeightAssignerFactory
                                .getWeightAssigner(new DirectPredecessorDominanceWeightingFunction());
                        weightAssigner.assignWeights(constraintGraph);
                    } else if (item.equals("Max-Predecessor-Dominance")) {
                        System.out.println("Combobox selection changed to Max-Predecessor-Dominance");
                        WeightAssigner weightAssigner = WeightAssignerFactory
                                .getWeightAssigner(new SinglePredecessorDominanceWeightingFunction());
                        weightAssigner.assignWeights(constraintGraph);
                    }
                    latexCodeArea.setText(xygraph(graphLayout));
                    mainSplitPane.remove(zspane);
                    initVisualization();
                    mainSplitPane.add(zspane);
                    frame.pack();
                }

            }

        });
        weightingFunctionPanel.add(weightingFunctionLabel);
        weightingFunctionPanel.add(weightingFunctionCombo);

        // JPanel that contains all control elements
        JPanel controls = new JPanel();
        controls.add(zoomPanel);
        controls.add(modeComboBox);
        controls.add(weightingFunctionPanel);
        controls.add(createTex);
        controls.add(saveButton);

        mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, zspane, xygraphPanel);
        mainSplitPane.setContinuousLayout(true);

        // JFrame
        frame = new JFrame("Constraint Weighter Tool");
        frame.setBounds(0, 0, 1000, 500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.getContentPane().add(mainSplitPane, BorderLayout.CENTER);
        frame.getContentPane().add(controls, BorderLayout.SOUTH);

        frame.pack();
        frame.setVisible(true);

    }

    private void initVisualization() {
        treeGraph = new DelegateForest<Constraint, Integer>(constraintGraph.getUnderlyingGraph());

        graphLayout = new DAGLayout<Constraint, Integer>(treeGraph);

        visualizationViewer = new VisualizationViewer<Constraint, Integer>(graphLayout, new Dimension(600, 600));

        visualizationViewer.getRenderContext().setEdgeShapeTransformer(new EdgeShape.Line<Constraint, Integer>());
        visualizationViewer.getRenderContext().setVertexLabelTransformer(new ToStringLabeller<Constraint>());

        visualizationViewer.setGraphMouse(graphMouse);

        zspane = new GraphZoomScrollPane(visualizationViewer);

        // ComboBox to set the edit mode
        modeComboBox = graphMouse.getModeComboBox();
        modeComboBox.addItemListener(graphMouse.getModeListener());
        graphMouse.setMode(ModalGraphMouse.Mode.TRANSFORMING);

        // Buttons for zooming
        final ScalingControl scaler = new CrossoverScalingControl();

        plus = new JButton("+");
        plus.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                scaler.scale(visualizationViewer, 1.2f, visualizationViewer.getCenter());
            }
        });

        minus = new JButton("-");
        minus.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                scaler.scale(visualizationViewer, 1 / 1.2f, visualizationViewer.getCenter());
            }
        });
    }

    /**
     * Hides the application window.
     */
    public void hide() {
        frame.setVisible(false);
    }

    /**
     * Gets the constraint graph currently being worked on.
     * 
     * @return the constraint graph
     */
    public DirectedConstraintGraph getConstraintGraph() {
        return constraintGraph;
    }

    /**
     * Sets the constraint graph to work on.
     * 
     * @param constraintGraph
     *            a new constraint graph
     */
    public void setConstraintGraph(DirectedConstraintGraph constraintGraph) {
        this.constraintGraph = constraintGraph;
    }

    /**
     * Creates a String containing the code for a LaTex xygraph from the graph
     * layout provided.
     * 
     * Important: at the moment the coordinate system for xygraph is in mm, the
     * coordinates from the graph are in pixles though
     * 
     * @param layout
     *            the graph layout used to render the xygraph
     * @return valid LaTeX code for the xy package corresponding to the supplied
     *         graph layout
     */
    private String xygraph(Layout<Constraint, Integer> layout) {
        DirectedGraph<Constraint, Integer> dg = constraintGraph.getUnderlyingGraph();

        Collection<Constraint> vertices = dg.getVertices();
        Collection<Integer> edges = dg.getEdges();
        String vText = "", eStart = "", eEnd = "";

        DecimalFormatSymbols modifiedSymbols = new DecimalFormatSymbols();
        modifiedSymbols.setDecimalSeparator('.');

        DecimalFormat f = new DecimalFormat("#0.00", modifiedSymbols);

        String outputString = "";
        // start xygraph
        String graphStart = "\\xygraph{ \n!{<0mm,0mm>;<1mm,0mm>:<0mm,-1mm>::}\n";
        outputString += graphStart;

        // reads vertex positions from VisualizationViewer and add them to the
        // xygraph
        for (Constraint c : vertices) {

            Point2D p = layout.transform(c);
            double px = p.getX() / Toolkit.getDefaultToolkit().getScreenResolution() / 2.54;
            double py = p.getY() / Toolkit.getDefaultToolkit().getScreenResolution() / 2.54;

            String pxString = f.format(px);
            String pyString = f.format(py);
            vText = c.getName();

            String vertex = String.format("!{ (%s,%s) }*+{%s : \\; \\left[%s\\right]} =\"%s\"\n", pxString, pyString,
                                          vText, c.getWeight(), vText);
            outputString += vertex;
        }

        // set the edges
        for (int i : edges) {
            Pair<Constraint> p = dg.getEndpoints(i);
            Constraint c1 = p.getFirst();
            Constraint c2 = p.getSecond();

            eStart = c1.getName();
            eEnd = c2.getName();

            String edge = String.format("\"%s\":\"%s\"\n", eStart, eEnd);
            outputString += edge;
        }

        // close xygraph
        outputString += "}";

        return outputString;
    }

    /**
     * Saves the current constraint graph to a file of the user's choosing.
     */
    private void saveFile() {
        // writing weighted CSP to file
        File outputFile = null;
        final JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Choose converted output file");
        int fileChooserReturnValue = fc.showSaveDialog(null);
        if (fileChooserReturnValue == JFileChooser.APPROVE_OPTION) {
            outputFile = fc.getSelectedFile();
        } else {
            System.out.println("No output file selected. Exiting.");
            return;
        }
        if (outputFile != null) {
            FileOutputStream fos;
            try {
                System.out.println("Writing data to \"" + outputFile.getCanonicalPath() + "\".");
                fos = new FileOutputStream(outputFile);
                transformer.exportGraph(constraintGraph, fos);
            } catch (FileNotFoundException e) {
                System.out.println("Could not write to output file.");
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

}
