package com.sinergise.gwt.gis.map.ui.basket;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.sinergise.gwt.gis.map.ui.attributes.FeaturesTableView;

public class FeatureBasketTableView extends FeaturesTableView {
	
	public FeatureBasketTableView(FeatureBasket basket) {
		super(basket.getFeatureDataLayer().getDescriptor(), basket, basket.getSelectionModel());
	}
	
	private FeatureBasket basket() {
		return (FeatureBasket) featuresProvider;
	}

	@Override
	protected void init() {
		super.init();
		
		if (cbSelectAll != null) {
			cbSelectAll.setValue(Boolean.valueOf(basket().getAutoSelectOnAdd()));
			cbSelectAll.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
				@Override
				public void onValueChange(ValueChangeEvent<Boolean> event) {
					basket().setAutoSelectOnAdd(cbSelectAll.getValue().booleanValue());
				}
			});
		}
	}
}
