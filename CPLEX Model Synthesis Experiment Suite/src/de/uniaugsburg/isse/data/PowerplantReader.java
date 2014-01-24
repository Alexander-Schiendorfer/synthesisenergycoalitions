package de.uniaugsburg.isse.data;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;

public class PowerplantReader {

	public Collection<Double> readPlantNominalCapacities(String fileName) {
		BufferedReader br = null;
		Collection<Double> plantList = new LinkedList<Double>();
		try {
			br = new BufferedReader(new FileReader(fileName));
			String nextLine;
			int i = 0;
			while ((nextLine = br.readLine()) != null) {
				double pMax = Double.parseDouble(nextLine);
				plantList.add(pMax);
				++i;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null)
					br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return plantList;
	}
}
