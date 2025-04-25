/**
 * 
 */
package com.sinergise.common.geometry.topo;

import static com.sinergise.common.geometry.topo.Edge.bearing;
import static com.sinergise.common.util.collections.CollectionUtil.first;
import static com.sinergise.common.util.collections.CollectionUtil.last;
import static com.sinergise.common.util.lang.Pair.newPair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sinergise.common.geometry.geom.Geometry;
import com.sinergise.common.geometry.geom.LineString;
import com.sinergise.common.geometry.geom.LinearRing;
import com.sinergise.common.geometry.geom.Polygon;
import com.sinergise.common.geometry.topo.RingIterator.InnerRingIterable;
import com.sinergise.common.util.geom.CoordinatePair;
import com.sinergise.common.util.geom.HasCoordinate;
import com.sinergise.common.util.lang.Pair;

/**
 * @author tcerovski
 */
public class TopoUtil {

	public static LinearRing nodesToLinearRing(List<Node> nodes) {
		return (LinearRing)nodesToLineString(nodes, true);
	}
	
	public static LineString nodesToLineString(List<Node> nodes, boolean forceClosed) {
		if (forceClosed && nodes.size() > 2 && !first(nodes).equalsPosition(last(nodes))) {
			nodes.add(nodes.get(0));
		}
		final boolean closed = first(nodes).equalsPosition(last(nodes));
		
		final double[] dCoords = new double[nodes.size()*2];
		int i=0;
		for(HasCoordinate c : nodes) {
			dCoords[i++] = c.x();
			dCoords[i++] = c.y();
		}
		return closed ? new LinearRing(dCoords) : new LineString(dCoords);
	}
	
	public static Polygon nodesToPolygon(List<Node> outer, List<List<Node>> inner) {
		if(outer == null || outer.size() == 0)
			return null;
		
		LinearRing outerRing = nodesToLinearRing(outer);
		LinearRing[] innerRings = new LinearRing[inner.size()];
		
		int i = 0;
		for(List<Node> nodes : inner) {
			innerRings[i++] = nodesToLinearRing(nodes);
		}
		
		return new Polygon(outerRing, innerRings);
	}
	
	@Deprecated /** Use TopoBuilder directly instead */
	public static Collection<Edge> geometryToTopology(Geometry geom) {
		TopoBuilder builder = new TopoBuilder();
		builder.addGeometry(geom);
		return builder.buildTopologyEdges();
	}
	
	@Deprecated /** Use TopologyBuilder directly instead */
	public static ITopoMap geometryToTopoMap(Geometry geom) throws TopologyException {
		TopoBuilder builder = new TopoBuilder();
		builder.addGeometry(geom);
		return builder.buildTopology();
	}
	
	public static List<Edge> findArc(CoordinatePair startSeg, LiveTopoMap topo) {
		LinkedList<Edge> arc = new LinkedList<Edge>();
		
		Node nearestN = topo.getNearestNode(startSeg.c1(), Double.MAX_VALUE, new HashSet<Node>());
		//find by nearest bearing as topo builder might split existing edges while cleaning topology
		Edge start = findEdgeWithNearestBearing(topo.getNodeStar(nearestN), bearing(startSeg));
		
		for (DirectedEdge e : new InnerRingIterable(topo, start.getDirectedEdge(start.getStartNode()))) {
			arc.addLast(e);
			    
			if (topo.getNodeStar(e.getEndNode()).size() > 2) {
				break;
			}
		}
		
		if (arc.getLast().getEndNode().equalsPosition(start.getStartNode())) {
			return arc;
		}
		
		for (DirectedEdge e : new InnerRingIterable(topo, start.getDirectedEdge(start.getEndNode()))) {
			if (!start.equals(e)) { 
				arc.addFirst(e.getReversed());
			}
			 
			if (topo.getNodeStar(e.getEndNode()).size() > 2) {
				break;
			}
		}
		
		return arc;
	}
	
	public static Edge findEdgeTo(NodeStar star, HasCoordinate c) {
		for (Edge e : star) {
			if (e.getEndNode().equalsPosition(c)) {
				return e;
			}
		}
		return null;
	}
	
	public static Edge findEdgeWithNearestBearing(NodeStar star, double bearing) {
		Edge nearest = null;
		double minDiff = Double.MAX_VALUE;
		
		for (Edge e : star) {
			double diff = Math.abs(bearing - e.bearing());
			if (diff < minDiff) {
				minDiff = diff;
				nearest = e;
			}
		}
		return nearest;
	}
	
	public static Collection<Edge> deepCopyEdges(Collection<Edge> edges) {
		Set<Edge> copy = new HashSet<Edge>(edges.size());
		Map<Node, Node> nodeCopies = new HashMap<Node, Node>();
		Map<Face, Face> faceCopies = new HashMap<Face, Face>();
		for (Edge e : edges) {
			copy.add(e.deepCopy(nodeCopies, faceCopies));
		}

		return copy;
	}
	
	public static void replaceTopology(LiveTopoMap topoMap, ITopoMap toReplace, ITopoMap replacement) throws TopologyException {
		TopoEditorModel topoEditor = new TopoEditorModel(topoMap);
		removeFromTopology(topoEditor, toReplace);
		insertIntoTopology(topoEditor, replacement);
	}
	
	public static void removeFromTopology(TopoEditorModel topoEditor, ITopoMap toRemove) throws TopologyException {
		for (Edge e : toRemove.getEdges()) {
			topoEditor.deleteEdge(e);
		}
		
		for (Node n : toRemove.getNodes()) {
			if (!n.isLocked()) {
				topoEditor.deleteNode(n);
			}
		}
	}
	
	public static void insertIntoTopology(TopoEditorModel topoEditor, ITopoMap toAdd) throws TopologyException {
		
		List<Pair<Node, Node>> toMerge = new ArrayList<Pair<Node,Node>>();
		Set<Node> excluded = Collections.emptySet();
		double maxDist = topoEditor.getGridSize();
		
		for (Node n : toAdd.getNodes()) {
			Node target = topoEditor.getNearestNode(n, maxDist, excluded);
			topoEditor.createNode(n);
			
			if (target != null) {
				toMerge.add(newPair(n, target));
			} 
		}
		
		for (Edge e : toAdd.getEdges()) {
			Edge newEdge = topoEditor.createEdge(e.getStartNode(), e.getEndNode());
			newEdge.setFace(e.getFace(true), true);
			newEdge.setFace(e.getFace(false), false);
		}
		Map<Node, Node> merged = new HashMap<Node, Node>();
		for (Pair<Node, Node> pair : toMerge) {
			Node source = followMerges(merged, pair.getFirst());
			Node target = followMerges(merged, pair.getSecond());
			
			Node result = topoEditor.mergeNodes(source, target);
			// Maybe too much, but better safe than sorry
			merged.put(pair.getFirst(), result);
			merged.put(pair.getSecond(), result);
			merged.put(source, result);
			merged.put(target, result);
		}
	}

	private static Node followMerges(Map<Node, Node> mergeMap, Node n) {
		while (true) {
			Node curTgt = mergeMap.get(n);
			if (curTgt == null || curTgt == n) {
				return n;
			}
			n = curTgt;
		}
	}
	
}
