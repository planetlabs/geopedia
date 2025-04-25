package com.sinergise.common.geometry.topo;

import java.util.Collection;
import java.util.Set;

import com.sinergise.common.geometry.geom.Geometry;
import com.sinergise.common.geometry.util.GeomUtil;
import com.sinergise.common.util.Util;
import com.sinergise.common.util.geom.Envelope;
import com.sinergise.common.util.geom.HasCoordinate;
import com.sinergise.common.util.geom.Position2D;

public class TopoEditorModel {
	
	public interface TopoEditorListener {
		void topoReset();

		void edgeAdded(Edge e);

		/**
		 * One or both of the edge's nodes have been replaced
		 */
		void edgeChanged(Edge e);

		void edgeRemoved(Edge e);

		void nodeAdded(Node n);

		void nodeLocationChanged(Node node);

		void nodeRemoved(Node n);
	}

	protected static class NoOpPeer implements TopoEditorListener {
		@Override
		public void topoReset() {}

		@Override
		public void edgeAdded(Edge e) {}

		@Override
		public void edgeChanged(Edge e) {}

		@Override
		public void edgeRemoved(Edge e) {}

		@Override
		public void nodeAdded(Node n) {}

		@Override
		public void nodeLocationChanged(Node node) {}

		@Override
		public void nodeRemoved(Node n) {}
	}

	protected LiveTopoMap topoMap;
	protected TopoEditorListener peer = new NoOpPeer();

	public TopoEditorModel() {
		this(new LiveTopoMap());
	}
	
	public TopoEditorModel(LiveTopoMap topoMap) {
		this.topoMap = topoMap;
	}
	
	public void setGridSize(double gridSize) {
		topoMap.setGridSize(gridSize);
	}
	
	public double getGridSize() {
		return topoMap.getGridSize();
	}
	
	public Geometry buildGeometry() throws TopologyException {
		return buildGeometry(false, false);
	}
	
	public Geometry buildGeometry(boolean allowLines, boolean allowPoints) throws TopologyException {
		return new GeometryBuilder(topoMap).buildGeometry(allowLines, allowPoints);
	}

	public void setPeer(TopoEditorListener peer) {
		this.peer = peer;
	}

	public void setTopology(Collection<? extends Node> nodes, Collection<? extends Edge> edgeTopo) throws TopologyException {
		peer.topoReset();
		topoMap.setTopology(nodes, edgeTopo);
		for (Node n : topoMap.getNodes()) {
			peer.nodeAdded(n);
		}

		for (Edge e : topoMap.getEdges()) {
			peer.edgeAdded(e);
		}
	}

	public void setNodeLocation(Node node, HasCoordinate position) throws TopologyException {
		intMoveNode(node, position);
	}

	public Node getNearestNode(HasCoordinate worldPos, double minDistSq, Set<? extends Node> excluded) {
		return topoMap.getNearestNode(worldPos, minDistSq, excluded);
	}

	public Node splitEdge(Edge e) throws TopologyException {
		Node newStart = topoMap.getTopoFactory().createNode(new Position2D(0.5 * (e.x1() + e.x2()), 0.5 * (e.y1() + e.y2())));
		Node oldStart = e.getStartNode();
		if (e.isLocked()) {
			newStart.constrainToDiagonal();
		}
		Edge newEdge = topoMap.getTopoFactory().createEdgeFrom(oldStart, newStart, e, null);

		intAddNode(newStart);
		intChangeEdgeNode(e, newStart, true);
		intAddEdge(newEdge);
		
		return newStart;
	}

	public boolean deleteNode(Node n) throws TopologyException {
		NodeStar star = topoMap.getNodeStar(n);
		if (star == null) {
			return false;
		}
		int cntAll = star.size();
		if (cntAll > 2) {
			return false;
		}
		if (cntAll == 2) {
			DirectedEdge e1 = star.next().getDirectedEdge(n);
			DirectedEdge e2 = star.next().getDirectedEdge(n);
			Node other = e1.getEndNode();

			intDeleteEdge(e1.getEdge());
			intDeleteEdge(e2.getEdge());
			if (!topoMap.containsEdgeBetween(other, e2.getEndNode())) {
				e2.setStartNode(other);
				intAddEdge(e2.getEdge());
			}
		} else if (cntAll == 1) {
			DirectedEdge e1 = star.next().getDirectedEdge(n);
			deleteEdge(e1.getEdge());
		}
		intDeleteNode(n);
		return true;
	}

	public Node getNextNode(Node n, Face f, boolean clockwise) {
		NodeStar star = topoMap.getNodeStar(n);
		for (int i = 0; i < star.size(); i++) {
			Edge e = star.next().getDirectedEdge(n);
			if (Util.safeEquals(e.getRightFace(), f) && clockwise) return e.getEndNode();
			if (Util.safeEquals(e.getLeftFace(), f) && !clockwise) return e.getEndNode();
		}
		return null;
	}

	public boolean deleteEdge(Edge e) throws TopologyException {
		if (intDeleteEdge(e)) {
			deleteNodeIfEmptyStar(e.getStartNode());
			deleteNodeIfEmptyStar(e.getEndNode());
			return true;
		}
		return false;
	}
	
	public Node mergeNodes(Node source, Node target) throws TopologyException {
		if (shouldReverseForMerging(source, target)) {
			Node tmp = target;
			target = source;
			source = tmp;
		}
		
		NodeStar star = topoMap.getNodeStar(source);
		for (Edge e : star.toArray(new Edge[star.size()])) {
			if (!topoMap.edges.contains(e.getEnvelope(), e)) {
				continue;
			}
			if (target.equals(e.getEndNode())) {
				deleteEdge(e);
			} else if (topoMap.containsEdgeBetween(target, e.getEndNode())) {
				mergeEdges(e, topoMap.getEdgeBetween(target, e.getEndNode()));
			} else {
				intChangeEdgeNode(e, target, true);
			}
		}
		//delete source node if not yet deleted by edge merging
		if (topoMap.containsNode(source)) {
			intDeleteNode(source);
		}
		return target;
	}
	
	private void mergeEdges(Edge source, Edge target) throws TopologyException {
		if (shouldReverseForMerging(source, target)) {
			Edge tmp = target;
			target = source;
			source = tmp;
		}
		Node n = source.findCommonNode(target);
		
		if (n == null) {
			throw new TopologyException("Cannot merge edges without a common node");
		}
		
		DirectedEdge dSrc = source.getDirectedEdge(n);
		DirectedEdge dTgt = target.getDirectedEdge(n);
		
		boolean mergeOnLeft = (dTgt.getLeftFace() == null && dSrc.getRightFace() == null) 
			|| (dTgt.getLeftFace() != null && dTgt.getLeftFace().equals(dSrc.getRightFace()));
		intChangeEdgeFace(dTgt, dSrc.getFace(mergeOnLeft), mergeOnLeft);

		deleteEdge(source);
		//remove result edge from map if same face on both sides
		if (!target.isLocked() && target.getLeftFace() != null && target.getLeftFace().equals(target.getRightFace())) {
			deleteEdge(target);
		}
	}

	protected boolean shouldReverseForMerging(TopoElement source, TopoElement target) {
		//When given a choice, we should keep the locked edge
		return source.isLocked() && !target.isLocked();
	}

	public Node createNode(HasCoordinate loc) throws TopologyException {
		Node ret = topoMap.getTopoFactory().createNode(loc);
		intAddNode(ret);
		return ret;		
	}

	public int unlockedEdgeCount(Node n) {
		int cnt = 0;
		for (Edge e : topoMap.getNodeStar(n)) {
			if (!e.isLocked()) {
				cnt++;
			}
		}
		return cnt;
	}
	
	public int edgeCount(Node n) {
		return topoMap.getNodeStar(n).size();
	}

	public Edge createEdge(Node n1, Node n2) throws TopologyException {
		if (topoMap.containsEdgeBetween(n1, n2)) return null;
		Edge ret = topoMap.getTopoFactory().createEdge(n1, n2, null, null);
		intAddEdge(ret);
		return ret;
	}
	
	public Edge createNodeAndEdge(Node n1, HasCoordinate loc) throws TopologyException {
		if (loc instanceof Node) {
			throw new IllegalArgumentException("loc argument must not be an existing topology node!");
		}
		Node n2 = createNode(loc);
		return createEdge(n1, n2);
	}

	public ITopoMap getTopoMap() {
		return topoMap;
	}

	public void reset() {
		topoMap.reset();
	}

	public Envelope getEnvelope() {
		return topoMap.getMBR();
	}
	
	public boolean isEmpty() {
		return topoMap.getEdges().isEmpty() && topoMap.getNodes().isEmpty();
	}

	public void addTopology(ITopoMap added) throws TopologyException {
		TopoBuilder bld = new TopoBuilder(added);
		bld.addTopology(topoMap);
		setTopology(bld.getTopologyNodes(), bld.getTopologyEdges());
	}
	
	
	private void intAddNode(Node n) throws TopologyException {
		topoMap.addNode(n);
		peer.nodeAdded(n);
	}
	
	private void intAddEdge(Edge e) throws TopologyException {
		topoMap.addEdge(e);
		peer.edgeAdded(e);
	}
	
	private void intDeleteNode(Node n) throws TopologyException {
		if (topoMap.deleteNode(n)) {
			peer.nodeRemoved(n);
		}
	}
	
	private boolean intDeleteEdge(Edge e) throws TopologyException {
		if (topoMap.deleteEdge(e)) {
			peer.edgeRemoved(e);
			return true;
		}
		return false;
	}
	
	private void deleteNodeIfEmptyStar(Node node) throws TopologyException {
		if (topoMap.getNodeStar(node) != null && topoMap.getNodeStar(node).isEmpty()) {
			intDeleteNode(node);
		}
	}
	
	private void intMoveNode(Node n, HasCoordinate pos) throws TopologyException {
		if (n.isConstrainedToDiagonal()) {
			NodeStar ns = topoMap.getNodeStar(n);
			Node n1 = null;
			Node n2 = null;
			for (Edge edge : ns) {
				if (edge.isLocked()) {
					if (n1 == null) {
						n1 = edge.getOtherNode(n);
					} else {
						n2 = edge.getOtherNode(n);
						break;
					}
				}
			}
			if (n1 != null && n2 != null) {
				Position2D newPos = new Position2D();
				GeomUtil.pointLineStringNearest(n1.x(), n1.y(), n2.x(), n2.y(), pos.x(), pos.y(), newPos);
				pos = newPos;
			}
		}
		topoMap.moveNode(n, pos);
		peer.nodeLocationChanged(n);
	}
	
	private void intChangeEdgeNode(Edge e, Node n, boolean startNode) throws TopologyException {
		topoMap.changeEdgeNode(e, n, startNode);
		peer.edgeChanged(e);
	}
	
	private void intChangeEdgeFace(Edge e, Face f, boolean leftFace) throws TopologyException {
		topoMap.changeEdgeFace(e, f, leftFace);
		peer.edgeChanged(e);
	}

	public Edge getNearestEdge(HasCoordinate worldPos, double minDistSq, Collection<Edge> excluded) {
		return topoMap.getNearestEdge(worldPos, minDistSq, excluded);
	}
}
