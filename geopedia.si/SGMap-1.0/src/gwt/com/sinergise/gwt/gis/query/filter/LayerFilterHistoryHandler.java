package com.sinergise.gwt.gis.query.filter;

import static com.sinergise.common.util.string.StringUtil.isNullOrEmpty;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.sinergise.common.gis.map.model.layer.FeatureDataLayer;
import com.sinergise.gwt.gis.query.QueryHistoryHandler;
import com.sinergise.gwt.util.history.HistoryHandler;
import com.sinergise.gwt.util.history.HistoryManager;

public class LayerFilterHistoryHandler implements HistoryHandler, LayerFilterListener {
	
	public static final String HISTORY_PARAM_KEY_FILTER = "filter";
	public static final String LAYER_PAIR_SEPARATOR = ":";
	public static final String LAYER_SEPARATOR = "@@";
	
	public static void bind(LayerFilterController filterControl) {
		HistoryManager.getInstance().registerHandler(new LayerFilterHistoryHandler(filterControl));
	}
	
	private final LayerFilterController filterControl;
	
	private LayerFilterHistoryHandler(LayerFilterController filterControl) {
		filterControl.addLayerFilterListener(this);
		this.filterControl = filterControl;
	}

	@Override
	public Collection<String> getHandledHistoryParams() {
		return Arrays.asList(HISTORY_PARAM_KEY_FILTER);
	}

	@Override
	public void handleHistoryChange(HistoryManager manager) {
		String filterStr = manager.getHistoryParam(HISTORY_PARAM_KEY_FILTER);
		if (isNullOrEmpty(filterStr)) return;
		
		//handle special values
		if (QueryHistoryHandler.HISTORY_PARAM_KEY_QUERY.equals(filterStr)) {
			filterStr = manager.getHistoryParam(QueryHistoryHandler.HISTORY_PARAM_KEY_QUERY);
		}
		
		Set<String> filterLayerNames = new HashSet<String>();
		for (FeatureDataLayer layer : filterControl.getFilterableLayers()) {
			filterLayerNames.add(layer.getLocalID());
		}
		
		for (String layerFilterParam : filterStr.split(LAYER_SEPARATOR)) {
			String layerName = layerFilterParam.substring(0, layerFilterParam.indexOf(LAYER_PAIR_SEPARATOR));
			String filterString = layerFilterParam.substring(layerFilterParam.indexOf(LAYER_PAIR_SEPARATOR)+1);
			
			filterControl.setLayerFilter(layerName, LayerFilterUtil.toLayerFilterValuesMap(filterString));
			filterLayerNames.remove(layerName);
		}
		
		//clear other filters
		for (String layerName : filterLayerNames) {
			filterControl.resetFilter(layerName);
		}
	}
	
	@Override
	public void layerFilterSet(String layerName, Map<String, String> filterValues) {
		setLayerFilterValue(layerName, filterValues);
	}
	
	private static void setLayerFilterValue(String layerName, Map<String, String> valuesMap) {
		String filterStr = HistoryManager.getInstance().getHistoryParam(HISTORY_PARAM_KEY_FILTER);
		Map<String, String> layerFilters = new HashMap<String, String>();
		
		if (!isNullOrEmpty(filterStr)) {
			for (String layerFilterParam : filterStr.split(LAYER_SEPARATOR)) {
				String layer = layerFilterParam.substring(0, layerFilterParam.indexOf(LAYER_PAIR_SEPARATOR));
				String filterValue = layerFilterParam.substring(layerFilterParam.indexOf(LAYER_PAIR_SEPARATOR)+1);
				if (!isNullOrEmpty(filterValue)) layerFilters.put(layer, filterValue); 
			}
		}
		layerFilters.put(layerName, LayerFilterUtil.toLayerFilterString(valuesMap));
		
		StringBuffer sb = new StringBuffer();
		int cnt = 0;
		for (String layer : layerFilters.keySet()) {
			if (cnt++ > 0) sb.append(LAYER_SEPARATOR);
			sb.append(layer).append(LAYER_PAIR_SEPARATOR).append(layerFilters.get(layer));
		}
		
		HistoryManager.getInstance().setHistoryParam(HISTORY_PARAM_KEY_FILTER, sb.toString());
	}
	
	

}
