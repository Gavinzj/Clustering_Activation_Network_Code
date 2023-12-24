package Graph;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;

import Community.Iterate;
import Utilities.Constant;
import Utilities.FilePath_Mon;
import Utilities.Functions;

public class Graph {
	private static String fileInput_edge;
	
	private static String fileInput_edge_weighted;
	
	private static String fileInput_adj;
	
	public static Runtime garbbageCollector = Runtime.getRuntime();
	
	public static Edge[] Edges;
	
	public static int[] ID2Rank;
	
	public static int[][] ADJ_vID;
	
	public static int[][] ADJ_eID;
	
	public static int currentTime = 0;
	
	public static boolean isWeightedEdge = false;
	
	private static ReLoad reLoad;
	private static SortAdj sortAdj;
	private static CountDownLatch latch;
	
	
	//////////////////// temporal variables ////////////////////////
	
	private static Vertex[] Vertices;  // temporal variable
	
	public static List<adj2EdgeID>[] adj;  // temporal variable
	
	public static int getVertexSize() {
		return ID2Rank.length;
	}
	
	public static int getEdgeSize() {
		return Edges.length;
	}
	
	public static void loadGraph_weighted(String pathAppend) {
		
		isWeightedEdge = true;
		
		fileInput_edge_weighted = FilePath_Mon.filePathPre + "/weightedGraph/" + pathAppend;
	}
	
	public static void loadGraph() throws IOException, InterruptedException {
		System.out.println(FilePath_Mon.filePathPre);
		
		fileInput_edge = FilePath_Mon.filePathPre + "/edge.txt";
		fileInput_adj = FilePath_Mon.filePathPre + "/adj.txt";
		
        double startMem = garbbageCollector.totalMemory() - garbbageCollector.freeMemory();
        double start = System.currentTimeMillis();
        
        System.out.println("isWeightedEdge ? " + isWeightedEdge);
        
        loadAdj(isWeightedEdge);
    	System.out.println("adj loaded");
    	
    	sortVertices();
		System.out.println("vertex sorted");
		
		sortAdj_parallel();
        
		double endMem = garbbageCollector.totalMemory() - garbbageCollector.freeMemory();
		double end = System.currentTimeMillis();
        
		double runningTime = (end - start) / Constant.RUNNING_TIME_UNIT;
		double memory = (endMem - startMem) / Constant.MEMORY_UNIT;
        
        String runningTime_update_print = String.format("%.2f", runningTime);
		String rmemory_update_print = String.format("%.2f", memory);
		
		System.out.println("Graph Loaded. " + "vertices size: " + getVertexSize() + ", edge size: " + getEdgeSize() + " time: " + runningTime_update_print + ""
				+ " memory: " + rmemory_update_print);
		
		garbbageCollector.gc();
	}
	
	private static class ReLoad extends Thread{
		private int idx_u_first;
		private int idx_u_last;
		
		ReLoad(int idx_u_first, int idx_u_last) throws InterruptedException {
			this.idx_u_first = idx_u_first;
			this.idx_u_last = idx_u_last;
			start();
		}
		
		public void run() {
			for (int i = idx_u_first; i < idx_u_last; i ++) {
				Graph.Edges[i].weight = Constant.INITIAL_EDGE_WEIGHT;
				Graph.Edges[i].lastActive = 0;
			}
			
			latch.countDown();
		}
	}
	
	public static void reLoad() throws IOException, InterruptedException {
		if (isWeightedEdge) {
			System.out.println("weighted");
			loadWeightedEdge(isWeightedEdge);
			
		} else {
			
			int edgeSize = Graph.getEdgeSize();
			int batchNum = 256;
			latch = new CountDownLatch(batchNum);
			int batchSize = (int) Math.ceil((double) edgeSize / (double) batchNum);
			
			int threadNum = 0;
			for (int idx_u_first = 0; idx_u_first < edgeSize; idx_u_first += batchSize) {
				int idx_u_last = Math.min((idx_u_first + batchSize), edgeSize);
				reLoad = new ReLoad(idx_u_first, idx_u_last);
				
				threadNum++;
				
				if (idx_u_last >= edgeSize) {
					int needRemove = (batchNum - threadNum);
					for (int i = 0; i < needRemove; i ++) {
						latch.countDown();
					}
					break;
				}
			}
			
			latch.await();
			
			Graph.currentTime = 0;
		}
	}
	
	private static void loadWeightedEdge(boolean isEdgeWeighted) throws IOException {
		Path path = Paths.get(fileInput_edge_weighted);
		
		Scanner scanner = new Scanner(path.toAbsolutePath());
	    
	    int edgeID = 0;
		
	    // read  the file line by line
		String line;
		double weight;
		while(scanner.hasNextLine()){
			
		    // process each line
		    line = scanner.nextLine();
		    
		    weight = Double.parseDouble(line.split("\t")[2]);
		    
		    // new an edge
	    	Edges[edgeID++].weight = weight;
		}
		
		scanner.close();
	}
	
	private static void loadAdj(boolean isEdgeWeighted) throws IOException {
		// vertex size
		Path path = Paths.get(fileInput_adj);
		Scanner scanner = new Scanner(path.toAbsolutePath());
		
		int verticeSize = Integer.parseInt(scanner.nextLine());
		
		scanner.close();
		
		adj = new ArrayList[verticeSize];
		Vertices = new Vertex[verticeSize];
		for (int i = 0; i < verticeSize; i++) {
			adj[i] = new ArrayList<adj2EdgeID>();
			Vertices[i] = new Vertex(i);
		}
		
		// edge size
		if (isEdgeWeighted) path = Paths.get(fileInput_edge_weighted);
		else path = Paths.get(fileInput_edge);
		
		scanner = new Scanner(path.toAbsolutePath());
		
	    int edgeSize = (int) Files.lines(path).count();
	    
	    Edges = new Edge[edgeSize];
	    int edgeID = 0;
		
	    // start reading edges
		String line;
		String[] strs;
		int v_1, v_2;
		double weight;
		while(scanner.hasNextLine()){
		    //process each line
		    line = scanner.nextLine();
		    strs = line.split("\t");
		    
		    v_1 = Integer.parseInt(strs[0]);
		    v_2 = Integer.parseInt(strs[1]);
		    
		    if (isEdgeWeighted) {
		    	v_1 = Integer.parseInt(strs[0]);
			    v_2 = Integer.parseInt(strs[1]);
			    weight = Double.parseDouble(strs[2]);
			    
			    // new an edge
		    	Edges[edgeID] = new Edge(v_1, v_2, weight);
		    	
		    } else {
		    	v_1 = Integer.parseInt(strs[0]);
			    v_2 = Integer.parseInt(strs[1]);
			    
			    // new an edge
			    Edges[edgeID] = new Edge(v_1, v_2, Constant.INITIAL_EDGE_WEIGHT);
		    }
	    	
	    	if(edgeID % 10000000 == 0) System.out.println(edgeID);
	    	
	    	adj[v_1].add(new adj2EdgeID(v_2, edgeID));
	    	adj[v_2].add(new adj2EdgeID(v_1, edgeID));
	    	
	    	edgeID++;
		}
		
		scanner.close();
	}
	
	private static void sortVertices(){
		// load degree
		for (int i = 0; i < Vertices.length; i ++) {
			Vertices[i].degree = adj[i].size();
		}
		
		Arrays.parallelSort(Vertices);
		
		ID2Rank = new int[Vertices.length];
		
		// from highest rank 0 to lowest 
		for (int i = 0; i < Vertices.length; i ++) {
			ID2Rank[Vertices[i].mID] = i;
		}
		
		Vertices = null;
		
		garbbageCollector.gc();
	}
	
	private static class SortAdj extends Thread{
		private int idx_u_first;
		private int idx_u_last;
		
		SortAdj(int idx_u_first, int idx_u_last) throws InterruptedException {
			this.idx_u_first = idx_u_first;
			this.idx_u_last = idx_u_last;
			start();
		}
		
		public void run() {
			
			int cnt = 0;
			int neighborSize;
			adj2EdgeID pair;
			List<adj2EdgeID> pointer = new ArrayList<adj2EdgeID>();
			
			for (int u = idx_u_first; u < idx_u_last; u ++) {
				Collections.sort(adj[u]);
				
				neighborSize = adj[u].size();
				pointer = adj[u];
				
				ADJ_vID[u] = new int[neighborSize];
				ADJ_eID[u] = new int[neighborSize];
				
				for (int idx = 0; idx < neighborSize; idx ++) {
					pair = pointer.get(idx);
					ADJ_vID[u][idx] = pair.mID;
					ADJ_eID[u][idx] = pair.edgeID;
				}
				
				adj[u] = null;
				
				cnt++;
				if(cnt % 100000 == 0) {
//					System.out.println(idx_u_first + " remove " + cnt);
				}
			}
			
			garbbageCollector.gc();
			
//			System.out.println("sort adj " + idx_u_first + " to " + idx_u_last);
			
			latch.countDown();
		}
	}
	
	private static void sortAdj_parallel() throws InterruptedException {
		// sorting
		ADJ_vID = new int[adj.length][];
		ADJ_eID = new int[adj.length][];
		
		int vertexSize = Graph.getVertexSize();
		int batchNum = 256;
		latch = new CountDownLatch(batchNum);
		int batchSize = (int) Math.ceil((double) vertexSize / (double) batchNum);
		
		int threadNum = 0;
		for (int idx_u_first = 0; idx_u_first < vertexSize; idx_u_first += batchSize) {
			int idx_u_last = Math.min((idx_u_first + batchSize), vertexSize);
			sortAdj = new SortAdj(idx_u_first, idx_u_last);
			
			threadNum++;
			
			if (idx_u_last >= vertexSize) {
				int needRemove = (batchNum - threadNum);
				for (int i = 0; i < needRemove; i ++) {
					latch.countDown();
				}
				break;
			}
		}
		
		latch.await();
		
		garbbageCollector.gc();
	}
	
	private static void sortAdj() {
		// sorting
		adj2EdgeID[] list;
		
		for (int i = 0; i < adj.length; i ++) {
			
			if(i % 1000000 == 0) System.out.println("sortAdj " + i);
			
			list = adj[i].toArray(new adj2EdgeID[0]);
			
			Arrays.parallelSort(list);
			
			adj[i] = new ArrayList<>(Arrays.asList(list));
		}
		
		// collect garbbage
		garbbageCollector.gc();
		
		ADJ_vID = new int[adj.length][];
		ADJ_eID = new int[adj.length][];
		
		int neighborSize;
		adj2EdgeID pair;
		for (int u = 0; u < adj.length; u ++) {
			neighborSize = adj[u].size();
			List<adj2EdgeID> pointer = new ArrayList<adj2EdgeID>();
			pointer = adj[u];
			
			ADJ_vID[u] = new int[neighborSize];
			ADJ_eID[u] = new int[neighborSize];
			
			for (int idx = 0; idx < neighborSize; idx ++) {
				pair = pointer.get(idx);
				ADJ_vID[u][idx] = pair.mID;
				ADJ_eID[u][idx] = pair.edgeID;
			}
		}
	}
	
	// -1: degree of v_1 is higher
	public static int compareVertex_byID(int v_1, int v_2) {
		if (ID2Rank[v_1] < ID2Rank[v_2]) {
			return -1;
		} else if (ID2Rank[v_1] > ID2Rank[v_2]) {
			return 1;
		} else {
			if (v_1 < v_2) {
				return -1;
			} else if (v_1 > v_2) {
				return 1;
			} else {
				return 0;
			}
		}
	}
	
	public static int getNeighborSize(int mID) {
		return ADJ_vID[mID].length;
	}
	
	public static int getNeighbor_byID(int mID, int neighborID) {
		return ADJ_vID[mID][neighborID];
	}
	
	public static List<Integer> getCommonNeighbors(int mID_1, int mID_2){
		List<Integer> result = new ArrayList<Integer>();
		for (int v : ADJ_vID[mID_1]) {
			if (indexOf(mID_2, v) != -1) result.add(v);
		}
		return result;
	}
	
	public static int getNeighborIdx(int uID, int vID) {
		int L_index = 0;
		int R_index = ADJ_vID[uID].length - 1;
		
		int M_index, M_ID;
		while(L_index <= R_index) {
			M_index = (int) Math.floor((L_index + R_index) / 2);
			
			M_ID = ADJ_vID[uID][M_index];
			
			if (compareVertex_byID(M_ID, vID) < 0) {
				// rankd of M is higher than v
				L_index = M_index + 1;
				
			} else if (compareVertex_byID(M_ID, vID) > 0) {
				// rankd of M is lower than v
				R_index = M_index - 1;
				
			} else {
				return M_index;
			}
		}
		
		return -1;
	}
	
	// return -1: not found; -2: distance = 0
	public static int getEdgeID_BS(int uID, int vID) {
		int edgeID = -1;
		
		int L_index = 0;
		int R_index = ADJ_vID[uID].length - 1;
		
		int M_index, M_ID;
		while(L_index <= R_index) {
			M_index = (int) Math.floor((L_index + R_index) / 2);
			
			M_ID = ADJ_vID[uID][M_index];
			edgeID = ADJ_eID[uID][M_index];
			
			if (compareVertex_byID(M_ID, vID) < 0) {
				// rankd of M is higher than v
				L_index = M_index + 1;
				
			} else if (compareVertex_byID(M_ID, vID) > 0) {
				// rankd of M is lower than v
				R_index = M_index - 1;
				
			} else {
				return edgeID;
			}
		}
		
		return edgeID;
	}
	
	// check whether v is neighbor of u, return the index
	public static int indexOf(int u, int neighbor) {
		int v;
		for (int idx = 0; idx < ADJ_vID[u].length; idx ++) {
			v = ADJ_vID[u][idx];
			if (v == neighbor) return idx;
		}
		
		return -1;
	}
	
	public static void main(String arg[]) throws IOException, InterruptedException {
		Graph.loadGraph();
		
		HashSet<Integer> numbers = new HashSet<Integer>();
		while (numbers.size() < 50) {
			numbers.add(Functions.randInt(0, 1440-1));
		}
		ArrayList<Integer> numbers_sort = new ArrayList<Integer>();
		Iterator<Integer> it = numbers.iterator();
		while(it.hasNext()){
			numbers_sort.add(it.next());
	    }
		
		Collections.sort(numbers_sort);
		
		for (int number : numbers_sort) {
			System.out.println(number);
		}
		
//		Graph.loadGraph_weighted("edge_time_0_trial_0.txt");
//		Graph.reLoad();
//		
//		Iterate.init(16);
//		Iterate.EPSLON = 0.3;
//		Iterate.MU = 5;
//		
//		Iterate.iterate_upadteAll_parallel(7);
//		
//		for (int i = 0; i < Graph.getEdgeSize(); i ++) {
//			System.out.println(Graph.Edges[i]);
//		}
		
//		for (int u = 0; u < ADJ_vID.length; u ++) {
//			System.out.print(u + ": ");
//			for (int v = 0; v < ADJ_vID[u].length; v++) {
//				System.out.print(ADJ_vID[u][v] + "(" + ADJ_eID[u][v] + "),");
//			}
//			System.out.println();
//		}
		
//		for (int i = 0; i < ADJ_vID.length; i ++) {
//			System.out.println(i + "(" + ADJ_vID[i].length + ") " + Functions.arrToString(ADJ_vID[i]));
//			System.out.println(i + "\t" + "(" + ADJ_eID[i].length + ") " + Functions.arrToString(ADJ_eID[i]));
//			System.out.println();
//		}
		
//		for (int i = 0; i < ADJ_vID.length; i ++) {
//			for (int v : ADJ_vID[i]) {
//				if (ADJ_vID[i][Graph.getNeighborIdx(i, v)] != v)System.out.println(v);
//			}
//		}
		
//		Iterate.iterate_upadteAll(2, 0);
//		Graph.reLoad();
//		for (int i = 0; i < ADJ_eID.length; i ++) {
//			if (Graph.Edges[i].weight != Constant.INITIAL_EDGE_WEIGHT) System.out.println(Graph.Edges[i]);
//		}
		
//		double avgDeg = 0;
//		int maxDeg = -1;
//		for (int v = 0; v < Graph.getVertexSize(); v ++) {
//			avgDeg += Graph.ADJ_vID[v].length;
//			if (maxDeg < Graph.ADJ_vID[v].length) maxDeg = Graph.ADJ_vID[v].length;
//		}
//		
//		System.out.println("avg deg " + (avgDeg / (double) Graph.getVertexSize()) + " max deg " + maxDeg);
		
//		for (int v = 0; v < Graph.getVertexSize(); v ++) {
//			for (int u : Graph.ADJ_vID[v]) {
//				System.out.print(u + " ");
//			}
//			System.out.println();
//		}
		
//		for (int v = 0; v < Graph.getVertexSize(); v ++) {
//			System.out.println("v " + v + " degree " + Graph.ADJ_vID[v].length + " rank " + Graph.ID2Rank[v]);
//		}
		
    }
}
