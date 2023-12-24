package Exp;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import Community.SetPyramid;
import Graph.Edge;
import Graph.Graph;
import Utilities.Constant;
import Utilities.FilePath_Mon;
import Utilities.Functions;

public class SetPyramidExp {
	static SetPyramid pyramidSet;

	static double voteThreshold = 0.7;

	public static boolean doParallel = true;

	public static void by_activation_number_single(boolean readSeed, boolean saveSeed) throws IOException, InterruptedException {
		System.out.println("by_activation_number_single");
		int trials = 3;

		int min_activation_number = 1;
		int max_activation_number = 1024;

		int min_pyramidNum = 4;
		int max_pyramidNum = 4;

		String fileOutput = FilePath_Mon.filePathPre + "/Exp_activation_number_single.txt";

		int pyramidSize = 0;
		int activationSize = 0;
		for (int pyramidNum = min_pyramidNum; pyramidNum <= max_pyramidNum; pyramidNum *= 2) {
			pyramidSize++;
		}
		
		for (int activationNum = min_activation_number; activationNum <= max_activation_number; activationNum *= 4) {
			activationSize++;
		}
		
		// [pyramidNum]
		double constructTimes[] = new double[pyramidSize];
		double constructMems[] = new double[pyramidSize];
		// [pyramidNum][activationSize]
		double updateTimes[][] = new double[pyramidSize][activationSize];
		double updateMems[][] = new double[pyramidSize][activationSize];
		
		Runtime r = Runtime.getRuntime();
		
		int pyramidIdx = 0;
		int activationIdx = 0;
		for (int trial = 0; trial < trials; trial++) {
			
			pyramidIdx = 0;
			for (int pyramidNum = min_pyramidNum; pyramidNum <= max_pyramidNum; pyramidNum *= 2) {
				
				r.gc();

				double startMem = (double) r.totalMemory() - (double) r.freeMemory();
				double start = System.currentTimeMillis();

				pyramidSet = new SetPyramid(pyramidNum, Graph.getVertexSize(), voteThreshold);
				
				if (!doParallel) {
					pyramidSet.construct(readSeed, saveSeed, trial);
				} else {
					pyramidSet.construct_parallel(readSeed, saveSeed, trial);
				}

				double end = System.currentTimeMillis();
				double endMem = (double) r.totalMemory() - (double) r.freeMemory();
				
				double runningTime_construct = (end - start) / Constant.RUNNING_TIME_UNIT;
				double rmemory_construct = (endMem - startMem) / Constant.MEMORY_UNIT;
				
				constructTimes[pyramidIdx] += runningTime_construct;
				constructMems[pyramidIdx] += rmemory_construct;
				
				activationIdx = 0;
				for (int activationNum = min_activation_number; activationNum <= max_activation_number; activationNum *= 4) {

					Graph.reLoad();

					String[] strs = doUpdate_byNum_single(activationNum, pyramidNum, readSeed, saveSeed, trial)
							.split(",");

					updateTimes[pyramidIdx][activationIdx] += Double.parseDouble(strs[3]);
					updateMems[pyramidIdx][activationIdx] += Double.parseDouble(strs[4]);
					
//					System.out.println(pyramidNum + "," + activationNum + "," + Double.parseDouble(strs[3]) + "," + Double.parseDouble(strs[4]) + ","
//							+ updateTimes[pyramidIdx][activationIdx] + "," + updateMems[pyramidIdx][activationIdx]);
					
					activationIdx++;
				}
				
				pyramidIdx++;
			}
		}
				
		activationIdx = 0;
		for (int activationNum = min_activation_number; activationNum <= max_activation_number; activationNum *= 4) {
			
			pyramidIdx = 0;
			for (int pyramidNum = min_pyramidNum; pyramidNum <= max_pyramidNum; pyramidNum *= 2) {

				String runningTime_update_print = String.format("%.3f", updateTimes[pyramidIdx][activationIdx] / (double) trials);
				String rmemory_update_print = String.format("%.3f", updateMems[pyramidIdx][activationIdx] / (double) trials);
				String runningTime_construct_print = String.format("%.3f", constructTimes[pyramidIdx] / (double) trials);
				String rmemory_construct_print = String.format("%.3f", constructMems[pyramidIdx] / (double) trials);

				System.out.println("==> " + pyramidNum + "," + activationNum + "," + runningTime_update_print + "," + rmemory_update_print + ","
						+ runningTime_construct_print + "," + rmemory_construct_print);

				try {
					FileWriter fw_user = new FileWriter(fileOutput, true);

					fw_user.write(pyramidNum + "," + activationNum + "," + runningTime_update_print + "," + rmemory_update_print + "," 
							+ runningTime_construct_print + "," + rmemory_construct_print + "\n");

					fw_user.flush();
					fw_user.close();

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return;
				}
				
				pyramidIdx++;
			}
			
			activationIdx++;
		}
	}

	public static void by_activation_number_single_decrease(boolean readSeed, boolean saveSeed) throws IOException, InterruptedException {
		System.out.println("by_activation_number_single_decrease");
		int trials = 3;

		int min_activation_number = 1;
		int max_activation_number = 1024;

		int min_pyramidNum = 4;
		int max_pyramidNum = 4;

		String fileOutput = FilePath_Mon.filePathPre + "/Exp_activation_number_single_decrease.txt";
		
		int pyramidSize = 0;
		int activationSize = 0;
		for (int pyramidNum = min_pyramidNum; pyramidNum <= max_pyramidNum; pyramidNum *= 2) {
			pyramidSize++;
		}
		
		for (int activationNum = min_activation_number; activationNum <= max_activation_number; activationNum *= 4) {
			activationSize++;
		}
		
		// [pyramidNum]
		double constructTimes[] = new double[pyramidSize];
		double constructMems[] = new double[pyramidSize];
		// [pyramidNum][activationSize]
		double updateTimes[][] = new double[pyramidSize][activationSize];
		double updateMems[][] = new double[pyramidSize][activationSize];
		
		Runtime r = Runtime.getRuntime();

		int pyramidIdx = 0;
		int activationIdx = 0;
		for (int trial = 0; trial < trials; trial++) {
			
			pyramidIdx = 0;
			for (int pyramidNum = min_pyramidNum; pyramidNum <= max_pyramidNum; pyramidNum *= 2) {
				
				r.gc();

				double startMem = (double) r.totalMemory() - (double) r.freeMemory();
				double start = System.currentTimeMillis();

				pyramidSet = new SetPyramid(pyramidNum, Graph.getVertexSize(), voteThreshold);
				
				if (!doParallel) {
					pyramidSet.construct(readSeed, saveSeed, trial);
				} else {
					pyramidSet.construct_parallel(readSeed, saveSeed, trial);
				}

				double end = System.currentTimeMillis();
				double endMem = (double) r.totalMemory() - (double) r.freeMemory();
				
				double runningTime_construct = (end - start) / Constant.RUNNING_TIME_UNIT;
				double rmemory_construct = (endMem - startMem) / Constant.MEMORY_UNIT;
				
				constructTimes[pyramidIdx] += runningTime_construct;
				constructMems[pyramidIdx] += rmemory_construct;
				
				activationIdx = 0;
				for (int activationNum = min_activation_number; activationNum <= max_activation_number; activationNum *= 4) {

					Graph.reLoad();

					String[] strs = doUpdate_byNum_single_decrease(activationNum, pyramidNum, readSeed, saveSeed, trial)
							.split(",");

					updateTimes[pyramidIdx][activationIdx] += Double.parseDouble(strs[3]);
					updateMems[pyramidIdx][activationIdx] += Double.parseDouble(strs[4]);
					
					activationIdx++;
				}
				
				pyramidIdx++;
			}
		}
				
		activationIdx = 0;
		for (int activationNum = min_activation_number; activationNum <= max_activation_number; activationNum *= 4) {
			
			pyramidIdx = 0;
			for (int pyramidNum = min_pyramidNum; pyramidNum <= max_pyramidNum; pyramidNum *= 2) {

				String runningTime_update_print = String.format("%.3f", updateTimes[pyramidIdx][activationIdx] / (double) trials);
				String rmemory_update_print = String.format("%.3f", updateMems[pyramidIdx][activationIdx] / (double) trials);
				String runningTime_construct_print = String.format("%.3f", constructTimes[pyramidIdx] / (double) trials);
				String rmemory_construct_print = String.format("%.3f", constructMems[pyramidIdx] / (double) trials);

				System.out.println("==> " + pyramidNum + "," + activationNum + "," + runningTime_update_print + "," + rmemory_update_print + ","
						+ runningTime_construct_print + "," + rmemory_construct_print);

				try {
					FileWriter fw_user = new FileWriter(fileOutput, true);

					fw_user.write(pyramidNum + "," + activationNum + "," + runningTime_update_print + "," + rmemory_update_print + "," 
							+ runningTime_construct_print + "," + rmemory_construct_print + "\n");

					fw_user.flush();
					fw_user.close();

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return;
				}
				
				pyramidIdx++;
			}
			
			activationIdx++;
		}
	}

	public static void by_timeLine_single(int first_version, int last_version, int trials) throws IOException, InterruptedException {
		System.out.println("by_timeLine_single");
		
		ArrayList<Integer> versions = new ArrayList<Integer>();
		for (int version = first_version; version <= last_version; version++) versions.add(version);

		double activation_ratio = 1;

		int min_pyramidNum = 4;
		int max_pyramidNum = 4;

		for (int version : versions) {
			for (int pyramidNum = min_pyramidNum; pyramidNum <= max_pyramidNum; pyramidNum *= 2) {
				doUpdate_byTimeLine_single(activation_ratio, pyramidNum, version, trials);
			}
		}
	}

	public static void by_timeLine_batch(int first_version, int last_version, double ratio, int trials) throws IOException, InterruptedException {
		System.out.println("by_timeLine_batch");
		
		ArrayList<Integer> versions = new ArrayList<Integer>();
		for (int version = first_version; version <= last_version; version++) versions.add(version);

		double activation_ratio = ratio;

		int min_pyramidNum = 4;
		int max_pyramidNum = 4;

		for (int version : versions) {
			for (int pyramidNum = min_pyramidNum; pyramidNum <= max_pyramidNum; pyramidNum *= 2) {
				doUpdate_byTimeLine_batch(activation_ratio, pyramidNum, version, trials);
			}
		}
	}

	public static void updateAndQuery_by_timeLine_batch(int first_version, int last_version, double updateRatio, int trials, double queryRatio) throws IOException, InterruptedException {
		System.out.println("updateAndQuery_by_timeLine_batch");
		
		ArrayList<Integer> versions = new ArrayList<Integer>();
		for (int version = first_version; version <= last_version; version++) versions.add(version);

		double activation_ratio = updateRatio;

		int min_pyramidNum = 4;
		int max_pyramidNum = 4;

		for (int version : versions) {
			for (int pyramidNum = min_pyramidNum; pyramidNum <= max_pyramidNum; pyramidNum *= 2) {
				doUpdateAndQuery_byTimeLine_batch(activation_ratio, queryRatio, pyramidNum, version, trials);
			}
		}
	}
	
	private static String doUpdate_byNum_single(int activation_number, int pyramidNum, boolean readSeed,
			boolean saveSeed, int trial) throws IOException, InterruptedException {
		// get activations
		int[] times = new int[activation_number];
		int[] activeEdges = new int[activation_number];

		String fileInput = FilePath_Mon.filePathPre + "/activations/activeNumSing_" + activation_number + "_trial_"
				+ trial + ".txt";
		Path path = Paths.get(fileInput);
		Scanner scanner = new Scanner(path.toAbsolutePath());

		String line;
		String[] strs;
		int index = 0;
		while (scanner.hasNextLine()) {
			// process each line
			line = scanner.nextLine();
			strs = line.split(",");

			activeEdges[index] = Integer.parseInt(strs[0]);
			times[index] = Integer.parseInt(strs[1]);
			index++;
		}

		scanner.close();

		Runtime r = Runtime.getRuntime();
		
		pyramidSet.reNew_parallel();

		// start
		// for each time stamp
		double totalRunningTime = 0;
		double totalMemory = 0;
		int edgeID, timeStamp;
		for (int timeIndex = 0; timeIndex < times.length; timeIndex++) {

			edgeID = activeEdges[timeIndex];
			timeStamp = times[timeIndex];

			Graph.currentTime = timeStamp;

			r = Runtime.getRuntime();
			r.gc();

			double startMem = (double) r.totalMemory() - (double) r.freeMemory();
			double start = System.currentTimeMillis();

			if (!doParallel) {
				pyramidSet.update(edgeID);
			}

			double endMem = (double) r.totalMemory() - (double) r.freeMemory();
			double end = System.currentTimeMillis();

			totalRunningTime += (end - start);
			totalMemory += (endMem - startMem);
		}

		String runningTime_update_print = String.format("%.3f", totalRunningTime / Constant.RUNNING_TIME_UNIT);
		String rmemory_update_print = String.format("%.3f", totalMemory / Constant.MEMORY_UNIT);

		System.out.println(trial + "," + pyramidNum + "," + activation_number + "," + runningTime_update_print + ","
				+ rmemory_update_print);

		return trial + "," + pyramidNum + "," + activation_number + "," + runningTime_update_print + ","
				+ rmemory_update_print;
	}

	private static String doUpdate_byNum_single_decrease(int activation_number, int pyramidNum, boolean readSeed,
			boolean saveSeed, int trial) throws IOException, InterruptedException {
		// get activations
		int[] times = new int[activation_number];
		int[] activeEdges = new int[activation_number];

		String fileInput = FilePath_Mon.filePathPre + "/activations/activeNumSing_" + activation_number + "_trial_"
				+ trial + ".txt";
		Path path = Paths.get(fileInput);
		Scanner scanner = new Scanner(path.toAbsolutePath());

		String line;
		String[] strs;
		int index = 0;
		while (scanner.hasNextLine()) {
			// process each line
			line = scanner.nextLine();
			strs = line.split(",");

			activeEdges[index] = Integer.parseInt(strs[0]);
			times[index] = Integer.parseInt(strs[1]);
			index++;
		}

		scanner.close();

		Runtime r = Runtime.getRuntime();
		
		pyramidSet.reNew_parallel();
		
		// start
		// for each time stamp
		double totalRunningTime = 0;
		double totalMemory = 0;
		int edgeID, timeStamp;
		for (int timeIndex = 0; timeIndex < times.length; timeIndex++) {

			edgeID = activeEdges[timeIndex];
			timeStamp = times[timeIndex];

			Graph.currentTime = timeStamp;

			r = Runtime.getRuntime();
			r.gc();

			double startMem = (double) r.totalMemory() - (double) r.freeMemory();
			double start = System.currentTimeMillis();

			if (!doParallel) {
				pyramidSet.update_decrease(edgeID);
			} else {
			}

			double endMem = (double) r.totalMemory() - (double) r.freeMemory();
			double end = System.currentTimeMillis();

			totalRunningTime += (end - start);
			totalMemory += (endMem - startMem);
		}

		String runningTime_update_print = String.format("%.3f", totalRunningTime / (double) Constant.RUNNING_TIME_UNIT);
		String rmemory_update_print = String.format("%.3f", totalMemory / (double) Constant.MEMORY_UNIT);

		System.out.println(trial + "," + pyramidNum + "," + activation_number + "," + runningTime_update_print + ","
				+ rmemory_update_print);

		return trial + "," + pyramidNum + "," + activation_number + "," + runningTime_update_print + ","
				+ rmemory_update_print;
	}

	private static void doUpdate_byTimeLine_single(double activation_ratio, int pyramidNum, int version, int trials) throws IOException, InterruptedException {
		String fileInput = FilePath_Mon.filePathPre + "/activations/activeRealBatch_ratio_" + activation_ratio
				+ "_version_" + version + ".txt";
		String fileOutput = FilePath_Mon.filePathPre + "/Exp_activation_timeLine_single_version_" + version + ".txt";

		Runtime r1 = Runtime.getRuntime();
		
		// [trials]
		ArrayList<Double>[] times = new ArrayList[trials];
		ArrayList<Double>[] memories = new ArrayList[trials];
		
		for (int trial = 0; trial < trials; trial++) {
			times[trial] = new ArrayList<Double>();
			memories[trial] = new ArrayList<Double>();
			
			pyramidSet = new SetPyramid(pyramidNum, Graph.getVertexSize(), voteThreshold);
			pyramidSet.construct_parallel(true, false, trial);
			
			Path path = Paths.get(fileInput);
			Scanner scanner = new Scanner(path.toAbsolutePath());
			
			String line;
			String[] strs;
			int edgeID, timeStamp;
			while (scanner.hasNextLine()) {
				// process each line
				line = scanner.nextLine();
				strs = line.split(",");

				edgeID = Integer.parseInt(strs[0]);
				timeStamp = Integer.parseInt(strs[1]);

				if (Graph.currentTime != timeStamp) {
					Graph.currentTime = timeStamp;
				}
				
				double startMem1 = r1.totalMemory() - r1.freeMemory();
				double start1 = System.currentTimeMillis();

				pyramidSet.update_decrease(edgeID);

				double end1 = System.currentTimeMillis();
				double endMem1 = r1.totalMemory() - r1.freeMemory();

				times[trial].add((end1 - start1));
				memories[trial].add((endMem1 - startMem1));
						
				String Time_print = String.format("%.3f",
						((end1 - start1) / (double) Constant.RUNNING_TIME_UNIT));
				String Memory_print = String.format("%.3f",
						((endMem1 - startMem1) / (double) Constant.MEMORY_UNIT));

				System.out.println("trial " + trial + " " +  " " + Time_print + " " + Memory_print);
			}

			scanner.close();
		}
		
		try {
			FileWriter fw_user = new FileWriter(fileOutput, true);
			
			for (int i = 0; i < times[0].size(); i ++) {
				double time = 0;
				double memory = 0;
				
				for (int trial = 0; trial < trials; trial++) {
					time += times[trial].get(i);
					memory += memories[trial].get(i);
				}
				
				time /= trials;
				memory /= trials;
				
				time /= Constant.RUNNING_TIME_UNIT;
				memory /= Constant.MEMORY_UNIT;
				
				fw_user.write(time + "," + memory + "\n");
				fw_user.flush();
			}

			fw_user.flush();
			fw_user.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}

	}

	private static void doUpdate_byTimeLine_batch(double activation_ratio, int pyramidNum, int version, int trials) throws IOException, InterruptedException {
		String fileInput = FilePath_Mon.filePathPre + "/activations/activeRealBatch_ratio_" + activation_ratio
				+ "_version_" + version + ".txt";
		String fileOutput = FilePath_Mon.filePathPre + "/Exp_activation_timeLine_batch_version_" + version + ".txt";
		
		Runtime r1 = Runtime.getRuntime();
		
		// get activations
		List<Integer> times = new ArrayList<Integer>();
		List<List<Integer>> activations = new ArrayList<List<Integer>>();

		Path path = Paths.get(fileInput);
		Scanner scanner = new Scanner(path.toAbsolutePath());

		String line;
		String[] strs;
		int edgeID, timeStamp;
		while (scanner.hasNextLine()) {
			// process each line
			line = scanner.nextLine();
			strs = line.split(",");

			edgeID = Integer.parseInt(strs[0]);
			timeStamp = Integer.parseInt(strs[1]);

			if (timeStamp > activations.size()) {
				times.add(timeStamp);
//				System.out.println(timeStamp);
				activations.add(new ArrayList<Integer>());
			}

			activations.get(activations.size() - 1).add(edgeID);
		}

		scanner.close();

		System.out.println("times " + times.size() + " " + activations.size());

		// [trials]
		ArrayList<Double>[] runningTimes = new ArrayList[trials];
		ArrayList<Double>[] memories = new ArrayList[trials];
		
		try {
			FileWriter fw_user = new FileWriter(fileOutput, true);
			
			for (int trial = 0; trial < trials; trial++) {
				runningTimes[trial] = new ArrayList<Double>();
				memories[trial] = new ArrayList<Double>();
				
				Graph.reLoad();
				
				pyramidSet = new SetPyramid(pyramidNum, Graph.getVertexSize(), voteThreshold);
				pyramidSet.construct_parallel(true, false, trial);
				
				int counter = 0;
				for (int timeIndex = 0; timeIndex < times.size(); timeIndex++) {
	
					timeStamp = times.get(timeIndex);
	
					Graph.currentTime = timeStamp;
	
					double startMem1 = r1.totalMemory() - r1.freeMemory();
					double start1 = System.currentTimeMillis();
		
					for (int edge : activations.get(timeIndex)) {
						pyramidSet.update_decrease(edge);
					}
		
					double end1 = System.currentTimeMillis();
					double endMem1 = r1.totalMemory() - r1.freeMemory();
					
					runningTimes[trial].add((end1 - start1));
					memories[trial].add((endMem1 - startMem1));
					
					String RunningTime_print = String.format("%.3f",
							((end1 - start1) / (double) Constant.RUNNING_TIME_UNIT));
					String Memory_print = String.format("%.3f",
							((endMem1 - startMem1) / (double) Constant.MEMORY_UNIT));
		
					System.out.println(trial + " " + counter++ + " edge number " + activations.get(timeIndex).size() + " "
							+ RunningTime_print + " " + Memory_print);
					
					fw_user.write(trial + " " + counter + " edge number " + activations.get(timeIndex).size() + " "
							+ RunningTime_print + " " + Memory_print + "\n");
					fw_user.flush();
				}
			}

			for (int i = 0; i < runningTimes[0].size(); i ++) {
				double time = 0;
				double memory = 0;
				
				for (int trial = 0; trial < trials; trial++) {
					time += runningTimes[trial].get(i);
					memory += memories[trial].get(i);
				}
				
				time /= trials;
				memory /= trials;
				
				time /= Constant.RUNNING_TIME_UNIT;
				memory /= Constant.MEMORY_UNIT;
				
				fw_user.write(time + "," + memory + "\n");
				fw_user.flush();
			}

			fw_user.flush();
			fw_user.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
	}

	
	private static void doUpdateAndQuery_byTimeLine_batch(double activation_ratio, double queryRatio, int pyramidNum, int version, int trials) throws IOException, InterruptedException {
			
		Runtime r1 = Runtime.getRuntime();
		
		int maxLevel = 20;
		int minLevel = maxLevel - 2;
		
		int queryNodeVersion = 0;
				
		String fileInput = "";
		String fileOutput = FilePath_Mon.filePathPre + "/Exp_activationAndQuery_timeLine_batch_queryRatio_" + queryRatio + "_queryVersion_" + queryNodeVersion + "_minLevel_" + minLevel + "_maxLevel_" + maxLevel + "_version_" + version + ".txt";
		
		// get times
		List<Integer> times = new ArrayList<Integer>();
		
		// get activations
		List<List<Integer>> activations = new ArrayList<List<Integer>>();
		int totalActivations = 0;
		int activeSize = 0;
		
		// get query
		List<List<Integer>> queries = new ArrayList<List<Integer>>();
		List<List<Integer>> levels = new ArrayList<List<Integer>>();
		int querySize = 0;
		
		boolean save = false;
		if (save) {
			
			// read in activations
			totalActivations = 0;
			
			fileInput = FilePath_Mon.filePathPre + "/activations/activeRealBatch_ratio_" + activation_ratio + "_version_" + version + ".txt";
			Path path = Paths.get(fileInput);
			Scanner scanner = new Scanner(path.toAbsolutePath());

			String line;
			String[] strs;
			int edgeID, timeStamp;
			while (scanner.hasNextLine()) {
				// process each line
				line = scanner.nextLine();
				strs = line.split(",");

				edgeID = Integer.parseInt(strs[0]);
				timeStamp = Integer.parseInt(strs[1]);

				if (timeStamp > activations.size()) {
					times.add(timeStamp);
					activations.add(new ArrayList<Integer>());
				}

				activations.get(activations.size() - 1).add(edgeID);
				
				totalActivations++;
				activeSize++;
			}
			scanner.close();

			System.out.println("times " + times.size() + " " + activations.size() + " total activations " + totalActivations);
			
			// sample query nodes
			int[] queryNodeSizes = new int[times.size()];
			
			for (int timeIdx = 0; timeIdx < times.size(); timeIdx++) {
				queries.add(new ArrayList<Integer>());
				levels.add(new ArrayList<Integer>());
			}
			
			int totalQueryNodeSize = (int) Math.ceil(totalActivations * queryRatio);
			
			while (querySize < totalQueryNodeSize) {
				// sample node
				int nodeID = Functions.randInt(0, Graph.getVertexSize() - 1);
				int level = Functions.randInt(minLevel, maxLevel);
				int timeIdx = Functions.randInt(0, times.size() - 1);
				
				queryNodeSizes[timeIdx]++;
				
				queries.get(timeIdx).add(nodeID);
				levels.get(timeIdx).add(level);
				
				querySize++;
			}
			
			// remove activations
			for (int timeIdx = 0; timeIdx < times.size(); timeIdx++) {
				int needRemoveCnt = queryNodeSizes[timeIdx];
				
//				System.out.println("time " + times.get(timeIdx) + " "
//						+ "query " + queryNodeSizes[timeIdx] + " activation " + activations.get(timeIdx).size() + " need remove " + needRemoveCnt);
				
				while (needRemoveCnt > 0) {
					int activationSize = activations.get(timeIdx).size();
					if (activationSize <= 1) break;
					
					int idx = Functions.randInt(0, activationSize - 1);
					activations.get(timeIdx).remove(idx);
					needRemoveCnt--;
					
					activeSize--;
				}
				
//				System.out.println("time " + times.get(timeIdx) + " "
//						+ "query " + queryNodeSizes[timeIdx] + " activation " + activations.get(timeIdx).size() + " need remove " + needRemoveCnt);
			}
			
			// save to file
			try {
				// save query nodes
				FileWriter fw_user = new FileWriter(FilePath_Mon.filePathPre + "/activations/queryNodes_queryRatio_" + queryRatio + "_version_" + queryNodeVersion + ".txt");

				List<Integer> query = new ArrayList<Integer>();
				List<Integer> level = new ArrayList<Integer>();
				
				for (int timeIdx = 0; timeIdx < times.size(); timeIdx++) {
					
					int time = times.get(timeIdx);
					query = queries.get(timeIdx);
					level = levels.get(timeIdx);
					
					for (int i = 0; i < query.size(); i ++) {
						fw_user.write(time + "," + query.get(i) + "," + level.get(i) + "\n");
					}
				}

				fw_user.flush();
				fw_user.close();
				
				// save activations
				fw_user = new FileWriter(FilePath_Mon.filePathPre + "/activations/activeRealBatch_ratio_" + activation_ratio + "_version_" + version + "_queryRatio_" + queryRatio + "_version_" + queryNodeVersion + ".txt");
				
				List<Integer> actives = new ArrayList<Integer>();
				
				for (int timeIdx = 0; timeIdx < times.size(); timeIdx++) {
					
					int time = times.get(timeIdx);
					actives = activations.get(timeIdx);
					
					for (int i = 0; i < actives.size(); i ++) {
						fw_user.write(actives.get(i) + "," + time + "\n");
					}
				}

				fw_user.flush();
				fw_user.close();
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			}
			
		} else {
			// read in activations
			fileInput = FilePath_Mon.filePathPre + "/activations/activeRealBatch_ratio_" + activation_ratio + "_version_" + version + "_queryRatio_" + queryRatio + "_version_" + queryNodeVersion + ".txt";
			Path path = Paths.get(fileInput);
			Scanner scanner = new Scanner(path.toAbsolutePath());

			String line;
			String[] strs;
			int edgeID, timeStamp;
			while (scanner.hasNextLine()) {
				// process each line
				line = scanner.nextLine();
				strs = line.split(",");

				edgeID = Integer.parseInt(strs[0]);
				timeStamp = Integer.parseInt(strs[1]);

				if (timeStamp > activations.size()) {
					times.add(timeStamp);
					activations.add(new ArrayList<Integer>());
				}

				activations.get(activations.size() - 1).add(edgeID);
				
				activeSize++;
			}
			scanner.close();
						
			// read in query 
			
			for (int timeIdx = 0; timeIdx < times.size(); timeIdx++) {
				queries.add(new ArrayList<Integer>());
				levels.add(new ArrayList<Integer>());
			}
			
			path = Paths.get(FilePath_Mon.filePathPre + "/activations/queryNodes_queryRatio_" + queryRatio + "_version_" + queryNodeVersion + ".txt");
			scanner = new Scanner(path.toAbsolutePath());

			int nodeID;
			int level;
			int timeIdx;
			while (scanner.hasNextLine()) {
				// process each line
				line = scanner.nextLine();
				strs = line.split(",");

				timeIdx = times.indexOf(Integer.parseInt(strs[0]));
				nodeID = Integer.parseInt(strs[1]);
				level = Integer.parseInt(strs[2]);

				queries.get(timeIdx).add(nodeID);
				levels.get(timeIdx).add(level);
				
				querySize++;
			}
			scanner.close();
		}
		
		System.out.println("query node " + querySize + " activation size " + activeSize + " total activation size " + totalActivations);

		// [trials]
		ArrayList<Double>[] updateTimes = new ArrayList[trials];
		ArrayList<Double>[] memories = new ArrayList[trials];
		ArrayList<Double>[] queryTimes = new ArrayList[trials];
		ArrayList<Double>[] avgClusterSizes = new ArrayList[trials];
		
		try {
			FileWriter fw_user = new FileWriter(fileOutput, true);
			
			for (int trial = 0; trial < trials; trial++) {
				
				Graph.reLoad();
				
				updateTimes[trial] = new ArrayList<Double>();
				memories[trial] = new ArrayList<Double>();
				queryTimes[trial] = new ArrayList<Double>();
				avgClusterSizes[trial] = new ArrayList<Double>();
				
				pyramidSet = new SetPyramid(pyramidNum, Graph.getVertexSize(), voteThreshold);
				pyramidSet.construct_parallel(true, false, trial);
				
				int counter = 0;
				for (int timeIndex = 0; timeIndex < times.size(); timeIndex++) {
	
					int timeStamp = times.get(timeIndex);
	
					Graph.currentTime = timeStamp;
	
					// update
					double startMem = r1.totalMemory() - r1.freeMemory();
					double startTime = System.currentTimeMillis();
		
					for (int edge : activations.get(timeIndex)) {
						pyramidSet.update_decrease(edge);
					}
		
					double endTime = System.currentTimeMillis();
					double endMem = r1.totalMemory() - r1.freeMemory();
					
					updateTimes[trial].add((endTime - startTime));
					memories[trial].add((endMem - startMem));
					
					String updateTime_print = String.format("%.8f",
							((endTime - startTime) / (double) Constant.RUNNING_TIME_UNIT));
					String Memory_print = String.format("%.8f",
							((endMem - startMem) / (double) Constant.MEMORY_UNIT));
					
					System.out.println(trial + " " + counter + " edge number " + activations.get(timeIndex).size() + " "
							+ updateTime_print + " " + Memory_print);
					
					// query
					List<Integer> query = new ArrayList<Integer>();
					List<Integer> level = new ArrayList<Integer>();
					query = queries.get(timeIndex);
					level = levels.get(timeIndex);
					
					double queryTime = 0;
					int clusterNum = 0;
					
					for (int i = 0; i < query.size(); i ++) {
						String result = pyramidSet.query(query.get(i), level.get(i));
						
						String[] strs = result.split(",");
						queryTime += Double.parseDouble(strs[1]);
						clusterNum += Integer.parseInt(strs[0]);
					}
		
					queryTimes[trial].add(queryTime);
					
					if (query.size() > 0) avgClusterSizes[trial].add(((double) clusterNum / (double) query.size()));
					else avgClusterSizes[trial].add((double) Double.NaN);
							
					String queryTime_print = String.format("%.8f",
							(queryTime / Constant.RUNNING_TIME_UNIT));
					
					String avgClusterNum_print = String.format("%.8f",
							((double) clusterNum / (double) query.size()));
					if (query.size() <= 0) avgClusterNum_print = Double.NaN + "";
					
					System.out.println(trial + " " + counter++ + " query number " + query.size() + " "
							+ queryTime_print + " " + avgClusterNum_print);
					
					fw_user.write(trial + " " + counter + " edge number " + activations.get(timeIndex).size() + " "
							+ updateTime_print + " " + Memory_print + " query number " + query.size() + " " + queryTime_print + " " + avgClusterNum_print + "\n");
					
					fw_user.flush();
				}
			}

			for (int i = 0; i < updateTimes[0].size(); i ++) {
				double updateTime = 0;
				double memory = 0;
				double queryTime = 0;
				double avgClusterNum = 0;
				int clusterNumCnt = 0;
				
				for (int trial = 0; trial < trials; trial++) {
					updateTime += updateTimes[trial].get(i);
					memory += memories[trial].get(i);
					queryTime += queryTimes[trial].get(i);
					
					if (!Double.isNaN(avgClusterSizes[trial].get(i))) {
						avgClusterNum += avgClusterSizes[trial].get(i);
						clusterNumCnt++;
					}
				}
				
				updateTime /= trials;
				memory /= trials;
				queryTime /= trials;
				if (clusterNumCnt > 0) avgClusterNum /= clusterNumCnt;
				else avgClusterNum = Double.NaN;
				
				updateTime /= Constant.RUNNING_TIME_UNIT;
				memory /= Constant.MEMORY_UNIT;
				queryTime /= Constant.RUNNING_TIME_UNIT;
				
				fw_user.write(updateTime + "," + memory + "," + queryTime + "," + avgClusterNum + "\n");
				fw_user.flush();
			}

			fw_user.flush();
			fw_user.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
	}

	private static void syntheticActivation_byNum_batch(int maxTime, int activation_number, int trial) {
		String fileOutput = FilePath_Mon.filePathPre + "/activations/activeNumBatch_" + activation_number + "_trial_" + trial + ".txt";

		try {
			FileWriter fw_user = new FileWriter(fileOutput);

			for (int time = 1; time <= maxTime; time++) {
				for (int count = 0; count < activation_number; count++) {
					fw_user.write(Functions.randInt(0, Graph.getEdgeSize() - 1) + "," + time + "\n");
				}
			}

			fw_user.flush();
			fw_user.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
	}

	private static void syntheticActivation_byNum_single(int activation_number, int trial) {
		String fileOutput = FilePath_Mon.filePathPre + "/activations/activeNumSing_" + activation_number + "_trial_"
				+ trial + ".txt";

		try {
			FileWriter fw_user = new FileWriter(fileOutput);

			for (int time = 1; time <= activation_number; time++) {
				fw_user.write(Functions.randInt(0, Graph.getEdgeSize() - 1) + "," + time + "\n");
			}

			fw_user.flush();
			fw_user.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
	}

	public static void createSyntheticActivations_byNum_batch() {
		int min_activation_number = 1;
		int max_activation_number = 10000;
		int trials = 8;
		int maxTime = 30;

		for (int activation_number = max_activation_number; activation_number >= min_activation_number; activation_number /= 10) {
			for (int trial = 0; trial < trials; trial++) {
				syntheticActivation_byNum_batch(maxTime, activation_number, trial);
			}
		}
	}

	public static void createSyntheticActivations_byNum_single() {
		int min_activation_number = 1;
		int max_activation_number = 20000;
		int trials = 8;

		for (int activation_number = min_activation_number; activation_number <= max_activation_number; activation_number *= 4) {
			for (int trial = 0; trial < trials; trial++) {
				syntheticActivation_byNum_single(activation_number, trial);
			}
		}
	}

	public static void createPyramidSets(int minPyramidNum, int maxPyramidNum, int time, int trials) throws IOException, InterruptedException {
		String fileOutput = FilePath_Mon.filePathPre + "/Exp_construct_pyramid_byNum.txt";

		ArrayList<Integer> pyramidNums = new ArrayList<Integer>();
		for (int pyramidNum = minPyramidNum; pyramidNum <= maxPyramidNum; pyramidNum *= time) {
			pyramidNums.add(pyramidNum);
		}

		boolean readSeed = false;
		boolean saveSeed = false;

		for (int pyramidNum : pyramidNums) {
			double RunningTime = 0;
			double Memory = 0;

			for (int trial = 0; trial < trials; trial++) {

				Graph.reLoad();

				Runtime r = Runtime.getRuntime();
				r.gc();

				double startMem = (double) r.totalMemory() - (double) r.freeMemory();
				double start = System.currentTimeMillis();

				pyramidSet = new SetPyramid(pyramidNum, Graph.getVertexSize(), voteThreshold);
				pyramidSet.construct(readSeed, saveSeed, trial);

				double endMem = (double) r.totalMemory() - (double) r.freeMemory();
				double end = System.currentTimeMillis();

				String runningTime_construct_print = String.format("%.3f", (end - start) / (double) Constant.RUNNING_TIME_UNIT);
				String rmemory_construct_print = String.format("%.3f", (endMem - startMem) / (double) Constant.MEMORY_UNIT);

				System.out.println(trial + "," + pyramidNum + "," + runningTime_construct_print + "," + rmemory_construct_print);

				RunningTime += (end - start);
				Memory += (endMem - startMem);
			}

			RunningTime /= (double) trials;
			Memory /= (double) trials;

			String RunningTime_print = String.format("%.3f", (RunningTime / (double) Constant.RUNNING_TIME_UNIT));
			String Memory_print = String.format("%.3f", (Memory / (double) Constant.MEMORY_UNIT));

			System.out.println("==> " + pyramidNum + "," + RunningTime_print + "," + Memory_print);

			try {
				FileWriter fw_user = new FileWriter(fileOutput, true);

				fw_user.write(pyramidNum + "," + RunningTime_print + "," + Memory_print + "\n");

				fw_user.flush();
				fw_user.close();

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			}
		}
	}
	
	public static void createPyramidSets_parallel(int minPyramidNum, int maxPyramidNum, int time, int trials) throws IOException, InterruptedException {
		String fileOutput = FilePath_Mon.filePathPre + "/Exp_construct_pyramid_byNum_parallel.txt";

		ArrayList<Integer> pyramidNums = new ArrayList<Integer>();
		for (int pyramidNum = minPyramidNum; pyramidNum <= maxPyramidNum; pyramidNum *= time) {
			pyramidNums.add(pyramidNum);
		}

		boolean readSeed = false;
		boolean saveSeed = false;

		for (int pyramidNum : pyramidNums) {
			double RunningTime = 0;
			double Memory = 0;

			for (int trial = 0; trial < trials; trial++) {

				Graph.reLoad();

				Runtime r = Runtime.getRuntime();
				r.gc();

				double startMem = (double) r.totalMemory() - (double) r.freeMemory();
				double start = System.currentTimeMillis();

				pyramidSet = new SetPyramid(pyramidNum, Graph.getVertexSize(), voteThreshold);
				pyramidSet.construct_parallel(readSeed, saveSeed, trial);

				double endMem = (double) r.totalMemory() - (double) r.freeMemory();
				double end = System.currentTimeMillis();

				String runningTime_construct_print = String.format("%.3f", (end - start) / (double) Constant.RUNNING_TIME_UNIT);
				String rmemory_construct_print = String.format("%.3f", (endMem - startMem) / (double) Constant.MEMORY_UNIT);

				System.out.println(trial + "," + pyramidNum + "," + runningTime_construct_print + "," + rmemory_construct_print);

				RunningTime += (end - start);
				Memory += (endMem - startMem);
			}

			RunningTime /= (double) trials;
			Memory /= (double) trials;

			String RunningTime_print = String.format("%.3f", (RunningTime / (double) Constant.RUNNING_TIME_UNIT));
			String Memory_print = String.format("%.3f", (Memory / (double) Constant.MEMORY_UNIT));

			System.out.println("==> " + pyramidNum + "," + RunningTime_print + "," + Memory_print);

			try {
				FileWriter fw_user = new FileWriter(fileOutput, true);

				fw_user.write(pyramidNum + "," + RunningTime_print + "," + Memory_print + "\n");

				fw_user.flush();
				fw_user.close();

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			}
		}
	}
	
	public static void pyramidSize(int minPyramidNum, int maxPyramidNum, int time, int trials) throws IOException, InterruptedException {
		String fileOutput = FilePath_Mon.filePathPre + "/Exp_pyramid_size_byNum.txt";

		ArrayList<Integer> pyramidNums = new ArrayList<Integer>();
		for (int pyramidNum = minPyramidNum; pyramidNum <= maxPyramidNum; pyramidNum *= time) {
			pyramidNums.add(pyramidNum);
		}

		boolean readSeed = false;
		boolean saveSeed = false;

		for (int pyramidNum : pyramidNums) {
			Runtime r = Runtime.getRuntime();
			r.gc();
			
			double Memory = 0;

			for (int trial = 0; trial < trials; trial++) {

				r.gc();

				double startMem = (double) r.totalMemory() - (double) r.freeMemory();

				SetPyramid pyramidSet = new SetPyramid(pyramidNum, Graph.getVertexSize(), voteThreshold);
				pyramidSet.construct(readSeed, saveSeed, trial);
				pyramidSet.freeMemory();

				r.gc();
				
				double endMem = (double) r.totalMemory() - (double) r.freeMemory();

				String rmemory_construct_print = String.format("%.3f", (endMem - startMem) / (double) Constant.MEMORY_UNIT);

				System.out.println(trial + "," + pyramidNum + "," + rmemory_construct_print);

				Memory += (endMem - startMem);
			}

			Memory /= (double) trials;

			String Memory_print = String.format("%.3f", (Memory / (double) Constant.MEMORY_UNIT));

			System.out.println("==> " + pyramidNum + "," + Memory_print);

			try {
				FileWriter fw_user = new FileWriter(fileOutput, true);

				fw_user.write(pyramidNum + "," + Memory_print + "\n");

				fw_user.flush();
				fw_user.close();

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			}
		}
	}
	
	public static void main(String arg[]) throws IOException, InterruptedException {
		Graph.loadGraph();

//		createSyntheticActivations_byNum_single();
//		by_activation_number_single(true, false);
//		by_activation_number_single_decrease(true, false);

//		createSyntheticActivations_byNum_batch();
//		by_activation_number_batch(false, false);

//		createPyramidSets();

//		by_timeLine_single(21,21,3);

//		by_timeLine_batch();
	}
}

