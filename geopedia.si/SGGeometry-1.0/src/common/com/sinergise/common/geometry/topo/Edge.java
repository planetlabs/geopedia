/**
 * 
 */
package com.sinergise.common.geometry.topo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sinergise.common.geometry.util.GeomUtil;
import com.sinergise.common.util.geom.CoordinatePair;
import com.sinergise.common.util.geom.CoordinatePairMutable;
import com.sinergise.common.util.geom.Envelope;
import com.sinergise.common.util.geom.HasCoordinate;
import com.sinergise.common.util.geom.HasEnvelope;
import com.sinergise.common.util.math.AngleUtil;
import com.sinergise.common.util.naming.EntityIdentifier;
import com.sinergise.common.util.naming.Identifier;


/**
 * Simple topology Edge holding references to 
 * its end and start node and left and right face.
 * 
 * @see TopoElement
 * 
 * @author tcerovski
 */
public class Edge extends TopoElement implements CoordinatePairMutable, HasEnvelope {
	private static final Identifier TEMP_EDGE_PREFIX = new Identifier(Identifier.ROOT, "tempEdge");
	
	private static final long serialVersionUID = 1L;

	private Node startNode;
	private Node endNode;
	
	private Face leftFace;
	private Face rightFace;
	
	private transient DirectedEdge directed = null;
	private transient DirectedEdge directedReversed = null;
	
	private boolean allowedAttachNodeWhenLocked = true;//TODO: set default to false ?

	protected Edge() {
		super();
	}
	
	public Edge(Node startNode, Node endNode)
	{
		this(startNode, endNode, null, null);
	}
	
	public Edge(Node startNode, Node endNode, Face leftFace, Face rightFace) 
	{
		this(null, startNode, endNode, leftFace, rightFace);
	}
	
	public Edge(EntityIdentifier id, Node startNode, Node endNode, Face leftFace, Face rightFace) 
	{
		super(id == null ? new EntityIdentifier(TEMP_EDGE_PREFIX) : id);
		this.startNode = startNode;
		this.endNode = endNode;
		this.leftFace = leftFace;
		this.rightFace = rightFace;
	}
	
	public boolean isAllowedAttachNodeWhenLocked() {
		return allowedAttachNodeWhenLocked;
	}
	
	/**
	 * @return Undirected edge.
	 */
	public Edge getEdge() {
		return this;
	}
	
	@Override
	public String getName() {
		if (hasPermanentId()) {
			return "Edge "+getLocalID();
		}
		return "New edge "+getLocalID();
	}
	
	public boolean isStartNode(Node n) {
		return getStartNode() != null && getStartNode().equals(n);
	}
	
	public boolean isEndNode(Node n) {
		return getEndNode() != null && getEndNode().equals(n);
	}
	
	public boolean hasNode(Node n) {
		return isStartNode(n) || isEndNode(n);
	}
	
	public boolean hasCommonNode(Edge other) {
		return hasNode(other.getStartNode()) || hasNode(other.getEndNode());
	}
	
	public Node findCommonNode(Edge other) {
		if (hasNode(other.getStartNode())) {
			return other.getStartNode();
		} else if (hasNode(other.getEndNode())) {
			return other.getEndNode();
		}
		return null;
	}
	
	public Node getNode(boolean getStart) {
		if(getStart)
			return getStartNode();
		return getEndNode();
	}
	
	public Node getOtherNode(Node n) {
		if(!hasNode(n))
			return null;
		if(isStartNode(n))
			return getEndNode();
		return getStartNode();
	}
	
	public boolean isLeftFace(Face f) {
		return getLeftFace() != null && getLeftFace().equals(f);
	}
	
	public boolean isRightFace(Face f) {
		return getRightFace() != null && getRightFace().equals(f);
	}
	
	public boolean hasFace(Face f) {
		return isLeftFace(f) || isRightFace(f);
	}
	
	public Collection<Face> getFaces() {
		List<Face> faces = new ArrayList<Face>();
		faces.add(getLeftFace());
		faces.add(getRightFace());
		return faces;
	}
	
	public Face getOtherFace(Face f) {
		if(!hasFace(f))
			return null;
		if(isLeftFace(f))
			return getRightFace();
		return getLeftFace();
	}
	
	public Face getFace(boolean getLeft) {
		if(getLeft)
			return getLeftFace();
		return getRightFace();
	}
	
	public Node getStartNode() {
		return startNode;
	}
	public Node getEndNode() {
		return endNode;
	}
	
	public Face getLeftFace() {
		return leftFace;
	}
	
	public Face getRightFace() {
		return rightFace;
	}
	
	public void setNode(Node node, boolean startNode) {
		if(startNode)
			setStartNode(node);
		else
			setEndNode(node);
	}
	
	public void setStartNode(Node startNode) {
		this.startNode = startNode;
	}

	@Override
	public Edge setCoordinate1(HasCoordinate c1) {
		setStartNode((Node)c1);
		return this;
	}
	
	public void setEndNode(Node endNode) {
		this.endNode = endNode;
	}
	
	@Override
	public Edge setCoordinate2(HasCoordinate c2) {
		setEndNode((Node)c2);
		return this;
	}
	
	public void setFace(Face face, boolean left) {
		if(left)
			setLeftFace(face);
		else
			setRightFace(face);
	}

	public void setLeftFace(Face leftFace) {
		this.leftFace = leftFace;
	}

	public void setRightFace(Face rightFace) {
		this.rightFace = rightFace;
	}
	
	@Override
	public final double x1() {
		if(getStartNode() == null)
			return Double.NaN;
		return getStartNode().x();
	}
	
	@Override
	public final double y1() {
		if(getStartNode() == null)
			return Double.NaN;
		return getStartNode().y();
	}
	
	@Override
	public final double x2() {
		if(getEndNode() == null)
			return Double.NaN;
		return getEndNode().x();
	}
	
	@Override
	public final double y2() {
		if(getEndNode() == null)
			return Double.NaN;
		return getEndNode().y();
	}
	
	@Override
	public HasCoordinate c1() {
		return getStartNode();
	}
	
	@Override
	public HasCoordinate c2() {
		return getEndNode();
	}
	
	@Override
	public Envelope getEnvelope() {
		return createEnvelope(0);
	}
	
	public Envelope createEnvelope(double buffer) {
		return Envelope.create(startNode, endNode, buffer);
	}

	public final double bearing() {
		return bearing(this);
	}
	
	public final double pseudoBearing() {
		return AngleUtil.pseudoATan2(y2()-y1(), x2()-x1());
	}
	
	public final double length() {
		return GeomUtil.distance(getStartNode(), getEndNode());
	}
	
	public final double lengthSq() {
		return GeomUtil.distanceSq(getStartNode(), getEndNode());
	}
	
	
	@Override
	public String toString() {
		return "Edge [startNode=" + getStartNode() + ", endNode=" + getEndNode()
				+ ", id=" + getQualifiedID() + "]";
	}
	
	public boolean equalsPosition(Edge e) {
		if (getStartNode() == null || getEndNode() == null
			|| e.getStartNode() == null || e.getEndNode() == null) 
		{
			return false;
		}
		return x1() == e.x1()
			&& y1() == e.y1()
			&& x2() == e.x2()
			&& y2() == e.y2();
	}

	public DirectedEdge getDirectedEdge(Node getStart) {
		try {
			if(this.getStartNode().equals(getStart)) {
				if(directed == null)
					directed = new DirectedEdge(this, false);
				return directed;
			} else if(getStart.equals(this.getEndNode())) {
				if(directedReversed == null)
					directedReversed = new DirectedEdge(this, true);
				return directedReversed;
			} 
		} catch (Throwable t) {
			t.printStackTrace();
		}
		throw new IllegalArgumentException("Not this edge node.");
	}
	
	@Override
	public Edge deepCopy() {
		return deepCopy(new HashMap<Node, Node>(),  new HashMap<Face, Face>());
	}

	@Override
	protected Edge newInstance() {
		return new Edge();
	}

	/**
	 * Nodes and faces maps use original objects for keys as
	 * mapping with IDs is not possible because of new elements.
	 * @param nodeCopies Map from original nodes to copies
	 * @param faceCopies Map from original faces to copies
	 */
	public Edge deepCopy(Map<Node, Node> nodeCopies, Map<Face, Face> faceCopies) {
		Edge copy = (Edge)super.deepCopy();
		if(startNode != null) {
			if(nodeCopies.containsKey(startNode)) {
				copy.startNode = nodeCopies.get(startNode);
			} else {
				nodeCopies.put(startNode, copy.startNode = startNode.deepCopy());
			}
		}
		if(endNode != null) {
			if(nodeCopies.containsKey(endNode)) {
				copy.endNode = nodeCopies.get(endNode);
			} else {
				nodeCopies.put(endNode, copy.endNode = endNode.deepCopy());
			}
		}
		if(leftFace != null) {
			if(faceCopies.containsKey(leftFace)) {
				copy.leftFace = faceCopies.get(leftFace);
			} else {
				faceCopies.put(leftFace, copy.leftFace = leftFace.deepCopy());
			}
		}
		if(rightFace != null) {
			if(faceCopies.containsKey(rightFace)) {
				copy.rightFace = faceCopies.get(rightFace);
			} else {
				faceCopies.put(rightFace, copy.rightFace = rightFace.deepCopy());
			}
		}
		
		//no need to copy directed edges as those will be created from edge copy if needed
		
		return copy;
	}
	
	public static double bearing(CoordinatePair pair) {
		return Math.atan2(pair.y2()-pair.y1(), pair.x2()-pair.x1());
	}
	
	
	private static BearingComparator bearingComparator = null;
	
	/**
	 * Returns a comparator which orders (directed) 
	 * edges clockwise from the negative x axis 
	 */
	public static Comparator<Edge> bearingComparator() {
		if (bearingComparator == null) {
			bearingComparator = new BearingComparator();
		}
		return bearingComparator;
	}
	
	private static class BearingComparator implements Comparator<Edge> {
		public BearingComparator() {}

		/* (non-Javadoc)
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		@Override
		public int compare(Edge o1, Edge o2) {
			HasCoordinate s1 = o1.c1();
			HasCoordinate e1 = o1.c2();
			HasCoordinate s2 = o2.c1();
			HasCoordinate e2 = o2.c2();
			return GeomUtil.compareBearing(e1.x() - s1.x(), e1.y() - s1.y(), e2.x() - s2.x(), e2.y() - s2.y());
		}
		
	}
	
}
