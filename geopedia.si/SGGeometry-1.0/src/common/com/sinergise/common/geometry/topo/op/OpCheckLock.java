/**
 * 
 */
package com.sinergise.common.geometry.topo.op;

import com.sinergise.common.geometry.topo.TopoElement;
import com.sinergise.common.geometry.topo.TopologyException;

/**
 * @author tcerovski
 */
public class OpCheckLock extends TopoOperation {
	
	private static final long serialVersionUID = 1L;
	
	private final TopoElement el;
	
	public OpCheckLock(TopoElement el) {
		this.el = el;
	}

	@Override
	protected TopoOperation createUndoOperation() {
		return new OpCheckLock(el);
	}

	@Override
	public void execute(TopoUpdater topo) throws TopologyException {
		topo.checkLock(el);
	}

}
