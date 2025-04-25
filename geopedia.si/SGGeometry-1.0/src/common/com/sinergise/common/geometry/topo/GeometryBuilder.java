package com.sinergise.common.geometry.topo;

import static com.sinergise.common.util.collections.CollectionUtil.first;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sinergise.common.geometry.geom.Geometry;
import com.sinergise.common.geometry.geom.GeometryCollection;
import com.sinergise.common.geometry.geom.LineString;
import com.sinergise.common.geometry.geom.LinearRing;
import com.sinergise.common.geometry.geom.MultiLineString;
import com.sinergise.common.geometry.geom.MultiPoint;
import com.sinergise.common.geometry.geom.Point;
import com.sinergise.common.util.collections.CollectionUtil;

public class GeometryBuilder {

	private ITopoMap topology = null;

	public GeometryBuilder() {
		//empty constructor
	}

	public GeometryBuilder(ITopoMap topology) {
		setTopology(topology);
	}

	public void setTopology(ITopoMap topology) {
		this.topology = topology;
	}

	private static List<DirectedEdge> getUnvisitedEdges(ITopoMap map, Node node, Set<Edge> visitedEdges ) {
		NodeStar star = map.getNodeStar(node);
		List<DirectedEdge> edges = new ArrayList<DirectedEdge>();
		if (visitedEdges==null ||visitedEdges.size()==0 ) {
			for (int i=0;i<star.size();i++) 
				edges.add(star.get(i));
			return edges;
		}
		for (int i = 0; i < star.size(); i++) {
			DirectedEdge edge = star.get(i);
			if (!visitedEdges.contains(edge)){
				edges.add(edge);
			}
		}
		return edges;
	}
	
	
	public List<List<Node>> buildSimpleLineStrings(Set<Edge> visitedEdges) {
		ArrayList<List<Node>> strings = new ArrayList<List<Node>>();
		Set<Node> visitedNodes = new HashSet<Node>(topology.getNodes().size());
		
		while (true) {
			Node currentNode = null;
			
			for (Node node:topology.getNodes()) {	
				if (visitedNodes.contains(node))
					continue;
				NodeStar star = topology.getNodeStar(node);
				if (star.size() == 1) {
					currentNode =node;
					break;
				}
				int edgeCount = 0;
				for (int i = 0; i < star.size(); i++) {
					if (!visitedEdges.contains(star.get(i))) {
						edgeCount++;
					}
				}
				if (edgeCount == 1) {
					currentNode = node;
					break;
				}
			}
						
			if (currentNode == null)
				break;

			
			ArrayList<Node> lineStringNodes = new ArrayList<Node>();

			DirectedEdge prevEdge = null;
			lineStringNodes.add(currentNode);

			while (true) {				
				List<DirectedEdge> unvisitedEdges = getUnvisitedEdges(topology, currentNode, visitedEdges);
				if (unvisitedEdges.size() > 1)
					break; // more than 1 edge
				DirectedEdge nextEdge = null;
				for (DirectedEdge edge:unvisitedEdges) {
					if (!edge.equals(prevEdge)) {
						nextEdge = edge;
						break;
					}
				}
				visitedNodes.add(currentNode);
				if (nextEdge == null)
					break; // end of line
				visitedEdges.add(nextEdge);
				Node nextNode = nextEdge.getStartNode().equals(currentNode)
					? nextEdge.getEndNode()
					: nextEdge.getStartNode();
				visitedNodes.add(nextNode);
				lineStringNodes.add(nextNode);
				currentNode = nextNode;
				prevEdge = nextEdge;
			}
			if (lineStringNodes.size() > 1) {
				strings.add(lineStringNodes);
			}
		}


		return strings;
	}


	public Geometry buildGeometry() throws TopologyException {
		return buildGeometry(true, true);
	}

	public Geometry buildGeometry(boolean allowLines, boolean allowPoints) throws TopologyException {
		Map<Face, List<List<Node>>> allNodes = buildLineStringsByFaces(allowLines, allowPoints);

		ArrayList<Point> ptsList = new ArrayList<Point>();
		ArrayList<LineString> lnList = new ArrayList<LineString>(allNodes.size());
		ArrayList<Geometry> polyList = new ArrayList<Geometry>();
		
		for (Face face : allNodes.keySet()) {
			List<List<Node>> faceNodes = allNodes.get(face);
			ArrayList<LinearRing> faceRings = new ArrayList<LinearRing>(faceNodes.size());

			for (List<Node> ringNodes : faceNodes) {
				if (ringNodes.size() == 1) {
					ptsList.add(new Point(first(ringNodes)));
				} else {
					LineString str = TopoUtil.nodesToLineString(ringNodes, false);
					if (str instanceof LinearRing && str.getArea() > 0) {
						faceRings.add((LinearRing)str);
					} else {
						lnList.add(str);
					}
				}
			}

			if (!faceRings.isEmpty()) {
				polyList.add(EvenOddPolygonizer.polygonize(faceRings));
			}
		}

		Geometry polys = null;
		if (!polyList.isEmpty()) {
			if (polyList.size() == 1) {
				polys = polyList.get(0);
			} else {
				polys = new GeometryCollection<Geometry>(polyList);
			}
		}

		Geometry lines = null;
		if (!lnList.isEmpty()) {
			if (lnList.size() == 1) {
				lines = lnList.get(0);
			} else {
				lines = new MultiLineString(lnList.toArray(new LineString[lnList.size()]));
			}
		}

		Geometry points = null;
		if (allowPoints && !ptsList.isEmpty()) {
			if (ptsList.size() == 1) {
				points = first(ptsList);
			} else {
				points = new MultiPoint(ptsList.toArray(new Point[ptsList.size()]));
			}
		}

		if (lines == null) {
			if (points == null) {
				return polys;
			}
			if (polys == null) {
				return points;
			}
		} else if (polys == null && points == null) {
			return lines;
		}
		//Flatten so that Oracle can parse the WKT (it will choke otherwise)
		return flatten(points, lines, polys);
	}

	public List<List<Node>> buildLineStrings(boolean allowLines, boolean allowPoints) throws TopologyException {
		Map<Face, List<List<Node>>> byFaces = buildLineStringsByFaces(allowLines, allowPoints);
		List<List<Node>> list = new ArrayList<List<Node>>(byFaces.size());

		for (List<List<Node>> nodes : byFaces.values()) {
			list.addAll(nodes);
		}

		return list;
	}

	private Map<Face, List<List<Node>>> buildLineStringsByFaces(boolean allowLines, boolean allowPoints) throws TopologyException {
		return buildLineStringsByFaces(cloneMap(topology), allowLines, allowPoints);
	}

	public Map<Face, List<List<Node>>> buildLineStringsByFaces(LiveTopoMap map, boolean allowLines, boolean allowPoints) throws TopologyException {
		
		Map<Face, List<List<Node>>> strings = PolygonBuilder.buildFaceRings(map, new Face());

		//TODO: This seems like it should be handled elsewhere; why would a single polygon be handled differently than two disjoint polygons?
		if (strings.size() > 1) { //if single polygon is build it will have only an outer/default face
			strings.remove(TopoBuilder.OUTER);
		}

		if (!allowLines && !allowPoints) {
			return strings;
		}
		
		List<List<Node>> dangling = strings.get(TopoBuilder.OUTER);
		if (dangling == null) {
			strings.put(TopoBuilder.OUTER, dangling = new ArrayList<List<Node>>());
		}

		if (allowLines) {
			extractDanglingLines(map, dangling);
		}
		
		if (allowPoints) {
			extractDanglingNodes(map, dangling);
		}
		
		return strings;
	}

	public void extractDanglingLines(LiveTopoMap map, List<List<Node>> dangling) {
		Set<Node> visited = new HashSet<Node>(map.getNodes().size());
		while (true) {
			Node singleEdgeNode = map.getSingleEdgeNode(visited);
			if (singleEdgeNode == null) {
				break;
			}
			DirectedEdge edge = map.getNodeStar(singleEdgeNode).getFirst(); // only 1 anyway
			Node nextNode = singleEdgeNode;
			
			List<Node> linestring = new ArrayList<Node>();
			
			while (true) {
				visited.add(nextNode);
				linestring.add(nextNode);
				
				nextNode = edge.getEndNode();
				NodeStar star = map.getNodeStar(nextNode).rotate(edge);
				
				if (star.size() != 2 || visited.contains(nextNode)) {
					break;
				}
				
				edge = star.rotate(edge).left();
			}
			visited.add(nextNode);
			linestring.add(nextNode);
			
			
			dangling.add(linestring);
		}
	}

	public void extractDanglingNodes(LiveTopoMap map, List<List<Node>> dangling) {
		for (Node n : map.getNodes()) {
			if (map.getNodeStar(n).isEmpty()) {
				dangling.add(Collections.singletonList(n));
			}
		}
	}

	/**
	 * handle open linestrings
	 */
	protected void addLineStringsFor(LiveTopoMap tempMap, Face f, Collection<List<Node>> ret) {
		if (tempMap.getNodes() == null || tempMap.getNodes().size() == 0)
			return;
		Set<Edge> edges = new HashSet<Edge>(tempMap.getFaceEdges(f));
		while (true) {

			if (CollectionUtil.isNullOrEmpty(edges))
				break;

			Edge start = null;
			for (Edge e : edges) {
				if (f.equals(e.getRightFace()) && f.equals(e.getLeftFace())) {
					NodeStar star = tempMap.getNodeStar(e, true);
					if (star.size() < 2) {
						start = e;
						break;
					}
					NodeStar end = tempMap.getNodeStar(e, false);
					if (end.size() < 2) {
						start = e.getDirectedEdge(e.getEndNode());
						break;
					}
				}
			}
			if (start == null) { // should only happen with rings
				start = first(edges);
			}
			Node startNode = start.getStartNode();

			Set<Edge> toDelete = new HashSet<Edge>();
			List<Node> nodes = new ArrayList<Node>();
			Edge cur = start;
			if (f.equals(cur.getRightFace()) && f.equals(cur.getLeftFace())) {
				nodes.add(startNode);
			}
			while (true) {
				if (cur == null) {
					break;
				}
				if (!f.equals(cur.getRightFace()) || !f.equals(cur.getLeftFace())) {
					toDelete.add(cur);
					break;
				}
				Node nextNode = cur.getEndNode();
				nodes.add(nextNode);
				if (startNode.equals(nextNode)) { // shouldn't happen but let's check anyway
					break;
				}
				toDelete.add(cur);

				NodeStar star = tempMap.getNodeStar(nextNode);
				Edge next = null;
				if (star.size() > 1) {
					star.rotate(cur);
					for (int i = 0; i < star.size() - 1; i++) {
						DirectedEdge e = star.right(); //go left
						if (e.isStartNode(cur.getEndNode()) && f.equals(e.getLeftFace()) && f.equals(e.getRightFace())) {
							next = e;
							break;
						}
					}
					if (next == null) {
						star.rotate(cur);
						for (int i = 0; i < star.size() - 1; i++) {
							DirectedEdge e = star.right(); //go left
							if (f.equals(e.getLeftFace()) && f.equals(e.getRightFace())) {
								next = e.getDirectedEdge(nextNode);
								break;
							}
						}
					}
				}
				cur = next;
			}
			if (nodes.size() > 0)
				ret.add(nodes);

			for (Edge e : toDelete) {
				edges.remove(e);
				try {
					tempMap.deleteEdge(e);
				} catch(Exception ex) {
					ex.printStackTrace();
				}
			}
		}
	}

	private Geometry flatten(Geometry... geoms) {
		if (geoms == null) {
			return null;
		}
		if (geoms.length == 1) {
			return geoms[0];
		}
		
		ArrayList<Geometry> ret = new ArrayList<Geometry>();
		for (Geometry g : geoms) {
			flatten(ret, g);
		}
		if (ret.isEmpty()) {
			return null;
		}
		return new GeometryCollection<Geometry>(ret.toArray(new Geometry[ret.size()]));
	}

	private void flatten(Collection<Geometry> ret, Geometry g) {
		if (g == null) {
			return;
		}
		if (g instanceof GeometryCollection<?>) {
			for (Geometry child : (GeometryCollection<?>)g) {
				flatten(ret, child);
			}
		} else {
			ret.add(g);
		}
	}
	
	public static LiveTopoMap cloneMap(ITopoMap map) throws TopologyException {
		LiveTopoMap clone = new LiveTopoMap();
		
		// Create copies of edges (nodes will not be changed, so originals can be kept) 
		for (Edge edge : map.getEdges()) {
			clone.addEdge(clone.getTopoFactory().createEdge(
				edge.getStartNode(), edge.getEndNode(), edge.getLeftFace(), edge.getRightFace()));
		}
		
		for (Node n : map.getNodes()) {
			if (map.getNodeStar(n).isEmpty() && clone.getNodeStar(n) == null) {
				clone.addNode(n);
			}
		}
		
		return clone;
	}

}
