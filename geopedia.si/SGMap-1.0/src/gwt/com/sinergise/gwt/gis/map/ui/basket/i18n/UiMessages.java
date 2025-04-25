package com.sinergise.gwt.gis.map.ui.basket.i18n;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.Messages;

public interface UiMessages extends Messages {

	public static final UiMessages BASKET_UI_MSGS = GWT.create(UiMessages.class);
	
	@DefaultMessage("Total: {0} / Selected: {1}")
	String FeatureBasket_count(int total, int selected);
	@DefaultMessage("Graph. area: {0}")
	String FeatureBasket_graphArea(String formattedArea);
}
