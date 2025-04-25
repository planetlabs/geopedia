package com.sinergise.gwt.gis.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import com.sinergise.common.gis.filter.FilterCapabilities;
import com.sinergise.common.gis.map.model.layer.FeatureDataLayer;
import com.sinergise.common.gis.map.model.layer.LayerTreeElement;
import com.sinergise.common.gis.map.model.layer.MapContextLayers;
import com.sinergise.common.gis.map.model.layer.info.FeatureItemCollector;
import com.sinergise.common.util.collections.tree.TreeVisitor;
import com.sinergise.common.util.event.status.DummyStatusListener;
import com.sinergise.common.util.event.status.StatusListener;

public abstract class AbstractQueryController {

	protected FeatureItemCollector featureCollector;
	protected final Map<String, FeatureDataLayer> queriableLayers = new LinkedHashMap<String, FeatureDataLayer>();

	protected StatusListener statusListener = new DummyStatusListener();
	protected int maxResultsLimit = Integer.MIN_VALUE;
	
	public AbstractQueryController(Collection<? extends FeatureDataLayer> queryLayers, FeatureItemCollector featureCollector) {
		this.featureCollector = featureCollector;
		setQueriableLayers(queryLayers);
	}
	
	public AbstractQueryController(final MapContextLayers mapLayers, FeatureItemCollector featureCollector) {
		this.featureCollector = featureCollector;
		setQueriableLayers(findQueryableLayers(mapLayers));
	}
	
	private Collection<FeatureDataLayer> findQueryableLayers(MapContextLayers mapLayers) {
		final Collection<FeatureDataLayer> queryLayers = new ArrayList<FeatureDataLayer>();
		mapLayers.traverseDepthFirst(new TreeVisitor<LayerTreeElement>() {
			@Override
			public boolean visit(LayerTreeElement node) {
				if (isLayerQueryable(node)) {
					queryLayers.add((FeatureDataLayer)node);
				}
				return true;
			}
		});
		
		return queryLayers;
	}
	
	protected final boolean isLayerQueryable(LayerTreeElement layer) {
		return layer instanceof FeatureDataLayer && isLayerQueryable((FeatureDataLayer) layer);
	}
	
	protected boolean isLayerQueryable(FeatureDataLayer layer) {
		return layer.isFeatureDataQueryEnabled(getMinimalQueryCapabilities());
	}
	
	protected abstract FilterCapabilities getMinimalQueryCapabilities();
	
	protected void setQueriableLayers(Collection<? extends FeatureDataLayer> queryLayers) {
		queriableLayers.clear();
		for (FeatureDataLayer layer : queryLayers) {
			queriableLayers.put(layer.getFeatureTypeName(), layer);
		}
	}
	
	public Collection<FeatureDataLayer> getQueriableLayers() {
		return queriableLayers.values();
	}
	
	public boolean hasStatusListener() {
		return !(statusListener instanceof DummyStatusListener || statusListener == null);
	}
	
	public void setStatusListener(StatusListener statusListener) {
		if (statusListener == null) statusListener = new DummyStatusListener(); //to avoid NPE checking
		this.statusListener = statusListener;
	}
	
	public void setMaxResultsLimit(int limit) {
		maxResultsLimit = limit;
	}
	
	protected abstract void handleError(String msg, Throwable e);

}
