package Exp;

import java.io.IOException;
import java.util.Scanner;

import Community.ClusterExtractor;
import Community.Iterate;
import Community.SetPyramid;
import Graph.Graph;
import Utilities.FilePath_Mon;

//To process the user input, modify the global variables correspondingly, then run the program
public class Panel {
	
	public static void main(String arg[]) throws IOException, InterruptedException {

		// Create a Scanner object to read the user input
		Scanner myObj = new Scanner(System.in); // Create a Scanner object
		
		// read and process the user input
		while (true) {
			
			// Read user input
			String input = myObj.nextLine();
			
			// if user input is "0", terminate the program
			if (input.equals("0")) break;
			String[] graphs = input.split(",");
			
			// process user input
			input = myObj.nextLine(); // Read user input
			String[] strs = input.split(" ");
			
			// for each data set that user input
			for (String graph : graphs) {
				
				FilePath_Mon.filePathPre = System.getProperty("user.dir") + "/Data/" + graph;
				Graph.loadGraph();

				String PACKAGE = strs[0];
				String FUNCTION = strs[1];
				
				// variables and their default values (if applicable)
				int pyramidNum = 4;
				int trials_Pyramid = 3;
				double voteThreshold = 0.7;
				int local_reinforcement_itrNum = 7;
				int iterationStrategy = 16;
				int minTime;
				int maxTime;
				int timeStep = 20;
				int trials_Clustering;
				
				
				// parameters for doclustering
				double EPSILON;
				int MU;

				switch (PACKAGE) {
				case "SetPyramidExp":
					{
						int minPyramidNum;
						int maxPyramidNum;
						
						switch (FUNCTION) {
						case "createPyramidSets":
							minPyramidNum = Integer.parseInt(strs[2]);
							maxPyramidNum = Integer.parseInt(strs[3]);
							trials_Pyramid = Integer.parseInt(strs[4]);
	
							SetPyramidExp.createPyramidSets(minPyramidNum, maxPyramidNum, trials_Pyramid);
	
							break;
							
						case "createPyramidSets_parallel":
							minPyramidNum = Integer.parseInt(strs[2]);
							maxPyramidNum = Integer.parseInt(strs[3]);
							trials_Pyramid = Integer.parseInt(strs[4]);
	
							SetPyramidExp.createPyramidSets_parallel(minPyramidNum, maxPyramidNum, trials_Pyramid);
	
							break;
							
						case "pyramidSize":
							minPyramidNum = Integer.parseInt(strs[2]);
							maxPyramidNum = Integer.parseInt(strs[3]);
							trials_Pyramid = Integer.parseInt(strs[4]);
							
							SetPyramidExp.pyramidSize(minPyramidNum, maxPyramidNum, trials_Pyramid);

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
								trials_Pyramid = Integer.parseInt(strs[5]);
								
								SetPyramidExp.by_timeLine_batch(first_version, last_version, ratio, trials_Pyramid);
							}
							
							break;
							
						case "updateAndQuery_by_timeLine_batch":
							{
								int first_version = Integer.parseInt(strs[2]);
								int last_version = Integer.parseInt(strs[3]);
								double ratio = Double.parseDouble(strs[4]);
								trials_Pyramid = Integer.parseInt(strs[5]);
								double queryRatio = Double.parseDouble(strs[6]);
								
								SetPyramidExp.updateAndQuery_by_timeLine_batch(first_version, last_version, ratio, trials_Pyramid, queryRatio);
							}
							
							break;
						}
					}
					
					break;

				case "ClusteringTime":
					{
						switch (FUNCTION) {
						case "clusteringTime":
							
							trials_Clustering = Integer.parseInt(strs[2]);
							iterationStrategy = Integer.parseInt(strs[3]);
							EPSILON = Double.parseDouble(strs[4]);
							MU = Integer.parseInt(strs[5]);
							
							Iterate.EPSLON = EPSILON;
							Iterate.MU = MU;

							ClusterExtractor.clusteringTime(pyramidNum, voteThreshold, local_reinforcement_itrNum, iterationStrategy, trials_Clustering);
							break;
							
						case "clusteringTime_Active":
							
							EPSILON = Double.parseDouble(strs[2]);
							MU = Integer.parseInt(strs[3]);
							minTime = Integer.parseInt(strs[4]);
							maxTime = Integer.parseInt(strs[5]);
							trials_Clustering = Integer.parseInt(strs[6]);
							
							Iterate.EPSLON = EPSILON;
							Iterate.MU = MU;
							
							ClusterExtractor.clusteringTime_Active(pyramidNum, voteThreshold, local_reinforcement_itrNum, iterationStrategy, trials_Pyramid, minTime, maxTime, timeStep, trials_Clustering);
							
							break;
							
						case "clusteringTime_Active_Online":
							{
								EPSILON = Double.parseDouble(strs[2]);
								MU = Integer.parseInt(strs[3]);
								minTime = Integer.parseInt(strs[4]);
								maxTime = Integer.parseInt(strs[5]);
								trials_Clustering = Integer.parseInt(strs[6]);
								
								Iterate.EPSLON = EPSILON;
								Iterate.MU = MU;
								
								ClusterExtractor.clusteringTime_Active_Online(pyramidNum, voteThreshold, local_reinforcement_itrNum, iterationStrategy, trials_Pyramid, minTime, maxTime, timeStep, trials_Clustering);
								
							}
							
							break;
							
						case "clusteringTime_Active_Online_LocalUpdate":
							{
								EPSILON = Double.parseDouble(strs[2]);
								MU = Integer.parseInt(strs[3]);
								minTime = Integer.parseInt(strs[4]);
								maxTime = Integer.parseInt(strs[5]);
								trials_Clustering = Integer.parseInt(strs[6]);
								
								Iterate.EPSLON = EPSILON;
								Iterate.MU = MU;
								
								ClusterExtractor.clusteringTime_Active_Online_LocalUpdate(pyramidNum, voteThreshold, local_reinforcement_itrNum, iterationStrategy, trials_Pyramid, minTime, maxTime, timeStep, trials_Clustering);
								
							}
						
							break;
						}
					}
					
					break;
				
				case "ClusterExtractor":
					{
						switch (FUNCTION) {
						case "doClustering":
							
							double minThreshold = Double.parseDouble(strs[2]);
							double maxThreshold = Double.parseDouble(strs[3]);
							trials_Clustering = Integer.parseInt(strs[4]);
							EPSILON = Double.parseDouble(strs[5]);
							MU = Integer.parseInt(strs[6]);
							
							Iterate.EPSLON = EPSILON;
							Iterate.MU = MU;

							ClusterExtractor.doClustering(pyramidNum, minThreshold, maxThreshold, local_reinforcement_itrNum, iterationStrategy, trials_Clustering);

							break;
							
						case "doClustering_Active":
							
							EPSILON = Double.parseDouble(strs[2]);
							MU = Integer.parseInt(strs[3]);
							minTime = Integer.parseInt(strs[4]);
							maxTime = Integer.parseInt(strs[5]);
							trials_Clustering = Integer.parseInt(strs[6]);
							
							Iterate.EPSLON = EPSILON;
							Iterate.MU = MU;
							
							ClusterExtractor.doClustering_Active(pyramidNum, voteThreshold, local_reinforcement_itrNum, iterationStrategy, trials_Pyramid, minTime, maxTime, timeStep, trials_Clustering);
							
							break;
							
						case "doClustering_Active_Online":
							{
								EPSILON = Double.parseDouble(strs[2]);
								MU = Integer.parseInt(strs[3]);
								minTime = Integer.parseInt(strs[4]);
								maxTime = Integer.parseInt(strs[5]);
								trials_Clustering = Integer.parseInt(strs[6]);
								
								Iterate.EPSLON = EPSILON;
								Iterate.MU = MU;
								
								ClusterExtractor.doClustering_Active_Online(pyramidNum, voteThreshold, local_reinforcement_itrNum, iterationStrategy, trials_Pyramid, minTime, maxTime, timeStep, trials_Clustering);
								
							}
							
							break;
							
						case "doClustering_Active_Online_LocalUpdate":
							{
								EPSILON = Double.parseDouble(strs[2]);
								MU = Integer.parseInt(strs[3]);
								minTime = Integer.parseInt(strs[4]);
								maxTime = Integer.parseInt(strs[5]);
								trials_Clustering = Integer.parseInt(strs[6]);
								
								Iterate.EPSLON = EPSILON;
								Iterate.MU = MU;
								
								ClusterExtractor.doClustering_Active_Online_LocalUpdate(pyramidNum, voteThreshold, local_reinforcement_itrNum, iterationStrategy, trials_Pyramid, minTime, maxTime, timeStep, trials_Clustering);
								
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
								trials_Pyramid = Integer.parseInt(strs[2]);
								pyramidNum = Integer.parseInt(strs[3]);
								
								SetPyramid.samplingPyramidSet(trials_Pyramid, pyramidNum);
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
