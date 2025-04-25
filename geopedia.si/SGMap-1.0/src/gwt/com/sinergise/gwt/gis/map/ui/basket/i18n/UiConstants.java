package com.sinergise.gwt.gis.map.ui.basket.i18n;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.Constants;

public interface UiConstants extends Constants {

	public static final UiConstants BASKET_UI_CONSTANTS = GWT.create(UiConstants.class);
	
	@DefaultStringValue("Basket")
	String basketTab_title();
	
	@DefaultStringValue("New empty basket")
	String newEmptyBasketAction_title();
	
	@DefaultStringValue("Parcel basket")
	String Features2BasketAction_tooltip();
	@DefaultStringValue("Add to basket")
	String Features2BasketAction_add();
	@DefaultStringValue("Remove from basket")
	String Features2BasketAction_remove();
	@DefaultStringValue("Retain in basket")
	String Features2BasketAction_retain();
	
	@DefaultStringValue("Selected features")
	String BasketAction_selectedFeatures();
	@DefaultStringValue("Remove selected items")
	String BasketAction_remove();
	@DefaultStringValue("Retain selected items")
	String BasketAction_retain();
	@DefaultStringValue("Clear")
	String BasketAction_clear();
	@DefaultStringValue("Add/remove parcels from the map")
	String BasketAction_pickFeatures();
	
	@DefaultStringValue("The basket is empty")
	String isEmpty();

}
