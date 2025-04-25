package com.sinergise.gwt.gis.map.ui.actions.info;

import com.sinergise.common.gis.feature.CFeatureDataSource.FeatureCollectionCallback;
import com.sinergise.common.gis.map.model.layer.FeatureDataLayer;
import com.sinergise.common.gis.map.model.layer.info.FeatureItemCollector;
import com.sinergise.common.gis.query.Query;
import com.sinergise.gwt.gis.map.ui.MapComponent;

public class PickSingleFeatureClickAction extends PickFeaturesClickAction {

	public PickSingleFeatureClickAction(MapComponent map, FeatureDataLayer layer, FeatureItemCollector collector) {
		super(map, layer, collector);
	}

	public PickSingleFeatureClickAction(MapComponent map, FeatureDataLayer layer, FeatureCollectionCallback callback) {
		super(map, layer, callback);
	}

	@Override
	protected Query preparePickQuery(double xWorld, double yWorld) {
		Query query = super.preparePickQuery(xWorld, yWorld);
		query.setMaxResults(1);
		return query;
	}
	
}
