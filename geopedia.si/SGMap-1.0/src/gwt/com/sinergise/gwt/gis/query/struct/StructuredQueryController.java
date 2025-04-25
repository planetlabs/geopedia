package com.sinergise.gwt.gis.query.struct;

import static com.sinergise.common.gis.filter.FilterCapabilities.LOGICAL_BINARY_OPS;
import static com.sinergise.common.gis.filter.FilterCapabilities.SCALAR_OP_COMP_EQUALTO;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sinergise.common.gis.feature.CFeatureUtils;
import com.sinergise.common.gis.feature.FeatureAccessException;
import com.sinergise.common.gis.feature.descriptor.CFeatureDescriptor;
import com.sinergise.common.gis.filter.FilterCapabilities;
import com.sinergise.common.gis.map.model.layer.FeatureDataLayer;
import com.sinergise.common.gis.map.model.layer.LayerTreeElement;
import com.sinergise.common.gis.map.model.layer.info.FeatureInfoCollection;
import com.sinergise.common.gis.map.model.layer.info.FeatureInfoItem;
import com.sinergise.common.gis.map.model.layer.info.FeatureItemCollector;
import com.sinergise.common.gis.map.model.layer.info.SingleFeatureCollection;
import com.sinergise.common.gis.query.Query;
import com.sinergise.gwt.gis.i18n.Messages;
import com.sinergise.gwt.gis.map.ui.MapComponent;
import com.sinergise.gwt.gis.map.ui.actions.info.FeatureInfoUtilGWT;
import com.sinergise.gwt.gis.query.AbstractQueryController;
import com.sinergise.gwt.gis.query.FeatureQuerier;
import com.sinergise.gwt.gis.query.QueryHistoryHandler;
import com.sinergise.gwt.gis.query.QueryHistoryListener;
import com.sinergise.gwt.gis.query.struct.cond.QueryConditionFactory;
import com.sinergise.gwt.ui.maingui.ILoadingWidget;

/**
 * Control for preparing and executing structured queries.
 * 
 * @author tcerovski
 */
public class StructuredQueryController extends AbstractQueryController implements QueryHistoryListener {
	
	public static final FilterCapabilities MINIMAL_QUERY_CAPS 
		 = new FilterCapabilities(SCALAR_OP_COMP_EQUALTO | LOGICAL_BINARY_OPS);
	
	private final Logger logger = LoggerFactory.getLogger(StructuredQueryController.class);

	final MapComponent map;
	protected QueryConditionFactory conditionFactory;
	private Map<String, StructuredLayerQueryBuilder> layerQueryBuilders;
	
	private FeatureQuerier querier = new FeatureQuerier();
	private boolean boundWithHistory = false;
	boolean ensureResultsVisibility = true;
	
	public StructuredQueryController(MapComponent map, FeatureItemCollector featureCollector) {
		super(map.getLayers(), featureCollector);
		this.map = map;
		this.featureCollector = new WrappedResultsCollector(featureCollector);
	}
	
	public StructuredQueryController(MapComponent map, Collection<? extends FeatureDataLayer> queryLayers, FeatureItemCollector featureCollector) {
		super(queryLayers, featureCollector);
		this.map = map;
		this.featureCollector = new WrappedResultsCollector(featureCollector);
	}
	
	@Override
	protected boolean isLayerQueryable(FeatureDataLayer layer) {
		return super.isLayerQueryable(layer)
			&& getLayerQueryBuilder(layer).hasConditions();
	}
	
	@Override
	protected FilterCapabilities getMinimalQueryCapabilities() {
		return MINIMAL_QUERY_CAPS;
	}
	
	public void bindWithHistory() {
		QueryHistoryHandler.bind(this);
		boundWithHistory = true;
	}
	
	public void setQueryConditionFactory(QueryConditionFactory factory) {
		this.conditionFactory = factory;
	}
	
	public StructuredLayerQueryBuilder getLayerQueryBuilder(final FeatureDataLayer layer) {
		if (layerQueryBuilders == null) {
			layerQueryBuilders = new HashMap<String, StructuredLayerQueryBuilder>();
		}
		
		StructuredLayerQueryBuilder builder = layerQueryBuilders.get(layer.getFeatureTypeName());
		
		if (builder == null) {
			builder = createQueryBuilder(layer.getDescriptor(), layer.getFilterCapabilities());
			layerQueryBuilders.put(layer.getFeatureTypeName(), builder);
		}
		
		return builder;
	}
	
	protected StructuredLayerQueryBuilder createQueryBuilder(CFeatureDescriptor fDesc, FilterCapabilities capabilities) {
		return new StructuredLayerQueryBuilder(fDesc, capabilities, getConditionFactory());
	}
	
	protected void doExecuteQuery(FeatureDataLayer layer, Query query) {
		if (query == null) return;
		
		statusListener.clearStatus();
		try {
			featureCollector.clearFeatures();
			if (query.getMaxResults() == Integer.MIN_VALUE) {
				query.setMaxResults(maxResultsLimit);
			}
			querier.executeQuery(layer.getFeaturesSource(), new Query[]{query}, featureCollector, statusListener);
		} catch (FeatureAccessException e) {
			handleError(Messages.INSTANCE.featureQuerier_errorOnQuery(e.getMessage()), e);
		}
	}

	public void setLoadingWidget(ILoadingWidget loading) {
		if (querier != null)
			querier.setLoadingWidget(loading);
	}

	@Override
	public void executeQuery(String featureType, final Map<String, String> valuesMap) {
		final FeatureDataLayer layer = queriableLayers.get(featureType);
		if (layer == null) {
			return;
		}

		StructuredLayerQueryBuilder queryBuilder = getLayerQueryBuilder(layer);
		if (valuesMap != null) {
			queryBuilder.setQueryFieldValues(valuesMap, true);
		}
		queryBuilder.buildQuery(valuesMap != null && valuesMap.isEmpty(), new AsyncCallback<Query>() {
			@Override
			public void onSuccess(Query query) {
				doExecuteQuery(layer, query);
			}
			
			@Override
			public void onFailure(Throwable caught) {
				handleError(caught.getMessage(), caught);
			}
		});
	}
	
	public void executeQuery(String featureType) {
		if (boundWithHistory && layerQueryBuilders.containsKey(featureType)) {
			//execute will be called from history change
			//clear param values to execute the search again if no parameters were changed
			QueryHistoryHandler.setQueryParamValue(null, null);
			QueryHistoryHandler.setQueryParamValue(featureType, layerQueryBuilders.get(featureType).getQueryFieldValues());
		} else {
			executeQuery(featureType, null);
		}
	}
	
	public void setEnsureResultsVisiblity(boolean b) {
		this.ensureResultsVisibility = b;
	}
	
	@Override
	protected void handleError(String msg, Throwable e) {
		logger.error(msg, e);
		statusListener.setErrorStatus(msg);
	}
	
	private class WrappedResultsCollector implements FeatureItemCollector {
		
		final FeatureItemCollector wrapped;
		
		WrappedResultsCollector(FeatureItemCollector wrapped) {
			this.wrapped = wrapped;
		}
		
		@Override
		public void add(FeatureInfoItem feature) {
			wrapped.add(feature);
			ensureVisibility(new SingleFeatureCollection(feature));
		}
		
		@Override
		public void addAll(FeatureInfoCollection features) {
			wrapped.addAll(features);
			ensureVisibility(features);
		}
		
		@Override
		public void clearFeatures() {
			wrapped.clearFeatures();
		}
		
		void ensureVisibility(FeatureInfoCollection features) {
			if (ensureResultsVisibility) {
				FeatureInfoUtilGWT.ensureVisible(CFeatureUtils.getMBR(features), map, 6);
			}
		}
	}
	
	protected QueryConditionFactory getConditionFactory() {
		if (conditionFactory == null) {
			conditionFactory = new QueryConditionFactory();
		}
		return conditionFactory;
	}
	
	public static final boolean isStructuredQuerySupported(LayerTreeElement elem) {
		return FeatureDataLayer.Util.isFeatureDataQueryEnabled(elem, MINIMAL_QUERY_CAPS);
	}
}
