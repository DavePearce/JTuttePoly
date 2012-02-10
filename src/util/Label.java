package util;

public class Label {
	int[] to,from;
	
	public Label(int size, int domain){
		to = new int[domain];
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
	
	public String toString(){
		StringBuilder ss = new StringBuilder();
		for(int i=0;i<from.length;i++){
			ss.append(from[i]);
			ss.append(" -> ");
			ss.append(i);
			ss.append("\n");
		}
		
		return ss.toString();
	}
	
}
