/**
 * 
 */
package com.sinergise.common.geometry.topo;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.sinergise.common.geometry.geom.LinearRing;
import com.sinergise.common.geometry.util.GeomUtil;
import com.sinergise.common.util.geom.Envelope;
import com.sinergise.common.util.geom.HasCoordinate;


/**
 * @author tcerovski
 */
public class TopoValidator {
	
	public static int CHECK_TOPOLOGY = 1;
	public static int CHECK_TOLERANCES = 2;
	
	public static int CHECK_ALL_MASK = CHECK_TOPOLOGY | CHECK_TOLERANCES;
	
	protected boolean exitOnException = true;
	protected Set<TopoValidationException> errorMessages = new LinkedHashSet<TopoValidationException>();
	
	public final void validate(TopoMap map, int flags) throws TopoValidationException {
		if (flags == 0) {
			return;
		}
		doValidation(map, flags);
		
		if(errorMessages.size() > 0) {
			if(errorMessages.size() == 1) {
				throw errorMessages.iterator().next();
			}
			throw new TopoValidationException("Multiple topology validation errors found", errorMessages);
		}
	}
	
	protected void doValidation(TopoMap map, int flags) throws TopoValidationException {
		if ((flags & CHECK_TOPOLOGY) > 0) {
			checkDanglingNodes(map.getNodes(), map);
			checkEdgeCompleteness(map.getEdges());
			checkEdgeIntersections(map.getEdges());
			validateFaces(map.getFaces(), map);
		}
	}
	
	public void setExitOnException(boolean exit) {
		this.exitOnException = exit;
	}
	
	protected void handleTopoError(String msg, HasCoordinate c) throws TopoValidationException {
		handleTopoError(msg, c != null ? new Envelope(c.x(), c.y(), c.x(), c.y()) : null);
	}
	
	protected void handleTopoError(String msg, Envelope env) throws TopoValidationException {
		TopoValidationException ex = new TopoValidationException(msg, CHECK_TOPOLOGY, env);
		if(exitOnException)
			throw ex;
		errorMessages.add(ex);
	}
	
	protected void validateFaces(Collection<Face> faces, TopoMap map) throws TopoValidationException { 
		for(Face f : faces) {
			checkFaceForMultiPolygons(f, map);
		}
	}
	
	protected void checkFaceForMultiPolygons(Face face, TopoMap map) throws TopoValidationException {
		//TODO: slow - checks every point of inner ring, maybe store right most and check only that
		List<Node> outer = map.getFaceOuterRing(face);
		
		if(outer == null) //if exterior ring
			return;
		
		LinearRing outerLR = TopoUtil.nodesToLinearRing(outer);
		
		for(List<Node> inner : map.getFaceInnerRings(face)) {
			boolean touches = false;
			int c=0;
			for(Node n : inner) {
				//skip - first same as last
				if(c++ == 0)
					continue;
				
				if(outer.contains(n)) {
					if(touches)
						handleTopoError(face.getName()+" is a multipolygon", map.getFaceEnvelope(face));
					touches = true;
				}
				
				if(!touches && !GeomUtil.isPointInRing(n.x(), n.y(), outerLR)) {
					handleTopoError(face.getName()+" is a multipolygon", map.getFaceEnvelope(face));
				}
				
				if(!outer.contains(n) && !GeomUtil.isPointInRing(n.x(), n.y(), outerLR)) {
					handleTopoError(face.getName()+" is a multipolygon", map.getFaceEnvelope(face));
					return;
				}
			}
		}
	}
	
	
	protected void checkEdgeCompleteness(Collection<Edge> edges) throws TopoValidationException {
		for(Edge e : edges) {
			checkEdgeCompleteness(e);
		}
	}
	
	protected void checkEdgeIntersections(Collection<Edge> edges) throws TopoValidationException {
		//TODO: slow - O(n^2), use scanline
		
		for(Edge e1 : edges) {
			for(Edge e2 : edges) {
				if(e1.equals(e2))
					continue;
				
				if(e1.hasNode(e2.getStartNode()) ^ e1.hasNode(e2.getEndNode()))
					continue;
				
				if(GeomUtil.lineLineIntersect(e1.x1(), e1.y1(), e1.x2(), e1.y2(), e2.x1(), e2.y1(), e2.x2(), e2.y2()) > 0) 
				{
					handleTopoError(e1.getName() + " and "+e2.getName()+" intersects", e1.getEnvelope().union(e2.getEnvelope()));
				}
			}
		}
	}
	
	protected void checkEdgeCompleteness(Edge e) throws TopoValidationException {
		checkEdgeNodes(e);
		checkEdgeFaces(e);
	}
	
	protected void checkEdgeNodes(Edge e) throws TopoValidationException {
		if(e.getStartNode() == null) 
			handleTopoError(e.getName() + " is missing start node", e.getEndNode());
		if(e.getEndNode() == null) 
			handleTopoError(e.getName() + " is missing end node", e.getStartNode());
		if(e.getStartNode().equals(e.getEndNode())) 
			handleTopoError(e.getName() + " has same start and end node", e.getEnvelope());
	}
	
	protected void checkEdgeFaces(Edge e) throws TopoValidationException {
		if(e.getLeftFace() == null) {
			handleTopoError(e.getName() + " is missing left face", e.getEnvelope());
		}
		if(e.getRightFace() == null) {
			handleTopoError(e.getName() + " is missing right face", e.getEnvelope());
		}
		if(e.getLeftFace().equals(e.getRightFace())) {
			handleTopoError(e.getName() + " has same left and right face", e.getEnvelope());
		}
	}
	
	protected void checkDanglingNodes(Collection<Node> nodes, TopoMap map) throws TopoValidationException {
		for(Node n : nodes) {
			if(map.getNodeStar(n).size() < 2) {
				handleTopoError(n.getName() + " is a dangling node", map.getElementEnvelope(n));
			}
		}
	}
	
}
