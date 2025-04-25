/**
 * 
 */
package com.sinergise.common.geometry.topo.op;

import java.io.Serializable;

import com.sinergise.common.geometry.topo.TopologyException;


/**
 * @author tcerovski
 */
public abstract class TopoOperation implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private TopoOperation undoOp = null;
	
	public TopoOperation getUndoOperation() {
		if(undoOp == null) {
			undoOp = createUndoOperation();
		}
		return undoOp;
	}
	
	public abstract void execute(TopoUpdater topo) throws TopologyException;
	
	protected abstract TopoOperation createUndoOperation();
	
	
	
	
}
