package de.uniaugsburg.isse.statistics;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ExperimentStatistics {
	public static String FAILED = "fail";
	private final Map<String, List<Double>> rawData;
	public static final String V = "v";
	public static final String P = "p";
	public static final String VC = "vc";
	public static final String PRA = "pra";
	public static final String SO = "so"; // suboptimal
	public static final String PDIFF = "pdiff";
	public static final String VDIFF = "vdiff";

	public int fails;
	public int subOptimals;

	public ExperimentStatistics() {
		rawData = new HashMap<String, List<Double>>();
		fails = 0;
	}

	public void addValue(String kind, Double value) {
		List<Double> series = null;
		if ((series = rawData.get(kind)) == null)
			rawData.put(kind, (series = new LinkedList<Double>()));
		series.add(value);
	}

	public String toCsv() {
		StringBuilder sb = new StringBuilder();

		for (String kinds : rawData.keySet()) {
			sb.append(kinds + ";");
		}
		sb.append("\n");
		int index = 0;
		boolean hasMoreLines = true;
		while (hasMoreLines) {
			hasMoreLines = false;
			for (String kind : rawData.keySet()) {
				List<Double> vals = rawData.get(kind);
				if (vals != null && index < vals.size()) {
					sb.append(vals.get(index));
					hasMoreLines = true;
				}
				sb.append(";");
			}
			++index;
			sb.append("\n");
		}
		return sb.toString();
	}

	public void addValues(String p2, double[] values) {
		for (int i = 0; i < values.length; ++i) {
			addValue(p2 + i, values[i]);
		}
	}

	public void increaseFailCounter() {
		++fails;
	}

}
