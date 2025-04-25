/**
 * 
 */
package com.sinergise.common.geometry.topo.action;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import com.sinergise.common.geometry.geom.LinearRing;
import com.sinergise.common.geometry.topo.Edge;
import com.sinergise.common.geometry.topo.Face;
import com.sinergise.common.geometry.topo.Node;
import com.sinergise.common.geometry.topo.NodeStar;
import com.sinergise.common.geometry.topo.TopoFactory;
import com.sinergise.common.geometry.topo.TopoMap;
import com.sinergise.common.geometry.topo.TopologyException;
import com.sinergise.common.geometry.topo.op.TopoOpFactory;
import com.sinergise.common.geometry.util.GeomUtil;
import com.sinergise.common.util.geom.HasCoordinate;


/**
 * <p>Updates polygon boundary.</p>
 * <b>Only simple adding of new parcel supported for now.</b>
 * 
 * Required locks:
 * <ul>
 * 	<li>Seed</li>
 *  <li>All edges on possible existing boundaries that will have their faces changed</li>
 * </ul>
 * Additional checks:
 * <ul>
 * 	<li>Parcel centroid must be insde the parcel.</li>
 * </ul>
 * 
 * @author tcerovski
 */
public class EditFaceBoundaryAction extends TopoAction {
	
	final Face parcel;
	final List<List<HasCoordinate>> coords;
	
	
	public EditFaceBoundaryAction(Face parcel, List<List<HasCoordinate>> coords, TopoActionParams params) 
	{
		super(params);
		
		this.parcel = parcel;
		this.coords = coords;
	}
	
	@Override
	public void doPrepareAction(TopoMap map) throws TopologyException {
		
		TopoOpFactory opFact = map.getTopoOpFactory();
		TopoFactory topoFact = map.getTopoFactory();
		
		addOp(opFact.createCheckLockOp(parcel, params));
		
		//check locks on all touching nodes
		for(List<HasCoordinate> list : coords) {
			for(HasCoordinate c : list) {
				if(c instanceof Node) {
					addOp(opFact.createCheckLockOp((Node)c, params));
				}
			}
		}
		
		//find ring
		LinkedList<HasCoordinate> ring = null;
		LinearRing linearRing = null;
		
		RingWalker leftWalker = new RingWalker(map, true);
		RingWalker rightWalker = new RingWalker(map, false);
		
		leftWalker.findNow();
		if (leftWalker.containsParcelCentroid()) {
			linearRing = leftWalker.getLinearRing();
			ring = leftWalker.ring;
		} else {
			rightWalker.findNow();
			if (rightWalker.containsParcelCentroid()) {
				rightWalker.getLinearRing();
				ring = rightWalker.ring;
			} else {
				throw new TopologyException("Parcel seed point outside of parcel boundary or could not find new ring.");
			}
		}
		
		if (ring == null || linearRing == null) {
			throw new TopologyException("Could not find new ring.");
		}
		
		//set new faces
		Face innerFace = parcel;
		Face outerFace = map.getTopoFactory().getOuterFace(); 

		List<Edge> oldEdges = map.getFaceEdges(parcel);
		if (oldEdges == null) {
			oldEdges = new ArrayList<Edge>();
		}
		
		//ensure new ring orientation
		if(!GeomUtil.isCCW(linearRing)) {
			Collections.reverse(ring);
		}
		
		Node firstNode = topoFact.createNode(ring.getFirst());
		Node prevNode = firstNode;
		addOp(opFact.createAddNodeOp(prevNode, params, map));
		for(int i=1; i<ring.size(); i++) {
			HasCoordinate c1 = ring.get(i);
			
			//first and last nodes are the same
			Node node = i==ring.size()-1 ? firstNode : topoFact.createNode(c1);
			//check to prevent adding first node twice
			if(node != firstNode)
				addOp(opFact.createAddNodeOp(node, params, map));
			
			//check if edge exists
			Edge e = null;
			if((e = map.findEdge(prevNode, node)) != null) {
				addOp(opFact.createCheckLockOp(e, params));
				//use undirected edge
				//TODO: check this
				addOp(opFact.createChangeEdgeFaceOp(e.getEdge(), innerFace, e.getEdge().isLeftFace(parcel), params));
			} else {
				e = topoFact.createEdge(prevNode, node, innerFace, outerFace);
				addOp(opFact.createAddEdgeOp(e, params, map));
			}
			oldEdges.remove(e);
			
			prevNode = node;
		}
		
		//check if any island edges fall inside the new face
		List<Edge> toRemove = new ArrayList<Edge>();
		for(Edge e : oldEdges) {
			//both edge points in ring or one point in the ring and one point touching the ring
			boolean aIn = GeomUtil.isPointInRing(e.x1(), e.y1(), linearRing);
			boolean bIn = GeomUtil.isPointInRing(e.x2(), e.y2(), linearRing);
			boolean aOn = GeomUtil.indexOfXY(linearRing.coords, e.x1(), e.y1()) >= 0;
			boolean bOn = GeomUtil.indexOfXY(linearRing.coords, e.x2(), e.y2()) >= 0;
			
			if((aIn && bIn) || (aIn && bOn && !aOn) || (aOn && bIn && !bOn)) {
				toRemove.add(e);
				addOp(opFact.createCheckLockOp(e, params));
				//use undirected edge
				addOp(opFact.createChangeEdgeFaceOp(e.getEdge(), innerFace, e.getEdge().isLeftFace(parcel), params));
			}
		}
		oldEdges.removeAll(toRemove);
		
		//TODO: update all remaining edges
//		if(!parentFace.equals(secondResultingFace)) {
//			for(Edge e : oldEdges) {
//				addOp(opFact.createCheckLockOp(e, params));
//				//use undirected edge
//				addOp(opFact.createChangeEdgeFaceOp(e.getEdge(), outerFace, e.getEdge().isLeftFace(parentFace), params));
//			}
//		}
		
	}
	
	private class RingWalker {
		
		Queue<LinkedList<HasCoordinate>> queue;
		final Node firstNode;
		
		Edge lastEdge = null;
		Node nextFromQ1 = null;
		Node nextFromQ2 = null;
		
		Set<Edge> visited = new HashSet<Edge>();
		LinkedList<HasCoordinate> ring = new LinkedList<HasCoordinate>();
		
		//indicates direction
		final boolean goLeft;
		final TopoMap map;
		
		RingWalker(TopoMap map, boolean goLeft) throws TopologyException {
			this.goLeft = goLeft;
			this.map = map;
			init();
			addFromQueue();
			
			//make a temporary node
			firstNode = map.getTopoFactory().createTemporaryNode(ring.getFirst());
		}
		
		void init() throws TopologyException {
			queue = new LinkedList<LinkedList<HasCoordinate>>();
			for(List<HasCoordinate> c : coords) {
				//check nodes
				LinkedList<HasCoordinate> list = new LinkedList<HasCoordinate>(c);
				if(!list.getLast().equals(list.getFirst())) {
					if(!(list.getFirst() instanceof Node))
						throw new TopologyException("First point in coordinates list must be a node", list.getFirst());
					if(!(list.getLast() instanceof Node))
						throw new TopologyException("Last point in coordinates list must be a node", list.getLast());
				}
				
				queue.add(list);
			}
		}
		
		void addFromQueue() throws TopologyException{
			LinkedList<HasCoordinate> next = queue.poll();
			if(next.size() < 2) {
				throw new TopologyException("Not enough arc coordinates", 
						next.size() > 0 ? next.getFirst() : null);
			}
			HasCoordinate c1 = next.get(next.size()-2);
			HasCoordinate c2 = next.getLast();
			
			if(lastEdge != null) {
				ring.removeLast();
			}
			
			//make a temporary edge
			lastEdge = map.getTopoFactory().createTemporaryEdge(c1, c2);
			
			//check both
			nextFromQ1 = !queue.isEmpty() ? (Node)queue.peek().getFirst() : null;
			nextFromQ2 = !queue.isEmpty() ? (Node)queue.peek().getLast() : null;
			ring.addAll(next);
		}
		
		void next() throws TopologyException {
			if(isFinished()) {
				return;
			} else if(lastEdge.getEndNode().equals(nextFromQ1) || lastEdge.getEndNode().equals(nextFromQ2)) {
				addFromQueue();
				return;
			}
			
			NodeStar star = map.getNodeStar(lastEdge.getEndNode(), parcel);
			if(!star.contains(lastEdge))
				star.add(lastEdge);
			star.rotate(lastEdge);
			if(goLeft)
				lastEdge = star.left();
			else
				lastEdge = star.right();
			
			//check for loops
			if(visited.contains(lastEdge))
				throw new TopologyException("No ring found", lastEdge.getEnvelope());
			visited.add(lastEdge);
			
			ring.add(lastEdge.getEndNode());
		}
		
		LinearRing findNow() throws TopologyException {
			while(!isFinished()) {
				next();
			}
			if(containsParcelCentroid())
				return getLinearRing();
			return null;
		}
		
		boolean isFinished() {
			if (lastEdge == null) {
				return false;
			}
			return lastEdge.getEndNode().equalsPosition(firstNode);
		}
		
		private LinearRing linearRing = null;
		LinearRing getLinearRing() {
			if(!isFinished())
				return null;
			if(linearRing == null) {
				double[] dcoords = new double[ring.size()*2];
				int i=0;
				for(HasCoordinate c : ring) {
					dcoords[i++] = c.x();
					dcoords[i++] = c.y();
				}
				
				linearRing = new LinearRing(dcoords);
			}
			return linearRing;
		}
		
		boolean containsParcelCentroid() {
			if (!isFinished())
				return false;
			return GeomUtil.isPointInRing(parcel.getCentroid(), getLinearRing());
		}
		
//		double parcelCentroidDistanceSq() {
//			if(lastEdge == null)
//				return Double.MAX_VALUE;
//			return GeomUtil.distanceSq(parcel.getCentroid(), lastEdge.getEndNode());
//		}
//		
//		boolean hasSameRing(RingWalker other) {
//			if (ring.size() != other.ring.size()) {
//				return false;
//			}
//			
//			for (int i=0; i<ring.size(); i++) {
//				if (!ring.get(i).equals(other.ring.get(i))) {
//					return false;
//				}
//			}
//			
//			return true;
//		}
		
	}

}
