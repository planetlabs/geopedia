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
import com.sinergise.common.util.geom.Envelope;


/**
 * <p>Merges two edges by deleting their common node.<br>
 * Both edges are deleted by default and a new one is created from the
 * first edge, inheriting its properties.</p>
 * 
 * Required locks:
 * <ul>
 * 	<li>Both edges</li>
 *  <li>Common node</li>
 * </ul>
 * Additional checks:
 * <ul>
 * 	<li>Not merging an edge with itself</li>
 *  <li>Edges have a common node</li>
 * 	<li>Common node has exactly two edges</li>
 * </ul>
 * 
 * @author tcerovski
 */
public class MergeEdgesAction extends TopoAction {
	
	private Edge e1;
	private Edge e2;
	private Edge r;
	
	public MergeEdgesAction(Edge e1, Edge e2) {
		this(e1, e2, null, null);
	}
	
	public MergeEdgesAction(Edge e1, Edge e2, Edge result) {
		this(e1, e2, result, null);
	}
	
	public MergeEdgesAction(Edge e1, Edge e2, Edge result, TopoActionParams params) {
		super(params);
		this.e1 = e1;
		this.e2 = e2;
		this.r = result;
	}

	@Override
	public void doPrepareAction(TopoMap map) throws TopologyException {
		
		Envelope env = e1.getEnvelope().union(e2.getEnvelope());
		
		if(e1.equals(e2)) {
			throw new TopologyException("Cannot merge edge with itself", env);
		}
		
		Node n = null;
		
		if(map.getNodeStar(e1.getStartNode()).contains(e2))
			n = e1.getStartNode();
		else if(map.getNodeStar(e1.getEndNode()).contains(e2))
			n = e1.getEndNode();
		else
			throw new TopologyException("Edges do not have a common node.", env);
		
		
		NodeStar edges = map.getNodeStar(n);
		if(edges.size() > 2)
			throw new TopologyException("Cannot merge edges on node with more than two edges", env);
		
		Node startNode = edges.get(0).getEndNode();
		Node endNode = edges.get(1).getEndNode();
		
		TopoOpFactory opFact = map.getTopoOpFactory();
		
		addOp(opFact.createCheckLockOp(e1, params));
		addOp(opFact.createCheckLockOp(e2, params));
		addOp(opFact.createCheckLockOp(n, params));
		
		if (r == null) {
			addOp(opFact.createDeleteEdgeOp(e1, params));
			addOp(opFact.createDeleteEdgeOp(e2, params));
			
			//use directed edge
			Edge e = map.getTopoFactory().createEdgeFrom(startNode, endNode, e2.getDirectedEdge(n), e1.getDirectedEdge(n));
			
			addOp(opFact.createAddEdgeOp(e, params, map));
		} else if (r.equals(e1)) {
			addOp(opFact.createChangeEdgeNodeOp(e1, e2.getOtherNode(n), e1.isStartNode(n), params));
			addOp(opFact.createDeleteEdgeOp(e2, params));
		} else if (r.equals(e2)) {
			addOp(opFact.createChangeEdgeNodeOp(e2, e1.getOtherNode(n), e2.isStartNode(n), params));
			addOp(opFact.createDeleteEdgeOp(e1, params));
		}
		addOp(opFact.createDeleteNodeOp(n, params));

	}

}
