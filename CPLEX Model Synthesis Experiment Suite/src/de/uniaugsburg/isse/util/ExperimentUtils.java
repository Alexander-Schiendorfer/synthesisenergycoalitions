package de.uniaugsburg.isse.util;

import java.util.ArrayList;
import java.util.Collection;

import de.uniaugsburg.isse.RandomManager;

/**
 * Provides simple methods to draw from a collection
 * 
 * @author alexander
 * 
 */
public class ExperimentUtils {
	public static <T> Collection<T> drawSamples(Collection<T> baseSet, int draws) {
		ArrayList<T> origList = new ArrayList<T>(baseSet.size());
		origList.addAll(baseSet);
		ArrayList<T> drawnList = new ArrayList<T>(draws);

		int[] indices = new int[baseSet.size()];

		for (int i = 0; i < indices.length; ++i)
			indices[i] = i;

		shuffleArray(indices);
		for (int i = 0; i < draws; ++i) {
			drawnList.add(origList.get(indices[i]));
		}
		return drawnList;
	}

	private static void shuffleArray(int[] indices) {
		for (int j = indices.length - 1; j > 1; --j) {
			int nextIndex = RandomManager.getInt(j - 1);
			int help = indices[nextIndex];
			indices[nextIndex] = indices[j];
			indices[j] = help;
		}
	}
}
