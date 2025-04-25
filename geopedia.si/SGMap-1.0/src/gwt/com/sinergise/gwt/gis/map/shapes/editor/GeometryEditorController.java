package com.sinergise.gwt.gis.map.shapes.editor;

import static com.sinergise.common.geometry.util.GeomUtil.hasLineIntersections;
import static com.sinergise.common.gis.map.model.layer.FeatureDataLayer.TYPE_COLLECTION;
import static com.sinergise.common.gis.map.model.layer.FeatureDataLayer.TYPE_MULTI_POLY;
import static com.sinergise.common.gis.map.model.layer.FeatureDataLayer.TYPE_POLYGON;
import static com.sinergise.common.util.geom.Envelope.isNullOrEmpty;
import static com.sinergise.gwt.gis.i18n.Messages.UI_MESSAGES;
import static com.sinergise.gwt.gis.map.shapes.editor.GeometryEditorUtil.applyGeometryToACopy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.sinergise.common.geometry.geom.Geometry;
import com.sinergise.common.geometry.geom.GeometryCollection;
import com.sinergise.common.geometry.topo.GeometryBuilder;
import com.sinergise.common.geometry.topo.ITopoMap;
import com.sinergise.common.geometry.topo.PolygonBuilder;
import com.sinergise.common.geometry.topo.TopologyException;
import com.sinergise.common.gis.feature.CFeature;
import com.sinergise.common.gis.feature.CFeatureDataSource.FeatureCollectionCallback;
import com.sinergise.common.gis.feature.CFeatureIdentifier;
import com.sinergise.common.gis.feature.FeatureAccessException;
import com.sinergise.common.gis.map.model.layer.FeatureDataLayer;
import com.sinergise.common.gis.map.model.layer.info.FeatureInfoCollection;
import com.sinergise.common.gis.query.QueryUtil;
import com.sinergise.common.util.geom.Envelope;
import com.sinergise.gwt.gis.i18n.Messages;
import com.sinergise.gwt.gis.map.shapes.editor.GeometryEditorControllerBase.SupportsReset;
import com.sinergise.gwt.gis.map.shapes.editor.TopoEditor.DefaultTopoEditorBehavior;
import com.sinergise.gwt.gis.map.shapes.editor.TopoEditor.ITopoEditorBehavior;
import com.sinergise.gwt.gis.map.shapes.editor.TopoEditor.TopoEditorModificationListener;
import com.sinergise.gwt.gis.map.ui.MapComponent;
import com.sinergise.gwt.gis.map.ui.actions.info.FeatureInfoUtilGWT;

public class GeometryEditorController extends GeometryEditorControllerBase implements SupportsReset {
	
	public static String PROP_ACTIVE_FEATURE 	= "feature";
	
	protected final PolygonBuilder polyBuilder = new PolygonBuilder();
	protected final GeometryBuilder geomBuilder = new GeometryBuilder();
	
	protected boolean topoDirty = false;
	protected CFeature activeFeature = null;
	
	public GeometryEditorController(MapComponent mapComp) {
		super(mapComp);
		
		editor.addModificationListener(new TopoEditorModificationListener() {
			@Override
			public void topologyModified() {
				topoDirty = true;
			}
		});
	}
	
	@Override
	protected ITopoEditorBehavior createTopoEditorBehavior() {
		return new DefaultTopoEditorBehavior() {
			@Override
			public int getSupportedGeometryTypes() {
				if (activeFeatureLayer == null) {
					return 0;
				}
				return activeFeatureLayer.getTopoType(); 
			}
			
			@Override
			public boolean addNodeOnMouseDrag() {
				return false;
			}
		};
	}
	
	public PolygonBuilder getPolygonBuilder() {
		return polyBuilder;
	}
	
	public void editFeatureGeometry(CFeatureIdentifier featureId, final GeometryEditorCallback callback) {
		this.editorCallback = callback;
		//TODO: check if already editing
		
		setActiveFeatureLayer(featureId.getFeatureTypeName());
		
		//fetch feature geometry
		QueryUtil.queryFeatureGeometry(activeFeatureLayer, featureId, new FeatureCollectionCallback() {
			@Override
			public void onSuccess(FeatureInfoCollection result) {
				if (result.getItemCount() == 1) {
					editFeatureGeometry(result.getItem(0).f, callback);
				} else if (result.getItemCount() > 1){
					handleError(UI_MESSAGES.geometryEditor_errorTooManyHits(), null, true);
				} else {
					handleError(UI_MESSAGES.geometryEditor_errorNoFeature(), null, true);
				}
			}
			
			@Override
			public void onError(FeatureAccessException error) {
				handleError(error, true);
			}
		});
	}

	public void editFeatureGeometry(CFeature feature, GeometryEditorCallback callback) {
		this.editorCallback = callback;
		//TODO: check if already editing and prompt what to do
		
		setActiveFeature(feature);

		//fetch geometry if not loaded
		if (!feature.getDescriptor().hasGeometry()) {
			editFeatureGeometry(feature.getIdentifier(), callback);
			return;
		}
		
		startTopologyEdit();
	}
	
	protected void startTopologyEdit() {
		try {
			editTopology(buildInitialTopology());
			ensureVisible();
		} catch (TopologyException e) {
			handleError(UI_MESSAGES.geometryEditor_errorTopology(e.getMessage()), e, true);
		}
	}
	
	public void ensureVisible() {
		//change view only if no part of geometry is visible
		Envelope mbr = activeFeature.getEnvelope();
		if (!isNullOrEmpty(mbr) && !map.getCoordinateAdapter().worldRect.intersects(mbr)) {
			FeatureInfoUtilGWT.ensureVisible(mbr, map, 5);
		}
	}
	
	protected ITopoMap buildInitialTopology() throws TopologyException {
		topoBuilder.reset();
		topoBuilder.addGeometry(activeFeature);
		return topoBuilder.buildTopology();
	}
	
	@Override
	public void editTopology(ITopoMap topology) {
		super.editTopology(topology);
		updateBuilderReferences();
	}
	
	protected void setActiveFeature(CFeature feature) {
		CFeature oldActive = activeFeature;
		topoDirty = true;
		activeFeature = feature;
		updateBuilderReferences();
		propChangeListeners.fireChange(this, PROP_ACTIVE_FEATURE, oldActive, activeFeature);
		
		setActiveFeatureLayer(feature.getFeatureTypeName());
	}
	
	@Override
	protected void cleanup() {
		super.cleanup();
		activeFeature = null;
		activeFeatureLayer = null;
	}
	
	public CFeature getActiveFeature() {
		return activeFeature;
	}
	
	public FeatureDataLayer getActiveFeatureLayer() {
		return activeFeatureLayer;
	}
	
	@Override
	public Geometry getActiveGeometry() {
		return getActiveGeometry(true);
	}
	
	public Geometry getActiveGeometry(boolean checkTopoType) {
		if (activeFeature == null) {
			return null;
		}
		
		try {
			int topoType = activeFeatureLayer.getTopoType();
			if (topoDirty) {
				updateBuilders();
			}
			
			Geometry geom = null;
			
			//use polygon builder if editing polygons
			if (topoType == TYPE_POLYGON || topoType == TYPE_MULTI_POLY) {
				geom = polyBuilder.buildPolygonForDefaultFace();
			} 
			
			if (geom == null) {
				geom = geomBuilder.buildGeometry();
			}
			
			if (checkTopoType) {
				return parseForTopoType(geom);
			}
			return geom;
			
		} catch(TopologyException e) {
			handleError(UI_MESSAGES.geometryEditor_errorTopology(e.getMessage()), e, false);
			return null;
		}
	}
	
	public void clearActiveGeometry() {
		editor.topoReset();
	}
	
	@Override
	public void reset() {
		try {
			editTopology(buildInitialTopology());
		} catch (TopologyException e) {
			handleError(UI_MESSAGES.geometryEditor_errorTopology(e.getMessage()), e, true);
		}
	}
	
	@Override
	protected Collection<CFeature> getModifiedFeatureCopies() {
		return Collections.singletonList(applyGeometryToACopy(getActiveFeature(), getActiveGeometry()));
	}
	
	//should be updated only when topoDirty to use builders cache 
	private void updateBuilders() {
		geomBuilder.setTopology(editor.getTopology());
		polyBuilder.setTopology(editor.getTopology());
		updateBuilderReferences();
		topoDirty = false;
	}

	private void updateBuilderReferences() {
		polyBuilder.setFaceReferences(topoBuilder.getFaceReferences());
		polyBuilder.setDefaultFace(activeFeature);
	}
	
	protected Geometry parseForTopoType(Geometry geom) throws TopologyException {
		int topoType = activeFeatureLayer.getTopoType();
		
		//return first element if collection not supported
		if (geom instanceof GeometryCollection) {
			if ((topoType & TYPE_COLLECTION) == 0) {
				return parseForTopoType(((GeometryCollection<?>)geom).get(0));
			} 
			
			List<Geometry> parts = new ArrayList<Geometry>();
			for (Geometry part : (GeometryCollection<?>)geom) {
				Geometry checkedPart = parseForTopoType(part);
				if (checkedPart != null) {
					parts.add(checkedPart);
				}
			}
			return new GeometryCollection<Geometry>(parts);
		} 
		
		if ((topoType & FeatureDataLayer.Util.toTypeMask(geom)) > 0) {
			return geom;
		}
		
		return null;
	}
	
	@Override
	protected boolean validate(Collection<CFeature> modified) {
		if (!super.validate(modified)) {
			return false;
		}
		
		//check for line interior intersections
		for (CFeature f : modified) {
			if (f.getGeometry() != null && hasLineIntersections(f.getGeometry())) {
				handleError(Messages.INSTANCE.geometryEditor_errorTopologySelfIntersection(), null, false);
				return false;
			}
		}
		
		return true;
	}

	public boolean isEditing(CFeature f) {
		return activeFeature != null && activeFeature.equals(f);
	}
	
}
