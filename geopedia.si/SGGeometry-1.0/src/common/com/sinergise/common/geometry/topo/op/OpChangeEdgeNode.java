/**
 * 
 */
package com.sinergise.common.geometry.topo.op;

import com.sinergise.common.geometry.topo.Edge;
import com.sinergise.common.geometry.topo.Node;
import com.sinergise.common.geometry.topo.TopologyException;

/**
 * @author tcerovski
 */
public class OpChangeEdgeNode extends TopoOperation {

	private static final long serialVersionUID = 1L;
	
	private final Edge e;
	private final Node newNode;
	private final Node oldNode;
	private final boolean startNode;
	
	public OpChangeEdgeNode(Edge e, Node n, boolean startNode) {
		this(e, n, e.getNode(startNode), startNode);
	}
	
	private OpChangeEdgeNode(Edge e, Node newNode, Node oldNode, boolean startNode) {
		//use undirected edge
		this.e = e.getEdge();
		this.newNode = newNode;
		this.oldNode = oldNode;
		this.startNode = startNode;
	}
	
	@Override
	public void execute(TopoUpdater topo) throws TopologyException {
		topo.changeEdgeNode(e, newNode, startNode);
	}

	
	@Override
	protected TopoOperation createUndoOperation() {
		return new OpChangeEdgeNode(e, oldNode, newNode, startNode);
	}
	
}
