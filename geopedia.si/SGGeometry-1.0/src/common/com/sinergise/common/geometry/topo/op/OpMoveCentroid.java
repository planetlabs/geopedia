/**
 * 
 */
package com.sinergise.common.geometry.topo.op;

import com.sinergise.common.geometry.geom.Point;
import com.sinergise.common.geometry.topo.Face;
import com.sinergise.common.geometry.topo.TopologyException;
import com.sinergise.common.util.geom.HasCoordinate;

/**
 * @author tcerovski
 */
public class OpMoveCentroid extends TopoOperation {

	private static final long serialVersionUID = -930835195157044268L;
	
	private final Face f;
	private final HasCoordinate oldLocation;
	private final HasCoordinate newLocation;
	
	public OpMoveCentroid(Face f, HasCoordinate c) {
		this(f, c, f.getCentroid() != null ? new Point(f.getCentroid()) : null);
	}
	
	private OpMoveCentroid(Face f, HasCoordinate newLocation, HasCoordinate oldLocation) {
		this.f = f;
		this.newLocation = newLocation;
		this.oldLocation = oldLocation;
	}

	@Override
	public void execute(TopoUpdater topo) throws TopologyException {
		topo.moveCentroid(f, newLocation);
	}
	
	@Override
	protected TopoOperation createUndoOperation() {
		return new OpMoveCentroid(f, oldLocation, newLocation);
	}

}
