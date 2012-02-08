package util;
import graph.Graph;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Treats all graphs as simple (this is clearly not a problem, as it finds a
 * labelling)
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
		List<Set<Integer>> partition = new ArrayList<Set<Integer>>();
		partition.add(cell);

		// McKay's Algorithm
		int m = 1, M = 1;

		while (!(m > M || discrete(partition))) {
			Set<Integer> W = partition.get(m);
			m = m + 1;
			int k = 1;
			int r = partition.size();

			while (k <= r) {

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
								X.add(t);
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
					if(temps > size){
						max = i;
						size = temps;
					}
				}
			}

		}
		return null;
	}

	public static boolean discrete(List<Set<Integer>> partition) {
		for (Set<Integer> s : partition) {
			if (s.size() != 1) {
				return false;
			}
		}
		return true;

	}

	public static int d(int v, Set<Integer> W, Graph g) {
		int d = 0;
		for (int i : W) {
			d += g.numUnderlyingEdges(v, i);
		}

		return d;
	}

	public static <E> void insert(int p, E x, List<E> list) {
		for (int i = list.size() - 1; i >= p; i--) {
			list.set(i + 1, list.get(i));
		}
		list.set(p, x);
	}

}
