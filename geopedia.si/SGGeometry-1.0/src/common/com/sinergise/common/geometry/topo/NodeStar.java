/**
 * 
 */
package com.sinergise.common.geometry.topo;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import com.sinergise.common.util.collections.CyclicList;
import com.sinergise.common.util.geom.Envelope;
import com.sinergise.common.util.geom.EnvelopeBuilder;
import com.sinergise.common.util.geom.HasCoordinate;
import com.sinergise.common.util.geom.HasEnvelope;


/**
 * CyclicList of directed edges ordered by edge bearings.
 * Every modification resorts the edges in clockwise order.
 * All edges are directed from the <tt>node</tt> out.
 * That is <tt>node</tt> is startNode to all edges.
 * 
 * @author tcerovski
 */
public class NodeStar extends CyclicList<Edge> implements HasCoordinate, HasEnvelope {
	private static final Comparator<Edge> BEARING_COMPARATOR = Edge.bearingComparator(); 
	
	private static final long serialVersionUID = -3208583182909226378L;
	
	private final Node node;
	
	public NodeStar(Node node) {
		this(node, Collections.<Edge>emptyList());
	}
	
	public NodeStar(Node node, Collection<? extends Edge> c) {
		super(Math.max(2, c.size()));
		this.node = node;
		addAll(c);
	}
	
	public NodeStar(NodeStar original) {
		this(original.node, original);
	}
	
	@Override
	public double x() {
		return node.x();
	}
	
	@Override
	public double y() {
		return node.y();
	}
	
	/**
	 * @return Node of this star.
	 */
	public Node getNode() {
		return node;
	}
	
	protected void sort() {
		Collections.sort(this, BEARING_COMPARATOR);
	}
	
	private void sort(Edge selected) {
		sort();
		if (selected != null) {
			rotate(selected);
		}
	}

	/* (non-Javadoc)
	 * @see com.sinergise.common.common.util.collections.CyclicList#add(java.lang.Object)
	 */
	@Override
	public boolean add(Edge o) {
		DirectedEdge dir = o.getDirectedEdge(node);
		int idx = Collections.binarySearch(this, dir, BEARING_COMPARATOR);
		if (idx < 0) {
			idx = -(idx+1);
		}
		super.add(idx, dir);
		//no need to rotate, pos will be maintaned by CyclicList's add
		return true;
	}
	
	/* (non-Javadoc)
	 * @see java.util.ArrayList#add(int, java.lang.Object)
	 */
	@Override
	public void add(int index, Edge element) {
		add(element);
	}
	
	/* (non-Javadoc)
	 * @see com.sinergise.common.common.util.collections.CyclicList#addAll(java.util.Collection)
	 */
	@Override
	public boolean addAll(Collection<? extends Edge> c) {
		if (c.isEmpty()) {
			return false;
		}
		ensureCapacity(size() + c.size());
		Edge sel = selected();
		for (Edge e : c) {
			super.add(e.getDirectedEdge(node));
		}
		sort(sel);
		return true;
	}
	
	/* (non-Javadoc)
	 * @see com.sinergise.common.common.util.collections.CyclicList#addAllToTail(java.util.Collection)
	 */
	@Override
	public boolean addAllToTail(Collection<? extends Edge> c) {
		return addAll(c);
	}
	
	/* (non-Javadoc)
	 * @see java.util.ArrayList#addAll(int, java.util.Collection)
	 */
	@Override
	public boolean addAll(int index, Collection<? extends Edge> c) {
		return addAll(c);
	}
	
	/* (non-Javadoc)
	 * @see com.sinergise.common.common.util.collections.CyclicList#addToTail(java.lang.Object)
	 */
	@Override
	public boolean addToTail(Edge o) {
		return add(o);
	}
	
	/* (non-Javadoc)
	 * @see com.sinergise.common.common.util.collections.CyclicList#remove(int)
	 */
	@Override
	public DirectedEdge remove(int index) {
		DirectedEdge sel = selected();
		DirectedEdge ret = (DirectedEdge)super.remove(index);
		if (!ret.equals(sel)) {
			rotate(sel);
		}
		return ret;
	}
	
	@Override
	public DirectedEdge getFirst() {
		return (DirectedEdge)super.getFirst();
	}
	
	@Override
	public DirectedEdge getLast() {
		return (DirectedEdge)super.getLast();
	}
	
	@Override
	public DirectedEdge afterNext() {
		return (DirectedEdge)super.afterNext();
	}
	
	@Override
	public DirectedEdge get(int index) {
		return (DirectedEdge)super.get(index);
	}
	
	/* (non-Javadoc)
	 * @see java.util.ArrayList#remove(java.lang.Object)
	 */
	@Override
	public boolean remove(Object o) {
		Edge sel = selected();
		if (!super.remove(o)) {
			return false;
		}
		if (!o.equals(sel)) {
			rotate(sel);
		}
		return true;
	}
	
	/* (non-Javadoc)
	 * @see java.util.AbstractCollection#removeAll(java.util.Collection)
	 */
	@Override
	public boolean removeAll(Collection<?> c) {
		Edge sel = selected();
		if (!super.removeAll(c)) {
			return false;
		}
		if (contains(sel)) {
			rotate(sel);
		}
		return true;
	}
	
	/* (non-Javadoc)
	 * @see java.util.AbstractCollection#retainAll(java.util.Collection)
	 */
	@Override
	public boolean retainAll(Collection<?> c) {		
		Edge sel = selected();
		if (!super.retainAll(c)) {
			return false;
		}
		if (contains(sel)) {
			rotate(sel);
		}
		return true;
	}
	
	/**
	 * Rotates the star to the specified <tt>edge</tt>.
	 * Previous edge will be left of the specified edge and 
	 * after next edge will be to its right.
	 * @param edge
	 */
	public NodeStar rotate(Edge edge) {
		positionAt(edge);
		return this;
	}
	
	/**
	 * @return Edge on current position.
	 */
	public DirectedEdge selected() {
		if (isEmpty()) {
			return null;
		}
		return peekNext();
	}
	
	@Override
	public DirectedEdge peekNext() {
		return (DirectedEdge)super.peekNext();
	}
	
	/**
	 * @return Edge left of the current position. 
	 */
	public DirectedEdge left() {
		return peekPrevious();
	}
	
	@Override
	public DirectedEdge peekPrevious() {
		return (DirectedEdge)super.peekPrevious();
	}
	
	/**
	 * @return Edge right of the current position. 
	 */
	public DirectedEdge right() {
		return peekAfterNext();
	}
	
	@Override
	public DirectedEdge peekAfterNext() {
		return (DirectedEdge)super.peekAfterNext();
	}
	
	/**
	 * Returns edge left of the current position 
	 * and rotates the star one left.
	 * @return Edge left of the current position. 
	 */
	public DirectedEdge goLeft() {
		return previous();
	}
	
	@Override
	public DirectedEdge previous() {
		return (DirectedEdge)super.previous();
	}
	
	/**
	 * Returns edge right of the current position
	 * and rotates the star one right.
	 * @return Edge right of the current position. 
	 */
	public DirectedEdge goRight() {
		DirectedEdge ret = peekAfterNext();
		next();
		return ret;
	}
	
	/**
	 * @return Faces adjoining the star node.
	 */
	public Collection<Face> getNodeFaces() {
		Set<Face> faces = new HashSet<Face>();
		for(Edge e : this) {
			faces.add(e.getLeftFace());
			faces.add(e.getRightFace());
		}
		return faces;
	}
	
	@Override
	public String toString() {
		StringBuilder ret = new StringBuilder("NodeStar[");
		int i=0;
		for(Edge e : this) {
			if (i++>0) ret.append(",");
			else ret.append(e.getStartNode().toString()+": ");
			ret.append(e.getEndNode().toString());
		}
		ret.append("]");
		return ret.toString();
	}
	
	@Override
	public Envelope getEnvelope() {
		return EnvelopeBuilder.createUnionForEnvelopes(this);
	}

	public boolean hasEdgeTo(Node n) {
		return getEdgeTo(n) != null;
	}
	
	public Edge getEdgeTo(Node n) {
		for (Edge e : this) {
			if (e.getEndNode().equals(n)) {
				return e;
			}
		}
		return null;
	}
	
}
