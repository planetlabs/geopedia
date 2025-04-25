package com.sinergise.gwt.gis.map.util;

import static com.sinergise.common.gis.map.model.layer.MapContextLayers.LAYERS_VIEW_ON;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.google.gwt.user.client.Timer;
import com.sinergise.common.gis.map.model.layer.LayerTreeElement;
import com.sinergise.common.gis.map.model.layer.MapContextLayers.RootLayersNode;
import com.sinergise.common.gis.map.model.layer.view.LayersView;
import com.sinergise.common.gis.map.model.layer.view.LayersViewListenerAdapter;
import com.sinergise.gwt.gis.map.ui.MapComponent;
import com.sinergise.gwt.util.history.HistoryHandler;
import com.sinergise.gwt.util.history.HistoryManager;

/**
 * @author tcerovski
 *
 */
public class MapLayersHistoryHandler implements HistoryHandler {
	
	public static final String HISTORY_PARAM_KEY_LAYERS = "layers";
	public static final String HISTORY_PARAM_KEY_LAYERS_DEEP_ON = "layersDeepOn";
	
	public static void bind(MapComponent map) {
		HistoryManager.getInstance().registerHandler(new MapLayersHistoryHandler(map));
	}
	
	private final MapComponent map;
	final HistUpdateTimer updateTimer = new HistUpdateTimer();
	
	private MapLayersHistoryHandler(MapComponent map) {
		this.map = map;
		init();
	}
	
	private void init() {
		final LayersView onLayers = map.getLayers().getNamedLayersView(LAYERS_VIEW_ON);
		onLayers.addLayersViewListener(new LayersViewListenerAdapter() {
			@Override
			public void layerViewChanged() {
				StringBuffer param = new StringBuffer();
				for (LayerTreeElement layer : onLayers) {
					if (shouldIgnoreLayer(layer)) {
						continue;
					}
					
					if (param.length() > 0) {
						param.append(",");
					}
					param.append(layer.getLocalID());
				}
				updateTimer.scheduleUpdate(param.toString(), 350);
			}
		});
	}

	@Override
	public Collection<String> getHandledHistoryParams() {
		return Arrays.asList(HISTORY_PARAM_KEY_LAYERS, HISTORY_PARAM_KEY_LAYERS_DEEP_ON);
	}
	
	protected boolean shouldIgnoreLayer(LayerTreeElement layer) {
		if (layer instanceof RootLayersNode || layer.equals(map.getDefaultHighlightLayer())) {
			return true;
		}
		return false;
	}

	@Override
	public void handleHistoryChange(HistoryManager manager) {
		String layersParam = manager.getHistoryParam(HISTORY_PARAM_KEY_LAYERS);
		boolean deepOn = manager.hasHistoryParam(HISTORY_PARAM_KEY_LAYERS_DEEP_ON);
		manager.removeHistoryParam(HISTORY_PARAM_KEY_LAYERS_DEEP_ON);
		if (layersParam == null) {
			return;
		}
		
		Set<String> onLayers = new HashSet<String>();
		
		for (String layer : layersParam.split(",")) {
			onLayers.add(layer);
		}
		
		for (LayerTreeElement layer : map.getLayers().getFlatLayerList()) {
			if (!shouldIgnoreLayer(layer)) {
				boolean on = onLayers.contains(layer.getLocalID());
				if (deepOn) {
					layer.setDeepOn(on);
				} else {
					layer.setOn(on);
				}
			}
		}
	}
	
	private static class HistUpdateTimer extends Timer {
		
		String paramValue = null;
		
		public HistUpdateTimer() {}

		void scheduleUpdate(String newParamValue, int delayMillis) {
			this.paramValue = newParamValue;
			cancel();
			schedule(delayMillis);
		}
		
		@Override
		public void run() {
			HistoryManager.getInstance().setHistoryParam(HISTORY_PARAM_KEY_LAYERS, paramValue);
		}
	}

}
