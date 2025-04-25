package com.sinergise.gwt.gis.map.shapes.editor;

import static com.sinergise.common.gis.feature.CFeatureUtils.toFeatureList;
import static com.sinergise.common.gis.filter.FilterCapabilities.SCALAR_OP_LOGICAL_OR;
import static com.sinergise.common.gis.filter.FilterCapabilities.SPATIAL_OP_TOUCHES;
import static com.sinergise.gwt.gis.i18n.Messages.UI_MESSAGES;
import static com.sinergise.gwt.gis.map.shapes.editor.GeometryEditorUtil.applyGeometryToACopy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.sinergise.common.geometry.geom.Geometry;
import com.sinergise.common.geometry.topo.Face;
import com.sinergise.common.geometry.topo.ITopoMap;
import com.sinergise.common.geometry.topo.TopoBuilder;
import com.sinergise.common.geometry.topo.TopologyException;
import com.sinergise.common.gis.feature.CFeature;
import com.sinergise.common.gis.feature.CFeatureDataSource.FeatureCollectionCallback;
import com.sinergise.common.gis.feature.CFeatureIdentifier;
import com.sinergise.common.gis.feature.FeatureAccessException;
import com.sinergise.common.gis.filter.ExpressionDescriptor;
import com.sinergise.common.gis.filter.FilterDescriptor;
import com.sinergise.common.gis.filter.GeometryReference;
import com.sinergise.common.gis.filter.IdentifierOperation;
import com.sinergise.common.gis.filter.Literal;
import com.sinergise.common.gis.filter.LogicalOperation;
import com.sinergise.common.gis.filter.SpatialOperation;
import com.sinergise.common.gis.map.model.layer.FeatureDataLayer;
import com.sinergise.common.gis.map.model.layer.info.FeatureInfoCollection;
import com.sinergise.common.gis.query.QueryUtil;
import com.sinergise.gwt.gis.map.ui.MapComponent;

public class TopoEditorController extends GeometryEditorController {

	protected Set<CFeature> features = new HashSet<CFeature>();
	
	public TopoEditorController(MapComponent map) {
		super(map);
	}
	
	private boolean isTopologyLayer(String featureTypeName) {
		FeatureDataLayer layer = map.getLayers().findByFeatureType(featureTypeName);
		return layer != null && (layer.getTopoType() & FeatureDataLayer.TYPE_TOPOLOGY) > 0;
	}
	
	@Override
	public void editFeatureGeometry(CFeature feature, GeometryEditorCallback callback) {
		if (isTopologyLayer(feature.getFeatureTypeName())) {
			editFeatureTopology(feature, callback);
		} else {
			super.editFeatureGeometry(feature, callback);
		}
	}
	
	@Override
	public void editFeatureGeometry(CFeatureIdentifier featureId, GeometryEditorCallback callback) {
		if (isTopologyLayer(featureId.getFeatureTypeName())) {
			editFeatureTopology(featureId, callback);
		} else {
			super.editFeatureGeometry(featureId, callback);
		}
	}
	
	public void editFeatureTopology(CFeatureIdentifier featureId, final GeometryEditorCallback callback) {
		this.editorCallback = callback;
		//TODO: check if already editing
		
		setActiveFeatureLayer(featureId.getFeatureTypeName());
		
		//fetch feature geometry
		QueryUtil.queryFeatureGeometry(activeFeatureLayer, featureId, new FeatureCollectionCallback() {
			@Override
			public void onSuccess(FeatureInfoCollection result) {
				if (result.getItemCount() == 1) {
					editFeatureTopology(result.getItem(0).f, callback);
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
	
	public void editFeatureTopology(final CFeature feature, GeometryEditorCallback callback) {
		this.editorCallback = callback;
		setActiveFeatureLayer(feature.getFeatureTypeName());
		
		//fetch feature and adjacent features
		QueryUtil.queryFeatures(activeFeatureLayer, 
			buildTopologyQueryFilter(feature), 
			new FeatureCollectionCallback() {
				@Override
				public void onSuccess(FeatureInfoCollection result) {
					if (result.getItemCount() > 0) {
						editTopology(feature.getQualifiedID(), toFeatureList(result));
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
	
	protected void editTopology(CFeatureIdentifier activeFeatureId, Collection<CFeature> topoFeatuers) {
		features.clear();
		
		for (CFeature f : topoFeatuers) {
			if (f.getQualifiedID().equals(activeFeatureId)) {
				setActiveFeature(f);
			} else {
				features.add(f);
			}
		}
		
		startTopologyEdit();
	}
	
	@Override
	protected ITopoMap buildInitialTopology() throws TopologyException {
		topoBuilder.reset();
		appendAdjacentFeatures(topoBuilder);
		appendActiveFeature(topoBuilder);
		
		return topoBuilder.buildTopology();
	}
	
	private void appendAdjacentFeatures(TopoBuilder builder) {
		for (CFeature f : features) {
			Face face = builder.addPolygon(f);
			builder.lockFaceBoudnary(face);
		}
	}
	
	private void appendActiveFeature(TopoBuilder builder) {
		Face activeFace = builder.addPolygon(activeFeature);
		builder.unlockFaceBoundary(activeFace);
	}
	
	protected FilterDescriptor buildTopologyQueryFilter(CFeature feature) {
		return new LogicalOperation(
			new ExpressionDescriptor[] {
				new IdentifierOperation(feature.getLocalID()),
				new SpatialOperation(new GeometryReference(), 
					SPATIAL_OP_TOUCHES, Literal.newInstance(feature.getGeometry()))
			},
			SCALAR_OP_LOGICAL_OR
		);
	}
	
	@Override
	public void clearActiveGeometry() {
		try {
			topoBuilder.reset();
			appendAdjacentFeatures(topoBuilder);
			
			editor.topoReset();
			editor.setTopology(topoBuilder.buildTopology());
		} catch (TopologyException e) {
			handleError(UI_MESSAGES.geometryEditor_errorTopology(e.getMessage()), e, true);
		}
	}
	
	@Override
	protected void cleanup() {
		super.cleanup();
		features.clear();
	}
	
	@Override
	protected Collection<CFeature> getModifiedFeatureCopies() {
		try {
			List<CFeature> modified = new ArrayList<CFeature>(features.size()+1);
			
			modified.add(applyGeometryToACopy(getActiveFeature(), getActiveGeometry()));
			for (CFeature f : features) {
				Geometry geom = parseForTopoType(polyBuilder.buildPolygonFor(f));
				modified.add(applyGeometryToACopy(f, geom));
			}
			
			return modified;
			
		} catch (TopologyException e) {
			handleError(UI_MESSAGES.geometryEditor_errorTopology(e.getMessage()), e, false);
			return null;
		}
	}

}
