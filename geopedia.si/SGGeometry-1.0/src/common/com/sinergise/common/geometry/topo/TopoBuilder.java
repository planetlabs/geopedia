package com.sinergise.common.geometry.topo;

import static com.sinergise.common.geometry.util.GeomUtil.withinDistancePointLineSegment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sinergise.common.geometry.geom.Geometry;
import com.sinergise.common.geometry.geom.GeometryCollection;
import com.sinergise.common.geometry.geom.HasGeometry;
import com.sinergise.common.geometry.geom.LineString;
import com.sinergise.common.geometry.geom.LinearRing;
import com.sinergise.common.geometry.geom.MultiPolygon;
import com.sinergise.common.geometry.geom.Point;
import com.sinergise.common.geometry.geom.Polygon;
import com.sinergise.common.geometry.index.JtsQuadtree;
import com.sinergise.common.geometry.index.PointQuadtree;
import com.sinergise.common.util.collections.SearchItemReceiver;
import com.sinergise.common.util.geom.Position2D;
import com.sinergise.common.util.math.MathUtil;
import com.sinergise.common.util.naming.EntityIdentifier;

public class TopoBuilder {
	
	private static final double ULP1 = 1E-7;
	
	public static final class NodeKey {
		private final double x;
		private final double y;
		private final int hashCode;

		public NodeKey(Node n) {
			this(n.x(), n.y());
		}

		public NodeKey(double x, double y) {
			this.x = x;
			this.y = y;
			this.hashCode = 37 * MathUtil.hashCode(x) + MathUtil.hashCode(y);  
		}
		@Override
		public int hashCode() {
			return hashCode;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj instanceof NodeKey) {
				NodeKey other = (NodeKey)obj;
				return x == other.x && y == other.y;
			}
			return false;
		}
	}
	
	private static final class EdgeKey {
		private final double x1;
		private final double y1;
		private final double x2;
		private final double y2;
		private final int hashCode;

		public EdgeKey(Edge e) {
			this(e.x1(), e.y1(), e.x2(), e.y2());
		}

		public EdgeKey(double x1, double y1, double x2, double y2) {
			if (x1 < x2 || (x1 == x2 && y1 < y2)) {
				this.x1 = x1;
				this.y1 = y1;
				this.x2 = x2;
				this.y2 = y2;
			} else {
				this.x1 = x2;
				this.y1 = y2;
				this.x2 = x1;
				this.y2 = y1;
			}
			hashCode = computeHashCode();
		}
		private int computeHashCode() {
			return 37*(37*(37 * MathUtil.hashCode(x1) + MathUtil.hashCode(y1)) + MathUtil.hashCode(x2)) + MathUtil.hashCode(y2);
		}
		@Override
		public int hashCode() {
			return hashCode;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj instanceof EdgeKey) {
				EdgeKey other = (EdgeKey)obj;
				return x1 == other.x1 && y1 == other.y1 && x2 == other.x2 && y2 == other.y2;
			}
			return false;
		}
	}
	
	public static final Face OUTER = new Face();
	
	private PointQuadtree<Node> nodesIndex = null;
	private final Map<NodeKey, Node> nodes = new HashMap<NodeKey, Node>();
	private JtsQuadtree<Edge> edgesIndex = null;
	private final Map<EdgeKey, Edge> edges = new HashMap<EdgeKey, Edge>();
	
	private final Map<HasGeometry, Face> faceReferences = new HashMap<HasGeometry, Face>();
	
	private double gridSize = ULP1;
	private double minDist = gridSize * (1 - ULP1); //use tolerance when comparing distances due to rounding errors
	
	private TopoCleaner cleaner = new TopoCleaner();
	
	private boolean cleanTopology 	= false;
	private boolean snapToEdge;
	
	public TopoBuilder() {
		this(true);
	}
	
	public TopoBuilder(boolean snapToEdge) {
		this.snapToEdge = snapToEdge;
	}
	
	public TopoBuilder(ITopoMap added) {
		this();
		addTopology(added);
	}

	public void setGridSize(double gridSize) {
		this.gridSize = Math.max(gridSize, ULP1);
		this.minDist = gridSize*(1-ULP1);
		cleaner.setGridSize(gridSize);
	}

	public void setCleanTopology(boolean doClean) {
		this.cleanTopology = doClean;
	}

	public void setSnapToEdge(boolean snapToEdge) {
		this.snapToEdge = snapToEdge;
	}

	public void setTopoCleaner(TopoCleaner cleaner) {
		this.cleaner = cleaner;
		this.cleaner.setGridSize(gridSize);
	}

	public void addTopology(ITopoMap added) {
		for (Node n : added.getNodes()) {
			addNode(n);
		}
		for (Edge e : added.getEdges()) {
			addEdge(e);
		}
	}

	public void addGeometry(HasGeometry hasGeom) {
		Geometry geom = hasGeom.getGeometry();
		if (geom instanceof MultiPolygon || geom instanceof Polygon) {
			addPolygon(hasGeom);
		} else {
			addGeometry(geom);
		}
	}

	public void addGeometry(Geometry geom) {
		if (geom instanceof Point) {
			addPoint((Point)geom);
			return;
		}
		insertGeometry(geom, null);
	}
	
	public void insertGeometry(Geometry geom, Face face) {
		List<Edge> addedEdges = new ArrayList<Edge>();
		insertGeometry(geom, face, addedEdges);
		for (Edge e : addedEdges) {
			addEdge(e.getStartNode(), e.getEndNode(), e.getLeftFace(), e.getRightFace());
		}
	}

	private void insertGeometry(Geometry geom, Face face, List<Edge> edgesOut) {
		if (geom instanceof Point) {
			addPoint((Point)geom);
		} else if (geom instanceof GeometryCollection) {
			for (Geometry part : (GeometryCollection<?>)geom) {
				insertGeometry(part, face, edgesOut);
			}
		} else if (geom instanceof Polygon) {
			addPolygon((Polygon)geom, face, edgesOut);
		} else if (geom instanceof LineString) {
			addLineString((LineString)geom, null, null, edgesOut);
		} else {
			throw new IllegalArgumentException("Unknown geometry type: "+geom.getClass());
		}
	}
	
	private void addLineString(LineString line, Face leftFace, Face rightFace, List<Edge> edgesOut) {
		Node nPrev = null;
		for (int i=0; i < line.getNumCoords(); i++) {
			double x = line.getX(i);
			double y = line.getY(i);
			
			Node n = addNode(x, y);
			if (nPrev != null && !n.equals(nPrev)) {
				edgesOut.add(new Edge(nPrev, n, leftFace, rightFace));
			}
			nPrev = n;
		}
	}

	private void addPoint(Point point) {
		addNode(point.x(), point.y());
	}

	public Face addPolygon(HasGeometry geomHolder) {
		Geometry geom = geomHolder.getGeometry();
		EntityIdentifier faceId = EntityIdentifier.extractEntityIdentifier(geomHolder);
		Face face = new Face(faceId, null);
		if (geom != null) {
			if (geom instanceof Polygon || geom instanceof MultiPolygon) {
				insertGeometry(geom, face);
			} else {
				throw new IllegalArgumentException("Invalid geometry type. Should be either Polygon or Multipolygon.");
			}
		}
		registerFaceReference(geomHolder, face);
		return face;
	}
	
	private void addPolygon(Polygon poly, Face face, List<Edge> edgesOut) {
		//clear self intersections and ensure orientation on self intersection free results

		//cleaning self intersections on the outer ring can result in multiple outer or inner rings.
		ArrayList<LinearRing> candidates = new ArrayList<LinearRing>(cleanRingTopology(poly.getOuter()));
		if (candidates.isEmpty()) {
			return; //no suitable rings
		}
		
		Collections.sort(candidates, EvenOddPolygonizer.RING_COMP_GRT_FIRST);
		
		List<LinearRing> outerRings = new ArrayList<LinearRing>();
		List<LinearRing> innerRings = new ArrayList<LinearRing>();
		outerRings.add(candidates.remove(0));
		for (LinearRing candidate : candidates) {
			boolean inner = false;
			for (LinearRing outer : outerRings) {
				if (EvenOddPolygonizer.isInside(outer, candidate)) {
					inner = true;
					break;
				}
			}
			
			if (inner) {
				innerRings.add(candidate);
			} else {
				outerRings.add(candidate);
			}
		}
		
		//holes will always result in inner rings
		for (int i=0; i<poly.getNumHoles(); i++) {
			innerRings.addAll(cleanRingTopology(poly.getHole(i)));
		}
		
		
		for (LinearRing ring : outerRings) {
			addLineString(ring.ensureCCW(), face, OUTER, edgesOut);
		}
		
		for (LinearRing ring : innerRings) {
			addLineString(ring.ensureCW(), face, OUTER, edgesOut);
		}
		
	}

	private Node addNode(Node other) {
		double x = snapToGrid(other.x());
		double y = snapToGrid(other.y());
		other.setLocation(new Position2D(x, y));
		
		NodeKey nKey = new NodeKey(x, y);
		Node n = nodes.get(nKey);
		if (n == null) {
			addToDataStore(nKey, n = other);
			snapToEdge(n);
		}
		return n;
	}

	private Node addNode(double x, double y) {
		return addNode(new Node(x, y));
	}

	private void addToDataStore(NodeKey nodeKey, Node n) {
		nodes.put(nodeKey, n);
		if (nodesIndex != null) {
			nodesIndex.add(n);
		}
	}

	private Edge addEdge(Node n1, Node n2, Face left, Face right) {
		EdgeKey eKey = new EdgeKey(n1.x(), n1.y(), n2.x(), n2.y());
		Edge e = edges.get(eKey);
		if (e != null) {
			DirectedEdge ret = e.getDirectedEdge(n1);
			setEdgeFaces(ret, left, right);
			return ret;
		}
		addToDataStore(eKey, e = new Edge(n1, n2, left, right));
		snapToNode(e);
		return e.getDirectedEdge(n1);
	}
	
	private void addToDataStore(EdgeKey edgeKey, Edge e) {
		edges.put(edgeKey, e);
		if (edgesIndex != null) {
			edgesIndex.insert(e.getEnvelope(), e);
		}
	}

	private void addEdge(Edge e) {
		addEdge(e.getStartNode(), e.getEndNode(), e.getLeftFace(), e.getRightFace());
	}

	private List<LinearRing> cleanRingTopology(LinearRing ring) {
		if (cleanTopology) {
			return cleaner.cleanRing(ring);
		}
		return Arrays.asList(ring);
	}

	private void snapToEdge(final Node n) {
		if (!snapToEdge) {
			return;
		}
		
		checkEdgesIndex();
		edgesIndex.query(n.createEnvelope(gridSize),  new SearchItemReceiver<Edge>() {
			@Override
			public Boolean execute(Edge e) {
				return Boolean.valueOf(!splitEdgeIfOnNode(e, n));
			}
		});
	}

	private void snapToNode(final Edge e) {
		if (!snapToEdge) {
			return;
		}
		checkNodesIndex();
		nodesIndex.findInEnvelope(e.createEnvelope(gridSize), new SearchItemReceiver<Node>() {
			@Override
			public Boolean execute(Node n) {
				return Boolean.valueOf(!splitEdgeIfOnNode(e, n));
			}
		});
	}

	private boolean splitEdgeIfOnNode(Edge e, Node n) {
		if (e.hasNode(n)) {
			return false;
		}
		
		if (withinDistancePointLineSegment(n.x(), n.y(), e.x1(), e.y1(), e.x2(), e.y2(), minDist)) {		
			//split the edge
			removeFromDataStore(e);
			addEdge(e.getStartNode(), n, e.getLeftFace(), e.getRightFace()).setLocked(e.isLocked());
			addEdge(n, e.getEndNode(), e.getLeftFace(), e.getRightFace()).setLocked(e.isLocked());
			return true;
		}
		return false;
	}

	private void checkNodesIndex() {
		if (nodesIndex == null) {
			nodesIndex = new PointQuadtree<Node>();
			nodesIndex.setAll(nodes.values());
		}
	}
	
	private void checkEdgesIndex() {
		if (edgesIndex == null) {
			edgesIndex = new JtsQuadtree<Edge>();
			for (Edge e : edges.values()) {
				edgesIndex.insert(e.getEnvelope(), e);
			}
		}
	}

	private void removeEdge(Edge e) {
		removeFromDataStore(e);
		
		Node sN = e.getStartNode();
		Node eN = e.getEndNode();
		//remove left over node(s)
		boolean sNFound = false;
		boolean eNFound = false;
		//TODO: use edgesIndex
		for (Edge toCheck : edges.values()) {
			if (!sNFound && toCheck.hasNode(sN)) {
				sNFound = true;
				if (eNFound) {
					break;
				}
			}
			if (!eNFound && toCheck.hasNode(eN)) {
				eNFound = true;
				if (sNFound) {
					break;
				}
			}
		}
		
		if (!sNFound) {
			removeFromDataStore(sN);
		}
		if (!eNFound) {
			removeFromDataStore(eN);
		}
	}

	private void removeFromDataStore(Edge e) {
		edges.remove(new EdgeKey(e));
		if (edgesIndex != null) {
			edgesIndex.remove(e.getEnvelope(), e);
		}
	}

	private void removeFromDataStore(Node n) {
		nodes.remove(new NodeKey(n));
		if (nodesIndex != null) {
			nodesIndex.remove(n);
		}
	}

	private void setEdgeFaces(DirectedEdge e, Face leftFace, Face rightFace) {
		if (leftFace != null && (isNullOrOuter(e.getLeftFace()) || e.getLeftFace().equals(rightFace))) {
			e.setLeftFace(leftFace);
		}
		if (rightFace != null && (isNullOrOuter(e.getRightFace()) || e.getRightFace().equals(leftFace))) {
			e.setRightFace(rightFace);
		}
		
		//remove empty edge (doubling back)
		if (e.getLeftFace() != null && e.getLeftFace().equals(e.getRightFace())) {
			removeEdge(e);
		}
	}

	private void registerFaceReference(HasGeometry geomHolder, Face face) {
		faceReferences.put(geomHolder, face);
	}

	public void lockAll() {
		setLockOnAll(true);
	}

	public void unlockAll() {
		setLockOnAll(false);
	}

	public void lockFaceBoudnary(Face face) {
		setFaceBoundaryLock(face, true);
	}

	public void unlockFaceBoundary(Face face) {
		setFaceBoundaryLock(face, false);
	}

	private void setFaceBoundaryLock(Face face, boolean lock) {
		for (Edge e : edges.values()) {
			if (e.hasFace(face)) {
				e.setLocked(lock);
				e.getStartNode().setLocked(lock);
				e.getEndNode().setLocked(lock);
			}
		}
	}

	private void setLockOnAll(boolean lock) {
		for (Edge e : edges.values()) {
			e.setLocked(lock);
		}
		for (Node n : nodes.values()) {
			n.setLocked(lock);
		}
		for (Face f : faceReferences.values()) {
			f.setLocked(lock);
		}
	}

	public void reset() {
		nodes.clear();
		edges.clear();
		faceReferences.clear();
		nodesIndex = null;
		edgesIndex = null;
	}

	public LiveTopoMap buildTopology() throws TopologyException {
		return new LiveTopoMap(nodes.values(), edges.values());
	}
	
	public Collection<Edge> buildTopologyEdges() {
		return new ArrayList<Edge>(edges.values());
	}
	
	public List<Node> getTopologyNodes() {
		return new ArrayList<Node>(nodes.values());
	}

	public List<Edge> getTopologyEdges() {
		return new ArrayList<Edge>(edges.values());
	}

	public Map<HasGeometry, Face> getFaceReferences() {
		return Collections.unmodifiableMap(faceReferences);
	}

	private double snapToGrid(double ord) {
		return gridSize <= 0 ? ord : MathUtil.roundToNearestMultiple(ord, gridSize);
	}
	
	private static boolean isNullOrOuter(Face face) {
		return face == null || face == OUTER;
	}

	public static ITopoMap build(Geometry geom) throws TopologyException {
		return build(geom, true);
	}

	public static ITopoMap build(Geometry geom, boolean snapToEdge) throws TopologyException {
		TopoBuilder tb = new TopoBuilder(snapToEdge);
		tb.addGeometry(geom);
		return tb.buildTopology();
	}
	
}
