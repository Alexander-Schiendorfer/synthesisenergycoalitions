package ua.cr2csop.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import javax.swing.JFileChooser;

import ua.cr2csop.graph.DirectedConstraintGraph;
import ua.cr2csop.gui.ConstraintGraphView;
import ua.cr2csop.parser.ConstraintRelationParser;
import ua.cr2csop.transformer.concrete.OplTransformer;
import ua.cr2csop.util.OplModelFileFilter;
import ua.cr2csop.weights.WeightAssigner;
import ua.cr2csop.weights.WeightAssignerFactory;

/**
 * Offers easy access to the classes involved with a CPLEX-OPL conversion
 * 
 * @author alexander
 * 
 */
public class CplexOplFacade {

    private final OplTransformer transformer = new OplTransformer();;

    /**
     * Reads constraints from an input file and writes a CPLEX OPL specification
     * to output file opens a JFrame visualizing the constraint graph
     * 
     * @param inputFile
     * @param outputFile
     */
    public void processFiles(InputStream inputFile, OutputStream outputFile) {
        DirectedConstraintGraph dcg = openFile(inputFile, false);

        // writing weighted CSP to file
        transformer.exportGraph(dcg, outputFile);
    }

    public DirectedConstraintGraph openFile(InputStream inputFile, boolean showGui) {

        // extracting input relations from OPL file
        InputStream extractedConstraintRelations = transformer.readFormat(inputFile);

        // parsing input file to constraint graph
        ConstraintRelationParser parser = new ConstraintRelationParser();
        DirectedConstraintGraph dcg = parser.getDirectedConstraintGraph(extractedConstraintRelations);

        // assigning weights to graph
        WeightAssigner weightAssigner = WeightAssignerFactory.getWeightAssigner();
        weightAssigner.assignWeights(dcg);

        if (showGui) {
            // display Graph as Tree
            ConstraintGraphView view = new ConstraintGraphView(transformer);
            // GraphView view = new GraphView();
            view.setConstraintGraph(dcg);
            view.display();
        }

        return dcg;
    }

    /**
     * Main method for the CPLEX OPL Facade. Shows an open file and a save file
     * dialog to select the input and output files and starts the transformation
     * on the selected input file. If two file names are provided on the command
     * line, these are used as input files and output files respectively.
     * 
     * @param args
     * @throws FileNotFoundException
     */
    public static void main(String[] args) throws FileNotFoundException {
        File inputFile = null;
        File outputFile = null;
        final JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new OplModelFileFilter());
        fc.setDialogTitle("Choose OPL file for conversion");

        // Select the input file
        if (args.length > 1) {
            String fileName = args[1];
            inputFile = new File(fileName);
        } else {
            int fileChooserReturnValue = fc.showOpenDialog(null);
            if (fileChooserReturnValue == JFileChooser.APPROVE_OPTION) {
                inputFile = fc.getSelectedFile();
            } else {
                System.out.println("No input file selected. Exiting.");
                System.exit(1);
            }
        }
        FileInputStream fis = new FileInputStream(inputFile);

        CplexOplFacade facade = new CplexOplFacade();
        // Select output file
        if (args.length > 2) {
            String fileName = args[2];
            outputFile = new File(fileName);
            FileOutputStream fos = new FileOutputStream(outputFile);

            // Do the conversion

            facade.processFiles(fis, fos);
        } else {
            facade.openFile(fis, true);
        }

    }

}
