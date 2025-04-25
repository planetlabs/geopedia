package com.sinergise.gwt.gis.map.ui.basket;

import static com.sinergise.common.util.collections.CollectionUtil.isNullOrEmpty;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.sinergise.common.gis.app.ApplicationContext;
import com.sinergise.common.gis.map.model.layer.FeatureDataLayer;
import com.sinergise.common.gis.map.model.layer.LayerTreeElement;
import com.sinergise.common.gis.map.model.layer.MapContextLayers;
import com.sinergise.common.gis.map.model.layer.view.LayersView;
import com.sinergise.gwt.gis.map.ui.MapComponent;

public class FeatureBasketRegistry {
	
	public interface FeatureBasketRegistryListener {
		void onBasketAdded(FeatureBasket basket);
		void onBasketRemoved(FeatureBasket basket);
	}
	
	
	private static FeatureBasketRegistry instance = null;
	
	public static void initialize() {
		if (isInitialized()) {
			throw new IllegalStateException("FeatureBasketRegistry already initialized");
		}
		instance = new FeatureBasketRegistry();
	}
	
	public static boolean isInitialized() {
		return instance != null && !isNullOrEmpty(instance.getSupportedLayers());
	}
	
	public static FeatureBasketRegistry getInstance() {
		if (!isInitialized()) {
			throw new IllegalStateException("FeatureBasketRegistry not initialized.");
		}
		return instance;
	}
	

	private final Map<String, FeatureBasket> baskets = new LinkedHashMap<String, FeatureBasket>();
	private final List<FeatureBasketRegistryListener> listeners = new ArrayList<FeatureBasketRegistry.FeatureBasketRegistryListener>();
	
	private final FeatureBasketLayersView supportedLayers;
	
	private FeatureBasketRegistry() {
		MapComponent map = (MapComponent) ApplicationContext.getInstance().getPrimaryMap();
		supportedLayers = new FeatureBasketLayersView(map.getLayers());
	}
	
	public LayersView getSupportedLayers() {
		return supportedLayers;
	}
	
	public FeatureBasket getBasket(String featureTypeName) {
		return baskets.get(featureTypeName);
	}
	
	public FeatureBasket getBasket(FeatureDataLayer layer) {
		return getBasket(layer.getFeatureTypeName());
	}
	
	public FeatureBasket getOrCreateBasket(FeatureDataLayer layer) {
		FeatureBasket basket = getBasket(layer);
		if (basket == null) {
			basket = createBasket(layer);
		}
		
		return basket;
	}
	
	public boolean removeBasket(FeatureDataLayer layer) {
		FeatureBasket basket = baskets.remove(layer.getFeatureTypeName()); 
		if (basket != null) {
			fireBasketRemoved(basket);
			return true;
		}
		return false;
	}
	
	public boolean removeBasket(FeatureBasket basket) {
		return removeBasket(basket.getFeatureDataLayer());
	}
	
	public boolean isBasketRegistered(FeatureDataLayer layer) {
		return baskets.containsKey(layer.getFeatureTypeName());
	}
	
	public FeatureBasket createBasket(FeatureDataLayer layer) {
		if (!supportedLayers.match((LayerTreeElement) layer)) {
			return null;
		}
		
		FeatureBasket basket = new FeatureBasket(layer);
		baskets.put(layer.getFeatureTypeName(), basket);
		fireBasketAdded(basket);
		return basket;
	}
	
	public List<FeatureBasket> listBaskets() {
		return new ArrayList<FeatureBasket>(baskets.values());
	}
	
	public void addListener(FeatureBasketRegistryListener listener) {
		listeners.add(listener);
	}
	
	public void removeListener(FeatureBasketRegistryListener listener) {
		listeners.remove(listener);
	}
	
	private void fireBasketAdded(FeatureBasket basket) {
		for (FeatureBasketRegistryListener l : listeners) {
			l.onBasketAdded(basket);
		}
	}
	
	private void fireBasketRemoved(FeatureBasket basket) {
		for (FeatureBasketRegistryListener l : listeners) {
			l.onBasketRemoved(basket);
		}
	}
	
	private static final class FeatureBasketLayersView extends LayersView {
		
		private FeatureBasketLayersView(MapContextLayers layersTree) {
			super(layersTree);
		}
		
		@Override
		protected boolean match(LayerTreeElement node) {
			return FeatureBasket.isBasketEnabledOnLayer(node);
		}
		
		@Override
		protected boolean ignorePropertyChange(LayerTreeElement node, String propertyName) {
			return true;
		}
		
	}

}
