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
 * Topology Operation Factory interface.
 * Behaviour of default topology actions can be changed with 
 * implementing and using specific operations factory.
 * 
 * @author tcerovski
 */
public interface TopoOpFactory {
	
	public TopoOperation createAddEdgeOp(Edge e, TopoActionParams params, TopoMap map);
	
	public TopoOperation createAddFaceOp(Face f, TopoActionParams params, TopoMap map);
	
	public TopoOperation createAddNodeOp(Node n, TopoActionParams params, TopoMap map);
	
	public TopoOperation createChangeEdgeFaceOp(Edge e, Face f, boolean leftFace, TopoActionParams params);
	
	public TopoOperation createChangeEdgeNodeOp(Edge e, Node n, boolean startNode, TopoActionParams params);
	
	public TopoOperation createChangeAllEdgeFacesOp(Face from, Face to, TopoActionParams params);
	
	public TopoOperation createCheckLockOp(TopoElement el, TopoActionParams params);
	
	public TopoOperation createDeleteEdgeOp(Edge e, TopoActionParams params);
	
	public TopoOperation createDeleteFaceOp(Face f, TopoActionParams params);
	
	public TopoOperation createDeleteNodeOp(Node n, TopoActionParams params);
	
	public TopoOperation createMoveNodeOp(Node n, HasCoordinate c, TopoActionParams params);
	
	public TopoOperation createMoveCentroidOp(Face f, HasCoordinate c, TopoActionParams params);
	
	/**
	 * Called before TopoAction.prepareAction(). Can be extended to 
	 * execute specific operations before default operations.
	 */
	public TopoOperation createBeforeActionOp(TopoActionParams params);
	
	/**
	 * Called after TopoAction.prepareAction(). Can be extended to 
	 * execute specific operations after default operations.
	 */
	public TopoOperation createAfterActionOp(TopoActionParams params, List<TopoOperation> prevOps);

}
