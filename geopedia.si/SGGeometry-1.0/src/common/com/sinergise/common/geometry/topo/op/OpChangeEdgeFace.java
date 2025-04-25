/**
 * 
 */
package com.sinergise.common.geometry.topo.op;

import com.sinergise.common.geometry.topo.Edge;
import com.sinergise.common.geometry.topo.Face;
import com.sinergise.common.geometry.topo.TopologyException;

/**
 * @author tcerovski
 */
public class OpChangeEdgeFace extends TopoOperation {
	
	private static final long serialVersionUID = 1L;

	private final Edge e;
	private final Face newFace;
	private final Face oldFace;
	private final boolean leftFace;
	
	public OpChangeEdgeFace(Edge e, Face f, boolean leftFace) {
		this(e, f, e.getFace(leftFace), leftFace);
	}
	
	private OpChangeEdgeFace(Edge e, Face newFace, Face oldFace, boolean leftFace) {
		this.e = e;
		this.newFace = newFace;
		this.oldFace = oldFace;
		this.leftFace = leftFace;
	}
	
	@Override
	public void execute(TopoUpdater topo) throws TopologyException {
		topo.changeEdgeFace(e, newFace, leftFace);
	}

	
	@Override
	protected TopoOperation createUndoOperation() {
		return new OpChangeEdgeFace(e, oldFace, newFace, leftFace);
	}

}
