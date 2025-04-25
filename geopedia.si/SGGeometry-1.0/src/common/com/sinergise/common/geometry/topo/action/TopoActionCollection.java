/**
 * 
 */
package com.sinergise.common.geometry.topo.action;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.sinergise.common.geometry.topo.TopoMap;
import com.sinergise.common.geometry.topo.TopologyException;
import com.sinergise.common.geometry.topo.op.TopoOperation;

/**
 * @author tcerovski
 */
public class TopoActionCollection extends TopoAction {

	private final List<TopoAction> actions;
	
	public TopoActionCollection(Collection<? extends TopoAction> actions) {
		this(actions, null);
	}
	
	public TopoActionCollection(Collection<? extends TopoAction> actions, TopoActionParams params) {
		super(params);
		this.actions = new ArrayList<TopoAction>(actions);
	}
	
	@Override
	protected void doPrepareAction(TopoMap map) throws TopologyException {
		int cnt = 0;
		for (TopoAction action : actions) {
			map = map.cloneMap();
			action.prepareAction(map);
			
			//update map for next action
			if (++cnt < actions.size()) {
				for (TopoOperation op : action.getOps()) {
					op.execute(map);
				}
			}
		}
	}
	
	public List<TopoAction> getActions() {
		return actions;
	}
	
	@Override
	public List<TopoOperation> getOps() {
		List<TopoOperation> joinedOps = new ArrayList<TopoOperation>();
		for (TopoAction action : actions) {
			joinedOps.addAll(action.getOps());
		}
		return joinedOps;
	}
	
}
