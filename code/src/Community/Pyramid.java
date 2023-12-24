package Community;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;

import Graph.Graph;
import ShortestPath.SetSPT;
import Utilities.Functions;

public class Pyramid {
	private SetSPT[] SPTs;
	private int maxLevel;
	private int verticeSize;
	
	public Pyramid(int verticeSize) {
		this.maxLevel = (int)(Math.log(verticeSize) / Math.log(2));
		SPTs = new SetSPT[maxLevel+1];
		this.verticeSize = verticeSize;
	}
	
	public void construct() {
		List<Integer>[] seeds_allLevel = new ArrayList[maxLevel + 1];
		seeds_allLevel = Functions.sampling(maxLevel + 1);
		
		for (int level = 0; level <= maxLevel; level ++) {
			SPTs[level] = new SetSPT(seeds_allLevel[level], verticeSize);
			SPTs[level].contructSPTs_RH();
		}
	}
	
	public void setup_parallel() {
		List<Integer>[] seeds_allLevel = new ArrayList[maxLevel + 1];
		seeds_allLevel = Functions.sampling(maxLevel + 1);
		
		for (int level = 0; level <= maxLevel; level ++) {
			SPTs[level] = new SetSPT(seeds_allLevel[level], verticeSize);
		}
	}
	
	public void construct_spt_parallel(int level) {
		SPTs[level].contructSPTs_RH();
	}
	
	public void construct(String levels) {
		String[] strs = levels.split(",");
		
		List<Integer>[] seeds_allLevel = new ArrayList[maxLevel + 1];
		for (int level = 0; level <= maxLevel; level ++) seeds_allLevel[level] = new ArrayList<Integer>((int) Math.pow(2, level));
		
		for (int v = 0; v < strs.length; v ++) {
			int minLevel = Integer.parseInt(strs[v]);
			
			if (minLevel != -1) {
				for (int level = minLevel; level <= maxLevel; level ++) seeds_allLevel[level].add(v);
			}
		}
		
		// construct spts
		for (int level = 0; level <= maxLevel; level ++) {
			SPTs[level] = new SetSPT(seeds_allLevel[level], verticeSize);
			SPTs[level].contructSPTs_RH();
		}
	}
	
	public void setup_parallel(String levels) {
		String[] strs = levels.split(",");
		
		List<Integer>[] seeds_allLevel = new ArrayList[maxLevel + 1];
		for (int level = 0; level <= maxLevel; level ++) seeds_allLevel[level] = new ArrayList<Integer>((int) Math.pow(2, level));
		
		for (int v = 0; v < strs.length; v ++) {
			int minLevel = Integer.parseInt(strs[v]);
			
			if (minLevel != -1) {
				for (int level = minLevel; level <= maxLevel; level ++) seeds_allLevel[level].add(v);
			}
		}
		
		// construct spts
		for (int level = 0; level <= maxLevel; level ++) {
			SPTs[level] = new SetSPT(seeds_allLevel[level], verticeSize);
		}
	}
	
	public void reNew() {
		for (int i = 0; i < SPTs.length; i ++) {
			SPTs[i].reNew();
		}
	}
	
	public void reNew(int level) {
		SPTs[level].reNew();
	}
	
	public int firstLevel(int u, int v) {
		for (int level = maxLevel; level >= 0; level --) {
			if (SPTs[level].sameRoot(u, v)) {
				return level;
			}
		}
		
		return -1;
	}
	
	// check whether vertex pair u v meet at a level , if not return false, if yes return true
	public boolean isMeet(int u, int v, int level) {
		if (SPTs[level].sameRoot(u, v)) return true;
		
		return false;
	}
	
	public String seedToLevel() {
		int[] seedToLevel = new int[this.verticeSize]; 	// the entry is the minimum level of a node exists in pyramid
		
		// for the highest level
		for (int i = 0; i < this.verticeSize; i ++) {
			boolean isSeed = false;
			
			for (int level = 0; level < SPTs.length; level ++) {
				if (SPTs[level].isSeed(i)) {
					seedToLevel[i] = level;
					isSeed = true;
					break;
				}
			}
			
			if (!isSeed) seedToLevel[i] = -1;
		}
		
		List<Integer> temp = new ArrayList<Integer>();
		for (int level : seedToLevel) {
			temp.add(level);
		}
		
		return temp.stream().map(Object::toString).collect(Collectors.joining(","));
	}
	
	public String seedToString() {
		String str = "";
		
		for (int i = 0; i < SPTs.length; i ++) {
			str += "Level " + i + "\n";
			str += SPTs[i].seedToString() + "\n";
		}
		
		return str;
	}
	
	public String distanceToString(){
		String str = "";
		
		for (int i = 0; i < SPTs.length; i ++) {
			str += "Level " + i + "\n";
			str += SPTs[i].distanceToString() + "\n";
		}
		
		return str;
	}
	
	public void update_decrease(int modifiedEdgeID) throws IOException {
		for (int i = 0; i < SPTs.length; i ++) {
			SPTs[i].update_decrease(modifiedEdgeID);
		}
	}
	
	public void update_increase(int modifiedEdgeID) throws IOException {
		for (int i = 0; i < SPTs.length; i ++) {
			SPTs[i].update_increase(modifiedEdgeID);
		}
	}
	
	public void update(int modifiedEdgeID) throws IOException {
		for (int i = 0; i < SPTs.length; i ++) {
			SPTs[i].update(modifiedEdgeID);
		}
	}
		
	public int getSPTNum() {
		return this.SPTs.length;
	}
	
	public void freeMemory() {
		for (int i = 0; i < SPTs.length; i ++) {
			SPTs[i].freeMemory();
		}
	}
	
	public static void main(String arg[]) throws IOException, InterruptedException {
		Graph.loadGraph();
		Pyramid pyramid = new Pyramid(Graph.getVertexSize());
		pyramid.construct();
	}
}
