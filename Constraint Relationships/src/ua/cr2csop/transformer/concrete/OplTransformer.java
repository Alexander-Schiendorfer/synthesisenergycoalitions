package ua.cr2csop.transformer.concrete;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Scanner;

import ua.cr2csop.constraints.Constraint;
import ua.cr2csop.exceptions.InconsistentModelException;
import ua.cr2csop.graph.DirectedConstraintGraph;
import ua.cr2csop.transformer.FormatTransformer;

/**
 * Transforms an OPL file without weights to a weighted CSP
 * 
 * @author Alexander Schiendorfer
 * 
 */
public class OplTransformer implements FormatTransformer {

    private String fileContent; // keep file for further replacements
    private String newFileContent; // modified content
    private Charset charset; // charset of input file, relevant for streams

    private static final String RELATIONSHIPS_START = "RELATIONSHIPS";
    private static final String RELATIONSHIPS_END = "*/";

    public OplTransformer() {
        charset = Charset.forName("UTF-8");
    }

    @Override
    /**
     * all relations have to be written between RELATIONSHIPS_START and 
     * RELATIONSHIPS_END, one line per constraint relation chain
     * file will be processed linewise, * and / characters will be ignored
     * File is expected to contain UTF-8 characters / or other charset when
     * set explicitly !
     */
    public InputStream readFormat(InputStream file) {

        // read contents into file for later use
        Scanner sc = new Scanner(file);
        StringBuilder fileReadBuilder = new StringBuilder();

        // read only constraint relation lines for parser
        StringBuilder constraintRelationBuilder = new StringBuilder();

        boolean readingRelationships = false;

        while (sc.hasNextLine()) {
            String newLine = sc.nextLine();
            fileReadBuilder.append(newLine);
            fileReadBuilder.append("\n");

            if (readingRelationships) {
                if (newLine.contains(RELATIONSHIPS_END))
                    readingRelationships = false;
                else {
                    newLine = newLine.replaceAll("\\*", "").replaceAll("\\/", "").trim();
                    // ignore empty lines
                    if (!"".equals(newLine)) {
                        constraintRelationBuilder.append(newLine);
                        constraintRelationBuilder.append("\n");
                    }
                }
            } else if (newLine.contains(RELATIONSHIPS_START)) {
                readingRelationships = true;
            }
        }
        fileContent = fileReadBuilder.toString();
        String constraints = constraintRelationBuilder.toString();
        InputStream constraintStream = new ByteArrayInputStream(constraints.getBytes(charset));

        return constraintStream;
    }

    @Override
    public void exportGraph(DirectedConstraintGraph dcg, OutputStream outputStream) {
        newFileContent = fileContent;
        addPenaltyVariable(dcg);
        addPenaltyToConstraint(dcg);

        try {
            outputStream.write(newFileContent.getBytes(charset));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * rewrites the input file content by adding a penalty decision variable and
     * a minimize function on that penalty variable. Replaces the string
     * "PENALTYMINIMIZER" in the original file.
     * 
     * TODO: Remove necessity to leave a mark in the original file
     * 
     */
    protected String addPenaltyVariable(DirectedConstraintGraph dcg) {
        int numberOfConstraints = dcg.getUnderlyingGraph().getVertexCount();

        StringBuilder penaltyVariableString = new StringBuilder(String.format("range PENALTYRANGE = 1..%d;\n",
                                                                              numberOfConstraints));
        penaltyVariableString.append("dvar int+ penalty[PENALTYRANGE];\n");
        penaltyVariableString.append("minimize \n\t sum (i in PENALTYRANGE) penalty[i];\n");

        // replace the original line with the new line
        return newFileContent = newFileContent.replaceAll("\\/\\*PENALTYMINIMIZER\\*\\/",
                                                          penaltyVariableString.toString());
    }

    /**
     * Rewrites the constraints in a CPLEX model by adding their weights
     */
    public String addPenaltyToConstraint(DirectedConstraintGraph dcg) {
        int constraintNumber = 1;

        for (Constraint c : dcg.getUnderlyingGraph().getVertices()) {
            String constraintName = c.getName();

            // split the string at the regular expression
            String regex = constraintName + "\\s*:\\s*";
            String[] split = fileContent.split(regex, 2);

            if (split.length > 1) { // constraintName was found
                // split the string that comes after the regular expression at
                // the first ";"
                String[] constraintLine = split[1].split(";", 2);
                String originalLine = constraintLine[0];

                String newLine = "(" + originalLine + ") || (penalty[" + constraintNumber + "] == " + c.getWeight()
                        + ")";

                constraintNumber++;

                // replace the original line with the new line
                // newFileContent = newFileContent.replaceAll(originalLine,
                // newLine);
                newFileContent = newFileContent.replace(originalLine, newLine);
            } else {
                throw new InconsistentModelException("Error occured, constraint " + constraintName
                        + " used in constraint relations but not defined in OPL.");
            }
        }
        return newFileContent;
    }

    public Charset getCharset() {
        return charset;
    }

    public void setCharset(Charset charset) {
        this.charset = charset;
    }

}
