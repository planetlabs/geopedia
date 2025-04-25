package com.sinergise.gwt.gis.map.ui.basket.action;

import static com.sinergise.gwt.gis.map.ui.basket.i18n.UiConstants.BASKET_UI_CONSTANTS;
import static com.sinergise.gwt.gis.map.ui.basket.res.FeatureBasketResources.BASKET_RESOURCES;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.sinergise.common.gis.map.model.layer.FeatureDataLayer;
import com.sinergise.common.gis.map.model.layer.LayerTreeElement;
import com.sinergise.common.ui.action.Action;
import com.sinergise.common.ui.action.ActionSelection;
import com.sinergise.gwt.gis.map.ui.basket.FeatureBasket;
import com.sinergise.gwt.gis.map.ui.basket.FeatureBasketRegistry;
import com.sinergise.gwt.gis.map.ui.basket.FeatureBasketRegistry.FeatureBasketRegistryListener;

public class NewFeatureBasketAction extends ActionSelection {
	
	private final Map<String, Action> newLayerBasketActions = new HashMap<String, Action>();
	
	public NewFeatureBasketAction(final FeatureBasketRegistry registry, Iterable<LayerTreeElement> qualifiedLayers) {
		super("NewFeatureBasketSelection");
		
		setDescription(BASKET_UI_CONSTANTS.newEmptyBasketAction_title());
		setIcon(BASKET_RESOURCES.basket_new());
		
		for (LayerTreeElement layer : qualifiedLayers) {
			if (layer instanceof FeatureDataLayer && layer.isVisible()) {
				final FeatureDataLayer featuresLayer = (FeatureDataLayer) layer;
				
				newLayerBasketActions.put(
					featuresLayer.getFeatureTypeName(),
					new Action(layer.getTitle()) {
						@Override
						protected void actionPerformed() {
							registry.getOrCreateBasket(featuresLayer);
						}
					}	
				);
			}
		}
		
		registry.addListener(new FeatureBasketRegistryListener() {
			
			@Override
			public void onBasketRemoved(FeatureBasket basket) {
				setLayerEnabled(basket.getFeatureType(), true);
			}
			
			@Override
			public void onBasketAdded(FeatureBasket basket) {
				setLayerEnabled(basket.getFeatureType(), false);
			}
			
		});
		
		updateSelections();
	}
	
	private void setLayerEnabled(String featureType, boolean enabled) {
		Action action = newLayerBasketActions.get(featureType);
		if (action != null) {
			action.setExternalEnabled(enabled);
		}
	}
	
	private void updateSelections() {
		Collection<Action> selection = newLayerBasketActions.values();
		setSelections(selection.toArray(new Action[selection.size()]));
	}
	
}