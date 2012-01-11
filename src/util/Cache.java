package util;

import graph.Graph;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import polynomial.FactorPoly;

public class Cache {
	private Map<Graph, FactorPoly> data;
	private static final long MIN_MEM = 5 * 1024 * 1024;
	private double delete_prop;
	private long acesses = 0;
	private long misses = 0; 
	
	public Cache() {
		this.data = new HashMap<Graph, FactorPoly>();
	}

	public void add(Graph g, FactorPoly f) {
		long mem = Runtime.getRuntime().freeMemory();
		if (mem < MIN_MEM) {
			System.out.println(mem/(1024.0*1024.0));
			cleanCache();
		}
		Graph gclone = new Graph(g);
		FactorPoly fclone = new FactorPoly(f);
		data.put(gclone, fclone);
	}

	public FactorPoly get(Graph g) {
		acesses ++;
		FactorPoly f= data.get(g);
		if(f == null){ misses ++;}
		return data.get(g);
	}
	
	private void cleanCache(){
		System.out.println("Cleaning cache");
		Iterator<Map.Entry<Graph, FactorPoly>> i = data.entrySet().iterator();
		for(Map.Entry<Graph, FactorPoly> e = i.next(); i.hasNext();e=i.next() ){
			if(Math.random() < delete_prop){
				i.remove();
			}
		}
		
		System.gc();
	}
	
	public String statistics(){
		return String.format("%d Cache Access Attempts\n%.2f%% Cache Hits",acesses,((double)(acesses-misses)/(double)acesses));
	}
	
}
