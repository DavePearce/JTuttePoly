package graph;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import polynomial.Pair;

public class AdjacencyMatrix {
	private int numEdges;
	private int domainSize;
	private int numMultiEdges;
	private int[] edges;
	private static final int CELL_SIZE = 4;
	private int numVertices;
	private int[] vertices;
	private int startVertex;

	/**
	 * Make a new Adjacency matrix with a given number of vertices. All vertices MUST have edges added to them during construction
	 * 
	 * @param n
	 *            The number of vertices
	 */
	public AdjacencyMatrix(int n) {
		numEdges = 0;
		numMultiEdges = 0;
		domainSize = n;
		numVertices = n;
		edges = new int[(int) Math.ceil(n * n * 32.0 / CELL_SIZE)];

		vertices = new int[n];
		startVertex = 0;
		for (int i = 0; i < n - 1; i++) {
			vertices[n] = n + 1;
		}
		vertices[n - 1] = -1;

	}

	/**
	 * Deep clone an existing AdjacencyMatrix. The two are independent and identical
	 * 
	 * @param g
	 *            The AdjacencyMatrix to clone
	 */
	public AdjacencyMatrix(AdjacencyMatrix g) {
		numEdges = g.numEdges;
		numMultiEdges = g.numMultiEdges;
		domainSize = g.domainSize;
		edges = new int[g.edges.length];

		for (int i = 0; i < edges.length; i++) {
			edges[i] = g.edges[i];
		}

		vertices = new int[g.vertices.length];
		for (int i = 0; i < vertices.length; i++) {
			vertices[i] = g.vertices[i];
		}
	}

	/**
	 * The range of values that are vertices
	 * 
	 * @return The number of vertex there are
	 */
	public int domainSize() {
		return domainSize;
	}

	/**
	 * Vertex iterator
	 * 
	 * @return
	 */
	public Iterable<Integer> vertices() {
		return new VertexIterable();
	}

	/**
	 * Iterator of edges from v
	 * 
	 * @param v
	 *            The source of the edges
	 * @return Iterator overthe edges from v
	 */
	public Iterable<Pair<Integer, Integer>> edges(int v) {
		return new EdgeIterable(v);
	}

	public boolean equals(Object o) {
		if (o instanceof AdjacencyList) {
			return this.equals((AdjacencyList) o);
		}
		return false;
	}

	public boolean equals(AdjacencyMatrix a) {
		if (a.domainSize != this.domainSize) {
			return false;
		}
		if (a.numEdges != this.numEdges) {
			return false;
		}
		if (a.numMultiEdges != this.numMultiEdges) {
			return false;
		}
		if (a.edges.length != this.edges.length) {
			return false;
		}

		for (int i = 0; i < this.edges.length; i++) {
			if (this.edges[i] != a.edges[i]) {
				return false;
			}
		}
		return true;
	}

	public int numVertices() {
		return numVertices;
	}

	public int numEdges() {
		return numEdges;
	}

	public int numUnderlyingEdges() {
		return numEdges - numMultiEdges;
	}

	/**
	 * Return the degree of a vertex
	 * 
	 * @param vertex
	 * @return
	 */
	public int numEdges(int vertex) {
		int count = 0;
		for (Pair<Integer, Integer> i : edges(vertex)) {
			count += i.second();
		}
		return count;
	}

	/**
	 * Return the number of vertices that this vertex is connected to. This is the same as the degree except that multiedges are only counted as 1
	 * rather than their actual number
	 * 
	 * @param vertex
	 * @return
	 */
	public int numUnderlyingEdges(int vertex) {
		int count = 0;
		for (Pair<Integer, Integer> i : edges(vertex)) {
			count++;
		}
		return count;
	}

	public int numEdges(int from, int to) {
		long startBit = domainSize * CELL_SIZE * from + CELL_SIZE * to;
		int startInt = (int) (startBit / 32);
		int index = (int) (startBit % 32);
		boolean overflow = (index + CELL_SIZE) > 32;
		if (overflow) {
			long v = edges[startInt] | edges[startInt + 1] << 32;
			int mask = 0;
			int m = 1;
			for (int i = 0; i < CELL_SIZE; i++) {
				mask |= m;
				m <<= 1;
			}
			long ret = v & (mask << index);
			ret >>= index;
			ret &= m;
			return (int) ret;
		} else {
			int v = edges[startInt];
			int mask = 0;
			int m = 1;
			for (int i = 0; i < CELL_SIZE; i++) {
				mask |= m;
				m <<= 1;
			}
			int ret = v & (mask << index);
			ret >>= index;
			ret &= m;
			return ret;
		}
	}

	public int numMultiedges() {
		return numMultiEdges;
	}

	public boolean isMultiGraph() {
		return numMultiEdges > 0;
	}

	/**
	 * Remove a vertex. This vertex is now guaranteed to have no edges to or from it
	 * 
	 * @param v
	 *            The vertex to clear
	 */
	public void clear(int v) {
		// Remove v from the list of vertices
		if (vertices[v] == -1) {
			throw new RuntimeException("Vertex has already been removed");
		}

		numVertices--;
		if (startVertex == v) {
			startVertex = vertices[v];
		} else {
			int i = vertices[startVertex];
			while (vertices[i] != v) {
				i = vertices[i];
			}
			vertices[i] = vertices[v];
		}
		vertices[v] = -1;

		// Now, clear all edges involving v
		// first the row
		long startBit = domainSize * CELL_SIZE * v;
		long endBit = domainSize * CELL_SIZE * v + 1;

		int startInt = (int) (startBit / 32);
		int endInt = (int) (endBit / 32);
		int endIndex = (int) (endBit % 32);
		int startIndex = (int) (startBit % 32);

		if (startInt == endInt) {
			// If the whole row is contain within the current int
			int m = 0xFFFFFFFE << (domainSize * CELL_SIZE);
			for (int i = 0; i < startIndex; i++) {
				m <<= 1;
				m |= 1;
			}
			vertices[startInt] &= m;
		} else {
			// clear the starting int
			if (startIndex != 0) {
				// Need to only clear the END of the starting int
				int m = 0;
				// ADD 1's until the start index
				for (int i = 0; i < startIndex; i++) {
					m <<= 1;
					m |= 1;
				}
				vertices[startInt] &= m;
				startInt++;
			}
			while (startInt < endInt) {
				vertices[startInt++] = 0;
			}
			//Clear as much of the last index as needed
			int m = 0xFFFFFFFF << endIndex;
			vertices[endInt] &= m;

		}

		// Now delete the column
	}

	public void remove(int v) {
		clear(v);
	}

	public boolean addEdge(int from, int to, int c) {
		numEdges += c;

		// the following is a hack to check
		// whether the edge we're inserting
		// is already in the graph or not
		Map<Integer, Integer> tos = edges.get(to);
		Integer i = tos.get(from);
		if (i != null) {
			// edge already present so another multi-edge!
			numMultiEdges += c;
			tos.put(from, i + c);
			// don't want to increment same edge twice!
			if (from != to) {
				tos = edges.get(from);
				i = edges.get(from).get(to);
				tos.put(to, i + c);
			}
			return true;
		} else {
			// completely new edge!
			numMultiEdges += (c - 1);
			tos.put(from, c);
			// self-loops only get one mention in the edge set
			if (from != to) {
				edges.get(from).put(to, c);
			}
			return false;
		}
	}

	/**
	 * Add an undirected edge from from to to
	 * 
	 * @param from
	 *            Source
	 * @param to
	 *            Sink
	 * @return True if the edge already exists
	 */
	public boolean addEdge(int from, int to) {
		return addEdge(from, to, 1);
	}

	public boolean removeEdge(int from, int to, int c) {
		Map<Integer, Integer> fset = edges.get(from); // optimisation
		Integer i = fset.get(to);
		if (i != null) {
			if (i > c) {
				// this is a multi-edge, so decrement count.
				numMultiEdges -= c;
				numEdges -= c;
				fset.put(to, i - c);
				if (from != to) {
					i = edges.get(to).get(from);
					edges.get(to).put(from, i - c);
				}
			} else {
				// clear our ALL edges
				numEdges -= i;
				numMultiEdges -= (i - 1);
				fset.remove(to);
				if (from != to) {
					edges.get(to).remove(from);
				}
			}
			return true;
		}
		return false;
	}

	public int removeAllEdges(int from, int to) {
		// remove all edges "from--to"
		int r = 0;
		Map<Integer, Integer> fset = edges.get(from); // optimisation
		Integer i = fset.get(to);
		if (i != null) {
			r = i;
			numEdges -= r;
			numMultiEdges -= (r - 1);
			fset.remove(to);
			if (from != to) {
				edges.get(to).remove(from);
			}
		}

		return r;
	}

	/**
	 * Remove an edge (only one in the case of a multi edge)
	 * 
	 * @param from
	 *            Source
	 * @param to
	 *            Sink
	 * @return
	 */
	public boolean removeEdge(int from, int to) {
		return removeEdge(from, to, 1);
	}

	public void remove(AdjacencyList g) {
		boolean done = false;
		for (Integer i : g.vertices) {
			done = false;
			out: while (!done) {
				for (Map.Entry<Integer, Integer> j : g.edges.get(i).entrySet()) {
					if (i >= j.getKey()) {
						if (removeEdge(i, j.getKey(), j.getValue())) {
							continue out;
						}
					}
				}
				done = true;
			}
		}
	}

	// Ok, this implementation is seriously inefficient!
	// could use an indirection trick here as one solution?
	//
	// POST: vertex 'from' remains, whilst vertex 'to' is removed
	void contractEdge(int from, int to) {
		if (from == to) {
			throw new RuntimeException("cannot contract a loop!");
		}
		for (Map.Entry<Integer, Integer> i : edges.get(to).entrySet()) {
			if (i.getKey() == to) {
				// is self loop
				addEdge(from, from, i.getValue());
			} else {
				addEdge(from, i.getKey(), i.getValue());
			}
		}
		remove(to);
	}

	// Ok, this implementation is seriously inefficient!
	// could use an indirection trick here as one solution?
	//
	// POST: vertex 'from' remains, whilst vertex 'to' is removed
	public void simpleContractEdge(int from, int to) {
		if (from == to) {
			throw new RuntimeException("cannot contract a loop!");
		}
		for (Map.Entry<Integer, Integer> i : edges.get(to).entrySet()) {
			if (from != i.getKey() && numEdges(from, i.getKey()) == 0) {
				addEdge(from, i.getKey(), 1);
			}
		}
		remove(to);
	}

	public String toString() {
		StringBuilder ss = new StringBuilder();
		for (int i : vertices) {
			for (Pair<Integer, Integer> e : edges(i)) {
				ss = ss.append(i).append(" -> ").append(e.first()).append(" x").append(e.second()).append('\n');
			}
		}

		return ss.toString();
	}

	public int hashcode() {
		return Hash.hashcode(edges);
	}

	private class VertexIterable implements Iterable<Integer> {

		@Override
		public Iterator<Integer> iterator() {
			return new VertexIterator(AdjacencyMatrix.this.vertices);
		}

	}

	private class EdgeIterable implements Iterable<Pair<Integer, Integer>> {
		private int vertex;

		public EdgeIterable(int v) {
			this.vertex = v;
		}

		@Override
		public Iterator<Pair<Integer, Integer>> iterator() {
			return new EdgeIterator(AdjacencyMatrix.this.edges, vertex);
		}

	}

	public class EdgeIterator implements Iterator<Pair<Integer, Integer>> {
		private int vertex;
		private int[] edges;
		private Pair<Integer, Integer> next;

		long startBit;
		long endBit;

		long currBit;
		int dest;

		public EdgeIterator(int[] edges, int vertex) {
			this.edges = edges;
			this.vertex = vertex;
			startBit = domainSize * CELL_SIZE * vertex;
			endBit = domainSize * CELL_SIZE * (vertex + 1);

			dest = 0;
			currBit = startBit;
			findNextEdge();
		}

		/**
		 * Goes through and sets next to be the next available edge if there is one
		 */
		private void findNextEdge() {
			while (true) {
				int startInt = (int) (currBit / 32);
				int index = (int) (currBit % 32);
				boolean overflow = (index + CELL_SIZE) > 32;

				int ret;
				if (overflow) {
					long v = edges[startInt] | edges[startInt + 1] << 32;
					int mask = 0;
					int m = 1;
					for (int i = 0; i < CELL_SIZE; i++) {
						mask |= m;
						m <<= 1;
					}
					ret = (int) (((v & (mask << index)) >> index) & m);
				} else {
					int v = edges[startInt];
					int mask = 0;
					int m = 1;
					for (int i = 0; i < CELL_SIZE; i++) {
						mask |= m;
						m <<= 1;
					}
					ret = ((v & (mask << index)) >> index) & m;
				}
				currBit += CELL_SIZE;
				dest++;
				if (ret != 0) {
					next = new Pair<Integer, Integer>(dest, ret);
					return;
				} else if (dest == domainSize) {
					next = null;
					return;
				}
			}
		}

		@Override
		public boolean hasNext() {
			return next == null;
		}

		@Override
		public Pair<Integer, Integer> next() {
			Pair<Integer, Integer> ret = next;
			findNextEdge();
			return ret;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException("Remove not implemented");

		}

	}

	private class VertexIterator implements Iterator<Integer> {
		private int[] vertices;
		private int nextIndex;

		public VertexIterator(int[] vertices) {
			this.vertices = vertices;
			if (numVertices == 0) {
				nextIndex = -1;
			} else {
				nextIndex = startVertex;
			}
		}

		@Override
		public boolean hasNext() {
			return nextIndex != -1;
		}

		@Override
		public Integer next() {
			int ret = nextIndex;
			nextIndex = vertices[nextIndex];
			return ret;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException("Remove not implemented");
		}

	}
}
