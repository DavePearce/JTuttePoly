package util;

import graph.Graph;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Treats all graphs as simple (this is clearly not a problem, as it finds a labelling)
 * 
 * @author roma
 * 
 */
public class Isomorphism {

	public static Label canonicalLabel(Graph g) {

		// Create an initial partition
		Set<Integer> cell = new TreeSet<Integer>();
		for (int i : g.vertices()) {
			cell.add(i);
		}
		List<Set<Integer>> unitpart = new ArrayList<Set<Integer>>();
		List<Set<Integer>> alpha = new ArrayList<Set<Integer>>();
		unitpart.add(cell);
		alpha.add(cell);
		
		List<Set<Integer>> partition = equitableRefinement(g,unitpart,alpha);
//		print(partition, null);

		Triple<List<Set<Integer>>, int[], Label> resultTriple = searchTree(g, partition, null);
		
//		System.out.println();
//		print(resultTriple.first, resultTriple.second);
		return resultTriple.third;
	}

	private static Triple<List<Set<Integer>>, int[], Label> searchTree(Graph g, List<Set<Integer>> partition,
			Triple<List<Set<Integer>>, int[], Label> soFar) {
		if (discrete(partition)) {
			if (soFar == null) {
				Label label = partitionToLabel(partition);
				return new Triple<List<Set<Integer>>, int[], Label>(partition, partitionToValue(partition, g, label), label);
			} else {
				Label label = partitionToLabel(partition);
				int[] value = partitionToValue(partition, g, label);
				if (greaterThan(value, soFar.second)) {
					return new Triple<List<Set<Integer>>, int[], Label>(partition, value, label);
				}
				return soFar;
			}
		}
		// Find the first smallest non trivial cell of partition since there must be one
		int min = -1;
		int size = Integer.MAX_VALUE;
		for (int i = 0; i < partition.size(); i++) {
			int s = partition.get(i).size();
			if (s > 1 && s < size) {
				min = i;
				size = s;
			}
		}
		Set<Integer> Wk = partition.get(min);

		Triple<List<Set<Integer>>, int[], Label> current = soFar;
		for (int i : Wk) {
			// DO THE splitting
			List<Set<Integer>> newPartition = new ArrayList<Set<Integer>>();

			for (int j = 0; j < min; j++) {
				newPartition.add(partition.get(j));
			}

			Set<Integer> u = new TreeSet<Integer>();
			u.add(i);
			newPartition.add(u);
			Set<Integer> Wk1 = new TreeSet<Integer>(Wk);
			Wk1.remove(i);
			newPartition.add(Wk1);

			for (int j = min + 1; j < partition.size(); j++) {
				newPartition.add(partition.get(j));
			}

			//Now you need to do the equitable refinement again
			List<Set<Integer>> alpha = new ArrayList<Set<Integer>>();
			alpha.add(u);
			newPartition = equitableRefinement(g, newPartition,alpha);
			
			
//			if (discrete(newPartition)) {
//				Label label = partitionToLabel(newPartition);
//				int[] value = partitionToValue(newPartition, g, label);
//				print(newPartition, value);
//			} else {
//				print(newPartition, null);
//			}
			
			current = searchTree(g, newPartition, current);

		}
		return current;

	}

	private static boolean greaterThan(int[] value, int[] second) {
		for (int i = 0; i < value.length; i++) {
			if (greaterthan(0xFFFFFFFFL & value[i], 0xFFFFFFFFL & second[i])) {
				return true;
			} else if (lessthan(0xFFFFFFFFL & value[i],0xFFFFFFFFL & second[i])) {
				return false;
			}
		}
		return false;
	}

	private static boolean greaterthan(long i, long j){
		return i > j;
	}
	
	private static boolean lessthan(long i, long j){
		return i<j;
	}
	
	private  static List<Set<Integer>> equitableRefinement(Graph g, List<Set<Integer>> partition, List<Set<Integer>> alpha) {

		// McKay's Algorithm
		int m = 0, M = 0;

		while (!(m > M || discrete(partition))) {
			Set<Integer> W = alpha.get(m);
			m = m + 1;
			int k = 0;
			int r = partition.size();

			while (k < r) {

				// Define a new set of partitions
				Set<Integer> Vk = partition.get(k);
				List<Set<Integer>> X = new ArrayList<Set<Integer>>();
				place: for (int x : Vk) {
					if (X.isEmpty()) {
						Set<Integer> t = new TreeSet<Integer>();
						t.add(x);
						X.add(t);
					} else {
						int deg = d(x, W, g);
						for (int c = 0; c < X.size(); c++) {
							Set<Integer> s = X.get(c);
							int deg2 = d(s.iterator().next(), W, g);
							if (deg2 == deg) {
								s.add(x);
								continue place;
							} else if (deg2 > deg) {
								Set<Integer> t = new TreeSet<Integer>();
								t.add(x);
								insert(c, t, X);
								continue place;
							}
						}
						Set<Integer> t = new TreeSet<Integer>();
						t.add(x);
						X.add(t);
					}
				}

				int s = X.size();
				if (s == 1) {
					k = k + 1;
					continue;
				}

				// find the smallest index
				int max = 0;
				int size = X.get(0).size();
				for (int i = 1; i < X.size(); i++) {
					int temps = X.get(i).size();
					if (temps > size) {
						max = i;
						size = temps;
					}
				}

				// Replace in place one
				for (int i = m; i <= M; i++) {
					if (alpha.get(i) == partition.get(k)) {
						alpha.set(i, X.get(max));
						break;
					}
				}

				// Add all others to list
				for (int i = 0; i < X.size(); i++) {
					if (i != max) {
						alpha.add(X.get(i));
					}
				}

				M = M + s - 1;// M is just the largest index of alpha

				// Insert relevant entries into partition instead of Vk
				partition.set(k, X.get(X.size() - 1));
				for (int i = X.size() - 2; i >= 0; i--) {
					insert(k, X.get(i), partition);
				}

				k = k + 1;
			}

		}
		return partition;
	}

	private static boolean discrete(List<Set<Integer>> partition) {
		for (Set<Integer> s : partition) {
			if (s.size() != 1) {
				return false;
			}
		}
		return true;

	}

	private static int d(int v, Set<Integer> W, Graph g) {
		int d = 0;
		for (int i : W) {
			d += g.numUnderlyingEdges(v, i);
		}

		return d;
	}

	private static <E> void insert(int p, E x, List<E> list) {
		if (p == list.size()) {
			list.add(x);
			return;
		}
		for (int i = list.size() - 1; i >= p; i--) {
			if (i == list.size() - 1) {
				list.add(list.get(i));
			} else {
				list.set(i + 1, list.get(i));
			}
		}
		list.set(p, x);
	}

	private static void print(List<Set<Integer>> p, int[] value) {
		System.out.print("[");
		for (int i = 0; i < p.size(); i++) {
			Iterator<Integer> iter = p.get(i).iterator();
			while (iter.hasNext()) {
				int j = iter.next();
				System.out.print(j);
				if (iter.hasNext()) {
					System.out.print(" ");
				}
			}
			if (i < p.size() - 1) {
				System.out.print(" | ");
			}
		}
		System.out.print("] ");

		if (value != null) {
			for(int i: value){
				System.out.printf("%08X", i);
			}
		}
		System.out.println();

	}

	private static Label partitionToLabel(List<Set<Integer>> partition) {
		Label l = new Label(partition.size());
		for (int i = 0; i < partition.size(); i++) {
			l.set(partition.get(i).iterator().next(), i);
		}
		return l;
	}

	private static int[] partitionToValue(List<Set<Integer>> partition, Graph g, Label l) {
		int numVertices = partition.size();
		int size = numVertices * numVertices;
		int numInts = (int) Math.ceil(size / 32.0);
		int[] value = new int[numInts];

		for (int i = 0; i < partition.size(); i++) {
			for (int j = 0; j < partition.size(); j++) {
				setBit(value, i, j, partition.size(), g.numUnderlyingEdges(l.oldName(i), l.oldName(j)));
			}
		}

		return value;
	}

	private static void setBit(int[] arr, int w, int i, int j, int v) {
		if(v == 0) return;
		int place = i + j * w;
		int idx = place / 32;
		int bit = place % 32;

		int mask = (1 << bit);
		arr[idx] |= mask;
	}

	public static void main(String args[]) {
		Graph g = new Graph(9);
		g.addEdge(0, 1);
		g.addEdge(2, 1);
		g.addEdge(0, 3);
		g.addEdge(4, 1);
		g.addEdge(2, 5);
		g.addEdge(3, 4);
		g.addEdge(5, 4);
		g.addEdge(3, 6);
		g.addEdge(4, 7);
		g.addEdge(8, 5);
		g.addEdge(6, 7);
		g.addEdge(7, 8);

		long start = System.currentTimeMillis();
		Label l = canonicalLabel(g);
		long end = System.currentTimeMillis();
		System.out.printf("%.2fs\n",(end - start)/ 1000.0);
		
//		System.out.println();
//		for (int i = 0; i < g.numVertices(); i++) {
//			System.out.println(i + " -> " + l.newName(i));
//		}
	}
}
