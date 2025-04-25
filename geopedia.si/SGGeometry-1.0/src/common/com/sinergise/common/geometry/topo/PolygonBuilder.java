package com.sinergise.common.geometry.topo;

import static com.sinergise.common.util.Util.ifnull;
import static com.sinergise.common.util.collections.CollectionUtil.first;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import com.sinergise.common.geometry.geom.Geometry;
import com.sinergise.common.geometry.geom.HasGeometry;
import com.sinergise.common.geometry.geom.LinearRing;
import com.sinergise.common.geometry.io.wkt.WKTWriter;
import com.sinergise.common.geometry.topo.RingIterator.InnerRingIterable;
import com.sinergise.common.geometry.topo.RingIterator.OuterRingIterable;
import com.sinergise.common.geometry.topo.TopoBuilder.NodeKey;

public class PolygonBuilder {

	private ITopoMap topology = null;
	private Face defaultFace = null;

	private final Map<HasGeometry, Face> faceReferences = new HashMap<HasGeometry, Face>();
	private final Map<Face, Geometry> polygonsCache = new HashMap<Face, Geometry>();

	public PolygonBuilder() {
		//empty constructor
	}

	public PolygonBuilder(ITopoMap topology) {
		setTopology(topology);
	}
	
	public PolygonBuilder(ITopoMap topology, Map<HasGeometry, Face> references) {
		this(topology);
		setFaceReferences(references);
	}

	public void setTopology(ITopoMap topology) {
		this.topology = topology;
		invalidateCache();
	}

	public void setFaceReferences(Map<HasGeometry, Face> references) {
		this.faceReferences.clear();
		this.faceReferences.putAll(references);
	}
	
	public Map<HasGeometry, Face> getFaceReferences() {
		return Collections.unmodifiableMap(faceReferences);
	}

	private void invalidateCache() {
		polygonsCache.clear();
	}
	
	/**
	 * @param defaultFace Face to use on unlabeled edges
	 */
	public void setDefaultFace(Face defaultFace) {
		this.defaultFace = defaultFace;
	}
	
	public void setDefaultFace(HasGeometry fromGeom) {
		setDefaultFace(faceReferences.get(fromGeom));
	}

	public Geometry buildPolygonFor(HasGeometry geomHolder) throws TopologyException {
		Face face = faceReferences.get(geomHolder);
		if (face == null) {
			return null; //face reference not found
		}
		return buildPolygonFor(face);
	}

	public synchronized Geometry buildPolygonFor(Face face) throws TopologyException {
		Geometry poly = polygonsCache.get(face);
		if (poly == null) {
			buildPolygons();
			poly = polygonsCache.get(face);
		}

		return poly;
	}

	public synchronized Geometry buildPolygonForDefaultFace() throws TopologyException {
		if (defaultFace != null) {
			return buildPolygonFor(defaultFace);
		}
		return null;
	}

	private void buildPolygons() throws TopologyException {
		polygonsCache.clear();
		Map<Face, List<List<Node>>> allNodes = buildFaceRings();
		
		for (Face face : allNodes.keySet()) {
			List<List<Node>> faceNodes = allNodes.get(face);
			ArrayList<LinearRing> faceRings = new ArrayList<LinearRing>(faceNodes.size());

			for (List<Node> ringNodes : faceNodes) {
				LinearRing ring = (LinearRing)TopoUtil.nodesToLineString(ringNodes, true);
				if (ring != null && ring.getArea() > 0) {
					faceRings.add(ring);
				}
			}

			if (!faceRings.isEmpty()) {
				Geometry poly = EvenOddPolygonizer.polygonize(faceRings);
				polygonsCache.put(face, poly);
			}
		}
	}
	
	private Map<Face, List<List<Node>>> buildFaceRings() throws TopologyException {
		return buildFaceRingsNoClone(new LiveTopoMap(topology.getEdges()), defaultFace);
	}
			
	static Map<Face, List<List<Node>>> buildFaceRings(LiveTopoMap map, Face defaultInnerFace) throws TopologyException {
//		PolygonBuilder builder = new PolygonBuilder(map);
//		builder.setDefaultFace(defaultInnerFace);
//		return builder.buildFaceRings();
		return buildFaceRingsNoClone(new LiveTopoMap(map.getEdges()), defaultInnerFace);
	}
	private static Map<Face, List<List<Node>>> buildFaceRingsNoClone(LiveTopoMap map, Face defaultInnerFace) throws TopologyException {
		Map<Face, List<List<Node>>> rings = new HashMap<Face, List<List<Node>>>();
		Set<Node> visited = new HashSet<Node>(map.getNodes().size());
		
		removeDanglingTopology(map);
		
		while (true) {
			Set<DirectedEdge> heap = new LinkedHashSet<DirectedEdge>();
			
			// find outer ring
			Node leftmostNode = map.getLeftmostNode(visited); //left most node
			if (leftmostNode == null) {
				break;
			}
			for (DirectedEdge edge : new OuterRingIterable(map, leftmostNode)) {
				if (!heap.add(edge)) {
					heap.remove(edge);
				}
			}
			
			// add ring to proper face
			Face outerFace = extractCCWRingFace(heap, defaultInnerFace, false);
			if (outerFace != null) {
				List<List<Node>> faceRings = rings.get(outerFace);
				if (faceRings == null) {
					rings.put(outerFace, faceRings = new ArrayList<List<Node>>());
				}
				faceRings.addAll(handleRingSelfIntersections(extractRingNodes(heap)));
			}
			
			// find inner rings inside the outer ring
			Set<DirectedEdge> removed = new HashSet<DirectedEdge>(heap.size());
			
			while (!heap.isEmpty()) {
				List<DirectedEdge> ring = new ArrayList<DirectedEdge>();
				for (DirectedEdge edge : new InnerRingIterable(map, first(heap))) {
					ring.add(edge);
					
					if (!heap.remove(edge)) {
						if (!removed.contains(edge)) {
							heap.add(edge.getReversed());
						} else { //edge should not be visited more than twice
							throw new TopologyException("Edge visited more than twice: "+edge);
						}
					} else {
						removed.add(edge);
					}
				}
				
				// add ring to proper face
				Face innerFace = extractCCWRingFace(ring, defaultInnerFace, true);
				List<Node> ringNodes = extractRingNodes(ring);
				
				if (innerFace != null) {
					List<List<Node>> faceRings = rings.get(innerFace);
					if (faceRings == null) {
						rings.put(innerFace, faceRings = new ArrayList<List<Node>>());
					}
					
					faceRings.addAll(handleRingSelfIntersections(ringNodes));
				}
				visited.addAll(ringNodes);
			}
		}
		
		removeRingsLabelledTwice(rings); 
		
		return rings;
	}

	// exclude rings labeled from both sides with the same face
	private static void removeRingsLabelledTwice(Map<Face, List<List<Node>>> rings) {
		for (List<List<Node>> r : rings.values()) {
			removeRingsLabelledTwice(r);
		}
	}

	private static void removeRingsLabelledTwice(List<List<Node>> faceRings) {
		HashMap<Node, AtomicInteger> countPerNode = new HashMap<Node, AtomicInteger>();
		for (List<Node> ring : faceRings) {
			for (Node n : ring) {
				// n.hashCode() could be expensive; increment count with a single put instead of get + put 
				AtomicInteger newVal = new AtomicInteger(1);
				AtomicInteger oldVal = countPerNode.put(n, newVal);
				if (oldVal != null) {
					newVal.addAndGet(oldVal.get());
				}
			}
		}
		for (Iterator<List<Node>> it = faceRings.iterator(); it.hasNext();) {
			List<Node> ring = it.next();
			boolean remove = true;
			for (Node n : ring) {
				if (countPerNode.get(n).intValue() < 2) {
					remove = false;
					break;
				}
			}
			if (remove) {
				for (Node n : ring) {
					countPerNode.get(n).decrementAndGet();
				}
				it.remove();
			}
		}
	}

	private static void removeDanglingTopology(LiveTopoMap map) throws TopologyException {
		Set<Node> visited = new HashSet<Node>(map.getNodes().size());
		
		//remove dangling edges
		Node singleEdgeNode = map.getSingleEdgeNode(visited);
		while (singleEdgeNode != null) {
			DirectedEdge edge = map.getNodeStar(singleEdgeNode).getFirst(); // only 1 anyway
			Node nextNode = singleEdgeNode;
			while (true) {
				visited.add(nextNode);
				
				nextNode = edge.getEndNode();
				NodeStar star = map.getNodeStar(nextNode).rotate(edge);
				
				try {
					if (star.size() != 2 || visited.contains(nextNode)) {
						//relabel possible outer ring edges
						if (star.size() > 2) {
							DirectedEdge last = edge.getDirectedEdge(nextNode);
							DirectedEdge leftEdge = star.left();
							DirectedEdge rightEdge = star.right();
							
							if (last.getLeftFace() != null && last.getLeftFace().equals(leftEdge.getRightFace())) {
								for (DirectedEdge e : new OuterRingIterable(map, leftEdge)) { //inner ring CW
									if (last.getLeftFace().equals(e.getRightFace())) {
										map.changeEdgeFace(e, last.getRightFace(), false);
									}
								}
							}
							
							if(last.getRightFace() != null && last.getRightFace().equals(rightEdge.getLeftFace())) {
								for (DirectedEdge e : new InnerRingIterable(map, rightEdge)) {
									if (last.getRightFace().equals(e.getLeftFace())) {
										map.changeEdgeFace(e, last.getLeftFace(), true);
									}
								}
							}
							
						}
						break;
					}
				} finally {
					map.deleteEdge(edge);
				}
				
				edge = star.getFirst();
			}
			singleEdgeNode = map.getSingleEdgeNode(visited);
		}
		
		//remove dangling nodes
		List<Node> danglingNodes = new ArrayList<Node>();
		for (Node n : map.getNodes()) {
			if (map.getNodeStar(n).isEmpty()) {
				danglingNodes.add(n);
			}
		}
		for (Node n : danglingNodes) {
			map.deleteNode(n);
		}
	}
	
	private static Face extractCCWRingFace(Collection<DirectedEdge> ring, Face defaultFace, boolean inner) {
		//return the face with most occurrences (some edges might me labeled wrong due to topology editing)
		
		Map<Face, AtomicInteger> ringCnt = new HashMap<Face, AtomicInteger>();
		for (DirectedEdge e : ring) {
			Face innerFace = e.getFace(inner);
			if (defaultFace != null && defaultFace.equals(innerFace)) {
				//return default face if contained on the ring (but not when assigned instead of null face)
				return defaultFace;
			}
			
			Face face = ifnull(innerFace, defaultFace);
			
			// single hashCode() invocation
			AtomicInteger newVal = new AtomicInteger(1);
			AtomicInteger val = ringCnt.put(face, newVal);
			if (val != null) {
				newVal.addAndGet(val.intValue());
			}
		}
		
		Face maxFace = null;
		int maxCnt = 0;
		for (Map.Entry<Face, AtomicInteger> f : ringCnt.entrySet()) {
			int cnt = f.getValue().intValue();
			if (cnt > maxCnt) {
				maxCnt = cnt;
				maxFace = f.getKey();
			}
		}
		return maxFace;
	}
	
	private static List<Node> extractRingNodes(Collection<DirectedEdge> ring) {
		List<Node> nodes = new ArrayList<Node>(ring.size()+1);
		
		nodes.add(first(ring).getStartNode());
		for (Edge e : ring) {
			nodes.add(e.getEndNode());
		}
		return nodes;
	}
	
	private static List<List<Node>> handleRingSelfIntersections(List<Node> ringNodes) {
		List<List<Node>> result = new ArrayList<List<Node>>();
		
		//TODO: NodeKey hashCode has slow performance (~ 100% of this method is spent on computing hashCode)
		Set<NodeKey> visited = new HashSet<NodeKey>(ringNodes.size());
		List<Node> mainRing = new ArrayList<Node>(ringNodes.size());
		
		int cnt = 0;
		for (Node n : ringNodes) {
			NodeKey key = new NodeKey(n);
			cnt++;
			
			//check for self intersection
			if (visited.contains(key) && ringNodes.size() != cnt) {
				//extract as new ring by backtracking to previous node occurrence
				List<Node> newRing = new ArrayList<Node>();
				newRing.add(n);
				
				while (!mainRing.isEmpty()) {
					Node next = mainRing.remove(mainRing.size()-1);
					newRing.add(next);
					if (next.equalsPosition(n)) {
						break;
					}
				}
				
				if (newRing.size() > 3) {
					result.add(newRing);
				}
			} 
			mainRing.add(n);
			visited.add(key);
		}
		
		if (mainRing.size() > 3) {
			result.add(mainRing);
		}
		return result;
	}
	
	static void printRing(Collection<? extends Edge> edges) {
		if (edges.size() < 3) {
			return;
		}
		double[] coords = new double[edges.size()*2 +2];
		int c=0;
		for (Edge e : edges) {
			coords[c++] = e.x1();
			coords[c++] = e.y1();
		}
		coords[c++] = first(edges).x1();
		coords[c++] = first(edges).y1();
		System.out.println(WKTWriter.write(new LinearRing(coords)));
	}
}
