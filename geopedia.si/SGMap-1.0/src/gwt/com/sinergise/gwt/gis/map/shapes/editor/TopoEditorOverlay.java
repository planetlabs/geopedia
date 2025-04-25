package com.sinergise.gwt.gis.map.shapes.editor;

import com.sinergise.common.geometry.display.DisplayCoordinateAdapter;
import com.sinergise.common.geometry.topo.Edge;
import com.sinergise.common.geometry.topo.ITopoMap;
import com.sinergise.common.geometry.topo.Node;
import com.sinergise.common.geometry.topo.NodeStar;
import com.sinergise.common.gis.map.render.RenderInfo;
import com.sinergise.gwt.gis.map.shapes.editor.TopoEditor.MidEdge;
import com.sinergise.gwt.gis.map.ui.vector.Marker;
import com.sinergise.gwt.gis.map.ui.vector.VectorOverlay;

public class TopoEditorOverlay extends VectorOverlay {
	private static final double LEN_LIMIT_MIDEDGE = 20;
	private static final double LEN_LIMIT_NODE = 6;
	private double lastWPerPx = 0;
	private TopoEditor editor;
	
	public TopoEditorOverlay(TopoEditor editor) {
		super(editor.map.getCoordinateAdapter());
		addStyleName("mapTopoEditor");
		this.editor = editor;
	}
	
	@Override
	public RenderInfo prepareToRender(DisplayCoordinateAdapter renderingDCA, boolean trans, boolean quick) {
		if (lastWPerPx != renderingDCA.worldLengthPerPix) {
			updateForCurrentScale();
			lastWPerPx = renderingDCA.worldLengthPerPix;
		}
		return super.prepareToRender(renderingDCA, trans, quick);
	}

	private void updateForCurrentScale() {
		boolean maxScale = isMaxScale();
		double minLenSqEdge = lenSqFromPx(LEN_LIMIT_MIDEDGE);
		for (Marker m : editor.midPoints.values()) {
			updateMidEdgeForScale(m, maxScale, minLenSqEdge);
		}
		double minLenSqNode = lenSqFromPx(LEN_LIMIT_NODE);
		for (Marker m : editor.points.values()) {
			updateNodeForScale(m, maxScale, minLenSqNode);
		}
	}
	
	protected double lenSqFromPx(double pxLen) {
		return dca.worldFromPix.area(pxLen*pxLen);
	}

	protected boolean isMaxScale() {
		return dca.getScale() == dca.bounds.minScale();
	}

	public void updateMidEdgeForScale(Marker m) {
		updateMidEdgeForScale(m, isMaxScale(), lenSqFromPx(LEN_LIMIT_MIDEDGE));
	}

	private void updateMidEdgeForScale(Marker m, boolean maxScale, double minLenSq) {
		MidEdge me = (MidEdge)m.getWorldPosition();
		m.setVisible(maxScale || (me.e.lengthSq() > minLenSq));
	}

	public void updateNodeForScale(Marker m) {
		updateNodeForScale(m, isMaxScale(), lenSqFromPx(LEN_LIMIT_NODE));
	}
	
	private void updateNodeForScale(Marker m, boolean maxScale, double minLenSq) {
		if (maxScale || m == editor.selectedPoint) {
			m.setVisible(true);
			updatePointLocation(m);
			return;
		}
		ITopoMap topology = editor.getTopology();
		if (topology == null) {
			return;
		}
		NodeStar nodeStar = topology.getNodeStar((Node)m.getWorldPosition());
		if (nodeStar == null || nodeStar.size() <= 1) {
			m.setVisible(true);
			updatePointLocation(m);
			return;
		}
		boolean allLong = true;
		for (Edge edge : nodeStar) {
			Marker endM = editor.points.get(edge.getEndNode());
			if (endM != null && endM == editor.selectedPoint) {
				m.setVisible(true);
				updatePointLocation(m);
				return;
			}
			if (editor.shouldShowEdge(edge) && edge.lengthSq() < minLenSq) {
				allLong = false;
			}
		}
		m.setVisible(allLong);
		updatePointLocation(m);
	}

	public void updateMarkerForScale(Marker m) {
		if (m == null) {
			return;
		}
		if (m.getWorldPosition() instanceof MidEdge) {
			updateMidEdgeForScale(m);
		} else {
			updateNodeForScale(m);
		}
	}
	
	public void updateNeighbours(Marker m) {
		if (m == null) {
			return;
		}
		if (m.getWorldPosition() instanceof Node) {
			updateNodeNeighbours(m);
		}
	}

	private void updateNodeNeighbours(Marker m) {
		ITopoMap topology = editor.getTopology();
		if (topology == null) {
			return;
		}
		NodeStar nodeStar = topology.getNodeStar((Node)m.getWorldPosition());
		if (nodeStar == null || nodeStar.size() <= 1) {
			return;
		}
		for (Edge edge : nodeStar) {
			Marker endM = editor.points.get(edge.getEndNode());
			if (endM != null) {
				updateNodeForScale(endM);
			}
		}
	}
}
