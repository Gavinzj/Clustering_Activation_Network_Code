package Community;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.CountDownLatch;

import Graph.Edge;
import Graph.Graph;
import Utilities.Constant;
import Utilities.FilePath_Mon;
import Utilities.Functions;

public class Iterate {
	private static int itrTimes;
	public static int strategy;
	
	// for attractor like
	public static double LAMBDA = 0.3;
	
	// for dbscan like
	public static double EPSLON = 0.3;
	public static int MU = 5;
	private static double maxWeight = -1;
	
	private static OverLap OverLap;
	private static Distance2Similarity Distance2Similarity;
	private static Similarity2Distance Similarity2Distance;
	private static StructuralSimilarity StructuralSimilarity;
	private static StructuralSimilarity_weighted StructuralSimilarity_weighted;
	private static CountDownLatch latch;
	
	private static double[] newWeights;
	private static double[] oldSims;
	private static double[] structuralSimilarity;
	private static double[] structuralSimilarity_weighted;
	
	public static double[][] OverLaps;
	private static double[][] JaccardSims;
	private static double[][] OverlapCoefs;
	public static double[][] Weights;
	
	private static ArrayList<Integer> not_intersect_edges_u;
	private static ArrayList<Integer>[] intersect_edges;
	
	public static void init(int strategy) throws InterruptedException {
		Iterate.strategy = strategy;
		
		OverLap();
		
		switch (strategy) {
		case 0:
		case 1:
		case 2:
		case 3:
		case 4:
		case 5:
		case 6:
		case 7:
		case 8:
		case 9:
		case 10:
		case 99:
			
			JaccardSim();
			OverlapCoef();
			Weight();
			
			break;
		}
	}

	private static class OverLap extends Thread{
		private int idx_first;
		private int idx_last;
		
		OverLap (int idx_first, int idx_last) throws InterruptedException {
			this.idx_first = idx_first;
			this.idx_last = idx_last;
			start();
		}
		
		public void run() {
			int[] Nu;
			int[] Nv;
			int v, ptr_u, ptr_v, u_neighbor, v_neighbor;
			for (int u = idx_first; u < idx_last; u ++) {
				Nu = Graph.ADJ_vID[u];
				
				for (int idx_v = 0; idx_v < Nu.length; idx_v++) {
					v = Nu[idx_v];
					Nv = Graph.ADJ_vID[v];
					int overlapSize = 2;
					
					ptr_u = 0;
					ptr_v = 0;
					u_neighbor = 0;
					v_neighbor = 0;
					while (ptr_u < Nu.length && ptr_v < Nv.length) {
						u_neighbor = Nu[ptr_u];
						v_neighbor = Nv[ptr_v];
						
						if (u_neighbor == v_neighbor) {
							overlapSize++;
							
							ptr_u++;
							ptr_v++;
							
							continue;
						}
						
						if (Graph.ID2Rank[u_neighbor] < Graph.ID2Rank[v_neighbor]) {
							ptr_u++;
						} else if (Graph.ID2Rank[u_neighbor] > Graph.ID2Rank[v_neighbor]) {
							ptr_v++;
						} else {
							if (u_neighbor < v_neighbor) {
								ptr_u++;
							} else if (u_neighbor > v_neighbor) {
								ptr_v++;
							}
						}
					}
					
					OverLaps[u][idx_v] = overlapSize;
				}
			}
			
			latch.countDown();
		}
	}
	
	private static void OverLap() throws InterruptedException {

		OverLaps = new double[Graph.ADJ_vID.length][];

		int vertexSize = Graph.getVertexSize();

		for (int u = 0; u < vertexSize; u++) {
			OverLaps[u] = new double[Graph.ADJ_vID[u].length];
		}

		// for each node u in graph
		int batchNum = 256;
		latch = new CountDownLatch(batchNum);
		int batchSize = (int) Math.ceil((double) vertexSize / (double) (batchNum));
		
		int threadNum = 0;
		for (int idx_u_first = 0; idx_u_first < vertexSize; idx_u_first += batchSize) {
			int idx_u_last = Math.min((idx_u_first + batchSize), vertexSize);
			OverLap = new OverLap(idx_u_first, idx_u_last);
			
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
	}

	private static void JaccardSim() {
		JaccardSims = new double[Graph.ADJ_vID.length][];

		int vertexSize = Graph.getVertexSize();

		for (int u = 0; u < vertexSize; u++) {
			JaccardSims[u] = new double[Graph.ADJ_vID[u].length];
		}

		int idx_u = -1;
		for (int u = 0; u < vertexSize; u++) {
			idx_u++;

			int idx_v = -1;
			for (int v : Graph.ADJ_vID[u]) {
				idx_v++;

				JaccardSims[idx_u][idx_v] = OverLaps[idx_u][idx_v]
						/ (Graph.ADJ_vID[u].length + Graph.ADJ_vID[v].length - OverLaps[idx_u][idx_v]);
			}
		}
	}

	private static void OverlapCoef() {
		OverlapCoefs = new double[Graph.ADJ_vID.length][];

		int vertexSize = Graph.getVertexSize();

		for (int u = 0; u < vertexSize; u++) {
			OverlapCoefs[u] = new double[Graph.ADJ_vID[u].length];
		}

		int idx_u = -1;
		for (int u = 0; u < vertexSize; u++) {
			idx_u++;

			int idx_v = -1;
			for (int v : Graph.ADJ_vID[u]) {
				idx_v++;

				OverlapCoefs[idx_u][idx_v] = OverLaps[idx_u][idx_v]
						/ ((double) Math.min(Graph.ADJ_vID[u].length, Graph.ADJ_vID[v].length));
			}
		}
	}

	private static void Weight() {
		
		// strategy = 0: all equal
		// strategy = 1: on jaccard similarity
		// strategy = 2: on overlapping coefficient
		switch (Iterate.strategy) {
		case 0:
		case 3:
		case 8:
		case 99:
			Weights = new double[Graph.ADJ_vID.length][];

			int vertexSize = Graph.getVertexSize();
			int neighborSize = 0;
			for (int idx_u = 0; idx_u < vertexSize; idx_u++) {
				
				neighborSize = Graph.ADJ_vID[idx_u].length;
				Weights[idx_u] = new double[neighborSize];
				
				for (int idx_v = 0; idx_v < neighborSize; idx_v++) {
					Weights[idx_u][idx_v] = 1;
				}
			}
			break;
		case 1:
		case 4:
		case 6:
		case 9:
			Weights = JaccardSims;
			break;
		case 2:
		case 5:
		case 7:
		case 10:
			Weights = OverlapCoefs;
			break;
		}
	}

	// for strategy 99, 0-10
	private static class Distance2Similarity extends Thread{
		private int idx_first;
		private int idx_last;
		
		Distance2Similarity (int idx_first, int idx_last) throws InterruptedException {
			this.idx_first = idx_first;
			this.idx_last = idx_last;
			start();
		}
		
		public void run() {
			for (int edgeID = idx_first; edgeID < idx_last; edgeID ++) {
				double weight = Graph.Edges[edgeID].weight;
				
				if (weight >= Constant.large) {
					oldSims[edgeID] = 0;
					newWeights[edgeID] = 0;
				} else if (weight <= 0) {
					oldSims[edgeID] = Constant.large;
					newWeights[edgeID] = Constant.large;
				} else {
					oldSims[edgeID] = ((double) 1 / weight);
					newWeights[edgeID] = ((double) 1 / weight);
				}
			}
			
			latch.countDown();
		}
	}
	
	private static class Similarity2Distance extends Thread{
		private int idx_first;
		private int idx_last;
		
		Similarity2Distance (int idx_first, int idx_last) throws InterruptedException {
			this.idx_first = idx_first;
			this.idx_last = idx_last;
			start();
		}
		
		public void run() {
			for (int edgeID = idx_first; edgeID < idx_last; edgeID ++) {
				double sim = newWeights[edgeID];
				
				if (sim >= Constant.large) {
					Graph.Edges[edgeID].weight = 0;
					oldSims[edgeID] = Constant.large;
				} else if (sim <= 0) {
					Graph.Edges[edgeID].weight =  Constant.large;
					oldSims[edgeID] = 0;
				} else {
					Graph.Edges[edgeID].weight = ((double) 1 / sim);
					oldSims[edgeID] = sim;
				}
			}
			
			latch.countDown();
		}
	}
	
	private static class StructuralSimilarity extends Thread{
		private int idx_first;
		private int idx_last;
		
		StructuralSimilarity (int idx_first, int idx_last) throws InterruptedException {
			this.idx_first = idx_first;
			this.idx_last = idx_last;
			start();
		}
		
		public void run() {
			int neighborSize = 0;
			int edgeID = 0;
			int v = 0;
			for (int u = idx_first; u < idx_last; u++) {
				neighborSize = Graph.ADJ_vID[u].length;
				
				for (int idx_v = 0; idx_v < neighborSize; idx_v++) {
					edgeID = Graph.ADJ_eID[u][idx_v];
					v = Graph.ADJ_vID[u][idx_v];
					
					if (u > v) continue;
					
					double cosineSim = OverLaps[u][idx_v] / Math.sqrt((Graph.ADJ_vID[u].length + 1) * (Graph.ADJ_vID[v].length + 1));
//					double overlapCoefficient = OverLaps[u][idx_v] / ((double) Math.min((Graph.ADJ_vID[u].length + 1), (Graph.ADJ_vID[v].length + 1)));
					
					structuralSimilarity[edgeID] = cosineSim;
				}
			}
			
			latch.countDown();
		}
	}
	
	private static class StructuralSimilarity_weighted extends Thread{
		private int idx_first;
		private int idx_last;
		
		private ArrayList<Integer>[] intersect_edges;
		
		StructuralSimilarity_weighted (int idx_first, int idx_last) throws InterruptedException {
			this.idx_first = idx_first;
			this.idx_last = idx_last;
			start();
		}
		
		public void run() {
			int neighborSize = 0;
			int edgeID = 0;
			int v = 0;
			for (int u = idx_first; u < idx_last; u++) {
				neighborSize = Graph.ADJ_vID[u].length;
				
				for (int idx_v = 0; idx_v < neighborSize; idx_v++) {
					edgeID = Graph.ADJ_eID[u][idx_v];
					v = Graph.ADJ_vID[u][idx_v];
					
					if (u > v) continue;
					
					commonNeighbor(u, v);
					
					// cosine similarity
					// for each common neighbors
					int edge_ux, edge_vx;
					double nominator = 0;
					
					// 5 / 9
					int edgeSize = intersect_edges[0].size();
					for (int i = 0; i < edgeSize; i++) {
						edge_ux = intersect_edges[0].get(i);
						edge_vx = intersect_edges[1].get(i);
						
						nominator += (Graph.Edges[edge_ux].weight + Graph.Edges[edge_vx].weight);
					}
					
					nominator += (Graph.Edges[edgeID].weight * 2);
					
					double denominator = 0;
					for (int i : Graph.ADJ_eID[u]) {
						denominator += Graph.Edges[i].weight;
					}
					
					for (int i : Graph.ADJ_eID[v]) {
						denominator += Graph.Edges[i].weight;
					}
					
					double weightCosineSim = nominator / denominator;
					
					structuralSimilarity_weighted[edgeID] = weightCosineSim;
				}
			}
			
			latch.countDown();
		}
		
		private void commonNeighbor(int u, int v) {
			int[] Nu = Graph.ADJ_vID[u];
			int[] Nu_edge = Graph.ADJ_eID[u];
			int[] Nv = Graph.ADJ_vID[v];
			int[] Nv_edge = Graph.ADJ_eID[v];
			
			int length = 0;
			if (Nu.length <= Nv.length) {
				length = Nu.length;
			} else {
				length = Nv.length;
			}
			
			intersect_edges = new ArrayList[2];
			intersect_edges[0] = new ArrayList<Integer>(length);
			intersect_edges[1] = new ArrayList<Integer>(length);
			
			int ptr_u = 0;
			int ptr_v = 0;
			int u_neighbor = 0;
			int v_neighbor = 0;
			int u_edge = 0;
			int v_edge = 0;
			while (ptr_u < Nu.length && ptr_v < Nv.length) {
				
				u_neighbor = Nu[ptr_u];
				v_neighbor = Nv[ptr_v];
				u_edge = Nu_edge[ptr_u];
				v_edge = Nv_edge[ptr_v];
				
				if (u_neighbor == v_neighbor) {
					
					intersect_edges[0].add(u_edge);
					intersect_edges[1].add(v_edge);
					
					ptr_u++;
					ptr_v++;
					
					continue;
				}
				
				if (Graph.ID2Rank[u_neighbor] < Graph.ID2Rank[v_neighbor]) {
					ptr_u++;
					
				} else if (Graph.ID2Rank[u_neighbor] > Graph.ID2Rank[v_neighbor]) {
					ptr_v++;
					
				} else {
					if (u_neighbor < v_neighbor) {
						ptr_u++;
						
					} else if (u_neighbor > v_neighbor) {
						ptr_v++;
						
					}
				}
			}
		}
	}
	
	public static double iterate_upadteAll_parallel(int itrTimes) throws InterruptedException {
		
		System.out.println("strategy " + Iterate.strategy + " itrTimes " + itrTimes);
		
		Iterate.itrTimes = itrTimes;
		
		if (itrTimes <= 0)
			return 0;

		newWeights = new double[Graph.getEdgeSize()];
		oldSims = new double[Graph.getEdgeSize()];
		structuralSimilarity = new double[Graph.getEdgeSize()];
		structuralSimilarity_weighted = new double[Graph.getEdgeSize()];
		
		long start = System.currentTimeMillis();
		
		switch (strategy) {
			
		case 16:
			{
				// transform distance to weighted cosine similarity
				int vertexSize = Graph.getVertexSize();
				int batchNum = 256;
				latch = new CountDownLatch(batchNum);
				int batchSize = (int) Math.ceil((double) vertexSize / (double) batchNum);
				
				int threadNum = 0;
				for (int idx_u_first = 0; idx_u_first < vertexSize; idx_u_first += batchSize) {
					int idx_u_last = Math.min((idx_u_first + batchSize), vertexSize);
					StructuralSimilarity_weighted = new StructuralSimilarity_weighted(idx_u_first, idx_u_last);
					
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
				
				// transform distance to cosine similarity
				vertexSize = Graph.getVertexSize();
				batchNum = 256;
				latch = new CountDownLatch(batchNum);
				batchSize = (int) Math.ceil((double) vertexSize / (double) batchNum);
				
				threadNum = 0;
				for (int idx_u_first = 0; idx_u_first < vertexSize; idx_u_first += batchSize) {
					int idx_u_last = Math.min((idx_u_first + batchSize), vertexSize);
					StructuralSimilarity = new StructuralSimilarity(idx_u_first, idx_u_last);
					
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
				
				// transform distance to similarity
				int edgeSize = Graph.getEdgeSize();
				batchNum = 256;
				latch = new CountDownLatch(batchNum);
				batchSize = (int) Math.ceil((double) edgeSize / (double) batchNum);
				
				threadNum = 0;
				for (int idx_u_first = 0; idx_u_first < edgeSize; idx_u_first += batchSize) {
					int idx_u_last = Math.min((idx_u_first + batchSize), edgeSize);
					Distance2Similarity = new Distance2Similarity(idx_u_first, idx_u_last);
					
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
				
				maxWeight = -1;
				// find maximum edge weight 
				for (int i = 0; i < Graph.getEdgeSize(); i++) if (maxWeight < structuralSimilarity_weighted[i]) maxWeight = structuralSimilarity_weighted[i];
			}
			
			break;
		}

		for (int itr = 0; itr < itrTimes; itr++) {
			System.out.print("itr " + itr + "=>");
			
			switch (strategy) {
			
			case 16:
				{
					
					System.out.println("EPSLON " + EPSLON + " MU " + MU);
					
					// get weight changes
					int neighborSize, edgeID, v;
					for (int idx_u = 0; idx_u < Graph.getVertexSize(); idx_u++) {
						neighborSize = Graph.ADJ_vID[idx_u].length;
						int degree = 1;
						
						for (int idx_v = 0; idx_v < neighborSize; idx_v++) {
							edgeID = Graph.ADJ_eID[idx_u][idx_v];
							
							if (structuralSimilarity_weighted[edgeID] >= EPSLON * maxWeight) degree++;
						}
						
						// check whether it can be core
						if ((Graph.ADJ_vID[idx_u].length + 1) >= MU) {
							if (degree >= MU) {
								for (int idx_v = 0; idx_v < neighborSize; idx_v++) {
									edgeID = Graph.ADJ_eID[idx_u][idx_v];
									
									v = Graph.ADJ_vID[idx_u][idx_v];
									findNodes(idx_u, v);
									
									newWeights[edgeID] -= DI_ss(edgeID, idx_u);
									newWeights[edgeID] -= CI_ss(edgeID, idx_u);
								}
							} else {
								for (int idx_v = 0; idx_v < neighborSize; idx_v++) {
									edgeID = Graph.ADJ_eID[idx_u][idx_v];
									
									v = Graph.ADJ_vID[idx_u][idx_v];
									findNodes(idx_u, v);
									
									newWeights[edgeID] -= DI_ss(edgeID, idx_u);
									newWeights[edgeID] -= CI_ss(edgeID, idx_u);
									newWeights[edgeID] -= EI_ss(edgeID, idx_u);
								}
							}
							
						} else {
							for (int idx_v = 0; idx_v < neighborSize; idx_v++) {
								edgeID = Graph.ADJ_eID[idx_u][idx_v];
								
								v = Graph.ADJ_vID[idx_u][idx_v];
								findNodes(idx_u, v);
								
								newWeights[edgeID] -= EI_ss(edgeID, idx_u);
							}
						}
					}
					
					// transform similarity to distance and update edge weight
					int edgeSize = Graph.getEdgeSize();
					int batchNum = 256;
					latch = new CountDownLatch(batchNum);
					int batchSize = (int) Math.ceil((double) edgeSize / (double) batchNum);
					
					int threadNum = 0;
					for (int idx_u_first = 0; idx_u_first < edgeSize; idx_u_first += batchSize) {
						int idx_u_last = Math.min((idx_u_first + batchSize), edgeSize);
						Similarity2Distance = new Similarity2Distance(idx_u_first, idx_u_last);
						
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
				}
				
				break;
			}
		}
		
		System.out.println();

		long end = System.currentTimeMillis();
		return (end - start) / Constant.RUNNING_TIME_UNIT;
	}
	
	public static double iterate_upadteAll(int itrTimes) throws InterruptedException {
		
		System.out.println("strategy " + Iterate.strategy + " itrTimes " + itrTimes);
		
		Iterate.itrTimes = itrTimes;
		
		if (itrTimes <= 0)
			return 0;

//		newWeights = new double[Graph.getEdgeSize()];
//		oldSims = new double[Graph.getEdgeSize()];
//		structuralSimilarity = new double[Graph.getEdgeSize()];
//		structuralSimilarity_weighted = new double[Graph.getEdgeSize()];
		
		long start = System.currentTimeMillis();
		
		switch (strategy) {
			
		case 16:
			{
				// transform distance to weighted cosine similarity
				int vertexSize = Graph.getVertexSize();
				int neighborSize = 0;
				int edgeID = 0;
				int v = 0;
				for (int u = 0; u < vertexSize; u++) {
					neighborSize = Graph.ADJ_vID[u].length;
					
					for (int idx_v = 0; idx_v < neighborSize; idx_v++) {
						edgeID = Graph.ADJ_eID[u][idx_v];
						v = Graph.ADJ_vID[u][idx_v];
						
						if (u > v) continue;
						
						commonNeighbor(u, v);
						
						// cosine similarity
						// for each common neighbors
						int edge_ux, edge_vx;
						double nominator = 0;
						
						// 5 / 9
						int edgeSize = intersect_edges[0].size();
						for (int i = 0; i < edgeSize; i++) {
							edge_ux = intersect_edges[0].get(i);
							edge_vx = intersect_edges[1].get(i);
							
							nominator += (Graph.Edges[edge_ux].weight + Graph.Edges[edge_vx].weight);
						}
						
						nominator += (Graph.Edges[edgeID].weight * 2);
						
						double denominator = 0;
						for (int i : Graph.ADJ_eID[u]) {
							denominator += Graph.Edges[i].weight;
						}
						
						for (int i : Graph.ADJ_eID[v]) {
							denominator += Graph.Edges[i].weight;
						}
						
						double weightCosineSim = nominator / denominator;
						
						structuralSimilarity_weighted[edgeID] = weightCosineSim;
					}
				}
				
				// transform distance to cosine similarity
				neighborSize = 0;
				edgeID = 0;
				v = 0;
				for (int u = 0; u < vertexSize; u++) {
					neighborSize = Graph.ADJ_vID[u].length;
					
					for (int idx_v = 0; idx_v < neighborSize; idx_v++) {
						edgeID = Graph.ADJ_eID[u][idx_v];
						v = Graph.ADJ_vID[u][idx_v];
						
						if (u > v) continue;
						
						double cosineSim = OverLaps[u][idx_v] / Math.sqrt((Graph.ADJ_vID[u].length + 1) * (Graph.ADJ_vID[v].length + 1));
//						double overlapCoefficient = OverLaps[u][idx_v] / ((double) Math.min((Graph.ADJ_vID[u].length + 1), (Graph.ADJ_vID[v].length + 1)));
						
						structuralSimilarity[edgeID] = cosineSim;
					}
				}
				
				// transform distance to similarity
				int edgeSize = Graph.getEdgeSize();
				for (edgeID = 0; edgeID < edgeSize; edgeID ++) {
					double weight = Graph.Edges[edgeID].weight;
					
					if (weight >= Constant.large) {
						oldSims[edgeID] = 0;
						newWeights[edgeID] = 0;
					} else if (weight <= 0) {
						oldSims[edgeID] = Constant.large;
						newWeights[edgeID] = Constant.large;
					} else {
						oldSims[edgeID] = ((double) 1 / weight);
						newWeights[edgeID] = ((double) 1 / weight);
					}
				}
				
				maxWeight = -1;
				// find maximum edge weight 
				for (int i = 0; i < Graph.getEdgeSize(); i++) if (maxWeight < structuralSimilarity_weighted[i]) maxWeight = structuralSimilarity_weighted[i];
			}
			
			break;
		}

		for (int itr = 0; itr < itrTimes; itr++) {
			System.out.print("itr " + itr + "=>");
			
			switch (strategy) {
			
			case 16:
				{
					
					System.out.println("EPSLON " + EPSLON + " MU " + MU);
					
					// get weight changes
					int neighborSize, edgeID, v;
					for (int idx_u = 0; idx_u < Graph.getVertexSize(); idx_u++) {
						neighborSize = Graph.ADJ_vID[idx_u].length;
						int degree = 1;
						
						for (int idx_v = 0; idx_v < neighborSize; idx_v++) {
							edgeID = Graph.ADJ_eID[idx_u][idx_v];
							
							if (structuralSimilarity_weighted[edgeID] >= EPSLON * maxWeight) degree++;
						}
						
						// check whether it can be core
						if ((Graph.ADJ_vID[idx_u].length + 1) >= MU) {
							if (degree >= MU) {
								for (int idx_v = 0; idx_v < neighborSize; idx_v++) {
									edgeID = Graph.ADJ_eID[idx_u][idx_v];
									
									v = Graph.ADJ_vID[idx_u][idx_v];
									findNodes(idx_u, v);
									
									newWeights[edgeID] -= DI_ss(edgeID, idx_u);
									newWeights[edgeID] -= CI_ss(edgeID, idx_u);
								}
							} else {
								for (int idx_v = 0; idx_v < neighborSize; idx_v++) {
									edgeID = Graph.ADJ_eID[idx_u][idx_v];
									
									v = Graph.ADJ_vID[idx_u][idx_v];
									findNodes(idx_u, v);
									
									newWeights[edgeID] -= DI_ss(edgeID, idx_u);
									newWeights[edgeID] -= CI_ss(edgeID, idx_u);
									newWeights[edgeID] -= EI_ss(edgeID, idx_u);
								}
							}
							
						} else {
							for (int idx_v = 0; idx_v < neighborSize; idx_v++) {
								edgeID = Graph.ADJ_eID[idx_u][idx_v];
								
								v = Graph.ADJ_vID[idx_u][idx_v];
								findNodes(idx_u, v);
								
								newWeights[edgeID] -= EI_ss(edgeID, idx_u);
							}
						}
					}
					
					// transform similarity to distance and update edge weight
					int edgeSize = Graph.getEdgeSize();
					for (edgeID = 0; edgeID < edgeSize; edgeID ++) {
						double sim = newWeights[edgeID];
						
						if (sim >= Constant.large) {
							Graph.Edges[edgeID].weight = 0;
							oldSims[edgeID] = Constant.large;
						} else if (sim <= 0) {
							Graph.Edges[edgeID].weight =  Constant.large;
							oldSims[edgeID] = 0;
						} else {
							Graph.Edges[edgeID].weight = ((double) 1 / sim);
							oldSims[edgeID] = sim;
						}
					}
				}
				
				break;
			}
		}
		
		System.out.println();

		long end = System.currentTimeMillis();
		return (end - start) / Constant.RUNNING_TIME_UNIT;
	}
	
	public static double local_iterate(int itrTimes, HashSet<Integer> edgeIDs) {
		
		Iterate.itrTimes = itrTimes;
		
		if (itrTimes <= 0)
			return 0;
		
		ArrayList<Integer> activeNodes = new ArrayList<Integer>();
		
		Iterator<Integer> iterator = edgeIDs.iterator();
	    while(iterator.hasNext()){
	    	Edge activeEdge = Graph.Edges[iterator.next()];
	    	activeNodes.add(activeEdge.v_1);
			activeNodes.add(activeEdge.v_2);
	    }
		
		long start = System.currentTimeMillis();
		
		switch (strategy) {
			
		case 16:
			{
//				int edgeID;
//				for (int u : activeNodes) {
//					int neighborSize = Graph.ADJ_vID[u].length;
//					
//					for (int idx_v = 0; idx_v < neighborSize; idx_v++) {
//						edgeID = Graph.ADJ_eID[u][idx_v];
//						int v = Graph.ADJ_vID[u][idx_v];
//						
////						if (u > v) continue;
//						
//						commonNeighbor(u, v);
//						
//						// cosine similarity
//						// for each common neighbors
//						int edge_ux, edge_vx;
//						double nominator = 0;
//						
//						// 5 / 9
//						int edgeSize = intersect_edges[0].size();
//						for (int i = 0; i < edgeSize; i++) {
//							edge_ux = intersect_edges[0].get(i);
//							edge_vx = intersect_edges[1].get(i);
//							
//							nominator += (Graph.Edges[edge_ux].weight + Graph.Edges[edge_vx].weight);
//						}
//						
//						nominator += (Graph.Edges[edgeID].weight * 2);
//						
//						double denominator = 0;
//						for (int i : Graph.ADJ_eID[u]) {
//							denominator += Graph.Edges[i].weight;
//						}
//						
//						for (int i : Graph.ADJ_eID[v]) {
//							denominator += Graph.Edges[i].weight;
//						}
//						
//						structuralSimilarity_weighted[edgeID] = nominator / denominator;
//					}
//				}
				
				// transform distance to weighted cosine similarity
				int vertexSize = Graph.getVertexSize();
				int neighborSize = 0;
				int edgeID = 0;
				int v = 0;
				for (int u = 0; u < vertexSize; u++) {
					neighborSize = Graph.ADJ_vID[u].length;
					
					for (int idx_v = 0; idx_v < neighborSize; idx_v++) {
						edgeID = Graph.ADJ_eID[u][idx_v];
						v = Graph.ADJ_vID[u][idx_v];
						
						if (u > v) continue;
						
						commonNeighbor(u, v);
						
						// cosine similarity
						// for each common neighbors
						int edge_ux, edge_vx;
						double nominator = 0;
						
						// 5 / 9
						int edgeSize = intersect_edges[0].size();
						for (int i = 0; i < edgeSize; i++) {
							edge_ux = intersect_edges[0].get(i);
							edge_vx = intersect_edges[1].get(i);
							
							nominator += (Graph.Edges[edge_ux].weight + Graph.Edges[edge_vx].weight);
						}
						
						nominator += (Graph.Edges[edgeID].weight * 2);
						
						double denominator = 0;
						for (int i : Graph.ADJ_eID[u]) {
							denominator += Graph.Edges[i].weight;
						}
						
						for (int i : Graph.ADJ_eID[v]) {
							denominator += Graph.Edges[i].weight;
						}
						
						double weightCosineSim = nominator / denominator;
						
						structuralSimilarity_weighted[edgeID] = weightCosineSim;
					}
				}
				
				// transform distance to similarity
				int edgeSize = Graph.getEdgeSize();
				for (edgeID = 0; edgeID < edgeSize; edgeID ++) {
					double weight = Graph.Edges[edgeID].weight;
					
					if (weight >= Constant.large) {
						oldSims[edgeID] = 0;
						newWeights[edgeID] = 0;
					} else if (weight <= 0) {
						oldSims[edgeID] = Constant.large;
						newWeights[edgeID] = Constant.large;
					} else {
						oldSims[edgeID] = ((double) 1 / weight);
						newWeights[edgeID] = ((double) 1 / weight);
					}
				}
				
				maxWeight = -1;
				// find maximum edge weight 
				for (int i = 0; i < Graph.getEdgeSize(); i++) {
					if (maxWeight < structuralSimilarity_weighted[i]) maxWeight = structuralSimilarity_weighted[i];
				}
			}
			
			break;
		}

		for (int itr = 0; itr < itrTimes; itr++) {
			
			switch (strategy) {
			
			case 16:
				{
					// get weight changes
					int neighborSize, edgeID, v;
					for (int idx_u : activeNodes) {
						
						neighborSize = Graph.ADJ_vID[idx_u].length;
						int degree = 1;
						
						for (int idx_v = 0; idx_v < neighborSize; idx_v++) {
							edgeID = Graph.ADJ_eID[idx_u][idx_v];
							
							if (structuralSimilarity_weighted[edgeID] >= EPSLON * maxWeight) degree++;
						}
						
						// check whether it can be core
						if ((Graph.ADJ_vID[idx_u].length + 1) >= MU) {
							if (degree >= MU) {
								for (int idx_v = 0; idx_v < neighborSize; idx_v++) {
									edgeID = Graph.ADJ_eID[idx_u][idx_v];
									
									v = Graph.ADJ_vID[idx_u][idx_v];
									findNodes(idx_u, v);
									
									newWeights[edgeID] -= DI_ss(edgeID, idx_u);
									newWeights[edgeID] -= CI_ss(edgeID, idx_u);
								}
							} else {
								for (int idx_v = 0; idx_v < neighborSize; idx_v++) {
									edgeID = Graph.ADJ_eID[idx_u][idx_v];
									
									v = Graph.ADJ_vID[idx_u][idx_v];
									findNodes(idx_u, v);
									
									newWeights[edgeID] -= DI_ss(edgeID, idx_u);
									newWeights[edgeID] -= CI_ss(edgeID, idx_u);
									newWeights[edgeID] -= EI_ss(edgeID, idx_u);
								}
							}
							
						} else {
							for (int idx_v = 0; idx_v < neighborSize; idx_v++) {
								edgeID = Graph.ADJ_eID[idx_u][idx_v];
								
								v = Graph.ADJ_vID[idx_u][idx_v];
								findNodes(idx_u, v);
								
								newWeights[edgeID] -= EI_ss(edgeID, idx_u);
							}
						}
					}
					
					// transform similarity to distance and update edge weight
					int edgeSize = Graph.getEdgeSize();
					for (edgeID = 0; edgeID < edgeSize; edgeID ++) {
						double sim = newWeights[edgeID];
						
						if (sim >= Constant.large) {
							Graph.Edges[edgeID].weight = 0;
							oldSims[edgeID] = Constant.large;
						} else if (sim <= 0) {
							Graph.Edges[edgeID].weight =  Constant.large;
							oldSims[edgeID] = 0;
						} else {
							Graph.Edges[edgeID].weight = ((double) 1 / sim);
							oldSims[edgeID] = sim;
						}
					}
				}
				
				break;
			}
		}

		long end = System.currentTimeMillis();
		return (end - start) / Constant.RUNNING_TIME_UNIT;
	}
	
	private static void commonNeighbor(int u, int v) {
		int[] Nu = Graph.ADJ_vID[u];
		int[] Nu_edge = Graph.ADJ_eID[u];
		int[] Nv = Graph.ADJ_vID[v];
		int[] Nv_edge = Graph.ADJ_eID[v];
		
		int length = 0;
		if (Nu.length <= Nv.length) {
			length = Nu.length;
		} else {
			length = Nv.length;
		}
		
		intersect_edges = new ArrayList[2];
		intersect_edges[0] = new ArrayList<Integer>(length);
		intersect_edges[1] = new ArrayList<Integer>(length);
		
		int ptr_u = 0;
		int ptr_v = 0;
		int u_neighbor = 0;
		int v_neighbor = 0;
		int u_edge = 0;
		int v_edge = 0;
		while (ptr_u < Nu.length && ptr_v < Nv.length) {
			
			u_neighbor = Nu[ptr_u];
			v_neighbor = Nv[ptr_v];
			u_edge = Nu_edge[ptr_u];
			v_edge = Nv_edge[ptr_v];
			
			if (u_neighbor == v_neighbor) {
				
				intersect_edges[0].add(u_edge);
				intersect_edges[1].add(v_edge);
				
				ptr_u++;
				ptr_v++;
				
				continue;
			}
			
			if (Graph.ID2Rank[u_neighbor] < Graph.ID2Rank[v_neighbor]) {
				ptr_u++;
				
			} else if (Graph.ID2Rank[u_neighbor] > Graph.ID2Rank[v_neighbor]) {
				ptr_v++;
				
			} else {
				if (u_neighbor < v_neighbor) {
					ptr_u++;
					
				} else if (u_neighbor > v_neighbor) {
					ptr_v++;
					
				}
			}
		}
	}
	
	private static double invDeg(int u) {
		return ((double) 1 / Graph.ADJ_vID[u].length); 
	}
	
	public static double DI(int edgeID, int u) {
		return (-1) * oldSims[edgeID] * invDeg(u);
	}
	
	public static double DI_ss(int edgeID, int u) {
		return (-1) * oldSims[edgeID] * invDeg(u) * structuralSimilarity[edgeID];
	}
	
	public static double CI(int u) {
		double CI = 0;
		
		int edgeSize = intersect_edges[0].size();
		for(int i = 0; i < edgeSize; i++) {
			CI = CI + invDeg(u) * Math.sqrt(oldSims[intersect_edges[0].get(i)] * oldSims[intersect_edges[1].get(i)]);
		}
		
		return (-1) * CI;
	}
	
	public static double CI_ss(int edgeID, int u) {
		double CI = 0;
		
		int edgeSize = intersect_edges[0].size();
		int edge_ux, edge_vx;
		for(int i = 0; i < edgeSize; i++) {
			edge_ux = intersect_edges[0].get(i);
			edge_vx = intersect_edges[1].get(i);
			
			CI = CI + invDeg(u) * structuralSimilarity[edgeID] * Math.sqrt(oldSims[edge_ux] * oldSims[edge_vx]);
		}
		
		return (-1) * CI;
	}
	
	public static double EI(int u) {
		double EI = 0;
		
		int edgeSize = not_intersect_edges_u.size();
		for(int i = 0; i < edgeSize; i++) {
			EI -= (invDeg(u) * oldSims[not_intersect_edges_u.get(i)]);
		}

		return (-1) * EI;
	}
	
	public static double EI_ss(int edgeID, int u) {
		double EI = 0;
		
		int edgeSize = not_intersect_edges_u.size();
		int edge_ux;
		for(int i = 0; i < edgeSize; i++) {
			edge_ux = not_intersect_edges_u.get(i);
			
			EI -= (invDeg(u) * structuralSimilarity[edgeID] * oldSims[edge_ux]);
		}

		return (-1) * EI;
	}
	
	private static void findNodes(int u, int v) {
		int[] Nu = Graph.ADJ_vID[u];
		int[] Nu_edge = Graph.ADJ_eID[u];
		int[] Nv = Graph.ADJ_vID[v];
		int[] Nv_edge = Graph.ADJ_eID[v];
		
		int length = 0;
		if (Nu.length <= Nv.length) {
			length = Nu.length;
		} else {
			length = Nv.length;
		}
		
		not_intersect_edges_u = new ArrayList<Integer>(Nu.length);
		intersect_edges = new ArrayList[2];
		intersect_edges[0] = new ArrayList<Integer>(length);
		intersect_edges[1] = new ArrayList<Integer>(length);
		
		int ptr_u = 0;
		int ptr_v = 0;
		int u_neighbor = 0;
		int v_neighbor = 0;
		int u_edge = 0;
		int v_edge = 0;
		while (ptr_u < Nu.length && ptr_v < Nv.length) {
			
			u_neighbor = Nu[ptr_u];
			v_neighbor = Nv[ptr_v];
			u_edge = Nu_edge[ptr_u];
			v_edge = Nv_edge[ptr_v];
			
			if (u_neighbor == v_neighbor) {
				
				intersect_edges[0].add(u_edge);
				intersect_edges[1].add(v_edge);
				
				ptr_u++;
				ptr_v++;
				
				continue;
			}
			
			if (Graph.ID2Rank[u_neighbor] < Graph.ID2Rank[v_neighbor]) {
				not_intersect_edges_u.add(u_edge);
				ptr_u++;
				
			} else if (Graph.ID2Rank[u_neighbor] > Graph.ID2Rank[v_neighbor]) {
				ptr_v++;
				
			} else {
				if (u_neighbor < v_neighbor) {
					not_intersect_edges_u.add(u_edge);
					ptr_u++;
					
				} else if (u_neighbor > v_neighbor) {
					ptr_v++;
					
				}
			}
		}
		
		while (ptr_u < Nu.length) {
			not_intersect_edges_u.add(Nu_edge[ptr_u]);
			ptr_u++;
		}
	}
	
	public static void saveEdgeWeight() {
		try {
			FileWriter fwCount = new FileWriter(FilePath_Mon.filePathPre + "/edgeWeight_strategy_" + Iterate.strategy + "_itrTimes_" + Iterate.itrTimes  + ".txt");

			// save
			int counter = 0;
			for (int i = 0; i < Graph.getVertexSize(); i++) {
				int neighborSize = Graph.ADJ_vID[i].length;

				for (int idx = 0; idx < neighborSize; idx++) {
					fwCount.write(
							i + "," + Graph.ADJ_vID[i][idx] + "," + Graph.Edges[Graph.ADJ_eID[i][idx]].weight + "\n");
				}
			}

			System.out.println(counter);

			fwCount.flush();
			fwCount.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
	}
	
	public static void main(String arg[]) throws IOException, InterruptedException {
		Graph.loadGraph();
		
		Iterate.init(16);
		
//		for (int u = 0; u < Graph.getVertexSize(); u ++) {
//			// for each of u's neighbor v
//			for (int idx_v = 0; idx_v < Graph.ADJ_vID[u].length; idx_v++) {
//				System.out.print(OverLaps[u][idx_v] + " ");
//			}
//			System.out.println();
//		}
		
//		generateUpdateSim(20);

//		findNodes(10);

		for (int i = 0; i < 1; i ++) {
			long start1 = System.currentTimeMillis();
			
			iterate_upadteAll_parallel(3);
			
			long end1 = System.currentTimeMillis();
			System.out.println(end1 - start1);
		}
		
		
//		saveEdgeWeight();
	}
}
