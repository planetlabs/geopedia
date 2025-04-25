package com.sinergise.common.geometry.topo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.sinergise.common.geometry.geom.LinearRing;
import com.sinergise.common.geometry.util.GeomUtil;
import com.sinergise.common.util.math.MathUtil;

public class TopoCleaner {
	
	private static final double ULP1 = 1E-7;
	
	private double gridSize = ULP1;
	
	public void setGridSize(double gridSize) {
		this.gridSize = Math.max(gridSize, ULP1);
	}
	
	public List<LinearRing> cleanRing(LinearRing ring) {
		return cleanRingSelfIntersections(ring);
	}
	
	public List<LinearRing> cleanRingSelfIntersections(LinearRing ring) {
		
		snapRingToGrid(ring);
		
		Node first = null;
		Set<String> visited = new HashSet<String>(ring.getNumCoords());
		
		List<List<Node>> newRings = new ArrayList<List<Node>>();
		List<Node> mainRing = new ArrayList<Node>(ring.getNumCoords());
		
		Node prev = null;
		for (int i=0; i<ring.getNumCoords(); i++) {
			Node n = new Node(ring.getX(i), ring.getY(i));
			if (prev != null && n.equalsPosition(prev)) {
				continue;
			}
			String nKey = nodeKey(n);
			
			//check for self intersection
			if (visited.contains(nKey) && !n.equalsPosition(first)) {
				//extract as new line string by backtracking to previous node occurrence
				List<Node> newRingNodes = new ArrayList<Node>();
				newRingNodes.add(n);
				
				while(!mainRing.isEmpty()) {
					Node next = mainRing.remove(mainRing.size()-1);
					newRingNodes.add(next);
					if (next.equalsPosition(n)) {
						break;
					}
				}
				
				newRings.add(newRingNodes);
			}
			
			if (i == 0) {
				first = n;
			}
			
			mainRing.add(n);
			visited.add(nKey);
			prev = n;
		}
		
		newRings.add(mainRing);
		
		List<LinearRing> result = new ArrayList<LinearRing>();
		for (List<Node> ringNodes : newRings) {
			//skip if not a linear ring
			if (ringNodes.size() > 3) {
				LinearRing newRing = TopoUtil.nodesToLinearRing(ringNodes);
				if (newRing.getArea() > 0) {
					result.add(newRing);
				}
			}
		}
		
		return result;
	}
	
	
	private void snapRingToGrid(LinearRing ring) {
		List<Node> nodes = new ArrayList<Node>(ring.getNumCoords());
		for (int i=0; i<ring.getNumCoords(); i++) {
			nodes.add(new Node(
				snapToGrid(ring.getX(i)), 
				snapToGrid(ring.getY(i))));
		}
		
		double minDistSq = gridSize*gridSize*(1-ULP1);
		
		//snap points intersecting edges
		//TODO: use spatial index
		//TODO: handle edge intersections
		List<Node> listIndex = new ArrayList<Node>(nodes);
		for (Node n : listIndex) {
			for (int i=1; i<listIndex.size(); i++) {
				Node n1 = listIndex.get(i-1);
				Node n2 = listIndex.get(i);
				
				double distSq = GeomUtil.distancePointLineSegmentSq(n.x(), n.y(), n1.x(), n1.y(), n2.x(), n2.y());
				if (distSq < minDistSq && !n1.equalsPosition(n) && !n2.equalsPosition(n)) {
					nodes.add(nodes.indexOf(n2), n);
				}
			}
		}
		
		//remove short segments
		listIndex = new ArrayList<Node>(nodes);
		for (int i=1; i<listIndex.size(); i++) {
			if (GeomUtil.distanceSq(listIndex.get(i-1), listIndex.get(i)) < minDistSq) {
				nodes.remove(listIndex.get(i));
			}
		}
		
		ring.coords = TopoUtil.nodesToLinearRing(nodes).coords;
	}
	
	private double snapToGrid(double ord) {
		if (gridSize != 0) {
			return MathUtil.roundToNearestMultiple(ord, gridSize);
		}
		return ord;
	}

	private static String nodeKey(Node n) {
		return nodeKey(n.x(), n.y());
	}
	
	private static String nodeKey(double x, double y) {
		return x+"-"+y;
	}

}
