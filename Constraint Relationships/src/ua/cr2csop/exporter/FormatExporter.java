package ua.cr2csop.exporter;

import java.io.OutputStream;

import ua.cr2csop.graph.DirectedConstraintGraph;

/**
 * Defines an interface for different exporters of directed constraint graphs
 * 
 * -> there might be different semantics of how to interpret weighted CSPs
 * 
 * @author Alexander Schiendorfer
 * 
 */
public interface FormatExporter {

    /**
     * Writes a textual representation of the given
     * {@link DirectedConstraintGraph} to the provided output stream.
     * 
     * @param dcg
     *            the graph to be exported
     * @param outputStream
     *            the output stream the textual representation is written to
     */
    void exportGraph(DirectedConstraintGraph dcg, OutputStream outputStream);
}
