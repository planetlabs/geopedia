/**
 * 
 */
package com.sinergise.common.geometry.topo.op;

import com.sinergise.common.geometry.topo.Edge;
import com.sinergise.common.geometry.topo.TopologyException;

/**
 * @author tcerovski
 */
public final class OpAddEdge extends TopoOperation {
	
	private static final long serialVersionUID = 1L;
	
	private final Edge e;
	
	public OpAddEdge(Edge e) {
		//use undirected edge
		this.e = e.getEdge();
	}

	@Override
	public void execute(TopoUpdater topo) throws TopologyException {
		topo.addEdge(e);
	}

	@Override
	public TopoOperation createUndoOperation() {
		return new OpDeleteEdge(e);
	}

}
