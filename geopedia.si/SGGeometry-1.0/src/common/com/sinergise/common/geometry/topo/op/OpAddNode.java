package com.sinergise.common.geometry.topo.op;

import com.sinergise.common.geometry.topo.Node;
import com.sinergise.common.geometry.topo.TopologyException;

/**
 * @author tcerovski
 */
public final class OpAddNode extends TopoOperation {
	
	private static final long serialVersionUID = 1L;
	
	private final Node n;
	
	public OpAddNode(Node n) {
		this.n = n;
	}

	@Override
	public void execute(TopoUpdater topo) throws TopologyException {
		topo.addNode(n);
	}
	
	@Override
	protected TopoOperation createUndoOperation() {
		return new OpDeleteNode(n);
	}

}
