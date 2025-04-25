/**
 * 
 */
package com.sinergise.common.geometry.topo.op;

import com.sinergise.common.geometry.topo.Face;
import com.sinergise.common.geometry.topo.TopologyException;

/**
 * @author tcerovski
 */
public class OpDeleteFace extends TopoOperation {

	private static final long serialVersionUID = 1L;
	
	private final Face f;
	
	public OpDeleteFace(Face f) {
		this.f = f;
	}

	@Override
	public void execute(TopoUpdater topo) throws TopologyException {
		topo.deleteFace(f);
	}

	@Override
	public TopoOperation createUndoOperation() {
		return new OpAddFace(f);
	}

}
