/**
 * 
 */
package com.sinergise.common.geometry.topo.action;

import java.util.HashSet;
import java.util.Set;

import com.sinergise.common.geometry.topo.Edge;
import com.sinergise.common.geometry.topo.Face;
import com.sinergise.common.geometry.topo.Node;
import com.sinergise.common.geometry.topo.TopoMap;
import com.sinergise.common.geometry.topo.TopologyException;
import com.sinergise.common.geometry.topo.op.TopoOpFactory;


/**
 * <p>Merges two faces by removing their common boundary.<br>
 * Both faces are deleted and a new one is created from the
 * first face, inheriting its properties.<br>
 * All edges on the common boundary are deleted (retired), 
 * common nodes are deleted if they have no more edges.</p>
 * 
 * Required locks:
 * <ul>
 * 	<li>Both faces</li>
 *  <li>Common node</li>
 * </ul>
 * Additional checks:
 * <ul>
 * 	<li>Not merging a face with itself</li>
 *  <li>Faces have a common boundary (at least one common edge)</li>
 * </ul>
 * 
 * @author tcerovski
 */
public class MergeFacesAction extends TopoAction {
	
	private final Face f1;
	private final Face f2;
	private final Face r;
	
	public MergeFacesAction(Face f1, Face f2) {
		this(f1, f2, null, null);
	}
	
	public MergeFacesAction(Face f1, Face f2, Face result) {
		this(f1, f2, result, null);
	}
	
	public MergeFacesAction(Face f1, Face f2, Face result, TopoActionParams params) {
		super(params);
		this.f1 = f1;
		this.f2 = f2;
		this.r = result;
	}
	
	
	@Override
	public void doPrepareAction(TopoMap map) throws TopologyException {
		if(f1.equals(f2)) {
			throw new TopologyException("Cannot merge face with itself", map.getFaceEnvelope(f1));
		}

		TopoOpFactory opFact = map.getTopoOpFactory();
		
		addOp(opFact.createCheckLockOp(f1, params));
		addOp(opFact.createCheckLockOp(f2, params));

		Face result = r;
		if(r == null) {
			//create new face
			result = map.getTopoFactory().createFaceFrom(f1);
		}
		
		if(!result.exists()) {
			addOp(opFact.createAddFaceOp(result, params, map));
		}
		
		if(result.getCentroid() == null) {
			addOp(opFact.createMoveCentroidOp(result, f1.getCentroid(), params));
		}
		
		//get common edges
		boolean isNeighbour = false;
		Set<Node> nodesToRemove = new HashSet<Node>();
		for(Edge e : map.getFaceEdges(f2)) {
			if(e.hasFace(f1)) {
				isNeighbour = true;
				addOp(opFact.createCheckLockOp(e, params));
				addOp(opFact.createDeleteEdgeOp(e, params));
				if(map.getNodeStar(e.getStartNode()).size() == 2) {
					nodesToRemove.add(e.getStartNode());
				}
				if(map.getNodeStar(e.getEndNode()).size() == 2) {
					nodesToRemove.add(e.getEndNode());
				}
			} else if(!f2.equals(result)){
				//use undirected edge
				addOp(opFact.createCheckLockOp(e, params));
				addOp(opFact.createChangeEdgeFaceOp(e.getEdge(), result, e.getEdge().isLeftFace(f2), params));
			}
		}
		//update faces
		for(Edge e : map.getFaceEdges(f1)) {
			if(!e.hasFace(f2) && !f1.equals(result)) { //common faces should already be delted
				//use undirected edge
				addOp(opFact.createCheckLockOp(e, params));
				addOp(opFact.createChangeEdgeFaceOp(e.getEdge(), result, e.getEdge().isLeftFace(f1), params));
			}
		}
		
		//check if neighbours
		if(!isNeighbour) {
			throw new TopologyException("Cannot merge faces with no common edges");
		}
		
		for(Node n : nodesToRemove) {
			addOp(opFact.createCheckLockOp(n, params));
			addOp(opFact.createDeleteNodeOp(n, params));
		}
		
		//remove old faces
		if(!f1.equals(result)) {
			addOp(opFact.createDeleteFaceOp(f1, params));
		}
		if(!f2.equals(result)) {
			addOp(opFact.createDeleteFaceOp(f2, params));
		}

	}

}
