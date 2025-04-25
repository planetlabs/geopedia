/**
 * 
 */
package com.sinergise.common.geometry.topo.action;

import java.util.Collection;
import java.util.List;

import com.sinergise.common.geometry.topo.Edge;
import com.sinergise.common.geometry.topo.Face;
import com.sinergise.common.geometry.topo.Node;
import com.sinergise.common.geometry.topo.TopoMap;
import com.sinergise.common.geometry.topo.TopologyException;
import com.sinergise.common.geometry.topo.op.TopoOpFactory;
import com.sinergise.common.util.geom.Envelope;


/**
 * <p>Reconnects an edge to a different nodes.</p>
 * <p>Face must be changed for all the edges on the path from old node to any new node.</p>
 * 
 * Required locks:
 * <ul>
 * 	<li>Edge to reconnect</li>
 *  <li>Old nodes to change</li>
 *  <li>New nodes</li>
 *  <li>Edges on the path between the nodes</li>
 * </ul>
 * Additional checks:
 * <ul>
 *  <li>Any reconnected node must have more than two edges connected</li>
 * 	<li>New nodes must have a common face with their old nodes</li>
 * 	<li>A path exists between old and any new node</li>
 * </ul>
 * 
 * @author tcerovski
 */
public class ReconnectNodeAction extends TopoAction {
	
	private final Edge e;
	private final Node startNode;
	private final Node endNode;
	
	public ReconnectNodeAction(Edge e, Node startNode, Node endNode) {
		this(e, startNode, endNode, null);
	}
	
	public ReconnectNodeAction(Edge e, Node startNode, Node endNode, TopoActionParams params) {
		super(params);
		this.e = e.getEdge(); //use undirected edge
		this.startNode = startNode;
		this.endNode = endNode;
	}

	@Override
	public void doPrepareAction(TopoMap map) throws TopologyException {
		
		Envelope env = e.getEnvelope().expandedFor(startNode.x(), startNode.y()).expandedFor(endNode.x(), endNode.y());
		
		//check if enough edges
		List<Edge> startStar = map.getNodeStar(e.getStartNode());
		List<Edge> endStar = map.getNodeStar(e.getEndNode());
		
		if(!e.hasNode(startNode) && startStar.size() < 3) {
			throw new TopologyException("Cannot disconnect edge from node with two edges only", env);
		}
		if(!e.hasNode(endNode) && endStar.size() < 3) {
			throw new TopologyException("Cannot disconnect edge from node with two edges only", env);
		}
		
		TopoOpFactory opFact = map.getTopoOpFactory();
		
		//check locks on nodes and edge
		addOp(opFact.createCheckLockOp(e, params));
		if(!e.hasNode(startNode)) {
			addOp(opFact.createCheckLockOp(startNode, params));
			//use undirected edge
			addOp(opFact.createChangeEdgeNodeOp(e.getEdge(), startNode,true, params));
		}
		if(!e.hasNode(endNode)) {
			addOp(opFact.createCheckLockOp(endNode, params));
			//use undirected edge
			addOp(opFact.createChangeEdgeNodeOp(e.getEdge(), endNode, false, params));
		}
		
		if(!e.hasNode(startNode)) {
			updateCommonFacePath(map, e.getStartNode(), startNode, env);
		}
		if(!e.hasNode(endNode)) {
			updateCommonFacePath(map, e.getEndNode(), endNode, env);
		}
	}
	
	protected void updateCommonFacePath(TopoMap map, Node oldNode, Node newNode, Envelope env) throws TopologyException {
		TopoOpFactory opFact = map.getTopoOpFactory();
		
		//get common faces
		Collection<Face> faces = map.getNodeFaces(newNode);
		faces.retainAll(e.getFaces());
		if(faces.isEmpty())
			throw new TopologyException("No common face found.", env);
		Face commonFace = faces.iterator().next();
		
		//update faces
		Collection<Edge> commonPath = map.getPathOnFace(commonFace, e.getOtherNode(oldNode), oldNode, newNode);
		if(commonPath == null || commonPath.isEmpty())
			throw new TopologyException("No path between nodes found", env);
		
		Face newFace = e.getOtherFace(commonFace);
		for(Edge edge : commonPath) {
			addOp(opFact.createCheckLockOp(edge, params));
			//use undirected edges
			addOp(opFact.createChangeEdgeFaceOp(edge.getEdge(), newFace, edge.getEdge().isLeftFace(commonFace), params));
		}
	}

}
