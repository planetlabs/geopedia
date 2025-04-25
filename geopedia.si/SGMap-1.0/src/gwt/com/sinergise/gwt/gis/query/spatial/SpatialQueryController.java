package com.sinergise.gwt.gis.query.spatial;

import static com.sinergise.common.gis.filter.FilterCapabilities.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sinergise.common.geometry.geom.Geometry;
import com.sinergise.common.geometry.property.GeometryProperty;
import com.sinergise.common.gis.feature.CFeatureDataSource;
import com.sinergise.common.gis.feature.CFeatureDataSource.FeatureCollectionCallback;
import com.sinergise.common.gis.feature.FeatureAccessException;
import com.sinergise.common.gis.filter.ElementDescriptor;
import com.sinergise.common.gis.filter.FilterCapabilities;
import com.sinergise.common.gis.filter.GeometryReference;
import com.sinergise.common.gis.filter.InvalidFilterDescriptorException;
import com.sinergise.common.gis.filter.Literal;
import com.sinergise.common.gis.filter.SpatialOperation;
import com.sinergise.common.gis.map.model.layer.FeatureDataLayer;
import com.sinergise.common.gis.map.model.layer.LayerTreeElement;
import com.sinergise.common.gis.map.model.layer.info.FeatureInfoCollection;
import com.sinergise.common.gis.map.model.layer.info.FeatureItemCollector;
import com.sinergise.common.gis.query.Query;
import com.sinergise.gwt.gis.i18n.Messages;
import com.sinergise.gwt.gis.map.ui.MapComponent;
import com.sinergise.gwt.gis.query.AbstractQueryController;

public class SpatialQueryController extends AbstractQueryController {

	public static final FilterCapabilities MINIMAL_QUERY_CAPS 
		= new FilterCapabilities(SPATIAL_OP_INTERSECT);

	private final Logger logger = LoggerFactory.getLogger(SpatialQueryController.class);
	
	private final MapComponent map;

	public SpatialQueryController(MapComponent map, FeatureItemCollector featureCollector) {
		super(map.getLayers(), featureCollector);
		this.map = map;
	}
	
	public SpatialQueryController(MapComponent map, Collection<? extends FeatureDataLayer> queryLayers, FeatureItemCollector featureCollector) {
		super(queryLayers, featureCollector);
		this.map = map;
	}
	
	@Override
	protected FilterCapabilities getMinimalQueryCapabilities() {
		return MINIMAL_QUERY_CAPS;
	}
	
	@Override
	protected void handleError(String msg, Throwable e) {
		logger.error(msg, e);
		statusListener.setErrorStatus(msg);
	}
	
	public void executeQuery(Geometry geom, int operation) {
		executeQuery(Literal.newInstance(new GeometryProperty(geom)), operation);
	}
	
	protected boolean isLayerEligibleForQuery(FeatureDataLayer layer) {
		return ((LayerTreeElement)layer).hasAnythingToRender(map.getCoordinateAdapter());
	}
	
	public void executeQuery(ElementDescriptor geomElement, int operation) {
		
		//use only rendered layers that support the operation and group group queries by source
		Map<CFeatureDataSource, List<Query>> srcQueries = new HashMap<CFeatureDataSource, List<Query>>();
		for (FeatureDataLayer layer : queriableLayers.values()) {
			if (isLayerEligibleForQuery(layer)
				&& layer.getFilterCapabilities().supportsOperation(operation)) 
			{
				List<Query> queries = srcQueries.get(layer.getFeaturesSource());
				if (queries == null) {
					srcQueries.put(layer.getFeaturesSource(), queries = new ArrayList<Query>());
				}
				try {
					Query q = new Query(layer.getFeatureTypeName(), new SpatialOperation(
						new GeometryReference(), operation, geomElement));
					q.setMaxResults(maxResultsLimit);
					queries.add(q);
				} catch(InvalidFilterDescriptorException e) {
					handleError(e.getMessage(), e);
				}
			}
		}
		
		featureCollector.clearFeatures();
		statusListener.clearStatus();
		
		FeatureCollectionCallback callback = new SpatialQueryCallback(srcQueries.size());
		for (CFeatureDataSource src : srcQueries.keySet()) {
			List<Query> queries = srcQueries.get(src);
			try {
				src.queryFeatures(queries.toArray(new Query[queries.size()]), callback);
			} catch(FeatureAccessException e) {
				callback.onError(e);
			}
		}
		
	}
	

	private class SpatialQueryCallback implements FeatureCollectionCallback {
		
		final int sourcesCount;
		int sourcesBack = 0;
		int featuresCount = 0;
		int featuresHits = 0;
		
		SpatialQueryCallback(int sourcesCount) {
			this.sourcesCount = sourcesCount;
		}
		
		@Override
		public void onSuccess(FeatureInfoCollection features) {
			sourcesBack++;
			featuresCount += features.getItemCount();
			featuresHits += features.getHitCount();
			featureCollector.addAll(features);
			
			if (sourcesCount == sourcesBack) { //all queries are back
				if (featuresCount < 1) {
					statusListener.setInfoStatus(Messages.INSTANCE.featureQuerier_noQueryResultsFound());
				} else {
					if (featuresCount < featuresHits) {
						if (featuresHits - featuresCount <= sourcesBack) {
							statusListener.setInfoStatus(Messages.INSTANCE.featureQuerier_tooManyQueryResultsUnknownTotalCount(featuresCount));
						} else {
							statusListener.setInfoStatus(Messages.INSTANCE.featureQuerier_tooManyQueryResults(featuresCount, featuresHits));
						}
					} 
				}
			}
		}
		
		@Override
		public void onError(FeatureAccessException e) {
			handleError(Messages.INSTANCE.featureQuerier_errorOnQuery(e.getMessage()), e);
		}
	}
	
}
