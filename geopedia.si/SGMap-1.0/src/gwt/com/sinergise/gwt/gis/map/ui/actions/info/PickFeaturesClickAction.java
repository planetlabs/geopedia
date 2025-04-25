package com.sinergise.gwt.gis.map.ui.actions.info;

import com.sinergise.common.gis.app.ApplicationContext;
import com.sinergise.common.gis.feature.CFeatureDataSource.FeatureCollectionCallback;
import com.sinergise.common.gis.feature.FeatureAccessException;
import com.sinergise.common.gis.filter.BBoxOperation;
import com.sinergise.common.gis.map.model.layer.FeatureDataLayer;
import com.sinergise.common.gis.map.model.layer.Layer;
import com.sinergise.common.gis.map.model.layer.info.FeatureInfoCollection;
import com.sinergise.common.gis.map.model.layer.info.FeatureItemCollector;
import com.sinergise.common.gis.query.Query;
import com.sinergise.common.util.geom.Envelope;
import com.sinergise.common.util.geom.EnvelopeBuilder;
import com.sinergise.gwt.gis.map.ui.MapComponent;
import com.sinergise.gwt.gis.map.ui.actions.MouseClickActionW;

//TODO: refactor and merge duplicated code with PickFeaturesAction
public class PickFeaturesClickAction extends MouseClickActionW {
	
	private static final int DEFAULT_DIST_BUFFER = 2;
	
	private final FeatureDataLayer layer;
	private final MapComponent map;
	private final FeatureCollectionCallback callback;
	
	private double distBuffer = DEFAULT_DIST_BUFFER;
	
	public PickFeaturesClickAction(MapComponent map, FeatureDataLayer layer, final FeatureItemCollector collector) {
		this(map, layer, new FeatureCollectionCallback() {
			
			@Override
			public void onSuccess(FeatureInfoCollection features) {
				if (features.getItemCount() > 0) {
					collector.addAll(features);
				}
			}
			
			@Override
			public void onError(FeatureAccessException error) {
				ApplicationContext.handleAppError("Error while picking features: "+error.getMessage(), error);
			}
			
		});
	}
	
	public PickFeaturesClickAction(MapComponent map, FeatureDataLayer layer, FeatureCollectionCallback callback) {
		super(map.getCoordinateAdapter(), "PickFeature");
		
		this.map = map;
		this.layer = layer;
		this.callback = callback;
	}

	@Override
	protected boolean mouseClickedW(double xWorld, double yWorld) {
		if (((Layer)layer).isOn()) {
			pickAt(xWorld, yWorld);
			return true;
		}
		return false;
	}
		
	protected void pickAt(double xWorld, double yWorld) {
		try {
			layer.getFeaturesSource().queryFeatures(
				new Query[] {preparePickQuery(xWorld, yWorld)}, callback);
		} catch(Exception e) {
			ApplicationContext.handleAppError("Error while picking features: "+e.getMessage(), e);
		}
	}
	
	protected Query preparePickQuery(double xWorld, double yWorld) {
		return new Query(layer.getFeatureTypeName(), new BBoxOperation(createQueryMBR(xWorld, yWorld)));
	}
	
	protected Envelope createQueryMBR(double xWorld, double yWorld) {
		EnvelopeBuilder bld = new EnvelopeBuilder();
		bld.expandToInclude(xWorld, yWorld);
		bld.expandFor(map.coords.worldFromPix.length(distBuffer));
		return bld.getEnvelope();
	}
	
}
