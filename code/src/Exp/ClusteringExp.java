package Exp;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Scanner;

import Community.Iterate;
import Graph.Edge;
import Graph.Graph;
import Utilities.Constant;
import Utilities.FilePath_Mon;
import Utilities.Functions;

public class ClusteringExp {
	
	private static double activeRatio = 0.05; // percentage of edges to be active at each time stamp
	private static int maxTime = 100;
	
	public static void syntheticWeightedGraph_static() throws IOException, InterruptedException {
		int trials = 3;
		int activeEdgeNum = (int) Math.ceil(activeRatio * Graph.getEdgeSize());
		
		// [trials][maxTime + 1][activeEdgeNum]
		HashSet<Integer>[][] activeEdges = new HashSet[trials][maxTime + 1];
		for (int trial = 0; trial < trials; trial++) {
			for (int time = 0; time <= maxTime; time++) {
				activeEdges[trial][time] = new HashSet<Integer>(activeEdgeNum);
			}
		}
		
		// do sampling
		int edgeID;
		for (int trial = 0; trial < trials; trial++) {
			String fileOutput = FilePath_Mon.filePathPre + "/weightedGraph/activeNumSing_maxTime_" + maxTime + "_trial_" + trial + ".txt";
			
			try {
				FileWriter fw_user = new FileWriter(fileOutput);

				for (int time = 1; time <= maxTime; time++) {
					
					for (int i = 0; i < activeEdgeNum; i ++) {
						
						edgeID = Functions.randInt(0, Graph.getEdgeSize() - 1);
						
						if (activeEdges[trial][time].contains(edgeID)) {
							i--;
							continue;
						}
						
						activeEdges[trial][time].add(edgeID);
								
						fw_user.write(edgeID + "," + time + "\n");
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
		
		// select time
		ArrayList<Integer> selectedTimes = new ArrayList<Integer>();
		selectedTimes.add(0);
		
		for (int time = 10; time <= maxTime; time += 10) {
			selectedTimes.add(time);
		}
		
		// update edge weight
		for (int trial = 0; trial < trials; trial++) {
			
			Graph.reLoad();
			
			for (edgeID = 0; edgeID < Graph.getEdgeSize(); edgeID++) {
				if (Graph.Edges[edgeID].weight != 1) System.out.println("edge " + edgeID + " weight " + Graph.Edges[edgeID].weight);
			}
			
			// for each time
			for (int time = 0; time <= maxTime; time++) {
				
				System.out.println("trial " + trial + " time " + time);
				
				Graph.currentTime = time;
				
				// do active
				Iterator<Integer> itr = activeEdges[trial][time].iterator();
			    while(itr.hasNext()){
			    	 edgeID = itr.next();
			    	 Graph.Edges[edgeID].update_active();
			     }
				
				for (edgeID = 0; edgeID < Graph.getEdgeSize(); edgeID++) {
					Graph.Edges[edgeID].update_unactive();
				}
				
				if (selectedTimes.contains(time)) {
					saveGraph_static(maxTime, time, trial);
				}
			}
			
			System.out.println();
		}
	}
	
	public static void saveGraph_static(int maxTime, int time, int trial) {
		String fileOutput = FilePath_Mon.filePathPre + "/weightedGraph/edge_time_" + time + "_trial_" + trial + ".txt";
		
		try {
			FileWriter fw_user = new FileWriter(fileOutput);

			Edge edge;
			int u, v;
			for (int edgeID = 0; edgeID < Graph.getEdgeSize(); edgeID++) {
				edge = Graph.Edges[edgeID];
				
				u = edge.v_1;
				v = edge.v_2;
				
				if (u < v) {
					fw_user.write(u + "\t" + v + "\t" + String.format("%.10f", Graph.Edges[edgeID].weight) + "\n");
				} else {
					fw_user.write(v + "\t" + u + "\t" + String.format("%.10f", Graph.Edges[edgeID].weight) + "\n");
				}
			}

			fw_user.flush();
			fw_user.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		
		fileOutput = FilePath_Mon.filePathPre + "/weightedGraph/sim_time_" + time + "_trial_" + trial + ".txt";
		
		try {
			FileWriter fw_user = new FileWriter(fileOutput);

			double weight, sim;
			int edgeID;
			HashMap<Integer, Integer> neighbors;
			for (int u = 0; u < Graph.getVertexSize(); u++) {
				
				neighbors = new HashMap<Integer, Integer>(Graph.ADJ_vID[u].length);
				for (int idx_v = 0; idx_v < Graph.ADJ_vID[u].length; idx_v++) {
					int v = Graph.ADJ_vID[u][idx_v];
					neighbors.put(v, idx_v);
				}
				
				for (int v = 0; v < Graph.getVertexSize(); v++) {
					if (u == v) continue;
					
					if (neighbors.containsKey(v)) {
						edgeID = neighbors.get(v);
						weight = Graph.Edges[edgeID].weight;
						
						if (weight <= 0) sim = Constant.large;
						else if (weight >= Constant.large) sim = 0;
						else sim = (double) 1 / weight;
						
					} else {
						
						sim = 0;
					}
					
					fw_user.write((u + 1) + "," + (v + 1) + "," + String.format("%.10f", sim) + "\n");
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
	
	public static void syntheticWeightedGraph_online(int iterationStrategy, double EPSLON, int MU) throws IOException, InterruptedException {
		int iterations = 9;
		int trials = 3;
		int activeEdgeNum = (int) Math.ceil(activeRatio * Graph.getEdgeSize());
		
		Iterate.init(iterationStrategy);
		Iterate.EPSLON = EPSLON;
		Iterate.MU = MU;
		
		// [trials][maxTime + 1][activeEdgeNum]
		HashSet<Integer>[][] activeEdges = new HashSet[trials][maxTime + 1];
		for (int trial = 0; trial < trials; trial++) {
			for (int time = 0; time <= maxTime; time++) {
				activeEdges[trial][time] = new HashSet<Integer>(activeEdgeNum);
			}
		}
		
		// do sampling
		for (int trial = 0; trial < trials; trial++) {
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
		
		
		// select time
		ArrayList<Integer> selectedTimes = new ArrayList<Integer>();
		selectedTimes.add(0);
		
		for (int time = 10; time <= maxTime; time += 10) {
			selectedTimes.add(time);
		}
		
		int edgeID;
		for (int trial = 0; trial < trials; trial++) {
			for (int itrNum = 1; itrNum <= iterations;) {
				
				System.out.println("trial " + trial + " itrTimes " + itrNum);
				Graph.reLoad();
				
				for (edgeID = 0; edgeID < Graph.getEdgeSize(); edgeID++) {
					if (Graph.Edges[edgeID].weight != 1) System.out.println("edge " + edgeID + " weight " + Graph.Edges[edgeID].weight);
				}
				
				if (itrNum != 0) {
					Iterate.iterate_upadteAll_parallel(itrNum);
				}
				
				// for each time
				for (int time = 0; time <= maxTime; time++) {
					
					System.out.println("trial " + trial + " time " + time);
					
					Graph.currentTime = time;
					
					// do active
					Iterator<Integer> itr = activeEdges[trial][time].iterator();
				    while(itr.hasNext()){
				    	 edgeID = itr.next();
				    	 Graph.Edges[edgeID].update_active();
				     }
					
					for (edgeID = 0; edgeID < Graph.getEdgeSize(); edgeID++) {
						Graph.Edges[edgeID].update_unactive();
					}
					
					if (selectedTimes.contains(time)) {
						saveGraph_online(maxTime, time, trial, itrNum);
					}
				}
				
				System.out.println();
				
				if (itrNum == 0) itrNum = 1;
				else itrNum += 2;
			}
		}
	}
	
	public static void saveGraph_online(int maxTime, int time, int trial, int itrNum) {
		String fileOutput = FilePath_Mon.filePathPre + "/weightedGraph/edge_itrNum_" + itrNum + "_time_" + time + "_trial_" + trial + ".txt";
		
		try {
			FileWriter fw_user = new FileWriter(fileOutput);

			Edge edge;
			int u, v;
			for (int edgeID = 0; edgeID < Graph.getEdgeSize(); edgeID++) {
				edge = Graph.Edges[edgeID];
				
				u = edge.v_1;
				v = edge.v_2;
				
				if (u < v) {
					fw_user.write(u + "\t" + v + "\t" + String.format("%.10f", Graph.Edges[edgeID].weight) + "\n");
				} else {
					fw_user.write(v + "\t" + u + "\t" + String.format("%.10f", Graph.Edges[edgeID].weight) + "\n");
				}
			}

			fw_user.flush();
			fw_user.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		
		fileOutput = FilePath_Mon.filePathPre + "/weightedGraph/sim_itrNum_" + itrNum + "_time_" + time + "_trial_" + trial + ".txt";
		
		try {
			FileWriter fw_user = new FileWriter(fileOutput);

			double weight, sim;
			int edgeID;
			HashMap<Integer, Integer> neighbors;
			for (int u = 0; u < Graph.getVertexSize(); u++) {
				
				neighbors = new HashMap<Integer, Integer>(Graph.ADJ_vID[u].length);
				for (int idx_v = 0; idx_v < Graph.ADJ_vID[u].length; idx_v++) {
					int v = Graph.ADJ_vID[u][idx_v];
					neighbors.put(v, idx_v);
				}
				
				for (int v = 0; v < Graph.getVertexSize(); v++) {
					if (u == v) continue;
					
					if (neighbors.containsKey(v)) {
						edgeID = neighbors.get(v);
						weight = Graph.Edges[edgeID].weight;
						
						if (weight <= 0) sim = Constant.large;
						else if (weight >= Constant.large) sim = 0;
						else sim = (double) 1 / weight;
						
					} else {
						
						sim = 0;
					}
					
					if (Double.isInfinite(sim) || Double.isNaN(sim)) System.out.println(u + " " + v + " " + sim);
					
					fw_user.write((u + 1) + "," + (v + 1) + "," + String.format("%.10f", sim) + "\n");
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
	
	public static void readGraph_updateAll_online() throws IOException, InterruptedException {
		
		int trials = 3;
		
		// [trials][maxTime + 1][activeEdgeNum]
		HashSet<Integer>[][] activeEdges = new HashSet[trials][maxTime + 1];
		for (int trial = 0; trial < trials; trial++) {
			for (int time = 0; time <= maxTime; time++) {
				activeEdges[trial][time] = new HashSet<Integer>();
			}
		}
		
		// read in
		for (int trial = 0; trial < trials; trial++) {
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
		
		int edgeID;
		for (int trial = 0; trial < trials; trial++) {
			
			Graph.reLoad();
			
			// for each time
			for (int time = 0; time <= maxTime; time++) {
				
				System.out.println("trial " + trial + " time " + time);
				
				Graph.currentTime = time;
				
				// do active
				Iterator<Integer> itr = activeEdges[trial][time].iterator();
			    while(itr.hasNext()){
			    	 edgeID = itr.next();
			    	 Graph.Edges[edgeID].update_active();
			    }
				
				for (edgeID = 0; edgeID < Graph.getEdgeSize(); edgeID++) {
					Graph.Edges[edgeID].update_unactive();
				}
				
				String fileOutput = FilePath_Mon.filePathPre + "/weightedGraph/perTime/edge_time_" + time + "_trial_" + trial + ".txt";
				
				try {
					FileWriter fw_user = new FileWriter(fileOutput);

					Edge edge;
					int u, v;
					for (edgeID = 0; edgeID < Graph.getEdgeSize(); edgeID++) {
						edge = Graph.Edges[edgeID];
						
						u = edge.v_1;
						v = edge.v_2;
						
						if (u < v) {
							fw_user.write(u + "\t" + v + "\t" + String.format("%.10f", Graph.Edges[edgeID].weight) + "\n");
						} else {
							fw_user.write(v + "\t" + u + "\t" + String.format("%.10f", Graph.Edges[edgeID].weight) + "\n");
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
		}
	}
	
	public static void readGraph_updateActive_online() throws IOException, InterruptedException {
		
		int trials = 3;
		
		// [trials][maxTime + 1][activeEdgeNum]
		HashSet<Integer>[][] activeEdges = new HashSet[trials][maxTime + 1];
		for (int trial = 0; trial < trials; trial++) {
			for (int time = 0; time <= maxTime; time++) {
				activeEdges[trial][time] = new HashSet<Integer>();
			}
		}
		
		// do sampling
		for (int trial = 0; trial < trials; trial++) {
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
		
		int edgeID;
		for (int trial = 0; trial < trials; trial++) {
			
			Graph.reLoad();
			
			// for each time
			for (int time = 0; time <= maxTime; time++) {
				
				System.out.println("trial " + trial + " time " + time);
				
				Graph.currentTime = time;
				
				// do active
				Iterator<Integer> itr = activeEdges[trial][time].iterator();
			    while(itr.hasNext()){
			    	 edgeID = itr.next();
			    	 Graph.Edges[edgeID].update_active();
			    }
				
				String fileOutput = FilePath_Mon.filePathPre + "/weightedGraph/perTime/edge_active_time_" + time + "_trial_" + trial + ".txt";
				
				try {
					FileWriter fw_user = new FileWriter(fileOutput);

					Edge edge;
					int u, v;
					for (edgeID = 0; edgeID < Graph.getEdgeSize(); edgeID++) {
						edge = Graph.Edges[edgeID];
						
						u = edge.v_1;
						v = edge.v_2;
						
						if (u < v) {
							fw_user.write(u + "\t" + v + "\t" + String.format("%.10f", Graph.Edges[edgeID].weight) + "\n");
						} else {
							fw_user.write(v + "\t" + u + "\t" + String.format("%.10f", Graph.Edges[edgeID].weight) + "\n");
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
		}
	}
	
	public static void main(String arg[]) throws IOException, InterruptedException  {
		Graph.loadGraph();
		
		syntheticWeightedGraph_static();
		
		readGraph_updateAll_online();
		
		readGraph_updateActive_online();
	}
}
