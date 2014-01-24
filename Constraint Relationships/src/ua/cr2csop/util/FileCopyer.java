package ua.cr2csop.util;

import java.io.File;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.IOException;

public class FileCopyer {
	
	private final File inputFile;
	
	public FileCopyer(File inputFile)
	{
		this.inputFile = inputFile;
	}
	
	/**
	 * Takes the input file that is provided by the constructor and creates a copy in the same directory.
	 * The name of the new file is the old filename + Weighted 
	 * @param File file
	 * 				The file we want to create a copy of
	 * @return File outputFile
	 * 
	 * @throws IOException
	 */
	public File createCopy() throws IOException
	{
		// get the file's path, name and extension as separate strings
		String fileName = inputFile.getName();
		String extension;
		
		int pos = fileName.lastIndexOf(".");
        if (pos == -1)
        {
        	throw new RuntimeException("Inconsistent filename: " + fileName);
        }
        else {
        	fileName = fileName.substring(0,pos);
        	extension = inputFile.getName().substring(pos+1);
        }
        
		String filePath = inputFile.getPath();
		
		//create new file in the same directory with the name addition "Weighted"
		String outputFileName = filePath + fileName + "Weighted" + extension;
		File outputFile = new File(outputFileName);
		
		/* check if file already exists
		 * if the file exists delete it
		 * if no such file exists create the file
		 */
		
		boolean exists = outputFile.exists();
		
		if(exists)
		{
			outputFile.delete();
		}
		
		FileReader in = new FileReader(inputFile);
		FileWriter out = new FileWriter(outputFile);
		
		int c;
		
		while ((c = in.read()) != -1)
			out.write(c);
		
		in.close();
		out.close();
		
		return outputFile;
	}

}
