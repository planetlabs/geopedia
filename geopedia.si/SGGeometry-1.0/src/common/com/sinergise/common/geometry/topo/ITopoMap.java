package com.sinergise.common.geometry.topo;

import java.util.Collection;

import com.sinergise.common.util.geom.Envelope;

public interface ITopoMap {
	Collection<Face> getFaces();
	Collection<Edge> getEdges();
	Collection<Node> getNodes();
	NodeStar getNodeStar(Node node);
	NodeStar getNodeStar(Edge e, boolean startNode);
	Envelope getMBR();
}
