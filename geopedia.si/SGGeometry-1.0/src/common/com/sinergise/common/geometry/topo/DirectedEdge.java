/**
 * 
 */
package com.sinergise.common.geometry.topo;

import java.util.Map;

import com.sinergise.common.util.geom.Envelope;
import com.sinergise.common.util.naming.EntityIdentifier;

/**
 * @author tcerovski
 */
public final class DirectedEdge extends Edge {

	private static final long serialVersionUID = 1L;
	
	private Edge edge;
	private boolean reversed;
	
	@Deprecated /** Serialization only */
	protected DirectedEdge() {  }
	
	public DirectedEdge(Edge edge, boolean reversed) {
		super();
		if (edge == null) {
			throw new IllegalArgumentException("edge is null.");
		}
		setId(edge.getQualifiedID());
		this.edge = edge;
		this.reversed = reversed;
	}
	
	public DirectedEdge getReversed() {
		return new DirectedEdge(this, true);
	}
	
	@Override
	public Envelope getEnvelope() {
		return edge.getEnvelope();
	}
	
	@Override
	public Edge getEdge() {
		return edge;
	}
	
	@Override
	public EntityIdentifier getQualifiedID() {
		return edge.getQualifiedID();
	}
	
	@Override
	public String getLocalID() {
		return edge.getLocalID();
	}
	
	@Override
	public void setLocked(boolean locked) {
		edge.setLocked(locked);
	}
	
	@Override
	public boolean isLocked() {
		return edge.isLocked();
	}
	
	@Override
	public int getSRID() {
		return edge.getSRID();
	}
	
	@Override
	public void setDirty(boolean dirty) {
		edge.setDirty(dirty);
	}
	
	@Override
	public boolean isDirty() {
		return edge.isDirty();
	}
	
	@Override
	public Node getStartNode() {
		if(reversed)
			return edge.getEndNode();
		return edge.getStartNode();
	}
	
	@Override
	public Node getEndNode() {
		if(reversed)
			return edge.getStartNode();
		return edge.getEndNode();
	}
	
	@Override
	public Face getLeftFace() {
		if(reversed)
			return edge.getRightFace();
		return edge.getLeftFace();
	}
	
	@Override
	public Face getRightFace() {
		if(reversed)
			return edge.getLeftFace();
		return edge.getRightFace();
	}
	
	@Override
	public void setStartNode(Node startNode) {
		if(reversed)
			edge.setEndNode(startNode);
		else
			edge.setStartNode(startNode);
	}

	@Override
	public void setEndNode(Node endNode) {
		if(reversed)
			edge.setStartNode(endNode);
		else
			edge.setEndNode(endNode);
	}

	@Override
	public void setLeftFace(Face leftFace) {
		if(reversed)
			edge.setRightFace(leftFace);
		else
			edge.setLeftFace(leftFace);
	}

	@Override
	public void setRightFace(Face rightFace) {
		if(reversed)
			edge.setLeftFace(rightFace);
		else
			edge.setRightFace(rightFace);
	}
	
	@Override
	public DirectedEdge getDirectedEdge(Node startNode) {
		return edge.getDirectedEdge(startNode);
	}
	
	@Override
	public DirectedEdge deepCopy(Map<Node, Node> nodeCopies, Map<Face, Face> faceCopies) {
		return new DirectedEdge(
			edge != null ? edge.deepCopy(nodeCopies, faceCopies) : null, reversed);
	}
	
	@Override
	protected DirectedEdge newInstance() {
		throw new UnsupportedOperationException();
	}
}
