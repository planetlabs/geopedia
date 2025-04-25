/**
 * 
 */
package com.sinergise.common.geometry.topo.action;

import java.util.ArrayList;
import java.util.List;

import com.sinergise.common.geometry.topo.TopoMap;
import com.sinergise.common.geometry.topo.TopologyException;
import com.sinergise.common.geometry.topo.op.OpEmpty;
import com.sinergise.common.geometry.topo.op.TopoOperation;


/**
 * Not serializable, prepared operations should be sent over the wire instead.
 * 
 * @author tcerovski
 */
public abstract class TopoAction {

	protected final TopoActionParams params;
	
	private List<TopoOperation> ops = null;
	private boolean isPrepared = false;
	
	protected TopoAction() {
		this(null);
	}
	
	protected TopoAction(TopoActionParams params) {
		this.params = params;
	}
	
	public void prepareAction(TopoMap map) throws TopologyException {
		ops = new ArrayList<TopoOperation>();
		beforePrepare(map);
		doPrepareAction(map);
		afterPrepare(map);
		isPrepared = true;
	}
	
	private void beforePrepare(TopoMap map) {
		addOp(map.getTopoOpFactory().createBeforeActionOp(params));
	}
	
	protected abstract void doPrepareAction(TopoMap map) throws TopologyException;
	
	private void afterPrepare(TopoMap map) {
		addOp(map.getTopoOpFactory().createAfterActionOp(params, ops));
	}
	
	protected void addOp(TopoOperation op) {
		if (op instanceof OpEmpty) {
			return;
		}
		ops.add(op);
	}
	
	public List<TopoOperation> getOps() {
		if (!isPrepared) {
			throw new IllegalStateException("Action not prepared.");
		}
		return ops;
	}
}
