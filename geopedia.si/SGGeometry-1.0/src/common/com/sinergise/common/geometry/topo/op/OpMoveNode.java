/**
 * 
 */
package com.sinergise.common.geometry.topo.op;

import com.sinergise.common.geometry.geom.Point;
import com.sinergise.common.geometry.topo.Node;
import com.sinergise.common.geometry.topo.TopologyException;
import com.sinergise.common.util.geom.HasCoordinate;

/**
 * @author tcerovski
 */
public final class OpMoveNode extends TopoOperation {

	private static final long serialVersionUID = 1L;
	
	private final Node n;
	private final HasCoordinate newLocation;
	private final HasCoordinate oldLocation;
	
	public OpMoveNode(Node n, HasCoordinate p) {
		this(n, p, new Point(n.x(), n.y()));
	}
	
	private OpMoveNode(Node n, HasCoordinate newLocation, HasCoordinate oldLocation) {
		this.n = n;
		this.newLocation = newLocation;
		this.oldLocation = oldLocation;
	}
	
	@Override
	public void execute(TopoUpdater topo) throws TopologyException {
		topo.moveNode(n, newLocation);
	}

	@Override
	protected TopoOperation createUndoOperation() {
		return new OpMoveNode(n, oldLocation, newLocation);
	}

}
