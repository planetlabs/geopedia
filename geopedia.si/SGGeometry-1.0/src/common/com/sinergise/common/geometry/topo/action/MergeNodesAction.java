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
import com.sinergise.common.util.geom.HasCoordinate;


/**
 * <p>Merges two nodes, by deleting the first node and reconnecting all its edges to the second node.<br>
 * If nodes have a common node, that node is retired.<br> 
 * The second node is moved if the position of the merging point is different than the node.</p>
 * 
 * Required locks:
 * <ul>
 * 	<li>Both nodes</li>
 *  <li>All edges connected to the first node</li>
 * </ul>
 * Additional checks:
 * <ul>
 * 	<li>Not merging a node with itself</li>
 * </ul>
 * 
 * @author tcerovski
 */
public class MergeNodesAction extends TopoAction {
	
	private final Node n1;
	private final Node n2;
	private final HasCoordinate p;
	
	public MergeNodesAction(Node n1, Node n2, HasCoordinate p) {
		this(n1, n2, p, null);
	}
	
	public MergeNodesAction(Node n1, Node n2, HasCoordinate p, TopoActionParams params) {
		super(params);
		this.n1 = n1;
		this.n2 = n2;
		this.p = p;
	}

	@Override
	public void doPrepareAction(TopoMap map) throws TopologyException {
		
		if(n1.equals(n2)) {
			throw new TopologyException("Cannot merge node with itself", n1);
		}
		
		NodeStar star = map.getNodeStar(n1);
		
		TopoOpFactory opFact = map.getTopoOpFactory();
		
		//check node locks
		addOp(opFact.createCheckLockOp(n1, params));
		addOp(opFact.createCheckLockOp(n2, params));
		if(p instanceof Node) {
			addOp(opFact.createCheckLockOp((Node)p, params));
		}
		
		//move second node if necessary
		if(!n2.equalsPosition(p)) {
			addOp(opFact.createMoveNodeOp(n2, p, params));
		}
		
		for(Edge e : star) {
			addOp(opFact.createCheckLockOp(e, params));
			//check if there is a common edge to retire
			if(e.getEndNode().equals(n2)) {
				addOp(opFact.createDeleteEdgeOp(e, params));
			//otherwise reconnect the edges	
			} else {
				//use undirected edge
				addOp(opFact.createChangeEdgeNodeOp(e.getEdge(), n2, e.getEdge().isStartNode(n1), params));
			}
		}
		
		//delete first point
		addOp(opFact.createDeleteNodeOp(n1, params));
	}

}
