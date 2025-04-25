package com.sinergise.geopedia.client.components.editor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.sinergise.common.geometry.display.GraphicMeasure;
import com.sinergise.common.geometry.geom.Geometry;
import com.sinergise.common.geometry.geom.GeometryCollection;
import com.sinergise.common.geometry.geom.LineString;
import com.sinergise.common.geometry.geom.LinearRing;
import com.sinergise.common.geometry.geom.MultiLineString;
import com.sinergise.common.geometry.geom.MultiPoint;
import com.sinergise.common.geometry.geom.MultiPolygon;
import com.sinergise.common.geometry.geom.Point;
import com.sinergise.common.geometry.geom.Polygon;
import com.sinergise.common.geometry.topo.Edge;
import com.sinergise.common.geometry.topo.EvenOddPolygonizer;
import com.sinergise.common.geometry.topo.Face;
import com.sinergise.common.geometry.topo.GeometryBuilder;
import com.sinergise.common.geometry.topo.LiveTopoMap;
import com.sinergise.common.geometry.topo.Node;
import com.sinergise.common.geometry.topo.TopoBuilder;
import com.sinergise.common.geometry.topo.TopoEditorModel;
import com.sinergise.common.geometry.topo.TopoUtil;
import com.sinergise.common.geometry.topo.TopologyException;
import com.sinergise.geopedia.client.core.ClientGlobals;
import com.sinergise.geopedia.client.core.events.FeatureInfoEvent;
import com.sinergise.geopedia.client.core.i18n.Messages;
import com.sinergise.geopedia.client.ui.map.MapComponent;
import com.sinergise.geopedia.client.ui.map.MapWidget;
import com.sinergise.geopedia.core.entities.Feature;
import com.sinergise.geopedia.core.entities.GeomType;
import com.sinergise.gwt.gis.map.shapes.editor.PointEditor;
import com.sinergise.gwt.gis.map.shapes.editor.PointEditor.GeomChangedListener;
import com.sinergise.gwt.gis.map.shapes.editor.TopoEditor;
import com.sinergise.gwt.gis.map.shapes.editor.TopoEditor.DefaultTopoEditorBehavior;
import com.sinergise.gwt.gis.map.shapes.editor.TopoEditor.ITopoEditorBehavior;
import com.sinergise.gwt.gis.map.shapes.editor.TopoEditor.TopoEditorModificationListener;
import com.sinergise.gwt.gis.map.ui.vector.VectorFilter.GlowFilter;

public class GeometryEditor {
	
	protected static final Logger logger = LoggerFactory.getLogger(GeometryEditor.class);

	protected MapWidget mapWidget;
	
	protected PointEditor pointEditor;
	protected TopoEditor topoEditor;
	public static final GlowFilter LINE_BLACK_GLOW = new GlowFilter("black", 2, 0.35);
	public static final GlowFilter TEXT_BLACK_GLOW = new GlowFilter("#333333", 2, 1);
	protected GeomType geomType = GeomType.NONE;	
	private boolean editingFeature;
	
	public GeometryEditor(MapWidget mapWidget) {
		this.mapWidget = mapWidget;
		this.editingFeature = false;
	}

	public boolean isEditingFeature() {
		return editingFeature;
	}

	public Geometry toGeometry() throws TopologyException {
		if (pointEditor!=null){
			return pointToGeometry();
		} 
		return topologyToGeometry();
	}

	public GeometryCollection<?> toGeometryCollection() throws TopologyException {
		Geometry g = topologyToGeometry();
		if (g instanceof GeometryCollection<?>) {
			return (GeometryCollection<?>) g;
		}
		if (g instanceof Point) {
			return new MultiPoint(new Point[]{(Point)g});
		}
		if (g instanceof Polygon) {
			return new MultiPolygon(new Polygon[]{(Polygon)g});
		}
		if (g instanceof LineString) {
			return new MultiLineString(new LineString[]{(LineString)g});
		}
		if (g == null) {
			return new GeometryCollection<Geometry>();
		}
		throw new IllegalStateException("Unknown geometry class "+g);
	}
	
	protected Geometry topologyToGeometry() throws TopologyException {
		
		TopoEditorModel topoModel = topoEditor.getTopoModel();		
		Set<Edge> visitedEdges = new HashSet<Edge>();
		
		GeometryBuilder gb = new GeometryBuilder(topoModel.getTopoMap());		
		List<List<Node>> simpleLines =  gb.buildSimpleLineStrings(visitedEdges);
		
		LiveTopoMap tempMap = new LiveTopoMap();

		// Create copies of edges (nodes will not be changed, so originals can be kept) 
		Collection<Edge> origEdges = topoModel.getTopoMap().getEdges();
		for (Edge edge : origEdges) {
			if (!visitedEdges.contains(edge)) {
				tempMap.addEdge(tempMap.getTopoFactory().createEdge(edge.getStartNode(), edge.getEndNode(),
					edge.getLeftFace(), edge.getRightFace()));
			}
		}
		Map<Face, List<List<Node>>>  byFaces = gb.buildLineStringsByFaces(tempMap, true, true);
		
		ArrayList<LineString> lineStrings = new ArrayList<LineString>();
		for (List<Node> lString:simpleLines) {
			lineStrings.add(TopoUtil.nodesToLineString(lString,false));			
		}
		ArrayList<LinearRing> rngs = new ArrayList<LinearRing>();
		for (List<List<Node>> lStrings:byFaces.values()) {
			for (List<Node> lString:lStrings) {
				LineString ls = TopoUtil.nodesToLineString(lString,false);
				if (ls instanceof LinearRing) {
					rngs.add((LinearRing) ls);
				} else {
					lineStrings.add(ls);
				}
			}
		}
		
		Geometry polys = rngs.isEmpty() ? null : EvenOddPolygonizer.polygonize(rngs);
		if (lineStrings.isEmpty()) {
			return polys;
		}
		Geometry lstrings = lineStrings.size() == 1 ? lineStrings.get(0) : new MultiLineString(lineStrings.toArray(new LineString[lineStrings.size()]));
		if (polys == null) {
			return lstrings;
		}
		return new GeometryCollection<Geometry>(lstrings, polys);
	}
	
	private Geometry pointToGeometry() {
		return pointEditor.getGeometry();
	}

	private ITopoEditorBehavior gpdBehavior = new DefaultTopoEditorBehavior() {
		
		@Override
		public boolean addNodeOnMouseDrag() {
			return false;
		}

		@Override
		public boolean chainMouseDragEvents() {
			return true;
		}
	};

	

	public TopoEditor getTopoEditor() {
		return topoEditor;
	}

	public void editFeature(Feature editedFeature) throws TopologyException {
		this.editingFeature = true;
		if (editedFeature.hasValidId()) {
			FeatureInfoEvent fie = new FeatureInfoEvent(editedFeature);
			ClientGlobals.eventBus.fireEvent(fie);
		}
		editGeometry(editedFeature.featureGeometry, editedFeature.geomType);
	}

	List<ValueChangeHandler<Geometry>> geometryChanged = new ArrayList<ValueChangeHandler<Geometry>>();
	
	public void addGeometryChangedListener(ValueChangeHandler<Geometry> handler){
		geometryChanged.add(handler);
	}
	
	public void editGeometry(Geometry geom, GeomType newType) throws TopologyException {
		this.geomType = newType;
		mapWidget.repaint();
		MapComponent mapComponent = mapWidget.getMapComponent();
		mapComponent.getMouseHandler().saveActionsState();
		
		if (newType.isPoint()) {
			boolean isMultipoint = newType == GeomType.POINTS_M;
			pointEditor = new PointEditor(mapComponent,isMultipoint);
			pointEditor.setGeometry(geom);		
			pointEditor.startEditing();
			if(!geometryChanged.isEmpty()){
				pointEditor.addGeomChangedListener(new GeomChangedListener() {
					
					@Override
					public void geometryChanged() {
						for(ValueChangeHandler<Geometry> vch : geometryChanged){
							vch.onValueChange(new ValueChangeEvent<Geometry>(pointEditor.getGeometry()){});
						}
					}
				});
			}
			
		} else {
			topoEditor = new TopoEditor(mapComponent, gpdBehavior);
			topoEditor.getSelectableForLabelsDisplay().setSelected(false);
			topoEditor.setLineWidth(GraphicMeasure.fixedDisplaySize(2));
			topoEditor.setTextMarkerStyle(TEXT_BLACK_GLOW, "#ffffff");
			if (geom!=null) {
				topoEditor.setTopology(TopoBuilder.build(geom, !newType.isLine()));//https://rt.cosylab.com/rt3/Ticket/Display.html?id=93958
			}
			topoEditor.setVisible(true);			
			topoEditor.setActive(true);
			if(!geometryChanged.isEmpty()){
				topoEditor.addModificationListener(new TopoEditorModificationListener() {
					
					@Override
					public void topologyModified() {
						for(ValueChangeHandler<Geometry> vch : geometryChanged){
							vch.onValueChange(new ValueChangeEvent<Geometry>(pointEditor.getGeometry()){});
						}
					}
				});
			}
		}
	}
	
	

	public boolean isRunning() {
		return pointEditor !=null || topoEditor != null;
	}
	
	public GeomType getGeomType() {
		return geomType;
	}
	
	public void closeEditor() {
		boolean editorWasRunning = false;
		if (pointEditor != null) {
			pointEditor.stopEditing();
			pointEditor = null;
			editorWasRunning=true;
		} else if (topoEditor != null) {
			topoEditor.setActive(false);
			topoEditor.setVisible(false);
			topoEditor=null;
			editorWasRunning=true;
		}

		if (editorWasRunning) {
			MapComponent mapComponent = mapWidget.getMapComponent();
			mapComponent.getMouseHandler().deregisterAllActions();
			mapComponent.getMouseHandler().restoreActionsState();
		}
	}

	public boolean saveFeature(Feature feature) throws Exception {
		if (pointEditor != null) {
			Geometry geom = pointEditor.getGeometry();
			if (geom!=null) {
				if (feature.geomType == GeomType.POINTS && !(geom instanceof Point)) {
					throw new Exception(Messages.INSTANCE.geometryErrorOnlyPoint());
				}
				feature.featureGeometry = geom;
				return true;
			}
		} else if (topoEditor != null) {
			try {
				Geometry geom = getValidGeometry(); 
				if (geom!=null) {
					feature.featureGeometry=geom;
					return true;
				}
				return false;
			} catch (TopologyException ex) {
				logger.error(Messages.INSTANCE.geometryErrorTopology() + ": " + ex.getMessage());
				throw new Exception(Messages.INSTANCE.geometryErrorTopology());
			}
		}		
		feature.featureGeometry=null;
		return true;
	}
	
	
	public Geometry getValidGeometry() throws Exception {
		Geometry geom = topologyToGeometry();
		if (geom instanceof Point ||
				geom instanceof MultiPoint ||
				geom instanceof LineString || 
				geom instanceof MultiLineString || 
				geom instanceof Polygon || 
				geom instanceof MultiPolygon) {
			return geom;
		}
		throw new Exception(Messages.INSTANCE.geometryErrorTopology());
	}

	public PointEditor getPointEditor() {
		return pointEditor;
	}

}
