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
 * <p>Creates a new node at specified location and reconnects the
 * edges enclosing the new node to it while maintaining valid topology, i.e.:.</p>
 * <ul>
 * 	<li>If the original node had only two adjacent edges, it will be deleted.</li>
 * 	<li>Otherwise a new edge will be created between the two nodes if the node broken
 * was not a single node where hole was touching the outer ring - in that case
 * the new edge faces would be the same</li>
 * </ul>
 * <p>This action can also be used to reconnect two edges to a new or hidden existing node 
 * or to disconnect a hole from its outer ring when they touch in a single node.</p>
 * 
 * Required locks:
 * <ul>
 * 	<li>Node to break</li>
 *  <li>Both enclosing edges</li>
 *  <li>Second node if reconnecting to an existing (or hidden) node</li>
 * </ul>
 * Additional checks:
 * <ul>
 * 	<li>none</li>
 * </ul>
 * 
 * @author tcerovski
 */
public class BreakNodeAction extends TopoAction {
	
	private final Node n;
	private final HasCoordinate p;
	
	public BreakNodeAction(Node n, HasCoordinate p) {
		this(n, p, null);
	}
	
	public BreakNodeAction(Node n, HasCoordinate p, TopoActionParams params) {
		super(params);
		this.n = n;
		this.p = p;
	}

	@Override
	public void doPrepareAction(TopoMap map) throws TopologyException {
		TopoOpFactory opFact = map.getTopoOpFactory();
		
		addOp(opFact.createCheckLockOp(n, params));
		
		NodeStar star = map.getNodeStar(n);
		int edgesCnt = star.size();
		//add temp edge to the star to find edges to reconnect
		Edge tempE = map.getTopoFactory().createTemporaryEdge(n, p);
		star.add(tempE);
		
		//rotate to find left and right 
		star.rotate(tempE);
		Edge eR = star.left();
		Edge eL = star.right();
		
		Node newNode = map.getTopoFactory().createNode(p);
		addOp(opFact.createAddNodeOp(newNode, params, map));
		
		//check lock if reconnecting to existing node
		addOp(opFact.createCheckLockOp(newNode, params));
		
		//now we can create proper new edge
		Edge newEdge = map.getTopoFactory().createEdge(n, newNode, eR.getLeftFace(), eL.getRightFace());
		
		//check locks and reconnect
		addOp(opFact.createCheckLockOp(eR, params));
		addOp(opFact.createCheckLockOp(eL, params));
		//change nodes on undirected edge
		addOp(opFact.createChangeEdgeNodeOp(eR.getEdge(), newNode, eR.getEdge().isStartNode(n), params));
		addOp(opFact.createChangeEdgeNodeOp(eL.getEdge(), newNode, eL.getEdge().isStartNode(n), params));
		
		//if breaking an island, both faces will be the same
		if(!newEdge.getLeftFace().equals(newEdge.getRightFace())) {
			addOp(opFact.createAddEdgeOp(newEdge, params, map));
		}
		
		//delete original node if it has no more edges (had two edges at the start)
		if(edgesCnt == 2) {
			addOp(opFact.createDeleteNodeOp(n, params));
		}

	}

}
