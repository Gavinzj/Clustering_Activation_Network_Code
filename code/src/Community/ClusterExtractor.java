package Community;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

import Graph.Graph;
import Utilities.Constant;
import Utilities.FilePath_Mon;

public class ClusterExtractor {
	private static String fileOutput_extractedClusterPre = FilePath_Mon.filePathPre + "/extractedCluster/";

	private static SetPyramid setPyramid;

	private static boolean save = true;

	// order for finding clusterings
	private static int[] vertexInOrder;

	private static Extractor extractor;
	private static CountDownLatch latch;
	private static boolean doParallel = true;
	
	private static double extractClusterTime = 0;

	private static class Extractor extends Thread {
		private int iterationStrategy;
		private int level;
		private double voteThreshold;
		private int itrTimes;
		private int trial;

		Extractor(int iterationStrategy, int level, double voteThreshold, int itrTimes, int trial)
				throws InterruptedException {
			this.iterationStrategy = iterationStrategy;
			this.level = level;
			this.voteThreshold = voteThreshold;
			this.itrTimes = itrTimes;
			this.trial = trial;
			start();
		}

		public void run() {

			boolean[] checked = new boolean[Graph.getVertexSize()];
			int[] leaders = new int[Graph.getVertexSize()];
			Arrays.fill(leaders, -1);

			int leader;
			int clusterNum = 0;
			HashMap<Integer, Integer> clusterToIdx = new HashMap<Integer, Integer>();
			for (int u : vertexInOrder) {

				if (!checked[u]) { // u will be the leader
					leader = u;

					if (save) {
						clusterToIdx.put(leader, clusterNum);
					}

					clusterNum++;

					checked[u] = true;
					leaders[u] = leader;

					// for each of its neighbor
					for (int v : Graph.ADJ_vID[u]) {

						if (!checked[v]) {
							if (setPyramid.sameLabel(u, v, level)) {
								checked[v] = true;
								leaders[v] = leader;
							}
						}
					}

				} else { // the leader will be the leader of u
					leader = leaders[u];

					// for each of its neighbors
					for (int v : Graph.ADJ_vID[u]) {

						if (!checked[v]) {
							if (setPyramid.sameLabel(u, v, level)) {
								checked[v] = true;
								leaders[v] = leader;
							}
						}
					}
				}
			}

			if (save) {
				List<Integer>[] clusters = new ArrayList[clusterNum];

				for (int i = 0; i < clusters.length; i++) {
					clusters[i] = new ArrayList<Integer>();
				}

				for (int i = 0; i < leaders.length; i++) {
					clusters[clusterToIdx.get(leaders[i])].add(i);
				}

//				System.out.println("Number of cluster " + clusterNum + " at level " + level);
				
				try {
					FileWriter fwCount = new FileWriter(fileOutput_extractedClusterPre + "voteThreshold_"
							+ voteThreshold + "_strategy_" + iterationStrategy + "_level_" + level + "_itrTimes_"
							+ itrTimes + "_trial_" + trial + ".txt");

					for (int i = 0; i < clusters.length; i++) {
						fwCount.write(clusters[i].stream().map(Object::toString).collect(Collectors.joining("\t")) + "\n");
					}

					fwCount.flush();
					fwCount.close();

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return;
				}
			}

			latch.countDown();
		}
	}

	public static int extractCluster(int iterationStrategy, int level, double voteThreshold, int itrTimes, int trial)
			throws IOException {
		boolean[] checked = new boolean[Graph.getVertexSize()];
		int[] leaders = new int[Graph.getVertexSize()];
		Arrays.fill(leaders, -1);

		int leader;
		int clusterNum = 0;
		HashMap<Integer, Integer> clusterToIdx = new HashMap<Integer, Integer>();
		
		double startTime = System.currentTimeMillis();
		
		for (int u : vertexInOrder) {

			if (!checked[u]) { // u will be the leader
				leader = u;

				if (save) {
					clusterToIdx.put(leader, clusterNum);
				}

				clusterNum++;

				checked[u] = true;
				leaders[u] = leader;

				// for each of its neighbor
				for (int v : Graph.ADJ_vID[u]) {

					if (!checked[v]) {
						if (setPyramid.sameLabel(u, v, level)) {
							checked[v] = true;
							leaders[v] = leader;
						}
					}
				}

			} else { // the leader will be the leader of u
				leader = leaders[u];

				// for each of its neighbors
				for (int v : Graph.ADJ_vID[u]) {

					if (!checked[v]) {
						if (setPyramid.sameLabel(u, v, level)) {
							checked[v] = true;
							leaders[v] = leader;
						}
					}
				}
			}
		}
		
		double endTime = System.currentTimeMillis();
		
		extractClusterTime = endTime - startTime;

		if (save) {
			List<Integer>[] clusters = new ArrayList[clusterNum];

			for (int i = 0; i < clusters.length; i++) {
				clusters[i] = new ArrayList<Integer>();
			}

			for (int i = 0; i < leaders.length; i++) {
				clusters[clusterToIdx.get(leaders[i])].add(i);
			}

//			System.out.println("Number of cluster " + clusterNum + " at level " + level);
			
			try {
				FileWriter fwCount = new FileWriter(fileOutput_extractedClusterPre + "voteThreshold_"
						+ voteThreshold + "_strategy_" + iterationStrategy + "_level_" + level + "_itrTimes_"
						+ itrTimes + "_trial_" + trial + ".txt");

				for (int i = 0; i < clusters.length; i++) {
					fwCount.write(clusters[i].stream().map(Object::toString).collect(Collectors.joining("\t")) + "\n");
				}

				fwCount.flush();
				fwCount.close();

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return clusterNum;
			}
		}
		
		return clusterNum;
	}

	public static void doClustering(int pyramidNum, double minThreshold, double maxThreshold, double stepSize,
			int iterations, int iterationStrategy, int trials) throws IOException, InterruptedException {

		save = true;

		ordering();

		Iterate.init(iterationStrategy);

		int maxLevel = (int) (Math.log(Graph.getVertexSize()) / Math.log(2));
		int thresholdNum = (int) ((maxThreshold - minThreshold) / stepSize) + 1;

		fileOutput_extractedClusterPre = FilePath_Mon.filePathPre + "/extractedCluster/degree order/";

		// [trials][iterations + 1]
		double[][] iterationRunningTimes = new double[trials][iterations + 1];

		for (int trial = 0; trial < trials; trial++) {

			System.out.println("itrTimes " + 0);
			
			Graph.reLoad();

			/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

			for (int thresholdIdx = 0; thresholdIdx < thresholdNum; thresholdIdx++) {
				double voteThreshold = Double
						.parseDouble(String.format("%.1f", (minThreshold + thresholdIdx * stepSize)));

				System.out.println("voteThreshold " + voteThreshold);

				setPyramid = new SetPyramid(pyramidNum, Graph.getVertexSize(), voteThreshold);
				setPyramid.construct_parallel(true, false, trial);

				if (doParallel) {
					latch = new CountDownLatch(maxLevel + 1);
					for (int level = 0; level <= maxLevel; level++) {
						extractor = new Extractor(iterationStrategy, level, voteThreshold, 0, trial);
					}
					latch.await();
				} else {
					for (int level = 0; level <= 0; level++) {
						extractCluster(iterationStrategy, level, voteThreshold, 0, trial);
					}
				}
				
				
				if (iterations >= 1) {
					int minItr = 1;
					int maxItr = iterations;
					for (int itrTimes = minItr; itrTimes <= maxItr; itrTimes += 2) {
						System.out.println("itrTimes " + itrTimes);

						Graph.reLoad();

						double time = Iterate.iterate_upadteAll_parallel(itrTimes);

						iterationRunningTimes[trial][itrTimes] = time;

						///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

						setPyramid.reNew_parallel();

						if (doParallel) {
							latch = new CountDownLatch(maxLevel + 1);
							for (int level = 0; level <= maxLevel; level++) {
								extractor = new Extractor(iterationStrategy, level, voteThreshold, itrTimes, trial);
							}
							latch.await();
						} else {
							for (int level = 0; level <= maxLevel; level++) {
								extractCluster(iterationStrategy, level, voteThreshold, itrTimes, trial);
							}
						}
					}
				}
			}
		}

		try {
			FileWriter fwCount = new FileWriter(
					fileOutput_extractedClusterPre + "itrRunTime_itrTimes_" + iterations + "_trial_" + trials + ".txt");

			int minItr = 1;
			int maxItr = iterations;
			for (int itrTimes = minItr; itrTimes <= maxItr; itrTimes += 2) {
				double time = 0;

				for (int trial = 0; trial < trials; trial++) {
					time += iterationRunningTimes[trial][itrTimes];
				}

				time /= trials;

				fwCount.write(itrTimes + "," + time);

				System.out.println("itrTimes " + itrTimes + " time " + time);
			}

			fwCount.flush();
			fwCount.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
	}

	public static void doClustering_Active(int pyramidNum, double voteThreshold, int iterations, int iterationStrategy,
			int trials_Pyramid, int minTime, int maxTime, int timeStep, int trials_SC) throws IOException, InterruptedException {

		save = true;

		ordering();

		Iterate.init(iterationStrategy);

		int maxLevel = (int) (Math.log(Graph.getVertexSize()) / Math.log(2));
		
		HashSet<Integer>[][] activeEdges = new HashSet[trials_SC][maxTime + 1];
		for (int trial = 0; trial < trials_SC; trial++) {
			for (int time = 0; time <= maxTime; time++) {
				activeEdges[trial][time] = new HashSet<Integer>();
			}
		}
		
		// read in
		for (int trial = 0; trial < trials_SC; trial++) {
			String fileInput = FilePath_Mon.filePathPre + "/weightedGraph/activeNumSing_maxTime_" + maxTime + "_trial_" + trial + ".txt";
			
			Path path = Paths.get(fileInput);
			Scanner scanner = new Scanner(path.toAbsolutePath());
			
		    // read  the file line by line
			String line;
			String[] strs;
			int edgeID;
			int time;
			while(scanner.hasNextLine()){
				
			    // process each line
			    line = scanner.nextLine();
			    strs = line.split(",");
			    
			    edgeID = Integer.parseInt(strs[0]);
			    time = Integer.parseInt(strs[1]);
			    
			    activeEdges[trial][time].add(edgeID);
			}
			
			scanner.close();
		}
		
		ArrayList<Integer> activeTimes = new ArrayList<Integer>();
		for (int activeTime = minTime; activeTime <= maxTime; activeTime += timeStep) {
			activeTimes.add(activeTime);
		}
		
		double[][][][] updateTime = new double[trials_SC][iterations + 1][activeTimes.size()][trials_Pyramid];
		double[][][][] clusteringTime = new double[trials_SC][iterations + 1][activeTimes.size()][trials_Pyramid];
		double startTime = System.currentTimeMillis();
		double endTime = System.currentTimeMillis();
		
		for (int trial_SC = 0; trial_SC < trials_SC; trial_SC++) {
			
			for (int trial_Pyramid = 0; trial_Pyramid < trials_Pyramid; trial_Pyramid++) {

				int activeTimeIdx = 0;
				
				for (int activeTime = minTime; activeTime <= maxTime; activeTime += timeStep) {

					fileOutput_extractedClusterPre = FilePath_Mon.filePathPre
							+ "/extractedCluster/degree order/active/SCTrial_" + trial_SC + "/timeActive_" + activeTime + "_";

					Graph.loadGraph_weighted("perTime/edge_active_time_" + activeTime + "_trial_" + trial_SC + ".txt");
					Graph.reLoad();

					System.out.println("itrTimes " + 0);

					/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

					setPyramid = new SetPyramid(pyramidNum, Graph.getVertexSize(), voteThreshold);
					setPyramid.construct_parallel(true, false, trial_Pyramid);
					
					for (int level = 0; level <= maxLevel; level++) {
						extractCluster(iterationStrategy, level, voteThreshold, 0, trial_Pyramid);
						
						clusteringTime[trial_SC][0][activeTimeIdx][trial_Pyramid] +=  (extractClusterTime / (double)(maxLevel + 1));
					}
					
					/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

					if (iterations >= 1) {
						for (int itrTimes = 1; itrTimes <= iterations; itrTimes += 2) {
							System.out.println("itrTimes " + itrTimes);

							Graph.reLoad();
							
							///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

							startTime = System.currentTimeMillis();
							
							Iterate.iterate_upadteAll_parallel(itrTimes);
							
							setPyramid.reNew_parallel();
							
							endTime = System.currentTimeMillis();
							
							updateTime[trial_SC][itrTimes][activeTimeIdx][trial_Pyramid] += (endTime - startTime);
							
							for (int level = 0; level <= maxLevel; level++) {
								extractCluster(iterationStrategy, level, voteThreshold, itrTimes, trial_Pyramid);
								
								clusteringTime[trial_SC][itrTimes][activeTimeIdx][trial_Pyramid] +=  (extractClusterTime / (double)(maxLevel + 1));
							}
						}
					}
					
					activeTimeIdx++;
				}
			}
		}
		
		for (int trial_SC = 0; trial_SC < trials_SC; trial_SC++) {
			
			String ouputFile = FilePath_Mon.filePathPre
					+ "/extractedCluster/degree order/active/SCTrial_" + trial_SC + "/runningTime_discoverActive.txt";
			
			try {
				FileWriter fwCount = new FileWriter(ouputFile);

				for (int itrTimes = 0; itrTimes <= iterations;) {
					
					double avgUpdate = 0;
					double avgCluster = 0;
					double minUpdate = Constant.large;
					double minCluster = Constant.large;
					double minSum = Constant.large;
					
					for (int trial_Pyramid = 0; trial_Pyramid < trials_Pyramid; trial_Pyramid++) {
						
						double sumUpdate = 0;
						double sumCluster = 0;
						double sumSum = 0;
						
						for (int i = 1; i < activeTimes.size(); i ++) {
							
							avgUpdate += updateTime[trial_SC][itrTimes][i][trial_Pyramid];
							avgCluster += clusteringTime[trial_SC][itrTimes][i][trial_Pyramid];
							
							sumUpdate += updateTime[trial_SC][itrTimes][i][trial_Pyramid];
							sumCluster += clusteringTime[trial_SC][itrTimes][i][trial_Pyramid];
							sumSum += updateTime[trial_SC][itrTimes][i][trial_Pyramid] + clusteringTime[trial_SC][itrTimes][i][trial_Pyramid];
						}
						
						if (minUpdate > sumUpdate) minUpdate = sumUpdate;
						if (minCluster > sumCluster) minCluster = sumCluster;
						if (minSum > sumSum) minSum = sumSum;
					}
					
					avgUpdate /= (double) (trials_Pyramid * timeStep * activeTimes.size() * Constant.RUNNING_TIME_UNIT);
					avgCluster /= (double) (trials_Pyramid * activeTimes.size() * Constant.RUNNING_TIME_UNIT);
					
					minUpdate /= (double) (timeStep * activeTimes.size() * Constant.RUNNING_TIME_UNIT);
					minCluster /= (double) (activeTimes.size() * Constant.RUNNING_TIME_UNIT);
					minSum /= (double) (timeStep * activeTimes.size() * Constant.RUNNING_TIME_UNIT);
					
					fwCount.write("itrTimes " + itrTimes + ", avgUpdate " + String.format("%.6f", avgUpdate) + ", avgCluster " + String.format("%.6f", avgCluster) + ", avgSum " + String.format("%.6f", (avgUpdate + avgCluster)) + "\n");
					fwCount.write("itrTimes " + itrTimes + ", minUpdate " + String.format("%.6f", minUpdate) + ", minCluster " + String.format("%.6f", minCluster) + ", minSum " + String.format("%.6f", minSum) + "\n");
					
					
					if (itrTimes == 0) itrTimes = 1;
					else itrTimes += 2;
				}
				
				fwCount.flush();
				fwCount.close();

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			}
		}
	}

	public static void doClustering_Active_Online(int pyramidNum, double voteThreshold, int itrNum, int iterationStrategy,
			int trials_Pyramid, int minTime, int maxTime, int timeStep, int trials_SC) throws IOException, InterruptedException {

		save = true;

		ordering();

		Iterate.init(iterationStrategy);

		int maxLevel = (int) (Math.log(Graph.getVertexSize()) / Math.log(2));
		
		HashSet<Integer>[][] activeEdges = new HashSet[trials_SC][maxTime + 1];
		for (int trial = 0; trial < trials_SC; trial++) {
			for (int time = 0; time <= maxTime; time++) {
				activeEdges[trial][time] = new HashSet<Integer>();
			}
		}
		
		// read in
		for (int trial = 0; trial < trials_SC; trial++) {
			String fileInput = FilePath_Mon.filePathPre + "/weightedGraph/activeNumSing_maxTime_" + maxTime + "_trial_" + trial + ".txt";
			
			Path path = Paths.get(fileInput);
			Scanner scanner = new Scanner(path.toAbsolutePath());
			
		    // read  the file line by line
			String line;
			String[] strs;
			int edgeID;
			int time;
			while(scanner.hasNextLine()){
				
			    // process each line
			    line = scanner.nextLine();
			    strs = line.split(",");
			    
			    edgeID = Integer.parseInt(strs[0]);
			    time = Integer.parseInt(strs[1]);
			    
			    activeEdges[trial][time].add(edgeID);
			}
			
			scanner.close();
		}
		
		ArrayList<Integer> activeTimes = new ArrayList<Integer>();
		for (int activeTime = minTime; activeTime <= maxTime; activeTime += timeStep) {
			activeTimes.add(activeTime);
		}
		
		double[][][] updateTime = new double[trials_SC][activeTimes.size()][trials_Pyramid];
		double[][][] clusteringTime = new double[trials_SC][activeTimes.size()][trials_Pyramid];
		double startTime = System.currentTimeMillis();
		double endTime = System.currentTimeMillis();
		
		for (int trial_SC = 0; trial_SC < trials_SC; trial_SC++) {
			
			for (int trial_Pyramid = 0; trial_Pyramid < trials_Pyramid; trial_Pyramid++) {

				Graph.reLoad();
				int activeTimeIdx = 0;
				
				System.out.println("itrTimes " + itrNum);
				Iterate.iterate_upadteAll_parallel(itrNum);
				
				setPyramid = new SetPyramid(pyramidNum, Graph.getVertexSize(), voteThreshold);
				setPyramid.construct_parallel(true, false, trial_Pyramid);
				
				// for each time
				for (int time = 0; time <= maxTime; time++) {
					
					Graph.currentTime = time;
					
//					System.out.println("trial " + trial_SC + " time " + time);
					
					// do active
					int edgeID;
					Iterator<Integer> itr = activeEdges[trial_SC][time].iterator();
					
					startTime = System.currentTimeMillis();
				    while(itr.hasNext()){
				    	 edgeID = itr.next();
				    	 Graph.Edges[edgeID].update_active();
//				    	 setPyramid.update_decrease(edgeID);
				    }
				    endTime = System.currentTimeMillis();
				    
				    updateTime[trial_SC][activeTimeIdx][trial_Pyramid] += ((endTime - startTime) / (double) activeEdges[trial_SC][time].size());
				    
//				    System.out.println("time " + ((endTime - startTime) / (double) activeEdges[trial_SC][time].size()));
				    
				    if (activeTimes.contains(time)) {
				    	fileOutput_extractedClusterPre = FilePath_Mon.filePathPre
								+ "/extractedCluster/degree order/active/SCTrial_" + trial_SC + "/timeActive_" + time + "_itrNum_" + itrNum + "_";
						
						setPyramid.reNew_parallel();
				    	
						for (int level = 0; level <= maxLevel; level++) {
							extractCluster(iterationStrategy, level, voteThreshold, 0, trial_Pyramid);
							
							clusteringTime[trial_SC][activeTimeIdx][trial_Pyramid] += (extractClusterTime / (double)(maxLevel + 1));
						}
						
						activeTimeIdx++;
				    }
				}
			}
		}
		
		for (int trial_SC = 0; trial_SC < trials_SC; trial_SC++) {
			
			String ouputFile = FilePath_Mon.filePathPre
					+ "/extractedCluster/degree order/active/SCTrial_" + trial_SC + "/runningTime_discoverOnline.txt";
			
			try {
				FileWriter fwCount = new FileWriter(ouputFile);

				double avgUpdate = 0;
				double avgCluster = 0;
				double minUpdate = Constant.large;
				double minCluster = Constant.large;
				double minSum = Constant.large;
				
				for (int trial_Pyramid = 0; trial_Pyramid < trials_Pyramid; trial_Pyramid++) {
					
					for (int i = 1; i < activeTimes.size(); i ++) {
						avgUpdate += updateTime[trial_SC][i][trial_Pyramid];
						avgCluster += clusteringTime[trial_SC][i][trial_Pyramid];
						
						double sumUpdate = updateTime[trial_SC][i][trial_Pyramid];
						double sumCluster = clusteringTime[trial_SC][i][trial_Pyramid];
						double sumSum = updateTime[trial_SC][i][trial_Pyramid] + clusteringTime[trial_SC][i][trial_Pyramid];
						
						if (minUpdate > sumUpdate) minUpdate = sumUpdate;
						if (minCluster > sumCluster) minCluster = sumCluster;
						if (minSum > sumSum) minSum = sumSum;
						
						fwCount.write("trial_Pyramid " + trial_Pyramid + ", updateTime " + (updateTime[trial_SC][i][trial_Pyramid] / (double) (timeStep * Constant.RUNNING_TIME_UNIT)) + ""
								+ ", clusteringTime " + (clusteringTime[trial_SC][i][trial_Pyramid] / (double) (Constant.RUNNING_TIME_UNIT)) + "\n");
					}
				}
				
				avgUpdate /= (double) (trials_Pyramid * timeStep * activeTimes.size() * Constant.RUNNING_TIME_UNIT);
				avgCluster /= (double) (trials_Pyramid * activeTimes.size() * Constant.RUNNING_TIME_UNIT);
				
				minUpdate /= (double) (timeStep * Constant.RUNNING_TIME_UNIT);
				minCluster /= (double) ( Constant.RUNNING_TIME_UNIT);
				minSum /= (double) (timeStep * Constant.RUNNING_TIME_UNIT);
				
				fwCount.write("avgUpdate " + String.format("%.6f", avgUpdate) + ", avgCluster " + String.format("%.6f", avgCluster) + ", avgSum " + String.format("%.6f", (avgUpdate + avgCluster)) + "\n");
				fwCount.write("minUpdate " + String.format("%.6f", minUpdate) + ", minCluster " + String.format("%.6f", minCluster) + ", minSum " + String.format("%.6f", minSum) + "\n");
				
				fwCount.flush();
				fwCount.close();

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			}
		}
	}

	public static void doClustering_Active_Online_LocalUpdate(int pyramidNum, double voteThreshold, int itrNum, int iterationStrategy,
			int trials_Pyramid, int minTime, int maxTime, int timeStep, int trials_SC) throws IOException, InterruptedException {

		save = true;

		ordering();

		Iterate.init(iterationStrategy);

		int maxLevel = (int) (Math.log(Graph.getVertexSize()) / Math.log(2));
		
		HashSet<Integer>[][] activeEdges = new HashSet[trials_SC][maxTime + 1];
		for (int trial = 0; trial < trials_SC; trial++) {
			for (int time = 0; time <= maxTime; time++) {
				activeEdges[trial][time] = new HashSet<Integer>();
			}
		}
		
		// read in
		for (int trial = 0; trial < trials_SC; trial++) {
			String fileInput = FilePath_Mon.filePathPre + "/weightedGraph/activeNumSing_maxTime_" + maxTime + "_trial_" + trial + ".txt";
			
			Path path = Paths.get(fileInput);
			Scanner scanner = new Scanner(path.toAbsolutePath());
			
		    // read  the file line by line
			String line;
			String[] strs;
			int edgeID;
			int time;
			while(scanner.hasNextLine()){
				
			    // process each line
			    line = scanner.nextLine();
			    strs = line.split(",");
			    
			    edgeID = Integer.parseInt(strs[0]);
			    time = Integer.parseInt(strs[1]);
			    
			    activeEdges[trial][time].add(edgeID);
			}
			
			scanner.close();
		}
		
		ArrayList<Integer> activeTimes = new ArrayList<Integer>();
		for (int activeTime = minTime; activeTime <= maxTime; activeTime += timeStep) {
			activeTimes.add(activeTime);
		}
		
		double[][][] updateTime = new double[trials_SC][activeTimes.size()][trials_Pyramid];
		double[][][] clusteringTime = new double[trials_SC][activeTimes.size()][trials_Pyramid];
		double startTime = System.currentTimeMillis();
		double endTime = System.currentTimeMillis();
		
		for (int trial_SC = 0; trial_SC < trials_SC; trial_SC++) {
			
			for (int trial_Pyramid = 0; trial_Pyramid < trials_Pyramid; trial_Pyramid++) {

				Graph.reLoad();
				int activeTimeIdx = 0;
				
				System.out.println("itrTimes " + itrNum);
				Iterate.iterate_upadteAll_parallel(itrNum);
				
				setPyramid = new SetPyramid(pyramidNum, Graph.getVertexSize(), voteThreshold);
				setPyramid.construct_parallel(true, false, trial_Pyramid);
				
				// for each time
				for (int time = 0; time <= maxTime; time++) {
					
					Graph.currentTime = time;
					
					System.out.println("trial " + trial_SC + " time " + time);
					
					// do active
					int edgeID;
					Iterator<Integer> itr = activeEdges[trial_SC][time].iterator();
					
					startTime = System.currentTimeMillis();
				    while(itr.hasNext()){
				    	 edgeID = itr.next();
				    	 Graph.Edges[edgeID].update_active();
				    }
				    
//				    Iterate.local_iterate(1, activeEdges[trial_SC][time]);
				    
				    endTime = System.currentTimeMillis();
				    
				    if (time != 0 && (time % 5) == 0) {
				    	Iterate.iterate_upadteAll_parallel(1);
				    	setPyramid.reNew_parallel();
				    }
				    
				    updateTime[trial_SC][activeTimeIdx][trial_Pyramid] += ((endTime - startTime) / (double) activeEdges[trial_SC][time].size());
				    
				    if (activeTimes.contains(time)) {
				    	fileOutput_extractedClusterPre = FilePath_Mon.filePathPre
								+ "/extractedCluster/degree order/active/SCTrial_" + trial_SC + "/timeActive_" + time + "_itrNum_" + itrNum + "_";
						
				    	//if (time != 0) Iterate.iterate_upadteAll_parallel(1);
				    	
						setPyramid.reNew_parallel();
				    	
						for (int level = 0; level <= maxLevel; level++) {
							extractCluster(iterationStrategy, level, voteThreshold, 0, trial_Pyramid);
							
							clusteringTime[trial_SC][activeTimeIdx][trial_Pyramid] += (extractClusterTime / (double)(maxLevel + 1));
						}
						
						activeTimeIdx++;
				    }
				}
			}
		}
		
		for (int trial_SC = 0; trial_SC < trials_SC; trial_SC++) {
			
			String ouputFile = FilePath_Mon.filePathPre
					+ "/extractedCluster/degree order/active/SCTrial_" + trial_SC + "/runningTime_discoverOnlineLocalUpdate.txt";
			
			try {
				FileWriter fwCount = new FileWriter(ouputFile);

				double avgUpdate = 0;
				double avgCluster = 0;
				double minUpdate = Constant.large;
				double minCluster = Constant.large;
				double minSum = Constant.large;
				
				for (int trial_Pyramid = 0; trial_Pyramid < trials_Pyramid; trial_Pyramid++) {
					
					for (int i = 1; i < activeTimes.size(); i ++) {
						avgUpdate += updateTime[trial_SC][i][trial_Pyramid];
						avgCluster += clusteringTime[trial_SC][i][trial_Pyramid];
						
						double sumUpdate = updateTime[trial_SC][i][trial_Pyramid];
						double sumCluster = clusteringTime[trial_SC][i][trial_Pyramid];
						double sumSum = updateTime[trial_SC][i][trial_Pyramid] + clusteringTime[trial_SC][i][trial_Pyramid];
						
						if (minUpdate > sumUpdate) minUpdate = sumUpdate;
						if (minCluster > sumCluster) minCluster = sumCluster;
						if (minSum > sumSum) minSum = sumSum;
						
						fwCount.write("trial_Pyramid " + trial_Pyramid + ", updateTime " + (updateTime[trial_SC][i][trial_Pyramid] / (double) (timeStep * Constant.RUNNING_TIME_UNIT)) + ""
								+ ", clusteringTime " + (clusteringTime[trial_SC][i][trial_Pyramid] / (double) (Constant.RUNNING_TIME_UNIT)) + "\n");
					}
				}
				
				avgUpdate /= (double) (trials_Pyramid * timeStep * activeTimes.size() * Constant.RUNNING_TIME_UNIT);
				avgCluster /= (double) (trials_Pyramid * activeTimes.size() * Constant.RUNNING_TIME_UNIT);
				
				minUpdate /= (double) (timeStep * Constant.RUNNING_TIME_UNIT);
				minCluster /= (double) ( Constant.RUNNING_TIME_UNIT);
				minSum /= (double) (timeStep * Constant.RUNNING_TIME_UNIT);
				
				fwCount.write("avgUpdate " + String.format("%.6f", avgUpdate) + ", avgCluster " + String.format("%.6f", avgCluster) + ", avgSum " + String.format("%.6f", (avgUpdate + avgCluster)) + "\n");
				fwCount.write("minUpdate " + String.format("%.6f", minUpdate) + ", minCluster " + String.format("%.6f", minCluster) + ", minSum " + String.format("%.6f", minSum) + "\n");
				
				fwCount.flush();
				fwCount.close();

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			}
		}
	}

	public static void clusteringTime_Active(int pyramidNum, double voteThreshold, int iterations, int iterationStrategy,
			int trials_Pyramid, int minTime, int maxTime, int timeStep, int trials_SC) throws IOException, InterruptedException {

		save = false;

		ordering();

		Iterate.init(iterationStrategy);

		int maxLevel = (int) (Math.log(Graph.getVertexSize()) / Math.log(2));
		
		HashSet<Integer>[][] activeEdges = new HashSet[trials_SC][maxTime + 1];
		for (int trial = 0; trial < trials_SC; trial++) {
			for (int time = 0; time <= maxTime; time++) {
				activeEdges[trial][time] = new HashSet<Integer>();
			}
		}
		
		// read in
		for (int trial = 0; trial < trials_SC; trial++) {
			String fileInput = FilePath_Mon.filePathPre + "/weightedGraph/activeNumSing_maxTime_" + maxTime + "_trial_" + trial + ".txt";
			
			Path path = Paths.get(fileInput);
			Scanner scanner = new Scanner(path.toAbsolutePath());
			
		    // read  the file line by line
			String line;
			String[] strs;
			int edgeID;
			int time;
			while(scanner.hasNextLine()){
				
			    // process each line
			    line = scanner.nextLine();
			    strs = line.split(",");
			    
			    edgeID = Integer.parseInt(strs[0]);
			    time = Integer.parseInt(strs[1]);
			    
			    activeEdges[trial][time].add(edgeID);
			}
			
			scanner.close();
		}
		
		ArrayList<Integer> activeTimes = new ArrayList<Integer>();
		for (int activeTime = minTime; activeTime <= maxTime; activeTime += timeStep) {
			activeTimes.add(activeTime);
		}
		
		double[][][][] updateTime = new double[trials_SC][iterations + 1][activeTimes.size()][trials_Pyramid];
		double[][][][] clusteringTime = new double[trials_SC][iterations + 1][activeTimes.size()][trials_Pyramid];
		double startTime = System.currentTimeMillis();
		double endTime = System.currentTimeMillis();
		
		for (int trial_SC = 0; trial_SC < trials_SC; trial_SC++) {
			
			for (int trial_Pyramid = 0; trial_Pyramid < trials_Pyramid; trial_Pyramid++) {

				int activeTimeIdx = 0;
				
				for (int activeTime = minTime; activeTime <= maxTime; activeTime += timeStep) {

					fileOutput_extractedClusterPre = FilePath_Mon.filePathPre
							+ "/extractedCluster/degree order/active/SCTrial_" + trial_SC + "/timeActive_" + activeTime + "_";

					Graph.loadGraph_weighted("perTime/edge_active_time_" + activeTime + "_trial_" + trial_SC + ".txt");
					Graph.reLoad();

					System.out.println("itrTimes " + 0);

					/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

					setPyramid = new SetPyramid(pyramidNum, Graph.getVertexSize(), voteThreshold);
					setPyramid.construct_parallel(true, false, trial_Pyramid);
					
					for (int level = 0; level <= maxLevel; level++) {
						extractCluster(iterationStrategy, level, voteThreshold, 0, trial_Pyramid);
						
						clusteringTime[trial_SC][0][activeTimeIdx][trial_Pyramid] +=  (extractClusterTime / (double)(maxLevel + 1));
					}
					
					/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

					if (iterations >= 1) {
						for (int itrTimes = 1; itrTimes <= iterations; itrTimes += 2) {
							System.out.println("itrTimes " + itrTimes);

							Graph.reLoad();
							
							///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

							startTime = System.currentTimeMillis();
							
							Iterate.iterate_upadteAll(itrTimes);
							
							setPyramid.reNew();
							
							endTime = System.currentTimeMillis();
							
							updateTime[trial_SC][itrTimes][activeTimeIdx][trial_Pyramid] += (endTime - startTime);
							
							for (int level = 0; level <= maxLevel; level++) {
								extractCluster(iterationStrategy, level, voteThreshold, itrTimes, trial_Pyramid);
								
								clusteringTime[trial_SC][itrTimes][activeTimeIdx][trial_Pyramid] +=  (extractClusterTime / (double)(maxLevel + 1));
							}
						}
					}
					
					activeTimeIdx++;
				}
			}
		}
		
		for (int trial_SC = 0; trial_SC < trials_SC; trial_SC++) {
			
			String ouputFile = FilePath_Mon.filePathPre
					+ "/extractedCluster/degree order/active/SCTrial_" + trial_SC + "/runningTime_discoverActive.txt";
			
			try {
				FileWriter fwCount = new FileWriter(ouputFile);

				for (int itrTimes = 0; itrTimes <= iterations;) {
					
					double avgUpdate = 0;
					double avgCluster = 0;
					double minUpdate = Constant.large;
					double minCluster = Constant.large;
					double minSum = Constant.large;
					
					for (int trial_Pyramid = 0; trial_Pyramid < trials_Pyramid; trial_Pyramid++) {
						
						for (int i = 1; i < activeTimes.size(); i ++) {
							
							avgUpdate += updateTime[trial_SC][itrTimes][i][trial_Pyramid];
							avgCluster += clusteringTime[trial_SC][itrTimes][i][trial_Pyramid];
							
							double sumUpdate = updateTime[trial_SC][itrTimes][i][trial_Pyramid];
							double sumCluster = clusteringTime[trial_SC][itrTimes][i][trial_Pyramid];
							double sumSum = updateTime[trial_SC][itrTimes][i][trial_Pyramid] + clusteringTime[trial_SC][itrTimes][i][trial_Pyramid];
							
							if (minUpdate > sumUpdate) minUpdate = sumUpdate;
							if (minCluster > sumCluster) minCluster = sumCluster;
							if (minSum > sumSum) minSum = sumSum;
						}
					}
					
					avgUpdate /= (double) (trials_Pyramid * activeTimes.size() * Constant.RUNNING_TIME_UNIT);
					avgCluster /= (double) (trials_Pyramid * activeTimes.size() * Constant.RUNNING_TIME_UNIT);
					
					minUpdate /= (double) (Constant.RUNNING_TIME_UNIT);
					minCluster /= (double) (Constant.RUNNING_TIME_UNIT);
					minSum /= (double) (Constant.RUNNING_TIME_UNIT);
					
					fwCount.write("itrTimes " + itrTimes + ", avgUpdate " + String.format("%.6f", avgUpdate) + ", avgCluster " + String.format("%.6f", avgCluster) + ", avgSum " + String.format("%.6f", (avgUpdate + avgCluster)) + "\n");
					fwCount.write("itrTimes " + itrTimes + ", minUpdate " + String.format("%.6f", minUpdate) + ", minCluster " + String.format("%.6f", minCluster) + ", minSum " + String.format("%.6f", minSum) + "\n");
					avgUpdate /= (double) (maxLevel + 1);
					minUpdate /= (double) (maxLevel + 1);
					fwCount.write("divide by level, itrTimes " + itrTimes + ", avgUpdate " + String.format("%.6f", avgUpdate) + ", avgCluster " + String.format("%.6f", avgCluster) + ", avgSum " + String.format("%.6f", (avgUpdate + avgCluster)) + "\n");
					fwCount.write("divide by level, itrTimes " + itrTimes + ", minUpdate " + String.format("%.6f", minUpdate) + ", minCluster " + String.format("%.6f", minCluster) + ", minSum " + String.format("%.6f", minSum) + "\n");
					fwCount.write("# of levels " + (maxLevel + 1) + "\n");
					
					if (itrTimes == 0) itrTimes = 1;
					else itrTimes += 2;
				}
				
				fwCount.flush();
				fwCount.close();

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			}
		}
	}

	public static void clusteringTime_Active_Online(int pyramidNum, double voteThreshold, int itrNum, int iterationStrategy,
			int trials_Pyramid, int minTime, int maxTime, int timeStep, int trials_SC) throws IOException, InterruptedException {

		save = false;

		ordering();

		Iterate.init(iterationStrategy);

		int maxLevel = (int) (Math.log(Graph.getVertexSize()) / Math.log(2));
		
		HashSet<Integer>[][] activeEdges = new HashSet[trials_SC][maxTime + 1];
		for (int trial = 0; trial < trials_SC; trial++) {
			for (int time = 0; time <= maxTime; time++) {
				activeEdges[trial][time] = new HashSet<Integer>();
			}
		}
		
		// read in
		for (int trial = 0; trial < trials_SC; trial++) {
			String fileInput = FilePath_Mon.filePathPre + "/weightedGraph/activeNumSing_maxTime_" + maxTime + "_trial_" + trial + ".txt";
			
			Path path = Paths.get(fileInput);
			Scanner scanner = new Scanner(path.toAbsolutePath());
			
		    // read  the file line by line
			String line;
			String[] strs;
			int edgeID;
			int time;
			while(scanner.hasNextLine()){
				
			    // process each line
			    line = scanner.nextLine();
			    strs = line.split(",");
			    
			    edgeID = Integer.parseInt(strs[0]);
			    time = Integer.parseInt(strs[1]);
			    
			    activeEdges[trial][time].add(edgeID);
			}
			
			scanner.close();
		}
		
		ArrayList<Integer> activeTimes = new ArrayList<Integer>();
		for (int activeTime = minTime; activeTime <= maxTime; activeTime += timeStep) {
			activeTimes.add(activeTime);
		}
		
		double[][][] updateTime = new double[trials_SC][activeTimes.size()][trials_Pyramid];
		double[][][] clusteringTime = new double[trials_SC][activeTimes.size()][trials_Pyramid];
		double startTime = System.currentTimeMillis();
		double endTime = System.currentTimeMillis();
		
		for (int trial_SC = 0; trial_SC < trials_SC; trial_SC++) {
			
			for (int trial_Pyramid = 0; trial_Pyramid < trials_Pyramid; trial_Pyramid++) {

				Graph.reLoad();
				int activeTimeIdx = 0;
				
				System.out.println("itrTimes " + itrNum);
				Iterate.iterate_upadteAll_parallel(itrNum);
				
				setPyramid = new SetPyramid(pyramidNum, Graph.getVertexSize(), voteThreshold);
				setPyramid.construct_parallel(true, false, trial_Pyramid);
				
				// for each time
				for (int time = 0; time <= maxTime; time++) {
					
					Graph.currentTime = time;
					
					// do active
					int edgeID;
					Iterator<Integer> itr = activeEdges[trial_SC][time].iterator();
					
					startTime = System.currentTimeMillis();
				    while(itr.hasNext()){
				    	 edgeID = itr.next();
				    	 setPyramid.update_decrease(edgeID);
				    }
				    endTime = System.currentTimeMillis();
				    
				    updateTime[trial_SC][activeTimeIdx][trial_Pyramid] += ((endTime - startTime) / (double) activeEdges[trial_SC][time].size());
				    
				    if (activeTimes.contains(time)) {
				    	fileOutput_extractedClusterPre = FilePath_Mon.filePathPre
								+ "/extractedCluster/degree order/active/SCTrial_" + trial_SC + "/timeActive_" + time + "_itrNum_" + itrNum + "_";
				    	
				    	setPyramid.reNew_parallel();
				    	
						for (int level = 0; level <= maxLevel; level++) {
							extractCluster(iterationStrategy, level, voteThreshold, 0, trial_Pyramid);
							
							clusteringTime[trial_SC][activeTimeIdx][trial_Pyramid] += (extractClusterTime / (double)(maxLevel + 1));
						}
						
						activeTimeIdx++;
				    }
				}
			}
		}
		
		for (int trial_SC = 0; trial_SC < trials_SC; trial_SC++) {
			
			String ouputFile = FilePath_Mon.filePathPre
					+ "/extractedCluster/degree order/active/SCTrial_" + trial_SC + "/runningTime_discoverOnline.txt";
			
			try {
				FileWriter fwCount = new FileWriter(ouputFile);

				double avgUpdate = 0;
				double avgCluster = 0;
				
				for (int trial_Pyramid = 0; trial_Pyramid < trials_Pyramid; trial_Pyramid++) {
					
					for (int i = 1; i < activeTimes.size(); i ++) {
						avgUpdate += updateTime[trial_SC][i][trial_Pyramid];
						avgCluster += clusteringTime[trial_SC][i][trial_Pyramid];
						
						fwCount.write("trial_Pyramid " + trial_Pyramid + ", updateTime " + (updateTime[trial_SC][i][trial_Pyramid] / (double) (timeStep * Constant.RUNNING_TIME_UNIT)) + ""
								+ ", clusteringTime " + (clusteringTime[trial_SC][i][trial_Pyramid] / (double) (Constant.RUNNING_TIME_UNIT)) + "\n");
					}
				}
				
				avgUpdate /= (double) (trials_Pyramid * timeStep * (activeTimes.size() - 1) * Constant.RUNNING_TIME_UNIT);
				avgCluster /= (double) (trials_Pyramid * (activeTimes.size() - 1) * Constant.RUNNING_TIME_UNIT);
				
				fwCount.write("avgUpdate " + String.format("%.6f", avgUpdate) + ", avgCluster " + String.format("%.6f", avgCluster) + ", avgSum " + String.format("%.6f", (avgUpdate + avgCluster)) + "\n");
				avgUpdate /= (double) (maxLevel + 1);
				fwCount.write("divide by level, avgUpdate " + String.format("%.6f", avgUpdate) + ", avgCluster " + String.format("%.6f", avgCluster) + ", avgSum " + String.format("%.6f", (avgUpdate + avgCluster)) + "\n");
				fwCount.write("# of levels " + (maxLevel + 1) + "\n");
				
				fwCount.flush();
				fwCount.close();

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			}
		}
	}

	public static void clusteringTime_Active_Online_LocalUpdate(int pyramidNum, double voteThreshold, int itrNum, int iterationStrategy,
			int trials_Pyramid, int minTime, int maxTime, int timeStep, int trials_SC) throws IOException, InterruptedException {

		save = false;

		ordering();

		Iterate.init(iterationStrategy);

		int maxLevel = (int) (Math.log(Graph.getVertexSize()) / Math.log(2));
		
		HashSet<Integer>[][] activeEdges = new HashSet[trials_SC][maxTime + 1];
		for (int trial = 0; trial < trials_SC; trial++) {
			for (int time = 0; time <= maxTime; time++) {
				activeEdges[trial][time] = new HashSet<Integer>();
			}
		}
		
		// read in
		for (int trial = 0; trial < trials_SC; trial++) {
			String fileInput = FilePath_Mon.filePathPre + "/weightedGraph/activeNumSing_maxTime_" + maxTime + "_trial_" + trial + ".txt";
			
			Path path = Paths.get(fileInput);
			Scanner scanner = new Scanner(path.toAbsolutePath());
			
		    // read  the file line by line
			String line;
			String[] strs;
			int edgeID;
			int time;
			while(scanner.hasNextLine()){
				
			    // process each line
			    line = scanner.nextLine();
			    strs = line.split(",");
			    
			    edgeID = Integer.parseInt(strs[0]);
			    time = Integer.parseInt(strs[1]);
			    
			    activeEdges[trial][time].add(edgeID);
			}
			
			scanner.close();
		}
		
		ArrayList<Integer> activeTimes = new ArrayList<Integer>();
		for (int activeTime = minTime; activeTime <= maxTime; activeTime += timeStep) {
			activeTimes.add(activeTime);
		}
		
		double[][][] updateTime = new double[trials_SC][activeTimes.size()][trials_Pyramid];
		double[][][] clusteringTime = new double[trials_SC][activeTimes.size()][trials_Pyramid];
		double startTime = System.currentTimeMillis();
		double endTime = System.currentTimeMillis();
		
		for (int trial_SC = 0; trial_SC < trials_SC; trial_SC++) {
			
			for (int trial_Pyramid = 0; trial_Pyramid < trials_Pyramid; trial_Pyramid++) {

				Graph.reLoad();
				int activeTimeIdx = 0;
				
				System.out.println("itrTimes " + itrNum);
				Iterate.iterate_upadteAll_parallel(itrNum);
				
				setPyramid = new SetPyramid(pyramidNum, Graph.getVertexSize(), voteThreshold);
				setPyramid.construct_parallel(true, false, trial_Pyramid);
				
				double cumulateActiveEdgeNum = 0;
				
				// for each time
				for (int time = 0; time <= maxTime; time++) {
					
					Graph.currentTime = time;
					
					System.out.println("trial " + trial_SC + " time " + time);
					
					// do active
					int edgeID;
					Iterator<Integer> itr = activeEdges[trial_SC][time].iterator();
					
					startTime = System.currentTimeMillis();
				    while(itr.hasNext()){
				    	 edgeID = itr.next();
				    	 setPyramid.update_decrease(edgeID);
				    }
				    endTime = System.currentTimeMillis();
				    
				    updateTime[trial_SC][activeTimeIdx][trial_Pyramid] += ((endTime - startTime) / (double) activeEdges[trial_SC][time].size());
				    
				    cumulateActiveEdgeNum += (double) activeEdges[trial_SC][time].size();
				    
				    if (time != 0 && (time % 5) == 0) {
				    	startTime = System.currentTimeMillis();
				    	 
				    	Iterate.iterate_upadteAll(1);
				    	setPyramid.reNew();
				    	
				    	endTime = System.currentTimeMillis();
				    	
				    	updateTime[trial_SC][activeTimeIdx][trial_Pyramid] += ((endTime - startTime) / cumulateActiveEdgeNum);
				    	
				    	cumulateActiveEdgeNum = 0;
				    }
				    
				    if (activeTimes.contains(time)) {
				    	fileOutput_extractedClusterPre = FilePath_Mon.filePathPre
								+ "/extractedCluster/degree order/active/SCTrial_" + trial_SC + "/timeActive_" + time + "_itrNum_" + itrNum + "_";
				    	
						setPyramid.reNew_parallel();
				    	
						for (int level = 0; level <= maxLevel; level++) {
							extractCluster(iterationStrategy, level, voteThreshold, 0, trial_Pyramid);
							
							clusteringTime[trial_SC][activeTimeIdx][trial_Pyramid] += (extractClusterTime / (double)(maxLevel + 1));
						}
						
						activeTimeIdx++;
				    }
				}
			}
		}
		
		for (int trial_SC = 0; trial_SC < trials_SC; trial_SC++) {
			
			String ouputFile = FilePath_Mon.filePathPre
					+ "/extractedCluster/degree order/active/SCTrial_" + trial_SC + "/runningTime_discoverOnlineLocalUpdate.txt";
			
			try {
				FileWriter fwCount = new FileWriter(ouputFile);

				double avgUpdate = 0;
				double avgCluster = 0;
				
				for (int trial_Pyramid = 0; trial_Pyramid < trials_Pyramid; trial_Pyramid++) {
					
					for (int i = 1; i < activeTimes.size(); i ++) {
						avgUpdate += updateTime[trial_SC][i][trial_Pyramid];
						avgCluster += clusteringTime[trial_SC][i][trial_Pyramid];
						
						fwCount.write("trial_Pyramid " + trial_Pyramid + ", updateTime " + (updateTime[trial_SC][i][trial_Pyramid] / (double) (timeStep * Constant.RUNNING_TIME_UNIT)) + ""
								+ ", clusteringTime " + (clusteringTime[trial_SC][i][trial_Pyramid] / (double) (Constant.RUNNING_TIME_UNIT)) + "\n");
					}
				}
				
				avgUpdate /= (double) (trials_Pyramid * timeStep * (activeTimes.size() - 1) * Constant.RUNNING_TIME_UNIT);
				avgCluster /= (double) (trials_Pyramid * (activeTimes.size() - 1) * Constant.RUNNING_TIME_UNIT);
				
				fwCount.write("avgUpdate " + String.format("%.6f", avgUpdate) + ", avgCluster " + String.format("%.6f", avgCluster) + ", avgSum " + String.format("%.6f", (avgUpdate + avgCluster)) + "\n");
				avgUpdate /= (double) (maxLevel + 1);
				fwCount.write("divide by level, avgUpdate " + String.format("%.6f", avgUpdate) + ", avgCluster " + String.format("%.6f", avgCluster) + ", avgSum " + String.format("%.6f", (avgUpdate + avgCluster)) + "\n");
				fwCount.write("# of levels " + (maxLevel + 1) + "\n");
				
				fwCount.flush();
				fwCount.close();

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			}
		}
	}

	public static void clusteringTime(int pyramidNum, double voteThreshold, double stepSize,
			int iterations, int iterationStrategy, int trials) throws IOException, InterruptedException {

		save = false;

		ordering();

		Iterate.init(iterationStrategy);

		int maxLevel = (int) (Math.log(Graph.getVertexSize()) / Math.log(2));

		fileOutput_extractedClusterPre = FilePath_Mon.filePathPre + "/extractedCluster/degree order/";

		// [trials][iterations + 1]
		double[][] iterationRunningTimes = new double[trials][iterations + 1];
		
		// [trials][iterations + 1][level + 1]
		double[][][] clusteringTime = new double[trials][iterations + 1][maxLevel + 1];
		int[][][] clusteringNum = new int[trials][iterations + 1][maxLevel + 1];

		for (int trial = 0; trial < trials; trial++) {
			
			System.out.println("itrTimes " + 0);
			
			Graph.reLoad();

			/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

			System.out.println("voteThreshold " + voteThreshold);

			setPyramid = new SetPyramid(pyramidNum, Graph.getVertexSize(), voteThreshold);
			setPyramid.construct_parallel(true, false, trial);

			for (int level = 0; level <= maxLevel; level++) {
				extractCluster(iterationStrategy, level, voteThreshold, 0, trial);
			}
			
			if (iterations >= 1) {
				int minItr = 1;
				int maxItr = iterations;
				for (int itrTimes = minItr; itrTimes <= maxItr; itrTimes += 2) {
					
					System.out.println("itrTimes " + itrTimes);

					Graph.reLoad();

					iterationRunningTimes[trial][itrTimes] = Iterate.iterate_upadteAll(itrTimes);

					///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

					setPyramid.reNew_parallel();

					for (int level = 0; level <= maxLevel; level++) {
						extractCluster(iterationStrategy, level, voteThreshold, itrTimes, trial);
					}
				}
			}
		}

		try {
			FileWriter fwCount = new FileWriter(
					fileOutput_extractedClusterPre + "runningTime_doClustering_itrTimes_" + iterations + "_trial_" + trials + ".txt");

			int minItr = 1;
			int maxItr = iterations;
			for (int itrTimes = minItr; itrTimes <= maxItr; itrTimes += 2) {
				double time = 0;

				for (int trial = 0; trial < trials; trial++) {
					time += iterationRunningTimes[trial][itrTimes];
				}

				time /= trials;

				fwCount.write(itrTimes + "," + time);

				System.out.println("itrTimes " + itrTimes + " time " + time);
			}

			fwCount.flush();
			fwCount.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
	}

	private static void ordering() {
		int vertexSize = Graph.getVertexSize();

		vertexInOrder = new int[vertexSize];

		for (int v = 0; v < vertexSize; v++) {
			vertexInOrder[Graph.ID2Rank[v]] = v;
		}
	}

}
