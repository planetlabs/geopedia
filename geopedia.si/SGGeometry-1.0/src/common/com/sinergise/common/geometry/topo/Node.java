/**
 * 
 */
package com.sinergise.common.geometry.topo;

import com.sinergise.common.util.geom.Envelope;
import com.sinergise.common.util.geom.HasCoordinate;
import com.sinergise.common.util.geom.HasCoordinateMutable;
import com.sinergise.common.util.geom.Position2D;
import com.sinergise.common.util.naming.EntityIdentifier;
import com.sinergise.common.util.naming.Identifier;

/**
 * Simple topology node.
 * 
 * @see TopoElement
 * 
 * @author tcerovski
 */
public class Node extends TopoElement implements HasCoordinateMutable {
	private static final Identifier TEMP_NODE_PREFIX = new Identifier(Identifier.ROOT, "tempNode");

	private static final long serialVersionUID = 1L;
	
	HasCoordinateMutable point;
	
	private boolean enabledEdgeChangesWhenLocked = true; //TODO: set default to false ?
	private boolean constrainedToDiagonal = false;
	
	@Deprecated
	protected Node() { 
		super();
	}
	
	public Node(double x, double y) {
		this(new Position2D(x, y));
	}
	
	public Node(HasCoordinateMutable point) {
		this(null, point);
	}
	
	public Node(EntityIdentifier id, HasCoordinateMutable point) {
		super(id == null ? new EntityIdentifier(TEMP_NODE_PREFIX) : id);
		this.point = point;
	}
	
	public Envelope createEnvelope(double buffer) {
		return Envelope.withCenter(x(), y(), buffer, buffer);
	}
	
	public boolean isStub() {
		return point == null;
	}
	
	@Override
	public String getName() {
		if (hasPermanentId()) {
			return "Node "+getLocalID();
		}
		return "New node "+getLocalID();
	}
	
	@Override
	public double x() {
		return point.x();
	}
	
	@Override
	public double y() {
		return point.y();
	}
	
	public boolean equalsPosition(HasCoordinate c) {
		return c.x() == x() && c.y() == y();
	}

	@Override
	public Node setLocation(HasCoordinate loc) {
		point.setLocation(loc);
		return this;
	}
	
//	@Override //Don't override - GWT doesn't have clone() on Object
	@SuppressWarnings("all")
	public Node clone() {
		return deepCopy();
	}
	
	@Override
	public Node deepCopy() {
		Node copy = (Node)super.deepCopy();
		
		if (point == null) {
			copy.point = null;
		} else {
			copy.point = point.clone();
		}
		
		return copy;
	}
	
	@Override
	protected Node newInstance() {
		return new Node();
	}
	
	@Override
	public String toString() {
		if (point != null) {
			return point.toString();
		}
		return super.toString();
	}
	
	public boolean isEnabledEdgeChangesWhenLocked() {
		return enabledEdgeChangesWhenLocked;
	}
	
	public boolean isConstrainedToDiagonal() {
		return constrainedToDiagonal;
	}
	
	public void constrainToDiagonal() {
		this.constrainedToDiagonal = true;
	}
}
