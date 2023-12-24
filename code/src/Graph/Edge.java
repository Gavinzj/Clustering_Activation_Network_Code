package Graph;

import Utilities.Constant;

public class Edge{
	public int v_1;
	public int v_2;
	public double weight;
	public int lastActive = 0;
	
	public Edge(int v_1, int v_2, double weight) {
		this.v_1 = v_1;
		this.v_2 = v_2;
		this.weight = weight;
	}
	
	public Edge(int v_1, int v_2) {
		this.v_1 = v_1;
		this.v_2 = v_2;
	}
	
	// return true if the edge weight decrease
	public boolean update_unactive() {
		
		double w_old = weight;
		double w_new = (w_old / (Math.exp(-1 * Constant.LAMBDA * (Graph.currentTime - this.lastActive))));
		
		if (w_old >= Constant.large) w_new = Constant.large;
		else if (w_old <= 0) w_new = 0;
		
		this.weight = w_new;
		
		this.lastActive = Graph.currentTime;
		
		if (w_old > w_new) return true;
		else return false;
	}
	
	// return 1 if edge weight increase, -1 if decrease, 0 if unchange
	public int update_active() {
		
		double w_old = weight;
		double w_new = (w_old / (Math.exp(-1 * Constant.LAMBDA * (Graph.currentTime - this.lastActive)) + w_old));
		
		if (w_old >= Constant.large) w_new = Constant.large;
		else if (w_old <= 0) w_new = 0;
		
		this.weight = w_new;
		
		this.lastActive = Graph.currentTime;
		
		if (w_old > w_new) return -1;
		else if (w_old < w_new) return 1;
		else return 0;
	}
	
	public boolean isEqual(Edge another) {
		// check whether they are the same class object
		if (this.v_1 != another.v_1) return false;
		else {
			if (this.v_2 != another.v_2) return false;
			else return true;
		}
	}
	
	public String toString() {
		String string = this.v_1 + "\t" + this.v_2 + "\t" + weight;
		
		return string;
	}
}
