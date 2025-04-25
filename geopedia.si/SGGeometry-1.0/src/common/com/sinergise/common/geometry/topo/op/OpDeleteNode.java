/**
 * 
 */
package com.sinergise.common.geometry.topo.op;

import com.sinergise.common.geometry.topo.Node;
import com.sinergise.common.geometry.topo.TopologyException;

/**
 * @author tcerovski
 */
public final class OpDeleteNode extends TopoOperation {

	private static final long serialVersionUID = 1L;
	
	private final Node n;
	
	public OpDeleteNode(Node n) {
		this.n = n;
	}
	
	@Override
	public void execute(TopoUpdater topo) throws TopologyException {
		topo.deleteNode(n);
	}
	
	@Override
	protected TopoOperation createUndoOperation() {
		return new OpAddNode(n);
	}
	
}
