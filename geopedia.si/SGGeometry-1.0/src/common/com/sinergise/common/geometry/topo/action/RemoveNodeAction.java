/**
 * 
 */
package com.sinergise.common.geometry.topo.action;

import com.sinergise.common.geometry.topo.Edge;
import com.sinergise.common.geometry.topo.Node;
import com.sinergise.common.geometry.topo.NodeStar;
import com.sinergise.common.geometry.topo.TopoMap;
import com.sinergise.common.geometry.topo.TopologyException;
import com.sinergise.common.geometry.topo.op.TopoOpFactory;


/**
 * <p>Removes a node, deletes the adjacent edges and creates a new one from
 * the left most edge, inheriting its properties.</p>
 * 
 * Required locks:
 * <ul>
 * 	<li>Node</li>
 *  <li>Both edges</li>
 * </ul>
 * Additional checks:
 * <ul>
 * 	<li>Node has exactly two edges</li>
 * </ul>
 * 
 * 
 * @author tcerovski
 */
public class RemoveNodeAction extends TopoAction {

	private final Node n;
	
	public RemoveNodeAction(Node n) {
		this(n, null);
	}
	
	public RemoveNodeAction(Node n, TopoActionParams params) {
		super(params);
		this.n = n;
	}
	
	@Override
	public void doPrepareAction(TopoMap map) throws TopologyException {
		NodeStar edges = map.getNodeStar(n);
		if(edges.size() > 2)
			throw new TopologyException("Node can be removed only when connected to two edges", edges.getEnvelope());
		
		Edge e1 = edges.get(0);
		Edge e2 = edges.get(1);
		//use directed edge
		Edge e = map.getTopoFactory().createEdgeFrom(e1.getEndNode(), e2.getEndNode(), e2, e1);
		
		TopoOpFactory opFact = map.getTopoOpFactory();
		
		addOp(opFact.createCheckLockOp(e1, params));
		addOp(opFact.createCheckLockOp(e2, params));
		addOp(opFact.createCheckLockOp(n, params));
		
		addOp(opFact.createDeleteEdgeOp(e1, params));
		addOp(opFact.createDeleteEdgeOp(e2, params));
		addOp(opFact.createDeleteNodeOp(n, params));
		addOp(opFact.createAddEdgeOp(e, params, map));
	}

}
