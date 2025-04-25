/**
 * 
 */
package com.sinergise.common.geometry.topo.op;

import java.util.List;

import com.sinergise.common.geometry.topo.TopologyException;


/**
 * @author tcerovski
 */
public final class OpComposite extends TopoOperation {
	
	private static final long serialVersionUID = -5508086667271802328L;
	
	private final TopoOperation[] ops;
	
	public OpComposite(TopoOperation... ops) {
		this.ops = new TopoOperation[ops.length];
		for (int i = 0; i < ops.length; i++) {
			this.ops[i] = ops[i];
		}
	}
	
	public OpComposite(List<TopoOperation> ops) {
		this.ops = new TopoOperation[ops.size()];
		for (int i = 0; i < ops.size(); i++) {
			this.ops[i] = ops.get(i);
		}
	}

	@Override
	public void execute(TopoUpdater topo) throws TopologyException {
		for(TopoOperation op : ops) {
			op.execute(topo);
		}
	}
	
	@Override
	protected TopoOperation createUndoOperation() {
		TopoOperation[] undo = new TopoOperation[ops.length];
		for(int i=0; i<ops.length; i++) {
			undo[ops.length-i-1] = ops[i].getUndoOperation();
		}
		return new OpComposite(undo);
	}

}
