/**
 * 
 */
package com.sinergise.common.geometry.topo.action;

import com.sinergise.common.geometry.topo.Node;
import com.sinergise.common.geometry.topo.TopoMap;
import com.sinergise.common.geometry.topo.TopologyException;
import com.sinergise.common.geometry.topo.op.TopoOpFactory;
import com.sinergise.common.util.geom.HasCoordinate;


/**
 * <p>Moves node to a new location.</p>
 *
 * Required locks:
 * <ul>
 * 	<li>Node</li>
 * </ul>
 * Additional checks:
 * <ul>
 * 	<li>none</li>
 * </ul>
 * 
 * @author tcerovski
 */
public class MoveNodeAction extends TopoAction {
	
	private final Node n;
	private final HasCoordinate c;
	
	public MoveNodeAction(Node n, HasCoordinate c) {
		this(n, c, null);
	}
	
	public MoveNodeAction(Node n, HasCoordinate c, TopoActionParams params) {
		super(params);
		this.n = n;
		this.c = c;
	}

	@Override
	public void doPrepareAction(TopoMap map) throws TopologyException {
		TopoOpFactory opFact = map.getTopoOpFactory();
		addOp(opFact.createCheckLockOp(n, params));
		addOp(opFact.createMoveNodeOp(n, c, params));
	}

}
