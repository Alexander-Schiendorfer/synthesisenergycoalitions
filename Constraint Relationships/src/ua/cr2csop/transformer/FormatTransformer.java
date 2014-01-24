package ua.cr2csop.transformer;

import ua.cr2csop.exporter.FormatExporter;
import ua.cr2csop.importer.FormatImporter;

/**
 * A FormatTransformer is one object that implements
 * both import and export for a given file format
 * @author Alexander Schiendorfer
 *
 */
public interface FormatTransformer extends FormatImporter, FormatExporter{

}
