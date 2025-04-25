package com.sinergise.gwt.gis.map.ui.basket.action;

import static com.sinergise.gwt.gis.map.ui.basket.res.FeatureBasketResources.BASKET_RESOURCES;

import java.util.Collection;

import com.sinergise.common.gis.feature.CFeature;
import com.sinergise.common.gis.feature.CFeatureUtils;
import com.sinergise.common.gis.map.model.layer.info.FeatureInfoCollection;
import com.sinergise.gwt.gis.map.ui.MapComponent;
import com.sinergise.gwt.gis.map.ui.actions.info.PickFeaturesAction;
import com.sinergise.gwt.gis.map.ui.basket.FeatureBasket;
import com.sinergise.gwt.gis.map.ui.basket.i18n.UiConstants;
import com.sinergise.gwt.gis.map.ui.basket.res.FeatureBasketResources;

public class PickBasketFeaturesAction extends PickFeaturesAction {

	private final FeatureBasket basket;
	
	public PickBasketFeaturesAction(MapComponent map, final FeatureBasket basket) {
		super(map, basket.getFeatureDataLayer());
		this.basket = basket;
		
		setIcon(BASKET_RESOURCES.basket_pick());
		setDescription(UiConstants.BASKET_UI_CONSTANTS.BasketAction_pickFeatures());
		setPickCursor("url('"+FeatureBasketResources.BASKET_RESOURCES.basket_pick_cur().getSafeUri().asString() + "'), auto");
	}

	@Override
	public void gotFeatures(FeatureInfoCollection features) {
		addRemoveFeatures(CFeatureUtils.toFeatureList(features));
	}
	
	private void addRemoveFeatures(Collection<CFeature> features) {
		for (CFeature f : features) {
			if (basket.contains(f)) {
				basket.remove(f);
			} else {
				basket.add(f);
			}
		}
	}
}
