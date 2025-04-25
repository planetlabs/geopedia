/**
 * 
 */
package com.sinergise.common.geometry.topo.action;

import com.sinergise.common.geometry.topo.Edge;
import com.sinergise.common.geometry.topo.Node;
import com.sinergise.common.geometry.topo.TopoFactory;
import com.sinergise.common.geometry.topo.TopoMap;
import com.sinergise.common.geometry.topo.TopologyException;
import com.sinergise.common.geometry.topo.op.TopoOpFactory;
import com.sinergise.common.util.geom.HasCoordinate;


/**
 * <p>Splits an edge by creating a new node at provided location.<br>
 * By default old edge is deleted and two new edges are created, inheriting its properties.</p>
 * 
 * Required locks:
 * <ul>
 * 	<li>Node</li>
 * 	<li>Edge</li>
 * </ul>
 * Additional checks:
 * <ul>
 * 	<li>none</li>
 * </ul>
 * 
 * @author tcerovski
 */
public class SplitEdgeAction extends TopoAction {
	
	public static enum NewEdges {
		CREATE_TWO_NEW, 
		KEEP_EXISTING_AS_START, 
		KEEP_EXISTING_AS_END}

	private final Edge e;
	private final HasCoordinate c;
	private final NewEdges m;
	
	public SplitEdgeAction(Edge e, HasCoordinate c) {
		this(e, c, NewEdges.CREATE_TWO_NEW);
	}
	
	public SplitEdgeAction(Edge e, HasCoordinate c, NewEdges mode) {
		this(e, c, mode, null);
	}

	public SplitEdgeAction(Edge e, HasCoordinate c, NewEdges mode, TopoActionParams params) {
		super(params);
		this.e = e;
		this.c = c;
		this.m = mode;
	}
	
	@Override
	public void doPrepareAction(TopoMap map) throws TopologyException {
		TopoFactory topoFact = map.getTopoFactory();
		
		Node n = topoFact.createNode(c);
		Edge e1 = m != NewEdges.KEEP_EXISTING_AS_START 
			? topoFact.createEdgeFrom(e.getStartNode(), n, e, null) : e;
		Edge e2 = m != NewEdges.KEEP_EXISTING_AS_END 
			? topoFact.createEdgeFrom(n, e.getEndNode(), e, null) : e;
		
		TopoOpFactory opFact = map.getTopoOpFactory();
		
		addOp(opFact.createCheckLockOp(e, params));
		addOp(opFact.createCheckLockOp(n, params));

		addOp(opFact.createAddNodeOp(n, params, map));
		
		if(m == NewEdges.CREATE_TWO_NEW) {
			addOp(opFact.createAddEdgeOp(e1, params, map));
			addOp(opFact.createAddEdgeOp(e2, params, map));
			addOp(opFact.createDeleteEdgeOp(e, params));
		} else if(m == NewEdges.KEEP_EXISTING_AS_START){
			addOp(opFact.createChangeEdgeNodeOp(e1, n, false, params));
			addOp(opFact.createAddEdgeOp(e2, params, map));
		} else if(m == NewEdges.KEEP_EXISTING_AS_END){
			addOp(opFact.createAddEdgeOp(e1, params, map));
			addOp(opFact.createChangeEdgeNodeOp(e2, n, true, params));
		}
	}
	
}
