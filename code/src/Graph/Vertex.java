package Graph;

public class Vertex implements Comparable<Vertex>{
	public int mID;
	public int degree;
	
	public Vertex(int mID) {
		this.mID = mID;
	}

	@Override
	public int compareTo(Vertex arg0) {
		if (this.degree > arg0.degree) {
			return -1;
		} else if (this.degree < arg0.degree) {
			return 1;
		} else {
			if (this.mID < arg0.mID) {
				return -1;
			} else if (this.mID > arg0.mID) {
				return 1;
			} else {
				return 0;
			}
		}
	}
	
	public String toString() {
		return this.mID + "";
	}
}
