package util;

public class Label {
	int[] to,from;
	
	public Label(int size){
		to = new int[size];
		from = new int[size];
	}
	
	public void set(int from, int to){
		this.to[from] = to;
		this.from[to] = from;
	}
	
	public int oldName(int newName){
		return from[newName];
	}
	
	public int newName(int oldName){
		return to[oldName];
	}
	
}
