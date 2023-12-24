/**
 * 
 */
package Utilities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import Graph.Graph;

/**
 * @author feng zijin
 *
 */
public class Functions {
	public static void sampling(List<Integer> S, int seedSetSize) {

		HashSet<Integer> seedSet = new HashSet<Integer>(seedSetSize);
		while (seedSet.size() < seedSetSize) {
			seedSet.add(Functions.randInt(0, Graph.getVertexSize() - 1));
		}

		for (int seed : seedSet)
			S.add(seed);
	}

	public static List<Integer>[] sampling(int maxLevel) {

		List<Integer>[] seeds_allLevel = new ArrayList[maxLevel];

		// initialize
		for (int level = 0; level < maxLevel; level++) {
			seeds_allLevel[level] = new ArrayList<Integer>((int) Math.pow(2, level));
		}

		// sampling for the highest level
		int seedSetSize = (int) Math.pow(2, maxLevel - 1);
		HashSet<Integer> seedSet = new HashSet<Integer>(seedSetSize);
		while (seedSet.size() < seedSetSize) {
			seedSet.add(Functions.randInt(0, Graph.getVertexSize() - 1));
		}

		List<Integer> set = new ArrayList<Integer>(seedSetSize);
		for (int seed : seedSet)
			set.add(seed);
		Collections.shuffle(set);
		seeds_allLevel[maxLevel - 1] = set;

		// for other levels
		for (int level = maxLevel - 2; level >= 0; level--) {
			seedSetSize = (int) Math.pow(2, level);

			int currentSize = 0;
			for (int seed : seeds_allLevel[level + 1]) {
				seeds_allLevel[level].add(seed);
				if (++currentSize >= seedSetSize)
					break;
			}
		}

		return seeds_allLevel;
	}

	public static String listToString(List<Integer> list) {
		String str = "";

		for (int i = 0; i < list.size() - 1; i++) {
			str += list.get(i) + "\t";
		}

		if (list.size() > 0)
			str += list.get(list.size() - 1);

		return str;
	}

	public static String listToString(HashSet<Integer> list) {
		String str = "";

		Iterator<Integer> it = list.iterator();
		while (it.hasNext()) {
			str += it.next() + "\t";
		}

		return str;
	}

	public static String arrToString(String[] list) {
		String str = "";

		for (int i = 0; i < list.length - 1; i++) {
			str += list[i] + "\t";
		}

		if (list.length > 0)
			str += list[list.length - 1];

		return str;
	}

	public static String arrToString(int[] list) {
		String str = "";

		for (int i = 0; i < list.length - 1; i++) {
			str += list[i] + "\t";
		}

		if (list.length > 0)
			str += list[list.length - 1];

		return str;
	}

	public static String arrToString(float[] list) {
		String str = "";

		for (int i = 0; i < list.length - 1; i++) {
			str += list[i] + "\t";
		}

		if (list.length > 0)
			str += list[list.length - 1];

		return str;
	}

	public static String arrToString(double[] list) {
		String str = "";

		for (int i = 0; i < list.length - 1; i++) {
			str += list[i] + "\t";
		}

		if (list.length > 0)
			str += list[list.length - 1];

		return str;
	}

	public static int randInt(int min, int max) {
		Random rand = null;

		int randomNum = min + (int) (Math.random() * ((max - min) + 1));

		return randomNum;
	}

	public static int randIntNorm(int min, int mean) {
		Random rand = null;

		int randomNum = min + (int) Math.round(rand.nextGaussian() * mean);

		return randomNum;
	}

	public static int randIntExp(int min, int max) {
		int x = randInt(min, max);

		return (int) Math.pow(2, x);
	}

	public static float max(float[] arr) {
		return arr[arr.length - 1];
	}

	public static float min(float[] arr) {
		return arr[0];
	}
}
