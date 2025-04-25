package com.sinergise.gwt.gis.map.shapes.snap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sinergise.common.geometry.display.GraphicMeasure;
import com.sinergise.common.geometry.geom.Geometry;
import com.sinergise.common.geometry.geom.LinearRing;
import com.sinergise.common.geometry.geom.MultiPolygon;
import com.sinergise.common.geometry.geom.Point;
import com.sinergise.common.geometry.geom.Polygon;
import com.sinergise.common.geometry.topo.ITopoMap;
import com.sinergise.common.geometry.topo.Node;
import com.sinergise.common.geometry.topo.NodeStar;
import com.sinergise.common.geometry.topo.PolygonBuilder;
import com.sinergise.common.geometry.topo.TopologyException;
import com.sinergise.common.geometry.util.GeomUtil;
import com.sinergise.common.gis.map.shapes.snap.SnapProvider;
import com.sinergise.common.util.ArrayUtil;
import com.sinergise.common.util.event.SourcesValueChangeEvents;
import com.sinergise.common.util.event.ValueChangeListener;
import com.sinergise.common.util.event.ValueChangeListenerCollection;
import com.sinergise.common.util.event.selection.Selectable;
import com.sinergise.common.util.event.selection.SelectableImpl;
import com.sinergise.common.util.event.selection.SourcesToggleEvents;
import com.sinergise.common.util.event.selection.ToggleListener;
import com.sinergise.common.util.geom.Envelope;
import com.sinergise.common.util.geom.HasCoordinate;
import com.sinergise.common.util.geom.LineSegment2D;
import com.sinergise.gwt.gis.map.shapes.editor.GeometryEditorController;
import com.sinergise.gwt.gis.map.shapes.editor.TopoEditor;
import com.sinergise.gwt.gis.map.shapes.editor.TopoEditor.MarkerMoveListener;
import com.sinergise.gwt.gis.map.shapes.editor.TopoEditor.TopoEditorModificationListener;
import com.sinergise.gwt.gis.map.ui.MapComponent;
import com.sinergise.gwt.gis.map.ui.vector.LineMarker;
import com.sinergise.gwt.gis.map.ui.vector.LineMarkerStyle;
import com.sinergise.gwt.gis.map.ui.vector.Marker;
import com.sinergise.gwt.gis.map.ui.vector.VectorOverlay;

public class PolygonAreaSnapProvider implements SnapProvider, SourcesValueChangeEvents<Double> {
	
	private Logger logger = LoggerFactory.getLogger(PolygonAreaSnapProvider.class);
	
	private static final int DEFAULT_SNAP_PX_TOLERANCE = 12;
	private static final LineMarkerStyle SNAP_LINE_MARKER_STYLE = new LineMarkerStyle("rgb(226,56,56)", GraphicMeasure.fixedPixels(1));

	private final TopoEditor topoEditor;
	private final PolygonBuilder polyBuilder;
	private final MapComponent map;
	private final VectorOverlay ovr;
	private final Selectable enabled;
	
	private final ValueChangeListenerCollection<Double> vcListeners = new ValueChangeListenerCollection<Double>();
	
	private Double targetArea = null;
	private Geometry currentPolygon = null;
	private LineSegment2D snapLine = null;
	
	public PolygonAreaSnapProvider(MapComponent map, TopoEditor editor) {
		this(map, editor, new SelectableImpl(true));
	}
	
	public PolygonAreaSnapProvider(MapComponent map, TopoEditor editor, Selectable enabled) {
		this(map, editor, enabled, new PolygonBuilder(editor.getTopology()));
	}
	
	public PolygonAreaSnapProvider(GeometryEditorController ctrl, Selectable enabled) {
		this(ctrl.getMap(), ctrl.getTopoEditor(), enabled, ctrl.getPolygonBuilder());
	}
	
	public PolygonAreaSnapProvider(MapComponent map, TopoEditor editor, Selectable enabled, PolygonBuilder polyBuilder) {
		this.map = map;
		this.topoEditor = editor;
		this.polyBuilder = polyBuilder;
		this.ovr = new VectorOverlay(map.getCoordinateAdapter());
		this.enabled = enabled;
		
		init();
	}
	
	private void init() {
		topoEditor.addModificationListener(new TopoEditorModificationListener() {
			@Override
			public void topologyModified() {
				if (enabled.isSelected()) {
					updateCurrentPolygon();
				}
			}
		});
		
		topoEditor.addMarkerMoveListener(new MarkerMoveListener() {
			
			@Override
			public void onMarkerMoveStart(Marker marker) {
				if (marker.worldPos instanceof Node) {
					startAdjustingForNode((Node)marker.worldPos);
				}
				
			}
			
			@Override
			public void onMarkerMoveEnd(Marker marker) {
				stopAdjusting();
			}
			
		});
		
		enabled.addToggleListener(new ToggleListener() {
			@Override
			public void toggleStateChanged(SourcesToggleEvents source, boolean newOn) {
				if (newOn) {
					updateCurrentPolygon();
					setDefaultTargetArea();
				}
			}
		});
	}
	
	protected void setDefaultTargetArea() {
		if (currentPolygon != null) {
			setTargetArea(Double.valueOf(currentPolygon.getArea()));
		}
	}
	
	public void setTargetArea(Double area) {
		Double oldValue = targetArea;
		targetArea = area;
		vcListeners.fireChange(this, oldValue, targetArea);
	}
	
	public Double getTargetArea() {
		return targetArea;
	}
	
	public boolean hasTargetArea() {
		return targetArea != null && targetArea.doubleValue() > 0;
	}
	
	@Override
	public void addValueChangeListener(ValueChangeListener<? super Double> l) {
		vcListeners.add(l);
	}
	
	@Override
	public void removeValueChangeListener(ValueChangeListener<? super Double> l) {
		vcListeners.remove(l);
	}
	
	protected boolean isReady() {
		return enabled.isSelected() && hasTargetArea() && currentPolygon != null;
	}
	
	public void reset() {
		targetArea = null;
		currentPolygon = null;
		snapLine = null;
	}
	
	/**
	 * 
	 * @deprecated will be set automatically from the topoEditor
	 */
	@Deprecated
	protected void setCurrentPolygon(Geometry poly) {
		currentPolygon = poly;
	}
	
	protected void updateCurrentPolygon() {
		polyBuilder.setTopology(topoEditor.getTopology());
		try {
			currentPolygon = polyBuilder.buildPolygonForDefaultFace();
		} catch(TopologyException e) {
			currentPolygon = null;
			logger.error("Error constructing current polygon: "+e.getMessage());
		}
	}
	
	protected void startAdjustingForNode(Node node) {
		ITopoMap topoMap = topoEditor.getTopology();
		NodeStar star = topoMap.getNodeStar(node); 
		
		if (!isReady() || star.size() != 2) {
			return;
		}
		
		Node a = star.getFirst().getEndNode();
		Node b = star.getNode();
		Node c = star.getLast().getEndNode();
		
		Geometry exPoly = excludeNode(currentPolygon, star.getNode());
		double currentAreaEx = exPoly.getArea();
		double dArea = targetArea.doubleValue() - currentAreaEx;
		
		double slope = (a.y()-c.y()) / (a.x()-c.x());
		double bearing = Math.atan2(a.x()-c.x(), a.y()-c.y()) + Math.PI/2;
		double targetH = 2*Math.abs(dArea) / GeomUtil.distance(a, c);
		
		//check direction
		Point midPoint = new Point(
				(a.x()+c.x())*0.5 + Math.sin(bearing)*0.01,
				(a.y()+c.y())*0.5 + Math.cos(bearing)*0.01);
		Geometry chkGeom = moveNode(currentPolygon, b, midPoint);
		
		if (Math.abs(targetArea.doubleValue() - chkGeom.getArea()) > Math.abs(dArea)) {
			targetH = -targetH;
		}
		
		Point p0 = new Point(
				a.x() + Math.sin(bearing)*targetH,
				a.y() + Math.cos(bearing)*targetH);
		double yint = p0.y() - slope*p0.x();
		
		Envelope mbr = map.getCoordinateAdapter().worldRect;
		Point p1 = new Point(mbr.getMinX(), slope*mbr.getMinX() + yint);
		Point p2 = new Point(mbr.getMaxX(), slope*mbr.getMaxX() + yint);
		
		map.getOverlaysHolder().insertOverlay(ovr, MapComponent.Z_TOP_CONSTRUCTION, true);
		ovr.addLine(new LineMarker(p1, p2, SNAP_LINE_MARKER_STYLE));
		
		snapLine = new LineSegment2D(p1, p2);
	}
	
	protected void stopAdjusting() {
		ovr.clear();
	}
	
	@Override
	public void snapPoint(HasCoordinate point, SnapProviderCallback callback) {
		if (isReady() && snapLine != null) {
		
			double distSq = GeomUtil.distancePointLineSegmentSq(point.x(), point.y(), 
								snapLine.x1(), snapLine.y1(), snapLine.x2(), snapLine.y2());
			
			double maxDist = map.getCoordinateAdapter().worldFromPix.length(DEFAULT_SNAP_PX_TOLERANCE);
			if (distSq < maxDist) {
				Point snapLocation = new Point();
				GeomUtil.pointLineStringNearest(snapLine.x1(), snapLine.y1(), snapLine.x2(), snapLine.y2(), point.x(), point.y(), snapLocation);
				callback.onPointSnapped(point, snapLocation);
				return;
			} 
		}
		
		callback.onPointNotSnapped(point);
	}
	
	@Override
	public Selectable getEnabled() {
		return enabled;
	}
	
	public static Geometry excludeNode(Geometry geom, HasCoordinate n) {
		Geometry clone = geom.clone();
		if (clone instanceof Polygon) {
			excludePolygonNode((Polygon)clone, n);
		} else if (clone instanceof MultiPolygon) {
			for (Polygon p : ((MultiPolygon)clone)) {
				excludePolygonNode(p, n);
			}
		}
		
		return clone;
	}
	
	private static void excludePolygonNode(Polygon p, HasCoordinate n) {
		excludeRingNode(p.outer, n);
		for (int i=0; i<p.getNumHoles(); i++) {
			excludeRingNode(p.getHole(i), n);
		}
	}
	
	private static void excludeRingNode(LinearRing r, HasCoordinate n) {
		int idx = GeomUtil.indexOfXY(r.coords, n.x(), n.y());
		if (idx < 0) {
			return;
		}
		
		idx *= 2;
		double[] target = new double[r.coords.length-2];
		int targetLen = target.length;
		
		ArrayUtil.arraycopy(r.coords, 0, target, 0, idx);
		ArrayUtil.arraycopy(r.coords, idx+2, target, idx, r.coords.length - (idx+2));
		if (idx == 0) {
			target[targetLen - 2] = target[0];
			target[targetLen - 1] = target[1];
		} else if (idx == r.coords.length-2) {
			target[0] = target[targetLen - 2];
			target[1] = target[targetLen - 1];
		}
		
		r.coords = target;
	}
	
	private static Geometry moveNode(Geometry geom, HasCoordinate node, HasCoordinate moveTo) {
		Geometry clone = geom.clone();
		if (clone instanceof Polygon) {
			movePolygonNode((Polygon)clone, node, moveTo);
		} else if (clone instanceof MultiPolygon) {
			for (Polygon p : ((MultiPolygon)clone)) {
				movePolygonNode(p, node, moveTo);
			}
		}
		
		return clone;
	}
	
	private static void movePolygonNode(Polygon p, HasCoordinate node, HasCoordinate moveTo) {
		moveRingNode(p.outer, node, moveTo);
		for (int i=0; i<p.getNumHoles(); i++) {
			moveRingNode(p.getHole(i), node, moveTo);
		}
	}
	
	private static void moveRingNode(LinearRing r, HasCoordinate node, HasCoordinate moveTo) {
		int idx = GeomUtil.indexOfXY(r.coords, node.x(), node.y());
		if (idx < 0) {
			return;
		}
		
		idx *= 2;
		int len = r.coords.length;
		r.coords[idx] = moveTo.x();
		r.coords[idx+1] = moveTo.y();
		
		if (idx == 0) {
			r.coords[len - 2] = r.coords[0];
			r.coords[len - 1] = r.coords[1];
		} else if (idx == len-2) {
			r.coords[0] = r.coords[len - 2];
			r.coords[1] = r.coords[len - 1];
		}
		
	}
	
}