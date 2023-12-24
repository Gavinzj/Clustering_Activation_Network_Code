package Exp;

import java.io.IOException;
import java.util.Scanner;

import Community.ClusterExtractor;
import Community.Iterate;
import Community.SetPyramid;
import Graph.Graph;
import Utilities.FilePath_Mon;

public class Panel {
	
	public static void main(String arg[]) throws IOException, InterruptedException {

		Scanner myObj = new Scanner(System.in); // Create a Scanner object
		
		while (true) {
			String input = myObj.nextLine(); // Read user input
			if (input.equals("0")) break;
			String[] graphs = input.split(",");
			
			input = myObj.nextLine(); // Read user input
			String[] strs = input.split(" ");
			
			for (String graph : graphs) {
				
				FilePath_Mon.filePathPre = System.getProperty("user.dir") + "/Data/" + graph;
				Graph.loadGraph();

				String PACKAGE = strs[0];
				String FUNCTION = strs[1];
				
				int pyramidNum;
				int minPyramidNum;
				int maxPyramidNum;
				double voteThreshold;
				double minThreshold;
				double maxThreshold;
				double stepSize;
				int time;
				int iterations;
				int iterationStrategy;
				int trials;
				int minLevel;
				int maxLevel;
				boolean weighted;
				int minTime;
				int maxTime;
				int timeStep;
				int trials_SC;
				int trials_Pyramid;
				int minClusterNum;
				int maxClusterNum;
				int clusterNumStep;
				
				// parameters for doclustering
				double LAMBDA;
				double EPSLON;
				int MU;

				switch (PACKAGE) {
				case "SetPyramidExp":
					{
						switch (FUNCTION) {
						case "createPyramidSets":
							minPyramidNum = Integer.parseInt(strs[2]);
							maxPyramidNum = Integer.parseInt(strs[3]);
							time = Integer.parseInt(strs[4]);
							trials = Integer.parseInt(strs[5]);
	
							SetPyramidExp.createPyramidSets(minPyramidNum, maxPyramidNum, time, trials);
	
							break;
							
						case "createPyramidSets_parallel":
							minPyramidNum = Integer.parseInt(strs[2]);
							maxPyramidNum = Integer.parseInt(strs[3]);
							time = Integer.parseInt(strs[4]);
							trials = Integer.parseInt(strs[5]);
	
							SetPyramidExp.createPyramidSets_parallel(minPyramidNum, maxPyramidNum, time, trials);
	
							break;
							
						case "pyramidSize":
							minPyramidNum = Integer.parseInt(strs[2]);
							maxPyramidNum = Integer.parseInt(strs[3]);
							time = Integer.parseInt(strs[4]);
							trials = Integer.parseInt(strs[5]);
							
							SetPyramidExp.pyramidSize(minPyramidNum, maxPyramidNum, time, trials);

							break;
							
						case "by_activation_number":
							
							SetPyramidExp.doParallel = false;
							SetPyramidExp.by_activation_number_single(false, true);
							SetPyramidExp.by_activation_number_single_decrease(true, false);
							
							break;
							
						case "createSyntheticActivations_byNum_single":
							SetPyramidExp.createSyntheticActivations_byNum_single();
							
							break;
							
						case "by_timeLine_batch":
							{
								int first_version = Integer.parseInt(strs[2]);
								int last_version = Integer.parseInt(strs[3]);
								double ratio = Double.parseDouble(strs[4]);
								trials = Integer.parseInt(strs[5]);
								
								SetPyramidExp.by_timeLine_batch(first_version, last_version, ratio, trials);
							}
							
							break;
							
						case "updateAndQuery_by_timeLine_batch":
							{
								int first_version = Integer.parseInt(strs[2]);
								int last_version = Integer.parseInt(strs[3]);
								double ratio = Double.parseDouble(strs[4]);
								trials = Integer.parseInt(strs[5]);
								double queryRatio = Double.parseDouble(strs[6]);
								
								SetPyramidExp.updateAndQuery_by_timeLine_batch(first_version, last_version, ratio, trials, queryRatio);
							}
							
							break;
						}
					}
					
					break;

				case "ClusteringExp":
					{
						switch (FUNCTION) {
						case "clusteringTime":
							pyramidNum = Integer.parseInt(strs[2]);
							voteThreshold = Double.parseDouble(strs[3]);
							stepSize = Double.parseDouble(strs[4]);
							iterations = Integer.parseInt(strs[5]);
							trials = Integer.parseInt(strs[6]);
							iterationStrategy = Integer.parseInt(strs[7]);
							
							switch (iterationStrategy) {
							case 16:
								EPSLON = Double.parseDouble(strs[8]);
								MU = Integer.parseInt(strs[9]);
								Iterate.EPSLON = EPSLON;
								Iterate.MU = MU;
								break;
							}

							ClusterExtractor.clusteringTime(pyramidNum, voteThreshold, stepSize, iterations, iterationStrategy, trials);
							break;
							
						case "clusteringTime_Active":
							pyramidNum = Integer.parseInt(strs[2]);
							voteThreshold = Double.parseDouble(strs[3]);
							iterations = Integer.parseInt(strs[4]);
							trials_Pyramid = Integer.parseInt(strs[5]);
							iterationStrategy = Integer.parseInt(strs[6]);
							EPSLON = Double.parseDouble(strs[7]);
							MU = Integer.parseInt(strs[8]);
							minTime = Integer.parseInt(strs[9]);
							maxTime = Integer.parseInt(strs[10]);
							timeStep = Integer.parseInt(strs[11]);
							trials_SC = Integer.parseInt(strs[12]);
							
							Iterate.EPSLON = EPSLON;
							Iterate.MU = MU;
							
							ClusterExtractor.clusteringTime_Active(pyramidNum, voteThreshold, iterations, iterationStrategy, trials_Pyramid, minTime, maxTime, timeStep, trials_SC);
							
							break;
							
						case "clusteringTime_Active_Online":
							{
								pyramidNum = Integer.parseInt(strs[2]);
								voteThreshold = Double.parseDouble(strs[3]);
								int itrNum = Integer.parseInt(strs[4]);
								trials_Pyramid = Integer.parseInt(strs[5]);
								iterationStrategy = Integer.parseInt(strs[6]);
								EPSLON = Double.parseDouble(strs[7]);
								MU = Integer.parseInt(strs[8]);
								minTime = Integer.parseInt(strs[9]);
								maxTime = Integer.parseInt(strs[10]);
								timeStep = Integer.parseInt(strs[11]);
								trials_SC = Integer.parseInt(strs[12]);
								
								Iterate.EPSLON = EPSLON;
								Iterate.MU = MU;
								
								ClusterExtractor.clusteringTime_Active_Online(pyramidNum, voteThreshold, itrNum, iterationStrategy, trials_Pyramid, minTime, maxTime, timeStep, trials_SC);
								
							}
							
							break;
							
						case "clusteringTime_Active_Online_LocalUpdate":
							{
								pyramidNum = Integer.parseInt(strs[2]);
								voteThreshold = Double.parseDouble(strs[3]);
								int itrNum = Integer.parseInt(strs[4]);
								trials_Pyramid = Integer.parseInt(strs[5]);
								iterationStrategy = Integer.parseInt(strs[6]);
								EPSLON = Double.parseDouble(strs[7]);
								MU = Integer.parseInt(strs[8]);
								minTime = Integer.parseInt(strs[9]);
								maxTime = Integer.parseInt(strs[10]);
								timeStep = Integer.parseInt(strs[11]);
								trials_SC = Integer.parseInt(strs[12]);
								
								Iterate.EPSLON = EPSLON;
								Iterate.MU = MU;
								
								ClusterExtractor.clusteringTime_Active_Online_LocalUpdate(pyramidNum, voteThreshold, itrNum, iterationStrategy, trials_Pyramid, minTime, maxTime, timeStep, trials_SC);
								
							}
						
							break;
						}
					}
					
					break;
					
				case "ClusterExtractor":
					{
						switch (FUNCTION) {
						case "doClustering":
							pyramidNum = Integer.parseInt(strs[2]);
							minThreshold = Double.parseDouble(strs[3]);
							maxThreshold = Double.parseDouble(strs[4]);
							stepSize = Double.parseDouble(strs[5]);
							iterations = Integer.parseInt(strs[6]);
							trials = Integer.parseInt(strs[7]);
							iterationStrategy = Integer.parseInt(strs[8]);
							
							switch (iterationStrategy) {
							case 16:
								EPSLON = Double.parseDouble(strs[9]);
								MU = Integer.parseInt(strs[10]);
								Iterate.EPSLON = EPSLON;
								Iterate.MU = MU;
								break;
							}

							ClusterExtractor.doClustering(pyramidNum, minThreshold, maxThreshold, stepSize, iterations, iterationStrategy, trials);

							break;
							
						case "doClustering_Active":
							pyramidNum = Integer.parseInt(strs[2]);
							voteThreshold = Double.parseDouble(strs[3]);
							iterations = Integer.parseInt(strs[4]);
							trials_Pyramid = Integer.parseInt(strs[5]);
							iterationStrategy = Integer.parseInt(strs[6]);
							EPSLON = Double.parseDouble(strs[7]);
							MU = Integer.parseInt(strs[8]);
							minTime = Integer.parseInt(strs[9]);
							maxTime = Integer.parseInt(strs[10]);
							timeStep = Integer.parseInt(strs[11]);
							trials_SC = Integer.parseInt(strs[12]);
							
							Iterate.EPSLON = EPSLON;
							Iterate.MU = MU;
							
							ClusterExtractor.doClustering_Active(pyramidNum, voteThreshold, iterations, iterationStrategy, trials_Pyramid, minTime, maxTime, timeStep, trials_SC);
							
							break;
							
						case "doClustering_Active_Online":
							{
								pyramidNum = Integer.parseInt(strs[2]);
								voteThreshold = Double.parseDouble(strs[3]);
								int itrNum = Integer.parseInt(strs[4]);
								trials_Pyramid = Integer.parseInt(strs[5]);
								iterationStrategy = Integer.parseInt(strs[6]);
								EPSLON = Double.parseDouble(strs[7]);
								MU = Integer.parseInt(strs[8]);
								minTime = Integer.parseInt(strs[9]);
								maxTime = Integer.parseInt(strs[10]);
								timeStep = Integer.parseInt(strs[11]);
								trials_SC = Integer.parseInt(strs[12]);
								
								Iterate.EPSLON = EPSLON;
								Iterate.MU = MU;
								
								ClusterExtractor.doClustering_Active_Online(pyramidNum, voteThreshold, itrNum, iterationStrategy, trials_Pyramid, minTime, maxTime, timeStep, trials_SC);
								
							}
							
							break;
							
						case "doClustering_Active_Online_LocalUpdate":
							{
								pyramidNum = Integer.parseInt(strs[2]);
								voteThreshold = Double.parseDouble(strs[3]);
								int itrNum = Integer.parseInt(strs[4]);
								trials_Pyramid = Integer.parseInt(strs[5]);
								iterationStrategy = Integer.parseInt(strs[6]);
								EPSLON = Double.parseDouble(strs[7]);
								MU = Integer.parseInt(strs[8]);
								minTime = Integer.parseInt(strs[9]);
								maxTime = Integer.parseInt(strs[10]);
								timeStep = Integer.parseInt(strs[11]);
								trials_SC = Integer.parseInt(strs[12]);
								
								Iterate.EPSLON = EPSLON;
								Iterate.MU = MU;
								
								ClusterExtractor.doClustering_Active_Online_LocalUpdate(pyramidNum, voteThreshold, itrNum, iterationStrategy, trials_Pyramid, minTime, maxTime, timeStep, trials_SC);
								
							}
						
						break;
							
						}
					}
					
					break;

				case "SetPyramid":
					{
						switch (FUNCTION) {
						case "samplingPyramidSet":
							{
								trials = Integer.parseInt(strs[2]);
								pyramidNum = Integer.parseInt(strs[3]);
								
								SetPyramid.samplingPyramidSet(trials, pyramidNum);
							}
							break;
						}
					}
					
					break;
				}
			}
		}
		
		myObj.close();
	}
}
