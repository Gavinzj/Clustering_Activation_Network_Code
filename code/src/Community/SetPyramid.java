package Community;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;

import Graph.Graph;
import Utilities.FilePath_Mon;

public class SetPyramid {
	private Pyramid[] PyramidSet;
	private int verticeSize;
	private int PyramidNum;
	private double voteThreshold;

	private Setup setup;
	private Construct_Level construct_level;
	private ReNew reNew;
	private CountDownLatch latch;

	public SetPyramid(int PyramidNum, int verticeSize, double voteThreshold){
		PyramidSet = new Pyramid[PyramidNum];
		this.verticeSize = verticeSize;
		this.PyramidNum = PyramidNum;
		this.voteThreshold = voteThreshold;
	}

	private class Setup extends Thread {
		private boolean readSeed;
		private int pyramidID;
		private String levels;

		Setup(boolean readSeed, int pyramidID, String levels) throws InterruptedException {
			this.readSeed = readSeed;
			this.pyramidID = pyramidID;
			this.levels = levels;
			start();
		}

		public void run() {
			if (!readSeed) {
				PyramidSet[pyramidID] = new Pyramid(verticeSize);
				PyramidSet[pyramidID].setup_parallel();
			} else {
				PyramidSet[pyramidID] = new Pyramid(verticeSize);
				PyramidSet[pyramidID].setup_parallel(levels);
			}
			
			latch.countDown();
		}
	}

	private class Construct_Level extends Thread {
		private int pyramidID;
		private int SetSPTID;

		Construct_Level(int pyramidID, int SetSPTID) throws InterruptedException {
			this.pyramidID = pyramidID;
			this.SetSPTID = SetSPTID;
			start();
		}

		public void run() {
			PyramidSet[pyramidID].construct_spt_parallel(SetSPTID);
			
			latch.countDown();
		}
	}
	
	public void construct_parallel(boolean readSeed, boolean saveSeed, int trial) throws IOException, InterruptedException {
		int PyramidNum = PyramidSet.length;
		
		if (!readSeed) {
			latch = new CountDownLatch(PyramidSet.length);
			
			for (int i = 0; i < PyramidNum; i++) {
				setup = new Setup(readSeed, i, "");
			}
			
			latch.await(); // Wait for countdown
			
			int SetSPTNum = PyramidSet[0].getSPTNum();
			latch = new CountDownLatch(PyramidSet.length * SetSPTNum);
			
			for (int i = 0; i < PyramidNum; i++) {
				for (int j = 0; j < SetSPTNum; j ++) {
					construct_level = new Construct_Level(i, j);
				}
			}

			latch.await(); // Wait for countdown
			
			if (saveSeed)
				saveSeed(trial);

		} else {
			String fileInput = FilePath_Mon.filePathPre + "/setPyramid/PyramidNum_" + PyramidNum + "_trial_" + trial + ".txt";
			Path path = Paths.get(fileInput);
			Scanner scanner = new Scanner(path.toAbsolutePath());
			
			latch = new CountDownLatch(PyramidNum);
			
			for (int i = 0; i < PyramidNum; i++) {
				setup = new Setup(readSeed, i, scanner.nextLine());
			}
			
			latch.await(); // Wait for countdown

			scanner.close();
			
			int SetSPTNum = PyramidSet[0].getSPTNum();
			latch = new CountDownLatch(PyramidSet.length * SetSPTNum);
			
			for (int i = 0; i < PyramidNum; i++) {
				for (int j = 0; j < SetSPTNum; j ++) {
					construct_level = new Construct_Level(i, j);
				}
			}

			latch.await(); // Wait for countdown
			
			System.out.println("setPyramid read");
		}
	}

	public void construct(boolean readSeed, boolean saveSeed, int trial) throws IOException {
		if (!readSeed) {
			for (int i = 0; i < PyramidSet.length; i++) {
				PyramidSet[i] = new Pyramid(verticeSize);
				PyramidSet[i].construct();
			}

			if (saveSeed) saveSeed(trial);

		} else {
			readSeed(trial);
		}
	}

	private class ReNew extends Thread {
		private int pyramidID;
		private int SetSPTID;

		ReNew(int pyramidID, int SetSPTID) throws InterruptedException {
			this.pyramidID = pyramidID;
			this.SetSPTID = SetSPTID;
			start();
		}

		public void run() {
			PyramidSet[pyramidID].reNew(SetSPTID);
			
			latch.countDown();
		}
	}
	
	public void reNew_parallel() throws InterruptedException {
		int SetSPTNum = PyramidSet[0].getSPTNum();
		latch = new CountDownLatch(PyramidSet.length * SetSPTNum);
		
		for (int i = 0; i < PyramidNum; i++) {
			for (int j = 0; j < SetSPTNum; j ++) {
				reNew = new ReNew(i, j);
			}
		}

		latch.await(); // Wait for countdown
	}

	public void reNew() {
		for (int i = 0; i < PyramidNum; i++) {
			PyramidSet[i].reNew();
		}
	}
	
	public boolean sameLabel(int u, int v, int level) {
		double agree = 0;
		double cutOff = voteThreshold * (double) PyramidSet.length;
		
		for (int pyramidID = 0; pyramidID < PyramidSet.length; pyramidID ++) {
			if (pyramidID < 0 || pyramidID >= PyramidSet.length)System.out.println("pyramidID " + pyramidID);
			if (PyramidSet[pyramidID].isMeet(u, v, level)) agree++;
			
			if (agree >= cutOff) return true;
		}

		if (agree >= cutOff) return true;
		
		return false;
	}

	public void update_decrease(int modifiedEdgeID) throws IOException {
		
		int change = Graph.Edges[modifiedEdgeID].update_active();
		
		if (change < 0) {
			for (int i = 0; i < PyramidSet.length; i++) {
				PyramidSet[i].update_decrease(modifiedEdgeID);
			}
		} else if (change > 0){
			for (int i = 0; i < PyramidSet.length; i++) {
				PyramidSet[i].update_increase(modifiedEdgeID);
			}
		}
	}

	public void update(int modifiedEdgeID) throws IOException {
		
		Graph.Edges[modifiedEdgeID].update_active();
		
		for (int i = 0; i < PyramidSet.length; i++) {
			PyramidSet[i].update(modifiedEdgeID);
		}
	}
	
	public String query(int queryNode, int level) {
		boolean[] checked = new boolean[Graph.getVertexSize()];
		Queue<Integer> queue = new LinkedList<Integer>();
		
		queue.add(queryNode);
		
		int clusterSize = 0;
		
		double startTime = System.currentTimeMillis();
		
		int u;
		while (!queue.isEmpty()) {
			u = queue.remove();
			
			if (!checked[u]) {
				checked[u] = true;
				clusterSize++;
			}
			
			// for each of its neighbor
			for (int v : Graph.ADJ_vID[u]) {
				if (!checked[v]) {
					if (sameLabel(u, v, level)) {
						checked[v] = true;
						queue.add(v);
						
						clusterSize++;
					}
				}
			}
		}
		
		double endTime = System.currentTimeMillis();
		
		return clusterSize + "," + (endTime - startTime);
	}

	private void saveSeed(int trial) {
		try {
			FileWriter fwCount = new FileWriter(FilePath_Mon.filePathPre + "/setPyramid/PyramidNum_" + PyramidNum + "_trial_" + trial + ".txt");
			
			// save
			for (int i = 0; i < PyramidSet.length; i++) {
				fwCount.write(PyramidSet[i].seedToLevel() + "\n");
			}

			fwCount.flush();
			fwCount.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
	}

	private void readSeed(int trial) throws IOException {
		String fileInput = FilePath_Mon.filePathPre + "/setPyramid/PyramidNum_" + PyramidNum + "_trial_" + trial + ".txt";
		Path path = Paths.get(fileInput);
		Scanner scanner = new Scanner(path.toAbsolutePath());

		for (int i = 0; i < PyramidSet.length; i++) {
			PyramidSet[i] = new Pyramid(verticeSize);
		}
		
		int pyramidID = 0;
		while (scanner.hasNextLine()) {
			// process each line
			PyramidSet[pyramidID++].construct(scanner.nextLine());
		}

		scanner.close();
	}

	public String seedToString() {
		String result = "";
		
		for (int i = 0; i < PyramidSet.length; i++) {
			result = result + PyramidSet[i].seedToString() + "\n";
		}
		
		return result;
	}

	public String distanceToString() {
		String result = "";
		
		for (int i = 0; i < PyramidSet.length; i++) {
			result = result + PyramidSet[i].distanceToString() + "\n";
		}
		
		return result;
	}
	
	public static void samplingPyramidSet(int trials, int pyramidNum) throws IOException, InterruptedException {
		System.out.println("samplingPyramidSet trials " + trials + " pyramidNum " + pyramidNum);
		
		for (int i = 0; i < trials; i ++) {
			SetPyramid setPyramid = new SetPyramid(pyramidNum, Graph.getVertexSize(), 0.7);
			setPyramid.construct_parallel(false, true, i);
		}
	}

	public void freeMemory() {
		for (int i = 0; i < PyramidSet.length; i ++) {
			PyramidSet[i].freeMemory();
		}
		
		this.setup = null;
		this.construct_level = null;
		this.reNew = null;
		this.latch = null;
		
	}
	
	public static void main(String arg[]) throws IOException, InterruptedException {
		Graph.loadGraph();
		
//		for (int i = 0; i < 5; i ++) {
//			SetPyramid setPyramid = new SetPyramid(1, Graph.getVertexSize(), 0.7);
//			setPyramid.construct(false, true, i);
//		}
//		
//		for (int i = 0; i < 5; i ++) {
//			SetPyramid setPyramid = new SetPyramid(2, Graph.getVertexSize(), 0.7);
//			setPyramid.construct(false, true, i);
//		}
		
//		for (int i = 0; i < 5; i ++) {
//			SetPyramid setPyramid = new SetPyramid(4, Graph.getVertexSize(), 0.7);
//			setPyramid.construct_parallel(false, true, i);
//		}
//		
//		for (int i = 0; i < 5; i ++) {
//			SetPyramid setPyramid = new SetPyramid(6, Graph.getVertexSize(), 0.7);
//			setPyramid.construct_parallel(false, true, i);
//		}
//		
//		for (int i = 0; i < 5; i ++) {
//			SetPyramid setPyramid = new SetPyramid(8, Graph.getVertexSize(), 0.7);
//			setPyramid.construct_parallel(false, true, i);
//		}
		
//		double start1 = System.currentTimeMillis();
//
//		setPyramid.construct(false, true, -1);
//
//		double end1 = System.currentTimeMillis();
//
//		double runningTime = (end1 - start1);
//
//		System.out.println(runningTime);
//		
//		try {
//			FileWriter fwCount = new FileWriter(FilePath_Mon.filePathPre + "/record.txt");
//			
//			// save
//			fwCount.write(setPyramid.distanceToString());
//
//			fwCount.flush();
//			fwCount.close();
//
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			return;
//		}
//		
//		start1 = System.currentTimeMillis();
//
//		setPyramid.construct_parallel(true, false, -1);
//
//		end1 = System.currentTimeMillis();
//
//		runningTime = (end1 - start1);
//
//		System.out.println(runningTime);
//		
//		try {
//			FileWriter fwCount = new FileWriter(FilePath_Mon.filePathPre + "/record1.txt");
//			
//			// save
//			fwCount.write(setPyramid.distanceToString());
//
//			fwCount.flush();
//			fwCount.close();
//
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			return;
//		}
	}
}
