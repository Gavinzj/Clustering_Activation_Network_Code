package ShortestPath;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import Graph.Edge;
import Graph.Graph;
import Utilities.Constant;
import Utilities.FilePath_Mon;

public class SetSPT {
	private int vertexSize;
	private double[] dists;
	private int[] prevs;
	private int[] edges;
	private int[] roots;
	
	private int[] heap_update;
	private int[] revHeap_update;
	private int heapTop_update = 0;
	
	public SetSPT(List<Integer> seeds, int vertexSize) {
		this.dists = new double[vertexSize];
		this.prevs = new int[vertexSize];
		this.edges = new int[vertexSize];
		this.roots = new int[vertexSize];
		this.vertexSize = vertexSize;
		
		this.heap_update = new int[vertexSize];
		this.revHeap_update = new int[vertexSize];
		this.heapTop_update = 0;
		
		// set roots for seeds
		Arrays.fill(this.roots, Constant.UNDERFINE_VALUE);
		for (int seed : seeds) this.roots[seed] = seed;
	}
	
	public void reNew() {
		this.dists = new double[vertexSize];
		this.prevs = new int[vertexSize];
		this.edges = new int[vertexSize];
		
		for (int i = 0; i < roots.length; i ++) {
			if (!isSeed(this.roots[i])) roots[i] = Constant.UNDERFINE_VALUE;
		}
		
		this.heap_update = new int[vertexSize];
		this.revHeap_update = new int[vertexSize];
		this.heapTop_update = 0;
        
        contructSPTs_RH();
	}
	
	public void contructSPTs_RH() {
		boolean reached[] = new boolean[vertexSize];
		int heap[] = new int[vertexSize + 1];
		int revHeap[] = new int[vertexSize + 1];
		double newdis;
		
		// initialization 
		for (int i = 0; i < this.vertexSize; i++) {
			reached[i] = false;
			revHeap[i] = -1;
			
			dists[i] = Constant.large;
			prevs[i] = Constant.UNDERFINE_VALUE;
			edges[i] = Constant.UNDERFINE_VALUE;
		}
		
		// insert elements for seeds
		int cur, tmp; 
		int heapTop = 0;
		for (int idx = 0; idx < this.vertexSize; idx ++) {
			
			if (!isSeed(idx)) continue;
			
			reached[idx] = true;
			
			dists[idx] = 0;
			
			heap[++heapTop] = idx;
			revHeap[idx] = heapTop;
			cur = heapTop;
			
			while ((cur > 1) && (dists[heap[cur]] < dists[heap[cur / 2]])) {
				tmp = heap[cur];
				heap[cur] = heap[cur / 2];
				heap[cur / 2] = tmp;
				revHeap[heap[cur]] = cur; 
				revHeap[heap[cur / 2]] = cur / 2;
				cur /= 2;
			}
		}
		
		int top, bst;
		Edge edge;
		while (heapTop > 0) {
			// Remove the top one;
			top = heap[1];
			
			heap[1] = heap[heapTop--];
			cur = 1;
//			// Expand from the top node;
			
			if (heapTop > 0)
				while (true) {
					bst = cur;
					if (cur * 2 <= heapTop) {
					
						if (dists[heap[cur * 2]] < dists[heap[bst]])
							bst = cur * 2;
					}
						
					if (cur * 2 + 1 <= heapTop) {
						if (dists[heap[cur * 2 + 1]] < dists[heap[bst]])
							bst = cur * 2 + 1;
					}
					
					if (bst == cur)
						break;
					
					tmp = heap[cur];
					heap[cur] = heap[bst];
					heap[bst] = tmp;
					revHeap[heap[cur]] = cur; 
					revHeap[heap[bst]] = bst;
					cur = bst;
				}
			
			// Expand from the top node;
			int [] pointer_vID = Graph.ADJ_vID[top];
			int [] pointer_eID = Graph.ADJ_eID[top];
			int neighborSize = pointer_vID.length;
			
			for (int idx = 0; idx < neighborSize; idx ++) {
				int v = pointer_vID[idx];
				edge = Graph.Edges[pointer_eID[idx]];
				
				newdis = dists[top] + edge.weight;
				
				if (!reached[v]) {
					
					reached[v] = true; 
					
					dists[v] = newdis;
					
					roots[v] = roots[top];
					prevs[v] = top;
					edges[v] = pointer_eID[idx];
					
					heap[++heapTop] = v;
					revHeap[v] = heapTop;
					cur = heapTop;
					
					while ((cur > 1) && (dists[heap[cur]] < dists[heap[cur / 2]])) {
						tmp = heap[cur];
						heap[cur] = heap[cur / 2];
						heap[cur / 2] = tmp;
						revHeap[heap[cur]] = cur; 
						revHeap[heap[cur / 2]] = cur / 2;
						cur /= 2;
					}
				} else {
					if (dists[v] > newdis) {
						
						dists[v] = newdis;
						
						roots[v] = roots[top];
						prevs[v] = top;
						edges[v] = pointer_eID[idx];
						
						cur = revHeap[v];
						while ((cur > 1) && (dists[heap[cur]] < dists[heap[cur / 2]])) {
							heap[cur] = heap[cur / 2];
							heap[cur/2] = v;
							revHeap[heap[cur]] = cur;
							revHeap[heap[cur/2]] = cur / 2; 
							cur /= 2;	
						}
					}
				}
			}
		}
	}
	
	public void update(int modifiedEdgeID) {
		
		// for each modified head v
		int v_1, v_2;
		v_1 = Graph.Edges[modifiedEdgeID].v_1;
		v_2 = Graph.Edges[modifiedEdgeID].v_2;
		
		if (Graph.compareVertex_byID(v_1, v_2) < 0) {
			firstAddToQueue(v_1);
			firstAddToQueue(v_2);
		} else {
			firstAddToQueue(v_2);
			firstAddToQueue(v_1);
		}
		
		while(heapTop_update > 0) {
			// Remove the top one;
			int top_update = heap_update[1];
			
			heap_update[1] = heap_update[heapTop_update--];
			int cur_update = 1;
			int bst_update;
			int tmp_update;
			
			if (heapTop_update > 0) {
				while (true) {
					bst_update = cur_update;
					if (cur_update * 2 <= heapTop_update) {
						if (dists[heap_update[cur_update * 2]] < dists[heap_update[bst_update]])
							bst_update = cur_update * 2;
					}
						
					if (cur_update * 2 + 1 <= heapTop_update) {
						if (dists[heap_update[cur_update * 2 + 1]] < dists[heap_update[bst_update]])
							bst_update = cur_update * 2 + 1;
					}
					
					if (bst_update == cur_update)
						break;
					
					tmp_update = heap_update[cur_update];
					heap_update[cur_update] = heap_update[bst_update];
					heap_update[bst_update] = tmp_update;
					revHeap_update[heap_update[cur_update]] = cur_update; 
					revHeap_update[heap_update[bst_update]] = bst_update;
					cur_update = bst_update;
				}
			}
			
			int x = top_update;
			
//			PROB(x);
			
			// for each neighbors of x, y
			for (int y : Graph.ADJ_vID[x]) {
				
				if (isSeed(y)) {
					continue;
				}
				
				if (PROB(y)) {
					add_to_queue(y, dists[y]);
				}
			}
		}
	}
	
	// handle the case of edge weight increase
	public void update_increase(int modifiedEdgeID) throws IOException {
		
		int o = -1;
		
		// for each modified head v
		int v_1, v_2;
		v_1 = Graph.Edges[modifiedEdgeID].v_1;
		v_2 = Graph.Edges[modifiedEdgeID].v_2;
				
		if (prevs[v_1] == v_2) o = v_1;
		else if (prevs[v_2] == v_1) o = v_2;
		else return;
		
		HashSet<Integer> treeNodes = new HashSet<Integer>();
		HashSet<Integer> borderNodes = new HashSet<Integer>();
		
		Queue<Integer> queue = new LinkedList<Integer>();
		queue.add(o);
		
		int v = -1;
		int u = -1;
		int[] pointer_vID = null;
		int neighborSize = -1;
		while (!queue.isEmpty()) {
			
			u = queue.remove();
			treeNodes.add(u);
			
			dists[u] = Constant.large;
			prevs[u]= Constant.UNDERFINE_VALUE;
			edges[u] = Constant.UNDERFINE_VALUE;
			roots[u] = Constant.UNDERFINE_VALUE;
			
			// for each neighbors, check whether it is the child of u
			pointer_vID = Graph.ADJ_vID[u];
			neighborSize = pointer_vID.length;
			for (int idx = 0; idx < neighborSize; idx ++) {
				v = pointer_vID[idx];
				
				if (prevs[v] == u) {
					queue.add(v);
				}
			}
		}
		
		if (borderNodes.add(o)) add_to_queue(o, dists[o]);
		
		Iterator<Integer> it = treeNodes.iterator();
	    while(it.hasNext()) {
	    	u = it.next();
	    	
	    	pointer_vID = Graph.ADJ_vID[u];
			neighborSize = pointer_vID.length;
			for (int idx = 0; idx < neighborSize; idx ++) {
				v = pointer_vID[idx];
				
				if (!treeNodes.contains(v)) {
					if (borderNodes.add(v)) add_to_queue(v, dists[v]);
				}
			}
	    }
	    
	    while(heapTop_update > 0) {
			// Remove the top one;
			int top_update = heap_update[1];
			
			heap_update[1] = heap_update[heapTop_update--];
			int cur_update = 1;
			int bst_update;
			int tmp_update;
			
			if (heapTop_update > 0) {
				while (true) {
					bst_update = cur_update;
					if (cur_update * 2 <= heapTop_update) {
						if (dists[heap_update[cur_update * 2]] < dists[heap_update[bst_update]])
							bst_update = cur_update * 2;
					}
						
					if (cur_update * 2 + 1 <= heapTop_update) {
						if (dists[heap_update[cur_update * 2 + 1]] < dists[heap_update[bst_update]])
							bst_update = cur_update * 2 + 1;
					}
					
					if (bst_update == cur_update)
						break;
					
					tmp_update = heap_update[cur_update];
					heap_update[cur_update] = heap_update[bst_update];
					heap_update[bst_update] = tmp_update;
					revHeap_update[heap_update[cur_update]] = cur_update; 
					revHeap_update[heap_update[bst_update]] = bst_update;
					cur_update = bst_update;
				}
			}
			
			int x = top_update;
			
			// for each neighbors of x, y
			for (int z : Graph.ADJ_vID[x]) {
				
				if (isSeed(z)) {
					continue;
				}
				
				if (PROB_TRIGGER(z,x)) {
					add_to_queue(z, dists[z]);
				}
			}
		}
	}
	
	// handle the case of edge weight decrease
	public void update_decrease(int modifiedEdgeID) throws IOException {
		
		// for each modified head v
		int v_1, v_2;
		v_1 = Graph.Edges[modifiedEdgeID].v_1;
		v_2 = Graph.Edges[modifiedEdgeID].v_2;
		
		if (Graph.compareVertex_byID(v_1, v_2) < 0) {
			firstAddToQueue(v_1);
			firstAddToQueue(v_2);
		} else {
			firstAddToQueue(v_2);
			firstAddToQueue(v_1);
		}
		
		while(heapTop_update > 0) {
			// Remove the top one;
			int top_update = heap_update[1];
			
			heap_update[1] = heap_update[heapTop_update--];
			int cur_update = 1;
			int bst_update;
			int tmp_update;
			
			if (heapTop_update > 0) {
				while (true) {
					bst_update = cur_update;
					if (cur_update * 2 <= heapTop_update) {
						if (dists[heap_update[cur_update * 2]] < dists[heap_update[bst_update]])
							bst_update = cur_update * 2;
					}
						
					if (cur_update * 2 + 1 <= heapTop_update) {
						if (dists[heap_update[cur_update * 2 + 1]] < dists[heap_update[bst_update]])
							bst_update = cur_update * 2 + 1;
					}
					
					if (bst_update == cur_update)
						break;
					
					tmp_update = heap_update[cur_update];
					heap_update[cur_update] = heap_update[bst_update];
					heap_update[bst_update] = tmp_update;
					revHeap_update[heap_update[cur_update]] = cur_update; 
					revHeap_update[heap_update[bst_update]] = bst_update;
					cur_update = bst_update;
				}
			}
			
			int x = top_update;
			
			// for each neighbors of x, y
			for (int z : Graph.ADJ_vID[x]) {
				
				if (isSeed(z)) {
					continue;
				}
				
				if (PROB_TRIGGER(z,x)) {
					add_to_queue(z, dists[z]);
				}
			}
		}
	}
	
	public void update_decrease(int[] modifiedEdges) throws IOException {
		for (int modifiedEdge_ID : modifiedEdges) {
			int change = Graph.Edges[modifiedEdge_ID].update_active();
			
			if (change > 0) {
				update_increase(modifiedEdge_ID);
			} else if (change < 0) {
				update_decrease(modifiedEdge_ID);
			}
		}
	}
	
	public void update(int[] modifiedEdges) throws IOException {
		for (int modifiedEdge_ID : modifiedEdges) {
			Graph.Edges[modifiedEdge_ID].update_active();
			
			update(modifiedEdge_ID);
		}
	}
	
	private void add_to_queue(int v, double distance) {
		
		dists[v] = distance;
		
		if ((heapTop_update + 1) >= heap_update.length) {
			
			int arraySize = heap_update.length;
			
			heap_update = Arrays.copyOf(heap_update, (arraySize * 2));
			revHeap_update = Arrays.copyOf(revHeap_update, (arraySize * 2));
			
//			System.out.println(heapTop_update + " " + v + " expand array size " + heap_update.length + " edge ID " + meid);
		}
			
		heap_update[++heapTop_update] = v;
		
		revHeap_update[v] = heapTop_update;
		int cur_update = heapTop_update;
		int tmp_update;
		
		while ((cur_update > 1) && (dists[heap_update[cur_update]] < dists[heap_update[cur_update / 2]])) {
			tmp_update = heap_update[cur_update];
			heap_update[cur_update] = heap_update[cur_update / 2];
			heap_update[cur_update / 2] = tmp_update;
			revHeap_update[heap_update[cur_update]] = cur_update; 
			revHeap_update[heap_update[cur_update / 2]] = cur_update / 2;
			cur_update /= 2;
		}
	}
	
	private void firstAddToQueue(int v) {
		if (isSeed(v)) {
			return;
		}
		
		if (!PROB(v)) return;
		
		add_to_queue(v, dists[v]);
	}
	
	private double[] get_min_parent_rhs(int v) {
		// [0]: parent [1]: distance [2]: root [3]: edgeID
		double[] result = new double[4];
		
		int current_parent = prevs[v];
		
		int min_parent = current_parent;
		int min_edgeID = Constant.UNDERFINE_VALUE;
		double min_distance = Constant.large;
		
		if (min_parent != Constant.UNDERFINE_VALUE) {
			min_edgeID = getEdgeID(v, min_parent);
			min_distance = dists[min_parent] + Graph.Edges[min_edgeID].weight;
		}
		
		int u, edgeID;
		double tempDist, edgeWeight;
		
		int [] pointer_vID = Graph.ADJ_vID[v];
		int [] pointer_eID = Graph.ADJ_eID[v];
		int neighborSize = pointer_vID.length;
		
		for (int idx = 0; idx < neighborSize; idx ++) {
			u = pointer_vID[idx];
			
			edgeID = pointer_eID[idx];
			edgeWeight = Graph.Edges[edgeID].weight;
			
			// v is disconnected from others
			if (edgeWeight >= Constant.large) {
				continue;
			}
			
			tempDist = dists[u] + edgeWeight;
			
			if (tempDist < min_distance) {
				min_parent = u;
				min_distance = tempDist;
				min_edgeID = edgeID;
				
			}
		}
		
		if (min_parent != Constant.UNDERFINE_VALUE) {
			result[0] = min_parent;
			result[1] = min_distance;
			result[2] = roots[min_parent];
			result[3] = min_edgeID;
		} else {
			result[0] = min_parent;
			result[1] = min_distance;
			result[2] = min_parent;
			result[3] = min_edgeID;
		}
		
		return result;
	}
	
	private int getEdgeID(int v, int min_parent) {
		int min_edgeID = edges[v];
		
		if (min_edgeID == Constant.UNDERFINE_VALUE) min_edgeID = Graph.getEdgeID_BS(v, min_parent);
		
		int v_1 = Graph.Edges[min_edgeID].v_1;
		int v_2 = Graph.Edges[min_edgeID].v_2;
		
		if ((v_1 == v && v_2 == min_parent) || 
				(v_1 == min_parent && v_2 == v)) {
				
			} else {
				min_edgeID = Graph.getEdgeID_BS(min_parent, v);
			}
		
		return min_edgeID;
	}
	
	private boolean PROB(int v) {
		
		double[] pairs = get_min_parent_rhs(v);
		int min_parent = (int) pairs[0];
		double min_distance = pairs[1];
		int min_root = (int) pairs[2];
		int min_edge = (int) pairs[3];
		
		if (dists[v] != min_distance || roots[v] != min_root) {
			prevs[v] = min_parent;
			dists[v] = min_distance;
			roots[v] = min_root;
			edges[v] = min_edge;
			
			return true;
		} else {
			if (prevs[v] != min_parent) {
				prevs[v] = min_parent;
				dists[v] = min_distance;
				roots[v] = min_root;
				edges[v] = min_edge;
				
				return false;
			}
		}
		
		return false;
	}
	
	private boolean PROB_TRIGGER(int v, int u) throws IOException {
		int min_parent = u;
		int min_edge = getEdgeID(u, v);
		double min_distance = dists[u] + Graph.Edges[min_edge].weight;
		int min_root = roots[u];
		
		if (dists[v] > min_distance) {
			
			prevs[v] = min_parent;
			dists[v] = min_distance;
			roots[v] = min_root;
			edges[v] = min_edge;
			
			return true;
		}
		
		return false;
	}
	
	public int getRoot(int v) {
		return roots[v];
	}
	
	public boolean sameRoot(int u, int v) {
		return roots[u] == roots[v];
	}
	
	public double p2Set(int mID) {
		return dists[mID];
	}
	
	// check whether seeds set S contains seed v, using binary search
	public boolean isSeed(int v) {
		if (v != Constant.UNDERFINE_VALUE && this.roots[v] == v) return true;
		return false;
	}
	
	public boolean print(int root, int level) {
		boolean hasNext = true;
		
		String pre = "";
		
		for (int i = 0; i < level; i ++) {
			pre = pre + "\t";
		}
		
		System.out.println(pre + "(" + String.format("%f", dists[root]) + ") " + root + "-");
		
		ArrayList<Integer> children = new ArrayList<Integer>();
		
		for (int i = 0; i < prevs.length; i ++) {
			if (prevs[i] == root) children.add(i);
		}
		
		if (children.size() <= 0) return false;
		
		for (int child : children) {
			print(child, level + 1);
		}
		
		return hasNext;
	}
	
	public void print() {
		for (int root = 0; root < this.vertexSize; root ++) {
			
			if (!isSeed(root)) continue;
			
			System.out.println("root as " + root);
			print(root, 0);
			System.out.println();
		}
	}
	
	public String seedToString() {
		String str = "";
		
		for (int root = 0; root < this.vertexSize; root ++) {
			
			if (!isSeed(root)) continue;
			
			str += root + ",";
		}
		
		return str;
	}
	
	public String distanceToString() {
		return Arrays.toString(dists).replace("[","").replace("]", "").replaceAll("\\s+", "");
	}
	
	public void freeMemory() {
		this.edges = null;
		this.heap_update = null;
		this.revHeap_update = null;
	}
}
