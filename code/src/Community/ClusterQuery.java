package Community;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;

import Graph.Graph;
import Utilities.Constant;
import Utilities.FilePath_Mon;
import Utilities.Functions;

public class ClusterQuery {
	private static String fileOutputPre = FilePath_Mon.filePathPre + "/query/";

	private static SetPyramid setPyramid;
	
	// order for finding clusterings
	private static int[] vertexInOrder;
	
	public static void queryTime(int pyramidNum, double voteThreshold, int pyramidTrials, double sampleRatio, int sampleTrials, boolean doSample) throws IOException, InterruptedException {

		fileOutputPre = FilePath_Mon.filePathPre + "/query/";
		
		if (doSample) {
			for (int sampleTrial = 0; sampleTrial < sampleTrials; sampleTrial++) {
				sampleQueryNodes(sampleRatio, sampleTrial);
			}
		}
		
		int maxLevel = (int) (Math.log(Graph.getVertexSize()) / Math.log(2));
		int minLevel = maxLevel - 6;
		
		ArrayList<Integer>[] queryNodes = new ArrayList[sampleTrials];
		
		// read in sample nodes
		for (int sampleTrial = 0; sampleTrial < sampleTrials; sampleTrial++) {
			
			queryNodes[sampleTrial] = new ArrayList<Integer>();
			
			String fileInput = fileOutputPre + "queryNodes_sampleRatio_" + sampleRatio + "_trial_" + sampleTrial + ".txt";
			
			Path path = Paths.get(fileInput);
			Scanner scanner = new Scanner(path.toAbsolutePath());
			
		    // read  the file line by line
			while(scanner.hasNextLine()){
				
			    // process each line
			    int node = Integer.parseInt(scanner.nextLine());
			    
			    queryNodes[sampleTrial].add(node);
			}
			
			scanner.close();
		}

		// [pyramidTrials][sampleTrials][maxLevel + 1]
		double[][][] queryTimes = new double[pyramidTrials][sampleTrials][maxLevel + 1];
		double [][][] clusterSize = new double[pyramidTrials][sampleTrials][maxLevel + 1];
		
		for (int pyramidTrial = 0; pyramidTrial < pyramidTrials; pyramidTrial++) {

			System.out.println("pyramid trial " + pyramidTrial);
			
			/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

			setPyramid = new SetPyramid(pyramidNum, Graph.getVertexSize(), voteThreshold);
			setPyramid.construct_parallel(true, false, pyramidTrial);
			
			for (int sampleTrial = 0; sampleTrial < sampleTrials; sampleTrial++) {
				
				System.out.println(" ==> sample trial " + sampleTrial);
				
				for (int level = minLevel; level < queryTimes[pyramidTrial][sampleTrial].length; level ++) {
					
					// for each query nodes
					for (int queryNode : queryNodes[sampleTrial]) {
						String result = setPyramid.query(queryNode, level);
						String[] strs = result.split(",");
						
						queryTimes[pyramidTrial][sampleTrial][level] += Double.parseDouble(strs[1]);
						clusterSize[pyramidTrial][sampleTrial][level] += Integer.parseInt(strs[0]);
					}
					
					queryTimes[pyramidTrial][sampleTrial][level] /= (double) queryNodes[sampleTrial].size();
					clusterSize[pyramidTrial][sampleTrial][level] /= (double) queryNodes[sampleTrial].size();
				}
			}
		}

		try {
			FileWriter fwCount = new FileWriter(
					fileOutputPre + "queryNodes_sampleRatio_" + sampleRatio + "_queryTime.txt");
			
			for (int level = minLevel; level <= maxLevel; level ++) {
				
				double avgOnAll_time = 0;
				double minOnPyramid_time = Constant.large;
				double minOnPyramidOnSample_time = Constant.large;
				
				double avgOnAll_clusterNum = 0;
				double minOnPyramid_clusterNum = Constant.large;
				double minOnPyramidOnSample_clusterNum = Constant.large;
				
				for (int pyramidTrial = 0; pyramidTrial < pyramidTrials; pyramidTrial++) {
					
					double sumPerPyramid_time = 0;
					
					double sumPerPyramid_clusterNum = 0;
					
					for (int sampleTrial = 0; sampleTrial < sampleTrials; sampleTrial++) {
						
						avgOnAll_time += queryTimes[pyramidTrial][sampleTrial][level];
						sumPerPyramid_time += queryTimes[pyramidTrial][sampleTrial][level];
						if (minOnPyramidOnSample_time > queryTimes[pyramidTrial][sampleTrial][level]) minOnPyramidOnSample_time = queryTimes[pyramidTrial][sampleTrial][level];
						
						avgOnAll_clusterNum += clusterSize[pyramidTrial][sampleTrial][level];
						sumPerPyramid_clusterNum += clusterSize[pyramidTrial][sampleTrial][level];
						if (minOnPyramidOnSample_clusterNum > clusterSize[pyramidTrial][sampleTrial][level]) minOnPyramidOnSample_clusterNum = clusterSize[pyramidTrial][sampleTrial][level];
					}
					
					if (minOnPyramid_time > sumPerPyramid_time) minOnPyramid_time = sumPerPyramid_time; 
					
					if (minOnPyramid_clusterNum > sumPerPyramid_clusterNum) minOnPyramid_clusterNum = sumPerPyramid_clusterNum; 
				}
				
				avgOnAll_time /= (double) (pyramidTrials * sampleTrials * Constant.RUNNING_TIME_UNIT);
				minOnPyramid_time /= (double) (sampleTrials * Constant.RUNNING_TIME_UNIT);
				minOnPyramidOnSample_time /= (double) (Constant.RUNNING_TIME_UNIT);
				
				avgOnAll_clusterNum /= (double) (pyramidTrials * sampleTrials);
				minOnPyramid_clusterNum /= (double) (sampleTrials);
				
				fwCount.write("level " + level + " time average " + avgOnAll_time + " min on pyramid " + minOnPyramid_time + " min on pyramid on sample " + minOnPyramidOnSample_time + "\n");
				fwCount.write("level " + level + " cluster number average " + avgOnAll_clusterNum + " min on pyramid " + minOnPyramid_clusterNum + " min on pyramid on sample " + minOnPyramidOnSample_clusterNum + "\n");
				fwCount.write("\n");
			}

			fwCount.flush();
			fwCount.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
	}
	
	private static void sampleQueryNodes (double sampleRatio, int sampleTrial) {
		int sampleSize = (int) Math.ceil(Graph.getVertexSize() * sampleRatio);
		
		System.out.println("trial " + sampleTrial + " sample " + sampleSize + " nodes");
		
		HashSet<Integer> sampleNodes = new HashSet<Integer>(sampleSize);
		
		while (sampleNodes.size() < sampleSize) {
			sampleNodes.add(Functions.randInt(0, Graph.getVertexSize() - 1));
		}
		
		try {
			FileWriter fwCount = new FileWriter(
					fileOutputPre + "queryNodes_sampleRatio_" + sampleRatio + "_trial_" + sampleTrial + ".txt");

			Iterator<Integer> it = sampleNodes.iterator();
		    while(it.hasNext()){
		    	fwCount.write(it.next() + "\n");
		    }

			fwCount.flush();
			fwCount.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
	}
	
	public static void main(String arg[]) throws IOException, InterruptedException {
		
	}
}
