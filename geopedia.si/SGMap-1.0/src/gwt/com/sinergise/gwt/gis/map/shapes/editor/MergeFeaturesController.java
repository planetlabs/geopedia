package com.sinergise.gwt.gis.map.shapes.editor;

import static com.sinergise.common.gis.filter.FilterCapabilities.SCALAR_OP_LOGICAL_AND;
import static com.sinergise.common.gis.filter.FilterCapabilities.SPATIAL_OP_TOUCHES;
import static com.sinergise.gwt.gis.i18n.Messages.UI_MESSAGES;
import static com.sinergise.gwt.gis.map.shapes.editor.GeometryEditorUtil.applyGeometryToACopy;

import java.util.Arrays;
import java.util.Collection;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sinergise.common.geometry.geom.Geometry;
import com.sinergise.common.geometry.service.util.BinaryGeomOpRequest;
import com.sinergise.common.geometry.service.util.GeomOpResult;
import com.sinergise.common.geometry.service.util.GeomUtilService;
import com.sinergise.common.geometry.topo.TopologyException;
import com.sinergise.common.gis.feature.CFeature;
import com.sinergise.common.gis.filter.BBoxOperation;
import com.sinergise.common.gis.filter.ExpressionDescriptor;
import com.sinergise.common.gis.filter.FilterDescriptor;
import com.sinergise.common.gis.filter.GeometryReference;
import com.sinergise.common.gis.filter.Literal;
import com.sinergise.common.gis.filter.LogicalOperation;
import com.sinergise.common.gis.filter.SpatialOperation;
import com.sinergise.common.gis.map.model.layer.FeatureDataLayer;
import com.sinergise.common.gis.map.model.layer.info.FeatureInfoCollection;
import com.sinergise.common.util.geom.Envelope;
import com.sinergise.gwt.gis.map.shapes.editor.GeometryEditorControllerBase.SupportsReset;
import com.sinergise.gwt.gis.map.ui.MapComponent;
import com.sinergise.gwt.gis.map.ui.actions.info.PickFeaturesAction;
import com.sinergise.gwt.util.html.CSS;

public class MergeFeaturesController extends GeometryEditorControllerBase implements SupportsReset {

	public static String PROP_ADJACENT_FEATURE 	= "adjacentFeature";
	
	private final PickAdjacentFeatureAction pickAction;
	

	private CFeature 	activeFeature;
	private CFeature 	adjacentFeature;
	
	private Geometry 	mergedGeometry = null;
	
	public MergeFeaturesController(MapComponent mapComp) {
		super(mapComp);
		this.pickAction = new PickAdjacentFeatureAction(map);
		
		getSelectableForLabelsDisplay().setSelected(false);
	}
	
	public void startEditor(CFeature feature, GeometryEditorCallback callback) {
		setActiveFeatureLayer(feature.getFeatureTypeName());
		
		this.activeFeature = feature;
		this.editorCallback = callback;
		setActive(true);
		
		setAdjacentFeature(null);
	}
	
	@Override
	public void reset() {
		setAdjacentFeature(null);
	}
	
	@Override
	protected void setActive(boolean newActive) {
		boolean oldActive = isActive();
		if (oldActive == newActive) {
			return;
		}
		
		activeSelectable.setSelected(newActive);
		editor.setVisible(newActive);
		pickAction.setSelected(newActive);
		propChangeListeners.fireChange(this, PROP_ACTIVE, Boolean.valueOf(oldActive), Boolean.valueOf(newActive));
		
		map.getDefaultHighlightLayer().setOn(!newActive);
	}
	
	@Override
	public boolean isActive() {
		return editor.isVisible();
	}
	
	@Override
	protected Collection<CFeature> getModifiedFeatureCopies() {
		return Arrays.asList(
			applyGeometryToACopy(activeFeature, getMergedGeometry()),
			applyGeometryToACopy(adjacentFeature, null)
		);
	}
	
	public CFeature getActiveFeature() {
		return activeFeature;
	}
	
	@Override
	public Geometry getActiveGeometry() {
		return mergedGeometry;
	}
	
	
	@Override
	protected void cleanup() {
		super.cleanup();
		activeFeature = null;
		adjacentFeature = null;
	}
	
	private void updateTopology() {
		try {
			topoBuilder.reset();
			if (mergedGeometry != null) {
				topoBuilder.addGeometry(mergedGeometry);
			}
			topoBuilder.lockAll();
			editor.setTopology(topoBuilder.buildTopology());
		} catch (TopologyException e) {
			handleError(UI_MESSAGES.geometryEditor_errorTopology(e.getMessage()), e, true);
		}
	}
	
	private void setMergedGeometry(Geometry geom) {
		this.mergedGeometry = geom;
		updateTopology();
	}
	
	private Geometry getMergedGeometry() {
		return mergedGeometry;
	}
	
	private Geometry getActiveFeatureGeometry() {
		return activeFeature.getGeometry();
	}
	
	private Geometry getAdjacentFeatureGeometry() {
		return adjacentFeature != null ? adjacentFeature.getGeometry() : null;
	}
	
	private void setAdjacentFeature(CFeature feature) {
		final CFeature oldAdjacent = adjacentFeature;
		this.adjacentFeature = feature;
		
		if (adjacentFeature == null) {
			setMergedGeometry(activeFeature.getGeometry());
			propChangeListeners.fireChange(this, PROP_ADJACENT_FEATURE, oldAdjacent, null);
		} else {
			GeomUtilService.INSTANCE.getGeomUnion(
				new BinaryGeomOpRequest(getActiveFeatureGeometry(), getAdjacentFeatureGeometry(), gridSize), 
				new AsyncCallback<GeomOpResult>() {
					
					@Override
					public void onSuccess(GeomOpResult result) {
						setMergedGeometry(result.getResult());
						propChangeListeners.fireChange(this, PROP_ADJACENT_FEATURE, oldAdjacent, adjacentFeature);
					}
					
					@Override
					public void onFailure(Throwable e) {
						handleError(UI_MESSAGES.geometryEditor_errorTopology(e.getMessage()), e, false);
					}
				});
		}
		
	}
	
	protected FilterDescriptor buildAdjacentQueryFilter(Envelope pickBBox) {
		return new LogicalOperation(new ExpressionDescriptor[] {
			new SpatialOperation(new GeometryReference(), SPATIAL_OP_TOUCHES, 
					Literal.newInstance(activeFeature.getGeometry())),
			new BBoxOperation(pickBBox)
		}, SCALAR_OP_LOGICAL_AND);
	}
	
	private class PickAdjacentFeatureAction extends PickFeaturesAction {
		
		public PickAdjacentFeatureAction(MapComponent map) {
			super(map, (FeatureDataLayer)null);
			leftClickAct.setCursor(CSS.CURSOR_DEFAULT);
		}
		
		@Override
		protected FeatureDataLayer getLayer() {
			return map.getLayers().findByFeatureType(activeFeature.getFeatureTypeName());
		}
		
		@Override
		protected FilterDescriptor buildQueryFilter(int x, int y) {
			return buildAdjacentQueryFilter(createQueryMBR(x, y));
		}
		
		@Override
		protected void cancelPick() {
			//don't cancel it
		}
		
		@Override
		public void gotFeatures(FeatureInfoCollection features) {
			if (features.getItemCount() > 0) {
				setAdjacentFeature(features.getItem(0).f);
			}
		}
	}
}
