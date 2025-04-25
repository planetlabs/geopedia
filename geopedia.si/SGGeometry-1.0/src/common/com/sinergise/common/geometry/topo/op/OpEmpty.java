/**
 * 
 */
package com.sinergise.common.geometry.topo.op;

import com.sinergise.common.geometry.topo.TopologyException;


/**
 * @author tcerovski
 */
public final class OpEmpty extends TopoOperation {

	private static final long serialVersionUID = -7546316866148220627L;

	@Override
	protected TopoOperation createUndoOperation() {
		return new OpEmpty();
	}

	@Override
	public void execute(TopoUpdater topo) throws TopologyException {
		//do nothing
	}

}
