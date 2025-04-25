/**
 * 
 */
package com.sinergise.common.geometry.topo.action;

import com.sinergise.common.geometry.topo.Face;
import com.sinergise.common.geometry.topo.TopoMap;
import com.sinergise.common.geometry.topo.TopologyException;
import com.sinergise.common.geometry.topo.op.TopoOpFactory;
import com.sinergise.common.util.geom.HasCoordinate;

/**
 * @author tcerovski
 */
public class MoveCentroidAction extends TopoAction {

	private final Face f;
	private final HasCoordinate c;
	
	public MoveCentroidAction(Face f, HasCoordinate c) {
		this(f, c, null);
	}
	
	public MoveCentroidAction(Face f, HasCoordinate c, TopoActionParams params) {
		super(params);
		this.f = f;
		this.c = c;
	}
	
	@Override
	public void doPrepareAction(TopoMap map) throws TopologyException {
		if(map.getFaceReference(f) != null && !map.isPointInFace(f, c)) {
			throw new TopologyException("New seed location is not on the parcel.", c);
		}
		
		TopoOpFactory opFact = map.getTopoOpFactory();
		addOp(opFact.createCheckLockOp(f, params));
		addOp(opFact.createMoveCentroidOp(f, c, params));
	}

}
