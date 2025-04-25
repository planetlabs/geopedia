package com.sinergise.gwt.gis.map.ui.basket.action;

import static com.sinergise.gwt.gis.map.ui.basket.i18n.UiConstants.BASKET_UI_CONSTANTS;
import static com.sinergise.gwt.gis.map.ui.basket.res.FeatureBasketResources.BASKET_RESOURCES;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sinergise.common.gis.app.ApplicationContext;
import com.sinergise.common.gis.feature.CFeature;
import com.sinergise.common.gis.feature.HasFeatureRepresentations;
import com.sinergise.common.gis.feature.RepresentsFeature;
import com.sinergise.common.gis.map.model.layer.FeatureDataLayer;
import com.sinergise.common.ui.action.Action;
import com.sinergise.common.ui.action.ActionSelection;
import com.sinergise.gwt.gis.map.ui.MapComponent;
import com.sinergise.gwt.gis.map.ui.attributes.FeatureActionsProvider;
import com.sinergise.gwt.gis.map.ui.basket.FeatureBasket;
import com.sinergise.gwt.gis.map.ui.basket.FeatureBasketRegistry;

public class Features2BasketActionSelection extends ActionSelection {

	public Features2BasketActionSelection(FeatureBasketRegistry basketsRegistry, HasFeatureRepresentations featuresProvider) {
		super("Features2BasketActionSelection");
		
		setDescription(BASKET_UI_CONSTANTS.Features2BasketAction_tooltip());
		setIcon(BASKET_RESOURCES.basket_to_list());
		
		setSelections(new Action[] {
			new Features2BasketActionAdd(basketsRegistry, featuresProvider),
			new Features2BasketActionRemove(basketsRegistry, featuresProvider),
			new Features2BasketActionRetain(basketsRegistry, featuresProvider)
		});
	}
	

	private static FeatureDataLayer findFeatureLayer(String featureType) {
		return ((MapComponent) ApplicationContext.getInstance().getPrimaryMap()).getLayers().findByFeatureType(featureType);
	}
	
	public static class Features2BasketActionProvider implements FeatureActionsProvider {
		@Override
		public List<? extends Action> getFeatureActions(HasFeatureRepresentations features, Object requestor) {
			if (FeatureBasketRegistry.isInitialized()) {
				return Collections.singletonList(new Features2BasketActionSelection(
					FeatureBasketRegistry.getInstance(), features));
			}
			
			return Collections.emptyList();
		}
	}
	
	private abstract static class Features2BasketAbstractAction extends Action {
		
		final FeatureBasketRegistry registry;
		final HasFeatureRepresentations featuresProvider;
		
		Features2BasketAbstractAction(String name, FeatureBasketRegistry registry, HasFeatureRepresentations featuresProvider) {
			super(name);
			this.registry = registry;
			this.featuresProvider = featuresProvider;
		}
	
		Map<String, List<CFeature>> getFeaturesByType() {
			Map<String, List<CFeature>> featuresMap = new HashMap<String, List<CFeature>>();
			for (RepresentsFeature repF : featuresProvider.getFeatures()) {
				if (!(repF instanceof CFeature)) {
					continue;
				}
				final String type = ((CFeature)repF).getFeatureTypeName();
				
				List<CFeature> list = featuresMap.get(type);
				if (list == null) {
					featuresMap.put(type, list = new ArrayList<CFeature>());
				}
				list.add(((CFeature)repF));
			}
			
			return featuresMap;
		}
		
		@Override
		protected void actionPerformed() {
			Map<String, List<CFeature>> featuresMap = getFeaturesByType();
			for (String featureType : featuresMap.keySet()) {
				actionPerformedOnFeatureLayer(findFeatureLayer(featureType), featuresMap.get(featureType));
			}
		}
		
		protected abstract void actionPerformedOnFeatureLayer(FeatureDataLayer layer, Collection<CFeature> features);
		
	}
	
	private static class Features2BasketActionAdd extends Features2BasketAbstractAction {
		
		Features2BasketActionAdd(FeatureBasketRegistry registry, HasFeatureRepresentations featuresProvider) {
			super("Features2BasketActionAdd", registry, featuresProvider);
			
			setDescription(BASKET_UI_CONSTANTS.Features2BasketAction_add());
			setIcon(BASKET_RESOURCES.basket_to());
		}
		
		@Override
		protected void actionPerformedOnFeatureLayer(FeatureDataLayer layer, Collection<CFeature> features) {
			registry.getOrCreateBasket(layer).addAll(features);
		}
		
	}
	
	private static class Features2BasketActionRemove extends Features2BasketAbstractAction {
		
		Features2BasketActionRemove(FeatureBasketRegistry registry, HasFeatureRepresentations featuresProvider) {
			super("Features2BasketActionRemove", registry, featuresProvider);
			
			setDescription(BASKET_UI_CONSTANTS.Features2BasketAction_remove());
			setIcon(BASKET_RESOURCES.basket_remove());
		}
		
		@Override
		protected void actionPerformedOnFeatureLayer(FeatureDataLayer layer, Collection<CFeature> features) {
			FeatureBasket basket = registry.getBasket(layer);
			if (basket != null) {
				basket.removeAll(features);
			}
		}
		
	}
	
	private static class Features2BasketActionRetain extends Features2BasketAbstractAction {
		
		Features2BasketActionRetain(FeatureBasketRegistry registry, HasFeatureRepresentations featuresProvider) {
			super("Features2BasketActionRetain", registry, featuresProvider);
			
			setDescription(BASKET_UI_CONSTANTS.Features2BasketAction_retain());
			setIcon(BASKET_RESOURCES.basket_retain());
		}
		
		@Override
		protected void actionPerformedOnFeatureLayer(FeatureDataLayer layer, Collection<CFeature> features) {
			FeatureBasket basket = registry.getBasket(layer);
			if (basket != null) {
				basket.retainAll(features);
			}
		}
		
	}
	
	
	
}
