package ua.cr2csop.importer;

import java.io.InputStream;

/**
 * Defines an interface for FormatImporters
 * i.e. files that are preprocessed to extract
 * the relation part (a>>c,...) for the parser
 * @author alexander
 *
 */
public interface FormatImporter {
	InputStream readFormat(InputStream file); 
}
