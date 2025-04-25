package com.sinergise.common.geometry.topo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import com.sinergise.common.util.geom.Envelope;
import com.sinergise.common.util.geom.EnvelopeBuilder;
import com.sinergise.common.util.geom.HasCoordinate;

/**
 * Note: Hanging nodes are valid in this topo map
 * TODO: Provide optimized copy constructor or clone - indexes can be cloned in O(n) rather than rebuilt in O(n log(n))
 * 
 * @author Miha
 */
public class LiveTopoMap extends AbstractTopoUpdater {
	
	HashMap<Node, NodeStar> nodeStars;
	HashMap<Face, ArrayList<Edge>> faceEdges;
	private TopoFactory topoFactory = new TopoFactory();
	
	public LiveTopoMap() {
		reset();
	}
	
	public LiveTopoMap(Collection<? extends Edge> topology) throws TopologyException {
		this(null, topology);
	}
	
	public LiveTopoMap(Collection<? extends Node> nodes, Collection<? extends Edge> edgeTopo) throws TopologyException {
		setTopology(nodes, edgeTopo);
	}
	
	@Override
	public Envelope getMBR() {
		return nodes.getEnvelope();
	}
	
	@Override
	public void reset() {
		super.reset();
		nodeStars = new HashMap<Node, NodeStar>();
		faceEdges = new HashMap<Face, ArrayList<Edge>>();
	}
	
	@Override
	public void addNode(Node n) throws TopologyException {
		super.addNode(n);
		nodeStars.put(n, new NodeStar(n));
	}
	
	@Override
	public void addNodes(Collection<? extends Node> toAdd) {
		super.addNodes(toAdd);
		for (Node n : toAdd) {
			nodeStars.put(n, new NodeStar(n));
		}
	}
	
	@Override
	public void addEdge(Edge e) throws TopologyException {
		if (!nodeStars.containsKey(e.getStartNode())) {
			addNode(e.getStartNode());
		}
		if (!nodeStars.containsKey(e.getEndNode())) {
			addNode(e.getEndNode());
		}
		super.addEdge(e);
		getNodeStar(e.getStartNode()).add(e);
		getNodeStar(e.getEndNode()).add(e);
		
		addFaceEdge(e, e.getLeftFace());
		addFaceEdge(e, e.getRightFace());
	}	
	
	public void removeFaceEdge(Edge e, Face fRemove) {
		if (fRemove != null) {
			ArrayList<Edge> list = faceEdges.get(fRemove);
			if (list != null) {
				list.remove(e);
			}
		}
	}

	public void addFaceEdge(Edge e, Face fAdd) throws TopologyException {
		if (fAdd != null) {
			addFace(fAdd);
			faceEdges.get(fAdd).add(e);
		}
	}
	
	@Override
	public boolean addFace(Face f) throws TopologyException {
		if (super.addFace(f)) {
			faceEdges.put(f, new ArrayList<Edge>());
			return true;
		}
		return false;
	}
	
	@Override
	public void changeEdgeFace(Edge e, Face f, boolean leftFace) throws TopologyException {
		removeFaceEdge(e, e.getFace(leftFace));
		super.changeEdgeFace(e, f, leftFace);
		addFaceEdge(e, f);
	}
	
	@Override
	public void changeEdgeNode(Edge e, Node n, boolean startNode) throws TopologyException {
		getNodeStar(e.getNode(startNode)).remove(e);
		super.changeEdgeNode(e, n, startNode);
		getNodeStar(n).add(e);
	}

	@Override
	public boolean deleteNode(Node n) throws TopologyException {
		NodeStar star = getNodeStar(n);
		if (star != null && star.size()>0) {
			throw new TopologyException("Cannot delete node with edges attached");
		}
		nodeStars.remove(n);
		return super.deleteNode(n);
	}
	
	@Override
	public boolean deleteFace(Face f) throws TopologyException {
		if (super.deleteFace(f)) {
			for (Edge e : edges.query(Envelope.getInfinite())) {
				if (e.hasFace(f)) {
					changeEdgeFace(e, null, e.isLeftFace(f));
				}
			}
			return true;
		}
		return false;
	}
	
	@Override
	public boolean deleteEdge(Edge e) throws TopologyException {
		if (super.deleteEdge(e)) {
			getNodeStar(e.getStartNode()).remove(e);
			getNodeStar(e.getEndNode()).remove(e);
			removeFaceEdge(e, e.getLeftFace());
			removeFaceEdge(e, e.getRightFace());
			return true;
		}
		return false;
	}
	
	@Override
	public NodeStar getNodeStar(Node n) {
		return nodeStars.get(n);
	}

	@Override
	protected Envelope getFaceEnvelope(Face f) {
		List<Edge> fEdges = getFaceEdges(f);
		if (fEdges == null || fEdges.size() == 0) return null;
		
		return EnvelopeBuilder.createUnionForEnvelopes(fEdges);
	}
	
	public List<Edge> getFaceEdges(Face f) {
		return faceEdges.get(f);
	}

	@Override
	public void validate(int flags) throws TopoValidationException {
		throw new UnsupportedOperationException();
	}

	public HashMap<Node, NodeStar> getNodeStars() {
		return nodeStars;
	}
	public Node getSingleEdgeNode(Set<Node> excluded) {
		for (NodeStar ns : nodeStars.values()) {
			if (excluded != null && excluded.contains(ns.getNode())) {
				continue;
			}
			if (ns.size() == 1) {
				return ns.getNode();
			}
		}
		return null;
	}
	@Override
	public void moveNode(Node n, HasCoordinate p) throws TopologyException {
		super.moveNode(n, p);
		nodeStars.get(n).sort();
	}

	public TopoFactory getTopoFactory() {
		return topoFactory;
	}

	public void setTopology(Collection<? extends Edge> edgeTopo) throws TopologyException {
		setTopology(null, edgeTopo);
	}

	public void setTopology(Collection<? extends Node> nodes, Collection<? extends Edge> edgeTopo) throws TopologyException {
		reset();
		if (nodes != null) {
			addNodes(nodes);
		}
		if (edgeTopo != null) {
			for (Edge e : edgeTopo) {
				addEdge(e);
			}
		}
	}
	
	public void setTopology(ITopoMap topo) throws TopologyException {
		setTopology(topo.getNodes(), topo.getEdges());
	}
	
	public boolean containsEdgeBetween(Node n1, Node n2) {
		NodeStar ns1 = getNodeStar(n1);
		NodeStar ns2 = getNodeStar(n2);
		return ns1.hasEdgeTo(n2) || ns2.hasEdgeTo(n1);
	}
	
	public Edge getEdgeBetween(Node n1, Node n2) {
		Edge e = getNodeStar(n1).getEdgeTo(n2);
		if (e != null) {
			return e;
		}
		return getNodeStar(n2).getEdgeTo(n1);
	}
	
	@Override
	public NodeStar getNodeStar(Edge e, boolean startNode) {
		NodeStar star = getNodeStar(e.getNode(startNode));
		star.rotate(e);
		return star;
	}
}
