/**
 * 
 */
package com.sinergise.common.geometry.topo;

import static com.sinergise.common.geometry.topo.Face.OUTER_FACE_ID;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sinergise.common.geometry.geom.Point;
import com.sinergise.common.geometry.topo.op.DefaultTopoOpFactory;
import com.sinergise.common.geometry.topo.op.TopoOpFactory;
import com.sinergise.common.geometry.topo.op.TopoUpdater;
import com.sinergise.common.geometry.util.GeomUtil;
import com.sinergise.common.util.geom.Envelope;
import com.sinergise.common.util.geom.EnvelopeBuilder;
import com.sinergise.common.util.geom.HasCoordinate;
import com.sinergise.common.util.naming.IdentifiableEntitiesMap;
import com.sinergise.common.util.naming.Identifier;


/**
 * @author tcerovski
 */
public class TopoMap implements TopoUpdater, ITopoMap {


	protected Set<Edge> edges;
	protected IdentifiableEntitiesMap<Node> nodesMap;
	protected IdentifiableEntitiesMap<Face> facesMap;
	//additional sets of nodes and faces - map can cointain only one new element
	protected Set<Node> nodes;
	protected Set<Face> faces;
	protected IdentifiableEntitiesMap<Edge> edgesMap;
	protected Map<Node, NodeStar> nodeStars;
	protected Map<Face, List<Edge>> faceEdges;
	protected Map<Face, Node> leftNodes;
	protected Map<Face, List<Node>> faceOuterRings;
	protected Map<Face, List<List<Node>>> faceInnerRings;

	protected final EnvelopeBuilder mbr = new EnvelopeBuilder();

	private TopoFactory topoFactory = new TopoFactory();
	private TopoOpFactory topoOpFactory = new DefaultTopoOpFactory();
	private TopoValidator topoValidator = new TopoValidator();

	
	public TopoMap() {}

	public TopoMap(Collection<? extends Edge> edgeList) {
		this();
		setTopology(edgeList);
	}

	public void setTopoFactory(TopoFactory factory) {
		this.topoFactory = factory;
	}

	public TopoFactory getTopoFactory() {
		return topoFactory;
	}

	public void setTopoOpFactory(TopoOpFactory factory) {
		this.topoOpFactory = factory;
	}

	public TopoOpFactory getTopoOpFactory() {
		return topoOpFactory;
	}

	public void setTopoValidator(TopoValidator validator) {
		this.topoValidator = validator;
	}

	public TopoValidator getTopoValidator() {
		return topoValidator;
	}

	@Override
	public Collection<Node> getNodes() {
		return nodes;
	}

	@Override
	public Collection<Edge> getEdges() {
		return edges;
	}

	@Override
	public Collection<Face> getFaces() {
		return faces;
	}

	public Collection<Edge> getEdgesDeepCopy() {
		return TopoUtil.deepCopyEdges(edges);
	}

	public TopoMap cloneMap() {
		TopoMap clone = new TopoMap(getEdgesDeepCopy());
		clone.setTopoFactory(getTopoFactory());
		clone.setTopoOpFactory(getTopoOpFactory());
		clone.setTopoValidator(getTopoValidator());

		return clone;
	}

	public Node getNodeForId(Identifier id) {
		if (nodesMap == null) return null;
		return nodesMap.get(id);
	}

	public Edge getEdgeForId(Identifier id) {
		if (edgesMap == null) return null;
		return edgesMap.get(id);
	}

	public Face getFaceForId(Identifier id) {
		if (facesMap == null) return null;
		return facesMap.get(id);
	}

	public Node getNodeReference(Node n) {
		if (nodesMap == null || n == null) return null;
		if (!n.hasPermanentId()) {
			return n;
		}
		return nodesMap.get(n.getQualifiedID());
	}

	public Edge getEdgeReference(Edge e) {
		if (edgesMap == null || e == null) return null;
		if (!e.hasPermanentId()) {
			return e;
		}
		return edgesMap.get(e.getQualifiedID());
	}

	public Face getFaceReference(Face f) {
		if (facesMap == null || f == null) return null;
		if (!f.hasPermanentId()) {
			return f;
		}
		return facesMap.get(f.getQualifiedID());
	}

	public TopoElement getReference(TopoElement el) {
		if (el instanceof Node) return getNodeReference((Node)el);
		if (el instanceof Edge) return getEdgeReference((Edge)el);
		if (el instanceof Face) return getFaceReference((Face)el);
		return null;
	}

	@Override
	public Envelope getMBR() {
		return mbr.getEnvelope();
	}

	public void setTopology(Collection<? extends Edge> edgeList) {
		this.edges = new HashSet<Edge>();
		edges.addAll(edgeList);
		rescan();
	}

	protected void rescan() {
		edgesMap = new IdentifiableEntitiesMap<Edge>();
		nodeStars = new HashMap<Node, NodeStar>();
		faceEdges = new HashMap<Face, List<Edge>>();
		leftNodes = new HashMap<Face, Node>();
		faceOuterRings = null;
		faceInnerRings = null;

		nodesMap = new IdentifiableEntitiesMap<Node>();
		facesMap = new IdentifiableEntitiesMap<Face>();
		nodes = new HashSet<Node>();
		faces = new HashSet<Face>();
		mbr.clear();

		if (edges == null) return;

		for (Edge edge : edges) {
			edgesMap.put(edge.getQualifiedID(), edge);
			final Node n1 = edge.getStartNode();
			final Node n2 = edge.getEndNode();
			final Face f1 = edge.getLeftFace();
			final Face f2 = edge.getRightFace();

			_addEdge(edge, n1);
			_addEdge(edge, n2);
			_addEdge(edge, f1);
			_addEdge(edge, f2);

			nodesMap.put(n1);
			nodes.add(n1);
			nodesMap.put(n2);
			nodes.add(n2);

			if (f1 != null) {
				facesMap.put(f1);
				faces.add(f1);
			}
			if (f2 != null) {
				facesMap.put(f2);
				faces.add(f2);
			}
			mbr.expandToInclude(n1);
			mbr.expandToInclude(n2);
		}

	}

	private void _addEdge(Edge edge, Node node) {
		NodeStar star = nodeStars.get(node);
		if (star == null) {
			star = new NodeStar(node);
			nodeStars.put(node, star);
		}
		if (!star.contains(node)) {
			star.add(edge);
		}
	}

	private void _addEdge(Edge edge, Face face) {
		List<Edge> edgesOfTheFace = faceEdges.get(face);
		if (edgesOfTheFace == null) {
			edgesOfTheFace = new ArrayList<Edge>();
			faceEdges.put(face, edgesOfTheFace);
		}

		//update left nodes
		Node leftNode = leftNodes.get(face);
		double leftNodeX = leftNode == null ? Double.MAX_VALUE : leftNode.x();
		if (edge.getStartNode().x() < leftNodeX) {
			leftNode = edge.getStartNode();
			leftNodeX = leftNode.x();
		}
		if (edge.getEndNode().x() < leftNodeX) {
			leftNode = edge.getEndNode();
			leftNodeX = leftNode.x();
		}
		leftNodes.put(face, leftNode);

		edgesOfTheFace.add(edge);
	}

	private void _constructRings() {
		faceOuterRings = new HashMap<Face, List<Node>>(facesMap.size());
		faceInnerRings = new HashMap<Face, List<List<Node>>>();

		for (Face f : getFaces()) {
			List<Edge> bag = new ArrayList<Edge>(getFaceEdges(f));
			if (bag.size() == 0) continue;

			//get outer ring
			Node left = leftNodes.get(f);
			if (!OUTER_FACE_ID.equals(f.getQualifiedID())) { //not exterior face
				faceOuterRings.put(f, _constructFaceRing(f, true, left, bag));
			}


			List<List<Node>> innerRings = new ArrayList<List<Node>>();
			while (!bag.isEmpty()) {
				List<Node> iring = _constructFaceRing(f, false, bag.get(0).getStartNode(), bag);
				if (iring != null) innerRings.add(iring);
			}
			faceInnerRings.put(f, innerRings);

		}

	}

	private List<Node> _constructFaceRing(Face f, boolean outer, Node startNode, List<Edge> bag) {
		if (!getNodeFaces(startNode).contains(f)) return null;

		NodeStar firstStar = getNodeStar(startNode, f);
		firstStar.retainAll(bag);
		List<Edge> toRemove = new ArrayList<Edge>();

		//start node is leftmost node
		Edge edge = firstStar.getLast(); // Go CCW
		toRemove.add(edge);
		Node nextNode = edge.getEndNode();

		List<Node> ring = new ArrayList<Node>();
		ring.add(startNode);
		ring.add(nextNode);

		while (!(nextNode.equals(startNode) && ring.size() > 2)) {

			NodeStar star = getNodeStar(nextNode, f);
			if (star == null || star.isEmpty()) {
				ring = null;
				break;
			}

			star.retainAll(bag);
			if (star.size() <= 1) {
				ring = null;
				break;
			}

			star.rotate(edge);
			edge = outer ? star.right() : star.left();

			if (edge == null) {
				ring = null;
				break;
			}

			toRemove.add(edge);
			ring.add(nextNode = edge.getEndNode());
		}

		bag.removeAll(toRemove);

		if (ring == null || ring.size() < 3) return null;
		return ring;
	}

	public synchronized List<Node> getFaceOuterRing(Face f) {
		if (faceOuterRings == null) {
			_constructRings();
		}
		return faceOuterRings.get(f);
	}

	public synchronized List<List<Node>> getFaceInnerRings(Face f) {
		if (faceInnerRings == null) {
			_constructRings();
		}
		
		return faceInnerRings.get(f);
	}

	public List<List<Node>> getFaceRings(Face f) {
		List<List<Node>> rings = new ArrayList<List<Node>>();
		List<Node> outer = getFaceOuterRing(f);
		List<List<Node>> inners = getFaceInnerRings(f);
		if (outer != null) rings.add(outer);
		if (inners != null) rings.addAll(inners);
		return rings;
	}

	public Collection<Node> getFaceNodes(Face f) {
		Set<Node> fNodes = new HashSet<Node>();
		for (List<Node> ring : getFaceRings(f)) {
			for (Node n : ring) {
				fNodes.add(n);
			}
		}
		return fNodes;
	}

	public Face getFaceAt(HasCoordinate c) {
		NodeStar star = getNodeStar(getNearestEdge(c).getStartNode());
		Edge temp = topoFactory.createTemporaryEdge(star.getNode(), c);
		star.add(temp);
		star.rotate(temp);
		return star.left().getRightFace();
	}

	public Envelope getFaceEnvelope(Face f) {
		List<Edge> fEdges = getFaceEdges(f);
		if (fEdges == null || fEdges.size() == 0) return null;
		
		return EnvelopeBuilder.createUnionForEnvelopes(fEdges);
	}

	@Override
	public void validate(int flags) throws TopoValidationException {
		rescan();
		topoValidator.validate(this, flags);
	}

	@Override
	public void checkLock(TopoElement el) throws TopoLockException {
		if (el.isLocked()) {
			throw new TopoLockException(el, getElementEnvelope(el));
		}
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

	@Override
	public void addEdge(Edge edge) throws TopologyException {
		edge.setDirty(true);
		edges.add(edge);
	}

	@Override
	public boolean deleteEdge(Edge edge) throws TopologyException {
		return edges.remove(edge);
	}

	@Override
	public void moveNode(Node n, HasCoordinate p) throws TopologyException {
		NodeStar starRef = getNodeStar(n);

		//set all edges on the star as dirty
		for (Edge e : starRef) {
			e.setDirty(true);
		}

		//update map reference only
		getNodeReference(n).setLocation(p);
	}

	@Override
	public void moveCentroid(Face f, HasCoordinate c) throws TopologyException {
		Face fRef = getFaceReference(f);
		if (fRef == null) fRef = f;
		fRef.setCentroid(c != null ? new Point(c.x(), c.y()) : null);
	}

	@Override
	public boolean addFace(Face f) throws TopologyException {
		if (!faces.add(f)) {
			return false;
		}
		f.setDirty(true);
		facesMap.put(f);
		return true;
	}

	@Override
	public boolean deleteFace(Face f) throws TopologyException {
		facesMap.remove(f);
		return faces.remove(f);
	}

	@Override
	public void addNode(Node n) throws TopologyException {
		n.setDirty(true);
		nodes.add(n);
		nodesMap.put(n);
	}

	@Override
	public boolean deleteNode(Node n) throws TopologyException {
		nodesMap.remove(n);
		return nodes.remove(n);
	}

	@Override
	public void changeEdgeNode(Edge e, Node n, boolean startNode) throws TopologyException {
		Edge edgeRef = getEdgeReference(e);
		edgeRef.setDirty(true);

		if (nodeStars.containsKey(n)) {
			Node nodeRef = nodeStars.get(n).getNode();
			nodeRef.setDirty(true);
		}

		//update map reference only
		edgeRef.setNode(getNodeReference(n), startNode);
	}

	@Override
	public void changeEdgeFace(Edge e, Face f, boolean leftFace) throws TopologyException {
		Edge edgeRef = getEdgeReference(e);
		if (edgeRef == null) return; //can happen when restoring hidden edges, should probably be handled somewhere else

		edgeRef.setDirty(true);

		//update map reference only
		edgeRef.getDirectedEdge(e.getStartNode()).setFace(getFaceReference(f), leftFace);
	}

	@Override
	public void changeAllEdgeFaces(Face from, Face to) throws TopologyException {
		for (Edge e : getFaceEdges(from)) {
			e.setDirty(true);
			e.setFace(to, e.isLeftFace(from));
		}
	}

	public Edge getNearestEdge(HasCoordinate c) {
		if (edges == null || edges.size() == 0) return null;

		double minDist = Double.POSITIVE_INFINITY;
		Edge nearest = null;
		for (Edge e : edges) {
			double dist = GeomUtil.distancePointLineSegmentSq(c.x(), c.y(), e.x1(), e.y1(), e.x2(), e.y2());
			if (dist < minDist) {
				nearest = e;
				minDist = dist;
			}
		}
		return nearest;
	}

	public Node getNearestNode(HasCoordinate c) {
		return getNearestNode(c, Double.POSITIVE_INFINITY);
	}

	public Node getNearestNode(HasCoordinate c, double minDistSq) {
		if (edges == null || edges.size() == 0) return null;
		final double x = c.x();
		final double y = c.y();

		Node nearest = null;
		for (Node n : getNodes()) {
			final double distSq = GeomUtil.distanceSq(x, y, n.x(), n.y());
			if (distSq < minDistSq) {
				nearest = n;
				minDistSq = distSq;
			}
		}
		return nearest;
	}

	@Override
	public NodeStar getNodeStar(Node n) {
		NodeStar star = nodeStars.get(n);
		if (star == null) return null;
		//return a copy
		return new NodeStar(star.getNode(), star);
	}
	
	@Override
	public NodeStar getNodeStar(Edge e, boolean startNode) {
		NodeStar star = getNodeStar(e.getNode(startNode));
		star.rotate(e);
		return star;
	}

	public NodeStar getNodeStar(Node n, Face f) {
		NodeStar faceStar = new NodeStar(n);
		for (Edge e : nodeStars.get(n)) {
			if (e.hasFace(f)) faceStar.add(e);
		}

		return faceStar;
	}

	public Edge findEdge(Node n1, Node n2) {
		List<Edge> nEdges = getNodeStar(n1);
		if (nEdges == null) return null;
		for (Edge e : nEdges) {
			if (e.getEndNode().equals(n2)) return e;
		}
		return null;
	}

	public List<Edge> getFaceEdges(Face f) {
		if (faceEdges == null) return null;
		return faceEdges.get(f);
	}

	public Collection<Face> getNodeFaces(Node n) {
		return getNodeStar(n).getNodeFaces();
	}

	public List<Edge> getPathOnFace(Face f, Node prevNode, Node from, Node to) {
		List<Edge> path = new ArrayList<Edge>();
		if (from.equals(to)) return path;

		Edge prevEdge = findEdge(prevNode, from).getDirectedEdge(from);
		if (prevEdge == null) return null;

		Node nextNode = from;
		while (!(nextNode.equals(to) && path.size() > 0)) {
			NodeStar nEdges = getNodeStar(nextNode, f);
			if (nEdges == null || nEdges.isEmpty()) return null; //no path found

			nEdges.rotate(prevEdge);
			if (prevEdge.isLeftFace(f)) {
				prevEdge = nEdges.left();
			} else {
				prevEdge = nEdges.right();
			}

			nextNode = prevEdge.getEndNode();
			path.add(prevEdge);

			if (from.equals(nextNode)) return null; //loop - no path
		}
		if (path.size() == 0) return null;
		return path;
	}

	public boolean isPointInFace(Face f, HasCoordinate c) {

		//check if has outer ring - if not it is the exterior boundary
		List<Node> outer = getFaceOuterRing(f);

		if (outer == null) return false;

		//check if inside outer ring
		if (!GeomUtil.isPointInRing(c.x(), c.y(), TopoUtil.nodesToLinearRing(outer))) return false;

		//check if not in hole
		for (List<Node> inner : getFaceInnerRings(f)) {
			if (GeomUtil.isPointInRing(c.x(), c.y(), TopoUtil.nodesToLinearRing(inner))) return false;
		}
		return true;
	}

	public boolean containsNode(Node n) {
		return nodes.contains(n);
	}

	public boolean containsEdge(Edge e) {
		return edges.contains(e);
	}

	public boolean containsFace(Face f) {
		return faces.contains(f);
	}

	public void rebuildEnvelope() {}

}
