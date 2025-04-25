/**
 * 
 */
package com.sinergise.common.geometry.topo.op;

import java.util.List;

import com.sinergise.common.geometry.topo.Edge;
import com.sinergise.common.geometry.topo.Face;
import com.sinergise.common.geometry.topo.Node;
import com.sinergise.common.geometry.topo.TopoElement;
import com.sinergise.common.geometry.topo.TopoMap;
import com.sinergise.common.geometry.topo.action.TopoActionParams;
import com.sinergise.common.util.geom.HasCoordinate;


/**
 * Default Topology Operations Factory creating default operations. 
 * 
 * @author tcerovski
 */
public class DefaultTopoOpFactory implements TopoOpFactory {

	@Override
	public TopoOperation createAddEdgeOp(Edge e, TopoActionParams params, TopoMap map) {
		if (map.containsEdge(e)) { //should check if already exists in case of composite action
			return new OpEmpty();
		}
		return new OpAddEdge(e);
	}

	@Override
	public TopoOperation createAddFaceOp(Face f, TopoActionParams params, TopoMap map) {
		if (map.containsFace(f)) { //should check if already exists in case of composite action
			return new OpEmpty();
		}
		return new OpAddFace(f);
	}

	@Override
	public TopoOperation createAddNodeOp(Node n, TopoActionParams params, TopoMap map) {
		if (map.containsNode(n)) { //should check if already exists in case of composite action
			return new OpEmpty();
		}
		return new OpAddNode(n);
	}

	@Override
	public TopoOperation createChangeEdgeFaceOp(Edge e, Face f, boolean leftFace, TopoActionParams params) {
		return new OpChangeEdgeFace(e, f, leftFace);
	}

	@Override
	public TopoOperation createChangeEdgeNodeOp(Edge e, Node n, boolean startNode, TopoActionParams params) {
		return new OpChangeEdgeNode(e, n, startNode);
	}
	
	@Override
	public TopoOperation createChangeAllEdgeFacesOp(Face from, Face to, TopoActionParams params) {
		return new OpChangeAllEdgeFaces(from, to);
	}

	@Override
	public TopoOperation createCheckLockOp(TopoElement el, TopoActionParams params) {
		return new OpCheckLock(el);
	}

	@Override
	public TopoOperation createDeleteEdgeOp(Edge e, TopoActionParams params) {
		return new OpDeleteEdge(e);
	}

	@Override
	public TopoOperation createDeleteFaceOp(Face f, TopoActionParams params) {
		return new OpDeleteFace(f);
	}

	@Override
	public TopoOperation createDeleteNodeOp(Node n, TopoActionParams params) {
		return new OpDeleteNode(n);
	}

	@Override
	public TopoOperation createMoveNodeOp(Node n, HasCoordinate c, TopoActionParams params) {
		return new OpMoveNode(n, c);
	}
	
	@Override
	public TopoOperation createMoveCentroidOp(Face f, HasCoordinate c, TopoActionParams params) {
		return new OpMoveCentroid(f, c);
	}
	
	/* (non-Javadoc)
	 * @see com.sinergise.common.geometry.topo.op.TopoOpFactory#createAfterActionOp(com.sinergise.common.geometry.topo.action.TopoActionParams, com.sinergise.common.geometry.topo.op.TopoOperationCollection)
	 */
	@Override
	public TopoOperation createAfterActionOp(TopoActionParams params, List<TopoOperation> prevOps) {
		return new OpEmpty();
	}
	
	/* (non-Javadoc)
	 * @see com.sinergise.common.geometry.topo.op.TopoOpFactory#createBeforeActionOp(com.sinergise.common.geometry.topo.action.TopoActionParams)
	 */
	@Override
	public TopoOperation createBeforeActionOp(TopoActionParams params) {
		return new OpEmpty();
	}
}
