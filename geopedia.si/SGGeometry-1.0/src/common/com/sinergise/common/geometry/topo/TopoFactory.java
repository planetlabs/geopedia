/**
 * 
 */
package com.sinergise.common.geometry.topo;

import com.sinergise.common.geometry.geom.Point;
import com.sinergise.common.util.geom.HasCoordinate;

/**
 * @author tcerovski
 */
public class TopoFactory {

	@SuppressWarnings("unused")
	public Node createNode(HasCoordinate loc) throws TopologyException {

		if (loc instanceof Node) {
			return (Node)loc;
		} else if (loc instanceof Point) {
			return new Node((Point)loc);
		} else {
			return new Node(new Point(loc.x(), loc.y()));
		}

	}

	@SuppressWarnings("unused")
	public Edge createEdge(Node n1, Node n2, Face f1, Face f2) throws TopologyException {
		return new Edge(n1, n2, f1, f2);
	}

	@SuppressWarnings("unused")
	public Edge createEdgeFrom(Node n1, Node n2, Edge other, Edge other2) throws TopologyException {
		Edge e = new Edge(n1, n2, other.getLeftFace(), other.getRightFace());
		e.setSRID(other.getSRID());
		e.setLocked(other.isLocked());
		return e;
	}

	@SuppressWarnings("unused")
	public Face createFace(HasCoordinate cen) throws TopologyException {
		if (cen==null || (cen instanceof Point)) { return new Face(cen); }
		return new Face(new Point(cen.x(), cen.y()));
	}

	@SuppressWarnings("unused")
	public Face createFaceFrom(Face other) throws TopologyException {
		Face f = new Face(other.getCentroid());
		f.setSRID(other.getSRID());
		f.setLocked(other.isLocked());
		return f;
	}
	
	public Face getOuterFace() {
		return Face.OUTER_FACE;
	}


	public Edge createTemporaryEdge(HasCoordinate c1, HasCoordinate c2) {
		return new Edge(createTemporaryNode(c1), createTemporaryNode(c2), null, null);
	}

	public Node createTemporaryNode(HasCoordinate c) {
		if (c instanceof Node) {
			return (Node)c;
		} else if (c instanceof Point) {
			return new Node((Point)c);
		} else {
			return new Node(new Point(c.x(), c.y()));
		}
	}
}
