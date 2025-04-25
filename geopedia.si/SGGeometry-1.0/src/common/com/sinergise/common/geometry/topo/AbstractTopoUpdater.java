package com.sinergise.common.geometry.topo;


import static com.sinergise.common.util.math.MathUtil.roundToNearestMultiple;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.sinergise.common.geometry.index.JtsQuadtree;
import com.sinergise.common.geometry.index.PointQuadtree;
import com.sinergise.common.geometry.topo.op.TopoUpdater;
import com.sinergise.common.geometry.util.GeomUtil;
import com.sinergise.common.util.collections.SearchItemReceiver;
import com.sinergise.common.util.geom.Envelope;
import com.sinergise.common.util.geom.HasCoordinate;
import com.sinergise.common.util.geom.HasCoordinateMutable;
import com.sinergise.common.util.geom.Position2D;

public abstract class AbstractTopoUpdater implements TopoUpdater, ITopoMap {
	protected JtsQuadtree<Edge> edges;
	protected PointQuadtree<Node> nodes;
	protected Set<Face> faces;
	
	private double gridSize = 0;
	
	public AbstractTopoUpdater() {
		super();
	}
	
	public void setGridSize(double gridSize) {
		this.gridSize = Math.max(gridSize, 0);
	}
	
	public double getGridSize() {
		return gridSize;
	}

	@Override
	public void checkLock(TopoElement el) throws TopologyException {
		if (el.isLocked()) {
			throw new TopoLockException(el, getElementEnvelope(el));
		}
	}
	
	protected void reset() {
		edges = new JtsQuadtree<Edge>();
		nodes = new PointQuadtree<Node>();
		faces = new HashSet<Face>();
	}
	
	public Envelope getElementEnvelope(TopoElement el) {
		if (el instanceof Node) {
			return new Envelope(((Node)el).x(), ((Node)el).y(), ((Node)el).x(), ((Node)el).y());
			
		} else if (el instanceof Edge) {
			return ((Edge)el).getEnvelope();
			
		} else if (el instanceof Face) {
			return getFaceEnvelope((Face)el);
			
		} else {
			return null;
		}
	}

	protected abstract Envelope getFaceEnvelope(Face el);
	
	@Override
	public boolean addFace(Face f) throws TopologyException {
		return faces.add(f);
	}

	@Override
	public boolean deleteFace(Face f) throws TopologyException {
		return faces.remove(f);
	}

	@Override
	public void addEdge(Edge e) throws TopologyException {
		if (e.getStartNode() == e.getEndNode()) {
			throw new TopologyException("Cannot add degenerate edge");
		}
		edges.insert(e.getEnvelope(), e);
	}

	@Override
	public boolean deleteEdge(Edge e) throws TopologyException {
		return edges.remove(e.getEnvelope(), e);
	}

	@Override
	public void addNode(Node n) throws TopologyException {
		if (shouldSnap()) {
			snapNodeToGrid(n);
		}
		nodes.add(n);
	}

	@Override
	public boolean deleteNode(Node n) throws TopologyException {
		return nodes.remove(n);
	}

	@Override
	public void moveNode(Node n, HasCoordinate p) throws TopologyException {
		List<Edge> lst = edges.query(Envelope.forPoint(n));
		for (Iterator<Edge> it = lst.iterator(); it.hasNext();) {
			Edge e = it.next();
			if (e.hasNode(n)) {
				edges.remove(e.getEnvelope(), e);
			} else {
				it.remove();
			}
		}
		nodes.remove(n);
		n.setLocation(shouldSnap() ? snapToGrid(p) : p);
		nodes.add(n);
		for (Edge e : lst) {
			edges.insert(e.getEnvelope(), e);
		}
	}

	@Override
	public void moveCentroid(Face f, HasCoordinate c) throws TopologyException {
		((HasCoordinateMutable)f.getCentroid()).setLocation(c);
	}

	@Override
	public void changeEdgeNode(Edge e, Node n, boolean startNode) throws TopologyException {
		edges.remove(e.getEnvelope(), e);
		try {
			if (e.getNode(!startNode) == n) {
				throw new TopologyException("Operation would cause degenerate edge");
			}
			e.setNode(n, startNode);
		} finally {
			edges.insert(e.getEnvelope(), e);
		}
	}

	@Override
	public void changeEdgeFace(Edge e, Face f, boolean leftFace) throws TopologyException {
		e.setFace(f, leftFace);
	}

	@Override
	public void changeAllEdgeFaces(Face from, Face to) throws TopologyException {
		for (Edge e : edges.query(Envelope.getInfinite())) {
			if (e.isLeftFace(from)) {
				changeEdgeFace(e, to, true);
			}
			if (e.isRightFace(from)) {
				changeEdgeFace(e, to, false);
			}
		}
	}

	@Override
	public Collection<Face> getFaces() {
		return faces;
	}

	@Override
	public Collection<Edge> getEdges() {
		return edges.query(Envelope.getInfinite());
	}
	
	public Edge getNearestEdge(HasCoordinate worldPos, double minDistSq, Collection<Edge> excluded) {
		double minDist = Math.sqrt(minDistSq);
		NearestEdgeCollector collector = new NearestEdgeCollector(worldPos, minDistSq, excluded);
		edges.query(Envelope.withCenter(worldPos.x(), worldPos.y(), 2*minDist, 2*minDist), collector);
		return collector.getResult();
	}

	@Override
	public Collection<Node> getNodes() {
		return nodes;
	}

	/**
	 * @param withinDistSq search limit squared
	 */
	public Node getNearestNode(HasCoordinate pos, double withinDistSq, Set<? extends Node> excluded) {
		return nodes.findNearest(pos, withinDistSq, excluded);
	}
	
	public Node getLeftmostNode(Set<? super Node> excluded) {
		return nodes.findLeftmost(excluded);
	}

	public boolean containsNode(Node n) {
		return nodes.contains(n);
	}

	public void addNodes(Collection<? extends Node> toAdd) {
		if (shouldSnap()) {
			for (Node n : toAdd) {
				snapNodeToGrid(n);
			}
		}
		nodes.addAll(toAdd);
	}
	
	private HasCoordinate snapToGrid(HasCoordinate coord) {
		return new Position2D(snapToGrid(coord.x()), snapToGrid(coord.y()));
	}
	
	private boolean shouldSnap() {
		return gridSize != 0;
	}

	private double snapToGrid(double ord) {
		assert shouldSnap();
		return roundToNearestMultiple(ord, gridSize);
	}
	
	private void snapNodeToGrid(Node n) {
		n.setLocation(snapToGrid(n));
	}
	
	private static final class NearestEdgeCollector extends SearchItemReceiver.MinFinder<Edge> {
		private final Node searchNode;
		private final double posX;
		private final double posY;
		private final Collection<Edge> excluded;
		
		public NearestEdgeCollector(HasCoordinate pos, double maxDistSq, Collection<Edge> excluded) {
			super(maxDistSq);
			this.posX = pos.x();
			this.posY = pos.y();
			this.excluded = excluded;
			if (pos instanceof Node) {
				this.searchNode = (Node)pos;
			} else {
				this.searchNode = null;
			}
		}
		
		@Override
		protected double calculate(Edge item) {
			if (searchNode != null && item.hasNode(searchNode)) {
				return Double.POSITIVE_INFINITY;
			}
			if (excluded.contains(item)) {
				return Double.POSITIVE_INFINITY;
			}
			return GeomUtil.distancePointLineSegmentSq(posX, posY, item.x1(), item.y1(), item.x2(), item.y2());
		}
	}
}