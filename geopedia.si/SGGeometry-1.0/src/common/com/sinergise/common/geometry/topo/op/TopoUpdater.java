/**
 * 
 */
package com.sinergise.common.geometry.topo.op;

import com.sinergise.common.geometry.topo.Edge;
import com.sinergise.common.geometry.topo.Face;
import com.sinergise.common.geometry.topo.Node;
import com.sinergise.common.geometry.topo.TopoElement;
import com.sinergise.common.geometry.topo.TopoValidationException;
import com.sinergise.common.geometry.topo.TopologyException;
import com.sinergise.common.util.geom.HasCoordinate;

/**
 * @author tcerovski
 */
public interface TopoUpdater {
	
	public void checkLock(TopoElement el) throws TopologyException;
	
	public boolean addFace(Face f) throws TopologyException;
	
	public boolean deleteFace(Face f) throws TopologyException;

	public void addEdge(Edge e) throws TopologyException;
	
	public boolean deleteEdge(Edge e) throws TopologyException;
	
	public void addNode(Node n) throws TopologyException;
	
	public boolean deleteNode(Node n) throws TopologyException;
	
	public void moveNode(Node n, HasCoordinate p) throws TopologyException;
	
	public void moveCentroid(Face f, HasCoordinate c) throws TopologyException;
	
	public void changeEdgeNode(Edge e, Node n, boolean startNode) throws TopologyException;
	
	public void changeEdgeFace(Edge e, Face f, boolean leftFace) throws TopologyException;
	
	public void changeAllEdgeFaces(Face from, Face to) throws TopologyException;
	
	public void validate(int flags) throws TopoValidationException;
}
