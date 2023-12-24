package Utilities;

public class Constant {
	/** a very large value */
	public static double large = Math.pow(10, 8);
	
	/** a very small value */
	public static double small = Math.pow(10, -8);
	
	/** the largest value */
	public static double MAX = Double.MAX_VALUE;
	
	public static int UNDERFINE_VALUE = -999999;
	
	// control the LCA distance calculating strategy
	public static int LCA_DISTANCE_STRATEGY = 1;
	
	// for time decaying rate
	public static float LAMBDA = (float) 0.01;
	
	// percentage of edges of activation
	public static int ACTIVATION_RATIO = 1000;
	
	// unit of memory measure
	public static double MEMORY_UNIT = 1024*1024;
	
	// unit of running time measure
	public static double RUNNING_TIME_UNIT = 1000;
	
	// initial edge Weight
	public static double INITIAL_EDGE_WEIGHT = 1;
	
	// initial cluster number
	public static int NG = 100;
}
