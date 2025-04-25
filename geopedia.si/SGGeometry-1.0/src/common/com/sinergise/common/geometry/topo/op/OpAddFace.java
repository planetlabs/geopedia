/**
 * 
 */
package com.sinergise.common.geometry.topo.op;

import com.sinergise.common.geometry.topo.Face;
import com.sinergise.common.geometry.topo.TopologyException;

/**
 * @author tcerovski
 */
public class OpAddFace extends TopoOperation {

	private static final long serialVersionUID = 1L;
	
	private final Face f;
	
	public OpAddFace(Face f) {
		this.f = f;
	}

	@Override
	public void execute(TopoUpdater topo) throws TopologyException {
		topo.addFace(f);
	}

	@Override
	public TopoOperation createUndoOperation() {
		return new OpDeleteFace(f);
	}

}
