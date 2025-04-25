package com.sinergise.common.geometry.algorithm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.TreeSet;

import com.sinergise.common.util.math.MathUtil;


/**
 * 
 * E.W.Dijkstra, A Note on Two Problems in Connexion with Graphs
 * Numerische Mathematlk 1, 269 - 271 (1959)
 * 
 * Adopted from <a href="http://en.wikipedia.org/wiki/Dijkstra%27s_algorithm#Pseudocode">Dijkstra's Algorithm at Wikipedia</a>
 * 
 * @author Miha
 *
 */
public class ShortestPathDijkstra<N, E> {
	
	public static interface DijkstraTopologyProvider<N, E> {
		//Edge should be directed away from node so that getStartNode returns 'node', and getEndNode returns something else
		Iterable<? extends E> getEdgesFrom(N node);
		double getCost(E edge);
		N getEndNode(E edge);
		N getStartNode(E edge);
	}
	
	public static final class DijkstraVertex<N, E> implements Comparable<DijkstraVertex<? super N, ? super E>> {
		protected double cost = Double.POSITIVE_INFINITY;
		protected E inEdge = null;
		protected boolean finished = false;
		protected N node;
		
		public DijkstraVertex(N node) {
			this.node = node;
		}
		
		public N getNode() {
			return node;
		}

		@Override
		public int compareTo(DijkstraVertex<? super N, ? super E> o) {
			int ret = MathUtil.fastCompare(this.cost, o.cost); //Don't return 0 or the TreeSet will merge vertices
			if (ret == 0 && this != o) {
				ret = this.hashCode() - o.hashCode();
				if (ret == 0) {
					return 1;
				}
			}
			return ret;
		}

		public E getShortestPathIncidentEdge() {
			return inEdge;
		}

		public double getCost() {
			return cost;
		}
	}
	
	TreeSet<DijkstraVertex<N, E>> q = new TreeSet<DijkstraVertex<N, E>>();
	HashSet<DijkstraVertex<N, E>> targets;
	HashMap<N, DijkstraVertex<N, E>> preparedNodes = new HashMap<N, DijkstraVertex<N,E>>();
	DijkstraTopologyProvider<N, E> topoProvider;

	public ShortestPathDijkstra(DijkstraTopologyProvider<N, E> topoProvider) {
		this.topoProvider = topoProvider;
	}
	
	public void reset() {
		q.clear();
	}
	
	public void addSource(N source) {
		DijkstraVertex<N, E> v = getVertex(source);
		v.cost = 0; // Distance from source to source
		q.add(v);
	}
	
	public DijkstraVertex<N, E> getVertex(N node) {
		DijkstraVertex<N, E> ret = preparedNodes.get(node);
		if (ret == null) {
			ret = createVertex(node);
			preparedNodes.put(node, ret);
		}
		return ret;
	}
	
	protected DijkstraVertex<N, E> createVertex(N node) {
		return new DijkstraVertex<N, E>(node);
	}

	public void addTarget(N target) {
		if (targets == null) {
			targets = new HashSet<DijkstraVertex<N, E>>();
		}
		DijkstraVertex<N, E> v = getVertex(target);
		targets.add(v);
	}
	
	/**
	 * To get the shortest path, use getVertex(target) and iteratively call getShortestPathIncidentEdge()
	 */
	public void compute() {
		while (!q.isEmpty()) {
			DijkstraVertex<N, E> u = q.first();
			if (u == null) {
				// all remaining vertices are inaccessible from source
				return;
			}
			u.finished  = true;
			q.remove(u);
			
			if (checkFinished(u)) {
				return;
			}
			
			for (E edgeV : topoProvider.getEdgesFrom(u.node)) {
				final N vNode = topoProvider.getEndNode(edgeV);
				final DijkstraVertex<N, E> v = getVertex(vNode);
				if (v.finished) {
					continue;
				}

				q.remove(v); // v.dist might be changed, so we have to remove before
				processPath(u, edgeV, v);
				q.add(v); // now add to re-apply the sort order
			}
		}
	}
	
	public List<N> getShortestPathFromTarget(N target) {
		DijkstraVertex<N, E> v = preparedNodes.get(target);
		
		if (v == null || v.getShortestPathIncidentEdge() == null) {
			return null;
		}
		
		List<N> path = new ArrayList<N>();
		path.add(target);
		
		while (v.getShortestPathIncidentEdge() != null) {
			N next = topoProvider.getStartNode(v.getShortestPathIncidentEdge());
			v = preparedNodes.get(next);
			path.add(next);
		}
		return path;
	}

	protected void processPath(DijkstraVertex<N, E> fromVertex, E overEdge, DijkstraVertex<N, E> toVertex) {
		final double newCost = fromVertex.cost + topoProvider.getCost(overEdge);
		if (newCost < toVertex.cost) {
			toVertex.cost = newCost;
			toVertex.inEdge = overEdge;
		}
	}

	protected boolean checkFinished(DijkstraVertex<N, E> u) {
		if (targets == null) {
			return false;
		}
		targets.remove(u);
		return targets.isEmpty();
	}
	
}
