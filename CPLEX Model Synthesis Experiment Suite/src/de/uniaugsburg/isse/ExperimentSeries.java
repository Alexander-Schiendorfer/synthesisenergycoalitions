package de.uniaugsburg.isse;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.Properties;
import java.util.Scanner;

import de.uniaugsburg.isse.statistics.ExperimentStatistics;

public class ExperimentSeries {
	public void run(String fileRef) {
		File f = new File(fileRef);
		// read line by line and load properties file
		try {
			Scanner sc = new Scanner(f);
			while (sc.hasNextLine()) {
				String line = sc.nextLine().trim();
				String fileName = line.substring(0, line.indexOf("."));
				ExperimentParameters params = loadExperimentFromFile("experiments/" + line);
				SingleExperiment exp = new SingleExperiment();
				ExperimentStatistics stats = new ExperimentStatistics();
				exp.setStatistics(stats);
				exp.run(params);
				String csvContent = stats.toCsv();
				File csvResult = new File("results/" + fileName + "_results.csv");
				writeFile(csvResult, csvContent);

				File archiveFile = new File("results/" + fileName + new Date().getTime() + ".txt");
				StringBuilder archiveContentBuilder = new StringBuilder();
				Date now = new Date();
				archiveContentBuilder.append("Filename: " + fileName + " / run at " + now.toString() + "\n");
				archiveContentBuilder.append("fails: " + stats.fails + " / suboptimals: " + stats.subOptimals + "\n");
				archiveContentBuilder.append("Time horizon: " + exp.timeHorizon);
				Properties props = params.getProperties();
				for (Object key : props.keySet()) {
					String strKey = (String) key;
					archiveContentBuilder.append(strKey + "=" + props.getProperty(strKey) + "\n");
				}
				archiveContentBuilder.append(csvContent);
				writeFile(archiveFile, archiveContentBuilder.toString());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void writeFile(File csvResult, String csvContent) throws IOException {
		FileWriter fw = new FileWriter(csvResult);
		fw.write(csvContent);
		fw.close();
	}

	private ExperimentParameters loadExperimentFromFile(String string) throws FileNotFoundException, IOException {
		Properties p = new Properties();
		p.load(new FileInputStream(string));
		ExperimentParameters params = new ExperimentParameters();
		params.setProperties(p);
		params.setSeed(Integer.parseInt(p.getProperty("seed")));
		params.setDeltaPenalties(Double.parseDouble(p.getProperty("deltaPenalties")));
		params.setDeltaViolation(Double.parseDouble(p.getProperty("deltaViolation")));
		params.setDominanceProperty(Integer.parseInt(p.getProperty("dominanceProperty")));
		params.setNumberOfPlants(Integer.parseInt(p.getProperty("numberOfPlants")));
		params.setNumberRuns(Integer.parseInt(p.getProperty("numberOfRuns")));

		return params;
	}

	public static void main(String[] args) {
		String fileRef = "experiments/experiments.txt";
		ExperimentSeries series = new ExperimentSeries();
		series.run(fileRef);
	}
}
