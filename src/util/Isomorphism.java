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
		List<Set<Integer>> partition = initialParition(g);
		print(partition);

		searchTree(g, partition);
		return null;
	}

	public static void searchTree(Graph g, List<Set<Integer>> partition) {
		if (discrete(partition)) {
			return;
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
		for (int i : Wk) {
			//DO THE splitting 
			List<Set<Integer>> newPartition = new ArrayList<Set<Integer>>();
			
			for(int j=0; j < min; j++ ){
				newPartition.add(partition.get(j));
			}
			
			Set<Integer> u = new TreeSet<Integer>();
			u.add(i);
			newPartition.add(u);
			Set<Integer> Wk1 = new TreeSet<Integer>(Wk);
			Wk1.remove(i);
			newPartition.add(Wk1);
			
			for(int j=min+1; j < partition.size(); j++ ){
				newPartition.add(partition.get(j));
			}
			
			print (newPartition);
			searchTree(g, newPartition);
		}

	}

	public static List<Set<Integer>> initialParition(Graph g) {
		Set<Integer> cell = new TreeSet<Integer>();
		for (int i : g.vertices()) {
			cell.add(i);
		}
		List<Set<Integer>> partition = new ArrayList<Set<Integer>>();
		List<Set<Integer>> alpha = new ArrayList<Set<Integer>>();
		partition.add(cell);
		alpha.add(cell);

		// McKay's Algorithm
		int m = 1, M = 1;

		while (!(m > M || discrete(partition))) {
			Set<Integer> W = alpha.get(m);
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

				M = M + s - 1;// M is just the size of alpha

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

	public static void print(List<Set<Integer>> p) {
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
				System.out.println(" | ");
			}
		}
		System.out.print("]");
	}

}
