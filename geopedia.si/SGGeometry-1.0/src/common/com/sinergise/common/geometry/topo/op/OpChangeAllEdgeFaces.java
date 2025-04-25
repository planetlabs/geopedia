/**
 * 
 */
package com.sinergise.common.geometry.topo.op;

import com.sinergise.common.geometry.topo.Face;
import com.sinergise.common.geometry.topo.TopologyException;

/**
 * Changes faces for all edges on source face to target face;
 * 
 * @author tcerovski
 */
public class OpChangeAllEdgeFaces extends TopoOperation {
	
	private static final long serialVersionUID = 1L;

	private final Face sourceFace;
	private final Face targetFace;
	
	public OpChangeAllEdgeFaces(Face sourceFace, Face targetFace) {
		this.sourceFace = sourceFace;
		this.targetFace = targetFace;
	}
	
	@Override
	public void execute(TopoUpdater topo) throws TopologyException {
		topo.changeAllEdgeFaces(sourceFace, targetFace);
	}

	@Override
	protected TopoOperation createUndoOperation() {
		return new OpChangeAllEdgeFaces(targetFace, sourceFace);
	}

	

}
